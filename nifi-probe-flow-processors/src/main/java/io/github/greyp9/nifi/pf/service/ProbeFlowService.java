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
package io.github.greyp9.nifi.pf.service;

import io.github.greyp9.nifi.pf.core.server.ServerFactory;
import io.github.greyp9.nifi.pf.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf.core.state.ProbeServiceState;
import org.apache.nifi.annotation.behavior.Restricted;
import org.apache.nifi.annotation.behavior.Restriction;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.annotation.lifecycle.OnShutdown;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.RequiredPermission;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.ssl.SSLContextService;
import org.eclipse.jetty.server.Server;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Tags({"debug", "probe"})
@CapabilityDescription("ProbeFlowService implementation.")
@Restricted(restrictions = {
        @Restriction(requiredPermission = RequiredPermission.EXECUTE_CODE,
                explanation = "Provides operator with the ability to "
                        + "insert, modify, and delete FlowFiles from the NiFi flow.")
}
)
public final class ProbeFlowService extends AbstractControllerService implements ProbeFlowControllerService {

    public static final PropertyDescriptor PORT = new PropertyDescriptor.Builder()
            .name("Port")
            .description("The (SSL) port to listen on for incoming connections")
            .required(true)
            .addValidator(StandardValidators.PORT_VALIDATOR)
            .defaultValue("18443")
            .build();
    public static final PropertyDescriptor SSL_CONTEXT_SERVICE = new PropertyDescriptor.Builder()
            .name("ssl-context-service")
            .displayName("SSL Context Service")
            .description("The SSL Context Service for this processor")
            .required(true)
            .identifiesControllerService(SSLContextService.class)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .build();
    public static final PropertyDescriptor BASIC_AUTH = new PropertyDescriptor.Builder()
            .name("basic-auth")
            .displayName("Basic Authentication")
            .description("Specifies the Basic Authentication credentials (if any) "
                    + "required to connect to the ProbeFlow port.")
            .sensitive(true)
            .required(false)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();
    public static final PropertyDescriptor ENABLE_CERTIFICATE_AUTH = new PropertyDescriptor.Builder()
            .name("enable-cert-auth")
            .displayName("Use Client Certificate Authentication")
            .description("Specifies whether SSL Certificate Client Authentication is required to connect.")
            .required(false)
            .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
            .defaultValue(Boolean.FALSE.toString())
            .allowableValues("true", "false")
            .build();

    private static final List<PropertyDescriptor> PROPERTY_DESCRIPTORS = Arrays.asList(
            PORT,
            SSL_CONTEXT_SERVICE,
            BASIC_AUTH,
            ENABLE_CERTIFICATE_AUTH
    );

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return PROPERTY_DESCRIPTORS;
    }

    private ProbeServiceState probeServiceState;
    private Server server;

    @Override
    public ProbeServiceState getProbeState() {
        return probeServiceState;
    }

    @Override
    public ProbeProcessorState register(final String id, final String name,
                                        final long maxMemorySize, final Set<Relationship> relationships) {
        return probeServiceState.register(id, name, maxMemorySize, relationships);
    }

    @Override
    public void unregister(final String id) {
        probeServiceState.unregister(id);
    }

    @OnEnabled
    public void onEnabled(final ConfigurationContext context) {
        getLogger().info("onEnabled() [{}]", context);
        probeServiceState = new ProbeServiceState(getIdentifier(), context.getName());
        final int port = context.getProperty(PORT).asInteger();
        final SSLContextService sslContextService =
                context.getProperty(SSL_CONTEXT_SERVICE).asControllerService(SSLContextService.class);
        final String basicAuth = context.getProperty(BASIC_AUTH).getValue();
        final boolean certificateAuth = context.getProperty(ENABLE_CERTIFICATE_AUTH).asBoolean();
        server = new ServerFactory().create(probeServiceState,
                port, MAX_UPLOAD_SIZE, sslContextService, basicAuth, certificateAuth);

        try {
            server.start();
            getLogger().info("Server started");
        } catch (Exception e) {
            getLogger().error("Server failed to start", e);
        }
    }

    @OnShutdown
    @OnDisabled
    public void onDisabled(final ConfigurationContext context) {
        getLogger().info("onDisabled():START");

        try {
            server.stop();
            getLogger().info("Server stopped");
        } catch (Exception e) {
            getLogger().error("Server failed to stop", e);
        } finally {
            server = null;
        }
        probeServiceState = null;

        getLogger().info("onDisabled():FINISH");
    }

    /**
     * Size large enough to accommodate a large incoming payload.
     */
    private static final int MAX_UPLOAD_SIZE = 5 * 1024 * 1024;
}
