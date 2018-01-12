package com.mehrsoft.service.original;

/**
 * Created by daryoush_maxsam1 on 7/31/15.
 * see http://docs.oracle.com/cd/E13222_01/wls/docs103/webserv_adv/handlers.html
 */


import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Collections;
import java.util.Set;


public class HBSISoapHandler implements SOAPHandler<SOAPMessageContext> {

    // save SOAPENV-Body attributes in the request to add them to response message
    public static final ThreadLocal reqIdThreadLocal = new ThreadLocal();
    public static final ThreadLocal transactionThreadLocal = new ThreadLocal();

    public Set<QName> getHeaders()
    {
        return Collections.emptySet();
    }

    public boolean handleMessage(SOAPMessageContext messageContext)
    {
        Boolean outboundProperty = (Boolean)
                messageContext.get (MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (outboundProperty.booleanValue()) {
            // Outbound message add the reqId and Transaction
            try {
                messageContext.getMessage().getSOAPBody().setAttribute("RequestId", (String) reqIdThreadLocal.get());
                String reqTransactionId = (String) transactionThreadLocal.get();

                messageContext.getMessage().getSOAPBody().setAttribute("Transaction", convToRespTransId(reqTransactionId));
                reqIdThreadLocal.remove();
                transactionThreadLocal.remove();
            } catch (SOAPException e) {
                e.printStackTrace();
            }


        } else {
            //Inbound message save ReqId and Transaction
            try {
                String reqId = messageContext.getMessage().getSOAPBody().getAttributeValue(new QName("RequestId"));
                String transaction = messageContext.getMessage().getSOAPBody().getAttributeValue(new QName("Transaction"));

                reqIdThreadLocal.set(reqId);
                transactionThreadLocal.set(transaction);


            } catch (SOAPException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String convToRespTransId(String reqTransactionId) {
        // convert the last character from Q to S  so HotelAvailNotifRQ would be HotelAvailNotifRS

        return reqTransactionId.substring(0, reqTransactionId.length() -1) + "S";
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }



    public void close(MessageContext messageContext)
    {
    }
}

