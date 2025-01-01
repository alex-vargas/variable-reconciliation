
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;

import utep.ilink.dropwizardtemplate.DropwizardTemplateApplication;
import utep.ilink.dropwizardtemplate.config.MainConfig;

@ExtendWith(DropwizardExtensionsSupport.class)
public class AppTest {

    private static final DropwizardAppExtension<MainConfig> EXT = new DropwizardAppExtension<>(
            DropwizardTemplateApplication.class,
            "settings.yml");

    private final String url = String.format("http://localhost:%d/main/", EXT.getLocalPort());

    private final Client client = EXT.client();

    // @Test
    public void customTestCases() throws IOException {
        String[] testCasesNames = {  };
        for(String testCaseName : testCasesNames){
            runTestCase(testCaseName);
        }

    }

    /**
     * Testing with ignoring a scientific assumption
     * @throws IOException
     */
    @Test
    public void ignoreTestCase() throws IOException {
        String testCaseName = "sample1";
        runTestCase(testCaseName);
    }

    /**
     * This test case includes an abstract assumption that contains a range
     * @throws IOException
     */
    @Test
    public void doNotIgnoreTestCase() throws IOException {
        String testCaseName = "sample2";
        runTestCase(testCaseName);
    }

    /**
     * First test case including a scientific assumption
     * @throws IOException
     */
    @Test
    public void environmentalTestCase() throws IOException {
        String testCaseName = "environmental-case-study";
        runTestCase(testCaseName);
    }

    /**
     * Case study including continous data
     * @throws IOException
     */
    @Test
    public void environmentalTestCaseContinous() throws IOException {
        String testCaseName = "environmental-case-study-continous-data";
        runTestCase(testCaseName);
    }

    /**
     * Test case described in eScience paper
     * @throws IOException
     */
    @Test
    public void eScience2022TestCase() throws IOException {
        String testCaseName = "eScience2022-case-study";
        runTestCase(testCaseName);
    }

    /**
     * Test case to show examples in dissertation manuscript
     * @throws IOException
     */
    @Test
    public void dissertationExampleTestCase() throws IOException {
        String testCaseName = "dissertation-example";
        runTestCase(testCaseName);
    }

    /**
     * This method will make a request to the endpoint given the name of the testcase. If a test case is to be run, this method should be called.
     * @param testCaseName The name of the test case, it needs to match the name of the folder that contains all required information. (available variables, ignore list, expected output)
     * @throws IOException
     */
    public void runTestCase(String testCaseName) throws IOException {

        System.out.println("\n");
        for (int i = 0; i < 100; i++) {
            System.out.print("-");
        }
        System.out.println("\n");
        System.out.println("\n");
        System.out.println("\t\t\tRunning test: " + testCaseName);
        System.out.println("\n");
        System.out.println("\n");

        String componentCatalogFilePath = testCaseName + "/componentCatalog.json";
        String availableDataFilePath = testCaseName + "/availableData.json";
        String ignoreFilePath = testCaseName + "/ignore.json";
        String rulesFilePath = testCaseName + "/output.json";

        String rules = "";
        rules = readTestDataFromFile(rulesFilePath);

        assertEquals(rules, callWebservice(componentCatalogFilePath, availableDataFilePath, ignoreFilePath));
    }

    /**
     * Reads a file and parses its content to a String
     * @param filePath
     * @return
     * @throws IOException
     */
    private String readTestDataFromFile(String filePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(filePath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString().trim();
        }
    }

    /**
     * Makes a request to a webservice. If a test case is to be run, this method should not be called.
     * @param componentCatalogFilePath
     * @param requestFilePath
     * @param ignoreFilePath
     * @return
     * @throws IOException
     */
    private String callWebservice(String componentCatalogFilePath, String requestFilePath, String ignoreFilePath)
            throws IOException {
        String componentCatalog, request, ignore;
        String responseBody = "";

        // Reading component catalog
        componentCatalog = readTestDataFromFile(componentCatalogFilePath);
        // Reading request (contains available input and exected output)
        request = readTestDataFromFile(requestFilePath);
        // Reading ignore list
        ignore = readTestDataFromFile(ignoreFilePath);

        // Creating request body
        MultivaluedMap<String, String> formData = new MultivaluedHashMap<>();
        formData.add("availableData", request);
        formData.add("componentCatalog", componentCatalog);
        formData.add("ignore", ignore);

        // Calling webservice
        // Response response = client.target(url).request().post(Entity.form(formData));
        Response response = client.target(url).request().post(Entity.form(formData));

        // Getting response
        // String responseHeaders = response.getStringHeaders().toString();
        responseBody = response.readEntity(String.class);

        System.out.println("\n");
        for (int i = 0; i < 100; i++) {
            System.out.print("-");
        }
        System.out.println("\n");
        // System.out.println("Response Headers: " + responseHeaders);
        System.out.println("Response Body: " + responseBody);

        return responseBody;
    }

}