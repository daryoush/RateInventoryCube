package com.mehrsoft.service;

import com.github.davidmoten.rx.slf4j.Logging;
import com.github.davidmoten.rx.slf4j.OperatorLogging;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.mehrsoft.model.RateAndAvail;
import org.opentravel.ota._2003._05.OTAHotelAvailNotifRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;

/**
 * Created by ijet on 6/3/16.
 */
@Singleton
public abstract class PersistenceService<T> {
    private final static Logger log = LoggerFactory.getLogger(PersistenceService.class);


    @Inject
    @Named("IncomingAvailNotifObservable")
    Observable<OTAHotelAvailNotifRQ> availNotifObs;

    abstract Observable<RateAndAvail> convert(T m);

    // https://github.com/davidmoten/rxjava-slf4j
//    OperatorLogging<T> logOperation = Logging.<T>logger().showCount("total")
//            .onCompleted("finished rate")
//            .log();

    static protected  <X> Observable.Operator<X, X> logOperator(Logging.Level level) {
        OperatorLogging<X> log = Logging.<X>logger().showCount("total")
                .onCompleted("finished")
                .onCompleted(Logging.Level.INFO)
                // set the error logging level
                .onError(Logging.Level.ERROR)
                // onNext at debug level
                .onNext(level)
                .log();
        return log;
    }


}
