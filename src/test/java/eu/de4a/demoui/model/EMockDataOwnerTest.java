package eu.de4a.demoui.model;

import static org.junit.Assert.assertTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
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
  @Ignore ("Participants not yet registered")
  public void testLookups () throws SMPDNSResolutionException, SMPClientException
  {
    final IProcessIdentifier aRequestProcessID = DcngIdentifierFactory.INSTANCE.createProcessIdentifier ("urn:de4a-eu:MessageType",
                                                                                                         "request");
    assertTrue (EMockDataEvaluatorTest.SMP_TRUST_STORE.isSuccess ());

    for (final EUseCase eUC : EUseCase.values ())
      for (final EMockDataOwner e : EMockDataOwner.values ())
        if (e.getPilot () == eUC.getPilot ())
        {
          LOGGER.info ("Trying " + eUC + " - " + e + " - " + e.getParticipantID ());
          final IParticipantIdentifier aRecipient = DcngIdentifierFactory.INSTANCE.parseParticipantIdentifier (e.getParticipantID ());
          final EndpointType aEndpoint = new BDXRClientReadOnly (BDXLURLProvider.INSTANCE,
                                                                 aRecipient,
                                                                 EMockDataEvaluatorTest.SML_DE4A).setTrustStore (EMockDataEvaluatorTest.SMP_TRUST_STORE.getKeyStore ())
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
