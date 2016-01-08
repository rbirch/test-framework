package org.familysearch.cmp.qa;

import org.familysearch.cmp.qa.util.TestUser;

/**
 * Created by rbirch on 1/8/2016.
 */
public interface MessagingAcceptanceTest {

  public String getApiBasePathDirect( );
  public String setApiBasePathDirect( );

  public String getApiBasePathThroughDLB( );
  public String setApiBasePathThroughDLB( );
}
