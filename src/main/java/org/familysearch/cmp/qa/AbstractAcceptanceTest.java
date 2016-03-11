package org.familysearch.cmp.qa;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import javax.ws.rs.core.MediaType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by rbirch on 12/7/2015.
 */
public abstract class AbstractAcceptanceTest {
  public enum Method {GET, POST, PUT, PATCH, DELETE}

  protected Client jerseyClient;

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private String domain;
  private String basePath;

  @BeforeClass(alwaysRun = true)
  @Parameters({"baseUrl"})
  public void setupClient(@Optional String baseUrl) {
    // There are two potential sources for the targetUrl, the environment variable and the testNG parameter.
    String testNgParamTargetUrl = baseUrl;
    String environmentTargetUrl = getTargetUrlFromEnv();

    // If the environment variable is set, it should be used.  Otherwise try using the testNG parameter
    if (environmentTargetUrl != null) {
      domain = environmentTargetUrl;
    }
    else if (testNgParamTargetUrl != null) {
      log.error("Target URL not defined by environment variable, falling back to testNG parameter. " +
          "The tests are probably not validating the new version of the application!");
      domain = testNgParamTargetUrl;
    }
    else {
      log.error("Failed to get target URL from testNG parameter or environment variable");
      throw new IllegalStateException("No target URL specified for target URL");
    }

    // Add the scheme to the URL string, if missing
    if(!domain.startsWith("http")) {
      domain = "http://" + domain;
    }

    // If the domain is familysearch.org, append the application's routing path
    if (domain.contains("familysearch.org")) {
      this.basePath = getBasePathWithRouting();
    }
    else {
      this.basePath = getBasePathDirect();
    }

    log.info("acceptance test URL: " + this.domain + basePath);

    ClientConfig config = new DefaultClientConfig();

    jerseyClient = Client.create(config);
  }

  public WebResource resourceForPath(String path) {
    return jerseyClient.resource(domain + basePath + path);
  }

  public ClientResponse getResponse(WebResource resource, Method method, String sessionId, String... jsonPayload) {
    WebResource.Builder builder = resource.accept(
        MediaType.APPLICATION_JSON)
        .header("Content-Type", "application/json");
    builder.header("Authorization", "Bearer " + sessionId);

    String payload = "";
    try {
      payload = jsonPayload[0];
    }
    catch (Exception ignore) {
    }

    switch (method) {
      case GET:
        return builder.get(ClientResponse.class);
      case POST:
        return builder.post(ClientResponse.class, payload);
      case PUT:
        return builder.put(ClientResponse.class, payload);
      case DELETE:
        return builder.delete(ClientResponse.class, payload);
    }
    return null;
  }

  /*
   * @param clientResponse - Jersey ClientResponse object
   * asserts that expectedResponseCode response is received
   */
  public void assertResponse(ClientResponse clientResponse, int expectedResponseCode) {
    assertEquals(clientResponse.getStatus(), expectedResponseCode,
      String.format("The http response had an incorrect status - message: %s", clientResponse));
  }

  /*
   * @param json - Jettison JSON object
   * @param fieldName - the name of the field to verify in the JSON object
   * asserts that fieldName is a field in the JSON object
   */
  public void assertJsonResponseField(JSONObject json, String fieldName) {
    assertTrue(json.has(fieldName), String.format("The response didn't include the %s field", fieldName));
  }

  protected abstract String getBasePathWithRouting();

  protected abstract String getBasePathDirect();
  
  protected abstract String getBlueprintServiceNameOfTargetService();

  public String getDomain() {
    return this.domain;
  }

  private static String getSystemValue(String key) {
    String val = System.getProperty(key);
    if (val == null) {
      val = System.getenv(key);
    }
    if(val != null) {
      return val.trim();
    }
    return null;
  }
  
  private String getTargetUrlFromEnv() {
    // Build the environment variable key for the system under test and use it to check the environment for a target URL

    String systemName = formatForEnvironmentKey(getSystemValue("environment"));
    String serviceName = formatForEnvironmentKey(getBlueprintServiceNameOfTargetService());

    if(serviceName == null || systemName == null) {
      return null;
    }

    String targetUrlKey = systemName + "_" + serviceName + "_URL";

    log.info(String.format("Checking environment variable '%s' for the URL of the target service", targetUrlKey));
    return getSystemValue(targetUrlKey);
  }

  private String formatForEnvironmentKey(String s) {
    if(s != null) {
      s = s.toUpperCase().replace("-", "_");
    }
    return s;
  }
  


}
