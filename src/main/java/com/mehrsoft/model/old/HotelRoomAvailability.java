package com.mehrsoft.model.old;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.base.Strings;
import org.opentravel.ota._2003._05.RoomStayType;
import org.opentravel.ota._2003._05.RoomStaysType;
import org.opentravel.ota._2003._05.TotalType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by ijet on 8/24/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelRoomAvailability implements Comparable<HotelRoomAvailability> {
    private static final long serialVersionUID = 1L;

    public static String fields = "hotel_room_availability_id as id,\n" +
            "  hotel_id,\n" +
            "  hotel_rate_category,\n" +
            "  hotel_room_category, " +
            "  date,\n" +
            "  booking_limit,\n" +
            "  master_restriction_status, arrival_restriction_status, departure_restriction_status,\n" +
            "  arrival_min_los, non_arrival_min_los,\n" +
            "  arrival_max_los, non_arrival_max_los,\n" +
            "  arrival_forward_min_stay, non_arrival_forward_min_stay,\n" +
            "  arrival_free_night, non_arrival_free_night,\n" +
            "  currency_code,\n" +
            "  base_rate_before_tax, base_rate_after_tax,\n" +
            "  one_person_rate_before_tax, one_person_rate_after_tax,\n" +
            "  two_person_rate_before_tax, two_person_rate_after_tax,\n" +
            "  three_person_rate_before_tax, three_person_rate_after_tax,\n" +
            "  four_person_rate_before_tax, four_person_rate_after_tax,\n" +
            "  age_rate_code, age_qualified_code_before_tax, age_qualified_code_after_tax\n";

    public HotelRoomAvailability() {

    }

    public RoomStaysType toRoomStay() {

        try {
            RoomStaysType rst = new RoomStaysType();

            RoomStayType.RoomRates roomRates = new RoomStayType.RoomRates();
            RoomStayType.RoomRates.RoomRate rr = new RoomStayType.RoomRates.RoomRate();
            RoomStaysType.RoomStay rs = new RoomStaysType.RoomStay();
            rst.getRoomStay().add(rs);
            rs.setRoomRates(roomRates);
            roomRates.getRoomRate().add(rr);

            roomRates.getRoomRate().add(rr);
            rr.setRatePlanCode(getRateCategory());
            rr.setRoomTypeCode(getRoomCategory());
            GregorianCalendar c = new GregorianCalendar();
            c.setTime(getDate());
            try {
                XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
                rr.setEffectiveDate(date);
                rr.setExpireDate(date);
            } catch (DatatypeConfigurationException e) {
                e.printStackTrace();
            }

            rr.setNumberOfUnits(BigInteger.ONE);
            final TotalType tt = new TotalType();
            rr.setTotal(tt);
            tt.setAmountAfterTax(getOnePersonRateAfterTax());
            tt.setAmountBeforeTax(getOnePersonRateBeforeTax());
            tt.setCurrencyCode(getCurrencyCode());
            return rst;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public int compareTo(HotelRoomAvailability that) {
        if (this.date.compareTo(that.date) < 0) {
            return -1;
        } else if (this.date.compareTo(that.date) > 0) {
            return 1;
        }

        if (this.hotel_id.compareTo(that.hotel_id) < 0) {
            return -1;
        } else if (this.hotel_id.compareTo(that.hotel_id) > 0) {
            return 1;
        }

        if (this.roomCategory.compareTo(that.roomCategory) < 0) {
            return -1;
        } else if (this.roomCategory.compareTo(that.roomCategory) > 0) {
            return 1;
        }

        if (this.rateCategory.compareTo(that.rateCategory) < 0) {
            return -1;
        } else if (this.rateCategory.compareTo(that.rateCategory) > 0) {
            return 1;
        }
        return 0;
    }

    static public class Builder {
        private String              rateCategory;
        private String              roomCategory;
        private Date date;

        public Builder on(Date d) {
            this.date = d;
            return this;
        }

        public Builder roomCatagory(String room) {
            this.roomCategory = room;
            return this;
        }

        public Builder rateCategory(String rate) {
            this.rateCategory = rate;
            return this;
        }

        public HotelRoomAvailability build() {
            assert(!Strings.isNullOrEmpty(rateCategory));
            assert(!Strings.isNullOrEmpty(roomCategory));
            assert(date != null);

            HotelRoomAvailability hra = new HotelRoomAvailability();
            hra.setDate(date);
            hra.setRoomCategory(roomCategory);
            hra.setRateCategory(rateCategory);
            return hra;
        }

    }

    public HotelRoomAvailability(HotelRoomAvailability other) {
        this.id = other.id;
        this.hotel_id = other.hotel_id;
        this.rateCategory = other.rateCategory;
        this.roomCategory = other.roomCategory;
        this.date = other.date;
        this.bookingLimit = other.bookingLimit;
        this.masterRestrictionStatus = other.masterRestrictionStatus;
        this.arrivalRestrictionStatus = other.arrivalRestrictionStatus;
        this.departureRestrictionStatus = other.departureRestrictionStatus;
        this.arrivalMinLos = other.arrivalMinLos;
        this.nonArrivalMinLos = other.nonArrivalMinLos;
        this.arrivalMaxLos = other.arrivalMaxLos;
        this.nonArrivalMaxLos = other.nonArrivalMaxLos;
        this.arrivalForwardMinStay = other.arrivalForwardMinStay;
        this.nonArrivalForwardMinStay = other.nonArrivalForwardMinStay;
        this.arrivalFreeNight = other.arrivalFreeNight;
        this.nonArrivalFreeNight = other.nonArrivalFreeNight;
        this.currencyCode = other.currencyCode;
        this.baseRateBeforeTax = other.baseRateBeforeTax;
        this.baseRateAfterTax = other.baseRateAfterTax;
        this.onePersonRateBeforeTax = other.onePersonRateBeforeTax;
        this.onePersonRateAfterTax = other.onePersonRateAfterTax;
        this.twoPersonRateBeforeTax = other.twoPersonRateBeforeTax;
        this.twoPersonRateAfterTax = other.twoPersonRateAfterTax;
        this.threePersonRateBeforeTax = other.threePersonRateBeforeTax;
        this.threePersonRateAfterTax = other.threePersonRateAfterTax;
        this.fourPersonRateBeforeTax = other.fourPersonRateBeforeTax;
        this.fourPersonRateAfterTax = other.fourPersonRateAfterTax;
        this.ageRateCode = other.ageRateCode;
        this.ageQualifiedCodeBeforeTax = other.ageQualifiedCodeBeforeTax;
        this.ageQualifiedCodeAfterTax = other.ageQualifiedCodeAfterTax;
    }

    public HotelRoomAvailability(Long id,
                                 Long hotel_id,
                                 String rateCategory,
                                 String roomCategory,
                                 Date date,
                                 Integer bookingLimit,
                                 String masterRestrictionStatus,
                                 String arrivalRestrictionStatus,
                                 String departureRestrictionStatus,
                                 Integer arrivalMinLos,
                                 Integer nonArrivalMinLos,
                                 Integer arrivalMaxLos,
                                 Integer nonArrivalMaxLos,
                                 Integer arrivalForwardMinStay,
                                 Integer nonArrivalForwardMinStay,
                                 Integer arrivalFreeNight,
                                 Integer nonArrivalFreeNight,
                                 String currencyCode,
                                 BigDecimal baseRateBeforeTax,
                                 BigDecimal baseRateAfterTax,
                                 BigDecimal onePersonRateBeforeTax,
                                 BigDecimal onePersonRateAfterTax,
                                 BigDecimal twoPersonRateBeforeTax,
                                 BigDecimal twoPersonRateAfterTax,
                                 BigDecimal threePersonRateBeforeTax,
                                 BigDecimal threePersonRateAfterTax,
                                 BigDecimal fourPersonRateBeforeTax,
                                 BigDecimal fourPersonRateAfterTax,
                                 String ageRateCode,
                                 BigDecimal ageQualifiedCodeBeforeTax,
                                 BigDecimal ageQualifiedCodeAfterTax) {
        this.id = id;
        this.hotel_id = hotel_id;
        this.rateCategory = rateCategory;
        this.roomCategory = roomCategory;
        this.date = date;
        this.bookingLimit = bookingLimit;
        this.masterRestrictionStatus = null == masterRestrictionStatus ? null :HotelAvailabilityRestrictionStatus.valueOf(masterRestrictionStatus);
        this.arrivalRestrictionStatus = null == arrivalRestrictionStatus ? null :HotelAvailabilityRestrictionStatus.valueOf(arrivalRestrictionStatus);
        this.departureRestrictionStatus = null == departureRestrictionStatus ? null :  HotelAvailabilityRestrictionStatus.valueOf(departureRestrictionStatus);
        this.arrivalMinLos = arrivalMinLos;
        this.nonArrivalMinLos = nonArrivalMinLos;
        this.arrivalMaxLos = arrivalMaxLos;
        this.nonArrivalMaxLos = nonArrivalMaxLos;
        this.arrivalForwardMinStay = arrivalForwardMinStay;
        this.nonArrivalForwardMinStay = nonArrivalForwardMinStay;
        this.arrivalFreeNight = arrivalFreeNight;
        this.nonArrivalFreeNight = nonArrivalFreeNight;
        this.currencyCode = currencyCode;
        this.baseRateBeforeTax = baseRateBeforeTax;
        this.baseRateAfterTax = baseRateAfterTax;
        this.onePersonRateBeforeTax = onePersonRateBeforeTax;
        this.onePersonRateAfterTax = onePersonRateAfterTax;
        this.twoPersonRateBeforeTax = twoPersonRateBeforeTax;
        this.twoPersonRateAfterTax = twoPersonRateAfterTax;
        this.threePersonRateBeforeTax = threePersonRateBeforeTax;
        this.threePersonRateAfterTax = threePersonRateAfterTax;
        this.fourPersonRateBeforeTax = fourPersonRateBeforeTax;
        this.fourPersonRateAfterTax = fourPersonRateAfterTax;
        this.ageRateCode = OTAAgeQualifyingCode.from(ageRateCode);
        this.ageQualifiedCodeBeforeTax = ageQualifiedCodeBeforeTax;
        this.ageQualifiedCodeAfterTax = ageQualifiedCodeAfterTax;
    }

    protected Long id;

    protected Long hotel_id = 0L;
    private String              rateCategory;
    private String              roomCategory;
    private Date                date;
    private Integer             bookingLimit;

    private HotelAvailabilityRestrictionStatus masterRestrictionStatus;
    private HotelAvailabilityRestrictionStatus arrivalRestrictionStatus;
    private HotelAvailabilityRestrictionStatus departureRestrictionStatus;

    private Integer arrivalMinLos;
    private Integer nonArrivalMinLos;
    private Integer arrivalMaxLos;
    private Integer nonArrivalMaxLos;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    private Integer arrivalForwardMinStay;
    private Integer nonArrivalForwardMinStay;
    private Integer arrivalFreeNight;
    private Integer nonArrivalFreeNight;

    private String  currencyCode;

    private BigDecimal  baseRateBeforeTax;
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

//    public void merge(HotelRoomAvailability hra) {
//        assert (this.rateCategory.equals(hra.getRateCategory()));
//        assert (this.roomCategory.equals(hra.getRoomCategory()));
//        assert (this.date.equals(hra.getDate()));
//        assert this.hotel_id.equals(hra.hotel_id);
//
//        bookingLimit = updateIfNotNull(hra.getBookingLimit(),bookingLimit);
//
//        arrivalMinLos = updateIfNotNull(hra.arrivalMinLos, arrivalMinLos);
//        nonArrivalMinLos = updateIfNotNull(hra.nonArrivalMinLos, nonArrivalMinLos);
//        arrivalMaxLos = updateIfNotNull(hra.arrivalMaxLos, arrivalMaxLos);
//        nonArrivalMaxLos = updateIfNotNull(hra.nonArrivalMaxLos, nonArrivalMaxLos);
//        arrivalForwardMinStay = updateIfNotNull(hra.arrivalForwardMinStay, arrivalForwardMinStay);
//        nonArrivalForwardMinStay = updateIfNotNull(hra.nonArrivalForwardMinStay, nonArrivalForwardMinStay);
//        arrivalFreeNight = updateIfNotNull(hra.arrivalFreeNight, arrivalFreeNight);
//        nonArrivalFreeNight = updateIfNotNull(hra.nonArrivalFreeNight, nonArrivalFreeNight);
//
//        currencyCode = updateIfNotNull(hra.currencyCode, currencyCode);
//        baseRateAfterTax = updateIfNotNull(hra.baseRateAfterTax, baseRateAfterTax);
//        baseRateBeforeTax = updateIfNotNull(hra.baseRateBeforeTax, baseRateBeforeTax);
//        onePersonRateAfterTax = updateIfNotNull(hra.onePersonRateAfterTax, onePersonRateAfterTax);
//        onePersonRateBeforeTax = updateIfNotNull(hra.onePersonRateBeforeTax, onePersonRateBeforeTax);
//        twoPersonRateAfterTax = updateIfNotNull(hra.twoPersonRateAfterTax, twoPersonRateAfterTax);
//        twoPersonRateBeforeTax = updateIfNotNull(hra.twoPersonRateBeforeTax, twoPersonRateBeforeTax);
//        threePersonRateAfterTax = updateIfNotNull(hra.threePersonRateAfterTax, threePersonRateAfterTax);
//        threePersonRateBeforeTax = updateIfNotNull(hra.threePersonRateBeforeTax, threePersonRateBeforeTax);
//        fourPersonRateAfterTax = updateIfNotNull(hra.fourPersonRateAfterTax, fourPersonRateAfterTax);
//        fourPersonRateBeforeTax = updateIfNotNull(hra.fourPersonRateBeforeTax, fourPersonRateBeforeTax);
//        ageRateCode = updateIfNotNull(hra.ageRateCode, ageRateCode);  // Accept only a single age rate
//        ageQualifiedCodeBeforeTax = updateIfNotNull(hra.ageQualifiedCodeBeforeTax ,ageQualifiedCodeBeforeTax);
//        ageQualifiedCodeAfterTax = updateIfNotNull(hra.ageQualifiedCodeAfterTax, ageQualifiedCodeAfterTax);
//    }

    public void setRestriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType type, HotelAvailabilityRestrictionStatus status) {
        System.out.println(">>>> Set restriction " + type + " to " + status);
        switch(type) {
            case MASTER:
                masterRestrictionStatus = status;
                break;
            case ARRIVAL:
                arrivalRestrictionStatus = status;
                break;
            case DEPARTURE:
                departureRestrictionStatus = status;
                break;
            default:
                throw new RuntimeException("Invalid Hotel restriction type was detected.  " + type);
        }
    }

    public void setLengthOfStay(HotelAvailabilityLengthOfStayType type, boolean arrivalBased,  int days) {
        switch (type) {
            case MINIMUM:
                if(arrivalBased) arrivalMinLos = days;
                else nonArrivalMinLos = days;
                break;
            case MAXIMUM:
                if(arrivalBased) arrivalMaxLos = days;
                else nonArrivalMaxLos = days;
                break;
            case PATTERN:
                throw new RuntimeException( "Full Pattern LOS is not suported by the model");
                //break;
            case SET_FORWARD_MIN_STAY:
                if(arrivalBased) arrivalForwardMinStay = days;
                else nonArrivalForwardMinStay = days;
                break;
            case FREE_NIGHT:
                if(arrivalBased) arrivalFreeNight = days;
                else nonArrivalFreeNight = days;
                break;
        }
    }

    public void setPrice(int numberOfGuests, String currencyCode, BigDecimal amountBeforeTax, BigDecimal amountAfterTax) {
        if (numberOfGuests > 4)
            throw new RuntimeException("For now we domain model can't handle more than 4 guests per room");

        setCurrencyCode(currencyCode);

        switch(numberOfGuests) {
            case 0:
                baseRateBeforeTax = amountBeforeTax;
                baseRateAfterTax = amountAfterTax;
                break;
            case 1:
                onePersonRateBeforeTax = amountBeforeTax;
                onePersonRateAfterTax = amountAfterTax;
                break;
            case 2:
                twoPersonRateBeforeTax = amountBeforeTax;
                twoPersonRateAfterTax = amountAfterTax;
                break;
            case 3:
                threePersonRateBeforeTax = amountBeforeTax;
                threePersonRateAfterTax = amountAfterTax;
                break;
            case 4:
                fourPersonRateBeforeTax = amountBeforeTax;
                fourPersonRateAfterTax = amountAfterTax;
                break;
            default:
                throw new RuntimeException("Invalid number of guests:" + numberOfGuests);
        }
    }

    public void setAgeQualifiedPrice(OTAAgeQualifyingCode ageCode, String currencyCode,  BigDecimal amountBeforeTax, BigDecimal amountAfterTax) {
        if(ageQualifiedCodeAfterTax != null || ageQualifiedCodeBeforeTax  != null ) {
            throw new RuntimeException("At this point the domain model can only support a single age qualifed rate.");
        }

        setCurrencyCode(currencyCode);
        ageQualifiedCodeBeforeTax = amountBeforeTax;
        ageQualifiedCodeAfterTax = amountAfterTax;
    }



    @Override
    public String toString() {
        return "HotelRoomAvailability{" +
                "rateCategory='" + rateCategory + '\'' +
                ", roomCategory='" + roomCategory + '\'' +
                ", bookingLimit=" + bookingLimit +
                ", date=" + date +
                ", hotel=" + hotel_id +
                ", masterRestrictionStatus=" + masterRestrictionStatus +
                ", arrivalRestrictionStatus=" + arrivalRestrictionStatus +
                ", departureRestrictionStatus=" + departureRestrictionStatus +
                ", arrivalMinLos=" + arrivalMinLos +
                ", nonArrivalMinLos=" + nonArrivalMinLos +
                ", arrivalMaxLos=" + arrivalMaxLos +
                ", nonArrivalMaxLos=" + nonArrivalMaxLos +
                ", arrivalForwardMinStay=" + arrivalForwardMinStay +
                ", nonArrivalForwardMinStay=" + nonArrivalForwardMinStay +
                ", arrivalFreeNight=" + arrivalFreeNight +
                ", nonArrivalFreeNight=" + nonArrivalFreeNight +
                ", currencyCode='" + currencyCode + '\'' +
                ", baseRateAfterTax=" + baseRateAfterTax +
                ", baseRateABeforeTax=" + baseRateBeforeTax +
                ", onePersonRateAfterTax=" + onePersonRateAfterTax +
                ", onePersonRateBeforeTax=" + onePersonRateBeforeTax +
                ", twoPersonRateAfterTax=" + twoPersonRateAfterTax +
                ", twoPersonRateBeforeTax=" + twoPersonRateBeforeTax +
                ", threePersonRateAfterTax=" + threePersonRateAfterTax +
                ", threePersonRateBeforeTax=" + threePersonRateBeforeTax +
                ", fourPersonRateAfterTax=" + fourPersonRateAfterTax +
                ", fourPersonRateBeforeTax=" + fourPersonRateBeforeTax +
                ", ageRateCode=" + ageRateCode +
                ", ageQualifiedCodeBeforeTax=" + ageQualifiedCodeBeforeTax +
                ", ageQualifiedCodeAfterTax=" + ageQualifiedCodeAfterTax +
                '}';
    }

    public boolean sameRate(HotelRoomAvailability that ) {

        // See if two HotelRoomAvail records, potentially differnt dates have the same prices for given room/rate category
        // if so they can be merged in booking call


        if (hotel_id != null ? !hotel_id.equals(that.hotel_id) : that.hotel_id != null)
            return false;
        if (rateCategory != null ? !rateCategory.equals(that.rateCategory) : that.rateCategory != null)
            return false;
        if (roomCategory != null ? !roomCategory.equals(that.roomCategory) : that.roomCategory != null)
            return false;


        if (baseRateBeforeTax != null ?
                !baseRateBeforeTax.equals(that.baseRateBeforeTax) :
                that.baseRateBeforeTax != null)
            return false;
        if (baseRateAfterTax != null ? !baseRateAfterTax.equals(that.baseRateAfterTax) : that.baseRateAfterTax != null)
            return false;
        if (onePersonRateBeforeTax != null ?
                !onePersonRateBeforeTax.equals(that.onePersonRateBeforeTax) :
                that.onePersonRateBeforeTax != null)
            return false;
        if (onePersonRateAfterTax != null ?
                !onePersonRateAfterTax.equals(that.onePersonRateAfterTax) :
                that.onePersonRateAfterTax != null)
            return false;
        if (twoPersonRateBeforeTax != null ?
                !twoPersonRateBeforeTax.equals(that.twoPersonRateBeforeTax) :
                that.twoPersonRateBeforeTax != null)
            return false;
        if (twoPersonRateAfterTax != null ?
                !twoPersonRateAfterTax.equals(that.twoPersonRateAfterTax) :
                that.twoPersonRateAfterTax != null)
            return false;
        if (threePersonRateBeforeTax != null ?
                !threePersonRateBeforeTax.equals(that.threePersonRateBeforeTax) :
                that.threePersonRateBeforeTax != null)
            return false;
        if (threePersonRateAfterTax != null ?
                !threePersonRateAfterTax.equals(that.threePersonRateAfterTax) :
                that.threePersonRateAfterTax != null)
            return false;
        if (fourPersonRateBeforeTax != null ?
                !fourPersonRateBeforeTax.equals(that.fourPersonRateBeforeTax) :
                that.fourPersonRateBeforeTax != null)
            return false;
        if (fourPersonRateAfterTax != null ?
                !fourPersonRateAfterTax.equals(that.fourPersonRateAfterTax) :
                that.fourPersonRateAfterTax != null)
            return false;
        if (ageRateCode != that.ageRateCode)
            return false;
        if (ageQualifiedCodeBeforeTax != null ?
                !ageQualifiedCodeBeforeTax.equals(that.ageQualifiedCodeBeforeTax) :
                that.ageQualifiedCodeBeforeTax != null)
            return false;
        return ageQualifiedCodeAfterTax != null ?
                ageQualifiedCodeAfterTax.equals(that.ageQualifiedCodeAfterTax) :
                that.ageQualifiedCodeAfterTax == null;

    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        HotelRoomAvailability that = (HotelRoomAvailability) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
            return false;
        if (hotel_id != null ? !hotel_id.equals(that.hotel_id) : that.hotel_id != null)
            return false;
        if (rateCategory != null ? !rateCategory.equals(that.rateCategory) : that.rateCategory != null)
            return false;
        if (roomCategory != null ? !roomCategory.equals(that.roomCategory) : that.roomCategory != null)
            return false;
        return date != null ? date.equals(that.date) : that.date == null;

    }

    @Override public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (hotel_id != null ? hotel_id.hashCode() : 0);
        result = 31 * result + (rateCategory != null ? rateCategory.hashCode() : 0);
        result = 31 * result + (roomCategory != null ? roomCategory.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        return result;
    }

    public long getHotel_id() {
        return hotel_id;
    }

    public void setHotel_id(long hotel_id) {
        this.hotel_id = hotel_id;
    }

    public String getRateCategory() {
        return rateCategory;
    }

    public void setRateCategory (String rateCategory) {
        this.rateCategory = rateCategory;
    }

    public String getRoomCategory() {
        return roomCategory;
    }

    public void setRoomCategory(String roomCategory) {
        this.roomCategory = roomCategory;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getBookingLimit() {
        return bookingLimit;
    }

    public void setBookingLimit(Integer bookingLimit) {
        this.bookingLimit = bookingLimit;
    }

    public HotelAvailabilityRestrictionStatus getMasterRestrictionStatus() {
        return masterRestrictionStatus;
    }

    public void setMasterRestrictionStatus(HotelAvailabilityRestrictionStatus masterRestrictionStatus) {
        this.masterRestrictionStatus = masterRestrictionStatus;
    }

    public HotelAvailabilityRestrictionStatus getArrivalRestrictionStatus() {
        return arrivalRestrictionStatus;
    }

    public void setArrivalRestrictionStatus(HotelAvailabilityRestrictionStatus arrivalRestrictionStatus) {
        this.arrivalRestrictionStatus = arrivalRestrictionStatus;
    }

    public HotelAvailabilityRestrictionStatus getDepartureRestrictionStatus() {
        return departureRestrictionStatus;
    }

    public void setDepartureRestrictionStatus(HotelAvailabilityRestrictionStatus departureRestrictionStatus) {
        this.departureRestrictionStatus = departureRestrictionStatus;
    }

    public Integer getArrivalMinLos() {
        return arrivalMinLos;
    }

    public void setArrivalMinLos(Integer arrivalMinLos) {
        this.arrivalMinLos = arrivalMinLos;
    }

    public Integer getNonArrivalMinLos() {
        return nonArrivalMinLos;
    }

    public void setNonArrivalMinLos(Integer nonArrivalMinLos) {
        this.nonArrivalMinLos = nonArrivalMinLos;
    }

    public Integer getArrivalMaxLos() {
        return arrivalMaxLos;
    }

    public void setArrivalMaxLos(Integer arrivalMaxLos) {
        this.arrivalMaxLos = arrivalMaxLos;
    }

    public Integer getNonArrivalMaxLos() {
        return nonArrivalMaxLos;
    }

    public void setNonArrivalMaxLos(Integer nonArrivalMaxLos) {
        this.nonArrivalMaxLos = nonArrivalMaxLos;
    }

    public Integer getArrivalForwardMinStay() {
        return arrivalForwardMinStay;
    }

    public void setArrivalForwardMinStay(Integer arrivalForwardMinStay) {
        this.arrivalForwardMinStay = arrivalForwardMinStay;
    }

    public Integer getNonArrivalForwardMinStay() {
        return nonArrivalForwardMinStay;
    }

    public void setNonArrivalForwardMinStay(Integer nonArrivalForwardMinStay) {
        this.nonArrivalForwardMinStay = nonArrivalForwardMinStay;
    }

    public Integer getArrivalFreeNight() {
        return arrivalFreeNight;
    }

    public void setArrivalFreeNight(Integer arrivalFreeNight) {
        this.arrivalFreeNight = arrivalFreeNight;
    }

    public Integer getNonArrivalFreeNight() {
        return nonArrivalFreeNight;
    }

    public void setNonArrivalFreeNight(Integer nonArrivalFreeNight) {
        this.nonArrivalFreeNight = nonArrivalFreeNight;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        if (this.currencyCode == null) {
            this.currencyCode = currencyCode;
        } else if (!this.currencyCode.equals(currencyCode)) {
            throw new RuntimeException("All rates must be in same currency.  " +
                    "Previous rates were specified in: " + this.currencyCode + " new rates are in: " + currencyCode);
        }
    }

    public BigDecimal getBaseRateBeforeTax() {
        return baseRateBeforeTax;
    }

    public void setBaseRateBeforeTax(BigDecimal baseRateBeforeTax) {
        this.baseRateBeforeTax = baseRateBeforeTax;
    }

    public BigDecimal getBaseRateAfterTax() {
        return baseRateAfterTax;
    }

    public void setBaseRateAfterTax(BigDecimal baseRateAfterTax) {
        this.baseRateAfterTax = baseRateAfterTax;
    }

    public BigDecimal getOnePersonRateBeforeTax() {
        return onePersonRateBeforeTax;
    }

    public void setOnePersonRateBeforeTax(BigDecimal onePersonRateBeforeTax) {
        this.onePersonRateBeforeTax = onePersonRateBeforeTax;
    }

    public BigDecimal getOnePersonRateAfterTax() {
        return onePersonRateAfterTax;
    }

    public void setOnePersonRateAfterTax(BigDecimal onePersonRateAfterTax) {
        this.onePersonRateAfterTax = onePersonRateAfterTax;
    }

    public BigDecimal getTwoPersonRateBeforeTax() {
        return twoPersonRateBeforeTax;
    }

    public void setTwoPersonRateBeforeTax(BigDecimal twoPersonRateBeforeTax) {
        this.twoPersonRateBeforeTax = twoPersonRateBeforeTax;
    }

    public BigDecimal getTwoPersonRateAfterTax() {
        return twoPersonRateAfterTax;
    }

    public void setTwoPersonRateAfterTax(BigDecimal twoPersonRateAfterTax) {
        this.twoPersonRateAfterTax = twoPersonRateAfterTax;
    }

    public BigDecimal getThreePersonRateBeforeTax() {
        return threePersonRateBeforeTax;
    }

    public void setThreePersonRateBeforeTax(BigDecimal threePersonRateBeforeTax) {
        this.threePersonRateBeforeTax = threePersonRateBeforeTax;
    }

    public BigDecimal getThreePersonRateAfterTax() {
        return threePersonRateAfterTax;
    }

    public void setThreePersonRateAfterTax(BigDecimal threePersonRateAfterTax) {
        this.threePersonRateAfterTax = threePersonRateAfterTax;
    }

    public BigDecimal getFourPersonRateBeforeTax() {
        return fourPersonRateBeforeTax;
    }

    public void setFourPersonRateBeforeTax(BigDecimal fourPersonRateBeforeTax) {
        this.fourPersonRateBeforeTax = fourPersonRateBeforeTax;
    }

    public BigDecimal getFourPersonRateAfterTax() {
        return fourPersonRateAfterTax;
    }

    public void setFourPersonRateAfterTax(BigDecimal fourPersonRateAfterTax) {
        this.fourPersonRateAfterTax = fourPersonRateAfterTax;
    }

    public OTAAgeQualifyingCode getAgeRateCode() {
        return ageRateCode;
    }

    public void setAgeRateCode (OTAAgeQualifyingCode ageRateCode) {
        this.ageRateCode = ageRateCode;
    }

    public BigDecimal getAgeQualifiedCodeBeforeTax() {
        return ageQualifiedCodeBeforeTax;
    }

    public void setAgeQualifiedCodeBeforeTax(BigDecimal ageQualifiedCodeBeforeTax) {
        this.ageQualifiedCodeBeforeTax = ageQualifiedCodeBeforeTax;
    }

    public BigDecimal getAgeQualifiedCodeAfterTax() {
        return ageQualifiedCodeAfterTax;
    }

    public void setAgeQualifiedCodeAfterTax(BigDecimal ageQualifiedCodeAfterTax) {
        this.ageQualifiedCodeAfterTax = ageQualifiedCodeAfterTax;
    }

    private  <T> T updateIfNotNull(T after, T before ) {
        return   after == null ? before : after;
    }
}





