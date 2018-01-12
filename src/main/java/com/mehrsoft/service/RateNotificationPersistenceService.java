package com.mehrsoft.service;

import com.github.davidmoten.rx.slf4j.Logging;
import com.google.inject.name.Named;
import com.mehrsoft.model.RateAndAvail;
import org.opentravel.ota._2003._05.OTAHotelRateAmountNotifRQ;
import rx.Observable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Created by ijet on 6/17/16.
 */
public class RateNotificationPersistenceService extends  PersistenceService<OTAHotelRateAmountNotifRQ> {

    @Inject
    @Named("IncomingRateNotifObservable")
    Observable<OTAHotelRateAmountNotifRQ> rateNotifObs;

    private void configureRateObservable() {

        rateNotifObs
                .lift(logOperator(Logging.Level.TRACE))
                .subscribe()
        ;
    }



    @PostConstruct
    public void init() {
        configureRateObservable();
    }

    @Override
    Observable<RateAndAvail> convert(OTAHotelRateAmountNotifRQ m) {
        return null;
    }
}
