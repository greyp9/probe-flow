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
import org.apache.nifi.flowfile.FlowFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ProcessorView {
    private final ProbeServiceState serviceState;

    public ProcessorView(final ProbeServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public String update(final String processorId,
                         final String location, final Map<String, String[]> parameters) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        return (processorState == null) ? location : update(processorState, location, parameters);
    }

    private String update(final ProbeProcessorState processorState,
                          final String location, final Map<String, String[]> parameters) {
        String locationUpdate = location;
        for (final Map.Entry<String, String[]> entry : parameters.entrySet()) {
            final String key = entry.getKey();
            for (final String value : entry.getValue()) {
                if (Probe.App.ACCEPT.equals(key) && (Probe.App.FLOWFILE.equals(value))) {
                    final String count = ProbeUtils.onNull(ServletUtils.getParameter(Probe.Form.COUNT, parameters), "");
                    processorState.incrementToConsume(ProbeUtils.toInt(count, 1));
                } else if (Probe.App.ROUTE.equals(key)) {
                    processorState.routeFlowFile(value);
                } else if (Probe.Html.ACTION.equals(key)) {
                    locationUpdate = processorState.actionFlowFile(processorState.getId(), value, location);
                }
            }
        }
        return locationUpdate;
    }

    public HttpResponse render(final String processorId, final String requestURI) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        return (processorState == null)
                ? ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND)
                : render(processorState, requestURI);
    }

    private HttpResponse render(final ProbeProcessorState processorState, final String requestURI) {
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(), String.format("%s - NiFi", processorState.getName()));

        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        XhtmlUtils.addNavBar(body, Probe.Resource.ROOT);

        final Element divHeader = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        XmlUtils.addChild(divHeader, Probe.Html.H1,
                String.format("%s (id=%s) - NiFi", processorState.getName(), processorState.getId()));

        final Element divContent = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.CONTENT));
        addDivState(divContent, processorState);
        addDivAccept(divContent, requestURI);
        addDivFlowFiles(divContent, processorState, requestURI);
        XhtmlUtils.createFooter(body);
        return ServletUtils.toResponse(HttpURLConnection.HTTP_OK,
                Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
    }

    private void addDivState(final Element parent, final ProbeProcessorState processorState) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, "state"));
        XmlUtils.addChild(div, Probe.Html.H2, "Processor State");
        XmlUtils.addChild(div, Probe.Html.P, "(information about the running processor)");
        final Element table = XhtmlUtils.createTable(div, new String[] {"Attribute", "Value"});
        final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addRow(tbody, "Processor started", processorState.getStart());
        XhtmlUtils.addRow(tbody, "Last 'onTrigger()'", processorState.getLastOnTrigger());
    }

    private void addDivAccept(final Element parent, final String requestURI) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, "accept"));
        XmlUtils.addChild(div, Probe.Html.H2, "Accept FlowFile");
        XmlUtils.addChild(div, Probe.Html.P, "(increment number of FlowFiles to be read from upstream connections)");

        final Element form = XmlUtils.addChild(div, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST));
        XmlUtils.addChild(form, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.Form.COUNT),
                new Attribute(Probe.Html.TYPE, Probe.Form.TEXT),
                new Attribute(Probe.Html.VALUE, "1"));
        XmlUtils.addChild(form, "button", "Accept Incoming FlowFile", new Attribute("accesskey", "A"),
                new Attribute("type", "submit"), new Attribute("name", "accept"), new Attribute("value", "flowfile"));
    }

    private void addDivFlowFiles(
            final Element parent, final ProbeProcessorState processorState, final String requestURI) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, "flowfiles"));
        XmlUtils.addChild(div, Probe.Html.H2, "FlowFiles");

        XmlUtils.addChild(div, Probe.Html.P,
                "This table lists the FlowFiles currently held by the processor.  Outgoing "
                + "relationships are defined in the processor properties.");

        final Element ulActions = XmlUtils.addChild(div, Probe.Html.UL);
        XmlUtils.addChild(ulActions, Probe.Html.LI, "View the FlowFile metadata "
                + "by clicking the link in the 'Metadata' column for the record.");
        XmlUtils.addChild(ulActions, Probe.Html.LI, "View the FlowFile content "
                + "by clicking the link in the 'Content' column for the record.");
        XmlUtils.addChild(ulActions, Probe.Html.LI, "Copy the FlowFile "
                + "by clicking the 'CLONE' action button for the record.");
        XmlUtils.addChild(ulActions, Probe.Html.LI, "Delete the FlowFile "
                + "by clicking the 'DROP' action button for the record.");
        XmlUtils.addChild(ulActions, Probe.Html.LI, "Update the FlowFile editor with the FlowFile data "
                + "by clicking the 'EDIT' action button for the record.");
        XmlUtils.addChild(ulActions, Probe.Html.LI, "Route the FlowFile to an outgoing relationship "
                + "by clicking the button for the relationship.");

        final Element divTable = XmlUtils.addChild(div, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.Html.TABLE));

        final List<String> actions = Arrays.asList(Probe.Action.CLONE, Probe.Action.DROP, Probe.Action.EDIT);
        final Set<String> relationships = processorState.getRelationships();
        addTable(processorState, divTable, requestURI, actions, relationships);
    }

    private void addTable(final ProbeProcessorState processorState, final Element div, final String requestURI,
                          final List<String> actions, final Set<String> relationships) {
        final Element form = XmlUtils.addChild(div, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST));
        final Element table = XmlUtils.addChild(form, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));

        final Element thead = XmlUtils.addChild(table, Probe.Html.THEAD,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trHead = XmlUtils.addChild(thead, Probe.Html.TR);
        final String[] columns = {"Metadata", "Content", "ID", "Entry Date", "Attributes", "Size", "Action", "Route"};
        for (final String column : columns) {
            XmlUtils.addChild(trHead, Probe.Html.TH, column);
        }
        final int flowFileToAcceptCount = processorState.incrementToConsume(0);
        final int flowFileCount = processorState.flowFileCount();
        final int flowFileRoutedCount = processorState.flowFileRoutedCount();
        if ((flowFileCount + flowFileRoutedCount) > 0) {
            final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                    new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
            for (final ProbeFlowFile flowFile : processorState.getFlowFiles()) {
                addRowFlowFile(processorState, tbody, flowFile, actions, relationships, flowFile.getRelationship());
            }
        }
        final Element tfoot = XmlUtils.addChild(table, Probe.Html.TFOOT,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        final Element trFoot = XmlUtils.addChild(tfoot, Probe.Html.TR);
        final String footer = String.format("%s FlowFile(s) to accept, %d FlowFile(s) held, %d FlowFile(s) routed",
                flowFileToAcceptCount, flowFileCount, flowFileRoutedCount);
        XmlUtils.addChild(trFoot, Probe.Html.TH, footer,
                new Attribute(Probe.Html.COLSPAN, "8"));
    }

    private void addRowFlowFile(final ProbeProcessorState processorState, final Element table, final FlowFile flowFile,
                                final List<String> actions, final Set<String> relationships, final String route) {
        final Element tr = XmlUtils.addChild(table, Probe.Html.TR);

        final Element tdMetadata = XmlUtils.addChild(tr, Probe.Html.TD);
        final String id = processorState.getId();
        XmlUtils.addChild(tdMetadata, Probe.Html.A, Probe.Icon.METADATA,
                new Attribute(Probe.Html.HREF, String.format("/viewer/%s/flowfile/metadata/%d", id, flowFile.getId())));
        final Element tdContent = XmlUtils.addChild(tr, Probe.Html.TD);
        XmlUtils.addChild(tdContent, Probe.Html.A, Probe.Icon.CONTENT,
                new Attribute(Probe.Html.HREF, String.format("/viewer/%s/flowfile/content/%d", id, flowFile.getId())));

        XmlUtils.addChild(tr, Probe.Html.TD, Long.toString(flowFile.getId()),
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
        XmlUtils.addChild(tr, Probe.Html.TD, new Date(flowFile.getEntryDate()).toInstant().toString());
        XmlUtils.addChild(tr, Probe.Html.TD, Integer.toString(flowFile.getAttributes().size()),
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));
        final ProbeFlowFile probeFlowFile = ProbeUtils.as(flowFile, ProbeFlowFile.class);
        final String size = (probeFlowFile == null) ? Long.toString(flowFile.getSize())
                : (probeFlowFile.isNull() ? "-" :  Long.toString(flowFile.getSize()));
        XmlUtils.addChild(tr, Probe.Html.TD, size,
                new Attribute(Probe.Html.CLASS, Probe.CSS.RIGHT));

        final Element tdAction = XmlUtils.addChild(tr, Probe.Html.TD);
        if (actions == null) {
            tdAction.setTextContent("-");
        } else {
            for (final String action : actions) {
                final String accesskey = action.substring(0, 1);
                final String value = String.format("[%d][%s]", flowFile.getId(), action);
                XmlUtils.addChild(tdAction, Probe.Html.BUTTON, action,
                        new Attribute(Probe.Html.ACCESS_KEY, accesskey),
                        new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                        new Attribute(Probe.Html.NAME, Probe.Html.ACTION),
                        new Attribute(Probe.Html.VALUE, value));
            }
        }
        final Element tdRoute = XmlUtils.addChild(tr, Probe.Html.TD);
        if (route == null) {
            for (final String relationship : relationships) {
                final String accesskey = relationship.substring(0, 1);
                final String value = String.format("[%d][%s]", flowFile.getId(), relationship);
                XmlUtils.addChild(tdRoute, Probe.Html.BUTTON, relationship,
                        new Attribute(Probe.Html.ACCESS_KEY, accesskey),
                        new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                        new Attribute(Probe.Html.NAME, Probe.App.ROUTE),
                        new Attribute(Probe.Html.VALUE, value));
            }
        } else {
            tdRoute.setTextContent(route);
        }
    }
}
