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
package io.github.greyp9.nifi.pf.core.xml;

import io.github.greyp9.nifi.pf.core.common.Probe;

import javax.xml.namespace.NamespaceContext;

public final class ProbeXml {

    /**
     * <a href="https://checkstyle.sourceforge.io/config_design.html#FinalClass">Constructor</a>
     */
    private ProbeXml() {
    }

    public static NamespaceContext getContext() {
        final XPathContext context = new XPathContext();
        context.addMapping(Probe.Xml.PREFIX_STATE, Probe.Xml.URI_STATE);
        return context;
    }
}
