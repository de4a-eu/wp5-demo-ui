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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.servlet.response.ERedirectMode;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.model.RedirectResponseMap;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.RedirectUserType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class APIExecutorPostDERedirect implements IAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIExecutorPostDERedirect.class);

  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    LOGGER.info ("Received redirect DE4A message");

    final byte [] aPayload = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());
    LOGGER.info ("Received " + aPayload.length + " bytes");

    final DE4ACoreMarshaller <RedirectUserType> marshaller = DE4ACoreMarshaller.dtUSIRedirectUserMarshaller ();
    // DE4ACoreMarshaller<RedirectUserType> marshaller =
    // DE4ACoreMarshaller.deUSIRedirectUserMarshaller();

    final RedirectUserType redirectUserType = marshaller.read (aPayload);
    LOGGER.info ("Unmarshalled redirect message URL: " + redirectUserType.getRedirectUrl ());

    if (redirectUserType == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to parse USI redirect response");
      aUnifiedResponse.setStatus (CHttp.HTTP_BAD_REQUEST).disableCaching ();
    }
    else
    {

      LOGGER.debug ("using UnifiedResponse for redirection: " + redirectUserType.getRedirectUrl ());

     // store message
  	  LOGGER.debug ("storing redirection message");
      RedirectResponseMap.getInstance ().register (redirectUserType);
      
      aUnifiedResponse.disableCaching ()
                      .setRedirect (redirectUserType.getRedirectUrl (), ERedirectMode.POST_REDIRECT_GET);

    }

    aUnifiedResponse.disableCaching ();
    aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT);

    LOGGER.info ("Finished handling redirect DE4A message");
  }

}
