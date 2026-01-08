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
package io.github.greyp9.nifi.pf2.core.server;

import io.github.greyp9.nifi.pf2.core.servlet.DashServlet;
import io.github.greyp9.nifi.pf2.core.servlet.EditorServlet;
import io.github.greyp9.nifi.pf2.core.servlet.RootServlet;
import io.github.greyp9.nifi.pf2.core.servlet.StateServlet;
import io.github.greyp9.nifi.pf2.core.servlet.ViewerServlet;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import jakarta.servlet.MultipartConfigElement;
import org.apache.nifi.security.util.TlsConfiguration;
import org.apache.nifi.ssl.SSLContextService;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee11.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.UserStore;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import java.util.Collections;

public final class ServerFactory {

    public ServerFactory() {
    }

    public Server create(final ProbeServiceState probeServiceState, final int port, final int maxUploadSize,
                         final SSLContextService sslContextService,
                         final String basicAuth, final boolean enableClientAuth) {
        final TlsConfiguration tlsConfiguration = sslContextService.createTlsConfiguration();
        final SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(tlsConfiguration.getKeystorePath());
        sslContextFactory.setKeyStorePassword(tlsConfiguration.getKeystorePassword());
        sslContextFactory.setKeyManagerPassword(tlsConfiguration.getKeyPassword());
        sslContextFactory.setNeedClientAuth(enableClientAuth);

        // https://www.eclipse.org/jetty/documentation/jetty-9/index.html#jetty-helloworld
        // https://stackoverflow.com/questions/39421686/jetty-pass-object-from-main-method-to-servlet
        final Server server = new Server();

        final ServletContextHandler contextHandler = new ServletContextHandler();
        contextHandler.setContextPath("/");
/* alternative; regex handling of URL path
        org.eclipse.jetty.servlet.ServletHandler;
        org.eclipse.jetty.servlet.ServletHolder;

        contextHandler.setServletHandler(new ServletHandler()
        {
            @Override
            protected PathSpec asPathSpec(String pathSpec)
            {
                return PathMappings.asPathSpec(pathSpec);
            }
        });
        contextHandler.addServlet(RootServlet.class, "^/([Hh])ello/(.+)/(.+)");
        contextHandler.addServlet(new ServletHolder(RootServlet.class), "^/([Hh])ello/(.+)/(.+)");
*/
        contextHandler.setAttribute(probeServiceState.getClass().getName(), probeServiceState);
        contextHandler.addServlet(RootServlet.class, "/*");
        contextHandler.addServlet(DashServlet.class, "/dash/*");
        contextHandler.addServlet(StateServlet.class, "/state/*").getRegistration()
                .setMultipartConfig(new MultipartConfigElement(null, maxUploadSize, maxUploadSize, maxUploadSize));
        contextHandler.addServlet(ViewerServlet.class, "/viewer/*");
        contextHandler.addServlet(EditorServlet.class, "/editor/*");
        contextHandler.addServlet(EditorServlet.class, "/editor/text/*");
        contextHandler.addServlet(EditorServlet.class, "/editor/file/*").getRegistration()
                .setMultipartConfig(new MultipartConfigElement(null, maxUploadSize, maxUploadSize, maxUploadSize));

        if (basicAuth != null) {
            final ConstraintSecurityHandler securityHandler = getSecurityHandler(server, basicAuth);
            server.setHandler(securityHandler);
            securityHandler.setHandler(contextHandler);
        } else {
            server.setHandler(contextHandler);
        }

        final ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory());
        sslConnector.setPort(port);
        server.addConnector(sslConnector);

        return server;
    }

    private ConstraintSecurityHandler getSecurityHandler(final Server server, final String basicAuth) {
        final int separator = basicAuth.indexOf(":");
        final boolean isPassword = (separator >= 0);
        final String username = isPassword ? basicAuth.substring(0, separator) : basicAuth;
        final String credential = isPassword ? basicAuth.substring(separator + 1) : "";

        final UserStore userStore = new UserStore();
        userStore.addUser(username, Credential.getCredential(credential), APP_ROLES);

        final HashLoginService loginService = new HashLoginService(REALM);
        loginService.setUserStore(userStore);
        server.addBean(loginService);

        final ConstraintSecurityHandler securityHandler = new ConstraintSecurityHandler();

        final Constraint constraint = new Constraint.Builder()
                .authorization(Constraint.Authorization.SPECIFIC_ROLE)
                .roles(APP_ROLES).build();

        final ConstraintMapping mapping = new ConstraintMapping();
        mapping.setPathSpec("/*");
        mapping.setConstraint(constraint);

        securityHandler.setConstraintMappings(Collections.singletonList(mapping));
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setLoginService(loginService);
        return securityHandler;
    }

    /**
     * The Basic Authentication realm managing access to the webapp (if enabled).
     */
    private static final String REALM = ProbeServiceState.class.getSimpleName();

    /**
     * The webapp role used for webapp access decisions.
     */
    private static final String[] APP_ROLES = new String[]{"ProbeFlow"};
}
