package com.hl7integration.camel;


import org.apache.camel.Exchange;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.spi.BrowsableEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CamelComplexTests extends CamelTestSupport{

    
    /**
     * Route Builder Definition
     */
    @Override
    protected RouteBuilder createRouteBuilder() throws Exception{
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:start").to("mock:end").end();
                
                from("direct:start2").to("mock:result").end();
            }
        };
    }
    

    /**
     * Test a User defined Expression expected
     * @throws Exception
     */
    @Test
    public void testCustomExpressions() throws Exception {
       final MockEndpoint mock = getMockEndpoint("mock:end");
        
        mock.expects(new Runnable(){
            public void run(){
                for(Exchange exchange : mock.getExchanges()){
                    
                    String versionHead = exchange.getIn().getHeader("version", String.class);
                    float version = Float.parseFloat(versionHead.substring(4));
                    
                    if (version < 2.3) 
                        fail("Version of the messeages is too old");
                    else if (version > 3)
                        fail("Version of the messeages is too new");
                }
            }
        });
        template.sendBodyAndHeader("direct:start", "my first message", "version", "HL7v2.6");
        template.sendBodyAndHeader("direct:start", "my second message", "version", "HL7v2.9");
        
        assertMockEndpointsSatisfied();
        
        template.sendBodyAndHeader("direct:start", "camel rocks", "version", "HL7v1.6");
        template.sendBodyAndHeader("direct:start", "camel is cool!", "version", "HL7v3.9");
        
        mock.assertIsNotSatisfied();
    }
    
    
    /**
     * Test that the message has been processed
     * @throws Exception
     */
    @Test
    public void testCamelProcessIsDone() throws Exception {
       
       NotifyBuilder notify = new NotifyBuilder(context).whenAnyDoneMatches(body().isEqualTo("camel rocks")).create();

        template.sendBodyAndHeader("direct:start", "camel rocks", "version", "HL7v2.6");
        template.sendBodyAndHeader("direct:start", "camel is cool!", "version", "HL7v2.9");
        
        
        boolean matches = notify.matches(5, TimeUnit.SECONDS);
        assertTrue(matches);
        
        
    }
    
    /**
     * Test that a queue has received a message and the body is equal
     * @throws Exception
     */
    @Test
    public void testCamelQueueISProcessed() throws Exception {
       
        template.sendBodyAndHeader("direct:start2", "camel rocks", "version", "HL7v2.6");
        template.sendBodyAndHeader("direct:start", "camel is cool!", "version", "HL7v2.9");
        
        BrowsableEndpoint be = context.getEndpoint("mock:result", BrowsableEndpoint.class);
        List<Exchange> le = be.getExchanges();
        assertEquals(1, le.size());
        String body = le.get(0).getIn().getBody(String.class);
        assertEquals("camel rocks", body);
    }
    
    
}
 