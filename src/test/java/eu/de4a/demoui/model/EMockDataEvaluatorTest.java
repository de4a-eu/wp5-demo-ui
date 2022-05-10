package eu.de4a.demoui.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppol.sml.SMLInfo;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
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
  static final ISMLInfo SMK_DE4A = new SMLInfo ("de4a-smk",
                                                "DE4A SMK",
                                                "de4a.acc.edelivery.tech.ec.europa.eu.",
                                                "https://acc.edelivery.tech.ec.europa.eu/edelivery-sml",
                                                true);
  private static final Logger LOGGER = LoggerFactory.getLogger (EMockDataEvaluatorTest.class);

  @Test
  public void testLookups () throws SMPDNSResolutionException, SMPClientException
  {
    final IProcessIdentifier aResponseProcessID = SimpleIdentifierFactory.INSTANCE.createProcessIdentifier ("urn:de4a-eu:MessageType",
                                                                                                            "response");
    final LoadedKeyStore aLTS = KeyStoreHelper.loadKeyStore (EKeyStoreType.JKS, "truststore/de4a-truststore-test-smp-pw-de4a.jks", "de4a");
    assertTrue (aLTS.isSuccess ());

    for (final EUseCase eUC : EUseCase.values ())
      for (final EMockDataEvaluator e : EMockDataEvaluator.values ())
        if (e.getPilot () == eUC.getPilot ())
        {
          LOGGER.info ("Trying " + eUC + " - " + e + " - " + e.getParticipantID ());
          final IParticipantIdentifier aRecipient = SimpleIdentifierFactory.INSTANCE.parseParticipantIdentifier (e.getParticipantID ());
          final EndpointType aEndpoint = new BDXRClientReadOnly (BDXLURLProvider.INSTANCE,
                                                                 aRecipient,
                                                                 SMK_DE4A).setTrustStore (aLTS.getKeyStore ())
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
