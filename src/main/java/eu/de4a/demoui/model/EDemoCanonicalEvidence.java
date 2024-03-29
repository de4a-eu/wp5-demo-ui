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
package eu.de4a.demoui.model;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.xml.datatype.XMLGregorianCalendar;

import org.w3c.dom.Element;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.math.MathHelper;

import eu.de4a.iem.cev.EDE4ACanonicalEvidenceType;
import eu.de4a.iem.core.xml.XSDDataTypeHelper;
import eu.de4a.iem.jaxb.t41.disability.v2022_05_12.DisabilityCertificateType;
import eu.de4a.iem.jaxb.t41.edci.IscedFOetCodeType;
import eu.de4a.iem.jaxb.t41.edci.LanguageCharCodeEnumType;
import eu.de4a.iem.jaxb.t41.edci.LanguageStringType;
import eu.de4a.iem.jaxb.t41.edci.LegalIdentifierType;
import eu.de4a.iem.jaxb.t41.edci.LocationType;
import eu.de4a.iem.jaxb.t41.edci.PersonType;
import eu.de4a.iem.jaxb.t41.edci.TextContentTypeCodeEnumType;
import eu.de4a.iem.jaxb.t41.largefamily.v2022_05_12.LargeFamilyCertificateType;
import eu.de4a.iem.jaxb.t43.birth.v1_7.BirthType;
import eu.de4a.iem.jaxb.t43.birth.v1_7.ChildType;
import eu.de4a.iem.jaxb.t43.codelists.country.ISO3166CountryType;
import eu.de4a.iem.jaxb.t43.codelists.humansex.GenderType;
import eu.de4a.iem.jaxb.t43.codelists.nuts2021.NUTS2021Type;
import eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriageType;
import eu.de4a.iem.jaxb.w3.cv10.ac.CvaddressType;
import eu.de4a.iem.jaxb.w3.cv10.ac.CvidentifierType;
import eu.de4a.iem.jaxb.w3.cv10.bc.GivenNameType;
import eu.de4a.iem.jaxb.w3.cv10.bc.IdentifierType;
import eu.de4a.iem.jaxb.w3.cv10.bc.PostCodeType;
import eu.de4a.iem.jaxb.w3.cv11.bc.LegalEntityLegalNameType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.DateType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.FamilyNameType;

/**
 * Demo canonical evidence type
 *
 * @author Philip Helger
 */
public enum EDemoCanonicalEvidence
{
  T41_HIGHER_EDUCATION_DIPLOMA_2022_06_23 (EDE4ACanonicalEvidenceType.T41_HIGHER_EDUCATION_EVIDENCE_2022_06_23,
                                           EDemoCanonicalEvidence::createSA_HigherEducation_v2021_04_13),
  T41_SECONDARY_EDUCATION_DIPLOMA_2022_05_12 (EDE4ACanonicalEvidenceType.T41_SECONDARY_EDUCATION_EVIDENCE_2022_05_12,
                                              EDemoCanonicalEvidence::createSA_SecondaryEducation_v2022_05_12),
  T41_DISABILITY_2022_05_12 (EDE4ACanonicalEvidenceType.T41_DISABILITY_EVIDENCE_2022_05_12,
                             EDemoCanonicalEvidence::createSA_Disability_v2022_05_12),
  T41_LARGE_FAMILY_2022_05_12 (EDE4ACanonicalEvidenceType.T41_LARGE_FAMILY_EVIDENCE_2022_05_12,
                               EDemoCanonicalEvidence::createSA_LargeFamily_v2022_05_12),

  T42_LEGAL_ENTITY_V06 (EDE4ACanonicalEvidenceType.T42_LEGAL_ENTITY_V06,
                        EDemoCanonicalEvidence::createDBA_LegalEntity_v06),

  T43_BIRTH_V17 (EDE4ACanonicalEvidenceType.T43_BIRTH_EVIDENCE_V17, EDemoCanonicalEvidence::createMA_Birth_v1_7),
  T43_DOMDEREG_V10 (EDE4ACanonicalEvidenceType.T43_DOMDEREG_EVIDENCE_V10,
                    EDemoCanonicalEvidence::createMA_DomesticDeregistration_v1_0),
  T43_DOMREG_V17 (EDE4ACanonicalEvidenceType.T43_DOMREG_EVIDENCE_V17,
                  EDemoCanonicalEvidence::createMA_DomesticRegistration_v1_7),
  T43_MARRIAGE_V17 (EDE4ACanonicalEvidenceType.T43_MARRIAGE_EVIDENCE_V17,
                    EDemoCanonicalEvidence::createMA_Marriage_v1_7);

  private final EDE4ACanonicalEvidenceType m_eCEType;
  private final Supplier <Element> m_aAnyCreator;

  EDemoCanonicalEvidence (@Nonnull final EDE4ACanonicalEvidenceType eCEType, final Supplier <Element> aAnyCreator)
  {
    m_eCEType = eCEType;
    m_aAnyCreator = aAnyCreator;
  }

  @Nonnull
  public EDE4ACanonicalEvidenceType getCEType ()
  {
    return m_eCEType;
  }

  /**
   * @return An example XML element for this canonical evidence type. Never
   *         <code>null</code>.
   */
  @Nonnull
  public Element createExampleElement ()
  {
    return m_aAnyCreator.get ();
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t41.edci.TextType _createT41Text ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.edci.TextType ret = new eu.de4a.iem.jaxb.t41.edci.TextType ();
    ret.setValue ("Text-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setContentType (EDemoDocument.random (TextContentTypeCodeEnumType.class));
    ret.setLang (EDemoDocument.random (LanguageCharCodeEnumType.class));
    return ret;
  }

  @Nonnull
  private static PersonType _createT41Person ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final PersonType ret = new PersonType ();
    ret.setId ("id-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final LegalIdentifierType b = new LegalIdentifierType ();
      b.setValue ("NationalID-" + MathHelper.abs (aTLR.nextInt ()));
      b.setSpatialID ("SpatialID-" + MathHelper.abs (aTLR.nextInt ()));
      ret.setNationalId (b);
    }
    {
      final LanguageStringType b = new LanguageStringType ();
      b.setText (_createT41Text ());
      ret.setGivenNames (b);
    }
    {
      final LanguageStringType b = new LanguageStringType ();
      b.setText (_createT41Text ());
      ret.setFamilyName (b);
    }
    ret.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (18 + aTLR.nextLong (80)));
    return ret;
  }

  @Nonnull
  public static Element createSA_HigherEducation_v2021_04_13 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.higheredu.v2022_06_23.HigherEducationDiplomaType p = new eu.de4a.iem.jaxb.t41.higheredu.v2022_06_23.HigherEducationDiplomaType ();
    p.setId ("urn:credential:" + UUID.randomUUID ().toString ());
    {
      final LanguageStringType a = new LanguageStringType ();
      a.setText (_createT41Text ());
      p.addTitle (a);
    }
    p.addDegree (_createT41Text ());
    p.setCountry ("http://publications.europa.eu/resource/authority/country/NZL");
    p.addInstitutionName (_createT41Text ());
    p.addStudyProgramme (_createT41Text ());
    {
      final IscedFOetCodeType a = new IscedFOetCodeType ();
      a.setUri ("http://data.europa.eu/snb/isced-f/0610");
      p.setMainFieldOfStudy (a);
    }
    p.setModeOfStudy ("http://data.europa.eu/europass/learningScheduleType/fullTime");
    p.setDurationOfEducation (XSDDataTypeHelper.getFactory ()
                                               .newDurationYearMonth (true,
                                                                      MathHelper.abs (aTLR.nextInt (10)),
                                                                      MathHelper.abs (aTLR.nextInt (12))));
    p.setScope (MathHelper.toBigDecimal (MathHelper.abs (aTLR.nextInt ())));
    p.setDateOfIssue (_createLocalDate ());
    {
      final LocationType l = new LocationType ();
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createT41Text ());
        l.addName (b);
      }
      p.setPlaceOfIssue (l);
    }
    p.setHolderOfAchievement (_createT41Person ());

    return eu.de4a.iem.cev.de4a.t41.DE4AT41Marshaller.higherEducationDiploma ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  public static Element createSA_SecondaryEducation_v2022_05_12 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.secondaryedu.v2022_05_12.SecondaryEducationDiplomaType p = new eu.de4a.iem.jaxb.t41.secondaryedu.v2022_05_12.SecondaryEducationDiplomaType ();
    p.setId ("urn:credential:" + UUID.randomUUID ().toString ());
    {
      final LanguageStringType a = new LanguageStringType ();
      a.setText (_createT41Text ());
      p.addTitle (a);
    }
    p.addDegree (_createT41Text ());
    p.setCountry ("http://publications.europa.eu/resource/authority/country/NZL");
    p.addNameOfSchool (_createT41Text ());
    p.addNameOfProgram (_createT41Text ());

    {
      final eu.de4a.iem.jaxb.t41.secondaryedu.v2022_05_12.GradeType a = new eu.de4a.iem.jaxb.t41.secondaryedu.v2022_05_12.GradeType ();
      a.setSchemeID ("schemeID-" + MathHelper.abs (aTLR.nextInt ()));
      a.setExplanation ("exp-" + MathHelper.abs (aTLR.nextInt ()));
      a.setValue ("Grade-" + MathHelper.abs (aTLR.nextInt ()));
      p.setGrade (a);
    }

    p.setIssuingDate (_createDate ());

    return eu.de4a.iem.cev.de4a.t41.DE4AT41Marshaller.secondaryEducationDiploma ()
                                                     .getAsDocument (p)
                                                     .getDocumentElement ();
  }

  @Nonnull
  private static LocalDate _createLocalDate ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    return PDTFactory.createLocalDate (2000 + aTLR.nextInt (-50, 50),
                                       EDemoDocument.random (Month.class),
                                       aTLR.nextInt (1, 29));
  }

  @Nonnull
  private static DateType _createDate ()
  {
    return new DateType (_createLocalDate ());
  }

  @Nonnull
  public static Element createSA_Disability_v2022_05_12 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.disability.v2022_05_12.DisabilityEvidenceType p = new eu.de4a.iem.jaxb.t41.disability.v2022_05_12.DisabilityEvidenceType ();
    p.setBeneficiary (_createT41Person ());

    {
      final DisabilityCertificateType a = new DisabilityCertificateType ();
      a.setCertificateID ("Cert-" + MathHelper.abs (aTLR.nextInt ()));
      a.setEffectiveDate (_createDate ());
      a.setRevisionDate (_createDate ());
      a.setDisabilityPercentage (aTLR.nextInt (0, 101));
      p.setDisabilityCertificate (a);
    }

    return eu.de4a.iem.cev.de4a.t41.DE4AT41Marshaller.disability ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  public static Element createSA_LargeFamily_v2022_05_12 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.largefamily.v2022_05_12.LargeFamilyEvidenceType p = new eu.de4a.iem.jaxb.t41.largefamily.v2022_05_12.LargeFamilyEvidenceType ();
    p.setBeneficiary (_createT41Person ());

    {
      final LargeFamilyCertificateType a = new LargeFamilyCertificateType ();
      a.setCertificateID ("Cert-" + MathHelper.abs (aTLR.nextInt ()));
      a.setValidCertificate (aTLR.nextBoolean ());
      a.setIssuingDate (_createDate ());
      a.setExpiryDate (_createDate ());
      a.setNumberOfChildren (aTLR.nextInt (0, 5));
      p.setLargeFamilyCertificate (a);
    }

    return eu.de4a.iem.cev.de4a.t41.DE4AT41Marshaller.largeFamily ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  public static Element createDBA_LegalEntity_v06 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType p = new eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType ();
    {
      final eu.de4a.iem.jaxb.t42.v0_6.NamesType a = new eu.de4a.iem.jaxb.t42.v0_6.NamesType ();
      final LegalEntityLegalNameType aLegalName = new LegalEntityLegalNameType ();
      aLegalName.setValue ("LegalEntity-" + MathHelper.abs (aTLR.nextInt ()));
      a.setLegalEntityLegalName (aLegalName);
      p.addCompanyName (a);
    }
    p.setCompanyType ("CompanyType-" + MathHelper.abs (aTLR.nextInt ()));
    p.setCompanyStatus ("CompanyStatus-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_6.ActivityType a = new eu.de4a.iem.jaxb.t42.v0_6.ActivityType ();
      a.addNaceCode ("Nace-" + MathHelper.abs (aTLR.nextInt ()));
      p.setCompanyActivity (a);
    }
    p.setRegistrationDate (_createLocalDate ());
    p.setCompanyEUID ("CompanyEUID-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_6.AddressType a = new eu.de4a.iem.jaxb.t42.v0_6.AddressType ();
      a.setPoBox ("POBox-" + MathHelper.abs (aTLR.nextInt ()));
      p.addRegisteredAddress (a);
    }
    return eu.de4a.iem.cev.de4a.t42.DE4AT42Marshaller.legalEntity ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  private static IdentifierType _createID ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final IdentifierType ret = new IdentifierType ();
    ret.setValue ("ID-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  private static CvidentifierType _createT43CvID ()
  {
    final CvidentifierType ret = new CvidentifierType ();
    ret.setIdentifier (_createID ());
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.birth.v1_7.PublicOrganisationType _createBirthPubOrg ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.birth.v1_7.PublicOrganisationType ret = new eu.de4a.iem.jaxb.t43.birth.v1_7.PublicOrganisationType ();
    ret.addIdentifier (_createT43CvID ());
    ret.addPrefLabel (new oasis.names.specification.bdndr.schema.xsd.unqualifieddatatypes_1.TextType ("PrefLabel-" +
                                                                                                      MathHelper.abs (aTLR.nextInt ())));
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domdereg.v1_0.PublicOrganisationType _createDomDeregPubOrg ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.domdereg.v1_0.PublicOrganisationType ret = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.PublicOrganisationType ();
    ret.addIdentifier (_createT43CvID ());
    ret.addPrefLabel (new oasis.names.specification.bdndr.schema.xsd.unqualifieddatatypes_1.TextType ("PrefLabel-" +
                                                                                                      MathHelper.abs (aTLR.nextInt ())));
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domreg.v1_7.PublicOrganisationType _createDomRegPubOrg ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.domreg.v1_7.PublicOrganisationType ret = new eu.de4a.iem.jaxb.t43.domreg.v1_7.PublicOrganisationType ();
    ret.addIdentifier (_createT43CvID ());
    ret.addPrefLabel (new oasis.names.specification.bdndr.schema.xsd.unqualifieddatatypes_1.TextType ("PrefLabel-" +
                                                                                                      MathHelper.abs (aTLR.nextInt ())));
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.marriage.v1_7.PublicOrganisationType _createMarriagePubOrg ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.marriage.v1_7.PublicOrganisationType ret = new eu.de4a.iem.jaxb.t43.marriage.v1_7.PublicOrganisationType ();
    ret.addIdentifier (_createT43CvID ());
    ret.addPrefLabel (new oasis.names.specification.bdndr.schema.xsd.unqualifieddatatypes_1.TextType ("PrefLabel-" +
                                                                                                      MathHelper.abs (aTLR.nextInt ())));
    return ret;
  }

  private static void _fill (@Nonnull final CvaddressType aAddr)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    {
      final PostCodeType a = new PostCodeType ();
      a.setValue ("PostCode-" + MathHelper.abs (aTLR.nextInt ()));
      aAddr.addPostCode (a);
    }
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.birth.v1_7.LocationAddressType _createBirthLA ()
  {
    final eu.de4a.iem.jaxb.t43.birth.v1_7.LocationAddressType ret = new eu.de4a.iem.jaxb.t43.birth.v1_7.LocationAddressType ();
    ret.setAdminUnitL1 (EDemoDocument.random (ISO3166CountryType.class));
    ret.setAdminUnitL2 (EDemoDocument.random (NUTS2021Type.class));
    _fill (ret);
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.birth.v1_7.ConstrainedLocationAddressType _createBirthCLA ()
  {
    final eu.de4a.iem.jaxb.t43.birth.v1_7.ConstrainedLocationAddressType ret = new eu.de4a.iem.jaxb.t43.birth.v1_7.ConstrainedLocationAddressType ();
    ret.setAdminUnitL1 (EDemoDocument.random (ISO3166CountryType.class));
    ret.setAdminUnitL2 (EDemoDocument.random (NUTS2021Type.class));
    _fill (ret);
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domdereg.v1_0.ConstrainedLocationAddressType _createDomDeregCLA ()
  {
    final eu.de4a.iem.jaxb.t43.domdereg.v1_0.ConstrainedLocationAddressType ret = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.ConstrainedLocationAddressType ();
    ret.setAdminUnitL1 (EDemoDocument.random (ISO3166CountryType.class));
    ret.setAdminUnitL2 (EDemoDocument.random (NUTS2021Type.class));
    _fill (ret);
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domreg.v1_7.ConstrainedLocationAddressType _createDomRegCLA ()
  {
    final eu.de4a.iem.jaxb.t43.domreg.v1_7.ConstrainedLocationAddressType ret = new eu.de4a.iem.jaxb.t43.domreg.v1_7.ConstrainedLocationAddressType ();
    ret.setAdminUnitL1 (EDemoDocument.random (ISO3166CountryType.class));
    ret.setAdminUnitL2 (EDemoDocument.random (NUTS2021Type.class));
    _fill (ret);
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.marriage.v1_7.ConstrainedLocationAddressType _createMarriageCLA ()
  {
    final eu.de4a.iem.jaxb.t43.marriage.v1_7.ConstrainedLocationAddressType ret = new eu.de4a.iem.jaxb.t43.marriage.v1_7.ConstrainedLocationAddressType ();
    ret.setAdminUnitL1 (EDemoDocument.random (ISO3166CountryType.class));
    ret.setAdminUnitL2 (EDemoDocument.random (NUTS2021Type.class));
    _fill (ret);
    return ret;
  }

  @Nonnull
  private static GivenNameType _createT43GivenName ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final GivenNameType ret = new GivenNameType ();
    ret.setValue ("Given-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  private static FamilyNameType _createT43FamilyName ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final FamilyNameType ret = new FamilyNameType ();
    ret.setValue ("Family-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.birth.v1_7.DateObjectType _createBirthDate ()
  {
    final LocalDate aLD = _createLocalDate ();
    final eu.de4a.iem.jaxb.t43.birth.v1_7.DateObjectType a = new eu.de4a.iem.jaxb.t43.birth.v1_7.DateObjectType ();
    XMLGregorianCalendar c = XSDDataTypeHelper.getFactory ().newXMLGregorianCalendar ();
    c.setYear (aLD.getYear ());
    a.setYear (c);

    c = XSDDataTypeHelper.getFactory ().newXMLGregorianCalendar ();
    c.setMonth (aLD.getMonthValue ());
    a.setMonth (c);

    c = XSDDataTypeHelper.getFactory ().newXMLGregorianCalendar ();
    c.setDay (aLD.getDayOfMonth ());
    a.setDay (c);

    return a;
  }

  @Nonnull
  private static ChildType _createT43ChildType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ChildType ret = new ChildType ();

    {
      final eu.de4a.iem.jaxb.t43.birth.v1_7.NameType a = new eu.de4a.iem.jaxb.t43.birth.v1_7.NameType ();
      a.addGivenName (_createT43GivenName ());
      if (aTLR.nextBoolean ())
        a.addGivenName (_createT43GivenName ());
      a.addFamilyName (_createT43FamilyName ());
      if (aTLR.nextBoolean ())
        a.addFamilyName (_createT43FamilyName ());
      ret.setPersonName (a);
    }

    ret.setDateOfBirth (_createBirthDate ());
    ret.setGender (EDemoDocument.random (GenderType.class));
    ret.setPlaceOfBirth (_createBirthLA ());
    return ret;
  }

  @Nonnull
  private static BirthType _createBirthType ()
  {
    final BirthType ret = new BirthType ();
    ret.setChild (_createT43ChildType ());
    return ret;
  }

  @Nonnull
  public static Element createMA_Birth_v1_7 ()
  {
    final eu.de4a.iem.jaxb.t43.birth.v1_7.BirthEvidenceType p = new eu.de4a.iem.jaxb.t43.birth.v1_7.BirthEvidenceType ();
    p.setIdentifier (_createT43CvID ());
    p.setIssueDate (_createDate ());
    p.setIssuingAuthority (_createBirthPubOrg ());
    p.setIssuingPlace (_createBirthCLA ());
    p.setCertifiesBirth (_createBirthType ());
    return eu.de4a.iem.cev.de4a.t43.DE4AT43Marshaller.birthEvidence ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domdereg.v1_0.DomicileType _createDomDeregDomicileType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.domdereg.v1_0.DomicileType ret = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.DomicileType ();
    {
      final eu.de4a.iem.jaxb.t43.domdereg.v1_0.PersonType a = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.PersonType ();
      {
        final eu.de4a.iem.jaxb.t43.domdereg.v1_0.NameType b = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.NameType ();
        b.addGivenName (_createT43GivenName ());
        if (aTLR.nextBoolean ())
          b.addGivenName (_createT43GivenName ());
        b.addFamilyName (_createT43FamilyName ());
        if (aTLR.nextBoolean ())
          b.addFamilyName (_createT43FamilyName ());
        a.setPersonName (b);
      }
      a.setGender (EDemoDocument.random (GenderType.class));
      ret.setInhabitant (a);
    }
    ret.setDomicile (_createDomDeregCLA ());
    return ret;
  }

  @Nonnull
  private static eu.de4a.iem.jaxb.t43.domreg.v1_7.DomicileType _createDomRegDomicileType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t43.domreg.v1_7.DomicileType ret = new eu.de4a.iem.jaxb.t43.domreg.v1_7.DomicileType ();
    {
      final eu.de4a.iem.jaxb.t43.domreg.v1_7.PersonType a = new eu.de4a.iem.jaxb.t43.domreg.v1_7.PersonType ();
      {
        final eu.de4a.iem.jaxb.t43.domreg.v1_7.NameType b = new eu.de4a.iem.jaxb.t43.domreg.v1_7.NameType ();
        b.addGivenName (_createT43GivenName ());
        if (aTLR.nextBoolean ())
          b.addGivenName (_createT43GivenName ());
        b.addFamilyName (_createT43FamilyName ());
        if (aTLR.nextBoolean ())
          b.addFamilyName (_createT43FamilyName ());
        a.setPersonName (b);
      }
      a.setGender (EDemoDocument.random (GenderType.class));
      ret.setInhabitant (a);
    }
    ret.setDomicile (_createDomRegCLA ());
    return ret;
  }

  @Nonnull
  public static Element createMA_DomesticDeregistration_v1_0 ()
  {
    final eu.de4a.iem.jaxb.t43.domdereg.v1_0.DomicileDeregistrationEvidenceType p = new eu.de4a.iem.jaxb.t43.domdereg.v1_0.DomicileDeregistrationEvidenceType ();
    p.setIdentifier (_createT43CvID ());
    p.setIssueDate (_createDate ());
    p.setIssuingAuthority (_createDomDeregPubOrg ());
    p.setIssuingPlace (_createDomDeregCLA ());
    p.setCertifiesDomicile (_createDomDeregDomicileType ());
    return eu.de4a.iem.cev.de4a.t43.DE4AT43Marshaller.domicileDeregistrationEvidence ()
                                                     .getAsDocument (p)
                                                     .getDocumentElement ();
  }

  @Nonnull
  public static Element createMA_DomesticRegistration_v1_7 ()
  {
    final eu.de4a.iem.jaxb.t43.domreg.v1_7.DomicileRegistrationEvidenceType p = new eu.de4a.iem.jaxb.t43.domreg.v1_7.DomicileRegistrationEvidenceType ();
    p.setIdentifier (_createT43CvID ());
    p.setIssueDate (_createDate ());
    p.setIssuingAuthority (_createDomRegPubOrg ());
    p.setIssuingPlace (_createDomRegCLA ());
    p.setCertifiesDomicile (_createDomRegDomicileType ());
    return eu.de4a.iem.cev.de4a.t43.DE4AT43Marshaller.domicileRegistrationEvidence ()
                                                     .getAsDocument (p)
                                                     .getDocumentElement ();
  }

  @Nonnull
  private static MarriageType _createMarriageType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final MarriageType ret = new MarriageType ();
    ret.setDateOfMarriage (_createDate ());

    {
      final eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriedPersonType a = new eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriedPersonType ();
      {
        final eu.de4a.iem.jaxb.t43.marriage.v1_7.NameType b = new eu.de4a.iem.jaxb.t43.marriage.v1_7.NameType ();
        b.addGivenName (_createT43GivenName ());
        if (aTLR.nextBoolean ())
          b.addGivenName (_createT43GivenName ());
        b.addFamilyName (_createT43FamilyName ());
        if (aTLR.nextBoolean ())
          b.addFamilyName (_createT43FamilyName ());
        a.setPersonName (b);
      }
      a.setGender (EDemoDocument.random (GenderType.class));
      ret.addSpouse (a);
    }
    {
      final eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriedPersonType a = new eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriedPersonType ();
      {
        final eu.de4a.iem.jaxb.t43.marriage.v1_7.NameType b = new eu.de4a.iem.jaxb.t43.marriage.v1_7.NameType ();
        b.addGivenName (_createT43GivenName ());
        if (aTLR.nextBoolean ())
          b.addGivenName (_createT43GivenName ());
        b.addFamilyName (_createT43FamilyName ());
        if (aTLR.nextBoolean ())
          b.addFamilyName (_createT43FamilyName ());
        a.setPersonName (b);
      }
      a.setGender (EDemoDocument.random (GenderType.class));
      ret.addSpouse (a);
    }
    return ret;
  }

  @Nonnull
  public static Element createMA_Marriage_v1_7 ()
  {
    final eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriageEvidenceType p = new eu.de4a.iem.jaxb.t43.marriage.v1_7.MarriageEvidenceType ();
    p.setIdentifier (_createT43CvID ());
    p.setIssueDate (_createDate ());
    p.setIssuingAuthority (_createMarriagePubOrg ());
    p.setIssuingPlace (_createMarriageCLA ());
    p.setCertifiesMarriage (_createMarriageType ());
    return eu.de4a.iem.cev.de4a.t43.DE4AT43Marshaller.marriageEvidence ().getAsDocument (p).getDocumentElement ();
  }
}
