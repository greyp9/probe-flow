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

import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf2.core.http.HttpResponse;
import io.github.greyp9.nifi.pf2.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf2.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;

import java.net.HttpURLConnection;
import java.util.Optional;

public final class ContentView {
    private final ProbeServiceState serviceState;

    public ContentView(final ProbeServiceState serviceState) {
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
        return flowFile.map(this::render).orElseGet(() -> ServletUtils.toError(HttpURLConnection.HTTP_NOT_FOUND));
    }

    private HttpResponse render(final ProbeFlowFile flowFile) {
        final String mimeType = flowFile.getAttribute(Probe.NiFi.ATTR_MIME_TYPE);
        final String contentType = (mimeType == null) ? Probe.Mime.TEXT_PLAIN : mimeType;
        return ServletUtils.toResponse(HttpURLConnection.HTTP_OK, contentType, flowFile.getData());
    }
}
