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
package io.github.greyp9.nifi.pf2.service;

import io.github.greyp9.nifi.pf2.core.state.ProbeProcessorState;
import io.github.greyp9.nifi.pf2.core.state.ProbeServiceState;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.processor.Relationship;

import java.util.Set;

public interface ProbeFlowControllerService extends ControllerService {

    ProbeServiceState getProbeState();

    ProbeProcessorState register(String id, String name, long maxMemorySize, Set<Relationship> relationships);

    void unregister(String id);
}
