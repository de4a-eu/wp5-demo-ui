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
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jscode.JSPackage;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
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
  private static final String FIELD_PROCESS = "process";

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
    SELECT_DATA_OWNER,
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
        m_eStep = EStep.SELECT_PROCESS;
      else
      {
        // TODO
      }
    }

    @Nullable
    public String getProcessID ()
    {
      return m_eProcType == null ? null : m_eProcType.getID ();
    }

    public void prevStep ()
    {
      m_eStep = m_eStep.prev ();
    }

    public void nextStep ()
    {
      m_eStep = m_eStep.next ();
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
    if (bGoNext)
      switch (aState.m_eStep)
      {
        case SELECT_PROCESS:
          final String sProcessID = aWPEC.params ().getAsString (FIELD_PROCESS, aState.getProcessID ());
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
        case SELECT_DATA_OWNER:
        {
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
                // TODO
                final NaturalPersonIdentifierType aPerson = new NaturalPersonIdentifierType ();
                aPerson.setPersonIdentifier ("PID");
                aPerson.setFirstName ("FirstName");
                aPerson.setFamilyName ("FamilyName");
                aPerson.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (42));
                // Ignore the optional stuff
                aDRS.setDataSubjectPerson (aPerson);
                break;
              }
              case COMPANY:
              {
                // TODO
                final LegalPersonIdentifierType aCompany = new LegalPersonIdentifierType ();
                aCompany.setLegalPersonIdentifier ("NO/AT/12345");
                aCompany.setLegalName ("Company name");
                // Ignore the optional stuff
                aDRS.setDataSubjectCompany (aCompany);
                break;
              }
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
      aState.prevStep ();
    }
    else
      if (bGoNext && !aState.m_eStep.isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        LOGGER.info ("One step forward from " + aState.m_eStep);
        aState.nextStep ();
      }

    // Check the requirements for the current step are fulfilled
    aState.validate ();

    final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC).ensureID ());

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
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Process to use")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PROCESS)));
        break;
      }
      case SELECT_DATA_OWNER:
      {
        switch (aState.m_eProcType.getDRSType ())
        {
          case PERSON:
            aForm.addChild (info ("input person data now"));
            break;
          case COMPANY:
            aForm.addChild (info ("input company data now"));
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
