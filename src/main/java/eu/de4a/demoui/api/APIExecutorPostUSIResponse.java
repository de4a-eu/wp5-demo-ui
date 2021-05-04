package eu.de4a.demoui.api;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.iem.jaxb.common.types.RequestForwardEvidenceType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.EDE4ACanonicalEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Take USI response
 *
 * @author Philip Helger
 */
public class APIExecutorPostUSIResponse implements IAPIExecutor
{
  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, "Received USI response");

    // Read all source bytes from request
    final byte [] aPayloadBytes = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());

    final RequestForwardEvidenceType aRequest = DE4AMarshaller.deUsiRequestMarshaller (EDE4ACanonicalEvidenceType.T41_UC1_2021_04_13)
                                                              .read (aPayloadBytes);
    if (aRequest == null)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to parse USI response");
      aUnifiedResponse.setStatus (CHttp.HTTP_BAD_REQUEST).disableCaching ();
    }
    else
    {
      // TODO store message
      DE4AKafkaClient.send (EErrorLevel.INFO, "Received USI response");
      aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT).disableCaching ();
    }
  }
}
