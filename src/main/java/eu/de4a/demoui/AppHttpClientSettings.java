package eu.de4a.demoui;

import org.apache.hc.core5.util.Timeout;

import com.helger.dcng.core.http.DcngHttpClientSettings;

public class AppHttpClientSettings extends DcngHttpClientSettings
{
  public AppHttpClientSettings ()
  {
    setConnectTimeout (Timeout.ofMinutes (2));
    setResponseTimeout (Timeout.ofMinutes (2));
  }
}
