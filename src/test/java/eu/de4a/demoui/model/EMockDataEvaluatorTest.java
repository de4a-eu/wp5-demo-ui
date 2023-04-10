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
package eu.de4a.demoui.model;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppol.sml.SMLInfo;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.security.keystore.EKeyStoreType;
import com.helger.security.keystore.KeyStoreHelper;
import com.helger.security.keystore.LoadedKeyStore;
import com.helger.smpclient.bdxr1.BDXRClientReadOnly;
import com.helger.smpclient.exception.SMPClientException;
import com.helger.smpclient.url.BDXLURLProvider;
import com.helger.smpclient.url.SMPDNSResolutionException;
import com.helger.xsds.bdxr.smp1.EndpointType;

/**
 * Test class for class {@link EMockDataEvaluator}
 *
 * @author Philip Helger
 */
public final class EMockDataEvaluatorTest
{
  static final ISMLInfo SML_DE4A = new SMLInfo ("de4a-sml",
                                                "DE4A SML",
                                                "de4a.edelivery.tech.ec.europa.eu.",
                                                "https://edelivery.tech.ec.europa.eu/edelivery-sml",
                                                true);
  static final LoadedKeyStore SMP_TRUST_STORE = KeyStoreHelper.loadKeyStore (EKeyStoreType.JKS,
                                                                             "truststore/de4a-truststore-smp-v3-pw-de4a2.jks",
                                                                             "de4a");

  private static final Logger LOGGER = LoggerFactory.getLogger (EMockDataEvaluatorTest.class);

  @Test
  @Ignore ("Participants not yet registered")
  public void testLookups () throws SMPDNSResolutionException, SMPClientException
  {
    final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();
    final IProcessIdentifier aResponseProcessID = aIF.createProcessIdentifier (DcngIdentifierFactory.PROCESS_SCHEME, "response");
    assertTrue (SMP_TRUST_STORE.isSuccess ());

    for (final EUseCase eUC : EUseCase.values ())
      for (final EMockDataEvaluator e : EMockDataEvaluator.values ())
        if (e.getPilot () == eUC.getPilot ())
        {
          LOGGER.info ("Trying " + eUC + " - " + e + " - " + e.getParticipantID ());
          final IParticipantIdentifier aRecipient = aIF.parseParticipantIdentifier (e.getParticipantID ());
          final EndpointType aEndpoint = new BDXRClientReadOnly (BDXLURLProvider.INSTANCE,
                                                                 aRecipient,
                                                                 SML_DE4A).setTrustStore (SMP_TRUST_STORE.getKeyStore ())
                                                                          .getEndpoint (aRecipient,
                                                                                        eUC.getDocumentTypeID (),
                                                                                        aResponseProcessID,
                                                                                        ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4);
          if (aEndpoint != null)
            LOGGER.info ("  found it");
          else
            LOGGER.error (" not found");
        }
  }
}
