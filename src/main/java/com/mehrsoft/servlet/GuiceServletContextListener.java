
package com.mehrsoft.servlet;

import com.codahale.metrics.servlets.AdminServlet;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;
import com.mehrsoft.RIModule;
import com.mehrsoft.metrics.MetricsService;
import com.palominolabs.metrics.guice.MetricsInstrumentationModule;
import com.squarespace.jersey2.guice.JerseyGuiceServletContextListener;
import com.mehrsoft.facade.soap.AppCXFNonSpringServletImpl;
import com.mehrsoft.facade.servlet.MyServlet;
import ru.vyarus.guice.ext.ExtAnnotationsModule;

import java.util.Arrays;
import java.util.List;


//  See https://github.com/google/guice/wiki/ServletModule
//Think of the ServletModule as an in-code replacement for the web.xml deployment descriptor. Filters and servlets are configured here using normal Java method calls.

public class GuiceServletContextListener extends JerseyGuiceServletContextListener {
    @Override
    protected List<? extends Module> modules() {
        return Arrays.asList(new RIModule(), new ServletModule() {
            @Override
            protected void configureServlets() {

                // note Myresouce has path, my servlet has following
                serve("/myservlet").with(MyServlet.class);
                serve("/soap/*").with(AppCXFNonSpringServletImpl.class);

                serve("/metrics/*").with(GuiceAdminServlet.class);
                install(new MetricsInstrumentationModule(MetricsService.metrics));
                install(new ExtAnnotationsModule());


            }
        });
    }
}

@Singleton
class GuiceAdminServlet extends AdminServlet {

}