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
package io.github.greyp.nifi.pf.processor;

import io.github.greyp9.nifi.pf.core.alert.Alerts;
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.processor.ProbeFlow;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

public class LimitMemoryTest {

    @Test
    void testLimitMemory() {
        final String pid = "pidA";
        final long maxMemorySize = 1536L;
        final Set<Relationship> relationships = Collections.singleton(ProbeFlow.REL_OUTGOING);
        final ProbeProcessorState processorState = new ProbeProcessorState(
                pid, pid, maxMemorySize, relationships, new Alerts());

        final byte[] data = new byte[1024];
        final ProbeFlowFile flowFile = processorState.create(System.currentTimeMillis(), Collections.emptyMap(), data);

        processorState.addFlowFileProcessSession(flowFile);
        Assertions.assertThrows(ProcessException.class, () -> processorState.addFlowFileProcessSession(flowFile));
    }
}
