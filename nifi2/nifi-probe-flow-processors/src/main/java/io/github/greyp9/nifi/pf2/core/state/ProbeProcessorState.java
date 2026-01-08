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
package io.github.greyp9.nifi.pf2.core.state;

import io.github.greyp9.nifi.pf2.core.alert.Alert;
import io.github.greyp9.nifi.pf2.core.alert.Alerts;
import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.flowfile.ProbeFlowFile;
import io.github.greyp9.nifi.pf2.core.flowfile.ProbeFlowFileEditor;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class ProbeProcessorState {

    /**
     * Processor identifier.
     */
    private final String processorId;

    /**
     * Processor name.
     */
    private final String processorName;

    /**
     * Start time of processor.
     */
    private final Date start;

    /**
     * Timestamp of last processor <code>onTrigger()</code> call.
     */
    private Date lastOnTrigger;

    /**
     * Processor-unique ordinal for held FlowFiles.
     */
    private final AtomicLong nextFlowFileId;

    /**
     * The number of upstream FlowFiles that should be consumed on subsequent invocations of "onTrigger()".
     */
    private final AtomicInteger countToConsume;

    /**
     * Maximum size of FlowFile content held.
     */
    private final long maxMemorySize;

    /**
     * Messages to present to user.
     */
    private final Alerts alerts;

    /**
     * Processor-internal storage for held FlowFiles.
     */
    private final LinkedBlockingQueue<ProbeFlowFile> flowFiles;

    /**
     * Set of {@link Relationship} configured for processor.
     */
    private final Set<Relationship> relationships;

    /**
     * Holder of state for freeform editing of one {@link org.apache.nifi.flowfile.FlowFile}.
     */
    private final ProbeFlowFileEditor flowFileEditor;

    /**
     * Constructor.
     *
     * @param id            NiFi processor identifier
     * @param name          NiFi processor name
     * @param maxMemorySize Maximum size of FlowFile content held
     * @param relationships Set of {@link Relationship} configured for processor
     * @param alerts        messages to present to user
     */
    public ProbeProcessorState(final String id, final String name, final long maxMemorySize,
                               final Set<Relationship> relationships, final Alerts alerts) {
        this.processorId = id;
        this.processorName = name;
        this.maxMemorySize = maxMemorySize;
        this.start = new Date();
        this.nextFlowFileId = new AtomicLong(0);
        this.countToConsume = new AtomicInteger(0);
        this.flowFiles = new LinkedBlockingQueue<>();
        this.relationships = new HashSet<>(relationships);
        this.flowFileEditor = new ProbeFlowFileEditor();
        this.alerts = alerts;
    }

    public String getId() {
        return processorId;
    }

    public String getName() {
        return processorName;
    }

    public String getStart() {
        return start.toInstant().toString();
    }

    public String getLastOnTrigger() {
        return (lastOnTrigger == null) ? "-" : lastOnTrigger.toInstant().toString();
    }

    public Set<String> getRelationships() {
        return relationships.stream().map(Relationship::getName).collect(Collectors.toSet());
    }

    /**
     * @return number of FlowFiles that should be pulled from upstream queue(s) (based on cached manual instruction)
     */
    public int shouldConsume() {
        lastOnTrigger = new Date();
        return countToConsume.get();
    }

    /**
     * @param amount the number of FlowFiles that should be consumed from upstream queue(s)
     * @return the updated number of FlowFiles to be consumed
     */
    public int incrementToConsume(final int amount) {
        return countToConsume.addAndGet(amount);
    }

    /**
     * @return FlowFiles held in state of processor
     */
    public List<ProbeFlowFile> getFlowFiles() {
        return new ArrayList<>(flowFiles);
    }

    /**
     * @return the count of FlowFiles marked to be routed to outgoing relationships
     */
    public long getFlowFilesRouted() {
        return flowFiles.stream().filter(ff -> (ff.getRelationship() != null)).count();
    }

    /**
     * @return holder of state for freeform editing of one {@link org.apache.nifi.flowfile.FlowFile}
     */
    public ProbeFlowFileEditor getFlowFileEditor() {
        return flowFileEditor;
    }

    /**
     * @return the count of FlowFiles held in state of processor
     */
    public int flowFileCount() {
        return flowFiles.size();
    }

    /**
     * @return the count of FlowFiles marked to be routed to outgoing relationships
     */
    public int flowFileRoutedCount() {
        return (int) flowFiles.stream().filter(f -> (f.getRelationship() != null)).count();
    }

    public ProbeFlowFile create(final long entryDate, final Map<String, String> attributes, final byte[] data) {
        return new ProbeFlowFile(nextFlowFileId.incrementAndGet(), entryDate, attributes, data);
    }

    public String addFlowFile(final ProbeFlowFile flowFile) {
        final long memorySizeCurrent = flowFiles.stream().mapToLong(ff -> ff.getData().length).sum();
        final boolean memorySizeExceeded = ((memorySizeCurrent + flowFile.getData().length) > maxMemorySize);
        final String errorMessage = (memorySizeExceeded ? "processor memory limit exceeded" : null);
        if (memorySizeExceeded) {
            alerts.add(new Alert(Alert.Severity.ERR, new Date(), errorMessage));
        } else {
            flowFiles.add(flowFile);
        }
        return errorMessage;
    }

    public void addFlowFileProcessSession(final ProbeFlowFile flowFile) {
        countToConsume.decrementAndGet();
        final String errorMessage = addFlowFile(flowFile);
        if (errorMessage != null) {
            throw new ProcessException(errorMessage);
        }
    }

    public Optional<ProbeFlowFile> getFlowFile(final String idString) {
        final long id = Long.parseLong(idString);
        return flowFiles.stream().filter(ff -> ff.getId() == id).findFirst();
    }

    public void routeFlowFile(final String value) {
        final Matcher matcher = PATTERN.matcher(value);
        if (matcher.matches()) {
            final String id = matcher.group(1);
            final String relationship = matcher.group(2);
            routeFlowFile(id, relationship);
        }
    }

    public void routeFlowFile(final String idString, final String relationship) {
        final long id = Long.parseLong(idString);
        final Optional<ProbeFlowFile> flowFile = flowFiles.stream().filter(ff -> ff.getId() == id).findFirst();
        flowFile.ifPresent(ff -> ff.setRelationship(relationship));
    }

    public String actionFlowFile(final String pid, final String value, final String location) {
        String locationAction = location;
        final Matcher matcher = PATTERN.matcher(value);
        if (matcher.matches()) {
            final String ffid = matcher.group(1);
            final String action = matcher.group(2);
            locationAction = actionFlowFile(pid, ffid, action, location);
        }
        return locationAction;
    }

    public String actionFlowFile(final String pid, final String ffid, final String action, final String location) {
        String locationAction = location;
        final long flowFileId = Long.parseLong(ffid);
        if (Probe.Action.CLONE.equals(action)) {
            cloneFlowFile(flowFileId);
        } else if (Probe.Action.DROP.equals(action)) {
            dropFlowFile(flowFileId);
        } else if (Probe.Action.EDIT.equals(action)) {
            locationAction = editFlowFile(pid, flowFileId);
        }
        return locationAction;
    }

    private static final Pattern PATTERN = Pattern.compile("\\[(\\d+)]\\[(\\w+)]");

    private void cloneFlowFile(final long flowFileId) {
        final Optional<ProbeFlowFile> flowFile = flowFiles.stream().filter(ff -> ff.getId() == flowFileId).findFirst();
        flowFile.ifPresent(ff -> addFlowFile(create(System.currentTimeMillis(), ff.getAttributes(), ff.getData())));
    }

    private void dropFlowFile(final long flowFileId) {
        final Optional<ProbeFlowFile> flowFile = flowFiles.stream().filter(ff -> ff.getId() == flowFileId).findFirst();
        //noinspection ResultOfMethodCallIgnored
        flowFile.ifPresent(flowFiles::remove);
    }

    private String editFlowFile(final String pid, final long flowFileId) {
        final Optional<ProbeFlowFile> flowFile = flowFiles.stream().filter(ff -> ff.getId() == flowFileId).findFirst();
        flowFile.ifPresent(flowFileEditor::set);
        return String.format("/editor/%s", pid);
    }

    public List<ProbeFlowFile> drainTo() {
        final List<ProbeFlowFile> flowFilesRouted = flowFiles.stream()
                .filter(ff -> (ff.getRelationship() != null)).collect(Collectors.toList());
        flowFiles.removeAll(flowFilesRouted);
        return flowFilesRouted;
    }

    public byte[] toXml() {
        return new ProbeSerializer().serialize(flowFiles);
    }

    public void addState(final byte[] xml) throws IOException {
        final Collection<ProbeFlowFile> flowFilesIn = new ProbeSerializer().deserialize(xml);
        for (final ProbeFlowFile flowFile : flowFilesIn) {
            if (addFlowFile(create(flowFile.getEntryDate(), flowFile.getAttributes(), flowFile.getData())) == null) {
                break;
            }
        }
    }
}
