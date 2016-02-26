package org.familysearch.cmp.qa;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jettison.json.JSONObject;
import org.familysearch.cmp.qa.util.TestUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import javax.ws.rs.core.MediaType;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by rbirch on 12/7/2015.
 */
public abstract class AbstractAcceptanceTest {
  protected enum Method { GET, POST, PUT, PATCH, DELETE }

  protected Client jerseyClient;

  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  protected static TestUser receivingUser;
  protected static TestUser sendingUser;

  protected String baseUrl;
  protected String basePath;


  @BeforeClass(alwaysRun = true)
  @Parameters({"baseUrl"})
  public void setupClient(String baseUrl) {
    this.baseUrl = baseUrl;

    if(baseUrl.contains("familysearch.org")) {
      this.basePath = getBasePathThroughDlb();
    }
    else {
      this.basePath = getBasePathDirect();
    }

    log.info("FS_SYSTEM_NAME property: " + System.getProperty("FS_SYSTEM_NAME"));
    log.info("acceptance test URL: " + this.baseUrl);

    ClientConfig config = new DefaultClientConfig();
    config.getFeatures().put(ClientConfig.FEATURE_DISABLE_XML_SECURITY, true);

    jerseyClient = Client.create(config);
  }

  protected WebResource resourceForPath(String path) {
    return jerseyClient.resource(baseUrl + basePath + path);
  }

  protected ClientResponse getResponse(WebResource resource, Method method, String sessionId, String... jsonPayload) {
    WebResource.Builder builder = resource.accept(
      MediaType.APPLICATION_JSON)
      .header("Content-Type", "application/json");
    builder.header("Authorization", "Bearer " + sessionId);

    String payload = "";
    try {
      payload = jsonPayload[0];
    }
    catch(Exception ignore) {}

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
  protected void assertResponse(ClientResponse clientResponse, int expectedResponseCode) {
    assertEquals(clientResponse.getStatus(), expectedResponseCode,
      String.format("The http response had an incorrect status - message: %s", clientResponse));
  }

  /*
   * @param json - Jettison JSON object
   * @param fieldName - the name of the field to verify in the JSON object
   * asserts that fieldName is a field in the JSON object
   */
  protected void assertJsonResponseField(JSONObject json, String fieldName) {
    assertTrue(json.has(fieldName), String.format("The response didn't include the %s field", fieldName));
  }

  protected abstract String getBasePathThroughDlb();

  protected abstract String getBasePathDirect();

}
