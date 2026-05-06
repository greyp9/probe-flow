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
package io.github.greyp9.nifi.pf2.core.view;

import io.github.greyp9.nifi.pf2.core.common.Attribute;
import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.http.HttpResponse;
import io.github.greyp9.nifi.pf2.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf2.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf2.core.xhtml.XhtmlUtils;
import io.github.greyp9.nifi.pf2.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ServiceView {
    private final ProbeServiceState serviceState;

    public ServiceView(final ProbeServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public HttpResponse render() {
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(), String.format("%s - NiFi", serviceState.getName()));
        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        final Element divHeader = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        XmlUtils.addChild(divHeader, Probe.Html.H1,
                String.format("%s (id=%s) - NiFi", serviceState.getName(), serviceState.getId()));
        XmlUtils.addChild(divHeader, Probe.Html.P,
                "NiFi was built to automate the flow of data between systems.  Use "
                + "ProbeFlow service to help debug flow mechanics during flow development.");

        XhtmlUtils.addAlerts(body, serviceState.getAlerts());

        final Element divContent = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.CONTENT));
        addDivState(divContent);
        addDivProcessors(divContent, serviceState.getProcessorStates());
        XhtmlUtils.createFooter(body);
        return ServletUtils.toResponseOk(Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
    }

    private void addDivState(final Element div) {
        final Element divState = XmlUtils.addChild(div, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, "state"));
        XmlUtils.addChild(divState, Probe.Html.H2, "Controller Service State");
        XmlUtils.addChild(divState, Probe.Html.P, "(information about the running controller service)");
        final Element table = XhtmlUtils.createTable(divState, new String[] {"Attribute", "Value"});
        final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addRow(tbody, "Controller Service started", serviceState.getStart());
    }

    private void addDivProcessors(final Element div, final Collection<ProbeProcessorState> processorStates) {
        final Element divProcessors = XmlUtils.addChild(div, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, "processors"));
        XmlUtils.addChild(divProcessors, Probe.Html.H2, "Active Processors");
        XmlUtils.addChild(divProcessors, Probe.Html.P, "(information about any running ProbeFlow processors)");
        final Element table = XmlUtils.addChild(divProcessors, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element thead = XmlUtils.addChild(table, Probe.Html.THEAD,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trHead = XmlUtils.addChild(thead, Probe.Html.TR);
        final String[] columns = {"Name", "ProcessorID", "Start Time", "State", "Editor",
                "FlowFiles to Accept", "FlowFiles", "FlowFiles Routed"};
        for (final String column : columns) {
            XmlUtils.addChild(trHead, Probe.Html.TH, column);
        }
        if (!processorStates.isEmpty()) {
            final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                    new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
            final List<ProbeProcessorState> processorsStatesSorted = processorStates.stream()
                    .sorted(Comparator.comparing(ProbeProcessorState::getName)).collect(Collectors.toList());
            for (final ProbeProcessorState processorState : processorsStatesSorted) {
                addRowProcessor(tbody, processorState);
            }
        }
        final Element tfoot = XmlUtils.addChild(table, Probe.Html.TFOOT,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trFoot = XmlUtils.addChild(tfoot, Probe.Html.TR);
        final String footer = String.format("%d active ProbeFlow processor(s)", processorStates.size());
        XmlUtils.addChild(trFoot, Probe.Html.TH, footer,
                new Attribute(Probe.Html.COLSPAN, Integer.toString(columns.length)));
    }

    private void addRowProcessor(final Element table, final ProbeProcessorState processorState) {
        final String id = processorState.getId();
        final Element tr = XmlUtils.addChild(table, Probe.Html.TR);
        XmlUtils.addChild(tr, Probe.Html.TD, processorState.getName());
        final Element tdLinkView = XmlUtils.addChild(tr, Probe.Html.TD);
        XmlUtils.addChild(tdLinkView, Probe.Html.A, id + " " + Probe.Icon.HREF,
                new Attribute(Probe.Html.HREF, String.format("/viewer/%s/flowfiles", id)));
        XmlUtils.addChild(tr, Probe.Html.TD, processorState.getStart());
        final Element tdLinkState = XmlUtils.addChild(tr, Probe.Html.TD);
        XmlUtils.addChild(tdLinkState, Probe.Html.A, Probe.Icon.DOWNLOAD + Probe.Icon.UPLOAD,
                new Attribute(Probe.Html.TITLE, "Download / Upload State"),
                new Attribute(Probe.Html.HREF, String.format("/state/%s", id)));

        final Element tdLinkEditor = XmlUtils.addChild(tr, Probe.Html.TD);
        XmlUtils.addChild(tdLinkEditor, Probe.Html.A, Probe.Icon.EDITOR,
                new Attribute(Probe.Html.TITLE, "FlowFile Editor"),
                new Attribute(Probe.Html.HREF, String.format("/editor/%s", id)));
        XmlUtils.addChild(tr, Probe.Html.TD, Integer.toString(processorState.incrementToConsume(0)),
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
        XmlUtils.addChild(tr, Probe.Html.TD, Integer.toString(processorState.getFlowFiles().size()),
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
        XmlUtils.addChild(tr, Probe.Html.TD, Long.toString(processorState.getFlowFilesRouted()),
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
    }
}
