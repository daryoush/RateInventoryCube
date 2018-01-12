package com.mehrsoft.service.original;

import com.mehrsoft.model.old.HotelRoomAvailability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.List;

/**
 * Created by daryoush_maxsam1 on 12/11/15.
 */
public class Utils {

    private final static Logger log = LoggerFactory.getLogger(Utils.class);

    public static  <T> void showObserver(Observable<T> d) {
        d.subscribe(new Subscriber<T>() {
            int ctr = 0;

            @Override
            public void onCompleted() {
                log.debug("OnCompleted " + " Number of messages received on OnNext: " + ctr);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("On Error With:", throwable);
            }

            @Override
            public void onNext(T d) {
                log.info("OnNext with: " + d);
                ctr++;
            }
        });
    }

    public static <T> void showSampledMsgObserver(Observable<Observable<T>> od) {
        od.subscribe(new Subscriber<Observable<T>>() {
            int ctr = 0;

            @Override
            public void onCompleted() {
                log.debug("OnCompleted " + " Number of messages received on OnNext: " + ctr);
            }

            @Override
            public void onError(Throwable throwable) {
                log.error("On Error With:", throwable);
            }


            @Override
            public void onNext(Observable<T> od) {
                log.info("OnNext with List Observable");
                od.toList().forEach(new Action1<List<T>>() {
                    @Override
                    public void call(List<T> ld) {
                        log.debug("List size:"+ ld.size());
                    }
                });
            }
        });
    }

    static public <T>  Observable.Transformer<T, HotelRoomAvailability> msgTransformer(final Func1<T, Observable<HotelRoomAvailability>> mapper) {
        return new Observable.Transformer<T, HotelRoomAvailability>() {
            @Override
            public Observable<HotelRoomAvailability> call(Observable<T> base) {
                return base
                        .concatMap(mapper)
                        .onErrorResumeNext(new Func1<Throwable, Observable<? extends HotelRoomAvailability>>() {
                            @Override
                            public Observable<? extends HotelRoomAvailability> call(Throwable throwable) {
                                throwable.printStackTrace();
                                log.error("DO SOMETING HERE", throwable);
                                return null;
                            }
                        });
            }
        };
    }
}
