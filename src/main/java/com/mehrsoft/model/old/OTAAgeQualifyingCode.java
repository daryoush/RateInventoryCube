package com.mehrsoft.model.old;

/**
 * Created by ijet on 8/24/16.
 */
public enum OTAAgeQualifyingCode {
    OVER_21,
    OVER_65,
    UNDER_2,
    UNDER_12,
    UNDER_17,
    UNDER_21,
    INFANT,
    CHILD,
    TEENAGER,
    ADULT,
    SENIOR,
    ADDITIONAL_OCCUPANT_WITH_ADULT,
    ADDITIONAL_OCCUPANT_WITHOUT_ADULT,
    FREE_CHILD,
    FREE_ADULT,
    YOUNG_DRIVER,
    YOUNGER_DRIVER,
    UNDER_10;

    public static OTAAgeQualifyingCode from(String code) {
        try {
            Integer ordinal = Integer.valueOf(code) - 1;
            return values()[ordinal];
        } catch (NumberFormatException|ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }
}
