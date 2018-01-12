package com.mehrsoft.service.original;



import com.google.common.base.Strings;
import com.mehrsoft.model.old.HotelRoomAvailability;
import com.mehrsoft.model.old.OTAAgeQualifyingCode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.opentravel.ota._2003._05.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.TreeMap;

/**
 * Created by daryoush_maxsam1 on 12/11/15.
 */
public class RateNotificationRX extends BaseNotificationRX<OTAHotelRateAmountNotifRQ> {

    private final static Logger log = LoggerFactory.getLogger(RateNotificationRX.class);

    public OTAHotelRateAmountNotifRS process(OTAHotelRateAmountNotifRQ request) throws Exception {
        String reqId = "ADD SOAP Handler"; //HBSISoapHandler.reqIdThreadLocal.get().toString();
        request.setEchoToken(reqId);
        request.setCorrelationID(""+ msgCnt++);
       // log.debug("Timestamp at incoming rate: " + request.getTimeStamp() + " Req ID: " + HBSISoapHandler.reqIdThreadLocal.get());
        processMessage(request);
        return ProtocolUtils.buildSuccessDescriptiveResponse(OTAHotelRateAmountNotifRS.class, request.getVersion(), request.getPrimaryLangID());
    }

    protected Iterable<? extends HotelRoomAvailability> toHRAIterable(OTAHotelRateAmountNotifRQ msg) {   // TODO Use reactive to derive this
        log.debug("Timestamp after queue: " + msg.getTimeStamp() + " Req ID: " + msg.getEchoToken() + " enqueue cnt " + msg.getCorrelationID());
        TreeMap<HDPI, HotelRoomAvailability> results = new TreeMap<>();
        String hotelCode = getHotelCode(msg);
        log.debug("Got msg for hotel: " + hotelCode);
        try {
//            hotel = hs.findHotel(hotelCode);
//            if (null == hotel || null == hotel.getId()) {  // TODO this is hotel that not onboarded...
//
//                return results.values();
//            }
           Long hotelid = Long.parseLong(hotelCode);  // just use hotel code as id!
            for (RateAmountMessageType ram : msg.getRateAmountMessages().getRateAmountMessage()) {
                System.out.println("................. next rate amount message");
                StatusApplicationControlType sac = ram.getStatusApplicationControl();
                String start = sac.getStart();
                String end = sac.getEnd();
                assert (!Strings.isNullOrEmpty(start));
                assert (!Strings.isNullOrEmpty(end));

                String inventoryCode = getRoomCode(sac);
                String planCode = getRatePlanCode(sac);

                DateTime startDate = parser.parseDateTime(start);
                DateTime endDate = parser.parseDateTime(end);


                assert (!Strings.isNullOrEmpty(inventoryCode));
                assert (!Strings.isNullOrEmpty(planCode));
                while (DateTimeComparator.getDateOnlyInstance().compare(startDate, endDate) <= 0) {
                    DayPlanInv dpi = DayPlanInv.build(hotelid, startDate, inventoryCode, planCode);
                    HDPI hdpi = new HDPI(hotelid, dpi);
                    HotelRoomAvailability hra = results.get(hdpi);

                    if (null == hra) {
                        hra = new HotelRoomAvailability.Builder().on(startDate.toDate()).rateCategory(planCode).roomCatagory(inventoryCode).build();
                        hra.setHotel_id(hotelid);
                        results.put(hdpi, hra);
                    }
                    updateAvailMsg(hra, ram.getRates());
                    log.debug("Update hotel room avail Record:  {}" + hra);
                    startDate = startDate.plusDays(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();  // TODO
        }
        return results.values();
    }

    protected void updateAvailMsg(HotelRoomAvailability hra, RateAmountMessageType.Rates rates) {
        for( RateAmountMessageType.Rates.Rate r: rates.getRate()) {
            assert(r.getNumberOfUnits().equals(1));
            assert(r.getRateTimeUnit().equals(TimeUnitType.DAY));
            BigInteger decimalPlaces = r.getDecimalPlaces();
            for(RateUploadType.BaseByGuestAmts.BaseByGuestAmt bgm: r.getBaseByGuestAmts().getBaseByGuestAmt()) {
                BigDecimal amountBeforeTax = bgm.getAmountBeforeTax();
                BigDecimal amountAfterTax = bgm.getAmountAfterTax();
                String currency = bgm.getCurrencyCode();
                Integer noOfGuests = bgm.getNumberOfGuests();
                String ageCode = bgm.getAgeQualifyingCode();
                if(decimalPlaces != null) {
                    amountAfterTax = new BigDecimal(amountAfterTax.toBigIntegerExact(), decimalPlaces.intValue());
                    amountBeforeTax = new BigDecimal(amountBeforeTax.toBigIntegerExact(), decimalPlaces.intValue());
                }
                if(Strings.isNullOrEmpty(ageCode)) {  // BasebyGuest ageQualifier or no of guests.
                    hra.setPrice(noOfGuests, currency, amountBeforeTax, amountAfterTax);
                } else {
                    hra.setAgeQualifiedPrice(OTAAgeQualifyingCode.from(ageCode), currency, amountBeforeTax, amountAfterTax );
                }
            }
        }
    }

    protected String getHotelCode(OTAHotelRateAmountNotifRQ request) {
        String hotelCode = request.getRateAmountMessages().getHotelCode();
        // Try chain code if hotel code is not set.
        if(Strings.isNullOrEmpty(hotelCode)) {
            log.trace("Received a hotel availability message with empty, hotel code.  Trying ChainCode instead.");
            hotelCode = request.getRateAmountMessages().getChainCode();
        }
        return hotelCode;
    }
    protected String getRoomCode(StatusApplicationControlType msg) {
        return msg.getInvCode();
    }

    protected String getRatePlanCode(StatusApplicationControlType msg) {
        return msg.getRatePlanCode();
    }

}

