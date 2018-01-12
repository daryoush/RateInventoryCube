package com.mehrsoft.metrics;

import com.codahale.metrics.*;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by ijet on 5/10/16.
 */
public class MetricsService {

    static public MetricRegistry metrics = new MetricRegistry();

    static  public HealthCheckRegistry healthCheckRegistry = new HealthCheckRegistry();

    static {

        healthCheckRegistry.register("database", new HealthCheck() {
            @Override protected Result check() throws Exception {
                //TODO add test to the database here
                return Result.healthy();
            }
        });

    }


    public void timeFunction(String name, Function f)   {

    }
    public MetricRegistry getMetrics() {
        return metrics;
    }

    public Timer getTimer(Class clazz, String name)  {
        return metrics.timer(MetricRegistry.name(clazz , name));
    }


    public Histogram getHistogram(Class clazz, String name) {
        return metrics.histogram(MetricRegistry.name(clazz , name));
    }

    public Counter getCounter(Class clazz, String name)  {
        return metrics.counter(MetricRegistry.name(clazz , name));
    }

    public Gauge registerGauge(Class clazz, String name, Gauge g)  {
        return metrics.register(MetricRegistry.name(clazz, name), g);
    }

    //using servlet metric modules you don;t need this anymore.
//    @PostConstruct
//    public void initMetrics() {
//        JmxReporter reporter = JmxReporter.forRegistry(metrics)
//                .convertRatesTo(TimeUnit.MILLISECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .build();
//        reporter.start();
//    }
}
