package com.mehrsoft.reactive;

import com.github.davidmoten.rx.slf4j.Logging;
import com.github.davidmoten.rx.slf4j.OperatorLogging;
import javafx.util.Pair;
import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import static rx.schedulers.Schedulers.from;
/**
 * Created by ijet on 6/26/16.
 */

/*

I: Incoming Data
M: Intermediate data
G: Grouping Key
R: Result After load

Observable<I> -> Observable<Observable<I>>    -- due to windowing at the source
ETL table Observable<Observable<I> and tries to transform and load the values
using transformer function I -> Iterable<M>  exapnd each message
Grouper function M -> Key
Merger (Key, Iterable<M> ) -> M
Loader M -> Status

Idea is that the caller passes in the basic functions
Builder convert those to Observables
ETL takes Observables and makes them work in the Observable<Observable>> type.

 */

    //TODO need to consolidate
    // get count of saved messages every minute
    // generate random # from zero 9999  see hoe many of each group is saved
public class ETL<I, M, G, R> implements  Observable.Transformer<Observable<I>, R> {

    final Func1< Observable<? extends I>,  Observable<R>> transformer;
//    final Func1<Observable< ? extends  M>, Observable< GroupedObservable<G,  M>>> grouper;
//    final Func2<? super G, ? super  Observable<M>, Observable<? extends  R>> loader;

    private ETL(Func1< Observable<? extends I>,  Observable<R>> transformer
//                Func1<Observable<? extends  M>, Observable<GroupedObservable< G,  M>>> grouper,
//                Func2<? super G, ? super  Observable<M>, Observable<? extends  R>> loader
    ) {
        this.transformer = transformer;
//        this.grouper = grouper;
//        this.loader = loader;
    }

    @Override
    public Observable<R> call(Observable<Observable<I>> oOI) {

        /*
        x is Obs<Obs<i>>   where the Obs<I> is finite but outer observer is infinite stream
        so x.concatMap  to an function that takes a finite Obs<i> to Obs<m>  where m is a single status
        for saving of all elements in Obs<i>
        so the transformer consumes all the Obs<I> and returns a single value observable

        obs<I> -> transformed to obs<m> (one to many)   ->  many is grouped  -> each group is consolidated (so we have group and single
        message to save for each group  -> all groups (one msg per group) is saved  to return a success or failure for the group

        From functions the builder builds the above transofmration

        ETL applies the transformer to the stream
         */
        return oOI.compose(x -> x
                .concatMap(transformer)
//                .concatMap(grouper)
//                .concatMap(g-> loader.call(g.getKey(), g))
        );
    }

    public static class Builder<I, M, G, R>{
        private  Func1<? super I,M>  transformFunction;
        private  Func1<M, G> groupingFunction;
        private  Func2<G, M, R> loaderFunction;

        public ETL<I, M, G, R> build() {
            Func1< Observable<? extends I>,  Observable<R>> transformer = null;
            //TODO if functions are null use identify function.
            assert (transformFunction != null);
            assert (groupingFunction != null);
            assert (loaderFunction != null);
//            Func1<Observable<I>, Observable< Observable<M>>> transformer =  null;  // TODO FIX THIS i -> Observable.from(i.map(transformFunction));
//            Func1<Observable<M>, Observable<GroupedObservable<G, M>>> grouper = m -> m.groupBy(groupingFunction);
//            Func2<G, Observable< M>, Observable<R>> loader = (g, m) -> m.map(x -> loaderFunction.call(g, x));
            return new ETL(transformer);
        }

        Builder<I, M, G, R> transformFunction(Func1<? super I,M>  t) {
             this.transformFunction = t;
            return this;
        }

        Builder<I, M, G, R> groupingFunction(Func1<M, G>  g) {
            this.groupingFunction = g;
            return this;
        }

        Builder<I, M, G, R> loadingFunction(Func2<G, M, R>  l) {
            this.loaderFunction = l;
            return this;
        }
    }
}

class NamedThreadFactory implements ThreadFactory {

    int ctr = 0;
    String name;

    public NamedThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(name + "-" + ctr++);
        return t;
    }
}

/*
//
//
//import com.appleleisuregroup.reactive.NamedThreadFactory;
//import rx.Observable;
//import rx.Producer;
//import rx.Subscriber;
//import rx.functions.Func1;
//import rx.observables.GroupedObservable;
//
//import java.util.concurrent.Executors;
//import java.util.concurrent.TimeUnit;
//
//import static rx.schedulers.Schedulers.from;
//
//
//public class TryObs {
//
//
//   /* idea is that extract generates data in obs<obs<XXX>>   because data is going to be bunched ip into smaller pacakges
//   there is data transformation function that is composed with the data compose(Transfomation(transformFunction))
//   where transforom function gets an item in the data and transforms is.  The transfomation essentially lifts that to the
//   compulation
//   compose(Grouping(grouping Function))   where the transformed data is grouped
//   compose(Loader(loading function))  loading function takes one group of data and loads it.
//
//   Exception at eveyr level must be dealt with correctly.
//    */
//
//public enum  Status {
//    SUCCESS,
//    FAILURE;
//}
//
//
//
//    public void doWork() {
//        Observable<Observable<Integer>> src = extract();   // TO be injected
//
//        Observable<Status> res = src.compose(new Observable.Transformer<Observable<Integer>, Status>() {
//            @Override public Observable<Status> call(Observable<Observable<Integer>> ooInt) {
//
//                // Question is concat map lazy?  or should I use for each?
//                Observable<Observable<String>> ooString = ooInt.concatMap(new Func1<Observable<Integer>, Observable<? extends Observable<String>>>() {
//                    @Override
//                    public Observable<? extends Observable<String>> call(Observable<Integer> oI) {
//                        Observable<String> oS = oI.map(getTransformationFunction());
//                        return Observable.just(oS);
//                    }
//                });
//
//                Observable<GroupedObservable<Integer, String>> ooSGrp = ooString.concatMap(new Func1<Observable<String>, Observable<? extends GroupedObservable<Integer, String>>>() {
//                    @Override
//                    public Observable<? extends GroupedObservable<Integer, String>> call(
//                            Observable<String> sO) {
//                        return sO.groupBy(getGroupingFunction());
//                    }
//                });
//
//                // print key(stirng length), list, length of list [That is persist], return status, introduce errors on save
//
//                return  null;
//            }
//        });
//    }
//
//
//    public void doWork2() {
//        Observable<Observable<Integer>> src = extract();   // TO be injected
//
//        Observable<Status> res = src.compose(new Observable.Transformer<Observable<Integer>, Status>() {
//            @Override public Observable<Status> call(Observable<Observable<Integer>> ooInt) {
//
//                // Question is concat map lazy?  or should I use for each?
//                Observable<Observable<String>> ooString = ooInt.concatMap(oI -> Observable.just(oI.map(
//                        getTransformationFunction())));
//                Observable<GroupedObservable<Integer, String>> ooSGrp = ooString.concatMap(ooS -> ooS.groupBy(
//                        getGroupingFunction()));
//
//                // save the ooStringGroup and return status
//
//                return  null;
//            }
//        });
//    }
//
//    public void doWork3() {
//        Observable<Observable<Integer>> src = extract();   // TO be injected
//
//        Observable<Status> res = src.compose(new Observable.Transformer<Observable<Integer>, Status>() {
//            @Override public Observable<Status> call(Observable<Observable<Integer>> ooInt) {
//
//                // Question is concat map lazy?  or should I use for each?
//                Observable<Observable<String>> ooString = ooInt
//                        .concatMap(Transformer())
//                        .concatMap(grouper());
//
//                // save the ooStringGroup and return status
//
//                return  null;
//            }
//        });
//    }
//
//
//    public Observable<Status> doWork4() {    // should happen in derived class, base data is injected as obs<obs<x>>
//        // get methods are implemented in the class, the transformer and grouper apply the function to the observables
//        //  may be concatMap(transformer2(x))   and grouper should be composed
//        Observable<Observable<String>>  ooS =
//                extract()   // TO be injected
//                        .concatMap(Transformer2(getTransformationFunction()))
//                        .concatMap(grouper2(getGroupingFunction()))
//                ;
//
//
//    }
//
//    public Observable<Observable<Status>> doWork5() {    // should happen in derived class, base data is injected as obs<obs<x>>
//        // get methods are implemented in the class, the transformer and grouper apply the function to the observables
//        //  may be concatMap(transformer2(x))   and grouper should be composed
//        final Observable.Transformer<Observable<Integer>, Observable<Status>> etl = x -> x
//                .concatMap(Transformer2(getTransformationFunction()))
//                .concatMap(grouper2(getGroupingFunction()))
//                .concatMap(getLoader());
//
//        Observable<Observable<Status>> ooS =
//                extract()   // TO be injected
//                        .compose(etl);
//
//        return ooS;
//
//    }
//
//
//
//
//    public Func1<? super Observable<String>,? extends Observable<? extends Observable<Status>>> getLoader() {
//        return null;
//    }
//
//    private Func1<Observable<String>, Observable<? extends Observable<String>>> grouper() {  // IN BASE CLASS
//        return ooS -> {
//            final Func1<String, Integer> groupingFunction = getGroupingFunction();
//            return ooS.groupBy(groupingFunction);
//        };
//    }
//
//    private Func1<Observable<Integer>, Observable<? extends Observable<String>>> Transformer() {  // IN BASE CALSS
//        return oI -> {
//            final Func1<Integer, String> transformationFunction = getTransformationFunction();
//            return Observable.just(oI.map(transformationFunction));
//        };
//    }
//
//
//    private Func1<Observable<String>, Observable<? extends Observable<String>>> grouper2(final Func1<String, Integer> groupingFunction) {  // IN BASE CLASS
//        return ooS -> {
//            return ooS.groupBy(groupingFunction);
//        };
//    }
//
//    private Func1<Observable<Integer>, Observable<? extends Observable<String>>> Transformer2(final Func1<Integer, String> transformationFunction) {  // IN BASE CALSS
//        return oI -> {
//            return Observable.just(oI.map(transformationFunction));
//        };
//    }
//
//    private  static Func1<String, Integer> getGroupingFunction() {   // SHOULD BE IN THE DERIVED CLASS
//        return String::length;
//    }
//
//    private static Func1<Integer, String> getTransformationFunction() {  // SHOULD BE IN DERIVED CLASS
//        return Object::toString;
//    }
//    // assume I have transformer for finite observable of int to obs of string  grouped by the lengtho fo string  -- introdcue random error
//
//    //  assume I have prersistence transformer  that takes string and write saved: "batch id:  (local ctr) string xxxx"  -- introduce randowm errors
//
//    // compose the two transformer show that they close in between the windown and errors are handled locally
//
//
//    public Observable<Observable<Integer>> extract() {
//        return Observable
//                .create(new Observable.OnSubscribe<Integer>() {
//                    @Override
//                    public void call(final Subscriber<? super Integer> subscriber) {
//                        subscriber.setProducer(producerFromQueue(subscriber));
//                    }
//                })
//
//                //.onBackpressureBuffer(10)
//                // NEED TO MAKE SURE PROCESSING OF THE QUEUE IS SINGLE THREADED.  WOULD THIS DO THE JOB?
//                .subscribeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("SubscribedOnPool"))))
//                .observeOn(from(Executors.newFixedThreadPool(1, new NamedThreadFactory("ObservedOnPool"))))
//                .window(1, TimeUnit.SECONDS)
//
//                ;
//
//    }
//
//    int data  = 0;
//
//    private Producer producerFromQueue(final Subscriber<? super Integer> subscriber) {
//        return

//    }
//}
//
//class ETLTransformer<Integer> {
//
//
//    public static  class Builder<Integer> {
//        public  Observable.Transformer<Observable<Integer>, Observable<TryObs.Status>> build() {
//            SEE dowrk5
//        }
//    }
//}
//
