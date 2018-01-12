package com.mehrsoft.service.original;


import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.mehrsoft.dao.BaseDao;
import com.mehrsoft.metrics.MetricsService;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.opentravel.ota._2003._05.MessageAcknowledgementType;
import org.opentravel.ota._2003._05.SuccessType;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;

import javax.annotation.PostConstruct;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class BaseNotificationRX<T> {

    DateTimeFormatter parser = ISODateTimeFormat.date();


    static volatile  int msgCnt = 0;

    BlockingQueueService<Iterable<? extends HotelRoomAvailability>> bqs = new BlockingQueueService();


    @javax.inject.Inject
    MetricsService metricsService;
    @Inject
    BaseDao dao;

    // Convert a given input message to an iterable on HotelRoomAvailability.
    abstract protected Iterable<? extends HotelRoomAvailability> toHRAIterable(T msg);

    protected void processMessage(T request) throws Exception {
        bqs.put(toHRAIterable(request));
    }


    @PostConstruct
    private void daemon() {
        System.out.println(">>>>>>>>>>>> IN prcessing  DAEMON");
        bqs.getObservable().window(1, TimeUnit.SECONDS).subscribe(new Subscriber<Observable<Iterable<? extends HotelRoomAvailability>>>() {
            @Override
            public void onCompleted() {
                System.out.println("ON COMPLETED IS CALLED for 1 second of data");
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("ON errror IS CALLED");

            }

            @Override
            public void onNext(Observable<Iterable<? extends HotelRoomAvailability>> ios) {
               //System.out.println("<<<<<<<<<<<<<<<<<<<<<  Iterables to save from last 1 seconds");
                ios.subscribe(new Observer<Iterable<? extends HotelRoomAvailability>>() {

                    List<HotelRoomAvailability> toSave = null;
                    @Override
                    public void onCompleted() {
//                        System.out.println("ON COMPLETED IS CALLED");
                        try {
                        upsertAll(toSave);
                    } catch (Exception e) {
                            System.out.println("EXCEPTION IN COMPLETE");
                        e.printStackTrace();
                    } finally {
//                            System.out.println("COmplete is done");
                        }

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        System.out.println("ON errror IS CALLED");

                    }

                    @Override
                    public void onNext(Iterable<? extends HotelRoomAvailability> hotelRoomAvailabilities) {
                            if(toSave == null) {
                                toSave = Lists.newArrayList(hotelRoomAvailabilities);
                            }
                            else {
                                //   May cause stack over flowtoSave = Iterables.concat(toSave, hotelRoomAvailabilities);

                                toSave.addAll(Lists.newArrayList(hotelRoomAvailabilities));
                            }

//                        for(HotelRoomAvailability hra: hotelRoomAvailabilities) {
//                            System.out.println(hra);
//                        }
                    }
                });
            }
        });


        //hotelRoomAvailabilityDao.upsertAll(toHRAIterable(request));
    }



    public void upsertAll(Iterable<? extends HotelRoomAvailability> values) throws Exception {
        if (values == null)
            return;

        // TODO  http://blog.shinetech.com/2007/08/04/how-to-close-jdbc-resources-properly-every-time/
        Connection con = null;
        try {
            con = dao.getDs().getConnection();
            con.setAutoCommit(false);
            PreparedStatement updateStatement = null;
            try {
                updateStatement = con.prepareStatement(stmt);
                for (HotelRoomAvailability hra : values) {
                    try {
                        fixPreparedStatement(updateStatement, hra);
                        updateStatement.executeUpdate();
                        con.commit();

                    } catch (SQLException e) {
                        System.out.println("Statement: " + updateStatement.toString());
                        //allErrors.add(hra);
                        printSQLException(e);

                        if (con != null) {
                            try {
                                System.err.print("Transaction is being rolled back");
                                con.rollback();
                            } catch (SQLException excep) {
                                printSQLException(e);
                                excep.printStackTrace();
                            }
                        }
                        break;
                    }
                }

            } finally {
                if (updateStatement != null) {
                    updateStatement.close();
                }
            }
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }


    private void fixPreparedStatement(PreparedStatement updateStatement, HotelRoomAvailability hra) throws SQLException {
        int i = 1;

        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getHotel_id()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getRateCategory()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getRoomCategory()));
        updateStatement.setObject(i++, new java.sql.Date(hra.getDate().getTime()));

        // For values
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBookingLimit()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getMasterRestrictionStatus().toString()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getArrivalRestrictionStatus().toString()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getDepartureRestrictionStatus().toString()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalMinLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalMinLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalMaxLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalMaxLos()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getArrivalForwardMinStay()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getNonArrivalForwardMinStay()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalFreeNight()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalFreeNight()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getCurrencyCode()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBaseRateBeforeTax()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBaseRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getOnePersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getOnePersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getTwoPersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getTwoPersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getThreePersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getThreePersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getFourPersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getFourPersonRateAfterTax()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getAgeRateCode()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getAgeQualifiedCodeBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getAgeQualifiedCodeAfterTax()));

        // For Coalesce
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBookingLimit()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getMasterRestrictionStatus().toString()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getArrivalRestrictionStatus().toString()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getDepartureRestrictionStatus().toString()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalMinLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalMinLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalMaxLos()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalMaxLos()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getArrivalForwardMinStay()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getNonArrivalForwardMinStay()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getArrivalFreeNight()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getNonArrivalFreeNight()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getCurrencyCode()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBaseRateBeforeTax()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getBaseRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getOnePersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getOnePersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getTwoPersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getTwoPersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getThreePersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getThreePersonRateAfterTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getFourPersonRateBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getFourPersonRateAfterTax()));
        updateStatement.setObject(i++, resolveToNullOrVal(() -> hra.getAgeRateCode()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getAgeQualifiedCodeBeforeTax()));
        updateStatement.setObject(i++,
                resolveToNullOrVal(() -> hra.getAgeQualifiedCodeAfterTax()));
    }


    public static void printSQLException(SQLException ex) {

        for (Throwable e : ex) {
            if (e instanceof SQLException) {
                if (true) {
//                    ignoreSQLException(
//                        ((SQLException)e).
//                                getSQLState()) == false) {

                    e.printStackTrace(System.err);
                    System.err.println("SQLState: " +
                            ((SQLException) e).getSQLState());

                    System.err.println("Error Code: " +
                            ((SQLException) e).getErrorCode());

                    System.err.println("Message: " + e.getMessage());

                    Throwable t = ex.getCause();
                    while (t != null) {
                        System.out.println("Cause: " + t);
                        t = t.getCause();
                    }
                }
            }
        }
    }

    // http://winterbe.com/posts/2015/03/15/avoid-null-checks-in-java/

    public static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }

    private <T> T nullOrValue(Optional<T> x) {
        if (x.isPresent()) return x.get();
        else return null;
    }

    public static <T> T resolveToNullOrVal(Supplier<T> resolver) {
        try {
            return resolver.get();
        } catch (NullPointerException e) {
            return null;
        }
    }


    static String databaseTableName = "v1.hra_upsert";
    String stmt =  "insert into "
            + databaseTableName
            + "  as A (hotel_id,hotel_rate_category, hotel_room_category, date, "
            +
            "  booking_limit, "
            +
            "  master_restriction_status, "
            +
            " arrival_restriction_status, "
            +
            " departure_restriction_status, "
            +
            "  arrival_min_los,"
            +
            " non_arrival_min_los, "
            +
            "  arrival_max_los,"
            +
            " non_arrival_max_los, "
            +
            "  arrival_forward_min_stay,"
            +
            " non_arrival_forward_min_stay, "
            +
            "  arrival_free_night,"
            +
            " non_arrival_free_night, "
            +
            "  currency_code, "
            +
            "  base_rate_before_tax,"
            +
            " base_rate_after_tax, "
            +
            "  one_person_rate_before_tax,"
            +
            " one_person_rate_after_tax, "
            +
            "  two_person_rate_before_tax,"
            +
            " two_person_rate_after_tax, "
            +
            "  three_person_rate_before_tax,"
            +
            " three_person_rate_after_tax, "
            +
            "  four_person_rate_before_tax,"
            +
            " four_person_rate_after_tax, "
            +
            "  age_rate_code,"
            +
            " age_qualified_code_before_tax,"
            +
            " age_qualified_code_after_tax "
            +
            ") values (?,?,?,?,"
            +
            //4
            " ?,?,?,?,?,?,?,?,?,?, "
            +
            //+26
            " ?,?,?,?,?,?,?,?,?,?, "
            +
            " ?,?,?,?,?,?"
            +
            ")  ON CONFLICT  "
            +
            "(hotel_id, hotel_rate_category, hotel_room_category, date) DO update set ("
            +
            "  booking_limit, "
            +
            "  master_restriction_status, "
            +
            " arrival_restriction_status, "
            +
            " departure_restriction_status, "
            +
            "  arrival_min_los,"
            +
            " non_arrival_min_los, "
            +
            "  arrival_max_los,"
            +
            " non_arrival_max_los, "
            +
            "  arrival_forward_min_stay,"
            +
            " non_arrival_forward_min_stay, "
            +
            "  arrival_free_night,"
            +
            " non_arrival_free_night, "
            +
            "  currency_code, "
            +
            "  base_rate_before_tax,"
            +
            " base_rate_after_tax, "
            +
            "  one_person_rate_before_tax,"
            +
            " one_person_rate_after_tax, "
            +
            "  two_person_rate_before_tax,"
            +
            " two_person_rate_after_tax, "
            +
            "  three_person_rate_before_tax,"
            +
            " three_person_rate_after_tax, "
            +
            "  four_person_rate_before_tax,"
            +
            " four_person_rate_after_tax, "
            +
            "  age_rate_code,"
            +
            " age_qualified_code_before_tax,"
            +
            " age_qualified_code_after_tax "
            +
            ") = "
            +
            "( "
            +
            "  COALESCE(?, A. booking_limit), "
            +
            "  COALESCE(?, A.master_restriction_status), "
            +
            " COALESCE(?, A.arrival_restriction_status), "
            +
            "  COALESCE(?, A.departure_restriction_status), "
            +
            "   COALESCE(?, A.arrival_min_los),"
            +
            "  COALESCE(?, A.non_arrival_min_los), "
            +
            "   COALESCE(?, A.arrival_max_los),"
            +
            "  COALESCE(?, A.non_arrival_max_los), "
            +
            "   COALESCE(?, A.arrival_forward_min_stay),"
            +
            "  COALESCE(?, A.non_arrival_forward_min_stay), "
            +
            "   COALESCE(?, A.arrival_free_night),"
            +
            "  COALESCE(?, A.non_arrival_free_night), "
            +
            "   COALESCE(?, A.currency_code), "
            +
            "   COALESCE(?, A.base_rate_before_tax),"
            +
            "  COALESCE(?, A.base_rate_after_tax), "
            +
            "   COALESCE(?, A.one_person_rate_before_tax),"
            +
            "  COALESCE(?, A.one_person_rate_after_tax), "
            +
            "   COALESCE(?, A.two_person_rate_before_tax),"
            +
            "  COALESCE(?, A.two_person_rate_after_tax), "
            +
            "   COALESCE(?, A.three_person_rate_before_tax),"
            +
            "  COALESCE(?, A.three_person_rate_after_tax), "
            +
            "   COALESCE(?, A.four_person_rate_before_tax),"
            +
            "  COALESCE(?, A.four_person_rate_after_tax), "
            +
            "   COALESCE(?, A.age_rate_code),"
            +
            "  COALESCE(?, A.age_qualified_code_before_tax),"
            +
            "  COALESCE(?, A.age_qualified_code_after_tax) "
            +
            ")";
}

class HDPI implements Comparable<HDPI> {
    final private Long hotel;  // Key
    final private DayPlanInv dpi; //Key
    final private long createTime = new Date().getTime();

    public long getCreateTime() {
        return createTime;
    }

    public HDPI(Long hotel, DayPlanInv dpi) {
        this.hotel = hotel;
        this.dpi = dpi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HDPI hdpi = (HDPI) o;

        if(hotel == null || hdpi.hotel == null) return false;
        if(hotel == null || hdpi.hotel == null) return false;

        if (!hotel.equals(hdpi.hotel)) return false;
        return dpi.equals(hdpi.dpi);

    }

    @Override
    public int hashCode() {
        int result = hotel.hashCode();
        result = 31 * result + dpi.hashCode();
        return result;
    }


    public DayPlanInv getDpi() {
        return dpi;
    }

    @Override
    public String toString() {
        return "HDPI{" +
                "hotelID=" + hotel +
                ", dpi=" + dpi +
                '}';
    }


    @Override
    public int compareTo(HDPI that) {
        if (this.hotel.compareTo(that.hotel) < 0) {
            return -1;
        } else if (this.hotel.compareTo(that.hotel) > 0) {
            return 1;
        }

        if (this.dpi.compareTo(that.dpi) < 0) {
            return -1;
        } else if (this.dpi.compareTo(that.dpi) > 0) {
            return 1;
        }
        return 0;
    }
}



class DayPlanInv implements Comparable<DayPlanInv> {
    private final DateTime day;
    private final String invCode;   //room type: Singe, DELUX...
    private final String planCode;  // rate plan: BAR1...
    private final Long hotelId;

    static public DayPlanInv build(Long hotelId, DateTime day, String invCode, String planCode) {
        return new DayPlanInv(hotelId, day, invCode, planCode);
    }

    private DayPlanInv(Long hotelId, DateTime day, String invCode, String planCode) {
        this.day = day;
        this.invCode = invCode;
        this.planCode = planCode;
        this.hotelId = hotelId;
    }

    public DateTime getDay() {
        return day;
    }


    public String getInvCode() {
        return invCode;
    }

    public String getPlanCode() {
        return planCode;
    }


    static DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy-MM-dd");

    public Long getHotelId() {
        return hotelId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayPlanInv that = (DayPlanInv) o;

        if (day != null ? !day.equals(that.day) : that.day != null) return false;
        if (invCode != null ? !invCode.equals(that.invCode) : that.invCode != null) return false;
        if (planCode != null ? !planCode.equals(that.planCode) : that.planCode != null) return false;
        return hotelId != null ? hotelId.equals(that.hotelId) : that.hotelId == null;

    }

    @Override
    public int hashCode() {
        int result = day != null ? day.hashCode() : 0;
        result = 31 * result + (invCode != null ? invCode.hashCode() : 0);
        result = 31 * result + (planCode != null ? planCode.hashCode() : 0);
        result = 31 * result + (hotelId != null ? hotelId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DayPlanInv{" +
                "day=" + day +
                ", invCode='" + invCode + '\'' +
                ", planCode='" + planCode + '\'' +
                ", hotelId=" + hotelId +
                '}';
    }

    @Override
    public int compareTo(DayPlanInv that) {
        if (this.day.compareTo(that.day) < 0) {
            return -1;
        } else if (this.day.compareTo(that.day) > 0) {
            return 1;
        }

        if (this.invCode.compareTo(that.invCode) < 0) {
            return -1;
        } else if (this.invCode.compareTo(that.invCode) > 0) {
            return 1;
        }

        if (this.planCode.compareTo(that.planCode) < 0) {
            return -1;
        } else if (this.planCode.compareTo(that.planCode) > 0) {
            return 1;
        }

        if (this.hotelId.compareTo(that.hotelId) < 0) {
            return -1;
        } else if (this.hotelId.compareTo(that.hotelId) > 0) {
            return 1;
        }


        return 0;
    }
}
class ProtocolUtils {
    public static <T extends MessageAcknowledgementType> T buildSuccessDescriptiveResponse(Class<T> type, BigDecimal version, String primaryLangID) {
        T result = null;

        try {
            result = type.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            // if this happens it's on the caller :P
            e.printStackTrace();
            return null;
        }

        result.setTarget("Test");
        result.setVersion(version);
        result.setPrimaryLangID(primaryLangID);
        XMLGregorianCalendar cal = null;
        try {
            cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();   // TODO: Just a temp hack
        }
        result.setTimeStamp(cal);
        result.setSuccess(new SuccessType());
        return result;
    }
}

