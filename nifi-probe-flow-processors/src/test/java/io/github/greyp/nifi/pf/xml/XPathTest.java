/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership.  The ASF licenses this
 * file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.github.greyp.nifi.pf.xml;

import io.github.greyp9.nifi.pf.core.ProbeUtils;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.xml.ProbeXml;
import io.github.greyp9.nifi.pf.core.xml.XPather;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class XPathTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    void testSimpleDocumentNs() throws IOException {
        final Document document = XmlUtils.create("state", "urn:probe:state");
        final byte[] bytes = XmlUtils.toXml(document);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n"
                + "<state xmlns=\"urn:probe:state\"/>\n", ProbeUtils.fromBytesUTF8(bytes));
        final XPather xpather = new XPather(document.getDocumentElement(), ProbeXml.getContext());
        final Element element = xpather.getElement("/st:state");
        assertNotNull(element);
        assertEquals("state", element.getTagName());
        assertEquals(Probe.Xml.URI_STATE, element.getNamespaceURI());
    }

    @Test
    void testDocumentWithChild() throws IOException {
        final Document document0 = XmlUtils.create("state", "urn:probe:state");
        XmlUtils.addChild(document0.getDocumentElement(), "flowfile");
        final XPather xpather0 = new XPather(document0, ProbeXml.getContext());

        final Element stateNoNs = xpather0.getElement("/state");
        assertNull(stateNoNs);
        final Element stateNs = xpather0.getElement("/st:state");
        assertNotNull(stateNs);

        final Element flowfileNs = xpather0.getElement("/st:state/st:flowfile");
        assertNotNull(flowfileNs);

        final byte[] bytes = XmlUtils.toXml(document0);
        logger.info(ProbeUtils.fromBytesUTF8(bytes));
        final Document document1 = XmlUtils.toDocument(bytes);

        final XPather xpather1 = new XPather(document1, ProbeXml.getContext());

        final Element state1NoNs = xpather1.getElement("/state");
        assertNull(state1NoNs);
        final Element state1Ns = xpather1.getElement("/st:state");
        assertNotNull(state1Ns);

        final Element flowfile1Ns = xpather1.getElement("/st:state/st:flowfile");
        assertNotNull(flowfile1Ns);
    }
}
