package com.mehrsoft.service;

import com.github.davidmoten.rx.slf4j.Logging;
import com.github.davidmoten.rx.slf4j.OperatorLogging;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mehrsoft.model.RateAndAvail;
import org.opentravel.ota._2003._05.AvailStatusMessageType;
import org.opentravel.ota._2003._05.LengthsOfStayType;
import org.opentravel.ota._2003._05.OTAHotelAvailNotifRQ;
import org.opentravel.ota._2003._05.StatusApplicationControlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.observables.GroupedObservable;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Created by ijet on 6/17/16.
 */
public class AvailNotificationPersistenceService extends  PersistenceService<OTAHotelAvailNotifRQ> {

    private final static Logger log = LoggerFactory.getLogger(AvailNotificationPersistenceService.class);

    private void configureAvailObservable() {
        availNotifObs
                .lift(logOperator(Logging.Level.TRACE))
                .concatMap(this::convert)    // Each message is broken up into its pieces by date range, it can stil be combined before persistence
                .lift(logOperator(Logging.Level.DEBUG)) // Log the   RateAndAvail Object in the message.
                .subscribe()
                ;

    }


    //TODO  Merge based on keys

    public static final String SET_LIMIT_MESSAGE = "SetLimit";
    @Override
    Observable<RateAndAvail> convert(OTAHotelAvailNotifRQ m) {
        List<RateAndAvail> results = Lists.newArrayList();

        String hotelCode = getHotelCode(m);
        log.debug(" hotel: {} ", hotelCode);


        for (AvailStatusMessageType asm : m.getAvailStatusMessages().getAvailStatusMessage()) {
            StatusApplicationControlType sac = asm.getStatusApplicationControl();
            String start = sac.getStart();
            String end = getEndDate(sac);

            RateAndAvail r = new  RateAndAvail.Builder().from(start).to(end).hotelId(hotelCode)
                    .invCode(sac.getInvCode())
                    .ratePlanCode(sac.getRatePlanCode()).build();

            if (SET_LIMIT_MESSAGE.equals(asm.getBookingLimitMessageType())) {
                r.setBookingLimit(Optional.of(asm.getBookingLimit()));   // Can't be null if set limit is set.
            }
            // TODO check for DAY los unit
            if(asm.getLengthsOfStay() != null) {
                for(LengthsOfStayType.LengthOfStay otaLos : asm.getLengthsOfStay().getLengthOfStay()) {
                    r.setLengthOfStay(RateAndAvail.AvailabilityLengthOfStayType.fromString(otaLos.getMinMaxMessageType()),
                            asm.getLengthsOfStay().getArrivalDateBased().booleanValue(),otaLos.getTime().intValue());
                }
            }
            if(asm.getRestrictionStatus() != null ) {
                r.setRestriction(RateAndAvail.AvailabilityRestrictionType.fromString(asm.getRestrictionStatus().getRestriction()),
                        RateAndAvail.RestrictionStatus.fromString(asm.getRestrictionStatus().getStatus().value()));
            }

            results.add(r);
        }
        return Observable.from(results);


    }


    // Test messages have issue with end date,  The end date should be incremented by 1 day.
    private String getEndDate(StatusApplicationControlType sac) {
        LocalDate endfromMessage = LocalDate.parse(sac.getEnd());
        return endfromMessage.plusDays(1).toString();
    }


    protected String getHotelCode(OTAHotelAvailNotifRQ request) {
        String hotelCode = request.getAvailStatusMessages().getHotelCode();

        // Try chain code if hotel code is not set.
        if(Strings.isNullOrEmpty(hotelCode)) {
            log.debug("empty, hotel code, use  ChainCode instead.");
            hotelCode = request.getAvailStatusMessages().getChainCode();
        }
        return hotelCode;
    }
    @PostConstruct
    public void init() {
        configureAvailObservable();
    }



}
