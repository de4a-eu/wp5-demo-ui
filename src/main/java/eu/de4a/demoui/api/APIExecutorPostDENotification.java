/*
 * Copyright (C) 2023, Partners of the EU funded DE4A project consortium
 *   (https://www.de4a.eu/consortium), under Grant Agreement No.870635
 * Author:
 *   Austrian Federal Computing Center (BRZ)
 *   Spanish Ministry of Economic Affairs and Digital Transformation -
 *     General Secretariat for Digital Administration (MAETD - SGAD)
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
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.model.ResponseMapEventNotification;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.EventNotificationType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class APIExecutorPostDENotification implements IAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIExecutorPostDENotification.class);

  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Received inbound DE4A Notification");

    final byte [] aPayload = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Received " + aPayload.length + " bytes");

    // MARSHALLING
    // TODO why DT and not DR?
    final DE4ACoreMarshaller <EventNotificationType> marshaller = DE4ACoreMarshaller.dtEventNotificationMarshaller ();
    final EventNotificationType response = marshaller.read (aPayload);
    if (response == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to parse EventNotificationType response");
      aUnifiedResponse.setStatus (CHttp.HTTP_BAD_REQUEST).disableCaching ();
    }
    else
    {
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Unmarshalled payload as " + response.getClass ().getSimpleName ());

      // SAVE TO FILE
      /*
       * LOGGER.info ("Saving evidence file  " + AppConfig.getDEXmlWriteTo () );
       * File targetFile = new File(AppConfig.getDEXmlWriteTo ()); OutputStream
       * outStream = new FileOutputStream(targetFile);
       * outStream.write(aPayload); IOUtils.closeQuietly(outStream);
       */
      // SAVE INTO MAP
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("storing evidence message");

      final ResponseMapEventNotification map = ResponseMapEventNotification.getInstance ();
      map.cleanMap ();
      map.register (response);

      aUnifiedResponse.disableCaching ();
      aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT);
    }

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Finished handling inbound DE4A message");
  }
}
