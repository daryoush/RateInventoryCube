package com.mehrsoft.model.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ijet on 8/24/16.
 */
public enum HotelAvailabilityLengthOfStayType {

    MINIMUM("N", "SetMinLOS"),
    MAXIMUM("X", "SetMaxLOS"),
    PATTERN("P", "FullPatternLOS"),
    SET_FORWARD_MIN_STAY("W", "SetForwardMinStay"),
    FREE_NIGHT("F", "FreeNight");

    private final static Logger log = LoggerFactory.getLogger(HotelAvailabilityLengthOfStayType.class);
    private String key;
    private String name;

    private HotelAvailabilityLengthOfStayType(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public static HotelAvailabilityLengthOfStayType fromString(String text) {
        log.trace("HotelAvailabilityLengthOfStayType lookup: " + text);
        for (HotelAvailabilityLengthOfStayType x : HotelAvailabilityLengthOfStayType.values()) {
            if (x.name.equals(text)) return x;
        }
        throw new RuntimeException("Failed to find enum for: " + text);
        //return null;
    }


    public enum HotelAvailabilityRestrictionType {
        MASTER("M", "Master"),
        ARRIVAL("A", "Arrival"),
        DEPARTURE("D", "Departure");

        private String key;
        private String name;

        private HotelAvailabilityRestrictionType(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public static HotelAvailabilityRestrictionType fromString(String text) {
            for (HotelAvailabilityRestrictionType x : HotelAvailabilityRestrictionType.values()) {
                if (x.name.equals(text)) return x;
            }
            return null;
        }


    }

}