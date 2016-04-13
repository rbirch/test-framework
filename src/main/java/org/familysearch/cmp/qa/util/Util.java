package org.familysearch.cmp.qa.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

/**
 * Created by rbirch on 6/19/2015.
 */
public class Util {
  private static Random random = new Random();

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

  public static String getRandomString(int size) {
    //String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?,.;:'\"?@#$%^&*~`(){}>\\/     ";
    //removed single and double quotes, @, >,`, - causing some test failures, showed up as html entities in browser
    String randomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!?,.;:?#$%^&*~(){}\\/     ";
    return RandomStringUtils.random(random.nextInt(size), randomChars);
  }
}
