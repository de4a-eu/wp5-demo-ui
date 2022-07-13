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

import java.io.File;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.model.ResponseMapEvidence;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.IDE4ACanonicalEvidenceType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class APIExecutorPostDEInbound implements IAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIExecutorPostDEInbound.class);

  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Received inbound DE4A message");

    final byte [] aPayload = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Received " + aPayload.length + " bytes");

    // MARSHALLING
    final DE4ACoreMarshaller <ResponseExtractMultiEvidenceType> marshaller = DE4ACoreMarshaller.dtResponseTransferEvidenceMarshaller (IDE4ACanonicalEvidenceType.NONE);
    final ResponseExtractMultiEvidenceType response = marshaller.read (aPayload);
    if (response == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to parse Event Notification response");
      aUnifiedResponse.setStatus (CHttp.HTTP_BAD_REQUEST).disableCaching ();
    }
    else
    {
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Unmarshalled payload");

      // SAVE TO FILE
      if (false)
      {
        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Saving evidence file  " + AppConfig.getDEXmlWriteTo ());
        final File targetFile = new File (AppConfig.getDEXmlWriteTo ());
        SimpleFileIO.writeFile (targetFile, aPayload);
      }

      // SAVE INTO MAP
      LOGGER.debug ("storing evidence message");
      final ResponseMapEvidence map = ResponseMapEvidence.getInstance ();
      map.cleanMap ();
      map.register (response);

      aUnifiedResponse.disableCaching ();
      aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT);
    }

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Finished handling inbound DE4A message");
  }

}
