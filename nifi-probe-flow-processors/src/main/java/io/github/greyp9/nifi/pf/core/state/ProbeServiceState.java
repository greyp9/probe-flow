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
package io.github.greyp9.nifi.pf.core.state;

import org.apache.nifi.processor.Relationship;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class ProbeServiceState {

    /**
     * Controller Service identifier.
     */
    private final String serviceId;

    /**
     * Controller Service name.
     */
    private final String serviceName;

    /**
     * Start time of controller service.
     */
    private final Date start;

    /**
     * Processors registered to this controller service.
     */
    private final Map<String, ProbeProcessorState> processorStates;

    /**
     * Constructor.
     *
     * @param id            NiFi controller service identifier
     * @param name          NiFi controller service name
     */
    public ProbeServiceState(final String id, final String name) {
        this.serviceId = id;
        this.serviceName = name;
        this.start = new Date();
        this.processorStates = new HashMap<>();
    }

    public String getId() {
        return serviceId;
    }

    public String getName() {
        return serviceName;
    }

    public String getStart() {
        return start.toInstant().toString();
    }

    public ProbeProcessorState register(final String pid, final String name,
                                        final long maxMemorySize, final Set<Relationship> relationships) {
        final ProbeProcessorState probeProcessorState = new ProbeProcessorState(
                pid, name, maxMemorySize, relationships);
        processorStates.put(pid, probeProcessorState);
        return probeProcessorState;
    }

    public void unregister(final String pid) {
        processorStates.remove(pid);
    }

    public Collection<ProbeProcessorState> getProcessorStates() {
        return processorStates.values();
    }

    public ProbeProcessorState getProcessorState(final String pid) {
        return processorStates.get(pid);
    }
}
