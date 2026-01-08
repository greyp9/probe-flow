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
package io.github.greyp9.nifi.pf2.core.servlet;

import io.github.greyp9.nifi.pf2.core.ProbeUtils;
import io.github.greyp9.nifi.pf2.core.common.Probe;
import io.github.greyp9.nifi.pf2.core.http.HttpResponse;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import io.github.greyp9.nifi.pf2.core.view.ServiceView;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Service the root context of the webapp.  Render view "Controller Service".
 */
public final class RootServlet extends HttpServlet {
    private static final long serialVersionUID = 9115823879423507386L;

    private transient ProbeServiceState serviceState;

    @Override
    public void init() throws ServletException {
        super.init();
        serviceState = (ProbeServiceState) getServletContext().getAttribute(ProbeServiceState.class.getName());
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        final HttpResponse httpResponse;
        if (request.getRequestURI().equals(Probe.Resource.FAVICON)) {
            final byte[] entity = ProbeUtils.toBytes(getClass(), FAVICON);
            httpResponse = ServletUtils.toResponseOk(Probe.Mime.IMAGE_ICON, entity);
        } else if (request.getRequestURI().equals(Probe.Resource.CSS)) {
            final byte[] entity = ProbeUtils.toBytes(getClass(), CSS);
            httpResponse = ServletUtils.toResponseOk(Probe.Mime.TEXT_CSS, entity);
        } else if (request.getRequestURI().equals(Probe.Resource.ROOT)) {
            httpResponse = new ServiceView(serviceState).render();
        } else {
            httpResponse = ServletUtils.toRedirect(Probe.Resource.ROOT);
        }
        ServletUtils.write(response, httpResponse);
    }

    private static final String CSS = "io/github/greyp9/nifi/pf2/probe.css";
    private static final String FAVICON = "io/github/greyp9/nifi/pf2/nifi16.ico";
}
