/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file;

import java.io.File;
import java.util.Date;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;

/**
 * @version 
 */
public class FileKeepLastModifiedTest extends ContextTestSupport {

    @Override
    protected void setUp() throws Exception {
        deleteDirectory("target/keep");
        super.setUp();
        template.sendBodyAndHeader("file://target/keep", "Hello World", "CamelFileName", "hello.txt");
    }

    public void testKeepLastModified() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/keep?noop=true")
                    .delay(3000).to("file://target/keep/out?keepLastModified=true", "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists("./target/keep/out/hello.txt");
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = new File("./target/keep/out/hello.txt").lastModified();

        assertEquals("Timestamp should have been kept", t1, t2);
    }

    public void testDoNotKeepLastModified() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/keep?noop=true")
                    .delay(3000).to("file://target/keep/out?keepLastModified=false", "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists("./target/keep/out/hello.txt");
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = new File("./target/keep/out/hello.txt").lastModified();

        assertNotSame("Timestamp should NOT have been kept", t1, t2);
    }

    public void testDoNotKeepLastModifiedIsDefault() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file://target/keep?noop=true")
                    .delay(3000).to("file://target/keep/out", "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists("./target/keep/out/hello.txt");
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = new File("./target/keep/out/hello.txt").lastModified();

        assertNotSame("Timestamp should NOT have been kept", t1, t2);
    }

    @Override
    public boolean isUseRouteBuilder() {
        return false;
    }

}
