package com.mehrsoft.queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;

import java.util.Queue;
import java.util.concurrent.*;

import static rx.schedulers.Schedulers.from;

/**
 * Created by ijet on 5/22/16.
 */
public class ObservableBlockingQueue<X> {
    private final static Logger log = LoggerFactory.getLogger(ObservableBlockingQueue.class);
    final BlockingQueue<X> incommingMsgQueue;
    boolean terminate = false;
    ExecutorService subscribedOnPool;

    public ObservableBlockingQueue() {
        this(1);
    }

    public ObservableBlockingQueue(int size) {
        incommingMsgQueue = new ArrayBlockingQueue<X>(size) ;
    }


    public BlockingQueue<X> getQueue() {
        return incommingMsgQueue;
    }
//
//    public Observable<X> getQueueObservable() {
//             return Observable.from(incommingMsgQueue); // Correct?
//    }


    public void terminate() {
        terminate = true;
        subscribedOnPool.shutdownNow();  // to interrupt the take calls.
    }
    public synchronized  Observable<X>  getQueueObservable() {
        subscribedOnPool = Executors.newFixedThreadPool(1, new NamedThreadFactory("SubscribedOnPool"));
        Observable<X> observable = Observable
                    .create(new Observable.OnSubscribe<X>() {
                        @Override
                        public void call(final Subscriber<? super X> subscriber) {
                            subscriber.setProducer(producerFromQueue(subscriber));
                        }
                    })


                    //TODO see https://github.com/davidmoten/rxjava-slf4j for loging

                    //.onBackpressureBuffer(10)
                    // NEED TO MAKE SURE PROCESSING OF THE QUEUE IS SINGLE THREADED.  WOULD THIS DO THE JOB?

                  /*
                  see http://www.grahamlea.com/2014/07/rxjava-threading-examples/
                  observeOn() is similar to subscribeOn(), except that it defines the Scheduler
                  to use only from the point it is added to the chain onwards, whereas subscribeOn()
                   defines the Scheduler used at the generation end of the chain.

                   */

                    .subscribeOn(from(subscribedOnPool))
            // .observeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("ObservedOnPool"))))
            ;

        return observable;


    }
    private Producer producerFromQueue(final Subscriber<? super X> subscriber) {
        Producer producer = new Producer() {
            @Override
            public void request(long l) {
                for (long i = 0; i < l; i++) {
                    if(terminate) {
                        System.out.println(">>>>>>>> TERMINATED, complete the subscriber");
                        subscriber.onCompleted();
                        subscriber.unsubscribe();
                    }
                    try {
                        subscriber.onNext(incommingMsgQueue.take());
                    } catch (InterruptedException e) {
                        log.info("InterruptedException");
                        subscriber.onCompleted();
                        subscriber.unsubscribe();
                        // need to re throw???
                    }
                }
            }
        };

        return producer;
    }

    static int factoryctr = 100;
    private class NamedThreadFactory implements ThreadFactory {
        int ctr = 0;
        String name;

        public NamedThreadFactory(String name) {
            this.name = name + ":"+factoryctr++;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(name + "-" + ctr++);
            return t;
        }
    }
}
