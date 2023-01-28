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

import io.github.greyp9.nifi.pf.core.common.Attribute;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.http.HttpResponse;
import io.github.greyp9.nifi.pf.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.core.xhtml.XhtmlUtils;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;

public final class StateView {
    private final ProbeServiceState serviceState;

    public StateView(final ProbeServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public HttpResponse render(final String processorId) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        return (processorState == null)
                ? ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND)
                : render(processorState);
    }

    private HttpResponse render(final ProbeProcessorState processorState) {
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(), String.format("%s - NiFi", processorState.getName()));
        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        XhtmlUtils.addNavBar(body, Probe.Resource.ROOT);
        final Element divHeader = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        XmlUtils.addChild(divHeader, Probe.Html.H1, "Processor State");

        final Element divContent = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.CONTENT));
        addDivDownload(divContent, processorState);
        addDivUpload(divContent, processorState);

        XhtmlUtils.createFooter(body);
        return ServletUtils.toResponseOk(Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
    }

    private void addDivDownload(final Element parent, final ProbeProcessorState processorState) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV);
        XmlUtils.addChild(div, Probe.Html.H2, "Download");
        XmlUtils.addChild(div, Probe.Html.P, "(download FlowFiles from processor)");
        XmlUtils.addChild(div, Probe.Html.A, "Download",
                new Attribute(Probe.Html.TITLE, "Download State"),
                new Attribute(Probe.Html.HREF, String.format("/state/xml/%s", processorState.getId())));
    }

    private void addDivUpload(final Element parent, final ProbeProcessorState processorState) {
        final Element div = XmlUtils.addChild(parent, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.Html.FORM));
        XmlUtils.addChild(div, Probe.Html.H2, "Upload");
        XmlUtils.addChild(div, Probe.Html.P, "(upload FlowFiles to processor)");

        final Element formUpload = XmlUtils.addChild(div, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, String.format("/state/%s", processorState.getId())),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST),
                new Attribute(Probe.Http.ENCTYPE, Probe.Http.FORM_MULTIPART));

        XmlUtils.addChild(formUpload, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.App.UPLOAD_FILE),
                new Attribute(Probe.Html.TYPE, Probe.Form.FILE));
        XmlUtils.addChild(formUpload, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.VALUE, "Upload Content"));
    }
}
