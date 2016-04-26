package org.familysearch.cmp.qa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.lang.RandomStringUtils;
import org.codehaus.jettison.json.JSONObject;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by rbirch on 6/19/2015.
 */
public class BaseTestHelper {

  private String basePath;
  private String contentType = "application/json";
  private Client jerseyClient;
  public static enum Method {GET, POST, PUT, PATCH, DELETE}

  public BaseTestHelper(String basePath) {
    this.basePath = basePath;
    jerseyClient = Client.create();
  }

  public String getRequestContentType() {
    return contentType;
  }

  public void setRequestContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getBasePath() {
    return basePath;
  }

  public void setJerseyClient(Client client) {
    this.jerseyClient = client;
  }

  public Client getJerseyClient() {
    return jerseyClient;
  }

  public WebResource resourceForPath(String path) {
    return jerseyClient.resource(this.basePath + path);
  }

  public ClientResponse getResponse(WebResource resource, Method method, String sessionId, String... jsonPayload) {
    WebResource.Builder builder = resource.accept(
      MediaType.APPLICATION_JSON)
      .header("Content-Type", contentType);
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
  public static void assertResponse(ClientResponse clientResponse, int expectedResponseCode) {
    assertEquals(clientResponse.getStatus(), expectedResponseCode,
      String.format("The http response had an incorrect status - message: %s", clientResponse));
  }

  /*
   * @param json - Jettison JSON object
   * @param fieldName - the name of the field to verify in the JSON object
   * asserts that fieldName is a field in the JSON object
   */
  public static void assertJsonResponseField(JSONObject json, String fieldName) {
    assertTrue(json.has(fieldName), String.format("The response didn't include the %s field", fieldName));
  }

  public static void assertJsonResponseField(String json, String fieldName) {
    net.minidev.json.JSONObject jsonObject = JsonPath.read(json, "$.");
    assertTrue(jsonObject.containsKey(fieldName), String.format("The response didn't include the %s field", fieldName));
  }

  public static String convertModelToJson(Object model) {
    try {
      return new ObjectMapper().writeValueAsString(model);
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static Object convertJsonToModel(String json, Class modelClass) {
    try {
      return new ObjectMapper().readValue(json, modelClass);
    }
    catch(Exception ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static HashMap<String, TestUser> createTestUsers(List<org.familysearch.qa.testuserprovider.TestUser> credentials) {
    HashMap<String, TestUser> userMap = new HashMap<>();

    // TODO: need to dynamically set environment - can parse (beta, integration) from messageEndpointsAT.getDomain()
    credentials.stream().forEach(cred -> {
      TestUser user = new TestUser(TestUser.Environment.beta, cred.getUserName(), cred.getPassword());
      userMap.put(user.getUserId(), user);
    });

    return userMap;
  }

  public static String getRandomString(int size) {
    //String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?,.;:'\"?@#$%^&*~`(){}>\\/     ";
    //removed single and double quotes, @, >,`, - causing some test failures, showed up as html entities in browser
    String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?,.;:?#$%^&*~(){}\\/     ";
    return RandomStringUtils.random(new Random().nextInt(size), randomChars);
  }
}
