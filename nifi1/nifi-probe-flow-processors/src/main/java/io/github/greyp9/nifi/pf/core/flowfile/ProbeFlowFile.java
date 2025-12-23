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
package io.github.greyp9.nifi.pf.core.flowfile;

import org.apache.nifi.flowfile.FlowFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ProbeFlowFile implements FlowFile {

    private final long id;
    private final long entryDate;
    private final Map<String, String> attributes;
    private final byte[] data;

    private String relationship;

    public ProbeFlowFile(final long id, final long entryDate, final Map<String, String> attributes, final byte[] data) {
        this.id = id;
        this.entryDate = entryDate;
        this.attributes = new HashMap<>(attributes);
        this.data = data;
        this.relationship = null;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public long getEntryDate() {
        return entryDate;
    }

    @Override
    public long getLineageStartDate() {
        return 0L;
    }

    @Override
    public long getLineageStartIndex() {
        return 0L;
    }

    @Override
    public Long getLastQueueDate() {
        return null;
    }

    @Override
    public long getQueueDateIndex() {
        return 0L;
    }

    @Override
    public boolean isPenalized() {
        return false;
    }

    @Override
    public String getAttribute(final String key) {
        return attributes.get(key);
    }

    @Override
    public long getSize() {
        return (data == null) ? 0L : data.length;
    }

    public boolean isNull() {
        return (data == null);
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    public byte[] getData() {
        return data;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(final String relationship) {
        this.relationship = relationship;
    }

    @Override
    public int compareTo(final FlowFile flowFile) {
        return (int) (id - flowFile.getId());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof FlowFile) {
            return (compareTo((FlowFile) o) == 0);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
