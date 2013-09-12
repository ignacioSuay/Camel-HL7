package com.hl7integrationengineer.camel.hl7Codec;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.hl7.HL7MLLPCodec;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: suay
 * Date: 7/23/13
 * Time: 5:27 PM
 */
public class HL7CamelCodecTest extends CamelTestSupport {

    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry jndi = super.createRegistry();
        jndi.bind("hl7codec", new HL7MLLPCodec());
        return jndi;
    }

@Override
protected RouteBuilder createRouteBuilder() throws Exception {
    return new RouteBuilder() {

        @Override
        public void configure() throws Exception {

    from("mina:tcp://localhost:8877?sync=true&codec=#hl7codec")

      .process(new Processor() {
        public void process(Exchange exchange) throws Exception {

        //The codec has converted the message from string to Hapi Message
        Message message = exchange.getIn().getBody(Message.class);

        //Now, we can use the terser to make some changes
        Terser terser = new Terser(message);

        //We will change the Patient Surname from Wood to INTEGRATION_ENGINEER
        terser.set("/PID-5-1", "INTEGRATION_ENGINEER");

        exchange.getOut().setBody(message);
       }
     });
    }
   };
}

    @Test
    public void testHl7Codec() throws Exception {

        String inMessage = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r" +
                "EVN|A01|20130617154644\r" +
                "PID|1|465 306 5961||407623|Wood^Patrick^^^MR||19700101|1|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||";

        Object outMessage =  template.requestBody("mina:tcp://localhost:8877?sync=true&codec=#hl7codec", inMessage);

        System.out.println(outMessage.toString());

    }
}
