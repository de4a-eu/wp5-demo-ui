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
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jscode.JSPackage;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.uictrls.datetimepicker.BootstrapDateTimePicker;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.scope.singleton.AbstractSessionSingleton;

import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;

public class PagePublicDE_IM_User extends AbstractAppWebPage
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_User.class);
  private static final String PARAM_DIRECTION = "dir";
  // Select process
  private static final String FIELD_PROCESS = "process";
  // Select DRS
  private static final String FIELD_DRS_ID = "id";
  private static final String FIELD_DRS_NAME = "name";
  private static final String FIELD_DRS_FIRSTNAME = "firstname";
  private static final String FIELD_DRS_FAMILYNAME = "familyname";
  private static final String FIELD_DRS_BIRTHDAY = "birthday";

  private static enum EDRSType
  {
    PERSON,
    COMPANY;

    public boolean allowsRepresentative ()
    {
      return this == COMPANY;
    }
  }

  private static enum EStep
  {
    // Order matters
    SELECT_PROCESS,
    SELECT_DATA_REQUEST_SUBJECT,
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

  private static enum EProcessType implements IHasID <String>, IHasDisplayName
  {
    HIGHER_EDUCATION_DIPLOMA ("t41uc1", "Higher Education Diploma (SA)", EDRSType.PERSON),
    DBA ("t42", "Company Registration (DBA)", EDRSType.COMPANY);

    private final String m_sID;
    private final String m_sDisplayName;
    private final EDRSType m_eDRSType;

    EProcessType (@Nonnull @Nonempty final String sID,
                  @Nonnull @Nonempty final String sDisplayName,
                  @Nonnull final EDRSType eDRSType)
    {
      m_sID = sID;
      m_sDisplayName = sDisplayName;
      m_eDRSType = eDRSType;
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

    @Nullable
    public static EProcessType getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EProcessType.class, sID);
    }
  }

  public static final class SessionState extends AbstractSessionSingleton
  {
    private EStep m_eStep = EStep.SELECT_PROCESS;
    private EProcessType m_eProcType;
    private String m_sDRSCompanyID;
    private String m_sDRSCompanyName;
    private String m_sDRSPersonID;
    private String m_sDRSPersonFirstName;
    private String m_sDRSPersonFamilyName;
    private LocalDate m_aDRSPersonBirthday;

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

    // Grab input parameters
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bIsSubmitted = bGoNext;
    if (bGoNext)
      switch (aState.m_eStep)
      {
        case SELECT_PROCESS:
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

              if (aFormErrors.isEmpty ())
              {
                aState.resetDRS ();
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

              if (aFormErrors.isEmpty ())
              {
                aState.resetDRS ();
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
        case SEND_REQUEST:
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
            // TODO
            aDE.setAgentUrn ("urn:DemoUI");
            aDE.setAgentName ("Demo UI Data Evaluator");
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
            switch (aState.m_eProcType.getDRSType ())
            {
              case PERSON:
              {
                final NaturalPersonIdentifierType aPerson = new NaturalPersonIdentifierType ();
                aPerson.setPersonIdentifier (aState.m_sDRSPersonID);
                aPerson.setFirstName (aState.m_sDRSPersonFirstName);
                aPerson.setFamilyName (aState.m_sDRSPersonFamilyName);
                aPerson.setDateOfBirth (aState.m_aDRSPersonBirthday);
                // Ignore the optional stuff
                aDRS.setDataSubjectPerson (aPerson);
                break;
              }
              case COMPANY:
              {
                final LegalPersonIdentifierType aCompany = new LegalPersonIdentifierType ();
                aCompany.setLegalPersonIdentifier (aState.m_sDRSCompanyID);
                aCompany.setLegalName (aState.m_sDRSCompanyName);
                // Ignore the optional stuff
                aDRS.setDataSubjectCompany (aCompany);
                break;
              }
              default:
                throw new IllegalStateException ();
            }
            aRequest.setDataRequestSubject (aDRS);
          }
          break;
        }
        default:
          aNodeList.addChild (error ("Unsupported step " + aState.m_eStep));
      }

    // Change step now
    if (bGoBack && !aState.m_eStep.isFirst ())
    {
      LOGGER.info ("One step backwards from " + aState.m_eStep);
      aState.moveBack ();
    }
    else
      if (bGoNext && !aState.m_eStep.isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        LOGGER.info ("One step forward from " + aState.m_eStep);
        aState.moveForward ();
      }

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
                                                                                                 bIsSubmitted ? null
                                                                                                              : StringHelper.getNotEmpty (aState.m_sDRSPersonID,
                                                                                                                                          "AB/CD/98765"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person First Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FIRSTNAME,
                                                                                                 bIsSubmitted ? null
                                                                                                              : StringHelper.getNotEmpty (aState.m_sDRSPersonFirstName,
                                                                                                                                          "Lisa"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FIRSTNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Last Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FAMILYNAME,
                                                                                                 bIsSubmitted ? null
                                                                                                              : StringHelper.getNotEmpty (aState.m_sDRSPersonFirstName,
                                                                                                                                          "Simpson"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FAMILYNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Birthday")
                                                         .setCtrl (BootstrapDateTimePicker.create (FIELD_DRS_BIRTHDAY,
                                                                                                   bIsSubmitted ? null
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
                                                                                                 bIsSubmitted ? null
                                                                                                              : StringHelper.getNotEmpty (aState.m_sDRSCompanyID,
                                                                                                                                          "AB/CD/12345"))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_NAME,
                                                                                                 bIsSubmitted ? null
                                                                                                              : StringHelper.getNotEmpty (aState.m_sDRSCompanyName,
                                                                                                                                          "ACME Inc."))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_NAME)));
            break;
          default:
            aForm.addChild (error ("Unsupported DRS type " + aState.m_eProcType.getDRSType ()));
        }
        break;
      }
      default:
        aForm.addChild (error ("Unsupported step " + aState.m_eStep));
    }

    // Buttons
    {
      final HCDiv aRow = aForm.addAndReturnChild (div ());

      if (!aState.m_eStep.isFirst ())
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='back'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
      }
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='next'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.m_eStep.isLast () ? "Finalize" : "Save")
                                             .setIcon (aState.m_eStep.isLast () ? EDefaultIcon.SAVE : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }
    }
  }
}
