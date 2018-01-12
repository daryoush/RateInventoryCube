package com.mehrsoft.service.original;



import com.google.common.base.Strings;
import com.mehrsoft.model.old.HotelAvailabilityLengthOfStayType;
import com.mehrsoft.model.old.HotelAvailabilityRestrictionStatus;
import com.mehrsoft.model.old.HotelRoomAvailability;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.opentravel.ota._2003._05.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeMap;


public class AvailNotificationRX  extends BaseNotificationRX<OTAHotelAvailNotifRQ> {
    private final static Logger log = LoggerFactory.getLogger(AvailNotificationRX.class);


    public OTAHotelAvailNotifRS process(OTAHotelAvailNotifRQ request) throws Exception {
//        String reqId = HBSISoapHandler.reqIdThreadLocal.get().toString();
//        request.setEchoToken(reqId);
//        request.setCorrelationID(""+ msgCnt++);
//        log.debug("Timestamp at incoming avail: " + request.getTimeStamp() + " Req ID: " + HBSISoapHandler.reqIdThreadLocal.get());
        processMessage(request);
        return ProtocolUtils.buildSuccessDescriptiveResponse(OTAHotelAvailNotifRS.class, request.getVersion(), request.getPrimaryLangID());
    }



    protected Iterable<? extends HotelRoomAvailability> toHRAIterable(OTAHotelAvailNotifRQ msg) {   // TODO Use reactive to derive this
        log.debug("Timestamp after queue: " + msg.getTimeStamp() + " Req ID: " + msg.getEchoToken() + " enqueue cnt " + msg.getCorrelationID());

        TreeMap<HDPI, HotelRoomAvailability> results = new TreeMap<>();
        String hotelCode = getHotelCode(msg);
        log.debug("Got msg for hotel: " + hotelCode);
        try {

            Long hotelid = Long.parseLong(hotelCode);  // just use hotel code as id!

            for (AvailStatusMessageType asm : msg.getAvailStatusMessages().getAvailStatusMessage()) {
                StatusApplicationControlType sac = asm.getStatusApplicationControl();
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
                    updateAvailMsg(hra, asm);
                    log.trace("Update hotel room avail Record:  {}" + hra);
                    startDate = startDate.plusDays(1);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();  // TODO
        }
        return results.values();
    }


    public static final String SET_LIMIT_MESSAGE = "SetLimit";
    protected void updateAvailMsg(HotelRoomAvailability hra, AvailStatusMessageType asm) {
        if (SET_LIMIT_MESSAGE.equals(asm.getBookingLimitMessageType())) {
            hra.setBookingLimit(asm.getBookingLimit().intValue());
        }
        // TODO check for DAY los unit
        if(asm.getLengthsOfStay() != null) {
            for(LengthsOfStayType.LengthOfStay otaLos : asm.getLengthsOfStay().getLengthOfStay()) {
                hra.setLengthOfStay(HotelAvailabilityLengthOfStayType.fromString(otaLos.getMinMaxMessageType()),
                        asm.getLengthsOfStay().getArrivalDateBased().booleanValue(),otaLos.getTime().intValue());
            }
        }
        if(asm.getRestrictionStatus() != null ) {
            String restrictionStatus = asm.getRestrictionStatus().getStatus().value();
            if("open".equals(restrictionStatus.toLowerCase())) restrictionStatus = "O";
            else restrictionStatus = "D";
            hra.setRestriction(HotelAvailabilityLengthOfStayType.HotelAvailabilityRestrictionType.fromString(asm.getRestrictionStatus().getRestriction()),
                    HotelAvailabilityRestrictionStatus.valueOf(restrictionStatus));
        }

        System.out.println(".......AFTER RESTRICTION...." + hra );
    }

    protected String getHotelCode(OTAHotelAvailNotifRQ request) {
        String hotelCode = request.getAvailStatusMessages().getHotelCode();

        // Try chain code if hotel code is not set.
        if(Strings.isNullOrEmpty(hotelCode)) {
            log.debug("Received a hotel availability message with empty, hotel code.  Trying ChainCode instead.");
            hotelCode = request.getAvailStatusMessages().getChainCode();
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
