/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.math.MathHelper;
import com.helger.commons.name.IHasDisplayName;

import eu.de4a.edm.jaxb.common.idtypes.LegalEntityIdentifierType;
import eu.de4a.edm.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.edm.jaxb.common.types.AgentCVType;
import eu.de4a.edm.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.edm.jaxb.common.types.EvidenceServiceDataType;
import eu.de4a.edm.jaxb.common.types.ExplicitRequestType;
import eu.de4a.edm.jaxb.common.types.RequestGroundsType;
import eu.de4a.edm.jaxb.common.types.RequestTransferEvidenceIMType;
import eu.de4a.edm.jaxb.eidas.np.GenderType;
import eu.de4a.edm.xml.de4a.DE4AMarshaller;

public enum EDemoDocument implements IHasID <String>, IHasDisplayName
{
  DR_IM ("dr-im",
         "Request to DR (IM)",
         "/dr1/im/transferevidence",
         EDemoDocument::createDemoDR_IM,
         DE4AMarshaller.drImRequestMarshaller ().formatted ()::getAsString);

  private String m_sID;
  private String m_sDisplayName;
  private String m_sRelativeURL;
  private Supplier <Object> m_aDemoRequestCreator;
  private Function <Object, String> m_aRequestToString;

  <T> EDemoDocument (@Nonnull @Nonempty final String sID,
                 @Nonnull @Nonempty final String sDisplayName,
                 @Nonnull @Nonempty final String sRelativeURL,
                 @Nonnull final Supplier <T> aDemoRequestCreator,
                 @Nonnull final Function <T, String> aRequestToString)
  {
    ValueEnforcer.isTrue (sRelativeURL.startsWith ("/"), "Relative URL must start with a slash");
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_sRelativeURL = sRelativeURL;
    m_aDemoRequestCreator = GenericReflection.uncheckedCast (aDemoRequestCreator);
    m_aRequestToString = GenericReflection.uncheckedCast (aRequestToString);
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
  @Nonempty
  public String getRelativeURL ()
  {
    return m_sRelativeURL;
  }

  @Nonnull
  public String getDemoRequestString ()
  {
    return m_aRequestToString.apply (m_aDemoRequestCreator.get ());
  }

  @Nullable
  public static EDemoDocument getFromIDOrNull (final String sID)
  {
    return EnumHelper.getFromIDOrNull (EDemoDocument.class, sID);
  }

  @Nonnull
  private static AgentCVType _createAgent ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final AgentCVType ret = new AgentCVType ();
    ret.setId ("ID-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setName ("Maxi Musterfrau " + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  private static <T> T _random (@Nonnull final T [] a)
  {
    return a[ThreadLocalRandom.current ().nextInt (a.length)];
  }

  @Nonnull
  private static NaturalPersonIdentifierType _createNP ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final NaturalPersonIdentifierType ret = new NaturalPersonIdentifierType ();
    ret.setIdentifier ("ID-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setGivenName ("GivenName-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setFamilyName ("FamilyName-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (18 + aTLR.nextInt (50)));
    ret.setGender (_random (GenderType.values ()));
    // Ignore the optional stuff
    return ret;
  }

  @Nonnull
  private static LegalEntityIdentifierType _createLP ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final LegalEntityIdentifierType ret = new LegalEntityIdentifierType ();
    ret.setLegalEntityIdentifier ("LEI-ID-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setLegalEntityName ("LEI_NAME-" + MathHelper.abs (aTLR.nextInt ()));
    // Ignore the optional stuff
    return ret;
  }

  @Nonnull
  private static DataRequestSubjectCVType _createDRS ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final DataRequestSubjectCVType ret = new DataRequestSubjectCVType ();
    if (aTLR.nextBoolean ())
      ret.setDataSubjectPerson (_createNP ());
    else
    {
      ret.setDataSubjectCompany (_createLP ());
      if (aTLR.nextBoolean ())
        ret.setDataSubjectRepresentative (_createNP ());
    }
    return ret;
  }

  @Nonnull
  private static RequestGroundsType _createRequestGrounds ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestGroundsType ret = new RequestGroundsType ();
    if (aTLR.nextBoolean ())
      ret.setLawELIPermanentLink ("https://example.org/article/" + MathHelper.abs (aTLR.nextInt ()));
    else
      ret.setExplicitRequest (_random (ExplicitRequestType.values ()));
    return ret;
  }

  @Nonnull
  private static EvidenceServiceDataType _createEvidenceServiceData ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final EvidenceServiceDataType ret = new EvidenceServiceDataType ();
    ret.setEvidenceServiceURI ("https://myevidenceprovider.example.org/service/" + MathHelper.abs (aTLR.nextInt ()));
    // Ignore the optional stuff
    return ret;
  }

  @Nonnull
  public static RequestTransferEvidenceIMType createDemoDR_IM ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestTransferEvidenceIMType req = new RequestTransferEvidenceIMType ();
    req.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    req.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    req.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    req.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    req.setDataEvaluator (_createAgent ());
    req.setDataOwner (_createAgent ());
    req.setDataRequestSubject (_createDRS ());
    req.setRequestGrounds (_createRequestGrounds ());
    req.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    req.setEvidenceServiceData (_createEvidenceServiceData ());
    req.setReturnServiceId ("ReturnService-" + MathHelper.abs (aTLR.nextInt ()));
    return req;
  }
}
