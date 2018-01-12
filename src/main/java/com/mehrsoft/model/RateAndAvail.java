package com.mehrsoft.model;

/**
 * Created by ijet on 6/8/16.
 */


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Optional;

public class RateAndAvail {


    public static class RateAndAvailKey {
        final String hotelId;
        final String ratePlanCode;
        final String invCode;
        final Range<LocalDate> day;


        public RateAndAvailKey(String hotelId, String ratePlanCode, String invCode, LocalDate from, LocalDate to) {
            this.hotelId = hotelId;
            this.ratePlanCode = ratePlanCode;
            this.invCode = invCode;
            this.day = Range.open(from, to);
        }

        public String getHotelId() {
            return hotelId;
        }

        public String getRatePlanCode() {
            return ratePlanCode;
        }

        public String getInvCode() {
            return invCode;
        }

        public Range<LocalDate> getDay() {
            return day;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            RateAndAvailKey that = (RateAndAvailKey) o;

            if (hotelId != null ? !hotelId.equals(that.hotelId) : that.hotelId != null) return false;
            if (ratePlanCode != null ? !ratePlanCode.equals(that.ratePlanCode) : that.ratePlanCode != null)
                return false;
            if (invCode != null ? !invCode.equals(that.invCode) : that.invCode != null) return false;
            return day != null ? day.equals(that.day) : that.day == null;

        }

        @Override
        public int hashCode() {
            int result = hotelId != null ? hotelId.hashCode() : 0;
            result = 31 * result + (ratePlanCode != null ? ratePlanCode.hashCode() : 0);
            result = 31 * result + (invCode != null ? invCode.hashCode() : 0);
            result = 31 * result + (day != null ? day.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "RateAndAvailKey{" +
                    "hotelId='" + hotelId + '\'' +
                    ", ratePlanCode='" + ratePlanCode + '\'' +
                    ", invCode='" + invCode + '\'' +
                    ", day=" + day +
                    '}';
        }
    }

    class BaseByGuestAmts {
        final BaseByGuestAmt byGuestAmtSet[];
        final ExtraGuestAmt extraGuestAmts[];

        public BaseByGuestAmts(BaseByGuestAmt[] byGuestAmtSet, ExtraGuestAmt[] extraGuestAmts) {
            this.byGuestAmtSet = byGuestAmtSet;
            this.extraGuestAmts = extraGuestAmts;
        }
    }

    class BaseByGuestAmt extends Price {
        private final Integer NumberOfGuests;

        public BaseByGuestAmt(BigDecimal amountBeforeTax, BigDecimal amountAfterTax, String currencyCode,
                              Integer numberOfGuests) {
            super(amountBeforeTax, amountAfterTax, currencyCode);
            NumberOfGuests = numberOfGuests;
        }

        public Integer getNumberOfGuests() {
            return NumberOfGuests;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;

            BaseByGuestAmt that = (BaseByGuestAmt) o;

            return NumberOfGuests != null ? NumberOfGuests.equals(that.NumberOfGuests) : that.NumberOfGuests == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (NumberOfGuests != null ? NumberOfGuests.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "BaseByGuestAmt{" +
                    "NumberOfGuests=" + NumberOfGuests +
                    '}';
        }
    }

    class ExtraGuestAmt extends Price {
        private final Range<Integer> ageRange;
        private final String ageCode;

        public ExtraGuestAmt(BigDecimal amountBeforeTax, BigDecimal amountAfterTax, String currencyCode,
                             Range<Integer> ageRange, String ageCode) {
            super(amountBeforeTax, amountAfterTax, currencyCode);
            this.ageRange = ageRange;
            this.ageCode = ageCode;
        }

        public Range<Integer> getAgeRange() {
            return ageRange;
        }

        public String getAgeCode() {
            return ageCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            if (!super.equals(o))
                return false;

            ExtraGuestAmt that = (ExtraGuestAmt) o;

            if (ageRange != null ? !ageRange.equals(that.ageRange) : that.ageRange != null)
                return false;
            return ageCode != null ? ageCode.equals(that.ageCode) : that.ageCode == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (ageRange != null ? ageRange.hashCode() : 0);
            result = 31 * result + (ageCode != null ? ageCode.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ExtraGuestAmt{" +
                    "ageRange=" + ageRange +
                    ", ageCode='" + ageCode + '\'' +
                    '}';
        }
    }

    class Price {
        private final BigDecimal AmountBeforeTax;
        private final BigDecimal AmountAfterTax;
        private final String CurrencyCode;

        public Price(BigDecimal amountBeforeTax, BigDecimal amountAfterTax, String currencyCode) {
            AmountBeforeTax = amountBeforeTax;
            AmountAfterTax = amountAfterTax;
            CurrencyCode = currencyCode;
        }

        public BigDecimal getAmountBeforeTax() {
            return AmountBeforeTax;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Price price = (Price) o;

            if (AmountBeforeTax != null ?
                    !AmountBeforeTax.equals(price.AmountBeforeTax) :
                    price.AmountBeforeTax != null)
                return false;
            if (AmountAfterTax != null ? !AmountAfterTax.equals(price.AmountAfterTax) : price.AmountAfterTax != null)
                return false;
            return CurrencyCode != null ? CurrencyCode.equals(price.CurrencyCode) : price.CurrencyCode == null;

        }

        @Override
        public int hashCode() {
            int result = AmountBeforeTax != null ? AmountBeforeTax.hashCode() : 0;
            result = 31 * result + (AmountAfterTax != null ? AmountAfterTax.hashCode() : 0);
            result = 31 * result + (CurrencyCode != null ? CurrencyCode.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Price{" +
                    "AmountBeforeTax=" + AmountBeforeTax +
                    ", AmountAfterTax=" + AmountAfterTax +
                    ", CurrencyCode='" + CurrencyCode + '\'' +
                    '}';
        }
    }



    public RateAndAvail(String hotelId, String ratePlanCode, String invCode, LocalDate startDate, LocalDate endDate,
                        Optional<BigInteger> bookingLimit, Optional<Integer> arrivalMinLos,
                        Optional<Integer> nonArrivalMinLos, Optional<Integer> arrivalMaxLos,
                        Optional<Integer> nonArrivalMaxLos, Optional<Integer> arrivalForwardMinStay,
                        Optional<Integer> nonArrivalForwardMinStay, Optional<Integer> arrivalFreeNight,
                        Optional<Integer> nonArrivalFreeNight, Optional<RestrictionStatus> masterRestrictionStatus,
                        Optional<RestrictionStatus> arrivalRestrictionStatus,
                        Optional<RestrictionStatus> departureRestrictionStatus,
                        Optional<String> baseByGuestAmtsAsJson) {
        this.key=new RateAndAvailKey(hotelId, ratePlanCode, invCode, startDate, endDate);
        this.bookingLimit = bookingLimit;
        this.arrivalMinLos = arrivalMinLos;
        this.nonArrivalMinLos = nonArrivalMinLos;
        this.arrivalMaxLos = arrivalMaxLos;
        this.nonArrivalMaxLos = nonArrivalMaxLos;
        this.arrivalForwardMinStay = arrivalForwardMinStay;
        this.nonArrivalForwardMinStay = nonArrivalForwardMinStay;
        this.arrivalFreeNight = arrivalFreeNight;
        this.nonArrivalFreeNight = nonArrivalFreeNight;
        this.masterRestrictionStatus = masterRestrictionStatus;
        this.arrivalRestrictionStatus = arrivalRestrictionStatus;
        this.departureRestrictionStatus = departureRestrictionStatus;
        BaseByGuestAmtsAsJson = baseByGuestAmtsAsJson;
    }


    public RateAndAvail(RateAndAvailKey key) {
        this.key = key;
    }

    public RateAndAvail(String hotelId, String ratePlanCode, String invCode, LocalDate startDate, LocalDate endDate,
                        BigInteger bookingLimit, Integer arrivalMinLos,
                        Integer nonArrivalMinLos, Integer arrivalMaxLos,
                        Integer nonArrivalMaxLos, Integer arrivalForwardMinStay,
                        Integer nonArrivalForwardMinStay, Integer arrivalFreeNight,
                        Integer nonArrivalFreeNight, RestrictionStatus masterRestrictionStatus,
                        RestrictionStatus arrivalRestrictionStatus,
                        RestrictionStatus departureRestrictionStatus,
                        String baseByGuestAmtsAsJson) {

        this(hotelId, ratePlanCode, invCode, startDate, endDate,
                Optional.ofNullable(bookingLimit), Optional.ofNullable(arrivalMinLos),
                Optional.ofNullable(nonArrivalMinLos), Optional.ofNullable(arrivalMaxLos),
                Optional.ofNullable(nonArrivalMaxLos), Optional.ofNullable(arrivalForwardMinStay),
                Optional.ofNullable(nonArrivalForwardMinStay), Optional.ofNullable(arrivalFreeNight),
                Optional.ofNullable(nonArrivalFreeNight), Optional.ofNullable(masterRestrictionStatus),
                Optional.ofNullable(arrivalRestrictionStatus),
                Optional.ofNullable(departureRestrictionStatus),
                Optional.ofNullable(baseByGuestAmtsAsJson));

    }


    // Note a set of JSON or object would unset the other value
    // A get of Json or Obj tries to see if the value exists, otherwise it would try to generate it form the other
    // if the generation fails then either null or empty is returned.
    public String getBaseByGuestAmtsAsJson() {
        if (BaseByGuestAmtsAsJson.isPresent())
            return BaseByGuestAmtsAsJson.get();
        if (!baseByGuestAmts.isPresent()) { // Covert object to JSON, keep the JSON representation in case it is needed again
            try {
                BaseByGuestAmtsAsJson = Optional.of(mapper.writeValueAsString(baseByGuestAmts.get()));
                return BaseByGuestAmtsAsJson.get();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                throw new RuntimeException((e));
            }

        }
        return null;
    }


    public Optional<BaseByGuestAmts> getBaseByGuestAmts() {  // lazy conversion from JSON if present.
        if (baseByGuestAmts.isPresent())
            return baseByGuestAmts;
        else if (BaseByGuestAmtsAsJson.isPresent()) {
            try {
                baseByGuestAmts.of(mapper.readValue(BaseByGuestAmtsAsJson.get(), BaseByGuestAmts.class));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException((e));
            }
        }
        return baseByGuestAmts;  // it may be either empty from start, or Json was not present.
    }

    public void setBaseByGuestAmts(Optional<BaseByGuestAmts> baseByGuestAmts) {
        this.baseByGuestAmts = baseByGuestAmts;
        this.BaseByGuestAmtsAsJson = Optional.empty();
    }

    public void setBaseByGuestAmtsAsJson(Optional<String> baseByGuestAmtsAsJson) {
        BaseByGuestAmtsAsJson = baseByGuestAmtsAsJson;
        this.baseByGuestAmts = Optional.empty();   // clear the existing java object
    }


    static final ObjectMapper mapper = new ObjectMapper();


    final RateAndAvailKey key;

    /*
    Inventory and Restrictions should stay as attributes as saved in the database as columns.  This way query can
    filter out the items that are not valid for the search
     */
    // Inventory
    private Optional<BigInteger> bookingLimit = Optional.empty();

    // Length Of stays
    private Optional<Integer> arrivalMinLos = Optional.empty();
    private Optional<Integer> nonArrivalMinLos = Optional.empty();
    private Optional<Integer> arrivalMaxLos = Optional.empty();
    private Optional<Integer> nonArrivalMaxLos = Optional.empty();
    private Optional<Integer> arrivalForwardMinStay = Optional.empty();
    private Optional<Integer> nonArrivalForwardMinStay = Optional.empty();
    private Optional<Integer> arrivalFreeNight = Optional.empty();
    private Optional<Integer> nonArrivalFreeNight = Optional.empty();

    // Restrictions
    private Optional<RestrictionStatus> masterRestrictionStatus = Optional.empty();
    private Optional<RestrictionStatus> arrivalRestrictionStatus = Optional.empty();
    private Optional<RestrictionStatus> departureRestrictionStatus = Optional.empty();

    // Rate
    private Optional<String> BaseByGuestAmtsAsJson = Optional.empty();
    private Optional<BaseByGuestAmts> baseByGuestAmts = Optional.empty();



    public Optional<BigInteger> getBookingLimit() {
        return bookingLimit;
    }

    public void setBookingLimit(Optional<BigInteger> bookingLimit) {
        this.bookingLimit = bookingLimit;
    }

    public Optional<Integer> getArrivalMinLos() {
        return arrivalMinLos;
    }

    public void setArrivalMinLos(Optional<Integer> arrivalMinLos) {
        this.arrivalMinLos = arrivalMinLos;
    }

    public Optional<Integer> getNonArrivalMinLos() {
        return nonArrivalMinLos;
    }

    public void setNonArrivalMinLos(Optional<Integer> nonArrivalMinLos) {
        this.nonArrivalMinLos = nonArrivalMinLos;
    }

    public Optional<Integer> getArrivalMaxLos() {
        return arrivalMaxLos;
    }

    public void setArrivalMaxLos(Optional<Integer> arrivalMaxLos) {
        this.arrivalMaxLos = arrivalMaxLos;
    }

    public Optional<Integer> getNonArrivalMaxLos() {
        return nonArrivalMaxLos;
    }

    public void setNonArrivalMaxLos(Optional<Integer> nonArrivalMaxLos) {
        this.nonArrivalMaxLos = nonArrivalMaxLos;
    }

    public Optional<Integer> getArrivalForwardMinStay() {
        return arrivalForwardMinStay;
    }

    public void setArrivalForwardMinStay(Optional<Integer> arrivalForwardMinStay) {
        this.arrivalForwardMinStay = arrivalForwardMinStay;
    }

    public Optional<Integer> getNonArrivalForwardMinStay() {
        return nonArrivalForwardMinStay;
    }

    public void setNonArrivalForwardMinStay(Optional<Integer> nonArrivalForwardMinStay) {
        this.nonArrivalForwardMinStay = nonArrivalForwardMinStay;
    }

    public Optional<Integer> getArrivalFreeNight() {
        return arrivalFreeNight;
    }

    public void setArrivalFreeNight(Optional<Integer> arrivalFreeNight) {
        this.arrivalFreeNight = arrivalFreeNight;
    }

    public Optional<Integer> getNonArrivalFreeNight() {
        return nonArrivalFreeNight;
    }

    public void setNonArrivalFreeNight(Optional<Integer> nonArrivalFreeNight) {
        this.nonArrivalFreeNight = nonArrivalFreeNight;
    }

    public Optional<RestrictionStatus> getMasterRestrictionStatus() {
        return masterRestrictionStatus;
    }

    public void setMasterRestrictionStatus(Optional<RestrictionStatus> masterRestrictionStatus) {
        this.masterRestrictionStatus = masterRestrictionStatus;
    }

    public Optional<RestrictionStatus> getArrivalRestrictionStatus() {
        return arrivalRestrictionStatus;
    }

    public void setArrivalRestrictionStatus(Optional<RestrictionStatus> arrivalRestrictionStatus) {
        this.arrivalRestrictionStatus = arrivalRestrictionStatus;
    }

    public Optional<RestrictionStatus> getDepartureRestrictionStatus() {
        return departureRestrictionStatus;
    }

    public void setDepartureRestrictionStatus(Optional<RestrictionStatus> departureRestrictionStatus) {
        this.departureRestrictionStatus = departureRestrictionStatus;
    }

    public RateAndAvailKey getKey() {
        return key;
    }

    public void setRestriction(AvailabilityRestrictionType type, RestrictionStatus status) {
        Optional<RestrictionStatus> optionalOfStatus = Optional.of(status);
        switch(type) {
            case MASTER:
                masterRestrictionStatus = optionalOfStatus;
                break;
            case ARRIVAL:
                arrivalRestrictionStatus = optionalOfStatus;
                break;
            case DEPARTURE:
                departureRestrictionStatus = optionalOfStatus;
                break;
            default:
                throw new RuntimeException("Invalid Hotel restriction type was detected.  " + type);
        }
    }

    public void setLengthOfStay(AvailabilityLengthOfStayType type, boolean arrivalBased, int days) {
        Optional<Integer> optionalOfDays = Optional.of(days);
        switch (type) {
            case MINIMUM:
                if(arrivalBased) arrivalMinLos = optionalOfDays;
                else nonArrivalMinLos = optionalOfDays;
                break;
            case MAXIMUM:
                if(arrivalBased) arrivalMaxLos = optionalOfDays;
                else nonArrivalMaxLos = optionalOfDays;
                break;
            case PATTERN:
                throw new RuntimeException( "Full Pattern LOS is not suported by the model");
                //break;
            case SET_FORWARD_MIN_STAY:
                if(arrivalBased) arrivalForwardMinStay = optionalOfDays;
                else nonArrivalForwardMinStay = optionalOfDays;
                break;
            case FREE_NIGHT:
                if(arrivalBased) arrivalFreeNight = optionalOfDays;
                else nonArrivalFreeNight = optionalOfDays;
                break;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RateAndAvail that = (RateAndAvail) o;

        return key.equals(that.key);

    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

    @Override
    public String toString() {
        return "RateAndAvail{" +
                "key=" + key +
                ", bookingLimit=" + bookingLimit +
                ", arrivalMinLos=" + arrivalMinLos +
                ", nonArrivalMinLos=" + nonArrivalMinLos +
                ", arrivalMaxLos=" + arrivalMaxLos +
                ", nonArrivalMaxLos=" + nonArrivalMaxLos +
                ", arrivalForwardMinStay=" + arrivalForwardMinStay +
                ", nonArrivalForwardMinStay=" + nonArrivalForwardMinStay +
                ", arrivalFreeNight=" + arrivalFreeNight +
                ", nonArrivalFreeNight=" + nonArrivalFreeNight +
                ", masterRestrictionStatus=" + masterRestrictionStatus +
                ", arrivalRestrictionStatus=" + arrivalRestrictionStatus +
                ", departureRestrictionStatus=" + departureRestrictionStatus +
                ", BaseByGuestAmtsAsJson=" + BaseByGuestAmtsAsJson +
                ", baseByGuestAmts=" + baseByGuestAmts +
                '}';
    }

    static public class Builder {
         String ratePlanCode;
         String invCode;
        private LocalDate    from;
        private LocalDate    to;
        private String hotelId;

        public Builder from(LocalDate d) {
            this.from = d;
            return this;
        }

        public Builder to(LocalDate d) {
            this.to = d;
            return this;
        }

        public Builder from(String s) {
            this.from = LocalDate.parse(s);
            return this;
        }

        public Builder to(String s) {
            this.to = LocalDate.parse(s);
            return this;
        }

        public Builder ratePlanCode(String room) {
            this.ratePlanCode = room;
            return this;
        }

        public Builder invCode(String rate) {
            this.invCode = rate;
            return this;
        }
        public Builder hotelId(String hotelId) {
            this.hotelId = hotelId;
            return this;
        }

        public RateAndAvail build() {
            assert(!Strings.isNullOrEmpty(hotelId));
            assert(!Strings.isNullOrEmpty(invCode));
            assert(!Strings.isNullOrEmpty(ratePlanCode));
            assert(from != null);
            assert(to != null);

            return new RateAndAvail(new RateAndAvailKey(hotelId,  ratePlanCode,  invCode,from, to));

        }

    }

    static public enum AvailabilityRestrictionType {
        MASTER("M", "Master"),
        ARRIVAL("A", "Arrival"),
        DEPARTURE("D", "Departure");

        private String key;
        private String name;

        private AvailabilityRestrictionType(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public static AvailabilityRestrictionType fromString(String text) {
            for(AvailabilityRestrictionType x : AvailabilityRestrictionType.values()) {
                if(x.name.equals(text)) return x;
            }
            return null;
        }

    }

    public static enum AvailabilityLengthOfStayType {

        MINIMUM("N", "SetMinLOS"),
        MAXIMUM("X", "SetMaxLOS"),
        PATTERN("P", "FullPatternLOS"),
        SET_FORWARD_MIN_STAY("W", "SetForwardMinStay"),
        FREE_NIGHT("F", "FreeNight");

        private final static Logger log = LoggerFactory.getLogger(AvailabilityLengthOfStayType.class);
        private String key;
        private String name;

        private AvailabilityLengthOfStayType(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public static AvailabilityLengthOfStayType fromString(String text) {
            log.trace("AvailabilityLengthOfStayType lookup: " + text);
            for(AvailabilityLengthOfStayType x : AvailabilityLengthOfStayType.values()) {
                if(x.name.equals(text)) return x;
            }
            throw new RuntimeException("Failed to find enum for: " + text);
            //return null;
        }
    }


    static public enum RestrictionStatus {
        OPEN("O", "Open"),
        CLOSE("D", "Close");

        private String key;
        private String name;

        private RestrictionStatus(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public static RestrictionStatus fromString(String text) {
            for (RestrictionStatus x : RestrictionStatus.values()) {
                if (x.name.equals(text)) return x;
            }
            return null;
        }
    }

}
