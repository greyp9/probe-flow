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
package io.github.greyp9.nifi.pf.core.xml;

import io.github.greyp9.nifi.pf.core.common.Attribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public final class XmlUtils {

    /**
     * <a href="https://checkstyle.sourceforge.io/config_design.html#FinalClass">Constructor</a>
     */
    private XmlUtils() {
    }

    public static Document create(final String name, final Attribute... attributes) {
        return create(name, XMLConstants.NULL_NS_URI, attributes);
    }

    public static Document create(final String name, final String nsURI, final Attribute... attributes) {
        final ProbeDocumentProvider documentProvider = new ProbeDocumentProvider();
        final Document document = documentProvider.newDocument();
        final Element element = document.createElementNS(nsURI, name);
        for (final Attribute attribute : attributes) {
            element.setAttribute(attribute.getName(), attribute.getValue());
        }
        document.appendChild(element);
        return document;
    }

    public static Document toDocument(final byte[] xml) {
        final ProbeDocumentProvider documentProvider = new ProbeDocumentProvider();
        return documentProvider.parse(new ByteArrayInputStream(xml));
    }

    @SuppressWarnings("unused")
    public static byte[] toXhtml11(final Document document) {
        return toXml(document, DOCTYPE_SYSTEM_XHTML11, DOCTYPE_PUBLIC_XHTML11);
    }

    public static byte[] toXhtml(final Document document) {
        return toXml(document, DOCTYPE_SYSTEM_COMPAT, null);
    }

    public static byte[] toXml(final Document document) {
        return toXml(document, null, null);
    }

    private static byte[] toXml(final Document document, final String docTypeSystem, final String docTypePublic) {
        final DOMSource source = new DOMSource(document);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final Result result = new StreamResult(new OutputStreamWriter(bos, StandardCharsets.UTF_8));
        final ProbeTransformProvider transformProvider = new ProbeTransformProvider();
        transformProvider.setOmitXmlDeclaration(false);
        transformProvider.setMethod(METHOD_XML);
        transformProvider.setIndent(true);
        if (docTypeSystem != null) {
            transformProvider.setDocTypeSystem(docTypeSystem);
        }
        if (docTypePublic != null) {
            transformProvider.setDocTypePublic(docTypePublic);
        }
        transformProvider.setEncoding(StandardCharsets.UTF_8.name());
        transformProvider.transform(source, result);
        return bos.toByteArray();
    }

    public static Element addChild(final Element element, final String name,
                                   final Attribute... attributes) {
        return addChild(element, name, null, attributes);
    }

    public static Element addChild(final Element element, final String name, final String text,
                                   final Attribute... attributes) {
        final Document document = element.getOwnerDocument();
        final Element child = document.createElementNS(element.getNamespaceURI(), name);
        if ((text != null) && (!text.isEmpty())) {
            child.setTextContent(text);
        }
        for (final Attribute attribute : attributes) {
            child.setAttribute(attribute.getName(), attribute.getValue());
        }
        return (Element) element.appendChild(child);
    }

    private static final String DOCTYPE_PUBLIC_XHTML11 = "-//W3C//DTD XHTML 1.1//EN";
    private static final String DOCTYPE_SYSTEM_XHTML11 = "https://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd";
    private static final String DOCTYPE_SYSTEM_COMPAT = "about:legacy-compat";
    private static final String METHOD_XML = "xml";
}
