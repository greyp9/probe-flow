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
package io.github.greyp9.nifi.pf.core.state;

import io.github.greyp9.nifi.pf.core.common.Attribute;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf.core.xml.ProbeXml;
import io.github.greyp9.nifi.pf.core.xml.XPather;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbeSerializer {

    public final byte[] serialize(final Collection<ProbeFlowFile> flowFiles) {
        final Document document = XmlUtils.create(Probe.State.STATE, Probe.Xml.URI_STATE);
        for (final ProbeFlowFile flowFile : flowFiles) {
            final Element elementFlowFile = XmlUtils.addChild(document.getDocumentElement(), Probe.State.FLOWFILE);
            final Map<String, String> attributes = flowFile.getAttributes();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                XmlUtils.addChild(elementFlowFile, Probe.State.ATTRIBUTE, entry.getValue(),
                        new Attribute(Probe.State.NAME, entry.getKey()));
            }
            XmlUtils.addChild(elementFlowFile, Probe.State.CONTENT,
                    Base64.getEncoder().encodeToString(flowFile.getData()));
        }
        return XmlUtils.toXml(document);
    }


    public final Collection<ProbeFlowFile> deserialize(final byte[] bytes) throws IOException {
        final Collection<ProbeFlowFile> flowFilesDeserialized = new ArrayList<>();
        final Document document = XmlUtils.toDocument(bytes);
        final NamespaceContext context = ProbeXml.getContext();
        final XPather xpatherState = new XPather(document, context);
        final List<Element> flowfiles = xpatherState.getElements(XPATH_FLOWFILE);
        for (final Element flowfile : flowfiles) {
            final Map<String, String> attributes = new HashMap<>();
            final XPather xpatherFlowFile = new XPather(flowfile, context);
            final List<Element> elementsAttr = xpatherFlowFile.getElements(XPATH_ATTRIBUTE);
            for (Element elementAttr : elementsAttr) {
                attributes.put(elementAttr.getAttribute(Probe.State.NAME), elementAttr.getTextContent());
            }
            final byte[] content = Base64.getDecoder().decode(xpatherFlowFile.getText(XPATH_CONTENT));
            flowFilesDeserialized.add(new ProbeFlowFile(0L, System.currentTimeMillis(), attributes, content));
        }
        return flowFilesDeserialized;
    }

    public static final String XPATH_FLOWFILE = "/st:state/st:flowfile";
    public static final String XPATH_ATTRIBUTE = "st:attribute";
    public static final String XPATH_CONTENT = "st:content/text()";
}
