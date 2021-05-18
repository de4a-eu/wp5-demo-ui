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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.builder.IBuilder;
import com.helger.commons.datetime.PDTFactory;
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
import com.helger.html.hc.impl.HCTextNode;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.form.BootstrapViewForm;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.CApp;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;

public abstract class AbstractPageDE4ARequest extends AbstractAppWebPage
{
  protected static enum EPatternType
  {
    IM,
    USI;
  }

  protected static enum EDataRequestSubjectType
  {
    PERSON,
    COMPANY;
  }

  protected static enum EUseCase implements IHasID <String>, IHasDisplayName
  {
    HIGHER_EDUCATION_DIPLOMA ("t41uc1",
                              "Studying Abroad – Application to public higher education",
                              EPatternType.USI,
                              EDataRequestSubjectType.PERSON,
                              "urn:de4a-eu:CanonicalEvidenceType::HigherEducationDiploma"),
    COMPANY_REGISTRATION ("t42cr",
                          "Doing Business Abroad – Starting a business in another Member State",
                          EPatternType.IM,
                          EDataRequestSubjectType.COMPANY,
                          "urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration");

    private final String m_sID;
    private final String m_sDisplayName;
    private final EPatternType m_ePatternType;
    private final EDataRequestSubjectType m_eDRSType;
    private final String m_sCETID;

    EUseCase (@Nonnull @Nonempty final String sID,
              @Nonnull @Nonempty final String sDisplayName,
              @Nonnull final EPatternType ePatternType,
              @Nonnull final EDataRequestSubjectType eDRSType,
              @Nonnull @Nonempty final String sCETID)
    {
      m_sID = sID;
      m_sDisplayName = sDisplayName;
      m_ePatternType = ePatternType;
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
    public EPatternType getPatternType ()
    {
      return m_ePatternType;
    }

    @Nonnull
    public EDataRequestSubjectType getDRSType ()
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
    public static EUseCase getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EUseCase.class, sID);
    }
  }

  /**
   * Minimum Data Set for a Company
   *
   * @author Philip Helger
   */
  @Immutable
  protected static class MDSCompany
  {
    private final String m_sID;
    private final String m_sName;

    public MDSCompany (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sName)
    {
      ValueEnforcer.notEmpty (sID, "ID");
      ValueEnforcer.notEmpty (sName, "Name");
      m_sID = sID;
      m_sName = sName;
    }

    @Nonnull
    @Nonempty
    public String getID ()
    {
      return m_sID;
    }

    @Nonnull
    @Nonempty
    public String getName ()
    {
      return m_sName;
    }

    @Nonnull
    public static MDSCompany.Builder builder ()
    {
      return new MDSCompany.Builder ();
    }

    public static class Builder implements IBuilder <MDSCompany>
    {
      private String m_sID;
      private String m_sName;

      public Builder ()
      {}

      @Nonnull
      public Builder id (@Nullable final String s)
      {
        m_sID = s;
        return this;
      }

      @Nonnull
      public Builder name (@Nullable final String s)
      {
        m_sName = s;
        return this;
      }

      @Nonnull
      public MDSCompany build ()
      {
        return new MDSCompany (m_sID, m_sName);
      }
    }
  }

  /**
   * Minimum Data Set for a Person
   *
   * @author Philip Helger
   */
  @Immutable
  protected static class MDSPerson
  {
    private final String m_sID;
    private final String m_sFirstName;
    private final String m_sFamilyName;
    private final LocalDate m_aBirthday;

    public MDSPerson (@Nonnull @Nonempty final String sID,
                      @Nonnull @Nonempty final String sFirstName,
                      @Nonnull @Nonempty final String sFamilyName,
                      @Nonnull final LocalDate aBirthday)
    {
      ValueEnforcer.notEmpty (sID, "ID");
      ValueEnforcer.notEmpty (sFirstName, "FirstName");
      ValueEnforcer.notEmpty (sFamilyName, "FamilyName");
      ValueEnforcer.notNull (aBirthday, "Birthday");
      m_sID = sID;
      m_sFirstName = sFirstName;
      m_sFamilyName = sFamilyName;
      m_aBirthday = aBirthday;
    }

    @Nonnull
    @Nonempty
    public String getID ()
    {
      return m_sID;
    }

    @Nonnull
    @Nonempty
    public String getFirstName ()
    {
      return m_sFirstName;
    }

    @Nonnull
    @Nonempty
    public String getFamilyName ()
    {
      return m_sFamilyName;
    }

    @Nonnull
    public LocalDate getBirthday ()
    {
      return m_aBirthday;
    }

    @Nonnull
    public static MDSPerson.Builder builder ()
    {
      return new MDSPerson.Builder ();
    }

    public static class Builder implements IBuilder <MDSPerson>
    {
      private String m_sID;
      private String m_sFirstName;
      private String m_sFamilyName;
      private LocalDate m_aBirthday;

      public Builder ()
      {}

      @Nonnull
      public Builder id (@Nullable final String s)
      {
        m_sID = s;
        return this;
      }

      @Nonnull
      public Builder firstName (@Nullable final String s)
      {
        m_sFirstName = s;
        return this;
      }

      @Nonnull
      public Builder familyName (@Nullable final String s)
      {
        m_sFamilyName = s;
        return this;
      }

      @Nonnull
      public Builder birthday (final int y, final int m, final int d)
      {
        return birthday (PDTFactory.createLocalDate (y, Month.of (m), d));
      }

      @Nonnull
      public Builder birthday (@Nullable final LocalDate a)
      {
        m_aBirthday = a;
        return this;
      }

      @Nonnull
      public MDSPerson build ()
      {
        return new MDSPerson (m_sID, m_sFirstName, m_sFamilyName, m_aBirthday);
      }
    }
  }

  /**
   * DE/DO data
   *
   * @author Philip Helger
   */
  @Immutable
  protected static class Agent
  {
    private final String m_sID;
    private final String m_sName;
    private final String m_sCountryCode;

    public Agent (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sName, @Nonnull @Nonempty final String sCountryCode)
    {
      ValueEnforcer.notEmpty (sID, "ID");
      ValueEnforcer.notEmpty (sName, "Name");
      ValueEnforcer.notEmpty (sCountryCode, "CountryCode");
      m_sID = sID;
      m_sName = sName;
      m_sCountryCode = sCountryCode;
    }

    @Nonnull
    @Nonempty
    public String getID ()
    {
      return m_sID;
    }

    @Nonnull
    @Nonempty
    public String getName ()
    {
      return m_sName;
    }

    @Nonnull
    @Nonempty
    public String getCountryCode ()
    {
      return m_sCountryCode;
    }

    @Nonnull
    public static Agent.Builder builder ()
    {
      return new Agent.Builder ();
    }

    public static class Builder implements IBuilder <Agent>
    {
      private String m_sID;
      private String m_sName;
      private String m_sCountryCode;

      public Builder ()
      {}

      @Nonnull
      public Builder id (@Nullable final String s)
      {
        m_sID = s;
        return this;
      }

      @Nonnull
      public Builder name (@Nullable final String s)
      {
        m_sName = s;
        return this;
      }

      @Nonnull
      public Builder countryCode (@Nullable final String s)
      {
        m_sCountryCode = s;
        return this;
      }

      @Nonnull
      public Agent build ()
      {
        return new Agent (m_sID, m_sName, m_sCountryCode);
      }
    }
  }

  protected static enum EMockDataEvaluator implements IHasID <String>, IHasDisplayName
  {
    ES ("iso6523-actorid-upis::9999:esq6250003h", "(UJI) Universitat Jaume I de Castellón", EUseCase.HIGHER_EDUCATION_DIPLOMA, "ES"),
    PT ("iso6523-actorid-upis::9999:pt990000101", "Portuguese IST, University of Lisbon", EUseCase.HIGHER_EDUCATION_DIPLOMA, "PT"),
    SI1 ("iso6523-actorid-upis::9999:si000000016",
         "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
         EUseCase.HIGHER_EDUCATION_DIPLOMA,
         "SI"),
    SI2 ("iso6523-actorid-upis::9999:si000000018", "(JSI) Institut Jozef Stefan", EUseCase.HIGHER_EDUCATION_DIPLOMA, "SI"),
    AT ("iso6523-actorid-upis::9999:at000000271",
        "(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort",
        EUseCase.COMPANY_REGISTRATION,
        "AT"),
    SE ("iso6523-actorid-upis::9999:se000000013",
        "(BVE) BOLAGSVERKET (Companies Registration Office)",
        EUseCase.COMPANY_REGISTRATION,
        "SE"),
    RO ("iso6523-actorid-upis::9999:ro000000006",
        "(ORNC) Oficiul National B22 Al Registrului Comertului",
        EUseCase.COMPANY_REGISTRATION,
        "RO"),
    NL ("iso6523-actorid-upis::9999:nl000000024",
        "(RVO) Rijksdienst voor Ondernemend Nederland (Netherlands Enterprise Agency)",
        EUseCase.COMPANY_REGISTRATION,
        "NL");

    private final String m_sParticipantID;
    private final String m_sDisplayName;
    private final EnumSet <EUseCase> m_aProcesses = EnumSet.noneOf (EUseCase.class);
    private final String m_sCountryCode;

    EMockDataEvaluator (@Nonnull @Nonempty final String sParticipantID,
                        @Nonnull @Nonempty final String sDisplayName,
                        @Nonnull final EUseCase eProcess,
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

    public boolean supportsProcess (@Nullable final EUseCase eProcType)
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
        EUseCase.HIGHER_EDUCATION_DIPLOMA,
        "ES",
        MDSPerson.builder ().id ("53377873W").firstName ("Francisco José").familyName ("Aragó Monzonís").birthday (1984, 7, 24).build (),
        null),
    PT ("iso6523-actorid-upis::9999:pt990000101",
        "Portuguese IST, University of Lisbon",
        EUseCase.HIGHER_EDUCATION_DIPLOMA,
        "PT",
        MDSPerson.builder ().id ("123456789").firstName ("Alicea").familyName ("Alves").birthday (1997, 1, 1).build (),
        null),
    SI ("iso6523-actorid-upis::9999:si000000016",
        "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
        EUseCase.HIGHER_EDUCATION_DIPLOMA,
        "SI",
        MDSPerson.builder ().id ("123456").firstName ("Marjeta").familyName ("Maček").birthday (1999, 9, 16).build (),
        null),
    AT ("iso6523-actorid-upis::9999:at000000271",
        "(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort",
        EUseCase.COMPANY_REGISTRATION,
        "AT",
        null,
        MDSCompany.builder ().id ("???").name ("Carl-Markus Piswanger e.U.").build ()),
    SE ("iso6523-actorid-upis::9999:se000000013",
        "(BVE) BOLAGSVERKET (Companies Registration Office)",
        EUseCase.COMPANY_REGISTRATION,
        "SE",
        null,
        MDSCompany.builder ().id ("5591674170").name ("Företag Ett AB").build ()),
    RO ("iso6523-actorid-upis::9999:ro000000006",
        "(ORNC) Oficiul National B22 Al Registrului Comertului",
        EUseCase.COMPANY_REGISTRATION,
        "RO",
        null,
        MDSCompany.builder ().id ("J40/12487/1998").name ("Regional Tris-ice Coöperatie").build ()),
    NL ("iso6523-actorid-upis::9999:nl990000106",
        "(KVK) Chamber of Commerce of Netherlands",
        EUseCase.COMPANY_REGISTRATION,
        "NL",
        null,
        MDSCompany.builder ().id ("90000471").name ("ELVILA SA").build ());

    private final String m_sParticipantID;
    private final String m_sDisplayName;
    private final EnumSet <EUseCase> m_aProcesses = EnumSet.noneOf (EUseCase.class);
    private final String m_sCountryCode;
    private final MDSPerson m_aPerson;
    private final MDSCompany m_aCompany;

    EMockDataOwner (@Nonnull @Nonempty final String sParticipantID,
                    @Nonnull @Nonempty final String sDisplayName,
                    @Nonnull final EUseCase eProcess,
                    @Nonnull @Nonempty final String sCountryCode,
                    @Nullable final MDSPerson aPerson,
                    @Nullable final MDSCompany aCompany)
    {
      m_sParticipantID = sParticipantID;
      m_sDisplayName = sDisplayName;
      m_aProcesses.add (eProcess);
      m_sCountryCode = sCountryCode;
      // Either or must be set
      m_aPerson = aPerson;
      m_aCompany = aCompany;
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

    public boolean supportsProcess (@Nullable final EUseCase eProcType)
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
    public MDSPerson getMDSPerson ()
    {
      return m_aPerson;
    }

    @Nullable
    public MDSCompany getMDSCompany ()
    {
      return m_aCompany;
    }

    @Nullable
    public static EMockDataOwner getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EMockDataOwner.class, sID);
    }
  }

  protected static final String TARGET_URL_MOCK_DO = CApp.DEFAULT_BASE_URL + EDemoDocument.DR_IM_REQ.getRelativeURL ();
  protected static final String TARGET_URL_TEST_DR = "https://de4a-dev-connector.egovlab.eu/requestTransferEvidenceIM";

  public AbstractPageDE4ARequest (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sDisplayName)
  {
    super (sID, sDisplayName);
  }

  @Nonnull
  protected static IHCNode _code (@Nullable final String s)
  {
    return StringHelper.hasNoText (s) ? new HCEM ().addChild ("none") : new HCCode ().addChild (s);
  }

  @Nonnull
  protected static IHCNode _text (@Nullable final String s)
  {
    return StringHelper.hasNoText (s) ? new HCEM ().addChild ("none") : new HCTextNode (s);
  }

  @Nonnull
  protected static IHCNode _createAgent (@Nullable final AgentType aAgent)
  {
    if (aAgent == null)
      return _text (null);

    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("120"), HCCol.star ());
    aTable.addHeaderRow ().addCell ("Field").addCell ("Value");
    aTable.addBodyRow ().addCell ("Participant ID:").addCell (_code (aAgent.getAgentUrn ()));
    aTable.addBodyRow ().addCell ("Name:").addCell (_text (aAgent.getAgentNameValue ()));
    if (StringHelper.hasText (aAgent.getRedirectURL ()))
      aTable.addBodyRow ().addCell ("Redirect URL:").addCell (HCA.createLinkedWebsite (aAgent.getRedirectURL ()));
    return aTable;
  }

  @Nonnull
  protected static IHCNode _createLegalPerson (@Nonnull final LegalPersonIdentifierType aLP)
  {
    final BootstrapTable aTable2 = new BootstrapTable (HCCol.fromString ("170"), HCCol.star ());
    aTable2.addBodyRow ().addCell ("Legal Person Identifier:").addCell (_code (aLP.getLegalPersonIdentifier ()));
    aTable2.addBodyRow ().addCell ("Legal Name:").addCell (_text (aLP.getLegalNameValue ()));
    if (StringHelper.hasText (aLP.getLegalAddress ()))
      aTable2.addBodyRow ().addCell ("Legal Address:").addCell (_text (aLP.getLegalAddress ()));
    if (StringHelper.hasText (aLP.getVATRegistration ()))
      aTable2.addBodyRow ().addCell ("VAT Registration:").addCell (_code (aLP.getVATRegistration ()));
    if (StringHelper.hasText (aLP.getTaxReference ()))
      aTable2.addBodyRow ().addCell ("Tax Reference:").addCell (_code (aLP.getTaxReference ()));
    if (StringHelper.hasText (aLP.getD201217EUIdentifier ()))
      aTable2.addBodyRow ().addCell ("D-2012-17-EU Identifier:").addCell (_code (aLP.getD201217EUIdentifier ()));
    if (StringHelper.hasText (aLP.getLEI ()))
      aTable2.addBodyRow ().addCell ("LEI:").addCell (_code (aLP.getLEI ()));
    if (StringHelper.hasText (aLP.getEORI ()))
      aTable2.addBodyRow ().addCell ("EORI:").addCell (_code (aLP.getEORI ()));
    if (StringHelper.hasText (aLP.getSEED ()))
      aTable2.addBodyRow ().addCell ("SEED:").addCell (_code (aLP.getSEED ()));
    if (StringHelper.hasText (aLP.getSIC ()))
      aTable2.addBodyRow ().addCell ("SIC:").addCell (_code (aLP.getSIC ()));
    return aTable2;
  }

  @Nonnull
  protected static IHCNode _createNaturalPerson (@Nonnull final NaturalPersonIdentifierType aNP, @Nonnull final Locale aDisplayLocale)
  {
    final BootstrapTable aTable2 = new BootstrapTable (HCCol.fromString ("170"), HCCol.star ());
    aTable2.addBodyRow ().addCell ("Person Identifier:").addCell (_code (aNP.getPersonIdentifier ()));
    aTable2.addBodyRow ().addCell ("First Name:").addCell (_text (aNP.getFirstNameValue ()));
    aTable2.addBodyRow ().addCell ("Family Identifier:").addCell (_text (aNP.getFamilyNameValue ()));
    aTable2.addBodyRow ().addCell ("Date of Birth:").addCell (_text (PDTToString.getAsString (aNP.getDateOfBirthLocal (), aDisplayLocale)));
    if (aNP.getGender () != null)
      aTable2.addBodyRow ().addCell ("Gender:").addCell (_text (aNP.getGender ().value ()));
    if (StringHelper.hasText (aNP.getBirthNameValue ()))
      aTable2.addBodyRow ().addCell ("Birth Name:").addCell (_text (aNP.getBirthNameValue ()));
    if (StringHelper.hasText (aNP.getPlaceOfBirthValue ()))
      aTable2.addBodyRow ().addCell ("Place of Birth:").addCell (_text (aNP.getPlaceOfBirthValue ()));
    if (StringHelper.hasText (aNP.getCurrentAddress ()))
      aTable2.addBodyRow ().addCell ("Current Address:").addCell (_text (aNP.getCurrentAddress ()));
    return aTable2;
  }

  @Nonnull
  protected static IHCNode _createDRS (@Nonnull final DataRequestSubjectCVType aDRS, @Nonnull final Locale aDisplayLocale)
  {
    if (aDRS == null)
      return _text (null);

    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("120"), HCCol.star ());
    aTable.addHeaderRow ().addCell ("Field").addCell ("Value");

    if (aDRS.getDataSubjectPerson () != null)
    {
      aTable.addBodyRow ().addCell ("Natural Person").addCell (_createNaturalPerson (aDRS.getDataSubjectPerson (), aDisplayLocale));
    }
    if (aDRS.getDataSubjectCompany () != null)
    {
      aTable.addBodyRow ().addCell ("Company").addCell (_createLegalPerson (aDRS.getDataSubjectCompany ()));
    }
    if (aDRS.getDataSubjectRepresentative () != null)
    {
      aTable.addBodyRow ().addCell ("Representative").addCell (_createNaturalPerson (aDRS.getDataSubjectRepresentative (), aDisplayLocale));
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
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Request ID").setCtrl (_code (aResponseObj.getRequestId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Specification ID").setCtrl (_code (aResponseObj.getSpecificationId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Time stamp")
                                                  .setCtrl (_text (PDTToString.getAsString (aResponseObj.getTimeStamp (),
                                                                                            aDisplayLocale))));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Procedure ID").setCtrl (_code (aResponseObj.getProcedureId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator").setCtrl (_createAgent (aResponseObj.getDataEvaluator ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner").setCtrl (_createAgent (aResponseObj.getDataOwner ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject")
                                                  .setCtrl (_createDRS (aResponseObj.getDataRequestSubject (), aDisplayLocale)));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type ID")
                                                  .setCtrl (_code (aResponseObj.getCanonicalEvidenceTypeId ())));
    if (aResponseObj.getCanonicalEvidence () != null)
    {
      // TODO
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence").setCtrl (_text ("present, but not shown yet")));
    }
    if (aResponseObj.getDomesticEvidenceList () != null && aResponseObj.getDomesticEvidenceList ().getDomesticEvidenceCount () > 0)
    {
      // TODO
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Domestic Evidences")
                                                    .setCtrl (_text (aResponseObj.getDomesticEvidenceList ().getDomesticEvidenceCount () +
                                                                     " present, but not shown yet")));
    }
    return aTable;
  }
}
