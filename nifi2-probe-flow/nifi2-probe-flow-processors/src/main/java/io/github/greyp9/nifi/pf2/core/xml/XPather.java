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
package io.github.greyp9.nifi.pf2.core.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class XPather {
    private final Element element;
    private final NamespaceContext context;

    public XPather(final Document document) {
        this(document.getDocumentElement(), null);
    }

    public XPather(final Document document, final NamespaceContext context) {
        this(document.getDocumentElement(), context);
    }

    public XPather(final Element element) {
        this(element, null);
    }

    public XPather(final Element element, final NamespaceContext context) {
        this.element = element;
        this.context = context;
    }

    public final String getText(final String xpath) throws IOException {
        try {
            final XPathExpression expression = getExpression(xpath, context);
            final Object o = (element == null) ? null : expression.evaluate(element, XPathConstants.STRING);
            return ((o instanceof String) ? (String) o : null);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    public final Element getElement(final String xpath) throws IOException {
        try {
            final XPathExpression expression = getExpression(xpath, context);
            final Object o = (element == null) ? null : expression.evaluate(element, XPathConstants.NODE);
            return ((o instanceof Element) ? (Element) o : null);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    public final List<Element> getElements(final String xpath) throws IOException {
        try {
            final XPathExpression expression = getExpression(xpath, context);
            final List<Element> elements = new ArrayList<Element>();
            final Object result = (element == null) ? null : expression.evaluate(element, XPathConstants.NODESET);
            if (result instanceof NodeList) {
                final NodeList nodeList = (NodeList) result;
                final int length = nodeList.getLength();
                for (int i = 0; (i < length); ++i) {
                    elements.add((Element) nodeList.item(i));
                }
            }
            return elements;
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    private static XPathExpression getExpression(
            final String expression, final NamespaceContext context) throws IOException {
        try {
            final XPathFactory factory = XPathFactory.newInstance();
            final XPath xpath = factory.newXPath();
            if (context != null) {
                xpath.setNamespaceContext(context);
            }
            return xpath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }
}
