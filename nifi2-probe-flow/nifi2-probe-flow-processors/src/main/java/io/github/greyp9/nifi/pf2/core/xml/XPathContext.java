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
package io.github.greyp9.nifi.pf2.core.xml;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;

public final class XPathContext implements javax.xml.namespace.NamespaceContext {
    private final Map<String, String> prefixToURI;
    private final Map<String, String> uriToPrefix;

    public XPathContext() {
        this.prefixToURI = new TreeMap<>();
        this.uriToPrefix = new TreeMap<>();
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        String orDefault = prefixToURI.getOrDefault(prefix, XMLConstants.NULL_NS_URI);
        return orDefault;
    }

    @Override
    public String getPrefix(final String namespaceURI) {
        String orDefault = uriToPrefix.getOrDefault(namespaceURI, XMLConstants.DEFAULT_NS_PREFIX);
        return orDefault;
    }

    @Override
    public Iterator<String> getPrefixes(final String namespaceURI) {
        List<String> collect = uriToPrefix.entrySet().stream()
                .filter(e -> e.getKey().equals(namespaceURI))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        return collect.iterator();
    }

    public void addMapping(final String prefix, final String uri) {
        prefixToURI.put(prefix, uri);
        uriToPrefix.put(uri, prefix);
    }
}
