package com.mehrsoft.facade.rest;

import com.codahale.metrics.annotation.Timed;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mehrsoft.dao.BaseDao;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.apache.cxf.common.util.CollectionUtils;
import org.jvnet.hk2.component.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Created by ijet on 8/26/16.
 */
@Path("roomRatesearch")
@Singleton
public class RoomSearch extends BaseRoomSearch {
    private final static Logger log = LoggerFactory.getLogger(RoomSearch.class);
    private final static boolean USE_JAVA_FOR_SEARCH = false;

    @Inject
    BaseDao dao;


    @Path("searchandmodel/room")
    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResultAndModel roomSearchWithModel(@QueryParam(value = "hotelId") Long id, @QueryParam(value = "from") String fromDateString,
                                                                     @QueryParam(value = "to") String toDateString,
                                                                     @QueryParam(value = "inv") int inv,
                                                                     @QueryParam(value = "room") List<String> room,
                                                                     @QueryParam(value = "mixRooms") Boolean mixRoom
    ) {

        List<? extends AggregateHotelRoomAvailability> aggs = roomSearch(id, fromDateString, toDateString, inv, room, mixRoom);
        SearchResultAndModel res = new SearchResultAndModel(aggs);
//        addVariable("brit", ImmutableMap.of("capacity", 20000,
//                "plane", 1,
//                "person", 8,
//                "cost", 5000));
//        addVariable("yank",  ImmutableMap.of("capacity", 30000,
//                "plane", 1,
//                "person", 16,
//                "cost", 9000));
//
//        addConstraints("plane", ImmutableMap.of("max", 44));
//        addConstraints("person", ImmutableMap.of("max", 512));
//        addConstraints("cost", ImmutableMap.of("max", 300000));

        res.getModel().setOptimize("cost");
        res.getModel().setOpType("min");
        res.getModel().addConstraints("room", ImmutableMap.of("min", inv));  // fulfill the max number of rooms
        for (AggregateHotelRoomAvailability agg : aggs) {
            String r = agg.getId() + "_cnt";
            res.getModel().addConstraints(r, ImmutableMap.of("max", agg.getMaxBookingLimit()));
            res.getModel().addVariable(agg.getId(), ImmutableMap.of(r, 1,   "room", 1,  // each inventory is one room under the name of the room
                    "cost", agg.getTotalPrice()));

            //TODO for each room day find the total max and make sure there is constrant for it.  Each rate should have one count of the room type for each day
        }


        return res;

    }
        @Path("search/room")
    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<? extends AggregateHotelRoomAvailability> roomSearch(@QueryParam(value = "hotelId") Long id, @QueryParam(value = "from") String fromDateString,
                                                                     @QueryParam(value = "to") String toDateString,
                                                                     @QueryParam(value = "inv") int inv,
                                                                     @QueryParam(value = "room") List<String> room,
                                                                     @QueryParam(value = "mixRooms") Boolean mixRoom
    ) {

        try {
            LocalDate from = LocalDate.parse(fromDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate to = LocalDate.parse(toDateString, DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("Singe Room/Rate search for id {} from  {} to {}  ", id, from, to);

            log.info("roomSearch() called with: " + "id = [" + id + "], fromDateString = [" + fromDateString + "], toDateString = [" + toDateString + "], inv = [" + inv + "], room = [" + room + "], mixRoom = [" + mixRoom + "]");
            Observable<HotelRoomAvailability> allRecords = getHotelRoomAvailabilityByDateRange(id, from, to, inv);

            Map<Long, HotelRoomAvailability> allAvailByIdMap = allRecords.toMap(new Func1<HotelRoomAvailability, Long>() {
                @Override
                public Long call(HotelRoomAvailability h) {
                    return h.getId();
                }
            }).toBlocking().single();

            Map<LocalDate, Collection<HotelRoomAvailability>> allByDate = allRecords.toMultimap(new Func1<HotelRoomAvailability, LocalDate>() {
                @Override
                public LocalDate call(HotelRoomAvailability h) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(h.getDate().getTime()), ZoneId.systemDefault())
                            .toLocalDate();
                }
            }).toBlocking().single();
            System.out.println(">>>>>>>>>.allByDatesize = " + allByDate.size() + " duration: " + ChronoUnit.DAYS.between(from, to));

            if (allByDate.size() < ChronoUnit.DAYS.between(from, to) + 1)
                return new ArrayList<>();  // 1-1-16 -> 1-1-16 is considered one night stay

            Observable<AggregateHotelRoomAvailability> srcResult;

            if (USE_JAVA_FOR_SEARCH) {
                srcResult = searchForRoomsInJava(id, from, to, inv, allAvailByIdMap, room, mixRoom);

            } else {
                srcResult = searchForRooms(id, from, to, inv, allAvailByIdMap, room, mixRoom);
            }
            List<? extends AggregateHotelRoomAvailability> availabilities = srcResult
                    .filter(x -> hasValidRoom(x, room))
                    .map(x -> new AggregateHotelRoomAvailability(applyFreeNightPolicy(x.getHras())))
                    .toList().toBlocking().single();


            log.info("Search response size: {}", availabilities.size());
            //  System.out.println(">>>> RES " + availabilities + "\n by id map " + allAvailByIdMap);
            return availabilities;

        } catch (Exception e) {
            log.error("Failed in  search", e);
            throw new RuntimeException(e);
        }
    }

    private Observable<HotelRoomAvailability> getHotelRoomAvailabilityByDateRange(Long hotelId, LocalDate from, LocalDate to, int minInv) {
        String databaseTableName = "v1.hra_upsert"; //"hotel_room_availability";
        Database db = null;
        try {
            db = dao.getDb();
            long duration = DAYS.between(from, to) + 1;  // zero duration is one day!
            String query = "  SELECT  "
                    + HotelRoomAvailability.fields
                    + " from " + databaseTableName
                    + " where "
                    + SingleRoomRateSearchFacade.validRoomWhereClause
                    + ";";

            System.out.println(".... QUERY " + query);

            // use java 8 lambdas if you have them !
            return db.select(query)
                    .parameter("hotelId", hotelId)
                    .parameter("fromDay", Date.valueOf(from))
                    .parameter("toDay", Date.valueOf(to))
                    .parameter("minInv", minInv)
                    .parameter("duration", duration)
                    .autoMap(HotelRoomAvailability.class);
        } finally {
            //if (db != null) db.close();
        }
    }

    private Observable<AggregateHotelRoomAvailability> searchForRooms(Long hotelId, LocalDate from, LocalDate to, int minInv, Map<Long, HotelRoomAvailability> allAvailByIdMap, List<String> room, Boolean mixRoom) {

        try {
            String fromDateString = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String toDateString = to.format(DateTimeFormatter.ISO_LOCAL_DATE);

            List<String> days = new ArrayList<>();
            for (LocalDate nxt = from; !nxt.isAfter(to); nxt = nxt.plusDays(1)) {
                days.add(nxt.format(DateTimeFormatter.ISO_LOCAL_DATE));
            }

            String roomsAsString = "null";
            if (!CollectionUtils.isEmpty(room) && !room.contains("*")) {
                roomsAsString = "'{" + String.join(",", room) + "}'";
            }
            String query = "SELECT " +
                    "  s.ids, s.estimate " +
                    "FROM " +
                    "    searchForRooms_2('" + fromDateString + "'  ,'" + toDateString + "', " + minInv + ", '" + hotelId + "', null, " + roomsAsString + "  ," + mixRoom + " ) as s " +
                    "WHERE " +
                    "  s.remaingDays= '{}'" +
                    "ORDER BY " +
                    "  s.estimate" +
                    ";";

            return dao.getDb().select(query)
                    .get(rs -> new AggregateHotelRoomAvailability(allAvailByIdMap, rs.getArray(1), rs.getBigDecimal(2)))
                    ;
        } catch (Exception e) {
            log.error("Failed in  search", e);
            throw new RuntimeException(e);
        }
    }

    class DayRoom {
        final LocalDate day;
        final String room;

        public DayRoom(LocalDate day, String room) {
            this.day = day;
            this.room = room;
        }

        public LocalDate getDay() {
            return day;
        }

        public String getRoom() {
            return room;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DayRoom dayRoom = (DayRoom) o;

            if (day != null ? !day.equals(dayRoom.day) : dayRoom.day != null) return false;
            return room != null ? room.equals(dayRoom.room) : dayRoom.room == null;

        }

        @Override
        public int hashCode() {
            int result = day != null ? day.hashCode() : 0;
            result = 31 * result + (room != null ? room.hashCode() : 0);
            return result;
        }
    }


    private Observable<AggregateHotelRoomAvailability> searchForRoomsInJava(Long id, LocalDate from, LocalDate to, int inv, Map<Long, HotelRoomAvailability> allAvailByIdMap, List<String> room, Boolean mixRoom) {
        throw new RuntimeException("TBD");
    }

    private boolean hasValidRoom(AggregateHotelRoomAvailability x, List<String> room) {
        return room.contains("*") || x.getRooms().stream().map(s -> room.contains(s)).reduce(true, (a, b) -> a && b);
    }
}
