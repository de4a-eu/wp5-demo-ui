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
package eu.de4a.demoui.pub;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.IError;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.html.forms.HCTextArea;
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

import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.ui.AbstractAppWebPage;

/**
 * Take a user provided message and validate it.
 *
 * @author Philip Helger
 */
public final class PagePublicValidateMessage extends AbstractAppWebPage
{
  private static final String FIELD_MODE = "mode";
  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicValidateMessage (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Validate Message");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    final FormErrorList aFormErrors = new FormErrorList ();
    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      final String sMode = aWPEC.params ().getAsStringTrimmed (FIELD_MODE);
      final EDemoDocument eMode = EDemoDocument.getFromIDOrNull (sMode);

      final String sPayload = aWPEC.params ().getAsString (FIELD_PAYLOAD);

      if (eMode == null)
        aFormErrors.addFieldError (FIELD_MODE, "A valid test interface must be selected.");

      if (StringHelper.hasNoText (sPayload))
        aFormErrors.addFieldError (FIELD_PAYLOAD, "You need to provide the payload to be validated.");

      if (aFormErrors.isEmpty ())
      {
        // Main validation
        final IErrorList aEL = eMode.validateMessage (sPayload);

        // Show results
        if (aEL.containsNoError ())
          aNodeList.addChild (success ("The provided document is XSD compliant"));
        else
          aNodeList.addChild (error ("The provided document is not XSD compliant"));

        for (final IError e : aEL)
          if (e.getErrorLevel ().isError ())
            aNodeList.addChild (error (e.getAsString (aDisplayLocale)));
          else
            aNodeList.addChild (warn (e.getAsString (aDisplayLocale)));
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
      aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("XML message to validate")
                                                   .setCtrl (new HCTextArea (new RequestField (FIELD_PAYLOAD)).setRows (8))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_PAYLOAD)));

      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Validate message"));
    }
  }
}
