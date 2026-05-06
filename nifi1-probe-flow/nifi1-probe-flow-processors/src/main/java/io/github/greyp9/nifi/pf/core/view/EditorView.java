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
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFileEditor;
import io.github.greyp9.nifi.pf.core.http.HttpResponse;
import io.github.greyp9.nifi.pf.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.core.xhtml.XhtmlUtils;
import io.github.greyp9.nifi.pf.core.xml.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.HttpURLConnection;

public final class EditorView {
    private final ProbeServiceState serviceState;

    public EditorView(final ProbeServiceState serviceState) {
        this.serviceState = serviceState;
    }

    public HttpResponse render(final String processorId, final String requestURI,
                               final boolean textUI, final boolean fileUI) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        return (processorState == null)
                ? ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND)
                : render(processorState, requestURI, textUI, fileUI);
    }

    private HttpResponse render(final ProbeProcessorState processorState, final String requestURI,
                                final boolean textUI, final boolean fileUI) {
        final ProbeFlowFileEditor flowFileEditor = processorState.getFlowFileEditor();
        final Document document = XhtmlUtils.initDocument();
        XhtmlUtils.addHead(document.getDocumentElement(),
                String.format("Editor - %s - NiFi", processorState.getName()));

        final Element body = XmlUtils.addChild(document.getDocumentElement(), Probe.Html.BODY);
        XhtmlUtils.addNavBar(body, Probe.Resource.ROOT);

        final Element divHeader = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.HEADER));
        XmlUtils.addChild(divHeader, Probe.Html.H1,
                String.format("Editor - %s (id=%s) - NiFi", processorState.getName(), processorState.getId()));
        final Element p = XmlUtils.addChild(divHeader, Probe.Html.P, "Create a FlowFile from user inputs.  ");
        XmlUtils.addChild(p, Probe.Html.A, "Text",
                new Attribute(Probe.Html.HREF, String.format("/editor/text/%s", processorState.getId())));
        XmlUtils.addChild(p, Probe.Html.A, "File",
                new Attribute(Probe.Html.HREF, String.format("/editor/file/%s", processorState.getId())));

        XhtmlUtils.addAlerts(body, serviceState.getAlerts());

        final Element divContent = XmlUtils.addChild(body, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.CSS.CONTENT));

        final Element divMetadata = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.App.ID_METADATA));
        XmlUtils.addChild(divMetadata, Probe.Html.H2, "Metadata");
        XmlUtils.addChild(divMetadata, Probe.Html.P,
                "(metadata associated with the FlowFile currently being edited)");
        addTableMetadata(XmlUtils.addChild(divMetadata, Probe.Html.DIV), flowFileEditor);

        final Element divAttributes = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.App.ID_ATTRIBUTES));
        XmlUtils.addChild(divAttributes, Probe.Html.H2, "Attributes");
        XmlUtils.addChild(divAttributes, Probe.Html.P,
                "(attributes associated with the FlowFile currently being edited)");
        XhtmlUtils.addTableAttributes(divAttributes, flowFileEditor.getAttributes());

        final Element divFormAddAttribute = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.App.ID_ATTRIBUTE),
                new Attribute(Probe.Html.CLASS, Probe.Html.FORM));
        XmlUtils.addChild(divFormAddAttribute, Probe.Html.H2, "Attribute");
        XmlUtils.addChild(divFormAddAttribute, Probe.Html.P, "Add / Delete a FlowFile attribute here.");
        addFormAttribute(requestURI, divFormAddAttribute);

        if (fileUI) {
            addFileUI(divContent, requestURI);
        }
        if (textUI) {
            addTextUI(divContent, requestURI, flowFileEditor);
        }
        addFormCreate(divContent, requestURI);

        XhtmlUtils.createFooter(body);
        return ServletUtils.toResponseOk(Probe.Mime.TEXT_HTML_UTF8, XmlUtils.toXhtml(document));
    }

    private void addFormAttribute(final String requestURI, final Element divFormAddAttribute) {
        final Element formAttribute = XmlUtils.addChild(divFormAddAttribute, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST));

        XmlUtils.addChild(formAttribute, Probe.Html.SPAN, Probe.App.COLUMN_NAME);
        XmlUtils.addChild(formAttribute, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.Form.NAME),
                new Attribute(Probe.Html.TYPE, Probe.Form.TEXT),
                new Attribute(Probe.Html.VALUE, ""));

        XmlUtils.addChild(formAttribute, Probe.Html.SPAN, Probe.App.COLUMN_VALUE);
        XmlUtils.addChild(formAttribute, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.Form.VALUE),
                new Attribute(Probe.Html.TYPE, Probe.Form.TEXT),
                new Attribute(Probe.Html.VALUE, ""));

        XmlUtils.addChild(formAttribute, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.App.ADD_ATTRIBUTE),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.VALUE, "Add"));
        XmlUtils.addChild(formAttribute, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.App.DELETE_ATTRIBUTE),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.VALUE, "Delete"));
    }

    private void addFormCreate(final Element divContent, final String requestURI) {
        final Element divSubmit = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.Form.SUBMIT));
        XmlUtils.addChild(divSubmit, Probe.Html.H2, "FlowFile");
        final Element ul = XmlUtils.addChild(divSubmit, Probe.Html.UL);
        XmlUtils.addChild(ul, Probe.Html.LI,
                "To add the currently specified FlowFile, click the [Create FlowFile] button.");
        XmlUtils.addChild(ul, Probe.Html.LI,
                "To clear the currently specified FlowFile, click the [Reset FlowFile] button.");

        final Element divFormCreate = XmlUtils.addChild(divSubmit, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.Html.FORM));
        final Element form = XmlUtils.addChild(divFormCreate, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST));
        XmlUtils.addChild(form, Probe.Html.BUTTON, "Create FlowFile",
                new Attribute(Probe.Html.ACCESS_KEY, "F"),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.NAME, Probe.App.CREATE),
                new Attribute(Probe.Html.VALUE, Probe.App.FLOWFILE));

        XmlUtils.addChild(form, Probe.Html.BUTTON, "Reset FlowFile",
                new Attribute(Probe.Html.ACCESS_KEY, "R"),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.NAME, Probe.App.RESET),
                new Attribute(Probe.Html.VALUE, Probe.App.FLOWFILE));
    }

    private void addFileUI(final Element divContent, final String requestURI) {
        final Element divFile = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.Form.FILE));
        XmlUtils.addChild(divFile, Probe.Html.H2, "Content (Upload File)");
        XmlUtils.addChild(divFile, Probe.Html.P,
                "Upload FlowFile content from your filesystem here.  (Any "
                        + "existing content for this FlowFile will be replaced.)");
        final Element divFormUpload = XmlUtils.addChild(divFile, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.Html.FORM));
        final Element formUpload = XmlUtils.addChild(divFormUpload, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
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

    private void addTextUI(final Element divContent, final String requestURI,
                           final ProbeFlowFileEditor flowFileEditor) {
        final Element divText = XmlUtils.addChild(divContent, Probe.Html.DIV,
                new Attribute(Probe.Html.ID, Probe.Form.TEXT));
        XmlUtils.addChild(divText, Probe.Html.H2, "Content (Edit)");
        XmlUtils.addChild(divText, Probe.Html.P,
                "Enter text to be used as the FlowFile content here.  (Any "
                        + "existing content for this FlowFile will be replaced.)");
        final Element divFormEdit = XmlUtils.addChild(divText, Probe.Html.DIV,
                new Attribute(Probe.Html.CLASS, Probe.Html.FORM));
        final Element formEdit = XmlUtils.addChild(divFormEdit, Probe.Html.FORM,
                new Attribute(Probe.Html.ACTION, requestURI),
                new Attribute(Probe.Html.METHOD, Probe.Html.POST));
        final Element divFormEdit1 = XmlUtils.addChild(formEdit, Probe.Html.DIV);
        final String contentFlowFile = ProbeUtils.fromBytesUTF8(flowFileEditor.getContent().toByteArray());
        final String content = contentFlowFile.isEmpty() ? "\n" : contentFlowFile;
        XmlUtils.addChild(divFormEdit1, Probe.Form.TEXTAREA, content,
                new Attribute(Probe.Form.PLACEHOLDER, "enter text"),
                new Attribute(Probe.Form.ROWS, "12"),
                new Attribute(Probe.Form.COLS, "132"),
                new Attribute(Probe.Html.NAME, Probe.Form.TEXT));
        final Element divFormEdit2 = XmlUtils.addChild(formEdit, Probe.Html.DIV);
        XmlUtils.addChild(divFormEdit2, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.App.UPDATE_TEXT),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.VALUE, "Update Content"));
        XmlUtils.addChild(divFormEdit2, Probe.Html.INPUT,
                new Attribute(Probe.Html.NAME, Probe.App.UPDATE_TEXT_BASE64),
                new Attribute(Probe.Html.TYPE, Probe.Form.SUBMIT),
                new Attribute(Probe.Html.VALUE, "Update Content (From Base64)"));
    }

    private void addTableMetadata(final Element div, final ProbeFlowFileEditor editor) {
        final Element table = XmlUtils.addChild(div, Probe.Html.TABLE,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addTableHead(table, Probe.App.COLUMN_NAME, Probe.App.COLUMN_VALUE);

        final Element tbody = XmlUtils.addChild(table, Probe.Html.TBODY,
                new Attribute(Probe.Html.CLASS, Probe.Html.TABLE));
        XhtmlUtils.addRow(tbody, "Size (Bytes)", editor.getContent().toByteArray().length);
    }
}
