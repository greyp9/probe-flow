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
package io.github.greyp9.nifi.pf.core.servlet;

import io.github.greyp9.nifi.pf.core.ProbeUtils;
import io.github.greyp9.nifi.pf.core.common.Attribute;
import io.github.greyp9.nifi.pf.core.common.Attributes;
import io.github.greyp9.nifi.pf.core.common.Probe;
import io.github.greyp9.nifi.pf.core.http.HttpRequest;
import io.github.greyp9.nifi.pf.core.http.HttpResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServletUtils {

    /**
     * <a href="https://checkstyle.sourceforge.io/config_design.html#FinalClass">Constructor</a>
     */
    private ServletUtils() {
    }

    public static String toId(final HttpServletRequest request) {
        final Pattern pattern = Pattern.compile("/(.*)");
        final Matcher matcher = pattern.matcher(request.getPathInfo());
        return (matcher.matches()) ? matcher.group(1) : "";
    }

    public static HttpRequest read(final HttpServletRequest servletRequest) throws IOException {
        final String method = servletRequest.getMethod();
        final String resource = servletRequest.getRequestURI();
        final String query = servletRequest.getQueryString();
        final Attributes headers = toHeaders(servletRequest);
        return new HttpRequest(method, resource, query, headers, servletRequest.getInputStream());
    }

    private static Attributes toHeaders(final HttpServletRequest request) {
        final Attributes headers = new Attributes();
        final Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            final Enumeration<?> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                final String value = (String) values.nextElement();
                headers.add(new Attribute(name, value));
            }
        }
        return headers;
    }

    public static Map<String, String[]> toParameterMap(final HttpServletRequest servletRequest) {
        final Map<String, String[]> parameters = new LinkedHashMap<>();
        if (Probe.Http.FORM_URL_ENCODED.equals(servletRequest.getHeader(Probe.Http.CONTENT_TYPE))) {
            parameters.putAll(servletRequest.getParameterMap());
        }
        return parameters;
    }

    public static String getParameter(final String key, final Map<String, String[]> parameters) {
        final String value;
        final String[] values = parameters.get(key);
        if (values == null) {
            value = null;
        } else if (values.length == 0) {
            value = "";
        } else {
            value = values[0];
        }
        return value;
    }

    public static void write(final HttpServletResponse servletResponse,
                             final HttpResponse httpResponse) throws IOException {
        servletResponse.setStatus(httpResponse.getStatusCode());
        for (final Attribute nameValue : httpResponse.getHeaders()) {
            servletResponse.addHeader(nameValue.getName(), nameValue.getValue());
        }
        servletResponse.getOutputStream().write(ProbeUtils.toBytes(httpResponse.getEntity()));
    }

    public static HttpResponse toResponseOk(final String contentType, final byte[] entity) {
        return toResponse(HttpURLConnection.HTTP_OK, contentType, entity);
    }

    public static HttpResponse toResponse(final int statusCode, final String contentType, final byte[] entity) {
        Objects.requireNonNull(entity);
        final Attributes headers = new Attributes(
                new Attribute(Probe.Http.CONTENT_TYPE, contentType),
                new Attribute(Probe.Http.CONTENT_LENGTH, Integer.toString(entity.length)));
        return new HttpResponse(statusCode, headers, entity);
    }

    public static HttpResponse toRedirect(final String location) {
        final Attributes headers = new Attributes(new Attribute(Probe.Http.LOCATION, location));
        return new HttpResponse(HttpServletResponse.SC_FOUND, headers, new byte[0]);
    }

    public static HttpResponse toError(final int statusCode) {
        final byte[] entity = Integer.toString(statusCode).getBytes(StandardCharsets.UTF_8);
        return toResponse(statusCode, Probe.Mime.TEXT_PLAIN, entity);
    }

    public static void finishResponse(final byte[] payload, final String contentType,
                                      final HttpServletResponse response) throws IOException {
        finishResponse(HttpServletResponse.SC_OK, contentType, payload, response);
    }

    public static void finishResponse(final int httpStatus,
                                      final HttpServletResponse response) throws IOException {
        final byte[] payload = Integer.toString(httpStatus).getBytes(StandardCharsets.UTF_8);
        finishResponse(httpStatus, Probe.Mime.TEXT_PLAIN, payload, response);
    }

    public static void finishResponse(final int statusCode, final String contentType, final byte[] payload,
                                      final HttpServletResponse response) throws IOException {
        response.setStatus(statusCode);
        response.setContentType(contentType);
        response.setContentLength(payload.length);
        response.getOutputStream().write(payload);
    }
}
