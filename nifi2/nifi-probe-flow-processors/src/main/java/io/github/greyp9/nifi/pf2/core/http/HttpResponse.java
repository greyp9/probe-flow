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

import java.io.ByteArrayInputStream;

/**
 * Container for data associated with a {@link jakarta.servlet.http.HttpServletResponse}.
 */
public class HttpResponse {

    private final int statusCode;
    private final Attributes headers;
    private final ByteArrayInputStream entity;

    public HttpResponse(final int statusCode, final Attributes headers, final ByteArrayInputStream entity) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.entity = entity;
    }

    public HttpResponse(final int statusCode, final Attributes headers, final byte[] entity) {
        this(statusCode, headers, ((entity == null) ? null : new ByteArrayInputStream(entity)));
    }

    public final int getStatusCode() {
        return statusCode;
    }

    public final Attributes getHeaders() {
        return headers;
    }

    public final ByteArrayInputStream getEntity() {
        return entity;
    }
}
