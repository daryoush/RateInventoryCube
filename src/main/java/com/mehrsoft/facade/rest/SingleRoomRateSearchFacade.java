package com.mehrsoft.facade.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mehrsoft.dao.BaseDao;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.opentravel.ota._2003._05.RoomStayType;
import org.opentravel.ota._2003._05.RoomStaysType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by ijet on 8/26/16.
 */
@Singleton
@Path("singleRoomRatesearch")
public class SingleRoomRateSearchFacade extends BaseRoomSearch {
    private final static Logger log = LoggerFactory.getLogger(SingleRoomRateSearchFacade.class);

    @Inject
    BaseDao dao;

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    @Path("search/room/hotel/{id}/from/{from}/to/{to}/room/{room}/inv/{inv}")
    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<AggregateHotelRoomAvailability> roomSearch(@PathParam(value = "id") String id, @PathParam(value = "from") String from,
                                                           @PathParam(value = "to") String to,
                                                           @PathParam(value = "inv") int inv,
                                                           @PathParam(value = "room") String room
    ) throws
            ParseException, ParseException, Exception {
        log.info("Singe Room/Rate search for id {} from  {} to {}  ", id, from, to);


        try {
            //TODO  call the roomSearchPartial then filter the result

            Long hotelId = Long.parseLong(id);
            Date fromDate = formatter.parse(from);
            Date toDate = formatter.parse(to);
            int duration = Days.daysBetween(new DateTime(from), new DateTime(to)).getDays() + 1;  // zero duration is one day!

            return searchAggreagateAvailability(hotelId, fromDate, toDate, inv)
                    .filter(x -> x.getHras().size() == duration)
                    .filter(x -> "*".equals(room) || x.getRooms().contains(room))
                    .map(x -> new AggregateHotelRoomAvailability(applyFreeNightPolicy(x.getHras())))
                    .toList().toBlocking().single();
        } catch (Exception e) {
            log.error("Failed in  search", e);
            throw new RuntimeException(e);
        }
    }


    //Wont filter out results that are not for full itinerary
    @Path("partialsearch/room/hotel/{id}/from/{from}/to/{to}/room/{room}/inv/{inv}")
    @GET
    @Timed
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<AggregateHotelRoomAvailability> roomSearchPartial(@PathParam(value = "id") String id, @PathParam(value = "from") String from,
                                                                  @PathParam(value = "to") String to,
                                                                  @PathParam(value = "inv") int inv,
                                                                  @PathParam(value = "room") String room
    ) throws
            ParseException, ParseException, Exception {
        log.info("Singe Room/Rate search with partial results  id {} from  {} to {}  ", id, from, to);

        try {
            Long hotelId = Long.parseLong(id);
            Date fromDate = formatter.parse(from);
            Date toDate = formatter.parse(to);
            int duration = Days.daysBetween(new DateTime(from), new DateTime(to)).getDays() + 1;  // zero duration is one day!

            return searchAggreagateAvailability(hotelId, fromDate, toDate, inv)   // DON"T filter for length and room
                    .filter(x -> "*".equals(room) || room.equals(x.getRooms()))
                    .map(x -> new AggregateHotelRoomAvailability(applyFreeNightPolicy(x.getHras())))
                    .toList().toBlocking().single();
        } catch (Exception e) {
            log.error("Failed in partial search", e);
            throw new RuntimeException(e);
        }
    }

    static String databaseTableName = "v1.hra_upsert";

    public static       String validRoomWhereClause = " hotel_id = :hotelId "
            + " AND date >= :fromDay  AND  date <= :toDay "   /* between start and end */
            + " AND NOT (arrival_restriction_status = 'D' AND date = :fromDay)"
                    /* arrival is open on arrival date, for arrival date status is not D */
            + " AND NOT (departure_restriction_status = 'D' AND date = :toDay)"   /* departure is open on departure date */
            + " AND " + databaseTableName + ".master_restriction_status = 'O' "   /* master is open */
            + " AND " + databaseTableName + ".booking_limit > :minInv  "    /* has booking */
            //                /* on arrival day min los is greater  than my total stay = duration */
            + " AND NOT (arrival_min_los NOTNULL AND date = :fromDay AND arrival_min_los > :duration )"
            //                /* on any day non-arrival max los is less than my stay */
            + " AND NOT (non_arrival_min_los NOTNULL AND non_arrival_min_los > :duration )"
            + " AND one_person_rate_after_tax NOTNULL "
            + " AND two_person_rate_after_tax NOTNULL "
            + " AND one_person_rate_before_tax NOTNULL "
            + " AND two_person_rate_before_tax NOTNULL " ;

    // do a search and return the aggregate for best possible use of room.rates.  There may be missing days.  But all
    // rates will have the same room.rate
    public Observable<AggregateHotelRoomAvailability> searchAggreagateAvailability(long hotelId, Date from, Date to, int minInv) {

        String aggregateFields = "hotel_id, hotel_room_category, count(*)"
                + ", " +
                " array_agg(hotel_rate_category ORDER BY date ASC ), " +
                " array_agg(hotel_room_availability_id ORDER BY date ASC) as ids"
                + ", " +
                " array_agg(date ORDER BY date ASC) as dates, " +
                "  array_agg(booking_limit ORDER BY date ASC) as booking_limits, " +
                "  array_agg(master_restriction_status ORDER BY date ASC) as master_restriction_statuses, " +
                "  array_agg(arrival_restriction_status ORDER BY date ASC) as arrival_restriction_statuses, " +
                "  array_agg(departure_restriction_status ORDER BY date ASC) as departure_restriction_statuses, " +
                "  array_agg(arrival_min_los ORDER BY date ASC) as arrival_min_loses, " +
                "  array_agg(non_arrival_min_los ORDER BY date ASC) as non_arrival_min_loses, " +
                "  array_agg(arrival_max_los ORDER BY date ASC) as arrival_max_loses, " +
                "  array_agg(non_arrival_max_los ORDER BY date ASC) as non_arrival_max_loses, " +
                "  array_agg(arrival_forward_min_stay ORDER BY date ASC) as arrival_forward_min_stays, " +
                "  array_agg(non_arrival_forward_min_stay ORDER BY date ASC) as non_arrival_forward_min_stays, " +
                "  array_agg(arrival_free_night ORDER BY date ASC) as arrival_free_nights, " +
                "  array_agg(non_arrival_free_night ORDER BY date ASC) as non_arrival_free_nights, " +
                "  array_agg(currency_code ORDER BY date ASC) as currency_codes, " +
                "  array_agg(base_rate_before_tax ORDER BY date ASC) as base_rate_before_taxes, " +
                "  array_agg(base_rate_after_tax ORDER BY date ASC) as base_rate_after_taxes, " +
                "  array_agg(one_person_rate_before_tax ORDER BY date ASC) as one_person_rate_before_taxes, " +
                "  array_agg(one_person_rate_after_tax ORDER BY date ASC) as one_person_rate_after_taxes, " +
                "  array_agg(two_person_rate_before_tax ORDER BY date ASC) as two_person_rate_before_taxes, " +
                "  array_agg(two_person_rate_after_tax ORDER BY date ASC) as two_person_rate_after_taxes, " +
                "  array_agg(three_person_rate_before_tax ORDER BY date ASC) as three_person_rate_before_taxes, " +
                "  array_agg(three_person_rate_after_tax ORDER BY date ASC) as three_person_rate_after_taxes, " +
                "  array_agg(four_person_rate_before_tax ORDER BY date ASC) as four_person_rate_before_taxes, " +
                "  array_agg(four_person_rate_after_tax ORDER BY date ASC) as four_person_rate_after_taxes, " +
                "  array_agg(age_rate_code ORDER BY date ASC) as age_rate_codes, " +
                "  array_agg(age_qualified_code_before_tax ORDER BY date ASC) as age_qualified_code_before_taxes, " +
                "  array_agg(age_qualified_code_after_tax ORDER BY date ASC) as age_qualified_code_after_taxes ";


        String fromDay = formatter.format(from);
        String toDay = formatter.format(to);
        int duration = Days.daysBetween(new DateTime(from), new DateTime(to)).getDays() + 1;  // zero duration is one day!


        String query = "  SELECT  "
                + aggregateFields
                + " from " + databaseTableName
                + " where "
                + validRoomWhereClause
                + " GROUP BY  hotel_id, hotel_room_category, hotel_rate_category "
                + " ;";

        System.out.println(".... QUERY " + query);


        //TODO fix database close
//        These can occur when one request gets a db connection from the connection pool and closes it twice. When using a connection pool, closing the connection just returns it to the pool for reuse by another request, it doesn't close the connection. And Tomcat uses multiple threads to handle concurrent requests. Here is an example of the sequence of events which could cause this error in Tomcat:
//
//        Request 1 running in Thread 1 gets a db connection.
//
//        Request 1 closes the db connection.
//
//        The JVM switches the running thread to Thread 2
//
//        Request 2 running in Thread 2 gets a db connection
//        (the same db connection just closed by Request 1).
//
//        The JVM switches the running thread back to Thread 1
//
//        Request 1 closes the db connection a second time in a finally block.
//
//                The JVM switches the running thread back to Thread 2
//
//        Request 2 Thread 2 tries to use the db connection but fails
//        because Request 1 closed it.
//        Here is an example of properly written code to use a database connection obtained from a connection pool:
//
//        Connection conn = null;
//        Statement stmt = null;  // Or PreparedStatement if needed
//        ResultSet rs = null;
//        try {
//            conn = ... get connection from connection pool ...
//            stmt = conn.createStatement("select ...");
//            rs = stmt.executeQuery();
//    ... iterate through the result set ...
//            rs.close();
//            rs = null;
//            stmt.close();
//            stmt = null;
//            conn.close(); // Return to connection pool
//            conn = null;  // Make sure we don't close it twice
//        } catch (SQLException e) {
//    ... deal with errors ...
//        } finally {
//            // Always make sure result sets and statements are closed,
//            // and the connection is returned to the pool
//            if (rs != null) {
//                try { rs.close(); } catch (SQLException e) { ; }
//                rs = null;
//            }
//            if (stmt != null) {
//                try { stmt.close(); } catch (SQLException e) { ; }
//                stmt = null;
//            }
//            if (conn != null) {
//                try { conn.close(); } catch (SQLException e) { ; }
//                conn = null;
//            }
//        }
//

        // use java 8 lambdas if you have them !
        return dao.getDb().select(query)
                .parameter("hotelId", hotelId)
                .parameter("fromDay", from)
                .parameter("toDay", to)
                .parameter("minInv", minInv)
                .parameter("duration", duration)
                .autoMap(AggregateHotelRoomAvailability.class);

    }


}