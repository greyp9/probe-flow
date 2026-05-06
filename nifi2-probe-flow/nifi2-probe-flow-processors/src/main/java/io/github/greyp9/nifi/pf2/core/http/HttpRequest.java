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
package io.github.greyp9.nifi.pf2.core.http;

import io.github.greyp9.nifi.pf2.core.common.Attributes;

import java.io.InputStream;

/**
 * Container for data associated with a {@link jakarta.servlet.http.HttpServletRequest}.
 */
public class HttpRequest {
    private final String method;
    private final String resource;
    private final String query;
    private final Attributes headers;
    private final InputStream entity;

    public final String getMethod() {
        return method;
    }

    public final String getResource() {
        return resource;
    }

    public final String getQuery() {
        return query;
    }

    public final Attributes getHeaders() {
        return headers;
    }

    public final InputStream getEntity() {
        return entity;
    }

    public HttpRequest(final String method, final String resource, final String query,
                       final Attributes headers, final InputStream entity) {
        this.method = method;
        this.resource = resource;
        this.query = query;
        this.headers = headers;
        this.entity = entity;
    }
}
