package eu.de4a.demoui.api;

/**
 * Special exception if a parameter is invalid.
 *
 * @author Philip Helger
 */
public class APIParamException extends RuntimeException
{
  public APIParamException (final String sMsg)
  {
    super (sMsg);
  }
}
