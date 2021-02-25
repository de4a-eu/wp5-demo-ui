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
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.timing.StopWatch;
import com.helger.css.property.CCSSProperties;
import com.helger.html.hc.html.forms.HCHiddenField;
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

import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.demoui.ui.AppCommonUI;

public final class PagePublicDemoClient extends AbstractAppWebPage
{
  private static final String FIELD_MODE = "mode";
  private static final ICommonsList <IPrismPlugin> PRISM_PLUGINS = new CommonsArrayList <> (new PrismPluginLineNumbers (),
                                                                                            new PrismPluginCopyToClipboard ());

  public PagePublicDemoClient (@Nonnull @Nonempty final String sID)
  {
    super (sID, "WP5 Demo Client");
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
      final EDemoMode eMode = EDemoMode.getFromIDOrNull (sMode);

      if (eMode == null)
        aFormErrors.addFieldError (FIELD_MODE, "A valid test interface must be selected.");

      if (aFormErrors.isEmpty ())
      {
        final String sFinalURL = "https://de4a-dev-mock.egovlab.eu" + eMode.getRelativeURL ();
        final String sExampleDocument = eMode.getDemoRequestString ();

        final StopWatch aSW = StopWatch.createdStarted ();
        String sResponse = null;
        Exception aResponseEx = null;
        final HttpClientSettings aHCS = new HttpClientSettings ();
        try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
        {
          final HttpPost aPost = new HttpPost (sFinalURL);
          aPost.setEntity (new StringEntity (sExampleDocument, ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
          sResponse = aHCM.execute (aPost, new ResponseHandlerString ());
        }
        catch (final IOException ex)
        {
          aResponseEx = ex;
        }
        aSW.stop ();

        final HCNodeList aResNL = new HCNodeList ();
        {
          final HCPrismJS aPrism = new HCPrismJS (EPrismLanguage.MARKUP).addChild (sExampleDocument);
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
          aResNL.addChild (success ().addChild (div ("Response content received")).addChild (div (pre (sResponse))));
        }

        aWPEC.postRedirectGetInternal (aResNL);
        bShowForm = false;
      }
    }

    if (bShowForm)
    {
      final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC)).setLeft (2);

      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_MODE));
        for (final EDemoMode e : EDemoMode.values ())
          aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Interface to test")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_MODE)));
      }

      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Send Mock request"));
    }
  }
}
