package eu.de4a.demoui.pub;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.w3c.dom.Element;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.math.MathHelper;

import eu.de4a.iem.jaxb.t41.uc1.v2021_02_11.ModeOfStudy;
import eu.de4a.iem.jaxb.w3.cv.bc.LegalEntityLegalNameType;
import eu.de4a.iem.xml.XSDDataTypeHelper;
import eu.de4a.iem.xml.de4a.EDE4ACanonicalEvidenceType;
import eu.europa.data.europass.model.credentials_.IscedFOetCodeType;
import eu.europa.data.europass.model.credentials_.LanguageCharCodeEnumType;
import eu.europa.data.europass.model.credentials_.LanguageStringType;
import eu.europa.data.europass.model.credentials_.LegalIdentifierType;
import eu.europa.data.europass.model.credentials_.LocationType;
import eu.europa.data.europass.model.credentials_.PersonType;
import eu.europa.data.europass.model.credentials_.TextContentTypeCodeEnumType;
import eu.europa.data.europass.model.credentials_.TextType;
import oasis.names.specification.bdndr.schema.xsd.unqualifieddatatypes_1.NumericType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.DateType;

/**
 * Demo canonical evidence type
 *
 * @author Philip Helger
 */
public enum EDemoCE
{
  T41_UC1_2021_02_11 (EDE4ACanonicalEvidenceType.T41_UC1_2021_02_11, EDemoCE::createT41_UC1_v2021_02_11),
  T41_UC1_2021_04_13 (EDE4ACanonicalEvidenceType.T41_UC1_2021_04_13, EDemoCE::createT41_UC1_v2021_04_13),
  T42_COMPANY_INFO_V06 (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO_V06, EDemoCE::createDBA_v06);

  private final EDE4ACanonicalEvidenceType m_eCEType;
  private final Supplier <Element> m_aAnyCreator;

  EDemoCE (@Nonnull final EDE4ACanonicalEvidenceType eCEType, final Supplier <Element> aAnyCreator)
  {
    m_eCEType = eCEType;
    m_aAnyCreator = aAnyCreator;
  }

  @Nonnull
  public EDE4ACanonicalEvidenceType getCEType ()
  {
    return m_eCEType;
  }

  @Nonnull
  public Element createExampleElement ()
  {
    return m_aAnyCreator.get ();
  }

  @Nonnull
  private static TextType _createText ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final TextType t = new TextType ();
    t.setValue ("Text-" + MathHelper.abs (aTLR.nextInt ()));
    t.setContentType (EDemoDocument.random (TextContentTypeCodeEnumType.values ()));
    t.setLang (EDemoDocument.random (LanguageCharCodeEnumType.values ()));
    return t;
  }

  @Nonnull
  public static Element createT41_UC1_v2021_02_11 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.uc1.v2021_02_11.HigherEducationEvidenceType p = new eu.de4a.iem.jaxb.t41.uc1.v2021_02_11.HigherEducationEvidenceType ();
    p.setId ("urn:credential:" + UUID.randomUUID ().toString ());
    {
      final LanguageStringType a = new LanguageStringType ();
      a.setText (_createText ());
      p.addTitle (a);
    }
    p.addDegree (_createText ());
    p.setCountry ("http://publications.europa.eu/resource/authority/country/NZL");
    p.addInstitutionName (_createText ());
    p.addStudyProgramme (_createText ());
    p.setMainFieldOfStudy ("http://data.europa.eu/snb/isced-f/0610");
    p.setModeOfStudy (EDemoDocument.random (ModeOfStudy.values ()));
    p.setDurationOfEducation (new NumericType (MathHelper.toBigDecimal (MathHelper.abs (aTLR.nextInt ()))));
    p.setScope (new NumericType (MathHelper.toBigDecimal (MathHelper.abs (aTLR.nextInt ()))));
    p.setDateOfIssue (new DateType (PDTFactory.getCurrentLocalDate ()));
    p.addPlaceOfIssue (_createText ());
    {
      final PersonType a = new PersonType ();
      a.setId ("id-" + MathHelper.abs (aTLR.nextInt ()));
      {
        final LegalIdentifierType b = new LegalIdentifierType ();
        b.setValue ("NationalID-" + MathHelper.abs (aTLR.nextInt ()));
        b.setSpatialID ("SpatialID-" + MathHelper.abs (aTLR.nextInt ()));
        a.setNationalId (b);
      }
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createText ());
        a.setGivenNames (b);
      }
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createText ());
        a.setFamilyName (b);
      }
      a.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (18 + aTLR.nextInt (80)));
      p.setHolderOfAchievement (a);
    }

    return eu.de4a.iem.xml.de4a.t41.v2021_02_11.DE4AT41Marshaller.higherEducationEvidence ()
                                                                 .getAsDocument (p)
                                                                 .getDocumentElement ();
  }

  @Nonnull
  public static Element createT41_UC1_v2021_04_13 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t41.uc1.v2021_04_13.HigherEducationDiplomaType p = new eu.de4a.iem.jaxb.t41.uc1.v2021_04_13.HigherEducationDiplomaType ();
    p.setId ("urn:credential:" + UUID.randomUUID ().toString ());
    {
      final LanguageStringType a = new LanguageStringType ();
      a.setText (_createText ());
      p.addTitle (a);
    }
    p.addDegree (_createText ());
    p.setCountry ("http://publications.europa.eu/resource/authority/country/NZL");
    p.addInstitutionName (_createText ());
    p.addStudyProgramme (_createText ());
    {
      final IscedFOetCodeType a = new IscedFOetCodeType ();
      a.setUri ("http://data.europa.eu/snb/isced-f/0610");
      p.setMainFieldOfStudy (a);
    }
    p.setModeOfStudy ("http://data.europa.eu/europass/learningScheduleType/fullTime");
    p.addDurationOfEducation (XSDDataTypeHelper.getFactory ()
                                               .newDurationYearMonth (true,
                                                                      MathHelper.abs (aTLR.nextInt (10)),
                                                                      MathHelper.abs (aTLR.nextInt (12))));
    p.setScope (MathHelper.toBigDecimal (MathHelper.abs (aTLR.nextInt ())));
    p.setDateOfIssue (new DateType (PDTFactory.getCurrentLocalDate ()));
    {
      final LocationType l = new LocationType ();
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createText ());
        l.addName (b);
      }
      p.setPlaceOfIssue (l);
    }
    {
      final PersonType a = new PersonType ();
      a.setId ("id-" + MathHelper.abs (aTLR.nextInt ()));
      {
        final LegalIdentifierType b = new LegalIdentifierType ();
        b.setValue ("NationalID-" + MathHelper.abs (aTLR.nextInt ()));
        b.setSpatialID ("SpatialID-" + MathHelper.abs (aTLR.nextInt ()));
        a.setNationalId (b);
      }
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createText ());
        a.setGivenNames (b);
      }
      {
        final LanguageStringType b = new LanguageStringType ();
        b.setText (_createText ());
        a.setFamilyName (b);
      }
      a.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (18 + aTLR.nextInt (80)));
      p.setHolderOfAchievement (a);
    }

    return eu.de4a.iem.xml.de4a.t41.v2021_04_13.DE4AT41Marshaller.higherEducationDiploma ()
                                                                 .getAsDocument (p)
                                                                 .getDocumentElement ();
  }

  @Nonnull
  public static Element createDBA_v06 ()
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
    p.setRegistrationDate (PDTFactory.getCurrentLocalDate ().minusDays (aTLR.nextInt (100)));
    p.setCompanyEUID ("CompanyEUID-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_6.AddressType a = new eu.de4a.iem.jaxb.t42.v0_6.AddressType ();
      a.setPoBox ("POBox-" + MathHelper.abs (aTLR.nextInt ()));
      p.addRegisteredAddress (a);
    }
    return eu.de4a.iem.xml.de4a.t42.v0_6.DE4AT42Marshaller.legalEntity ().getAsDocument (p).getDocumentElement ();
  }
}
