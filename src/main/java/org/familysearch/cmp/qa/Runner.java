package org.familysearch.cmp.qa;

import org.familysearch.cmp.qa.util.TestUser;

/**
 * Created by jorobi on 1/28/2016.
 */
public class Runner {
  public static void main(String[] args) {
    TestUser user = new TestUser(TestUser.Environment.integration, "x", "y");
  }
}
