/**
 * Copyright (C) 2021 DE4A
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  public static final String API_USI_RESPONSE = "/usi-response";
  public static final String API_USI_REDIRECT_RESPONSE = "/usi-redirect-response";

  private DemoUIAPI ()
  {}

  public static void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    final IAPIExceptionMapper aExceptionMapper = new APIExceptionMapper ();

    // POST /usi-response
    {
      final APIDescriptor aDescriptor = new APIDescriptor (APIPath.post (API_USI_RESPONSE), new APIExecutorPostUSIResponse ());
      aDescriptor.setExceptionMapper (aExceptionMapper);
      aAPIRegistry.registerAPI (aDescriptor);
    }

    // GET /usi-response
    {
      final APIDescriptor aDescriptor = new APIDescriptor (APIPath.get (API_USI_RESPONSE), new APIExecutorGetUSIResponse ());
      aAPIRegistry.registerAPI (aDescriptor);
    }

    // POST /usi-redirect-response
    {
      final APIDescriptor aDescriptor = new APIDescriptor (APIPath.post (API_USI_REDIRECT_RESPONSE),
                                                           new APIExecutorPostUSIRedirectResponse ());
      aDescriptor.setExceptionMapper (aExceptionMapper);
      aAPIRegistry.registerAPI (aDescriptor);
    }
  }
}
