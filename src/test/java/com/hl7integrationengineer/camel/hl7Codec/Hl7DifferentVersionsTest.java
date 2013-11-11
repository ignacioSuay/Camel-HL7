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

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: suay
 * Date: 7/23/13
 * Time: 5:27 PM
 */
public class Hl7DifferentVersionsTest extends CamelTestSupport {

    static final String endpoint_url = "mina:tcp://localhost:8877?sync=true&codec=#hl7codec";
    static final String endpoint_url2 = "mina:tcp://localhost:9988?sync=true&codec=#hl7codec";

    static final String message_v23 = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r" +
            "EVN|A01|20130617154644\r" +
            "PID|1|465 306 5961||407623|Wood^Patrick^^^MR||19700101|1|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||";

    static final String message_v25 = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.5|\r" +
            "EVN|A01|20130617154644\r" +
            "PID|1|465 306 5961||407623|Wood^Patrick^^^MR||19700101|1|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||";

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

                from(endpoint_url)

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
                        }).end();


                from(endpoint_url2)
                        .unmarshal()
                        .hl7()
                        .choice()
                        .when(header("CamelHL7VersionId").isEqualTo("2.3"))
                        .to("direct:version_2_3")

                        .when(header("CamelHL7VersionId").isEqualTo("2.5"))
                        .to("direct:version_2_5")
                        .end();

                from("direct:version_2_3")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {

                                String res = "This message is version 2.3";
                                exchange.getOut().setBody(res);
                            }
                        }).end();

                from("direct:version_2_5")
                        .process(new Processor() {
                            public void process(Exchange exchange) throws Exception {

                                String res = "This message is version 2.5";
                                exchange.getOut().setBody(res);
                            }
                        }).end();
                ;}};
    }

    /**
     * Send two messages with different version v2.3 and v2.5) to the same route
     * @throws Exception
     */
    @Test
    public void testDifferentVersions() throws Exception {

        Object outMessage =  template.requestBody(endpoint_url, message_v23);

        System.out.println(outMessage.toString());

        outMessage =  template.requestBody(endpoint_url, message_v25);

        System.out.println(outMessage.toString());
    }

    /**
     * Send two messages with different version v2.3 and v2.5) to the same route
     * @throws Exception
     */
    @Test
    public void testRouteByVersion() throws Exception {

        Object outMessage =  template.requestBody(endpoint_url2, message_v23);

        System.out.println(outMessage.toString());

        outMessage =  template.requestBody(endpoint_url2, message_v25);

        System.out.println(outMessage.toString());
    }


}
