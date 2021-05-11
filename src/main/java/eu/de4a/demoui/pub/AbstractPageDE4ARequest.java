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

import java.util.EnumSet;
import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCEM;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.form.BootstrapViewForm;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;

public abstract class AbstractPageDE4ARequest extends AbstractAppWebPage
{

  protected static enum EDRSType
  {
    PERSON,
    COMPANY;
  }

  protected static enum EProcessType implements IHasID <String>, IHasDisplayName
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

  protected static enum EMockDataEvaluator implements IHasID <String>, IHasDisplayName
  {
    ES ("iso6523-actorid-upis::9999:esq6250003h",
        "(UJI) Universitat Jaume I de Castellón",
        EProcessType.HIGHER_EDUCATION_DIPLOMA,
        "ES"),
    PT ("iso6523-actorid-upis::9999:pt990000101",
        "Portuguese IST, University of Lisbon",
        EProcessType.HIGHER_EDUCATION_DIPLOMA,
        "PT"),
    SI1 ("iso6523-actorid-upis::9999:si000000016",
         "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
         EProcessType.HIGHER_EDUCATION_DIPLOMA,
         "SI"),
    SI2 ("iso6523-actorid-upis::9999:si000000018",
         "(JSI) Institut Jozef Stefan",
         EProcessType.HIGHER_EDUCATION_DIPLOMA,
         "SI"),
    AT ("iso6523-actorid-upis::9999:at000000271",
        "(BMDW) Bundesministerium Fuer Digitalisierung Und Wirtschaftsstandort",
        EProcessType.COMPANY_REGISTRATION,
        "AT"),
    SE ("iso6523-actorid-upis::9999:se000000013",
        "(BVE) BOLAGSVERKET (Companies Registration Office)",
        EProcessType.COMPANY_REGISTRATION,
        "SE"),
    RO ("iso6523-actorid-upis::9999:ro000000006",
        "(ORNC) Oficiul National B22 Al Registrului Comertului",
        EProcessType.COMPANY_REGISTRATION,
        "RO"),
    NL ("iso6523-actorid-upis::9999:nl000000024",
        "(RVO) Rijksdienst voor Ondernemend Nederland (Netherlands Enterprise Agency)",
        EProcessType.COMPANY_REGISTRATION,
        "NL");

    private final String m_sParticipantID;
    private final String m_sDisplayName;
    private final EnumSet <EProcessType> m_aProcesses = EnumSet.noneOf (EProcessType.class);
    private final String m_sCountryCode;

    EMockDataEvaluator (@Nonnull @Nonempty final String sParticipantID,
                        @Nonnull @Nonempty final String sDisplayName,
                        @Nonnull final EProcessType eProcess,
                        @Nonnull @Nonempty final String sCountryCode)
    {
      m_sParticipantID = sParticipantID;
      m_sDisplayName = sDisplayName;
      m_aProcesses.add (eProcess);
      m_sCountryCode = sCountryCode;
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

    public boolean supportsProcess (@Nullable final EProcessType eProcType)
    {
      return eProcType != null && m_aProcesses.contains (eProcType);
    }

    @Nonnull
    @Nonempty
    public String getCountryCode ()
    {
      return m_sCountryCode;
    }

    @Nullable
    public static EMockDataEvaluator getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EMockDataEvaluator.class, sID);
    }
  }

  protected static enum EMockDataOwner implements IHasID <String>, IHasDisplayName
  {
    ES ("iso6523-actorid-upis::9999:ess2833002e",
        "(MPTFP-SGAD) Secretaría General de Administración Digital",
        EProcessType.HIGHER_EDUCATION_DIPLOMA,
        "ES",
        "53377873W"),
    PT ("iso6523-actorid-upis::9999:pt990000101",
        "Portuguese IST, University of Lisbon",
        EProcessType.HIGHER_EDUCATION_DIPLOMA,
        "PT",
        "123456"),
    SI ("iso6523-actorid-upis::9999:si000000016",
        "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
        EProcessType.HIGHER_EDUCATION_DIPLOMA,
        "SI",
        "123456789"),
    AT ("iso6523-actorid-upis::9999:at000000271",
        "(BMDW) Bundesministerium Fuer Digitalisierung Und Wirtschaftsstandort",
        EProcessType.COMPANY_REGISTRATION,
        "AT",
        "???"),
    SE ("iso6523-actorid-upis::9999:se000000013",
        "(BVE) BOLAGSVERKET (Companies Registration Office)",
        EProcessType.COMPANY_REGISTRATION,
        "SE",
        "5591674170"),
    RO ("iso6523-actorid-upis::9999:ro000000006",
        "(ORNC) Oficiul National B22 Al Registrului Comertului",
        EProcessType.COMPANY_REGISTRATION,
        "RO",
        "J40/12487/1998"),
    NL ("iso6523-actorid-upis::9999:nl990000106",
        "(KVK) Chamber of Commerce of Netherlands",
        EProcessType.COMPANY_REGISTRATION,
        "NL",
        "90000471");

    private final String m_sParticipantID;
    private final String m_sDisplayName;
    private final EnumSet <EProcessType> m_aProcesses = EnumSet.noneOf (EProcessType.class);
    private final String m_sCountryCode;
    private final String m_sEntityID;

    EMockDataOwner (@Nonnull @Nonempty final String sParticipantID,
                    @Nonnull @Nonempty final String sDisplayName,
                    @Nonnull final EProcessType eProcess,
                    @Nonnull @Nonempty final String sCountryCode,
                    @Nonnull @Nonempty final String sEntityID)
    {
      m_sParticipantID = sParticipantID;
      m_sDisplayName = sDisplayName;
      m_aProcesses.add (eProcess);
      m_sCountryCode = sCountryCode;
      m_sEntityID = sEntityID;
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

    public boolean supportsProcess (@Nullable final EProcessType eProcType)
    {
      return eProcType != null && m_aProcesses.contains (eProcType);
    }

    @Nonnull
    @Nonempty
    public String getCountryCode ()
    {
      return m_sCountryCode;
    }

    @Nonnull
    @Nonempty
    public String getEntityID ()
    {
      return m_sEntityID;
    }

    @Nullable
    public static EMockDataOwner getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EMockDataOwner.class, sID);
    }
  }

  public AbstractPageDE4ARequest (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sDisplayName)
  {
    super (sID, sDisplayName);
  }

  @Nonnull
  protected static IHCNode _get (@Nullable final String s)
  {
    return StringHelper.hasNoText (s) ? new HCEM ().addChild ("none") : new HCCode ().addChild (s);
  }

  @Nonnull
  protected static IHCNode _createAgent (@Nullable final AgentType aAgent)
  {
    if (aAgent == null)
      return _get (null);

    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("120"), HCCol.star ());
    aTable.addHeaderRow ().addCell ("Field").addCell ("Value");
    aTable.addBodyRow ().addCell ("URN:").addCell (_get (aAgent.getAgentUrn ()));
    aTable.addBodyRow ().addCell ("Name:").addCell (_get (aAgent.getAgentNameValue ()));
    if (StringHelper.hasText (aAgent.getRedirectURL ()))
      aTable.addBodyRow ().addCell ("Redirect URL:").addCell (HCA.createLinkedWebsite (aAgent.getRedirectURL ()));
    return aTable;
  }

  @Nonnull
  protected static IHCNode _createDRS (final DataRequestSubjectCVType aDRS)
  {
    if (aDRS == null)
      return _get (null);

    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("120"), HCCol.star ());
    aTable.addHeaderRow ().addCell ("Field").addCell ("Value");

    if (aDRS.getDataSubjectPerson () != null)
    {
      aTable.addBodyRow ().addCell ("Natural Person").addCell (_get ("todo"));
    }
    if (aDRS.getDataSubjectCompany () != null)
    {
      aTable.addBodyRow ().addCell ("Company").addCell (_get ("todo"));
    }
    if (aDRS.getDataSubjectRepresentative () != null)
    {
      aTable.addBodyRow ().addCell ("Representative").addCell (_get ("todo"));
    }
    return aTable;
  }

  @Nonnull
  protected static IHCNode _createPreview (@Nonnull final WebPageExecutionContext aWPEC,
                                           @Nonnull final ResponseTransferEvidenceType aResponseObj)
  {
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    final BootstrapViewForm aTable = new BootstrapViewForm ();
    aTable.setSplitting (BootstrapGridSpec.create (-1, -1, -1, 2, 2), BootstrapGridSpec.create (-1, -1, -1, 10, 10));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Request ID")
                                                  .setCtrl (_get (aResponseObj.getRequestId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Specification ID")
                                                  .setCtrl (_get (aResponseObj.getSpecificationId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Time stamp")
                                                  .setCtrl (_get (PDTToString.getAsString (aResponseObj.getTimeStamp (),
                                                                                           aDisplayLocale))));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Procedure ID")
                                                  .setCtrl (_get (aResponseObj.getProcedureId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator")
                                                  .setCtrl (_createAgent (aResponseObj.getDataEvaluator ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner")
                                                  .setCtrl (_createAgent (aResponseObj.getDataOwner ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject")
                                                  .setCtrl (_createDRS (aResponseObj.getDataRequestSubject ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type ID")
                                                  .setCtrl (_get (aResponseObj.getCanonicalEvidenceTypeId ())));
    if (aResponseObj.getCanonicalEvidence () != null)
    {
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence")
                                                    .setCtrl (_get ("present, but not shown yet")));
    }
    if (aResponseObj.getDomesticEvidenceList () != null &&
        aResponseObj.getDomesticEvidenceList ().getDomesticEvidenceCount () > 0)
    {
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Domestic Evidences")
                                                    .setCtrl (_get (aResponseObj.getDomesticEvidenceList ()
                                                                                .getDomesticEvidenceCount () +
                                                                    " present, but not shown yet")));
    }
    return aTable;
  }
}
