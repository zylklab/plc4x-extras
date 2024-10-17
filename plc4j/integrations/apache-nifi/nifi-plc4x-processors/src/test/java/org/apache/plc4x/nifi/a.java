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
package org.apache.plc4x.nifi;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;

import org.apache.plc4x.java.DefaultPlcDriverManager;
import org.apache.plc4x.java.api.PlcDriver;
import org.apache.plc4x.java.api.model.PlcTag;

import com.fasterxml.jackson.databind.ObjectMapper;

public class a {

    private static DefaultPlcDriverManager manager = new DefaultPlcDriverManager();

    public static void main(String[] args) throws Exception {
        // ObjectMapper mapper = new ObjectMapper();

        String input = "/home/usuario/tests/plc4x-nifi_tests/plc4x-extras/plc4j/integrations/apache-nifi/nifi-plc4x-processors/addresses.json";

        // Map<String, String> re = mapper.readerForMapOf(String.class).readValue(input);
        // System.out.println(re.toString());

        PlcDriver driver =  manager.getDriverForUrl("opcua:tcp://10.105.143.3:49320?discovery=false&username=zylk&password=Zylk200024Frio$");

        for (String tag : getTags(input)) {
            PlcTag tagg = driver.prepareTag(tag);

            System.out.println(tagg.toString());
        }
    }

    public static Map<String,String> extractAddressesFromFile(String fileName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Path filePath = Path.of(fileName);
        InputStream input = Files.newInputStream(filePath, StandardOpenOption.READ);
        
        return mapper.readerForMapOf(String.class).readValue(input);
    }

    public static Collection<String> getTags(String input) throws Exception {
            return extractAddressesFromFile(input).values();
    } 
}
