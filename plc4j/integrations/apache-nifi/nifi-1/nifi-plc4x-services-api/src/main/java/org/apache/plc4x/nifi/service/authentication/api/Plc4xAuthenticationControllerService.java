/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.plc4x.nifi.service.authentication.api;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.controller.VerifiableControllerService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.plc4x.java.api.authentication.PlcAuthentication;

@Tags({ "plc4x", "authentication" })
@CapabilityDescription("A service for authentication on PLC4X connections.")
public interface Plc4xAuthenticationControllerService extends ControllerService, VerifiableControllerService {


    public PlcAuthentication getAuthentication(FlowFile flowFile);
}
