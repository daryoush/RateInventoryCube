package com.mehrsoft.service.original;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import static rx.schedulers.Schedulers.from;

/**
 * Created by daryoush_maxsam1 on 12/11/15.
 */
public class BlockingQueueService<T> {
    private final static Logger log = LoggerFactory.getLogger(BlockingQueueService.class);
    final int defaultSize = 1;


    // TODO:   Add option to poll or take
   final BlockingQueue<T> incommingMsgQueue;

     public BlockingQueueService() {
         this(1);
    }

     BlockingQueueService(int size) {
        incommingMsgQueue = new ArrayBlockingQueue<T>(size) ;
    }

    public void put(T t) throws InterruptedException {
        incommingMsgQueue.put(t);
    }

    public int size() {
        return incommingMsgQueue.size();
    }

    public Observable<T>  getObservable() {
        return Observable
                .create(new Observable.OnSubscribe<T>() {
                    @Override
                    public void call(final Subscriber<? super T> subscriber) {
                        subscriber.setProducer(producerFromQueue(subscriber));
                    }
                })

                //.onBackpressureBuffer(10)
                // NEED TO MAKE SURE PROCESSING OF THE QUEUE IS SINGLE THREADED.  WOULD THIS DO THE JOB?
                .subscribeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("SubscribedOnPool"))))
                .observeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("ObservedOnPool"))));

    }
    private Producer producerFromQueue(final Subscriber<? super T> subscriber) {
        return new Producer() {
            @Override
            public void request(long l) {
                for (long i = 0; i < l; i++) {
                    try {
                        subscriber.onNext(incommingMsgQueue.take());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        subscriber.onError(e);
                    }
                }
            }
        };
    }
}
