package com.mehrsoft.facade.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.Sets;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.opentravel.ota._2003._05.RoomStayType;
import org.opentravel.ota._2003._05.RoomStaysType;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ijet on 8/26/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AggregateHotelRoomAvailability {

    SortedSet<HotelRoomAvailability> hras = Sets.newTreeSet();
    BigDecimal totalPrice = null;
    String id = UUID.randomUUID().toString();


    //Set<String> roomCategory = Sets.newHashSet();


    public AggregateHotelRoomAvailability(Map<Long, HotelRoomAvailability> allAvailByIdMap, Array array, BigDecimal estimate) {
        try {
            totalPrice = estimate;  // FOR NOW
            Integer[] ids = (Integer[]) array.getArray();
            for (int i = 0; i < ids.length; i++) {
                HotelRoomAvailability roomAvailability = allAvailByIdMap.get(Long.valueOf(ids[i]));
                hras.add(roomAvailability);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public AggregateHotelRoomAvailability(HotelRoomAvailability[] in) {
        this.hras = Sets.newTreeSet(Arrays.asList(in));
    }

    public AggregateHotelRoomAvailability(SortedSet<HotelRoomAvailability> in) {
        this.hras = in;
    }

    public AggregateHotelRoomAvailability(Long hotel_id, String roomCategory, Integer count,
                                          Array rateCategory,
                                          Array ids,
                                          Array dates, Array bookingLimits,
                                          Array masterRestrictionStatuses,
                                          Array arrivalRestrictionStatuses,
                                          Array departureRestrictionStatuses, Array arrivalMinLoses,
                                          Array nonArrivalMinLoses, Array arrivalMaxLoses, Array nonArrivalMaxLoses,
                                          Array arrivalForwardMinStayes, Array nonArrivalForwardMinStayes, Array arrivalFreeNights,
                                          Array nonArrivalFreeNights, Array currencyCodes, Array baseRateBeforeTaxes,
                                          Array baseRateAfterTaxes, Array onePersonRateBeforeTaxes,
                                          Array onePersonRateAfterTaxes, Array twoPersonRateBeforeTaxes,
                                          Array twoPersonRateAfterTaxes, Array threePersonRateBeforeTaxes,
                                          Array threePersonRateAfterTaxes, Array fourPersonRateBeforeTaxes,
                                          Array fourPersonRateAfterTaxes, Array ageRateCodes,
                                          Array ageQualifiedCodeBeforeTaxes, Array ageQualifiedCodeAfterTaxes
    ) throws SQLException {

        for (int i = 0; i < count; i++) {
            hras.add(new HotelRoomAvailability(((Integer[]) ids.getArray())[i].longValue(),  // not sure why query in case of aggregate returning long
                    hotel_id,
                    ((String[]) rateCategory.getArray())[i],
                    roomCategory,
                    ((Date[]) dates.getArray())[i],
                    ((Integer[]) bookingLimits.getArray())[i],
                    ((String[]) masterRestrictionStatuses.getArray())[i],
                    ((String[]) arrivalRestrictionStatuses.getArray())[i],
                    ((String[]) departureRestrictionStatuses.getArray())[i],
                    ((Integer[]) arrivalMinLoses.getArray())[i],
                    ((Integer[]) nonArrivalMinLoses.getArray())[i],
                    ((Integer[]) arrivalMaxLoses.getArray())[i],
                    ((Integer[]) nonArrivalMaxLoses.getArray())[i],
                    ((Integer[]) arrivalForwardMinStayes.getArray())[i],
                    ((Integer[]) nonArrivalForwardMinStayes.getArray())[i],
                    ((Integer[]) arrivalFreeNights.getArray())[i],
                    ((Integer[]) nonArrivalFreeNights.getArray())[i],
                    ((String[]) currencyCodes.getArray())[i],
                    ((BigDecimal[]) baseRateBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) baseRateAfterTaxes.getArray())[i],
                    ((BigDecimal[]) onePersonRateBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) onePersonRateAfterTaxes.getArray())[i],
                    ((BigDecimal[]) twoPersonRateBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) twoPersonRateAfterTaxes.getArray())[i],
                    ((BigDecimal[]) threePersonRateAfterTaxes.getArray())[i],
                    ((BigDecimal[]) threePersonRateBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) fourPersonRateBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) fourPersonRateAfterTaxes.getArray())[i],
                    "" + ((Integer[]) ageRateCodes.getArray())[i],
                    ((BigDecimal[]) ageQualifiedCodeBeforeTaxes.getArray())[i],
                    ((BigDecimal[]) ageQualifiedCodeAfterTaxes.getArray())[i])
            );
        }

    }

    public AggregateHotelRoomAvailability() {

    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "AggregateHotelRoomAvailability{" +
                "hras=" + hras +
                ", totalPrice=" + totalPrice +
                '}';
    }

    public SortedSet<HotelRoomAvailability> getHras() {
        return hras;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice != null ? totalPrice : hras.stream().map(HotelRoomAvailability::getOnePersonRateAfterTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Set<String> getRooms() {
        return hras.stream().map(x -> x.getRoomCategory()).collect(Collectors.toSet());
    }

    public Set<String> getRates() {
        return hras.stream().map(x -> x.getRateCategory()).collect(Collectors.toSet());
    }

    public Integer getMaxBookingLimit() {
        return hras.stream().map(HotelRoomAvailability::getBookingLimit).reduce(
                Integer.MAX_VALUE,
                Math::min);
    }

    public RoomStaysType getResRqRoomStay() {
        RoomStaysType foldSeed = new RoomStaysType();

        RoomStayType.RoomRates roomRates = new RoomStayType.RoomRates();
        RoomStayType.RoomRates.RoomRate rr = new RoomStayType.RoomRates.RoomRate();
        RoomStaysType.RoomStay rs = new RoomStaysType.RoomStay();
        foldSeed.getRoomStay().add(rs);
        rs.setRoomRates(roomRates);
        RoomStaysType res = hras.stream()
                .map(HotelRoomAvailability::toRoomStay)
                .reduce(foldSeed, (x, y) -> {
                    // add the rooms, rates and from y to x
                    x.getRoomStay().get(0).getRoomRates().getRoomRate().addAll(y.getRoomStay().get(0).getRoomRates().getRoomRate());
                    // todo add roomtype and rate plan also

                    return x;

                });

        return res;
    }


}

