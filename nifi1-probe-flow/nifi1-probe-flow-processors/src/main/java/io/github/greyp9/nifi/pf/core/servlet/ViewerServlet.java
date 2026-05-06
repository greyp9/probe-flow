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

import io.github.greyp9.nifi.pf.core.http.HttpResponse;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.core.view.ContentView;
import io.github.greyp9.nifi.pf.core.view.MetadataView;
import io.github.greyp9.nifi.pf.core.view.ProcessorView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ViewerServlet extends HttpServlet {
    private static final long serialVersionUID = 8894110246043694936L;

    private transient ProbeServiceState serviceState;

    @Override
    public void init() throws ServletException {
        super.init();
        serviceState = (ProbeServiceState) getServletContext().getAttribute(ProbeServiceState.class.getName());
    }

    private static final Pattern PATTERN_FLOWFILES = Pattern.compile("/viewer/(.+?)/flowfiles");
    private static final Pattern PATTERN_FLOWFILE_METADATA = Pattern.compile("/viewer/(.+?)/flowfile/metadata/(.+?)");
    private static final Pattern PATTERN_FLOWFILE_CONTENT = Pattern.compile("/viewer/(.+?)/flowfile/content/(.+?)");

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String requestURI = request.getRequestURI();
        final HttpResponse httpResponse;
        final Matcher matcherFlowFiles = PATTERN_FLOWFILES.matcher(requestURI);
        final Matcher matcherMetadata = PATTERN_FLOWFILE_METADATA.matcher(requestURI);
        final Matcher matcherContent = PATTERN_FLOWFILE_CONTENT.matcher(requestURI);
        if (matcherFlowFiles.matches()) {
            final ProcessorView view = new ProcessorView(serviceState);
            httpResponse = view.render(matcherFlowFiles.group(1), requestURI);
        } else if (matcherMetadata.matches()) {
            final MetadataView view = new MetadataView(serviceState);
            httpResponse = view.render(matcherMetadata.group(1), matcherMetadata.group(2));
        } else if (matcherContent.matches()) {
            final ContentView view = new ContentView(serviceState);
            httpResponse = view.render(matcherContent.group(1), matcherContent.group(2));
        } else {
            httpResponse = ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND);
        }
        ServletUtils.write(response, httpResponse);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        String location = request.getRequestURI();
        final Map<String, String[]> parameters = ServletUtils.toParameterMap(request);
        final Matcher matcherFlowFiles = PATTERN_FLOWFILES.matcher(request.getRequestURI());
        if (matcherFlowFiles.matches()) {
            final ProcessorView view = new ProcessorView(serviceState);
            location = view.update(matcherFlowFiles.group(1), location, parameters);
        }
        ServletUtils.write(response, ServletUtils.toRedirect(location));
    }
}
