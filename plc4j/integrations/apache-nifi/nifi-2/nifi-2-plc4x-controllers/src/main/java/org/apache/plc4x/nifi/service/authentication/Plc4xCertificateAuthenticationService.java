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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.nifi.annotation.behavior.RequiresInstanceClassLoading;
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
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.apache.plc4x.java.api.authentication.PlcAuthentication;
import org.apache.plc4x.java.api.authentication.PlcCertificateAuthentication;
import org.apache.plc4x.nifi.service.authentication.api.Plc4xAuthenticationControllerService;


@RequiresInstanceClassLoading
@Tags({ "plc4x", "authentication", "user", "password" })
@CapabilityDescription("")
public class Plc4xCertificateAuthenticationService extends AbstractControllerService implements Plc4xAuthenticationControllerService {

	private List<PropertyDescriptor> propertyDescriptors;

    private static final PropertyDescriptor KEYSTORE_PATH_PROPERTY = new PropertyDescriptor.Builder()
        .name("Key Store path")
        .required(true)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.FILE_EXISTS_VALIDATOR)
        .build();

    private static final PropertyDescriptor KEYSTORE_TYPE_PROPERTY = new PropertyDescriptor.Builder()
        .name("Key Store type")
        .description("Possible values are between others: jks, pkcs11, dks, jceks.")
        .required(true)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .build();

    private static final PropertyDescriptor KEYSTORE_PASSWORD_PROPERTY = new PropertyDescriptor.Builder()
        .name("Key Store Password")
        .required(true)
        .sensitive(true)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .build();

    private static final PropertyDescriptor KEY_ALIAS = new PropertyDescriptor.Builder()
        .name("Key Alias")
        .required(false)
        .expressionLanguageSupported(ExpressionLanguageScope.ENVIRONMENT)
        .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
        .addValidator(StandardValidators.ATTRIBUTE_EXPRESSION_LANGUAGE_VALIDATOR)
        .build();

    @Override
    protected void init(ControllerServiceInitializationContext config) throws InitializationException {
        List<PropertyDescriptor> props = new ArrayList<>();
        props.add(KEYSTORE_PATH_PROPERTY);
        props.add(KEYSTORE_TYPE_PROPERTY);
        props.add(KEYSTORE_PASSWORD_PROPERTY);
        props.add(KEY_ALIAS);
        
        this.propertyDescriptors = Collections.unmodifiableList(props);
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propertyDescriptors;
    }

    @Override
    public List<ConfigVerificationResult> verify(ConfigurationContext context, ComponentLog verificationLogger,
            Map<String, String> variables) {
        List<ConfigVerificationResult> results = new ArrayList<>();
        try {
            keyStore = KeyStore.getInstance(getProperty(KEYSTORE_TYPE_PROPERTY).getValue());
            
            results.add(new ConfigVerificationResult.Builder()
                    .verificationStepName("Validate KeyStoreSpi implementation")
                    .outcome(Outcome.SUCCESSFUL)
                    .explanation("Provider found for KeyStoreSpi implementation")
                    .build());

        } catch (final Exception e) {
            verificationLogger.error("No provider supports a KeyStoreSpi implementation for the specified type: " + getProperty(KEYSTORE_TYPE_PROPERTY).getValue(), e);
            results.add(new ConfigVerificationResult.Builder()
                    .verificationStepName("Validate KeyStoreSpi implementation")
                    .outcome(Outcome.FAILED)
                    .explanation("No provider supports a KeyStoreSpi implementation for the specified type: " + getProperty(KEYSTORE_TYPE_PROPERTY).getValue() + e.getMessage())
                    .build());
        }
        return results;
    }

    protected KeyStore keyStore;

    @OnEnabled
    public void enable(final ConfigurationContext context) {
        try {
            keyStore = KeyStore.getInstance(getProperty(KEYSTORE_TYPE_PROPERTY).getValue());
            
            try (InputStream in = new FileInputStream(getProperty(KEYSTORE_PATH_PROPERTY).getValue())) {
                keyStore.load(in, getProperty(KEYSTORE_PASSWORD_PROPERTY).getValue().toCharArray());

            } catch (IOException e) {
                throw new ProcessException("Could not load keystore at " + getProperty(KEYSTORE_PATH_PROPERTY).getValue(), e);
           
            } catch (NoSuchAlgorithmException | CertificateException e) {
                throw new ProcessException("Could not load certificates from keystore at " + getProperty(KEYSTORE_PATH_PROPERTY).getValue(), e);
            }
        } catch (KeyStoreException e) {
            throw new ProcessException("No provider supports a KeyStoreSpi implementation for the specified type: " + getProperty(KEYSTORE_TYPE_PROPERTY).getValue(), e);
        }
        
    }


    @Override 
    public PlcAuthentication getAuthentication(FlowFile flowFile) {

        if (flowFile != null) {
            return new PlcCertificateAuthentication(keyStore,
                    getProperty(KEYSTORE_PASSWORD_PROPERTY).evaluateAttributeExpressions(flowFile).getValue().toCharArray(),
                    getProperty(KEY_ALIAS).evaluateAttributeExpressions(flowFile).getValue());
        }
        return new PlcCertificateAuthentication(keyStore,
                    getProperty(KEYSTORE_PASSWORD_PROPERTY).getValue().toCharArray(),
                    getProperty(KEY_ALIAS).getValue());
    }

}
