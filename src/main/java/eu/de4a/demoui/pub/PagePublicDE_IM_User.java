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

import java.time.LocalDate;
import java.time.Month;
import java.util.EnumSet;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCCheckBox;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jscode.JSPackage;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.uictrls.datetimepicker.BootstrapDateTimePicker;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.core.form.RequestFieldBoolean;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.scope.singleton.AbstractSessionSingleton;

import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ExplicitRequestType;
import eu.de4a.iem.jaxb.common.types.RequestGroundsType;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;

public class PagePublicDE_IM_User extends AbstractAppWebPage
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_User.class);
  private static final String PARAM_DIRECTION = "dir";
  // Select process
  private static final String FIELD_PROCESS = "process";
  // Select DE
  private static final String FIELD_DE = "de";
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
    SELECT_DATA_REQUEST_SUBJECT,
    EXPLICIT_CONSENT,
    SEND_REQUEST;

    public boolean isFirst ()
    {
      return ordinal () == 0;
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

  private static enum EDRSType
  {
    PERSON,
    COMPANY;

    public boolean allowsRepresentative ()
    {
      return this == COMPANY;
    }
  }

  private static enum EProcessType implements IHasID <String>, IHasDisplayName
  {
    HIGHER_EDUCATION_DIPLOMA ("t41uc1",
                              "Higher Education Diploma (SA)",
                              EDRSType.PERSON,
                              "urn:de4a-eu:CanonicalEvidenceType::HigherEducationDiploma"),
    COMPANY_REGISTRATION ("t42cr",
                          "Company Registration (DBA)",
                          EDRSType.COMPANY,
                          "urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration");

    private final String m_sID;
    private final String m_sDisplayName;
    private final EDRSType m_eDRSType;
    private final String m_sCETID;

    EProcessType (@Nonnull @Nonempty final String sID,
                  @Nonnull @Nonempty final String sDisplayName,
                  @Nonnull final EDRSType eDRSType,
                  @Nonnull @Nonempty final String sCETID)
    {
      m_sID = sID;
      m_sDisplayName = sDisplayName;
      m_eDRSType = eDRSType;
      m_sCETID = sCETID;
    }

    @Nonnull
    @Nonempty
    public String getID ()
    {
      return m_sID;
    }

    @Nonnull
    @Nonempty
    public String getDisplayName ()
    {
      return m_sDisplayName;
    }

    @Nonnull
    public EDRSType getDRSType ()
    {
      return m_eDRSType;
    }

    @Nonnull
    @Nonempty
    public String getCanonicalEvidenceTypeID ()
    {
      return m_sCETID;
    }

    @Nullable
    public static EProcessType getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EProcessType.class, sID);
    }
  }

  private static enum EMockDataEvaluator implements IHasID <String>, IHasDisplayName
  {
    ES ("iso6523-actorid-upis::9999:esq6250003h",
        "(UJI) Universitat Jaume I de Castellón",
        EProcessType.HIGHER_EDUCATION_DIPLOMA),
    PT ("iso6523-actorid-upis::9999:pt990000101",
        "Portuguese IST, University of Lisbon",
        EProcessType.HIGHER_EDUCATION_DIPLOMA),
    SI1 ("iso6523-actorid-upis::9999:si000000016",
         "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
         EProcessType.HIGHER_EDUCATION_DIPLOMA),
    SI2 ("iso6523-actorid-upis::9999:si000000018",
         "(JSI) Institut Jozef Stefan",
         EProcessType.HIGHER_EDUCATION_DIPLOMA),
    AT ("iso6523-actorid-upis::9999:at000000271",
        "(BMDW) Bundesministerium Fuer Digitalisierung Und Wirtschaftsstandort",
        EProcessType.COMPANY_REGISTRATION),
    SE ("iso6523-actorid-upis::9999:se000000013",
        "(BVE) BOLAGSVERKET (Companies Registration Office)",
        EProcessType.COMPANY_REGISTRATION),
    RO ("iso6523-actorid-upis::9999:ro000000006",
        "(ORNC) Oficiul National B22 Al Registrului Comertului",
        EProcessType.COMPANY_REGISTRATION),
    NL ("iso6523-actorid-upis::9999:nl000000024",
        "(RVO) Rijksdienst voor Ondernemend Nederland (Netherlands Enterprise Agency)",
        EProcessType.COMPANY_REGISTRATION);

    private final String m_sParticipantID;
    private final String m_sDisplayName;
    private final EnumSet <EProcessType> m_aProcesses = EnumSet.noneOf (EProcessType.class);

    EMockDataEvaluator (@Nonnull @Nonempty final String sParticipantID,
                        @Nonnull @Nonempty final String sDisplayName,
                        @Nonnull @Nonempty final EProcessType... aProcesses)
    {
      m_sParticipantID = sParticipantID;
      m_sDisplayName = sDisplayName;
      for (final EProcessType e : aProcesses)
        m_aProcesses.add (e);
    }

    @Nonnull
    @Nonempty
    public String getID ()
    {
      return m_sParticipantID;
    }

    @Nonnull
    @Nonempty
    public String getDisplayName ()
    {
      return m_sDisplayName;
    }

    public boolean supports (@Nullable final EProcessType eProcType)
    {
      return eProcType != null && m_aProcesses.contains (eProcType);
    }

    @Nullable
    public static EMockDataEvaluator getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EMockDataEvaluator.class, sID);
    }
  }

  public static final class SessionState extends AbstractSessionSingleton
  {
    private EStep m_eStep = EStep.SELECT_PROCESS;
    // Process
    private EProcessType m_eProcType;
    // DE
    private EMockDataEvaluator m_eDE;
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
        // TODO
        aDO.setAgentUrn ("urn:DemoUI");
        // TODO
        aDO.setAgentName ("Demo UI Data Owner");
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
                                                                                                                      .setValidationEventHandlerFactory (x -> new WrappedCollectingValidationEventHandler (aEL));

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
          if (e.supports (aState.m_eProcType))
            aSelect.addOption (e.getID (), e.getDisplayName ());
        aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Mock Data Evaluator to be used")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE)));
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
                                                                                                                                            "AB/CD/98765"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person First Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FIRSTNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSPersonFirstName,
                                                                                                                                            "Lisa"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FIRSTNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Last Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FAMILYNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_sDRSPersonFirstName,
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
                                                                                                                                            "AB/CD/12345"))))
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

        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Created request")
                                                     .setCtrl (aState.m_aRequest == null ? error ("Failed to create Request Object")
                                                                                         : new HCTextArea (new RequestField (FIELD_REQUEST,
                                                                                                                             aMP.apply (aErrorList)
                                                                                                                                .getAsString (aState.m_aRequest))).setRows (20)
                                                                                                                                                                  .setReadOnly (true)
                                                                                                                                                                  .addClass (CBootstrapCSS.FORM_CONTROL)
                                                                                                                                                                  .addClass (CBootstrapCSS.TEXT_MONOSPACE))
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_REQUEST)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Confirmation to send request")
                                                     .setCtrl (new HCCheckBox (new RequestFieldBoolean (FIELD_CONFIRM,
                                                                                                        false)))
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_CONFIRM)));

        break;
      }
      case SEND_REQUEST:
      {
        break;
      }
      default:
        aForm.addChild (error ("Unsupported step " + aState.m_eStep));
    }

    // Buttons
    {
      final HCDiv aRow = aForm.addAndReturnChild (div ());

      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='back'></input>")
                         .submit ());
        aFunc._return (false);
        if (aState.m_eStep.isFirst ())
        {
          // Disable and no-action
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setDisabled (true));
        }
        else
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
      }
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='next'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.m_eStep.isLast () ? "Save" : "Next")
                                             .setIcon (aState.m_eStep.isLast () ? EDefaultIcon.SAVE : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }
    }
  }
}
