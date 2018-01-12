package com.mehrsoft.facade.soap;

import com.google.inject.Singleton;
import com.mehrsoft.service.MyService;
import com.mehrsoft.model.MyTestData;
import com.mehrsoft.service.NotificationService;
import com.mehrsoft.service.PersistenceService;
import com.mehrsoft.service.original.AvailNotificationRX;
import com.mehrsoft.service.original.RateNotificationRX;
import org.opentravel.ota._2003._05.*;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by ijet on 5/10/16.
 */
@WebService(name = "TestServicePort", serviceName = "TestService", targetNamespace = "http://www.opentravel.org/OTA/2003/05")
@SOAPBinding(style=SOAPBinding.Style.DOCUMENT,
        use=SOAPBinding.Use.LITERAL,
        parameterStyle= SOAPBinding.ParameterStyle.BARE)
//http://docs.oracle.com/cd/E13222_01/wls/docs103/webserv_adv/handlers.html
//  remove for now @HandlerChain(file="/WEB-INF/SOAPHandlerChain.xml")
@Singleton
public class TestSoapFacade {

    @Inject
    MyService myService;

    @Inject
    //NotificationService notificationService;
    AvailNotificationRX  availNotificationService;

    @Inject
    RateNotificationRX rateNotificationRX;

    @WebMethod
    public void addNumbers(MyTestData x) {
        myService.hello("from add");
    }

    @WebMethod
    public List<MyTestData> getAllSoap() {
        return myService.getAllTestDataFromService();
    }



    @WebMethod(operationName = "OTA_HotelAvailNotifRQ", action = "http://www.demandmatrix.net/HBSIXML4/GetSoapRequest")
    @WebResult(name = "OTA_HotelAvailNotifRS", targetNamespace = "http://www.opentravel.org/OTA/2003/05")
    public OTAHotelAvailNotifRS OTAHotelAvailNotifRQ(
            OTAHotelAvailNotifRQ request) throws Exception {
        availNotificationService.process(request);
        return  buildSuccessDescriptiveResponse(OTAHotelAvailNotifRS.class, request.getVersion(), request.getPrimaryLangID());

    }


    @WebMethod(operationName = "OTA_HotelRateAmountNotifRQ", action = "http://www.demandmatrix.net/HBSIXML4/GetSoapRequest")
    @WebResult(name = "OTA_HotelRateAmountNotifRS", targetNamespace = "http://www.opentravel.org/OTA/2003/05")
    public OTAHotelRateAmountNotifRS OTA_HotelRateAmountNotifRQ(
            OTAHotelRateAmountNotifRQ request) throws Exception {
        rateNotificationRX.process(request);
        return  buildSuccessDescriptiveResponse(OTAHotelRateAmountNotifRS.class, request.getVersion(), request.getPrimaryLangID());

    }

    @WebMethod(operationName = "OTA_HotelDescriptiveContentNotifRQ",  action = "http://www.demandmatrix.net/HBSIXML4/GetSoapRequest")
    @WebResult(name = "OTA_HotelDescriptiveContentNotifRS", targetNamespace = "http://www.opentravel.org/OTA/2003/05")
    public OTAHotelDescriptiveContentNotifRS OTA_HotelDescriptiveContentNotifRQ(
            OTAHotelDescriptiveContentNotifRQ request) {
      //  notificationService.process(request);
        return  buildSuccessDescriptiveResponse(OTAHotelDescriptiveContentNotifRS.class, request.getVersion(), request.getPrimaryLangID());

    }

    public static <T extends MessageAcknowledgementType> T buildSuccessDescriptiveResponse(Class<T> type, BigDecimal version, String primaryLangID) {
        T result = null;

        try {
            result = type.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            // if this happens it's on the caller :P
            e.printStackTrace();
            return null;
        }

        result.setTarget("Test");
        result.setVersion(version);
        result.setPrimaryLangID(primaryLangID);
        XMLGregorianCalendar cal = null;
        try {
            cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();   // TODO: Just a temp hack
        }
        result.setTimeStamp(cal);
        result.setSuccess(new SuccessType());
        return result;
    }
}

