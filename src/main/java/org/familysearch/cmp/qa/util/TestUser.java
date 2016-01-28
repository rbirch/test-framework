package org.familysearch.cmp.qa.util;

import feign.*;
import feign.auth.BasicAuthRequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by rbirch on 6/19/2015.
 */

@XmlRootElement
public class TestUser {
  private static final Logger log = LoggerFactory.getLogger(TestUser.class);
  private String sessionId;
  private String userName;
  private String displayName;
  private String userId;

  public enum Environment { integration, beta, staging, production }

  public String getSessionId() { return sessionId; }
  public String getDisplayName() { return displayName; }
  public String getUserName() { return userName; }
  public String getUserId() { return userId; }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public void setDisplayName(String name) {
    this.displayName = name;
  }

  public void setUserId(String id) {
    this.userId = id;
  }

  public TestUser(Environment environment, String userName, String password) {
    String env = environment.toString();
    String basicAuthenticationUrl = "https://{env}.familysearch.org/links-gadget-service".replace("{env}", env);

    String apiKey = "WCQY-7J1Q-GKVV-7DNM-SQ5M-9Q5H-JX3H-CMJK";

    AuthenticationDto authenticationDto =
      new AuthenticationService(basicAuthenticationUrl, apiKey).login(new Credentials(userName, password));
    this.userName = userName;
    this.sessionId = authenticationDto.getSessionId();
    this.displayName = authenticationDto.getDisplayName();
    this.userId = authenticationDto.getId();
  }

  private class AuthenticationService {
    private String basicAuthenticationUrl;
    private String apiKey;

    private AuthenticationService(String basicAuthenticationUrl, String apiKey) {
      this.basicAuthenticationUrl = basicAuthenticationUrl;
      this.apiKey = apiKey;
    }

    private AuthenticationDto login(Credentials credentials) {
      AuthenticationProxyRequests authenticationProxyRequests = Feign.builder()
        .encoder(new JacksonEncoder())
        .decoder(new JacksonDecoder())
        .requestInterceptor(new RequestInterceptor() {
          @Override
          public void apply(RequestTemplate requestTemplate) {
            requestTemplate.header("Accept", "application/json");
          }
        })
        .requestInterceptor(new BasicAuthRequestInterceptor(credentials.getUserName(), credentials.getPassword()))
        .target(AuthenticationProxyRequests.class, basicAuthenticationUrl);
      return authenticationProxyRequests.login(apiKey);
    }
  }

  private interface AuthenticationProxyRequests {
    @RequestLine("GET /login?key={key}")
    AuthenticationDto login(@Param("key") String key);
  }

  @XmlRootElement
  private static class AuthenticationDto {
    private String id;
    private String displayName;
    private String sessionId;

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getDisplayName() {
      return displayName;
    }

    public void setDisplayName(String displayName) {
      this.displayName = displayName;
    }

    public String getSessionId() {
      return sessionId;
    }

    public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
    }
  }

  @XmlRootElement
  private static class Credentials {
    private String userName;
    private String password;

    private Credentials(String userName, String password) {
      this.userName = userName;
      this.password = password;
    }
    private String getUserName() {
      return userName;
    }

    private void setUsername(String userName) {
      this.userName = userName;
    }

    private String getPassword() {
      return password;
    }

    private void setPassword(String password) {
      this.password = password;
    }
  }
}
