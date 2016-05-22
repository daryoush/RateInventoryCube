package com.mehrsoft.facade.soap;

/**
 * Created by ijet on 5/8/16.
 */
import com.google.inject.Singleton;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;

import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.xml.ws.Endpoint;

/**
 * <p>
 *    Application Specific Custom Implementation for the {@link org.apache.cxf.transport.servlet.CXFNonSpringServlet}
 * </p>
 * @author Chathuranga Tennakoon / www.chathurangaonline.com
 */
@Singleton
public class AppCXFNonSpringServletImpl extends CXFNonSpringServlet{


    @Inject
    TestSoapFacade testSoapFacade;

    @Override
    public void loadBus(ServletConfig servletConfig){
        // See http://stackoverflow.com/questions/15901617/publishing-jax-ws-webservice-with-guice-in-a-servlet-application
        // this will get the endpoint injected with all its dependecy
        // note that the MyJerseyGuiceServletContextListner is also
        // configred to serve this servlet, this way its injection
        // happ.
        //
        super.loadBus(servletConfig);
        ServerFactoryBean factory = new ServerFactoryBean();
        factory.setBus(bus);


        Endpoint.publish("/testService", testSoapFacade);

    }
}
