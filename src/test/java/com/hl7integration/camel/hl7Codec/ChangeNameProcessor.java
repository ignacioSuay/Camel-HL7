package com.hl7integrationengineer.camel.hl7Codec;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Created with IntelliJ IDEA.
 * User: suay
 * Date: 7/24/13
 * Time: 11:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeNameProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        Message message = exchange.getIn().getBody(Message.class);
        Terser terser = new Terser(message);
        terser.set("/PID-5-1", "INTEGRATION_ENGNEER");

        exchange.getOut().setBody("capullo");
    }
}
