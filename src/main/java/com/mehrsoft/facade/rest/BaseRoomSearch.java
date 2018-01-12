package com.mehrsoft.facade.rest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.math.BigDecimal;
import java.util.SortedSet;

/**
 * Created by ijet on 8/29/16.
 */
public class BaseRoomSearch {
    static public SortedSet<HotelRoomAvailability> applyFreeNightPolicy(SortedSet<HotelRoomAvailability> hras) {
        Multimap<ImmutablePair<String, String>, HotelRoomAvailability> roomRateAvailMap = HashMultimap.create();
        SortedSet<HotelRoomAvailability> res = Sets.newTreeSet();

        for (HotelRoomAvailability h : hras) {

            ImmutablePair<String, String> roomRate = new ImmutablePair<String, String>(h.getRateCategory(),
                    h.getRoomCategory());
            roomRateAvailMap.put(roomRate, h);

            Integer freeNightValue = h.getArrivalFreeNight();


            if (freeNightValue == null || freeNightValue > roomRateAvailMap.get(roomRate).size())
                res.add(h);
            else {
                roomRateAvailMap.removeAll(roomRate);   // zero the count;
                HotelRoomAvailability newHRA = new HotelRoomAvailability(h);
                newHRA.setBaseRateAfterTax(BigDecimal.ZERO);
                newHRA.setBaseRateBeforeTax(BigDecimal.ZERO);
                newHRA.setBaseRateAfterTax(BigDecimal.ZERO);
                newHRA.setOnePersonRateBeforeTax(BigDecimal.ZERO);
                newHRA.setOnePersonRateAfterTax(BigDecimal.ZERO);
                newHRA.setTwoPersonRateBeforeTax(BigDecimal.ZERO);
                newHRA.setTwoPersonRateAfterTax(BigDecimal.ZERO);
                newHRA.setThreePersonRateBeforeTax(BigDecimal.ZERO);
                newHRA.setThreePersonRateAfterTax(BigDecimal.ZERO);
                newHRA.setFourPersonRateBeforeTax(BigDecimal.ZERO);
                newHRA.setFourPersonRateAfterTax(BigDecimal.ZERO);
                newHRA.setAgeQualifiedCodeBeforeTax(BigDecimal.ZERO);
                newHRA.setAgeQualifiedCodeAfterTax(BigDecimal.ZERO);
                res.add(newHRA);
            }
        }
        return res;
    }
}
