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
package io.github.greyp9.nifi.pf.core.servlet;

import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.http.HttpResponse;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.core.view.EditorUpdate;
import io.github.greyp9.nifi.pf.core.view.EditorView;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EditorServlet extends HttpServlet {
    private static final long serialVersionUID = -7830232987797448503L;

    private transient ProbeServiceState serviceState;

    @Override
    public void init() throws ServletException {
        super.init();
        serviceState = (ProbeServiceState) getServletContext().getAttribute(ProbeServiceState.class.getName());
    }

    private static final Pattern PATTERN_EDITOR = Pattern.compile("/editor/(.+?)");
    private static final Pattern PATTERN_EDITOR_FILE = Pattern.compile("/editor/file/(.+?)");
    private static final Pattern PATTERN_EDITOR_TEXT = Pattern.compile("/editor/text/(.+?)");

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        final HttpResponse httpResponse;
        final Matcher matcherEditor = PATTERN_EDITOR.matcher(requestURI);
        final Matcher matcherEditorFile = PATTERN_EDITOR_FILE.matcher(requestURI);
        final Matcher matcherEditorText = PATTERN_EDITOR_TEXT.matcher(requestURI);
        if (matcherEditorFile.matches()) {
            final EditorView view = new EditorView(serviceState);
            httpResponse = view.render(matcherEditorFile.group(1), requestURI, false, true);
        } else if (matcherEditorText.matches()) {
            final EditorView view = new EditorView(serviceState);
            httpResponse = view.render(matcherEditorText.group(1), requestURI, true, false);
        } else if (matcherEditor.matches()) {
            final EditorView view = new EditorView(serviceState);
            httpResponse = view.render(matcherEditor.group(1), requestURI, false, false);
        } else {
            httpResponse = ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND);
        }
        ServletUtils.write(response, httpResponse);
    }

    @Override
    protected void doPost(final HttpServletRequest request,
                          final HttpServletResponse response) throws IOException, ServletException {
        final ProbeProcessorState processorState = serviceState.getProcessorState(ServletUtils.toId(request));
        final String contentType = request.getHeader(Probe.Http.CONTENT_TYPE);
        if ((processorState == null) || (contentType == null)) {
            LoggerFactory.getLogger(getClass()).trace("missing state [{}] [{}]", processorState, contentType);
        } else if ((contentType.equals(Probe.Http.FORM_URL_ENCODED))) {
            final EditorUpdate editorUpdate = new EditorUpdate(processorState);
            editorUpdate.update(request.getParameterMap());
        } else if (contentType.startsWith(Probe.Http.FORM_MULTIPART)) {
            final EditorUpdate editorUpdate = new EditorUpdate(processorState);
            editorUpdate.update(request.getParts());
        }
        ServletUtils.write(response, ServletUtils.toRedirect(request.getRequestURI()));
    }
}
