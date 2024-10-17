package org.apache.plc4x.nifi;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.plc4x.nifi.util.Plc4xCommonTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Plc4xNifiTest {
    

    private static File dumyAddressesFile;

    public static File getDumyAddressesFile() {
        return dumyAddressesFile;
    }

    @BeforeAll
    public static void beforeAll() {

        dumyAddressesFile = new File("dumy-address-file");

        try (FileWriter fileWriter = new FileWriter(dumyAddressesFile)) {
            ObjectMapper mapper = new ObjectMapper();
            fileWriter.write(mapper.writeValueAsString(Plc4xCommonTest.getAddressMap()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void afterAll() {
        dumyAddressesFile.delete();
    }
}
