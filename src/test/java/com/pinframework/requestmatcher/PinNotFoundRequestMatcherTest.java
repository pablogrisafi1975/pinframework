package com.pinframework.requestmatcher;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

@Test
public class PinNotFoundRequestMatcherTest {

  private final PinNotFoundRequestMatcher pinNotFoundRequestMatcher =
      new PinNotFoundRequestMatcher();

  @Test
  public void matchesNull() throws Exception {
    assertTrue(pinNotFoundRequestMatcher.matches(null, null, null));
  }

  @Test
  public void matchesSomething() throws Exception {
    assertTrue(pinNotFoundRequestMatcher.matches("GET", "aaaa", "application/json"));
  }

  @Test
  public void extractPathParams() throws Exception {
    assertTrue(pinNotFoundRequestMatcher.extractPathParams(null).isEmpty());
  }

}
