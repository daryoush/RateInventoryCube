package com.mehrsoft.reactive;

import com.github.davidmoten.rx.slf4j.Logging;
import org.junit.Test;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static rx.schedulers.Schedulers.from;

/**
 * Created by ijet on 7/2/16.
 */
public class ETLTest {

    static int data  = 0;

    @Test
    public void testBuilder() {
        Observable<Observable<Integer>> ooi = Observable
                .create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(final Subscriber<? super Integer> subscriber) {
                        subscriber.setProducer(new Producer() {
                            @Override
                            public void request(long l) {
                                for (long i = 0; i < l; i++) {
                                    try {
                                        Thread.sleep(10);
                                        subscriber.onNext(data++);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        subscriber.onError(e);
                                    }
                                }
                            }
                        });
                    }
                })

                //.onBackpressureBuffer(10)
                // NEED TO MAKE SURE PROCESSING OF THE QUEUE IS SINGLE THREADED.  WOULD THIS DO THE JOB?
                .subscribeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("SubscribedOnPool"))))
                .observeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("ObservedOnPool"))))
                .window(1, TimeUnit.SECONDS);

        ooi.lift(Logging.logger().onNext(Logging.Level.DEBUG).onCompleted("finished")
                        .onCompleted(Logging.Level.INFO)
                        .onError(Logging.Level.ERROR).log()).subscribe();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        ooi.compose(new ETL.Builder<Integer, String, Integer, Boolean>()
//                .transformFunction(Object::toString)
//                .groupingFunction(String::length)
//                .loadingFunction((i, s) -> {
//                    System.out.println("Save key: " + i + " Value " + s);
//                    return true;
//                })
//                .build())
//                .lift(Logging.<Boolean>logger().showCount("total")
//                        .onCompleted("finished")
//                        .onCompleted(Logging.Level.INFO)
//                        // set the error logging level
//                        .onError(Logging.Level.ERROR)
//                        // onNext at debug level
//                        .onNext(Logging.Level.DEBUG)
//                        .log()) // Log the   RateAndAvail Object in the message.
//                .subscribe()
//        ;
    }
}