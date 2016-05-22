package com.mehrsoft.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;

import javax.inject.Inject;

public class MetricsServletContextListener extends MetricsServlet.ContextListener {


    @Inject
    MetricsService metricsService = new MetricsService();

    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricsService.getMetrics();
    }

}
