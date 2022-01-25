package eu.de4a.demoui.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class EMockDataOwnerTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (EMockDataOwnerTest.class);

  @Test
  public void testLookups () throws SMPDNSResolutionException, SMPClientException
  {
    final IProcessIdentifier aRequestProcessID = SimpleIdentifierFactory.INSTANCE.createProcessIdentifier ("urn:de4a-eu:MessageType",
                                                                                                           "request");
    final LoadedKeyStore aLTS = KeyStoreHelper.loadKeyStore (EKeyStoreType.JKS, "truststore/de4a-truststore-test-smp-pw-de4a.jks", "de4a");
    assertTrue (aLTS.isSuccess ());

    for (final EUseCase eUC : EUseCase.values ())
      for (final EMockDataOwner e : EMockDataOwner.values ())
        if (e.getPilot () == eUC.getPilot ())
        {
          LOGGER.info ("Trying " + eUC + " - " + e + " - " + e.getParticipantID ());
          final IParticipantIdentifier aRecipient = SimpleIdentifierFactory.INSTANCE.parseParticipantIdentifier (e.getParticipantID ());
          final EndpointType aEndpoint = new BDXRClientReadOnly (BDXLURLProvider.INSTANCE,
                                                                 aRecipient,
                                                                 EMockDataEvaluatorTest.SMK_DE4A).setTrustStore (aLTS.getKeyStore ())
                                                                                                 .getEndpoint (aRecipient,
                                                                                                               eUC.getDocumentTypeID (),
                                                                                                               aRequestProcessID,
                                                                                                               ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4);
          if (aEndpoint != null)
            LOGGER.info ("  found it");
          else
            LOGGER.error (" not found");
        }
  }
}
