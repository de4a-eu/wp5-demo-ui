package eu.de4a.demoui.api;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.mime.CMimeType;
import com.helger.html.hc.html.root.HCHtml;
import com.helger.html.hc.html.sections.HCH1;
import com.helger.html.hc.render.HCRenderer;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Just because we're nice
 *
 * @author Philip Helger
 */
public class APIExecutorGetUSIResponse implements IAPIExecutor
{
  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, "Somebody accessed the USI response with GET instead of POST");

    final HCHtml aHtml = new HCHtml ();
    aHtml.body ().addChild (new HCH1 ().addChild ("You need to use POST"));

    aUnifiedResponse.setContentAndCharset (HCRenderer.getAsHTMLStringWithoutNamespaces (aHtml), StandardCharsets.UTF_8)
                    .setMimeType (CMimeType.TEXT_HTML);
  }
}
