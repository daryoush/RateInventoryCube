package com.mehrsoft.tools;

import com.google.common.collect.Iterators;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mehrsoft.RIModule;
import com.mehrsoft.service.AvailNotificationPersistenceService;
import com.mehrsoft.service.NotificationService;
import com.mehrsoft.service.PersistenceService;
import com.mehrsoft.service.RateNotificationPersistenceService;
import org.opentravel.ota._2003._05.OTAHotelAvailNotifRQ;
import org.opentravel.ota._2003._05.OTAHotelRateAmountNotifRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by ijet on 6/8/16.
 */
public class main {
    private final static Logger log = LoggerFactory.getLogger(main.class);
    public static void main(String[] args) throws JAXBException, IOException, XMLStreamException, InterruptedException {
        RIModule riModule = new RIModule();
        final Injector injector = Guice.createInjector(riModule);
        injector.getInstance(AvailNotificationPersistenceService.class).init();   // Should happen automtically!!!
        injector.getInstance(RateNotificationPersistenceService.class).init();   // Should happen automtically!!!
        final NotificationService notificationService = injector.getInstance(NotificationService.class);

        log.info("Starting...");

        JAXBContext context;
        Unmarshaller unmarshaller;

        context = JAXBContext.newInstance(OTAHotelAvailNotifRQ.class);
        unmarshaller = context.createUnmarshaller();

        ZipFile zf = new ZipFile("/Users/ijet/Downloads/Archive.zip");
        int msgCtr = 0;

//        for (Enumeration<? extends ZipEntry> e = zf.entries();
//             e.hasMoreElements(); ) {
//            msgCtr++;
//            ZipEntry ze = e.nextElement();
//
//        }
//
//        System.out.println("TOTAL size: " +msgCtr);

        for (Enumeration<? extends ZipEntry> e = zf.entries();
             e.hasMoreElements(); ) {

            ZipEntry ze = e.nextElement();
            if(msgCtr ++ % 500 == 0 ) System.out.println("current count:" + msgCtr);

            InputStream is = zf.getInputStream(ze);

            // The zip file contains SOAP ENVELOPES to parse them use the trick here:
            // http://blog.bdoughan.com/2012/08/handle-middle-of-xml-document-with-jaxb.htmlhttp://blog.bdoughan.com/2012/08/handle-middle-of-xml-document-with-jaxb.html
            XMLInputFactory xif = XMLInputFactory.newFactory();
            StreamSource xml = new StreamSource(is);
            XMLStreamReader xsr = xif.createXMLStreamReader(xml);
            xsr.nextTag();
            while (!"OTA_HotelDescriptiveContentNotifRQ".equals( xsr.getLocalName()) && !xsr.getLocalName().equals("OTA_HotelAvailNotifRQ") && !xsr.getLocalName().equals("OTA_HotelRateAmountNotifRQ") ) {
                xsr.nextTag();
            }

            if (xsr.getLocalName().equals("OTA_HotelAvailNotifRQ")) {
                JAXBElement<OTAHotelAvailNotifRQ> msgInJax = unmarshaller.unmarshal(xsr, OTAHotelAvailNotifRQ.class);
                xsr.close();

                OTAHotelAvailNotifRQ msg = msgInJax.getValue();
                //System.out.print(" (" + availctr++ + " " + msg.getAvailStatusMessages().getHotelCode() + ")");
                notificationService.process(msg);
                is.close();
            } else if (xsr.getLocalName().equals("OTA_HotelRateAmountNotifRQ")) {
                JAXBElement<OTAHotelRateAmountNotifRQ> msgInJax = unmarshaller.unmarshal(xsr, OTAHotelRateAmountNotifRQ.class);
                xsr.close();

                OTAHotelRateAmountNotifRQ msg = msgInJax.getValue();
                //System.out.print(" {{" + ratecgtr++ + " " + msg.getRateAmountMessages().getHotelCode() + "}}");
                notificationService.process(msg);
                is.close();


            } else {
                System.out.println("INVALID LOCAL NAME: " + xsr.getLocalName());
                continue;
            }

        }

        riModule.terminate();
        Thread.sleep(10000);
        System.out.println("Done");
        System.exit(0);



    }
}
