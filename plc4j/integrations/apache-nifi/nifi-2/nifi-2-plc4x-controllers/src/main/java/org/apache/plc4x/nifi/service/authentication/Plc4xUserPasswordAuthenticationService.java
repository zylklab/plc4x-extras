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
package org.apache.plc4x.nifi.service.authentication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.ConfigVerificationResult;
import org.apache.nifi.components.ConfigVerificationResult.Outcome;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.controller.ControllerServiceInitializationContext;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.plc4x.java.api.authentication.PlcAuthentication;
import org.apache.plc4x.java.api.authentication.PlcUsernamePasswordAuthentication;
import org.apache.plc4x.nifi.service.authentication.api.Plc4xAuthenticationControllerService;


@Tags({ "plc4x", "authentication", "user", "password" })
@CapabilityDescription("")
public class Plc4xUserPasswordAuthenticationService extends AbstractControllerService implements Plc4xAuthenticationControllerService {

	private List<PropertyDescriptor> propertyDescriptors;

    private static final PropertyDescriptor USER_PROPERTY = new PropertyDescriptor.Builder()
        .name("Username")
        .required(true)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .build();

    private static final PropertyDescriptor PASSWORD_PROPERTY = new PropertyDescriptor.Builder()
        .name("Password")
        .required(true)
        .sensitive(true)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .build();

    @Override
    protected void init(ControllerServiceInitializationContext config) throws InitializationException {
        List<PropertyDescriptor> props = new ArrayList<>();
        props.add(USER_PROPERTY);
        props.add(PASSWORD_PROPERTY);
        this.propertyDescriptors = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propertyDescriptors;
    }

    @Override
    public List<ConfigVerificationResult> verify(ConfigurationContext context, ComponentLog verificationLogger,
            Map<String, String> variables) {

        return List.of(new ConfigVerificationResult.Builder()
                    .outcome(Outcome.SUCCESSFUL)
                    .build());
    }

    @OnEnabled
    public void enable(final ConfigurationContext context) {

    }


    @Override 
    public PlcAuthentication getAuthentication(FlowFile flowFile) {
        if (flowFile != null) {
            return new PlcUsernamePasswordAuthentication(
                    getProperty(USER_PROPERTY).evaluateAttributeExpressions(flowFile).getValue(),
                    getProperty(PASSWORD_PROPERTY).evaluateAttributeExpressions(flowFile).getValue());
        }

        return new PlcUsernamePasswordAuthentication(
                getProperty(USER_PROPERTY).getValue(),
                getProperty(PASSWORD_PROPERTY).getValue());
    }
    


}
