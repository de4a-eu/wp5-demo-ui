package eu.de4a.demoui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.helger.commons.url.SimpleURL;

public class SimpleTest
{
  @Test
  public void testURL ()
  {
    assertEquals ("a?b=c&d", new SimpleURL ("a").add ("b", "c").add ("d").getAsStringWithEncodedParameters ());
  }
}
