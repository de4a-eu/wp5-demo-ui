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
import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Element;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.builder.IBuilder;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.ext.HCA_MailTo;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.html.textlevel.HCCode;
import com.helger.html.hc.html.textlevel.HCEM;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.jaxb.validation.DoNothingValidationEventHandler;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.form.BootstrapViewForm;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.xml.serialize.write.EXMLSerializeIndent;
import com.helger.xml.serialize.write.XMLWriter;
import com.helger.xml.serialize.write.XMLWriterSettings;

import eu.de4a.demoui.CApp;
import eu.de4a.demoui.model.EDataRequestSubjectType;
import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.MDSCompany;
import eu.de4a.demoui.model.MDSPerson;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.CanonicalEvidenceType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;
import eu.de4a.iem.jaxb.t42.v0_6.ActivityType;
import eu.de4a.iem.jaxb.t42.v0_6.AddressType;
import eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType;
import eu.de4a.iem.xml.de4a.t42.v0_6.DE4AT42Marshaller;

public abstract class AbstractPageDE extends AbstractAppWebPage
{
  protected enum EUseCase implements IHasID <String>, IHasDisplayName
  {
    HIGHER_EDUCATION_DIPLOMA ("t41uc1",
                              "Studying Abroad – Application to public higher education",
                              EPatternType.USI,
                              EDataRequestSubjectType.PERSON,
                              SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                             "HigherEducationDiploma")),
    COMPANY_REGISTRATION ("t42cr",
                          "Doing Business Abroad – Starting a business in another Member State",
                          EPatternType.IM,
                          EDataRequestSubjectType.COMPANY,
                          SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                         "CompanyRegistration")),
    /*
     * BIRTH_EVIDENCE ("t43birth", "Moving Abroad – Birth Evidence",
     * EPatternType.USI, EDataRequestSubjectType.PERSON,
     * SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier
     * ("urn:de4a-eu:CanonicalEvidenceType", "BirthEvidence")), DOMREG_EVIDENCE
     * ("t43domreg", "Moving Abroad – Domicile Registration Evidence",
     * EPatternType.USI, EDataRequestSubjectType.PERSON,
     * SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier
     * ("urn:de4a-eu:CanonicalEvidenceType", "DomicileRegistrationEvidence")),
     * MARRIAGE_EVIDENCE ("t43marriage", "Moving Abroad – Marriage Evidence",
     * EPatternType.USI, EDataRequestSubjectType.PERSON,
     * SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier
     * ("urn:de4a-eu:CanonicalEvidenceType", "MarriageEvidence"))
     */;

    private final String m_sID;
    private final String m_sDisplayName;
    private final EPatternType m_ePatternType;
    private final EDataRequestSubjectType m_eDRSType;
    private final IDocumentTypeIdentifier m_aDocTypeID;

    EUseCase (@Nonnull @Nonempty final String sID,
              @Nonnull @Nonempty final String sDisplayName,
              @Nonnull final EPatternType ePatternType,
              @Nonnull final EDataRequestSubjectType eDRSType,
              @Nonnull final IDocumentTypeIdentifier aDocTypeID)
    {
      m_sID = sID;
      m_sDisplayName = sDisplayName;
      m_ePatternType = ePatternType;
      m_eDRSType = eDRSType;
      m_aDocTypeID = aDocTypeID;
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
    public IDocumentTypeIdentifier getDocumentTypeID ()
    {
      return m_aDocTypeID;
    }

    @Nullable
    public static EUseCase getFromIDOrNull (@Nullable final String sID)
    {
      return EnumHelper.getFromIDOrNull (EUseCase.class, sID);
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
    // Only for USI DO
    private final String m_sRedirectURL;

    public Agent (@Nonnull @Nonempty final String sID,
                  @Nonnull @Nonempty final String sName,
                  @Nonnull @Nonempty final String sCountryCode,
                  @Nullable final String sRedirectURL)
    {
      ValueEnforcer.notEmpty (sID, "ID");
      ValueEnforcer.notEmpty (sName, "Name");
      ValueEnforcer.notEmpty (sCountryCode, "CountryCode");
      m_sID = sID;
      m_sName = sName;
      m_sCountryCode = sCountryCode;
      m_sRedirectURL = sRedirectURL;
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

    @Nullable
    public String getRedirectURL ()
    {
      return m_sRedirectURL;
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
      private String m_sRedirectURL;

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
      public Builder redirectURL (@Nullable final String s)
      {
        m_sRedirectURL = s;
        return this;
      }

      @Nonnull
      public Agent build ()
      {
        return new Agent (m_sID, m_sName, m_sCountryCode, m_sRedirectURL);
      }
    }
  }

  protected enum EMockDataEvaluator implements IHasID <String>, IHasDisplayName
  {
    ES ("iso6523-actorid-upis::9999:esq6250003h", "(UJI) Universitat Jaume I de Castellón", EUseCase.HIGHER_EDUCATION_DIPLOMA, "ES"),
    PT ("iso6523-actorid-upis::9999:pt990000101", "Portuguese IST, University of Lisbon", EUseCase.HIGHER_EDUCATION_DIPLOMA, "PT"),
    SI1 ("iso6523-actorid-upis::9999:si000000016",
         "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
         EUseCase.HIGHER_EDUCATION_DIPLOMA,
         "SI"),
    SI2 ("iso6523-actorid-upis::9999:si000000016", "(JSI) Institut Jozef Stefan", EUseCase.HIGHER_EDUCATION_DIPLOMA, "SI"),
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

  protected enum EMockDataOwner implements IHasID <String>, IHasDisplayName
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

  protected final EPatternType m_ePattern;
  protected final String TARGET_URL_MOCK_DO;
  protected final String TARGET_URL_TEST_DR;

  public AbstractPageDE (@Nonnull @Nonempty final String sID,
                         @Nonnull @Nonempty final String sDisplayName,
                         @Nonnull final EPatternType ePattern)
  {
    super (sID, sDisplayName);
    m_ePattern = ePattern;
    if (ePattern == EPatternType.IM)
    {
      TARGET_URL_MOCK_DO = CApp.MOCK_BASE_URL + EDemoDocument.IM_REQ_DE_DR.getRelativeURL ();
      TARGET_URL_TEST_DR = CApp.CONNECTOR_BASE_URL + "/requestTransferEvidenceIM";
    }
    else
    {
      TARGET_URL_MOCK_DO = CApp.MOCK_BASE_URL + EDemoDocument.USI1_REQ_DE_DR.getRelativeURL ();
      TARGET_URL_TEST_DR = CApp.CONNECTOR_BASE_URL + "/requestTransferEvidenceUSI";
    }
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
  private static IHCNode _createDBAActivity (@Nonnull final ActivityType a)
  {
    final HCDiv aNaceCodes = new HCDiv ();
    if (a.hasNaceCodeEntries ())
      aNaceCodes.addChild ("NACE codes: ").addChild (StringHelper.imploder ().source (a.getNaceCode ()).separator (", ").build ());

    final HCDiv aActDesc = new HCDiv ();
    if (a.hasActivityDescriptionEntries ())
      aActDesc.addChild ("Activity Description: ")
              .addChild (StringHelper.imploder ().source (a.getActivityDescription ()).separator ("; ").build ());

    return new HCNodeList ().addChild (aNaceCodes).addChild (aActDesc);
  }

  @Nonnull
  private static IHCNode _createDBAAddresss (@Nonnull final AddressType a)
  {
    final HCDiv ret = new HCDiv ().addClasses (CBootstrapCSS.BORDER, CBootstrapCSS.BORDER_DARK, CBootstrapCSS.P_2);
    if (StringHelper.hasText (a.getPoBox ()))
      ret.addChild (new HCDiv ().addChild ("PO Box: " + a.getPoBox ()));
    if (StringHelper.hasText (a.getThoroughfare ()))
      ret.addChild (new HCDiv ().addChild ("Street: " + a.getThoroughfare ()));
    if (StringHelper.hasText (a.getThoroughfare ()))
      ret.addChild (new HCDiv ().addChild ("Building Number: " + a.getLocationDesignator ()));
    if (StringHelper.hasText (a.getPostCode ()))
      ret.addChild (new HCDiv ().addChild ("Post Code: " + a.getPostCode ()));
    if (StringHelper.hasText (a.getPostName ()))
      ret.addChild (new HCDiv ().addChild ("City: " + a.getPostName ()));
    if (StringHelper.hasText (a.getAdminUnitL1 ()))
      ret.addChild (new HCDiv ().addChild ("Admin Unit L1: " + a.getAdminUnitL1 ()));
    if (StringHelper.hasText (a.getAdminUnitL2 ()))
      ret.addChild (new HCDiv ().addChild ("Admin Unit L2: " + a.getAdminUnitL2 ()));
    return ret;
  }

  @Nonnull
  private static IHCNode _createCE_DBA (@Nonnull final LegalEntityType aLegalEntity, @Nonnull final Locale aDisplayLocale)
  {
    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("180"), HCCol.star ());
    if (aLegalEntity.hasCompanyNameEntries ())
    {
      aTable.addBodyRow ()
            .addCell ("Company Name(s):")
            .addCell (new CommonsArrayList <> (aLegalEntity.getCompanyName (),
                                               x -> new HCDiv ().addChild (x.getLegalEntityLegalName ().getValue ())));
    }

    if (StringHelper.hasText (aLegalEntity.getCompanyType ()))
      aTable.addBodyRow ().addCell ("Company Type:").addCell (aLegalEntity.getCompanyType ());

    if (StringHelper.hasText (aLegalEntity.getCompanyStatus ()))
      aTable.addBodyRow ().addCell ("Company Status:").addCell (aLegalEntity.getCompanyStatus ());

    if (aLegalEntity.getCompanyActivity () != null)
    {
      aTable.addBodyRow ().addCell ("Company Activity:").addCell (_createDBAActivity (aLegalEntity.getCompanyActivity ()));
    }

    if (aLegalEntity.getRegistrationDate () != null)
      aTable.addBodyRow ()
            .addCell ("Registration Date:")
            .addCell (PDTToString.getAsString (aLegalEntity.getRegistrationDate (), aDisplayLocale));

    if (StringHelper.hasText (aLegalEntity.getCompanyEUID ()))
      aTable.addBodyRow ().addCell ("Company EUID:").addCell (_code (aLegalEntity.getCompanyEUID ()));

    if (aLegalEntity.hasVatNumberEntries ())
    {
      final HCNodeList aNL = new HCNodeList ();
      for (final String s : aLegalEntity.getVatNumber ())
        aNL.addChild (new HCDiv ().addChild (_code (s)));
      aTable.addBodyRow ().addCell ("VAT Number(s):").addCell (aNL);
    }

    if (aLegalEntity.getCompanyContactData () != null)
    {
      final HCDiv aEmails = new HCDiv ();
      if (aLegalEntity.getCompanyContactData ().hasEmailEntries ())
      {
        aEmails.addChild ("E-Mail Address(es): ");
        int i = 0;
        for (final String s : aLegalEntity.getCompanyContactData ().getEmail ())
        {
          if (i++ > 0)
            aEmails.addChild (", ");
          aEmails.addChild (HCA_MailTo.createLinkedEmail (s));
        }
      }

      final HCDiv aPhones = new HCDiv ();
      if (aLegalEntity.getCompanyContactData ().hasTelephoneEntries ())
      {
        aPhones.addChild ("Telephone number(s): ")
               .addChild (StringHelper.imploder ()
                                      .source (aLegalEntity.getCompanyContactData ().getTelephone ())
                                      .separator (", ")
                                      .build ());
      }
      aTable.addBodyRow ().addCell ("Company Contact Data:").addCell (aEmails, aPhones);
    }

    if (aLegalEntity.hasRegisteredAddressEntries ())
    {
      final HCNodeList aNL = new HCNodeList ();
      for (final AddressType a : aLegalEntity.getRegisteredAddress ())
        aNL.addChild (_createDBAAddresss (a));
      aTable.addBodyRow ().addCell ("Registered Address(es):").addCell (aNL);
    }

    if (aLegalEntity.hasPostalAddressEntries ())
    {
      final HCNodeList aNL = new HCNodeList ();
      for (final AddressType a : aLegalEntity.getPostalAddress ())
        aNL.addChild (_createDBAAddresss (a));
      aTable.addBodyRow ().addCell ("Postal Address(es):").addCell (aNL);
    }

    if (aLegalEntity.getHasBranch () != null)
    {
      final HCNodeList aNL = new HCNodeList ();
      aNL.addChild (new HCDiv ().addChild ("Name: " +
                                           aLegalEntity.getHasBranch ().getBranchName ().getLegalEntityLegalName ().getValue ()));
      aNL.addChild (new HCDiv ().addChild ("EUID: ").addChild (_code (aLegalEntity.getHasBranch ().getBranchEUID ())));
      if (aLegalEntity.getHasBranch ().getBranchActivity () != null)
        aNL.addChild (new HCDiv ().addChild ("Activity: ")
                                  .addChild (_createDBAActivity (aLegalEntity.getHasBranch ().getBranchActivity ())));
      if (aLegalEntity.getHasBranch ().getBranchRegistredAddress () != null)
        aNL.addChild (new HCDiv ().addChild ("Registered Address: ")
                                  .addChild (_createDBAAddresss (aLegalEntity.getHasBranch ().getBranchRegistredAddress ())));
      if (aLegalEntity.getHasBranch ().getBranchPostalAddress () != null)
        aNL.addChild (new HCDiv ().addChild ("Postal Address: ")
                                  .addChild (_createDBAAddresss (aLegalEntity.getHasBranch ().getBranchPostalAddress ())));
      aTable.addBodyRow ().addCell ("Branch:").addCell (aNL);
    }

    return aTable;
  }

  @Nonnull
  protected static IHCNode _createCE (@Nonnull final CanonicalEvidenceType aCanonicalEvidence, @Nonnull final Locale aDisplayLocale)
  {
    final Element aElement = (Element) aCanonicalEvidence.getAny ();

    final HCNodeList ret = new HCNodeList ();

    // DBA
    final LegalEntityType aLegalEntity = DE4AT42Marshaller.legalEntity ()
                                                          .setValidationEventHandlerFactory (x -> new DoNothingValidationEventHandler ())
                                                          .read (aElement);
    if (aLegalEntity != null)
      ret.addChild (_createCE_DBA (aLegalEntity, aDisplayLocale));
    else
    {
      // TODO higher education
      ret.addChild (new BootstrapErrorBox ().addChild ("Unsupported canonical evidence using root namespace URL '" +
                                                       aElement.getNamespaceURI () +
                                                       "' and local name '" +
                                                       aElement.getLocalName () +
                                                       "'"));
    }

    // At the end to focus on the structured data
    ret.addChild (new HCDiv ().addChild ("Raw XML representation of the canonical evidence:"));
    ret.addChild (new HCTextArea ().setValue (XMLWriter.getNodeAsString (aElement,
                                                                         new XMLWriterSettings ().setIndent (EXMLSerializeIndent.INDENT_AND_ALIGN)))
                                   .setReadOnly (true)
                                   .setRows (8)
                                   .addClass (CBootstrapCSS.TEXT_MONOSPACE));

    return ret;
  }

  @Nonnull
  protected static IHCNode _createPreviewIM (@Nonnull final WebPageExecutionContext aWPEC,
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
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence")
                                                    .setCtrl (_createCE (aResponseObj.getCanonicalEvidence (), aDisplayLocale)));
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
