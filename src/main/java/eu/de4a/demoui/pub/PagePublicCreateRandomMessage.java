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
package eu.de4a.demoui.pub;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.css.property.CCSSProperties;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.button.BootstrapSubmitButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.photon.uictrls.prism.EPrismLanguage;
import com.helger.photon.uictrls.prism.HCPrismJS;
import com.helger.photon.uictrls.prism.PrismPluginCopyToClipboard;
import com.helger.photon.uictrls.prism.PrismPluginLineNumbers;

import eu.de4a.demoui.ui.AbstractAppWebPage;

/**
 * Create a random message and show it.
 *
 * @author Philip Helger
 */
public final class PagePublicCreateRandomMessage extends AbstractAppWebPage
{
  private static final String FIELD_MODE = "mode";

  public PagePublicCreateRandomMessage (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Create Random Message");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();

    final FormErrorList aFormErrors = new FormErrorList ();
    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      final String sMode = aWPEC.params ().getAsStringTrimmed (FIELD_MODE);
      final EDemoDocument eMode = EDemoDocument.getFromIDOrNull (sMode);

      if (eMode == null)
        aFormErrors.addFieldError (FIELD_MODE, "A valid test interface must be selected.");

      if (aFormErrors.isEmpty ())
      {
        final String sExampleDocument = eMode.getDemoMessageAsString ();

        final HCPrismJS aPrism = new HCPrismJS (EPrismLanguage.MARKUP).addChild (sExampleDocument)
                                                                      .addPlugin (new PrismPluginLineNumbers ())
                                                                      .addPlugin (new PrismPluginCopyToClipboard ());

        aNodeList.addChild (div ().addStyle (CCSSProperties.MAX_WIDTH.newValue ("75vw")).addChild (aPrism));
      }
    }

    {
      final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC)).setLeft (2);

      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_MODE));
        for (final EDemoDocument e : EDemoDocument.values ())
          aSelect.addOption (e.getID (), e.getDisplayName ());
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Interface to test")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_MODE)));
      }

      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Create example message"));
    }
  }
}
