/*
The contents of this file are subject to the Mozilla Public License Version 1.1 
(the "License"); you may not use this file except in compliance with the License. 
You may obtain a copy of the License at http://www.mozilla.org/MPL/ 
Software distributed under the License is distributed on an "AS IS" basis, 
WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for the 
specific language governing rights and limitations under the License. 

The Original Code is "JMSTransport.java".  Description: 
"A TransportLayer that exchanges messages through JMS destinations." 

The Initial Developer of the Original Code is University Health Network. Copyright (C) 
2004.  All Rights Reserved. 

Contributor(s): ______________________________________. 

Alternatively, the contents of this file may be used under the terms of the 
GNU General Public License (the "GPL"), in which case the provisions of the GPL are 
applicable instead of those above.  If you wish to allow use of your version of this 
file only under the terms of the GPL and not to allow others to use your version 
of this file under the MPL, indicate your decision by deleting  the provisions above 
and replace  them with the notice and other provisions required by the GPL License.  
If you do not delete the provisions above, a recipient may use your version of 
this file under either the MPL or the GPL. 

*/
package ca.uhn.hl7v2.protocol.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.protocol.JMSDestination;
import ca.uhn.hl7v2.protocol.TransportException;
import ca.uhn.hl7v2.protocol.TransportLayer;
import ca.uhn.hl7v2.protocol.Transportable;

/**
 * A <code>TransportLayer</code> that exchanges messages through JMS destinations.   
 * 
 * @author <a href="mailto:bryan.tripp@uhn.on.ca">Bryan Tripp</a>
 * @version $Revision: 1.1 $ updated on $Date: 2007-02-19 02:24:26 $ by $Author: jamesagnew $
 */
public class JMSTransport extends AbstractTransport implements TransportLayer {

    private static final Logger log = LoggerFactory.getLogger(URLTransport.class);    

    public static final String INBOUND_DESTINATION_NAME_KEY = "INBOUND_DESTINATION_NAME";
    public static final String INBOUND_CLIENT_ID_KEY = "INBOUND_CLIENT_ID";
    public static final String INBOUND_CONNECTION_METADATA_KEY = "INBOUND_CONNECTION_METADATA";
    public static final String OUTBOUND_DESTINATION_NAME_KEY = "OUTBOUND_DESTINATION_NAME";
    public static final String OUTBOUND_CLIENT_ID_KEY = "OUTBOUND_CLIENT_ID";
    public static final String OUTBOUND_CONNECTION_METADATA_KEY = "OUTBOUND_CONNECTION_METADATA";
     
    private JMSDestination myInbound;
    private JMSDestination myOutbound;
    private Map<String, Object> myMetadata;
    
    /**
     * @param theInboundDestination wrapper around the Queue or Topic to which outgoing messages 
     *      are to be sent
     * @param theOutboundDestination wrapper around the Queue or Topic from which incoming messages
     *      are to be retrieved
     */
    public JMSTransport(JMSDestination theInboundDestination, JMSDestination theOutboundDestination) {
        myInbound = theInboundDestination;
        myOutbound = theOutboundDestination;
    }
    
    /**
     * @param theConnection JMS connection over which messages are exchanged 
     * @param theDestination JMS destination to which messages are produced and 
     *      from which messages are consumed 
     */
    public JMSTransport() {
        myMetadata = makeMetadata();
    }
    
    /** 
     * Sets common metadata on the basis of connection and destination.  
     */ 
    private Map<String, Object> makeMetadata() {
        Map<String, Object> md = new HashMap<String, Object>();
        try {
            md.put(INBOUND_CLIENT_ID_KEY, myInbound.getConnection().getClientID());
            md.put(INBOUND_CONNECTION_METADATA_KEY, myInbound.getConnection().getMetaData());
            md.put(INBOUND_DESTINATION_NAME_KEY, myInbound.getName());
            md.put(OUTBOUND_CLIENT_ID_KEY, myOutbound.getConnection().getClientID());
            md.put(OUTBOUND_CONNECTION_METADATA_KEY, myOutbound.getConnection().getMetaData());
            md.put(OUTBOUND_DESTINATION_NAME_KEY, myOutbound.getName());
        } catch (JMSException e) {
            log.error("Error setting JMSTransport metadata", e);
        }
        return md;
    }
    
//    /**
//     * @param theDestination a Queue or Topic 
//     * @return either getQueueName() or getTopicName() 
//     */
//    private static String getName(Destination theDestination) throws JMSException {
//        String name = null;
//        
//        if (theDestination instanceof Queue) {
//            name = ((Queue) theDestination).getQueueName();
//        } else if (theDestination instanceof Topic) {
//            name = ((Topic) theDestination).getTopicName();
//        } else {
//            throw new IllegalArgumentException("We don't support Destinations of type " 
//                + theDestination.getClass().getName());
//        }
//        return name;
//    }

    /** 
     * @see ca.uhn.hl7v2.protocol.Transport#doSend(ca.uhn.hl7v2.protocol.Transportable)
     */
    public void doSend(Transportable theMessage) throws TransportException {
        try {            
            Message message = toMessage(theMessage);
            myOutbound.send(message);
        } catch (JMSException e) {
            throw new TransportException(e);
        }
    } 
    
    /**
     * Fills a JMS message object with text and metadata from the given 
     * <code>Transportable</code>.  The default implementation obtains a 
     * the Message from getMessage(), and expects this to be a TextMessage.   
     * Override this method if you want to use a different message type.  
     * 
     * @param theSource a Transportable from which to obtain data for filling the 
     *      given Message
     * @return a Message containing data from the given Transportable
     */
    protected Message toMessage(Transportable theSource) throws TransportException {
        Message message;
        try {
            message = myOutbound.createMessage();
         
            if ( !(message instanceof TextMessage)) {
                throw new TransportException("This implementation expects getMessage() to return "
                    + " a TextMessage.  Override this method if another message type is to be used");
            }

            ((TextMessage) message).setText(theSource.getMessage());
        
            Iterator<String> it = theSource.getMetadata().keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                Object val = theSource.getMetadata().get(key);
                message.setObjectProperty(key.toString(), val);
            }
        } catch (JMSException e) {
            throw new TransportException(e);
        }       
        
        return message;
    }
    
    /**
     * Copies data from the given Message into a Transportable.  The default 
     * implementation expects a TextMessage, but this can be overridden.  
     * 
     * @param theMessage a JMS Message from which to obtain data  
     * @return a Transportable containing data from the given Message
     */
    protected Transportable toTransportable(Message theMessage) throws TransportException {
        if ( !(theMessage instanceof TextMessage)) {
            throw new TransportException("This implementation expects getMessage() to return "
                + " a TextMessage.  Override this method if another message type is to be used");
        }
        
        Transportable result = null;
        try {
            String text = ((TextMessage) theMessage).getText();
            result = new TransportableImpl(text);
            result.getMetadata().putAll(getCommonMetadata());
        } catch (JMSException e) {
            throw new TransportException(e);
        }

        return result;
    }
    
    /** 
     * @see ca.uhn.hl7v2.protocol.AbstractTransport#doReceive()
     */
    public Transportable doReceive() throws TransportException {
        Transportable result = null;
        try {
            Message message = myInbound.receive();
            result = toTransportable(message);
        } catch (JMSException e) {
            throw new TransportException(e);            
        }
        return result;
    }

    /** 
     * Returns metadata under the static keys defined by this class.  
     *  
     * @see ca.uhn.hl7v2.protocol.TransportLayer#getCommonMetadata()
     */
    public Map<String, Object> getCommonMetadata() {
        return myMetadata;
    }

    /** 
     * @see ca.uhn.hl7v2.protocol.impl.AbstractTransport#doConnect()
     */
    public void doConnect() throws TransportException {
        try {
            myInbound.connect();
            if (myInbound != myOutbound) {
                myOutbound.connect();
            }
        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }

    /** 
     * @see ca.uhn.hl7v2.protocol.impl.AbstractTransport#doDisconnect()
     */
    public void doDisconnect() throws TransportException {
        try {
            myInbound.disconnect();
            if (myInbound != myOutbound) {
                myOutbound.disconnect();
            }
        } catch (JMSException e) {
            throw new TransportException(e);
        }
    }
    
}
