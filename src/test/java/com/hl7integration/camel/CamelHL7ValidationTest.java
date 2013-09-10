package com.hl7integration.camel;


import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class CamelHL7ValidationTest extends CamelTestSupport{

    
    /**
     * Route Builder Definition
     */
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception{
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:end").end();

            }
        };
    }
    
    /**
     * Test different type of Expected definitions
     * @throws Exception
     */
    @Test
    public void testExpressions() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:end");

        String m = "MSH|^~\\&|hl7Integration|hl7Integration|||||ADT^A01|||2.3|\r" +
                "EVN|A01|20130617154644\r" +
                "PID|1|465 306 5961||407623|Wood^Patrick^^^MR||19700101|1|||High Street^^Oxford^^Ox1 4DP~George St^^Oxford^^Ox1 5AP|||||||\r" +
                "NK1|1|\r"+
                "NK1|2|\r"+
                "PV1|1||Location||||||||||||||||261938_6_201306171546|||||||||||||||||||||||||20130617134644|||||||||";
        
        //Define the Expected message conditions
        mock.expectedMessageCount(2);
        
        //Expected for message 0 "my first message"
        mock.message(0).body().contains("first");
        mock.message(0).body().isInstanceOf(String.class);
        mock.message(0).body().startsWith("my");
        mock.message(0).body().endsWith("message");
        mock.message(0).body().isEqualTo("my first message");
        mock.message(0).body().isNotNull();
        
        //Expected for message 1 "I am cool!"
        mock.message(1).body().contains("is");
        mock.message(1).body().isInstanceOf(String.class);
        mock.message(1).body().startsWith("my");
        mock.message(1).body().endsWith("!");
        mock.message(1).body().isEqualTo("my message is cool!");
        mock.message(1).body().isNotNull();
        
        //Expected for all messages
        mock.allMessages().body().regex("^.*message.*"); //This is equal to contains("message")
        mock.allMessages().body().regex("^my.*"); //This is equal to startWith("my")

        template.sendBody("direct:start", "my first message");
        template.sendBody("direct:start", "my message is cool!");
        
        assertMockEndpointsSatisfied();
 
    }

}
 