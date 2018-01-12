package com.mehrsoft.service;

import com.google.inject.name.Named;
import org.opentravel.ota._2003._05.OTAHotelAvailNotifRQ;
import org.opentravel.ota._2003._05.OTAHotelDescriptiveContentNotifRQ;
import org.opentravel.ota._2003._05.OTAHotelRateAmountNotifRQ;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ijet on 6/3/16.
 */
public class NotificationService {

    ArrayList<String >x;

    private final static Logger log = LoggerFactory.getLogger(NotificationService.class);
    @Inject
    @Named("IncomingRateQueue")
    BlockingQueue<OTAHotelRateAmountNotifRQ>  incomingRateAmountQueue;

    @Inject
    @Named("IncomingAvailQueue")
    BlockingQueue<OTAHotelAvailNotifRQ> incomingAvailQueue;

    public void process(OTAHotelRateAmountNotifRQ req) {
        log.info("Processing rate amount notification");
        try {
            incomingRateAmountQueue.put(req);
        } catch (InterruptedException e) {
            e.printStackTrace();            // what is right thing to do here
        }
        return;
    }

    public void process(OTAHotelAvailNotifRQ req) {
        log.info("Processing avail notification");

        try {
            incomingAvailQueue.put(req);
        } catch (InterruptedException e) {
            e.printStackTrace();            // what is right thing to do here
        }
        return;
    }


    public void process(OTAHotelDescriptiveContentNotifRQ request) {
    }
}
