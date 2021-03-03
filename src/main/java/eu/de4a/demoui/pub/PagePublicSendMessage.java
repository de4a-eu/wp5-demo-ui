/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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
package eu.de4a.demoui.pub;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Document;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.IError;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.URLHelper;
import com.helger.css.property.CCSSProperties;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerString;
import com.helger.photon.app.html.PhotonCSS;
import com.helger.photon.app.html.PhotonJS;
import com.helger.photon.bootstrap4.button.BootstrapSubmitButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.photon.uictrls.EUICtrlsCSSPathProvider;
import com.helger.photon.uictrls.EUICtrlsJSPathProvider;
import com.helger.photon.uictrls.prism.EPrismLanguage;
import com.helger.photon.uictrls.prism.HCPrismJS;
import com.helger.photon.uictrls.prism.IPrismPlugin;
import com.helger.photon.uictrls.prism.PrismPluginCopyToClipboard;
import com.helger.photon.uictrls.prism.PrismPluginLineNumbers;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;

import eu.de4a.demoui.CApp;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.xml.de4a.DE4ANamespaceContext;

public final class PagePublicSendMessage extends AbstractAppWebPage
{
  private static final String FIELD_MODE = "mode";
  private static final String FIELD_DEST_BASE_URL = "destbaseurl";
  private static final String FIELD_PAYLOAD = "payload";
  private static final ICommonsList <IPrismPlugin> PRISM_PLUGINS = new CommonsArrayList <> (new PrismPluginLineNumbers (),
                                                                                            new PrismPluginCopyToClipboard ());

  public PagePublicSendMessage (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Send Message");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    // Required for PRG
    {
      for (final IPrismPlugin aPlugin : PRISM_PLUGINS)
        aPlugin.registerExternalResourcesBeforePrism ();
      PhotonCSS.registerCSSIncludeForThisRequest (EUICtrlsCSSPathProvider.PRISMJS);
      PhotonJS.registerJSIncludeForThisRequest (EUICtrlsJSPathProvider.PRISMJS);
      for (final IPrismPlugin aPlugin : PRISM_PLUGINS)
        aPlugin.registerExternalResourcesAfterPrism ();
    }

    final FormErrorList aFormErrors = new FormErrorList ();
    boolean bShowForm = true;
    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      final String sMode = aWPEC.params ().getAsStringTrimmed (FIELD_MODE);
      final EDemoDocument eMode = EDemoDocument.getFromIDOrNull (sMode);

      final String sTargetBaseURL = aWPEC.params ().getAsStringTrimmed (FIELD_DEST_BASE_URL);
      final URL aTargetBaseURL = URLHelper.getAsURL (sTargetBaseURL);

      final String sPayload = aWPEC.params ().getAsString (FIELD_PAYLOAD);

      if (eMode == null)
        aFormErrors.addFieldError (FIELD_MODE, "A valid test interface must be selected.");

      if (StringHelper.hasNoText (sTargetBaseURL))
        aFormErrors.addFieldError (FIELD_DEST_BASE_URL, "A target base URL must be provided");
      else
        if (aTargetBaseURL == null)
          aFormErrors.addFieldError (FIELD_DEST_BASE_URL, "The provided target base URL is invalid");

      if (StringHelper.hasNoText (sPayload))
        aFormErrors.addFieldError (FIELD_PAYLOAD, "You need to provide the payload to be send.");

      if (aFormErrors.isEmpty ())
      {
        final HCNodeList aResNL = new HCNodeList ();

        // Check if document is valid
        final IErrorList aEL = eMode.validateMessage (sPayload);
        if (aEL.containsAtLeastOneError ())
        {
          aResNL.addChild (error ("The provided document is not XSD compliant"));
          for (final IError e : aEL)
            if (e.getErrorLevel ().isError ())
              aResNL.addChild (error (e.getAsString (aDisplayLocale)));
            else
              aResNL.addChild (warn (e.getAsString (aDisplayLocale)));
        }
        else
        {
          // Send only valid documents
          final StopWatch aSW = StopWatch.createdStarted ();

          final String sFinalURL = sTargetBaseURL + eMode.getRelativeURL ();
          String sResponse = null;
          Exception aResponseEx = null;
          final HttpClientSettings aHCS = new HttpClientSettings ();
          try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
          {
            final HttpPost aPost = new HttpPost (sFinalURL);
            aPost.setEntity (new StringEntity (sPayload, ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
            sResponse = aHCM.execute (aPost, new ResponseHandlerString ());
          }
          catch (final IOException ex)
          {
            aResponseEx = ex;
          }
          aSW.stop ();

          {
            final HCPrismJS aPrism = new HCPrismJS (EPrismLanguage.MARKUP).addChild (sPayload);
            for (final IPrismPlugin p : PRISM_PLUGINS)
              aPrism.addPlugin (p);

            aResNL.addChild (h2 ("Sent request"))
                  .addChild (div ("Target URL: ").addChild (code (sFinalURL)))
                  .addChild (div ("Execution time: ").addChild (code (aSW.getMillis () + " milliseconds")))
                  .addChild (div ().addStyle (CCSSProperties.MAX_WIDTH.newValue ("75vw")).addChild (aPrism));
          }

          if (aResponseEx != null)
          {
            aResNL.addChild (error ().addChild (div ("Error sending Mock request to ").addChild (code (sFinalURL)))
                                     .addChild (AppCommonUI.getTechnicalDetailsUI (aResponseEx, true)));
          }

          if (sResponse != null)
          {
            final boolean isFormatted = StringHelper.getLineCount (sResponse) > 1;
            aResNL.addChild (success ().addChild (div ("Response content received (" +
                                                       sResponse.length () +
                                                       " chars)" +
                                                       (isFormatted ? "" : " - displayed re-formatted"))));
            final DOMReaderSettings aDRS = new DOMReaderSettings ();
            aDRS.exceptionCallbacks ().removeAll ();
            final Document aDoc = DOMReader.readXMLDOM (sResponse, aDRS);
            if (aDoc != null)
            {
              // Its XML

              // Reformat if necessary
              final String sFormatted = isFormatted ? sResponse
                                                    : XMLWriter.getNodeAsString (aDoc,
                                                                                 new XMLWriterSettings ().setNamespaceContext (DE4ANamespaceContext.getInstance ()));
              final HCPrismJS aPrism = new HCPrismJS (EPrismLanguage.MARKUP).addChild (sFormatted);
              for (final IPrismPlugin p : PRISM_PLUGINS)
                aPrism.addPlugin (p);

              aResNL.addChild (div ().addStyle (CCSSProperties.MAX_WIDTH.newValue ("75vw")).addChild (aPrism));
            }
            else
            {
              // Non-XML
              aResNL.addChild (div (pre (sResponse)));
            }
          }
        }
        if (true)
          aNodeList.addChild (aResNL);
        else
          aWPEC.postRedirectGetInternal (aResNL);
        bShowForm = false;
      }
    }

    if (bShowForm)
    {
      final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC)).setLeft (2);

      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_MODE));
        for (final EDemoDocument e : EDemoDocument.values ())
          if (e.getDocumentType () == EDemoDocumentType.REQUEST)
            aSelect.addOption (e.getID (), e.getDisplayName () + " (" + e.getRelativeURL () + ")");
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Interface to test")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_MODE)));
      }

      aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Target server base URL")
                                                   .setCtrl (new HCEdit (new RequestField (FIELD_DEST_BASE_URL, CApp.DEFAULT_BASE_URL)))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_DEST_BASE_URL))
                                                   .setHelpText ("The URL to which the request should be send. Use this to send a request to your server for testing purposes if you like." +
                                                                 " The suffix of the Interface to test is added to this path." +
                                                                 " The endpoint must be able to handle HTTP POST calls."));

      aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("XML message to send")
                                                   .setCtrl (new HCTextArea (new RequestField (FIELD_PAYLOAD)).setRows (8))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_PAYLOAD)));

      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Send Mock request"));
    }
  }
}
