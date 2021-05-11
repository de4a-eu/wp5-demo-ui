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
import java.time.Month;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCCheckBox;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jscode.JSPackage;
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
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.scope.singleton.AbstractSessionSingleton;

import eu.de4a.demoui.CApp;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ExplicitRequestType;
import eu.de4a.iem.jaxb.common.types.RequestGroundsType;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.IDE4ACanonicalEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_IM_User extends AbstractPageDE4ARequest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_User.class);
  private static final String PARAM_DIRECTION = "dir";
  // Select process
  private static final String FIELD_PROCESS = "process";
  // Select DE
  private static final String FIELD_DE = "de";
  // Select DO
  private static final String FIELD_DO = "do";
  // Select DRS
  private static final String FIELD_DRS_ID = "id";
  private static final String FIELD_DRS_NAME = "name";
  private static final String FIELD_DRS_FIRSTNAME = "firstname";
  private static final String FIELD_DRS_FAMILYNAME = "familyname";
  private static final String FIELD_DRS_BIRTHDAY = "birthday";
  // Request
  private static final String FIELD_REQUEST = "request";
  private static final String FIELD_CONFIRM = "confirm";

  private static enum EStep
  {
    // Order matters
    SELECT_PROCESS,
    SELECT_DATA_EVALUATOR,
    SELECT_DATA_OWNER,
    SELECT_DATA_REQUEST_SUBJECT,
    EXPLICIT_CONSENT,
    SEND_REQUEST;

    public boolean isFirst ()
    {
      return ordinal () == 0;
    }

    public boolean isSecondLast ()
    {
      return ordinal () == values ().length - 2;
    }

    public boolean isLast ()
    {
      return ordinal () == values ().length - 1;
    }

    @Nullable
    public EStep prev ()
    {
      if (isFirst ())
        return null;
      return values ()[ordinal () - 1];
    }

    @Nullable
    public EStep next ()
    {
      if (isLast ())
        return null;
      return values ()[ordinal () + 1];
    }

    @Nonnull
    public static EStep min (@Nonnull final EStep e1, @Nonnull final EStep e2)
    {
      return e1.ordinal () < e2.ordinal () ? e1 : e2;
    }
  }

  public static final class SessionState extends AbstractSessionSingleton
  {
    private EStep m_eStep = EStep.SELECT_PROCESS;
    // Process
    private EProcessType m_eProcType;
    // DE
    private EMockDataEvaluator m_eDE;
    // DO
    private EMockDataOwner m_eDO;
    // DRS
    private String m_sDRSCompanyID;
    private String m_sDRSCompanyName;
    private String m_sDRSPersonID;
    private String m_sDRSPersonFirstName;
    private String m_sDRSPersonFamilyName;
    private LocalDate m_aDRSPersonBirthday;
    // Consent to send this
    public RequestTransferEvidenceUSIIMDRType m_aRequest;
    public boolean m_bConfirmedToSend;

    @Deprecated
    @UsedViaReflection
    public SessionState ()
    {}

    @Nonnull
    public static SessionState getInstance ()
    {
      return getSessionSingleton (SessionState.class);
    }

    public void validate ()
    {
      if (m_eStep == null)
        throw new IllegalStateException ("No step");

      if (m_eProcType == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_PROCESS);
      if (m_eDE == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_EVALUATOR);
      if (m_eDO == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_OWNER);
      if (_allDRSNull ())
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_REQUEST_SUBJECT);
      if (m_aRequest == null || !m_bConfirmedToSend)
        m_eStep = EStep.min (m_eStep, EStep.EXPLICIT_CONSENT);
    }

    public void moveBack ()
    {
      m_eStep = m_eStep.prev ();
    }

    public void moveForward ()
    {
      m_eStep = m_eStep.next ();
    }

    @Nullable
    public String getProcessID ()
    {
      return m_eProcType == null ? null : m_eProcType.getID ();
    }

    @Nullable
    public String getDataEvaluatorID ()
    {
      return m_eDE == null ? null : m_eDE.getID ();
    }

    @Nullable
    public String getDataOwnerID ()
    {
      return m_eDO == null ? null : m_eDO.getID ();
    }

    private boolean _allDRSNull ()
    {
      return m_sDRSCompanyID == null &&
             m_sDRSCompanyName == null &&
             m_sDRSPersonID == null &&
             m_sDRSPersonFirstName == null &&
             m_sDRSPersonFamilyName == null &&
             m_aDRSPersonBirthday == null;
    }

    public void resetDRS ()
    {
      m_sDRSCompanyID = null;
      m_sDRSCompanyName = null;
      m_sDRSPersonID = null;
      m_sDRSPersonFirstName = null;
      m_sDRSPersonFamilyName = null;
      m_aDRSPersonBirthday = null;
    }

    @Nullable
    public LocalDate getBirthDayOr (@Nullable final LocalDate aFallbackDate)
    {
      return m_aDRSPersonBirthday != null ? m_aDRSPersonBirthday : aFallbackDate;
    }

    @Nonnull
    public RequestTransferEvidenceUSIIMDRType buildRequest ()
    {
      final RequestTransferEvidenceUSIIMDRType aRequest = new RequestTransferEvidenceUSIIMDRType ();
      aRequest.setRequestId (UUID.randomUUID ().toString ());
      // TODO
      aRequest.setSpecificationId ("SpecificationId");
      aRequest.setTimeStamp (PDTFactory.getCurrentXMLOffsetDateTimeMillisOnly ());
      // TODO
      aRequest.setProcedureId ("ProcedureId");
      {
        final AgentType aDE = new AgentType ();
        aDE.setAgentUrn (m_eDE.getID ());
        aDE.setAgentName (m_eDE.getDisplayName ());
        aRequest.setDataEvaluator (aDE);
      }
      {
        final AgentType aDO = new AgentType ();
        aDO.setAgentUrn (m_eDO.getID ());
        aDO.setAgentName (m_eDO.getDisplayName ());
        aRequest.setDataOwner (aDO);
      }
      {
        final DataRequestSubjectCVType aDRS = new DataRequestSubjectCVType ();
        switch (m_eProcType.getDRSType ())
        {
          case PERSON:
          {
            final NaturalPersonIdentifierType aPerson = new NaturalPersonIdentifierType ();
            aPerson.setPersonIdentifier (m_sDRSPersonID);
            aPerson.setFirstName (m_sDRSPersonFirstName);
            aPerson.setFamilyName (m_sDRSPersonFamilyName);
            aPerson.setDateOfBirth (m_aDRSPersonBirthday);
            // Ignore the optional stuff
            aDRS.setDataSubjectPerson (aPerson);
            break;
          }
          case COMPANY:
          {
            final LegalPersonIdentifierType aCompany = new LegalPersonIdentifierType ();
            aCompany.setLegalPersonIdentifier (m_sDRSCompanyID);
            aCompany.setLegalName (m_sDRSCompanyName);
            // Ignore the optional stuff
            aDRS.setDataSubjectCompany (aCompany);
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        aRequest.setDataRequestSubject (aDRS);
      }
      {
        final RequestGroundsType aRG = new RequestGroundsType ();
        // TODO okay for now?
        aRG.setExplicitRequest (ExplicitRequestType.SDGR_14);
        aRequest.setRequestGrounds (aRG);
      }
      aRequest.setCanonicalEvidenceTypeId (m_eProcType.getCanonicalEvidenceTypeID ());
      return aRequest;
    }
  }

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

    final boolean bGoBack = aWPEC.params ().hasStringValue (PARAM_DIRECTION, "back");
    final boolean bGoNext = !bGoBack && aWPEC.params ().hasStringValue (PARAM_DIRECTION, "next");

    final Function <ErrorList, GenericJAXBMarshaller <RequestTransferEvidenceUSIIMDRType>> aMP = aEL -> DE4AMarshaller.drImRequestMarshaller ()
                                                                                                                      .setFormattedOutput (true)
                                                                                                                      .setValidationEventHandlerFactory (aEL == null ? null
                                                                                                                                                                     : x -> new WrappedCollectingValidationEventHandler (aEL));

    // Grab input parameters
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bIsSubmitted = bGoBack || bGoNext;
    if (bGoNext)
      switch (aState.m_eStep)
      {
        case SELECT_PROCESS:
        {
          final String sProcessID = aWPEC.params ().getAsStringTrimmed (FIELD_PROCESS, aState.getProcessID ());
          final EProcessType eProcess = EProcessType.getFromIDOrNull (sProcessID);

          if (StringHelper.hasNoText (sProcessID))
            aFormErrors.addFieldError (FIELD_PROCESS, "Select a process");
          else
            if (eProcess == null)
              aFormErrors.addFieldError (FIELD_PROCESS, "Select valid a process");

          if (aFormErrors.isEmpty ())
          {
            aState.m_eProcType = eProcess;
          }
          break;
        }
        case SELECT_DATA_EVALUATOR:
        {
          final String sDEID = aWPEC.params ().getAsStringTrimmed (FIELD_DE, aState.getDataEvaluatorID ());
          final EMockDataEvaluator eDE = EMockDataEvaluator.getFromIDOrNull (sDEID);

          if (StringHelper.hasNoText (sDEID))
            aFormErrors.addFieldError (FIELD_DE, "Select a Mock Data Evaluator");
          else
            if (eDE == null)
              aFormErrors.addFieldError (FIELD_DE, "Select valid a Mock Data Evaluator");

          if (aFormErrors.isEmpty ())
          {
            aState.m_eDE = eDE;
          }
          break;
        }
        case SELECT_DATA_OWNER:
        {
          final String sDOID = aWPEC.params ().getAsStringTrimmed (FIELD_DO, aState.getDataOwnerID ());
          final EMockDataOwner eDO = EMockDataOwner.getFromIDOrNull (sDOID);

          if (StringHelper.hasNoText (sDOID))
            aFormErrors.addFieldError (FIELD_DO, "Select a Mock Data Owner");
          else
            if (eDO == null)
              aFormErrors.addFieldError (FIELD_DO, "Select valid a Mock Data Owner");

          if (aFormErrors.isEmpty ())
          {
            aState.m_eDO = eDO;
          }
          break;
        }
        case SELECT_DATA_REQUEST_SUBJECT:
        {
          switch (aState.m_eProcType.getDRSType ())
          {
            case PERSON:
            {
              final String sDRSID = aWPEC.params ().getAsStringTrimmed (FIELD_DRS_ID, aState.m_sDRSPersonID);
              final String sDRSFirstName = aWPEC.params ()
                                                .getAsStringTrimmed (FIELD_DRS_FIRSTNAME, aState.m_sDRSPersonFirstName);
              final String sDRSFamilyName = aWPEC.params ()
                                                 .getAsStringTrimmed (FIELD_DRS_FAMILYNAME,
                                                                      aState.m_sDRSPersonFamilyName);
              LocalDate aDRSBirthday = aWPEC.params ().getAsLocalDate (FIELD_DRS_BIRTHDAY, aDisplayLocale);
              if (aDRSBirthday == null)
                aDRSBirthday = aState.m_aDRSPersonBirthday;

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
                aState.m_sDRSPersonID = sDRSID;
                aState.m_sDRSPersonFirstName = sDRSFirstName;
                aState.m_sDRSPersonFamilyName = sDRSFamilyName;
                aState.m_aDRSPersonBirthday = aDRSBirthday;
              }
              break;
            }
            case COMPANY:
            {
              final String sDRSID = aWPEC.params ().getAsStringTrimmed (FIELD_DRS_ID, aState.m_sDRSCompanyID);
              final String sDRSName = aWPEC.params ().getAsStringTrimmed (FIELD_DRS_NAME, aState.m_sDRSCompanyName);

              if (StringHelper.hasNoText (sDRSID))
                aFormErrors.addFieldError (FIELD_DRS_ID, "A company ID must be provided");
              if (StringHelper.hasNoText (sDRSName))
                aFormErrors.addFieldError (FIELD_DRS_NAME, "A company name must be provided");

              aState.resetDRS ();
              if (aFormErrors.isEmpty ())
              {
                aState.m_sDRSCompanyID = sDRSID;
                aState.m_sDRSCompanyName = sDRSName;
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
          final boolean bConfirm = aWPEC.params ().isCheckBoxChecked (FIELD_CONFIRM, false);
          if (!bConfirm)
            aFormErrors.addFieldError (FIELD_CONFIRM, "Confirmation is required");

          aState.m_bConfirmedToSend = bConfirm;
          break;
        }
        case SEND_REQUEST:
        {
          // Nothing
          break;
        }
        default:
          aNodeList.addChild (error ("Unsupported step " + aState.m_eStep));
      }

    // Change step now
    final boolean bMoved;
    if (bGoBack && !aState.m_eStep.isFirst ())
    {
      LOGGER.info ("One step backwards from " + aState.m_eStep);
      aState.moveBack ();
      bMoved = true;
    }
    else
      if (bGoNext && !aState.m_eStep.isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        LOGGER.info ("One step forward from " + aState.m_eStep);
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

    // Handle current step
    switch (aState.m_eStep)
    {
      case SELECT_PROCESS:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_PROCESS, aState.getProcessID ()));
        for (final EProcessType e : CollectionHelper.getSorted (EProcessType.values (),
                                                                IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Process to use")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PROCESS)));
        break;
      }
      case SELECT_DATA_EVALUATOR:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_DE, aState.getDataEvaluatorID ()));
        for (final EMockDataEvaluator e : CollectionHelper.getSorted (EMockDataEvaluator.values (),
                                                                      IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eProcType))
            aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Mock Data Evaluator to be used")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE)));
        break;
      }
      case SELECT_DATA_OWNER:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_DO, aState.getDataOwnerID ()));
        for (final EMockDataOwner e : CollectionHelper.getSorted (EMockDataOwner.values (),
                                                                  IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eProcType) && !e.getID ().equals (aState.m_eDE.getID ()))
            aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Mock Data Owner to be used")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO)));
        break;
      }
      case SELECT_DATA_REQUEST_SUBJECT:
      {
        switch (aState.m_eProcType.getDRSType ())
        {
          case PERSON:
            aForm.addChild (info ("The selected process " +
                                  aState.m_eProcType.getDisplayName () +
                                  " requires a person as Data Request Subject"));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSPersonID,
                                                                                                                                            aState.m_eDO.getEntityID ()))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person First Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FIRSTNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSPersonFirstName,
                                                                                                                                            "Lisa"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FIRSTNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Family Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FAMILYNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSPersonFamilyName,
                                                                                                                                            "Simpson"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FAMILYNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Birthday")
                                                         .setCtrl (BootstrapDateTimePicker.create (FIELD_DRS_BIRTHDAY,
                                                                                                   bIsResubmitted ? null
                                                                                                                  : aState.getBirthDayOr (PDTFactory.createLocalDate (2002,
                                                                                                                                                                      Month.FEBRUARY,
                                                                                                                                                                      20)),
                                                                                                   aDisplayLocale))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_BIRTHDAY)));
            break;
          case COMPANY:
            aForm.addChild (info ("The selected process " +
                                  aState.m_eProcType.getDisplayName () +
                                  " requires a company as Data Request Subject"));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSCompanyID,
                                                                                                                                            aState.m_eDO.getCountryCode () +
                                                                                                                                                                    "/" +
                                                                                                                                                                    aState.m_eDE.getCountryCode () +
                                                                                                                                                                    "/" +
                                                                                                                                                                    aState.m_eDO.getEntityID ()))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_NAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSCompanyName,
                                                                                                                                            "ACME Inc."))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_NAME)));
            break;
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
            aFormErrors.add (SingleError.builder (a).errorFieldName (FIELD_REQUEST).build ());
        }
        else
        {
          aState.m_aRequest = aRequest;
        }

        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type")
                                                     .setCtrl (aState.m_eProcType.getDisplayName ()));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator")
                                                     .setCtrl (span (aState.m_eDE.getDisplayName () +
                                                                     " (").addChild (code (aState.m_eDE.getID ()))
                                                                          .addChild (")")));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner")
                                                     .setCtrl (span (aState.m_eDO.getDisplayName () +
                                                                     " (").addChild (code (aState.m_eDO.getID ()))
                                                                          .addChild (")")));
        switch (aState.m_eProcType.getDRSType ())
        {
          case PERSON:
          {
            final BootstrapTable t = new BootstrapTable (HCCol.perc (25), HCCol.star ());
            t.addBodyRow ().addCell (strong ("Person ID:")).addCell (aState.m_sDRSPersonID);
            t.addBodyRow ().addCell (strong ("First Name:")).addCell (aState.m_sDRSPersonFirstName);
            t.addBodyRow ().addCell (strong ("Family Name:")).addCell (aState.m_sDRSPersonFamilyName);
            t.addBodyRow ()
             .addCell (strong ("Birthday:"))
             .addCell (PDTToString.getAsString (aState.m_aDRSPersonBirthday, aDisplayLocale));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          case COMPANY:
          {
            final BootstrapTable t = new BootstrapTable (HCCol.perc (25), HCCol.star ());
            t.addBodyRow ().addCell (strong ("Company ID:")).addCell (aState.m_sDRSCompanyID);
            t.addBodyRow ().addCell (strong ("Company Name:")).addCell (aState.m_sDRSCompanyName);
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Created XML")
                                                     .setCtrl (aState.m_aRequest == null ? error ("Failed to create Request Object")
                                                                                         : new HCTextArea (new RequestField (FIELD_REQUEST,
                                                                                                                             aMP.apply (null)
                                                                                                                                .getAsString (aState.m_aRequest))).setRows (10)
                                                                                                                                                                  .setReadOnly (true)
                                                                                                                                                                  .addClass (CBootstrapCSS.FORM_CONTROL)
                                                                                                                                                                  .addClass (CBootstrapCSS.TEXT_MONOSPACE))
                                                     .setHelpText ("This is the technical request. It is just shown for helping developers")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_REQUEST)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Confirmation to send request")
                                                     .setCtrl (new HCCheckBox (new RequestFieldBoolean (FIELD_CONFIRM,
                                                                                                        false)))
                                                     .setHelpText ("You need to give your explicit consent here to proceed")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_CONFIRM)));

        break;
      }
      case SEND_REQUEST:
      {
        final String sTargetURL = CApp.DEFAULT_BASE_URL + EDemoDocument.DR_IM_REQ.getRelativeURL ();
        aForm.addChild (info ("Sending the mock request to ").addChild (code (sTargetURL)));

        DE4AKafkaClient.send (EErrorLevel.INFO,
                              "DemoUI sending IM request '" + aState.m_aRequest.getRequestId () + "'");

        final HttpClientSettings aHCS = new HttpClientSettings ();
        try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
        {
          final HttpPost aPost = new HttpPost (sTargetURL);
          aPost.setEntity (new ByteArrayEntity (aMP.apply (null).getAsBytes (aState.m_aRequest),
                                                ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
          final byte [] aResponse = aHCM.execute (aPost, new ResponseHandlerByteArray ());
          DE4AKafkaClient.send (EErrorLevel.INFO, "Response content received (" + aResponse.length + " bytes)");
          final ResponseTransferEvidenceType aResponseObj = DE4AMarshaller.drImResponseMarshaller (IDE4ACanonicalEvidenceType.NONE)
                                                                          .read (aResponse);
          if (aResponseObj == null)
            throw new IOException ("Failed to parse response XML");

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
            aResponseObj.getErrorList ()
                        .getError ()
                        .forEach (x -> aUL.addItem ("[" + x.getCode () + "] " + x.getText ()));
          }
        }
        catch (final IOException ex)
        {
          aForm.addChild (error ().addChild (div ("Error sending request to ").addChild (code (sTargetURL)))
                                  .addChild (AppCommonUI.getTechnicalDetailsUI (ex, true)));
        }
        // TODO
        break;
      }
      default:
        aForm.addChild (error ("Unsupported step " + aState.m_eStep));
    }

    // Buttons
    {
      final HCDiv aRow = aForm.addAndReturnChild (div ());

      {
        if (aState.m_eStep.isFirst ())
        {
          // Disable and no-action
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setDisabled (true));
        }
        else
        {
          final JSPackage aFunc = new JSPackage ();
          aFunc.add (JQuery.idRef (aForm)
                           .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='back'></input>")
                           .submit ());
          aFunc._return (false);
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
        }
      }
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='next'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.m_eStep.isSecondLast () ? "Send Request" : "Next")
                                             .setIcon (aState.m_eStep.isSecondLast () ? EDefaultIcon.YES
                                                                                      : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }
    }
  }
}
