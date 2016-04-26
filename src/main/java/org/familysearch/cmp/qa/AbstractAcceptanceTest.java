package org.familysearch.cmp.qa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Created by rbirch on 12/7/2015.
 */
public abstract class AbstractAcceptanceTest {


  protected final Logger log = LoggerFactory.getLogger(this.getClass());

  private String domain;
  private String basePath;
  private String targetUrl;

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

    targetUrl = domain + basePath;

    log.info("acceptance test URL: " + targetUrl);
  }

  protected String getTargetUrl() {
    return targetUrl;
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
