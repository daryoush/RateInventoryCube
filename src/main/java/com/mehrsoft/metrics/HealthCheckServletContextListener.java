package com.mehrsoft.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;

import javax.inject.Inject;


public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    @Inject
    MetricsService metricsService = new MetricsService();

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return metricsService.healthCheckRegistry;
    }

}