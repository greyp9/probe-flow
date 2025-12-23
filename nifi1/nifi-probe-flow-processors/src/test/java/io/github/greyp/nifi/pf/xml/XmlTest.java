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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class XmlTest {

    @Test
    void testSimpleDocument() throws IOException {
        final Document document = XmlUtils.create("foo");
        final byte[] bytes = XmlUtils.toXml(document);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<foo/>\n",
                ProbeUtils.fromBytesUTF8(bytes));
        final XPather xpather = new XPather(document.getDocumentElement(), ProbeXml.getContext());
        final Element element = xpather.getElement("/foo");
        assertNotNull(element);
        assertEquals("foo", element.getTagName());
    }

    @Test
    void testSimpleDocumentNs() throws IOException {
        final Document document = XmlUtils.create("foo", "urn:probe:state");
        final byte[] bytes = XmlUtils.toXml(document);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n<foo xmlns=\"urn:probe:state\"/>\n",
                ProbeUtils.fromBytesUTF8(bytes));
        final XPather xpather = new XPather(document.getDocumentElement(), ProbeXml.getContext());
        final Element element = xpather.getElement("/st:foo");
        assertNotNull(element);
        assertEquals("foo", element.getTagName());
        assertEquals(Probe.Xml.URI_STATE, element.getNamespaceURI());
    }
}
