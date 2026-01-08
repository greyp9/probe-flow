package io.github.greyp9.nifi.pf2.core.state;

import io.github.greyp9.nifi.pf2.core.alert.Alerts;
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
     * Messages to present to user.
     */
    private final Alerts alerts;

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
        this.alerts = new Alerts();
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

    public Alerts getAlerts() {
        return alerts;
    }

    public ProbeProcessorState register(final String pid, final String name,
                                        final long maxMemorySize, final Set<Relationship> relationships) {
        final ProbeProcessorState probeProcessorState = new ProbeProcessorState(
                pid, name, maxMemorySize, relationships, alerts);
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
