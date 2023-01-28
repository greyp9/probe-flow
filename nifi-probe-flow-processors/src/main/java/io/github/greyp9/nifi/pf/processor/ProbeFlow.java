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
package io.github.greyp9.nifi.pf.processor;

import io.github.greyp9.nifi.pf.core.ProbeUtils;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf.service.ProbeFlowControllerService;
import org.apache.nifi.annotation.behavior.Restricted;
import org.apache.nifi.annotation.behavior.Restriction;
import org.apache.nifi.annotation.behavior.TriggerWhenEmpty;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.lifecycle.OnUnscheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.RequiredPermission;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
@CapabilityDescription("Provide interactivity to manually debug a NiFi flow")
@Tags({"debug", "probe"})
@Restricted(restrictions = {
        @Restriction(requiredPermission = RequiredPermission.EXECUTE_CODE,
                explanation = "Provides operator with the ability to "
                        + "insert, modify, and delete FlowFiles from the NiFi flow.")
}
)
@TriggerWhenEmpty
public final class ProbeFlow extends AbstractProcessor {

    public static final PropertyDescriptor RELATIONSHIPS = new PropertyDescriptor.Builder()
            .name("relationships")
            .displayName("Relationships")
            .description("The (dynamic) set of outgoing relationships.  Define additional relationships using a "
                    + "comma-separated list of strings (e.g. 'X,Y,Z').")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .defaultValue("Outgoing")
            .build();
    public static final PropertyDescriptor CONTROLLER_SERVICE = new PropertyDescriptor.Builder()
            .name("controller-service")
            .displayName("Controller Service")
            .description("The Probe Controller Service for this processor.  Use the service configuration to define "
                    + "the port on which the processor web interface is available.")
            .required(true)
            .identifiesControllerService(ProbeFlowControllerService.class)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .build();

    private static final List<PropertyDescriptor> PROPERTY_DESCRIPTORS = Arrays.asList(
            RELATIONSHIPS, CONTROLLER_SERVICE
    );

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    private ProbeServiceState probeServiceState;
    private ProbeProcessorState probeProcessorState;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final Set<Relationship> set = new HashSet<>();
        set.add(REL_OUTGOING);
        relationships = new AtomicReference<>(set);
    }

    @Override
    public void onPropertyModified(final PropertyDescriptor descriptor, final String oldValue, final String newValue) {
        if (descriptor.equals(RELATIONSHIPS)) {
            relationships.set(toRelationships(newValue));
        }
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        getLogger().info("onScheduled()");
        final ProbeFlowControllerService probeService =
                context.getProperty(CONTROLLER_SERVICE).asControllerService(ProbeFlowControllerService.class);
        probeServiceState = probeService.getProbeState();
        probeProcessorState = probeServiceState.register(getIdentifier(), context.getName(), getRelationships());
    }

    @OnUnscheduled
    public void onUnscheduled(final ProcessContext context) {
        getLogger().info("onUnscheduled()");
        probeServiceState.unregister(getIdentifier());
        probeProcessorState = null;
        probeServiceState = null;
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        boolean consume = false;
        final int count = probeProcessorState.shouldConsume();
        if (count > 0) {
            final List<FlowFile> flowFilesIn = session.get(count);
            for (final FlowFile flowFileIn : flowFilesIn) {
                probeProcessorState.consumeFlowFile(fromProcessSession(session, flowFileIn));
                session.remove(flowFileIn);
            }
            if (!flowFilesIn.isEmpty()) {
                session.commit();
                consume = true;
            } else {
                session.rollback();
            }
        }

        final List<ProbeFlowFile> flowFilesRouted = probeProcessorState.drainTo();
        final boolean produce = !flowFilesRouted.isEmpty();
        for (final ProbeFlowFile flowFile : flowFilesRouted) {
            final FlowFile flowFileIt = toProcessSession(session, flowFile);
            session.transfer(flowFileIt, asRelationship(flowFile.getRelationship()));
        }
        if (produce) {
            session.commit();
        }

        if ((!consume) && (!produce)) {
            context.yield();
        }
    }

    private ProbeFlowFile fromProcessSession(final ProcessSession session, final FlowFile flowFile) {
        try {
            final long entryDate = flowFile.getEntryDate();
            final Map<String, String> attributes = new HashMap<>(flowFile.getAttributes());
            attributes.put(Probe.App.FLOWFILE_ID_IN, Long.toString(flowFile.getId()));
            try (InputStream read = session.read(flowFile)) {
                final byte[] data = ProbeUtils.toBytes(read);
                return probeProcessorState.create(entryDate, attributes, data);
            }
        } catch (final IOException e) {
            throw new ProcessException(e);
        }
    }

    private FlowFile toProcessSession(final ProcessSession session, final ProbeFlowFile probeFlowFile) {
        FlowFile flowFile = session.create();
        flowFile = session.write(flowFile, out -> out.write(probeFlowFile.getData()));
        return session.putAllAttributes(flowFile, probeFlowFile.getAttributes());
    }

    private Set<Relationship> toRelationships(final String config) {
        final Set<Relationship> relationshipsUpdate = new HashSet<>();
        final String[] names = config.split(",");
        if (names.length == 0) {
            relationshipsUpdate.add(REL_OUTGOING);
        } else {
            for (String name : names) {
                relationshipsUpdate.add(new Relationship.Builder().name(name).build());
            }
        }
        return relationshipsUpdate;
    }

    private Relationship asRelationship(final String name) {
        return getRelationships().stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships.get();
    }

    private AtomicReference<Set<Relationship>> relationships = new AtomicReference<>();

    public static final Relationship REL_OUTGOING = new Relationship.Builder()
            .name("Outgoing")
            .description("Default Relationship").build();
}
