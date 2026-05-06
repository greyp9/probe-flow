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
package io.github.greyp9.nifi.pf2.core.servlet;

import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.http.HttpResponse;
import io.github.greyp9.nifi.pf2.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf2.core.view.ServiceUpdate;
import io.github.greyp9.nifi.pf2.core.view.StateView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StateServlet extends HttpServlet {
    private static final long serialVersionUID = 1090889424107221852L;

    private transient ProbeServiceState serviceState;

    @Override
    public void init() throws ServletException {
        super.init();
        serviceState = (ProbeServiceState) getServletContext().getAttribute(ProbeServiceState.class.getName());
    }

    private static final Pattern PATTERN_STATE = Pattern.compile("/state/(.+?)");
    private static final Pattern PATTERN_STATE_XML = Pattern.compile("/state/xml/(.+?)");

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        final HttpResponse httpResponse;
        final Matcher matcherState = PATTERN_STATE.matcher(requestURI);
        final Matcher matcherStateXml = PATTERN_STATE_XML.matcher(requestURI);
        if (matcherStateXml.matches()) {
            httpResponse = toStateXml(matcherStateXml.group(1));
        } else if (matcherState.matches()) {
            final StateView view = new StateView(serviceState);
            httpResponse = view.render(matcherState.group(1));
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
        } else if (contentType.startsWith(Probe.Http.FORM_MULTIPART)) {
            final ServiceUpdate serviceUpdate = new ServiceUpdate(processorState);
            serviceUpdate.update(request.getParts());
        }
        ServletUtils.write(response, ServletUtils.toRedirect(request.getRequestURI()));
    }

    private HttpResponse toStateXml(final String processorId) {
        final ProbeProcessorState processorState = serviceState.getProcessorState(processorId);
        final byte[] xml = (processorState == null) ? null : processorState.toXml();
        return (xml == null)
                ? ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND)
                : ServletUtils.toResponseOk(Probe.Mime.TEXT_XML_UTF8, xml);
    }
}
