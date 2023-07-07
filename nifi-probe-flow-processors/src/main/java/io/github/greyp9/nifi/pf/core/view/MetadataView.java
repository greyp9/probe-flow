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
package io.github.greyp9.nifi.pf.core.view;

import io.github.greyp9.nifi.pf.core.ProbeUtils;
import io.github.greyp9.nifi.pf.core.common.Attribute;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf.core.http.HttpResponse;
import io.github.greyp9.nifi.pf.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.core.xhtml.XhtmlUtils;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.Optional;

public final class MetadataView {
    private final ProbeServiceState serviceState;

    public MetadataView(final ProbeServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public HttpResponse render(final String processorId, final String flowFileId) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        return (processorState == null)
                ? ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND)
                : render(processorState, flowFileId);
    }

    private HttpResponse render(final ProbeProcessorState processorState, final String flowFileId) {
        final Optional<ProbeFlowFile> flowFile = processorState.getFlowFile(flowFileId);
        return flowFile.map(ff -> render(processorState, ff))
                .orElseGet(() -> ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND));
    }

    private HttpResponse render(final ProbeProcessorState processorState, final ProbeFlowFile flowFile) {
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(), String.format("%s - NiFi", processorState.getName()));
        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        XhtmlUtils.addNavBar(body, Probe.Resource.ROOT);
        final Element divHeader = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        XmlUtils.addChild(divHeader, Probe.Html.H1, "FlowFile");
        XmlUtils.addChild(divHeader, Probe.Html.P, "(non-content details associated with this FlowFile)");

        XhtmlUtils.addAlerts(body, serviceState.getAlerts());

        final Element divContent = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.CONTENT));

        final Element divMetadata = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.App.ID_METADATA));
        XmlUtils.addChild(divMetadata, Probe.Html.H2, "Metadata");
        XmlUtils.addChild(divMetadata, Probe.Html.P, "(metadata associated with the FlowFile)");
        addTableMetadata(divMetadata, flowFile);

        final Element divAttributes = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.App.ID_ATTRIBUTES));
        XmlUtils.addChild(divAttributes, Probe.Html.H2, "FlowFile Attributes");
        XmlUtils.addChild(divAttributes, Probe.Html.P, "(attributes associated with the FlowFile)");
        XhtmlUtils.addTableAttributes(divAttributes, flowFile.getAttributes());

        XhtmlUtils.createFooter(body);
        return ServletUtils.toResponseOk(Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
    }

    private void addTableMetadata(final Element div, final ProbeFlowFile flowFile) {
        final Element table = XmlUtils.addChild(div, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addTableHead(table, Probe.App.COLUMN_NAME, Probe.App.COLUMN_VALUE);
        final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addRow(tbody, "ProbeFlow ID", flowFile.getId());
        XhtmlUtils.addRow(tbody, "Entry Date", ProbeUtils.toStringZ(new Date(flowFile.getEntryDate())));
        XhtmlUtils.addRow(tbody, "Lineage Start Date", ProbeUtils.toStringZ(new Date(flowFile.getEntryDate())));
        XhtmlUtils.addRow(tbody, "Lineage Start Index", flowFile.getLineageStartIndex());
        XhtmlUtils.addRow(tbody, "Last Queue Index", flowFile.getQueueDateIndex());
        XhtmlUtils.addRow(tbody, "Size (Bytes)", flowFile.getSize());
    }
}
