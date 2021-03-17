package eu.de4a.demoui.pub;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import org.w3c.dom.Element;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.math.MathHelper;

import eu.de4a.iem.jaxb.t42.v0_4.LegalEntityType;
import eu.de4a.iem.jaxb.w3.cv.bc.LegalEntityLegalNameType;
import eu.de4a.iem.xml.de4a.EDE4ACanonicalEvidenceType;

/**
 * Demo canonical evidence type
 *
 * @author Philip Helger
 */
public enum EDemoCE
{
  T42_COMPANY_INFO_V04 (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO_V04, EDemoCE::createDBA_v04),
  T42_COMPANY_INFO_V05 (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO_V05, EDemoCE::createDBA_v05);

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
  public static Element createDBA_v04 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t42.v0_4.LegalEntityType p = new LegalEntityType ();
    {
      final eu.de4a.iem.jaxb.t42.v0_4.NamesType a = new eu.de4a.iem.jaxb.t42.v0_4.NamesType ();
      a.setLegalEntityName ("LegalEntity-" + MathHelper.abs (aTLR.nextInt ()));
      p.addCompanyName (a);
    }
    p.setCompanyType ("CompanyType-" + MathHelper.abs (aTLR.nextInt ()));
    p.setCompanyStatus ("CompanyStatus-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_4.ActivityType a = new eu.de4a.iem.jaxb.t42.v0_4.ActivityType ();
      a.addNaceCode ("Nace-" + MathHelper.abs (aTLR.nextInt ()));
      p.setCompanyActivity (a);
    }
    p.setRegistrationDate (PDTFactory.getCurrentLocalDate ().minusDays (aTLR.nextInt (100)));
    p.setCompanyEUID ("CompanyEUID-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_4.AddressType a = new eu.de4a.iem.jaxb.t42.v0_4.AddressType ();
      a.setPoBox ("POBox-" + MathHelper.abs (aTLR.nextInt ()));
      p.addRegisteredAddress (a);
    }
    return eu.de4a.iem.xml.de4a.t42.v0_4.DE4AT42Marshaller.legalEntity ().getAsDocument (p).getDocumentElement ();
  }

  @Nonnull
  public static Element createDBA_v05 ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final eu.de4a.iem.jaxb.t42.v0_5.LegalEntityType p = new eu.de4a.iem.jaxb.t42.v0_5.LegalEntityType ();
    {
      final eu.de4a.iem.jaxb.t42.v0_5.NamesType a = new eu.de4a.iem.jaxb.t42.v0_5.NamesType ();
      final LegalEntityLegalNameType aLegalName = new LegalEntityLegalNameType ();
      aLegalName.setValue ("LegalEntity-" + MathHelper.abs (aTLR.nextInt ()));
      a.setLegalEntityLegalName (aLegalName);
      p.addCompanyName (a);
    }
    p.setCompanyType ("CompanyType-" + MathHelper.abs (aTLR.nextInt ()));
    p.setCompanyStatus ("CompanyStatus-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_5.ActivityType a = new eu.de4a.iem.jaxb.t42.v0_5.ActivityType ();
      a.addNaceCode ("Nace-" + MathHelper.abs (aTLR.nextInt ()));
      p.setCompanyActivity (a);
    }
    p.setRegistrationDate (PDTFactory.getCurrentLocalDate ().minusDays (aTLR.nextInt (100)));
    p.setCompanyEUID ("CompanyEUID-" + MathHelper.abs (aTLR.nextInt ()));
    {
      final eu.de4a.iem.jaxb.t42.v0_5.AddressType a = new eu.de4a.iem.jaxb.t42.v0_5.AddressType ();
      a.setPoBox ("POBox-" + MathHelper.abs (aTLR.nextInt ()));
      p.addRegisteredAddress (a);
    }
    return eu.de4a.iem.xml.de4a.t42.v0_5.DE4AT42Marshaller.legalEntity ().getAsDocument (p).getDocumentElement ();
  }
}
