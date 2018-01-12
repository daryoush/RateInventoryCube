package com.mehrsoft.facade.rest;

import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import com.github.davidmoten.rx.jdbc.Database;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mehrsoft.dao.BaseDao;
import com.mehrsoft.metrics.MetricsService;
import com.mehrsoft.model.old.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by ijet on 8/24/16.
 */
@Singleton
@Path("rawsearch")
public class RawRecordSearchFacade  {
    private final static Logger log = LoggerFactory.getLogger(RawRecordSearchFacade.class);

    @Inject
    BaseDao dao;

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
    @Path("availability/hotel/{id}/from/{from}/to/{to}")
    @GET
    @Metered
    @ExceptionMetered
    @Produces(MediaType.APPLICATION_JSON)
    public List<ExternalRateCube> hotelAvailabilityForDateRange(@PathParam(value="id")String id, @PathParam(value="from")String from, @PathParam(value="to")String to) throws ParseException, ParseException,  Exception {
        log.info("Getting availability for hotel with hbsi id {} from  {} to {}  ", id, from, to);
//        Long hotelId = hotelExternalMapDao.findHotelIdByChannelAndExternalId(hbsiChannel.getId(),
//                "" + id);
        try {
            Long hotelId = Long.parseLong(id);
            Date fromDate = formatter.parse(from);
            Date toDate = formatter.parse(to);

            List<HotelRoomAvailability> allAvails = getHotelRoomAvailabilityByDateRange(hotelId, fromDate, toDate); // First denormalize the db before getting children, HotelRoomAvailabilityDao.Children.values());

            return Lists.newArrayList(Iterables.transform(allAvails, new Function<HotelRoomAvailability, ExternalRateCube>() {

                @Override
                public ExternalRateCube apply(final HotelRoomAvailability input) {
                    return new ExternalRateCube(input);
                }
            }));
        } catch (Exception e)  {
            log.error("Error.... ", e);
            throw new RuntimeException(e);
        }


    }

    @Timed(name = "RawRecordSearchFacade.getHotelRoomAvailabilityByDateRange")
    private List<HotelRoomAvailability> getHotelRoomAvailabilityByDateRange(Long hotelId, Date fromDate, Date toDate) {

         String databaseTableName = "v1.hra_upsert"; //"hotel_room_availability";



        Database db = null;

        try {
            db = dao.getDb();
            String from = formatter.format(fromDate);
            String to = formatter.format(toDate);

            String query = "  SELECT  "
                    + HotelRoomAvailability.fields
                    + " from " + databaseTableName
                    + " where "
                    + " hotel_id = " + quoted(hotelId.toString())
                    + " AND date BETWEEN " + quoted(from) + " and " + quoted(to) + ";";

            // use java 8 lambdas if you have them !
            return db.select(query)
                    .autoMap(HotelRoomAvailability.class)
                    .toList().toBlocking().single();
        } finally {
            if (db != null) db.close();
        }
    }

    private static String quoted(String s) {
        return "'" + s + "'";
    }
}

class ExternalRateCube {
    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public ExternalRateCube(HotelRoomAvailability hra ) {
        rateCategory = hra.getRateCategory();
        roomCategory = hra.getRoomCategory();
        date = formatter.format( hra.getDate());
        dateTime = new DateTime(hra.getDate());
        bookingLimit = hra.getBookingLimit();
        //  lastModification = formatter.format(hra.getModified());
        //  lastModifiedBy = hra.getModifiedBy();
        currencyCode = hra.getCurrencyCode();


        //if (null != hra.getMasterRestrictionStatus())
        restrictions.add(new Restriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType.MASTER, hra.getMasterRestrictionStatus()));
        //if (null != hra.getArrivalRestrictionStatus())
        restrictions.add(new Restriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType.ARRIVAL, hra.getArrivalRestrictionStatus()));
        // if (null != hra.getDepartureRestrictionStatus())
        restrictions.add(new Restriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType.DEPARTURE, hra.getDepartureRestrictionStatus()));

        //if (null != hra.getNonArrivalForwardMinStay())
        los.add(new LOS(HotelAvailabilityLengthOfStayType.MINIMUM, hra.getArrivalMinLos(), hra.getNonArrivalMinLos()));
        //if (null != hra.getNonArrivalMaxLos())
        los.add(new LOS(HotelAvailabilityLengthOfStayType.MAXIMUM, hra.getArrivalMaxLos(), hra.getNonArrivalMaxLos()));
        //if (null != hra.getNonArrivalForwardMinStay())
        los.add(new LOS(HotelAvailabilityLengthOfStayType.SET_FORWARD_MIN_STAY, hra.getArrivalForwardMinStay(), hra.getNonArrivalForwardMinStay()));
        //if (null != hra.getNonArrivalFreeNight())
        los.add(new LOS(HotelAvailabilityLengthOfStayType.FREE_NIGHT, hra.getArrivalFreeNight(), hra.getNonArrivalFreeNight()));

        baseRateBeforeTax = hra.getBaseRateBeforeTax();
        baseRateAfterTax = hra.getBaseRateAfterTax();
        onePersonRateBeforeTax = hra.getOnePersonRateBeforeTax();
        onePersonRateAfterTax = hra.getOnePersonRateAfterTax();
        twoPersonRateBeforeTax = hra.getTwoPersonRateBeforeTax();
        twoPersonRateAfterTax = hra.getTwoPersonRateAfterTax();
        threePersonRateBeforeTax = hra.getThreePersonRateBeforeTax();
        threePersonRateAfterTax = hra.getThreePersonRateAfterTax();
        fourPersonRateBeforeTax = hra.getFourPersonRateBeforeTax();
        fourPersonRateAfterTax = hra.getFourPersonRateAfterTax();

        ageRateCode = hra.getAgeRateCode();
        ageQualifiedCodeBeforeTax = hra.getAgeQualifiedCodeBeforeTax();
        ageQualifiedCodeAfterTax = hra.getAgeQualifiedCodeAfterTax();

    }


    public String getRateCategory() {
        return rateCategory;
    }

    private String              rateCategory;
    private String              roomCategory;
    private String                date;
    private Integer             bookingLimit;
    private String                lastModification;
    private String              lastModifiedBy;
    DateTime				dateTime;


    static class Restriction {
        HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType type;
        HotelAvailabilityRestrictionStatus status = null;

        public Restriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType type, HotelAvailabilityRestrictionStatus status) {
            this.type = type;
            this.status = status;
        }

        public HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType getType() {
            return type;
        }

        public HotelAvailabilityRestrictionStatus getStatus() {
            return status;
        }
    }

    List<Restriction> restrictions = new ArrayList<>();


    static class LOS {
        HotelAvailabilityLengthOfStayType type;
        Integer arrival;
        Integer nonArrival;


        public LOS(HotelAvailabilityLengthOfStayType type, Integer arrival, Integer nonArrival) {
            this.type = type;
            this.arrival = arrival;
            this.nonArrival = nonArrival;
        }

        public HotelAvailabilityLengthOfStayType getType() {
            return type;
        }

        public Integer getArrival() {
            return arrival;
        }

        public Integer getNonArrival() {
            return nonArrival;
        }
    }

    List<LOS> los = new ArrayList<>();



    private String  currencyCode;

    private BigDecimal baseRateBeforeTax;
    private BigDecimal  baseRateAfterTax;
    private BigDecimal  onePersonRateBeforeTax;
    private BigDecimal  onePersonRateAfterTax;
    private BigDecimal  twoPersonRateBeforeTax;
    private BigDecimal  twoPersonRateAfterTax;
    private BigDecimal  threePersonRateBeforeTax;
    private BigDecimal  threePersonRateAfterTax;
    private BigDecimal  fourPersonRateBeforeTax;
    private BigDecimal  fourPersonRateAfterTax;

    private OTAAgeQualifyingCode ageRateCode;  // Accept only a single age rate
    private BigDecimal   ageQualifiedCodeBeforeTax;
    private BigDecimal   ageQualifiedCodeAfterTax;

    public String getRoomCategory() {
        return roomCategory;
    }

    public String getDate() {
        return date;
    }

    public Integer getBookingLimit() {
        return bookingLimit;
    }

    public List<Restriction> getRestrictions() {
        return restrictions;
    }

    public List<LOS> getLos() {
        return los;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getBaseRateBeforeTax() {
        return baseRateBeforeTax;
    }

    public BigDecimal getBaseRateAfterTax() {
        return baseRateAfterTax;
    }

    public BigDecimal getOnePersonRateBeforeTax() {
        return onePersonRateBeforeTax;
    }

    public BigDecimal getOnePersonRateAfterTax() {
        return onePersonRateAfterTax;
    }

    public BigDecimal getTwoPersonRateBeforeTax() {
        return twoPersonRateBeforeTax;
    }

    public BigDecimal getTwoPersonRateAfterTax() {
        return twoPersonRateAfterTax;
    }

    public BigDecimal getThreePersonRateBeforeTax() {
        return threePersonRateBeforeTax;
    }

    public BigDecimal getThreePersonRateAfterTax() {
        return threePersonRateAfterTax;
    }

    public BigDecimal getFourPersonRateBeforeTax() {
        return fourPersonRateBeforeTax;
    }

    public BigDecimal getFourPersonRateAfterTax() {
        return fourPersonRateAfterTax;
    }

    public OTAAgeQualifyingCode getAgeRateCode() {
        return ageRateCode;
    }

    public BigDecimal getAgeQualifiedCodeBeforeTax() {
        return ageQualifiedCodeBeforeTax;
    }

    public BigDecimal getAgeQualifiedCodeAfterTax() {
        return ageQualifiedCodeAfterTax;
    }

    public String getLastModification() {
        return lastModification;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public DateTime getDateTime() {
        return dateTime;
    }
}


