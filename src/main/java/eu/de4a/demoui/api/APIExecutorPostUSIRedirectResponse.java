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

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.url.SimpleURL;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.photon.core.execcontext.LayoutExecutionContext;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.model.RedirectResponseMap;
import eu.de4a.demoui.pub.MenuPublic;
import eu.de4a.iem.jaxb.common.types.ResponseUserRedirectionType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Take USI response
 *
 * @author Philip Helger
 */
public class APIExecutorPostUSIRedirectResponse implements IAPIExecutor
{
  public static final String PARAM_REQUEST_ID = "requestid";

  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, "Received async USI response");

    // Read all source bytes from request
    final byte [] aPayloadBytes = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());

    // Parse without a specific evidence
    final ResponseUserRedirectionType aResponse = DE4AMarshaller.deUsiRedirectResponseMarshaller ().read (aPayloadBytes);
    if (aResponse == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to parse USI redirect response");
      aUnifiedResponse.setStatus (CHttp.HTTP_BAD_REQUEST).disableCaching ();
    }
    else
    {
      // store message
      RedirectResponseMap.getInstance ().register (aResponse);

      DE4AKafkaClient.send (EErrorLevel.INFO, "Received USI redirect response for '" + aResponse.getRequestId () + "'");

      final LayoutExecutionContext aLEC = LayoutExecutionContext.createForAjaxOrAction (aRequestScope);
      aUnifiedResponse.disableCaching ()
                      .setRedirect (new SimpleURL (aRequestScope.getFullServerPath () +
                                                   aLEC.getLinkToMenuItem (MenuPublic.MENU_DE_USI_USER).getAsStringWithEncodedParameters ())
                                                                                                                                            .add (PARAM_REQUEST_ID,
                                                                                                                                                  aResponse.getRequestId ()));
    }
  }
}
