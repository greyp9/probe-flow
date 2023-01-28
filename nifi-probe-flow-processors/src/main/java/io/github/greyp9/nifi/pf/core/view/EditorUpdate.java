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
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFileEditor;
import io.github.greyp9.nifi.pf.core.servlet.ServletUtils;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;

import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public final class EditorUpdate {
    private final ProbeProcessorState processorState;
    private final ProbeFlowFileEditor flowFileEditor;

    public EditorUpdate(final ProbeProcessorState processorState) {
        this.processorState = processorState;
        this.flowFileEditor = processorState.getFlowFileEditor();
    }

    public void update(final Map<String, String[]> parameters) {
        final String name = ServletUtils.getParameter(Probe.Form.NAME, parameters);
        final String value = ServletUtils.getParameter(Probe.Form.VALUE, parameters);
        if (parameters.containsKey(Probe.App.ADD_ATTRIBUTE)) {
            flowFileEditor.setAttribute(name, value);
        } else if (parameters.containsKey(Probe.App.DELETE_ATTRIBUTE)) {
            flowFileEditor.setAttribute(name, null);
        } else if (parameters.containsKey(Probe.App.UPDATE_TEXT)) {
            // https://www.w3.org/TR/html401/interact/forms.html#h-17.13.4.1
            final String text = ServletUtils.getParameter(Probe.Form.TEXT, parameters).replaceAll("\r\n", "\n");
            //logger.trace("TEXT: size=[{}], sha256=[{}]",
            //        text.length(), ProbeUtils.sha256(text.getBytes(StandardCharsets.UTF_8)));
            flowFileEditor.setContent(ProbeUtils.toBytesUTF8(text));
        } else if (parameters.containsKey(Probe.App.CREATE)) {
            final Map<String, String> attributes = flowFileEditor.getAttributes();
            final byte[] content = flowFileEditor.getContent().toByteArray();
            processorState.createFlowFile(processorState.create(System.currentTimeMillis(), attributes, content));
        } else if (parameters.containsKey(Probe.App.RESET)) {
            flowFileEditor.reset();
        }
    }

    public void update(final Collection<Part> parts) throws IOException {
        for (final Part part : parts) {
            if (part.getName().equals(Probe.App.UPLOAD_FILE)) {
                final byte[] bytes = ProbeUtils.toBytes(part.getInputStream());
                //logger.trace("UPLOAD: file=[{}], size=[{}], sha256=[{}]",
                //        part.getName(), bytes.length, ProbeUtils.sha256(bytes));
                flowFileEditor.setContent(bytes);
            }
        }
    }
}
