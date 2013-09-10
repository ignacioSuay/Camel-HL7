package com.hl7integration.camel;


import org.junit.Test;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;

public class CamelSimpleTests extends CamelTestSupport{

    
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
     * Test that the number of messages received is correct
     * @throws Exception
     */
    @Test
    public void testNumberOfMessagesReceived() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:end");
        
        mock.expectedMessageCount(2);
        
        template.sendBody("direct:start", "my first message");
        template.sendBody("direct:start", "my second message");
        
        mock.assertIsSatisfied();
        

    }
    
    /**
     * Test that the messages arrive in the correct Order
     * @throws Exception
     */
    @Test
    public void testCorrectBodyInOrder() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:end");
        
        mock.expectedBodiesReceived("my first message","my second message");
        
        //Correct Order
        template.sendBody("direct:start", "my first message");
        template.sendBody("direct:start", "my second message");
        mock.assertIsSatisfied();

        mock.reset();

        mock.expectedBodiesReceived("my second message","my first message");

        //Wrong Order
        template.sendBody("direct:start", "my second message");
        template.sendBody("direct:start", "my first message");

        mock.assertIsSatisfied();

    }
    
    /**
     * Test that the messages arrive in Any order
     * @throws Exception
     */
    @Test
    public void testCorrectBodyInAnyOrder() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:end");

        //Wrong Order but is correct because we are using expectedBodiesReceivedInAnyOrder
        mock.expectedBodiesReceivedInAnyOrder("my first message", "my second message");

        template.sendBodyAndHeader("direct:start", "my second message", "Order", 2);
        template.sendBodyAndHeader("direct:start", "my first message", "Order", 1);

        mock.assertIsSatisfied();

    }

}
 