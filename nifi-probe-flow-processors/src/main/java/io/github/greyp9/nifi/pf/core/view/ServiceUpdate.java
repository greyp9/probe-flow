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
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;

public final class ServiceUpdate {
    private final ProbeProcessorState processorState;

    public ServiceUpdate(final ProbeProcessorState processorState) {
        this.processorState = processorState;
    }

    public void update(final Collection<Part> parts) throws IOException {
        final Logger logger = LoggerFactory.getLogger(getClass());
        for (final Part part : parts) {
            if (part.getName().equals(Probe.App.UPLOAD_FILE)) {
                final byte[] bytes = ProbeUtils.toBytes(part.getInputStream());
                logger.info("UPLOAD: file=[{}], size=[{}], sha256=[{}]",
                        part.getName(), bytes.length, ProbeUtils.sha256(bytes));
                if (bytes.length > 0) {
                    processorState.addState(bytes);
                }
            }
        }
    }
}
