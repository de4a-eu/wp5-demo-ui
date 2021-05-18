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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.URLHelper;
import com.helger.html.hc.html.forms.HCCheckBox;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.script.HCScriptInline;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.js.EJSEvent;
import com.helger.html.jscode.JSAnonymousFunction;
import com.helger.html.jscode.JSArray;
import com.helger.html.jscode.JSAssocArray;
import com.helger.html.jscode.JSBlock;
import com.helger.html.jscode.JSFunction;
import com.helger.html.jscode.JSPackage;
import com.helger.html.jscode.JSReturn;
import com.helger.html.jscode.JSVar;
import com.helger.html.jscode.html.JSHtml;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.button.EBootstrapButtonType;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonGroup;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.bootstrap4.uictrls.datetimepicker.BootstrapDateTimePicker;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.core.form.RequestFieldBoolean;
import com.helger.photon.icon.fontawesome.EFontAwesome5Icon;
import com.helger.photon.uicore.html.formlabel.ELabelType;
import com.helger.photon.uicore.html.formlabel.HCFormLabel;
import com.helger.photon.uicore.html.select.HCCountrySelect;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.IDE4ACanonicalEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_IM_User extends AbstractPageDE_User
{
  private static final EPatternType OUR_PATTERN = EPatternType.IM;
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_User.class);

  public PagePublicDE_IM_User (@Nonnull @Nonempty final String sID)
  {
    super (sID, "IM Exchange (User)");
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final SessionState aState = SessionState.getInstance ();

    final String sDir = aWPEC.params ().getAsStringTrimmed (PARAM_DIRECTION);
    final boolean bGoBack = "back".equals (sDir);
    final boolean bGoNext = !bGoBack && "next".equals (sDir);

    if ("reset".equals (sDir))
      aState.reset ();

    final Function <ErrorList, GenericJAXBMarshaller <RequestTransferEvidenceUSIIMDRType>> aMP;
    aMP = aEL -> DE4AMarshaller.drImRequestMarshaller ()
                               .setFormattedOutput (true)
                               .setValidationEventHandlerFactory (aEL == null ? null
                                                                              : x -> new WrappedCollectingValidationEventHandler (aEL));

    // Grab input parameters
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bIsSubmitted = bGoBack || bGoNext;
    if (bGoNext)
      switch (aState.step ())
      {
        case SELECT_PROCESS:
        {
          final String sProcessID = aWPEC.params ().getAsStringTrimmed (FIELD_PROCESS, aState.getProcessID ());
          final EUseCase eProcess = EUseCase.getFromIDOrNull (sProcessID);

          if (StringHelper.hasNoText (sProcessID))
            aFormErrors.addFieldError (FIELD_PROCESS, "Select a process");
          else
            if (eProcess == null)
              aFormErrors.addFieldError (FIELD_PROCESS, "Select valid a process");

          if (aFormErrors.isEmpty ())
          {
            aState.m_eUseCase = eProcess;
          }
          break;
        }
        case SELECT_DATA_EVALUATOR:
        {
          final String sDEID = aWPEC.params ().getAsStringTrimmed (FIELD_DE_ID, aState.getDataEvaluatorID ());
          final String sDEName = aWPEC.params ().getAsStringTrimmed (FIELD_DE_NAME, aState.getDataEvaluatorName ());
          final String sDECC = aWPEC.params ().getAsStringTrimmed (FIELD_DE_COUNTRY_CODE, aState.getDataEvaluatorCountryCode ());

          if (StringHelper.hasNoText (sDEID))
            aFormErrors.addFieldError (FIELD_DE_ID, "A Data Evaluator ID is needed");

          if (StringHelper.hasNoText (sDEName))
            aFormErrors.addFieldError (FIELD_DE_NAME, "A Data Evaluator name is needed");

          if (StringHelper.hasNoText (sDECC))
            aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "A Data Evaluator country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern ("[A-Z]{2}", sDECC))
              aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "The Data Evaluator country code is invalid");

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDE = Agent.builder ().id (sDEID).name (sDEName).countryCode (sDECC).build ();
          }
          break;
        }
        case SELECT_DATA_OWNER:
        {
          final String sDOID = aWPEC.params ().getAsStringTrimmed (FIELD_DO_ID, aState.getDataOwnerID ());
          final String sDOName = aWPEC.params ().getAsStringTrimmed (FIELD_DO_NAME, aState.getDataOwnerName ());
          final String sDOCC = aWPEC.params ().getAsStringTrimmed (FIELD_DO_COUNTRY_CODE, aState.getDataOwnerCountryCode ());

          if (StringHelper.hasNoText (sDOID))
            aFormErrors.addFieldError (FIELD_DO_ID, "A Data Owner ID is needed");

          if (StringHelper.hasNoText (sDOName))
            aFormErrors.addFieldError (FIELD_DO_NAME, "A Data Owner name is needed");

          if (StringHelper.hasNoText (sDOCC))
            aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "A Data Owner country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern ("[A-Z]{2}", sDOCC))
              aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "The Data Owner country code is invalid");

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDO = Agent.builder ().id (sDOID).name (sDOName).countryCode (sDOCC).build ();
          }
          break;
        }
        case SELECT_DATA_REQUEST_SUBJECT:
        {
          switch (aState.m_eUseCase.getDRSType ())
          {
            case PERSON:
            {
              final String sDRSID = aWPEC.params ()
                                         .getAsStringTrimmed (FIELD_DRS_ID,
                                                              aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getID () : null);
              final String sDRSFirstName = aWPEC.params ()
                                                .getAsStringTrimmed (FIELD_DRS_FIRSTNAME,
                                                                     aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFirstName ()
                                                                                                 : null);
              final String sDRSFamilyName = aWPEC.params ()
                                                 .getAsStringTrimmed (FIELD_DRS_FAMILYNAME,
                                                                      aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFamilyName ()
                                                                                                  : null);
              LocalDate aDRSBirthday = aWPEC.params ().getAsLocalDate (FIELD_DRS_BIRTHDAY, aDisplayLocale);
              if (aDRSBirthday == null)
                aDRSBirthday = aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getBirthday () : null;

              if (StringHelper.hasNoText (sDRSID))
                aFormErrors.addFieldError (FIELD_DRS_ID, "A person ID must be provided");
              if (StringHelper.hasNoText (sDRSFirstName))
                aFormErrors.addFieldError (FIELD_DRS_FIRSTNAME, "A person first name must be provided");
              if (StringHelper.hasNoText (sDRSFamilyName))
                aFormErrors.addFieldError (FIELD_DRS_FAMILYNAME, "A person family name must be provided");
              if (aDRSBirthday == null)
                aFormErrors.addFieldError (FIELD_DRS_BIRTHDAY, "A person birthday name must be provided");

              aState.resetDRS ();
              if (aFormErrors.isEmpty ())
              {
                aState.m_aDRSPerson = MDSPerson.builder ()
                                               .id (sDRSID)
                                               .firstName (sDRSFirstName)
                                               .familyName (sDRSFamilyName)
                                               .birthday (aDRSBirthday)
                                               .build ();
              }
              break;
            }
            case COMPANY:
            {
              final String sDRSID = aWPEC.params ()
                                         .getAsStringTrimmed (FIELD_DRS_ID,
                                                              aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getID () : null);
              final String sDRSName = aWPEC.params ()
                                           .getAsStringTrimmed (FIELD_DRS_NAME,
                                                                aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getName () : null);

              if (StringHelper.hasNoText (sDRSID))
                aFormErrors.addFieldError (FIELD_DRS_ID, "A company ID must be provided");
              if (StringHelper.hasNoText (sDRSName))
                aFormErrors.addFieldError (FIELD_DRS_NAME, "A company name must be provided");

              aState.resetDRS ();
              if (aFormErrors.isEmpty ())
              {
                aState.m_aDRSCompany = MDSCompany.builder ().id (sDRSID).name (sDRSName).build ();
              }
              break;
            }
            default:
              throw new IllegalStateException ();
          }
          break;
        }
        case EXPLICIT_CONSENT:
        {
          final String sTargetURL = aWPEC.params ().getAsStringTrimmed (FIELD_TARGET_URL);
          final boolean bConfirm = aWPEC.params ().isCheckBoxChecked (FIELD_CONFIRM, false);

          if (StringHelper.hasNoText (sTargetURL))
            aFormErrors.addFieldError (FIELD_TARGET_URL, "A target URL is required");
          else
            if (URLHelper.getAsURL (sTargetURL, false) == null)
              aFormErrors.addFieldError (FIELD_TARGET_URL, "The target URL must be valid URL");

          if (!bConfirm)
            aFormErrors.addFieldError (FIELD_CONFIRM, "Confirmation is required");

          aState.m_sTargetURL = null;
          aState.m_bConfirmedToSend = bConfirm;
          if (aFormErrors.isEmpty ())
          {
            aState.m_sTargetURL = sTargetURL;
          }

          break;
        }
        case SEND_REQUEST:
        {
          // Nothing
          break;
        }
        default:
          aNodeList.addChild (error ("Unsupported step " + aState.step ()));
      }

    // Change step now
    final boolean bMoved;
    if (bGoBack && !aState.step ().isFirst ())
    {
      LOGGER.info ("One step backwards from " + aState.step ());
      aState.moveBack ();
      bMoved = true;
    }
    else
      if (bGoNext && !aState.step ().isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        LOGGER.info ("One step forward from " + aState.step ());
        aState.moveForward ();
        bMoved = true;
      }
      else
      {
        bMoved = false;
      }

    final boolean bIsResubmitted = bIsSubmitted && !bMoved;

    // Check the requirements for the current step are fulfilled
    aState.validate ();

    final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC).ensureID ());
    aForm.setSplitting (BootstrapGridSpec.create (-1, -1, 3, 2, 2), BootstrapGridSpec.create (-1, -1, 9, 10, 10));

    if (aFormErrors.isNotEmpty ())
      aForm.addChild (getUIHandler ().createIncorrectInputBox (aWPEC));

    if (aState.step ().isGT (EStep.SELECT_PROCESS))
      aForm.addChild (h2 ("Running use case " + aState.m_eUseCase.getDisplayName ()));

    final JSFunction aJSSetDE;
    final JSFunction aJSSetDO;
    {
      final HCScriptInline aScript = new HCScriptInline ();
      final JSPackage aJS = new JSPackage ();
      aJSSetDE = aJS.function ("_setMDE");
      {
        final JSVar aJSID = aJSSetDE.param ("id");
        final JSVar aElementID = aJSSetDE.param ("eid");
        final JSVar aElementName = aJSSetDE.param ("en");
        final JSVar aElementCC = aJSSetDE.param ("ecc");
        final JSArray aMDE = new JSArray ();
        for (final EMockDataEvaluator e : EMockDataEvaluator.values ())
          aMDE.add (new JSAssocArray ().add ("id", e.getID ()).add ("n", e.getDisplayName ()).add ("cc", e.getCountryCode ()));
        final JSVar aArray = aJSSetDE.body ().var ("array", aMDE);
        final JSVar aCallbackParam = new JSVar ("x");
        final JSVar aFound = aJSSetDE.body ()
                                     .var ("f",
                                           aArray.invoke ("find")
                                                 .arg (new JSAnonymousFunction (aCallbackParam,
                                                                                new JSReturn (aJSID.eq (aCallbackParam.ref ("id"))))));
        final JSBlock aIfFound = aJSSetDE.body ()._if (aFound)._then ();
        aIfFound.add (JQuery.idRef (aElementID).val (aFound.component ("id")));
        aIfFound.add (JQuery.idRef (aElementName).val (aFound.component ("n")));
        aIfFound.add (JQuery.idRef (aElementCC).val (aFound.component ("cc")));
      }
      aJSSetDO = aJS.function ("_setMDO");
      {
        final JSVar aJSID = aJSSetDO.param ("id");
        final JSVar aElementID = aJSSetDO.param ("eid");
        final JSVar aElementName = aJSSetDO.param ("en");
        final JSVar aElementCC = aJSSetDO.param ("ecc");
        final JSArray aMDO = new JSArray ();
        for (final EMockDataOwner e : EMockDataOwner.values ())
          aMDO.add (new JSAssocArray ().add ("id", e.getID ()).add ("n", e.getDisplayName ()).add ("cc", e.getCountryCode ()));
        final JSVar aArray = aJSSetDO.body ().var ("array", aMDO);
        final JSVar aCallbackParam = new JSVar ("x");
        final JSVar aFound = aJSSetDO.body ()
                                     .var ("f",
                                           aArray.invoke ("find")
                                                 .arg (new JSAnonymousFunction (aCallbackParam,
                                                                                new JSReturn (aJSID.eq (aCallbackParam.ref ("id"))))));
        final JSBlock aIfFound = aJSSetDO.body ()._if (aFound)._then ();
        aIfFound.add (JQuery.idRef (aElementID).val (aFound.component ("id")));
        aIfFound.add (JQuery.idRef (aElementName).val (aFound.component ("n")));
        aIfFound.add (JQuery.idRef (aElementCC).val (aFound.component ("cc")));
      }
      aScript.setJSCodeProvider (aJS);

      aForm.addChild (aScript);
    }

    // Handle current step
    switch (aState.step ())
    {
      case SELECT_PROCESS:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_PROCESS, aState.getProcessID ()));
        for (final EUseCase e : CollectionHelper.getSorted (EUseCase.values (), IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.getPatternType () == OUR_PATTERN)
            aSelect.addOption (e.getID (), e.getDisplayName ());
        if (aSelect.getOptionCount () > 1)
          aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Use Case")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PROCESS)));
        break;
      }
      case SELECT_DATA_EVALUATOR:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField ("mockde", aState.getDataEvaluatorID ()));
        for (final EMockDataEvaluator e : CollectionHelper.getSorted (EMockDataEvaluator.values (),
                                                                      IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eUseCase))
            aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Evaluator to be used").setCtrl (aSelect));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DE_ID, aState.getDataEvaluatorID ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_ID)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DE_NAME, aState.getDataEvaluatorName ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_NAME)));

        // Country
        final HCCountrySelect aCSelect = new HCCountrySelect (new RequestField (FIELD_DE_COUNTRY_CODE,
                                                                                aState.getDataEvaluatorCountryCode ()),
                                                              aDisplayLocale);
        aCSelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator country")
                                                     .setCtrl (aCSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_COUNTRY_CODE)));

        // JS
        final JSPackage aJSOnChange = new JSPackage ();
        aJSOnChange.add (aJSSetDE.invoke ()
                                 .arg (JSHtml.getSelectSelectedValue ())
                                 .arg (aEditID.getID ())
                                 .arg (aEditName.getID ())
                                 .arg (aCSelect.getID ()));
        aSelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        break;
      }
      case SELECT_DATA_OWNER:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField ("mockdo", aState.getDataOwnerID ()));
        for (final EMockDataOwner e : CollectionHelper.getSorted (EMockDataOwner.values (),
                                                                  IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eUseCase) && !e.getID ().equals (aState.getDataEvaluatorID ()))
            aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Owner to be used").setCtrl (aSelect));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DO_ID, aState.getDataOwnerID ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_ID)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DO_NAME, aState.getDataOwnerName ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_NAME)));

        // Country
        final HCCountrySelect aCSelect = new HCCountrySelect (new RequestField (FIELD_DO_COUNTRY_CODE, aState.getDataOwnerCountryCode ()),
                                                              aDisplayLocale);
        aCSelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner country")
                                                     .setCtrl (aCSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_COUNTRY_CODE)));

        // JS
        final JSPackage aJSOnChange = new JSPackage ();
        aJSOnChange.add (aJSSetDO.invoke ()
                                 .arg (JSHtml.getSelectSelectedValue ())
                                 .arg (aEditID.getID ())
                                 .arg (aEditName.getID ())
                                 .arg (aCSelect.getID ()));
        aSelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        break;
      }
      case SELECT_DATA_REQUEST_SUBJECT:
      {
        switch (aState.m_eUseCase.getDRSType ())
        {
          case PERSON:
          {
            aForm.addChild (info ("The selected use case " +
                                  aState.m_eUseCase.getDisplayName () +
                                  " requires a person as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromIDOrNull (aState.getDataOwnerID ());

            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getID ()
                                                                                                                                                                        : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                     .getID ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person First Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FIRSTNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFirstName ()
                                                                                                                                                                        : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                     .getFirstName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FIRSTNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Family Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FAMILYNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFamilyName ()
                                                                                                                                                                        : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                     .getFamilyName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FAMILYNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Birthday")
                                                         .setCtrl (BootstrapDateTimePicker.create (FIELD_DRS_BIRTHDAY,
                                                                                                   bIsResubmitted ? null
                                                                                                                  : aState.getBirthDayOr (eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                   .getBirthday ()
                                                                                                                                                          : null),
                                                                                                   aDisplayLocale))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_BIRTHDAY)));
            break;
          }
          case COMPANY:
          {
            aForm.addChild (info ("The selected use case " +
                                  aState.m_eUseCase.getDisplayName () +
                                  " requires a company as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromIDOrNull (aState.getDataOwnerID ());

            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getID ()
                                                                                                                                                                         : null,
                                                                                                                                            aState.getDataOwnerCountryCode () +
                                                                                                                                                                                 "/" +
                                                                                                                                                                                 aState.getDataEvaluatorCountryCode () +
                                                                                                                                                                                 "/" +
                                                                                                                                                                                 (eMockDO != null ? eMockDO.getMDSCompany ()
                                                                                                                                                                                                           .getID ()
                                                                                                                                                                                                  : "")))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_NAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getName ()
                                                                                                                                                                         : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSCompany ()
                                                                                                                                                                     .getName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_NAME)));
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        break;
      }
      case EXPLICIT_CONSENT:
      {
        // Create request
        final RequestTransferEvidenceUSIIMDRType aRequest = aState.buildRequest ();

        // Check against XSD
        final ErrorList aErrorList = new ErrorList ();
        final byte [] aRequestBytes = aMP.apply (aErrorList).getAsBytes (aRequest);
        if (aRequestBytes == null)
        {
          aState.m_aRequest = null;
          for (final IError a : aErrorList)
            aFormErrors.add (SingleError.builder (a).errorFieldName (FIELD_REQUEST_XML).build ());
        }
        else
        {
          aState.m_aRequest = aRequest;
        }

        // First column for all nested tables
        final HCCol aCol1 = new HCCol (150);

        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type").setCtrl (aState.m_eUseCase.getDisplayName ()));

        {
          final Locale aDECountry = CountryCache.getInstance ().getCountry (aState.getDataEvaluatorCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataEvaluatorName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataEvaluatorID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDECountry != null ? aDECountry.getDisplayCountry (aDisplayLocale) : aState.getDataEvaluatorCountryCode ());
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator").setCtrl (t));
        }

        {
          final Locale aDOCountry = CountryCache.getInstance ().getCountry (aState.getDataOwnerCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataOwnerName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataOwnerID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDOCountry != null ? aDOCountry.getDisplayCountry (aDisplayLocale) : aState.getDataOwnerCountryCode ());
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner").setCtrl (t));
        }

        switch (aState.m_eUseCase.getDRSType ())
        {
          case PERSON:
          {
            final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
            t.addBodyRow ().addCell (strong ("Person ID:")).addCell (aState.m_aDRSPerson.getID ());
            t.addBodyRow ().addCell (strong ("First Name:")).addCell (aState.m_aDRSPerson.getFirstName ());
            t.addBodyRow ().addCell (strong ("Family Name:")).addCell (aState.m_aDRSPerson.getFamilyName ());
            t.addBodyRow ()
             .addCell (strong ("Birthday:"))
             .addCell (PDTToString.getAsString (aState.m_aDRSPerson.getBirthday (), aDisplayLocale));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          case COMPANY:
          {
            final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
            t.addBodyRow ().addCell (strong ("Company ID:")).addCell (aState.m_aDRSCompany.getID ());
            t.addBodyRow ().addCell (strong ("Company Name:")).addCell (aState.m_aDRSCompany.getName ());
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Created XML")
                                                     .setCtrl (aState.m_aRequest == null ? error ("Failed to create Request Object")
                                                                                         : new HCTextArea (new RequestField (FIELD_REQUEST_XML,
                                                                                                                             aMP.apply (null)
                                                                                                                                .getAsString (aState.m_aRequest))).setRows (10)
                                                                                                                                                                  .setReadOnly (true)
                                                                                                                                                                  .addClass (CBootstrapCSS.FORM_CONTROL)
                                                                                                                                                                  .addClass (CBootstrapCSS.TEXT_MONOSPACE))
                                                     .setHelpText ("This is the technical request. It is just shown for helping developers")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_REQUEST_XML)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Target URL")
                                                     .setCtrl (new HCEdit (new RequestField (FIELD_TARGET_URL, TARGET_URL_TEST_DR)))
                                                     .setHelpText (span ("The URL to send the request to. Use ").addChild (code (TARGET_URL_MOCK_DO))
                                                                                                                .addChild (" for the mock DO, or ")
                                                                                                                .addChild (code (TARGET_URL_TEST_DR))
                                                                                                                .addChild (" for the test DE4A Connector"))
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_TARGET_URL)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel (new HCFormLabel (span ("Confirmation to send request"),
                                                                                 ELabelType.MANDATORY))
                                                     .setCtrl (new HCCheckBox (new RequestFieldBoolean (FIELD_CONFIRM, false)))
                                                     .setHelpText ("You need to give your explicit consent here to proceed")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_CONFIRM)));

        break;
      }
      case SEND_REQUEST:
      {
        aForm.addChild (info ("Sending the request to ").addChild (code (aState.m_sTargetURL)));

        DE4AKafkaClient.send (EErrorLevel.INFO,
                              "DemoUI sending IM request '" + aState.m_aRequest.getRequestId () + "' to '" + aState.m_sTargetURL + "'");

        final StopWatch aSW = StopWatch.createdStarted ();
        final HttpClientSettings aHCS = new HttpClientSettings ();
        aHCS.setConnectionRequestTimeoutMS (120_000);
        aHCS.setSocketTimeoutMS (120_000);
        try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
        {
          final HttpPost aPost = new HttpPost (aState.m_sTargetURL);
          aPost.setEntity (new ByteArrayEntity (aMP.apply (null).getAsBytes (aState.m_aRequest),
                                                ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
          // Main POST
          final byte [] aResponse = aHCM.execute (aPost, new ResponseHandlerByteArray ());

          DE4AKafkaClient.send (EErrorLevel.INFO, "Response content received (" + aResponse.length + " bytes)");
          LOGGER.info ("Received (in UTF-8): " + new String (aResponse, StandardCharsets.UTF_8));

          final ResponseTransferEvidenceType aResponseObj = DE4AMarshaller.drImResponseMarshaller (IDE4ACanonicalEvidenceType.NONE)
                                                                          .read (aResponse);
          if (aResponseObj == null)
            throw new IOException ("Failed to parse response XML - see log for details");

          aState.m_aResponse = aResponseObj;

          if (aResponseObj.getErrorList () == null)
          {
            aForm.addChild (h2 ("Preview of the response data"));
            aForm.addChild (_createPreview (aWPEC, aResponseObj));

            if (false)
            {
              final BootstrapButtonGroup aDiv = aForm.addAndReturnChild (new BootstrapButtonGroup ());
              aDiv.addChild (new BootstrapButton (EBootstrapButtonType.SUCCESS).addChild ("Accept data")
                                                                               .setIcon (EDefaultIcon.YES)
                                                                               .setOnClick (JSHtml.windowAlert ("Okay, you accepted")));
              aDiv.addChild (new BootstrapButton (EBootstrapButtonType.OUTLINE_DANGER).addChild ("Reject data")
                                                                                      .setIcon (EDefaultIcon.NO)
                                                                                      .setOnClick (JSHtml.windowAlert ("Okay, you rejected")));
            }
          }
          else
          {
            final HCUL aUL = new HCUL ();
            aResponseObj.getErrorList ().getError ().forEach (x -> aUL.addItem ("[" + x.getCode () + "] " + x.getText ()));
            aForm.addChild (error (div ("The data could not be fetched from the Data Owner")).addChild (aUL));
          }
        }
        catch (final IOException ex)
        {
          aForm.addChild (error ().addChild (div ("Error sending request to ").addChild (code (aState.m_sTargetURL)))
                                  .addChild (AppCommonUI.getTechnicalDetailsUI (ex, true)));
        }
        finally
        {
          aSW.stop ();
          aForm.addChild (info ("It took " + aSW.getMillis () + " milliseconds to get the result"));
        }
        // TODO
        break;
      }
      default:
        aForm.addChild (error ("Unsupported step " + aState.step ()));
    }

    // Buttons
    {
      final HCDiv aRow = aForm.addAndReturnChild (div ());

      {
        if (aState.step ().isFirst () || aState.step ().wasRequestSent ())
        {
          // Disable and no-action
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setDisabled (true));
        }
        else
        {
          final JSPackage aFunc = new JSPackage ();
          aFunc.add (JQuery.idRef (aForm).append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='back'></input>").submit ());
          aFunc._return (false);
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
        }
      }
      if (aState.step ().isLast ())
      {
        aRow.addChild (new BootstrapButton ().addChild ("Next").setIcon (EDefaultIcon.NEXT).setDisabled (true));
      }
      else
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm).append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='next'></input>").submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.step ().isNextSendRequest () ? "Send Request" : "Next")
                                             .setIcon (aState.step ().isNextSendRequest () ? EDefaultIcon.YES : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }
      if (aState.step ().wasRequestSent ())
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm).append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='reset'></input>").submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild ("Restart").setIcon (EFontAwesome5Icon.UNDO).setOnClick (aFunc));
      }
    }
  }
}
