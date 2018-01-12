package com.mehrsoft;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.mehrsoft.queue.ObservableBlockingQueue;
import com.mehrsoft.service.MyService;
import com.mehrsoft.service.MyServiceImpl;
import org.opentravel.ota._2003._05.OTAHotelAvailNotifRQ;
import org.opentravel.ota._2003._05.OTAHotelRateAmountNotifRQ;
import rx.Observable;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by ijet on 6/8/16.
 */
public class RIModule extends AbstractModule {
    ObservableBlockingQueue<OTAHotelAvailNotifRQ> availNotifRQObservableBlockingQueue;
    ObservableBlockingQueue<OTAHotelRateAmountNotifRQ>  rateAmountNotifRQObservableBlockingQueue;

    @Override
    protected void configure() {
        // Bind the interfaces to implementation
        bind(MyService.class).to(MyServiceImpl.class);

        // Configure the queue and observables
        // https://github.com/google/guice/wiki/FrequentlyAskedQuestions (see parameterized types)
        availNotifRQObservableBlockingQueue = new ObservableBlockingQueue<OTAHotelAvailNotifRQ>();
        rateAmountNotifRQObservableBlockingQueue = new ObservableBlockingQueue<OTAHotelRateAmountNotifRQ>();
        bind(new TypeLiteral<Observable<OTAHotelRateAmountNotifRQ>>(){})
                .annotatedWith(Names.named("IncomingRateNotifObservable"))
                .toInstance(rateAmountNotifRQObservableBlockingQueue.getQueueObservable());
        bind(new TypeLiteral<Observable<OTAHotelAvailNotifRQ>>(){})
                .annotatedWith(Names.named("IncomingAvailNotifObservable"))
                .toInstance(availNotifRQObservableBlockingQueue.getQueueObservable());

        bind(new TypeLiteral<BlockingQueue<OTAHotelRateAmountNotifRQ>>(){})
                .annotatedWith(Names.named("IncomingRateQueue"))
                .toInstance(rateAmountNotifRQObservableBlockingQueue.getQueue());
        bind(new TypeLiteral<BlockingQueue<OTAHotelAvailNotifRQ>>(){})
                .annotatedWith(Names.named("IncomingAvailQueue"))
                .toInstance(availNotifRQObservableBlockingQueue.getQueue());
    }

    public void terminate() {
        availNotifRQObservableBlockingQueue.terminate();
        rateAmountNotifRQObservableBlockingQueue.terminate();
    }
}
