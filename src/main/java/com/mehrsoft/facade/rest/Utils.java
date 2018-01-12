package com.mehrsoft.facade.rest;

import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Created by daryoush on 1/15/17.
 */
public class Utils {


    static public <A> Collection<Collection<A>> chooseK(Collection<A> src, int k) {
        return chooseK(src, k, Arrays.asList(new Predicate<A>() {
            @Override
            public boolean test(A a) {
                return true;
            }
        }));
    }

    static public <A> Collection<Collection<A>> chooseK(Collection<A> src, int k, Collection<Predicate<A>> filters) {
//        src.stream()
//                .filter(new Predicate<A>() {
//                    @Override
//                    public boolean test(A a) {
//                        return filters.stream().map(f -> f.test(a)).reduce(Boolean.TRUE, (x, y) -> x && y);
//                    }
//                })
//        .map(new Consumer<A>() {
//            @Override
//            public void accept(A a) {
//                Collection<Collection<A>> k_1Res =
//            }
//        });

        return null;
    }

    public static void main(String[] args) {
        Arrays
                .asList(1, 2, 3, 4)
                .stream()
                .collect(new Collector<Integer, Object, Object>() {
                    @Override
                    public Supplier<Object> supplier() {
                        return new Supplier<Object>() {
                            @Override
                            public Object get() {
                                System.out.println("Utils.get");
                                return null;
                            }
                        };
                    }

                    @Override
                    public BiConsumer<Object, Integer> accumulator() {
                        return new BiConsumer<Object, Integer>() {
                            @Override
                            public void accept(Object o, Integer integer) {
                                System.out.println("Utils.accept");
                                System.out.println("o = [" + o + "], integer = [" + integer + "]");

                            }
                        };

                    }

                    @Override
                    public BinaryOperator<Object> combiner() {
                        System.out.println("Utils.combiner");
                        return null;
                    }

                    @Override
                    public Function<Object, Object> finisher() {
                        return new Function<Object, Object>() {
                            @Override
                            public Object apply(Object o) {
                                System.out.println("Utils.apply");
                                System.out.println("o = [" + o + "]");
                                return null;
                            }

                            @Override
                            public <V> Function<V, Object> compose(Function<? super V, ?> before) {
                                System.out.println("Utils.compose");
                                System.out.println("before = [" + before + "]");
                                return null;
                            }

                            @Override
                            public <V> Function<Object, V> andThen(Function<? super Object, ? extends V> after) {
                                System.out.println("Utils.andThen");
                                System.out.println("after = [" + after + "]");
                                return null;
                            }
                        };
                    }

                    @Override
                    public Set<Characteristics> characteristics() {
                        System.out.println("Utils.characteristics");
                        return Sets.newHashSet();
                    }
                });
    }
}
