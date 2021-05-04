package eu.de4a.demoui.api;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.photon.api.APIDescriptor;
import com.helger.photon.api.APIPath;
import com.helger.photon.api.IAPIExceptionMapper;
import com.helger.photon.api.IAPIRegistry;

/**
 * API registrar
 *
 * @author Philip Helger
 */
@Immutable
public final class DemoUIAPI
{
  private DemoUIAPI ()
  {}

  public static void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    final IAPIExceptionMapper aExceptionMapper = new APIExceptionMapper ();

    // POST /response
    {
      final APIDescriptor aDescriptor = new APIDescriptor (APIPath.post ("/response"),
                                                           new APIExecutorPostUSIResponse ());
      aDescriptor.setExceptionMapper (aExceptionMapper);
      aAPIRegistry.registerAPI (aDescriptor);
    }

    // GET /response
    {
      final APIDescriptor aDescriptor = new APIDescriptor (APIPath.get ("/response"), new APIExecutorGetUSIResponse ());
      aAPIRegistry.registerAPI (aDescriptor);
    }
  }
}
