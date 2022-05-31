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
package eu.de4a.demoui.model;

import java.awt.Color;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.base64.Base64;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.id.IHasID;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.math.MathHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;
import com.helger.jaxb.validation.IValidationEventHandlerFactory;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.pdflayout.PDFCreationException;
import com.helger.pdflayout.PageLayoutPDF;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.text.PLText;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.xml.XMLFactory;

import eu.de4a.iem.cev.EDE4ACanonicalEvidenceType;
import eu.de4a.iem.core.CIEM;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.DE4AResponseDocumentHelper;
import eu.de4a.iem.core.jaxb.common.AgentType;
import eu.de4a.iem.core.jaxb.common.CanonicalEvidenceType;
import eu.de4a.iem.core.jaxb.common.DataRequestSubjectCVType;
import eu.de4a.iem.core.jaxb.common.DomesticEvidenceType;
import eu.de4a.iem.core.jaxb.common.ErrorType;
import eu.de4a.iem.core.jaxb.common.ExplicitRequestType;
import eu.de4a.iem.core.jaxb.common.IssuingTypeType;
import eu.de4a.iem.core.jaxb.common.LegalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.NaturalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.RedirectUserType;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceUSIItemType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceIMType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceUSIType;
import eu.de4a.iem.core.jaxb.common.RequestGroundsType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;
import eu.de4a.iem.core.jaxb.eidas.np.GenderType;

/**
 * Available mock demo documents
 *
 * @author Philip Helger
 */
public enum EDemoDocument implements IHasID <String>, IHasDisplayName, IDemoDocument
{
  /* IM pattern */

  // DE-DR request (C1 -> C2)
  IM_REQ_DE_DR ("im-req-de-dr",
                "IM Request DE to DR (C1 -> C2)",
                "/request/im",
                EDemoDocumentType.REQUEST,
                EDemoDocument::createDemoRequestExtractMultiEvidenceIM,
                DE4ACoreMarshaller.drRequestExtractMultiEvidenceIMMarshaller ()),
  // DT-DO request (C3 -> C4)
  IM_REQ_DT_DO ("im-req-dt-do",
                "IM Request DT to DO (C3 -> C4)",
                "/request/im",
                EDemoDocumentType.REQUEST,
                EDemoDocument::createDemoRequestExtractMultiEvidenceIM,
                DE4ACoreMarshaller.doRequestExtractMultiEvidenceIMMarshaller ()),

  // DO-DT response (C4 -> C3)
  IM_RESP_DO_DT_T42_V06 ("im-resp-do-dt",
                         "IM Response from DO to DT (C4 -> C3)",
                         "/response/evidence",
                         EDemoDocumentType.RESPONSE,
                         EDemoCanonicalEvidence.T42_LEGAL_ENTITY_V06,
                         EDemoDocument::createDemoResponseExtractMultiEvidence,
                         DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),

  // DR-DE response (C2 -> C1)
  IM_RESP_DR_DE_T42_V06 ("im-resp-dr-de",
                         "IM Response from DR to DE (C2 -> C1)",
                         "/response/evidence",
                         EDemoDocumentType.RESPONSE,
                         EDemoCanonicalEvidence.T42_LEGAL_ENTITY_V06,
                         EDemoDocument::createDemoResponseExtractMultiEvidence,
                         DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),

  /* USI pattern */

  /* USI request */

  // DE-DR request (C1 -> C2)
  USI_REQ_DE_DR ("usi-req-de-dr",
                 "USI Request from DE to DR (C1 -> C2)",
                 "/request/usi",
                 EDemoDocumentType.REQUEST,
                 EDemoDocument::createDemoRequestExtractMultiEvidenceUSI,
                 DE4ACoreMarshaller.drRequestExtractMultiEvidenceUSIMarshaller ()),

  // DT-DO request (C3 -> C4)
  USI_REQ_DT_DO ("usi-req-dt-do",
                 "USI Request from DT to DO (C3 -> C4)",
                 "/request/usi",
                 EDemoDocumentType.REQUEST,
                 EDemoDocument::createDemoRequestExtractMultiEvidenceUSI,
                 DE4ACoreMarshaller.doRequestExtractMultiEvidenceUSIMarshaller ()),

  /* USI Redirect Response */

  // DO-DT response (C4 -> C3)
  USI_RESP_REDIR_DO_DT ("usi-redirect-do-dt",
                        "USI Redirect Response from DO to DT (C4 -> C3)",
                        "/response/usi/redirectUser",
                        EDemoDocumentType.RESPONSE,
                        EDemoDocument::createDemoUSIRedirectUser,
                        DE4ACoreMarshaller.dtUSIRedirectUserMarshaller ()),

  // DR-DE response (C2 -> C1)
  USI_RESP_REDIR_DE_DE ("usi-redirect-dr-de",
                        "USI Redirect Response from DR to DE (C2 -> C1)",
                        "/response/usi/redirectUser",
                        EDemoDocumentType.RESPONSE,
                        EDemoDocument::createDemoUSIRedirectUser,
                        DE4ACoreMarshaller.deUSIRedirectUserMarshaller ()),

  /* USI Data Response */

  // DO-DT (C4 ->C3)
  USI_RESP_DATA_DO_DT_T41_HIGHER_EDUCATION_DIPLOMA_2021_04_13 ("usi-resp-do-dt",
                                                               "USI Data Response DO to DT (C4 -> C3)",
                                                               "/response/evidence",
                                                               EDemoDocumentType.RESPONSE,
                                                               EDemoCanonicalEvidence.T41_HIGHER_EDUCATION_DIPLOMA_2021_04_13,
                                                               EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                               DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T41_SECONDARY_EDUCATION_DIPLOMA_2022_05_22 ("usi-resp-do-dt",
                                                                  "USI Data Response DO to DT (C4 -> C3)",
                                                                  "/response/evidence",
                                                                  EDemoDocumentType.RESPONSE,
                                                                  EDemoCanonicalEvidence.T41_SECONDARY_EDUCATION_DIPLOMA_2022_05_12,
                                                                  EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                                  DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T41_DISABILITY_2022_05_22 ("usi-resp-do-dt",
                                                 "USI Data Response DO to DT (C4 -> C3)",
                                                 "/response/evidence",
                                                 EDemoDocumentType.RESPONSE,
                                                 EDemoCanonicalEvidence.T41_DISABILITY_2022_05_12,
                                                 EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                 DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T41_LARGE_FAMILY_2022_05_22 ("usi-resp-do-dt",
                                                   "USI Data Response DO to DT (C4 -> C3)",
                                                   "/response/evidence",
                                                   EDemoDocumentType.RESPONSE,
                                                   EDemoCanonicalEvidence.T41_LARGE_FAMILY_2022_05_12,
                                                   EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                   DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_BIRTH_V17 ("usi-resp-do-dt",
                                     "USI Data Response DO to DT (C4 -> C3)",
                                     "/response/evidence",
                                     EDemoDocumentType.RESPONSE,
                                     EDemoCanonicalEvidence.T43_BIRTH_V17,
                                     EDemoDocument::createDemoResponseExtractMultiEvidence,
                                     DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_DOMREG_V17 ("usi-resp-do-dt",
                                      "USI Data Response DO to DT (C4 -> C3)",
                                      "/response/evidence",
                                      EDemoDocumentType.RESPONSE,
                                      EDemoCanonicalEvidence.T43_DOMREG_V17,
                                      EDemoDocument::createDemoResponseExtractMultiEvidence,
                                      DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_MARRIAGE_V17 ("usi-resp-do-dt",
                                        "USI Data Response DO to DT (C4 -> C3)",
                                        "/response/evidence",
                                        EDemoDocumentType.RESPONSE,
                                        EDemoCanonicalEvidence.T43_MARRIAGE_V17,
                                        EDemoDocument::createDemoResponseExtractMultiEvidence,
                                        DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_PENSION_MOL_V01 ("usi-resp-do-dt",
                                           "USI Data Response DO to DT (C4 -> C3)",
                                           "/response/evidence",
                                           EDemoDocumentType.RESPONSE,
                                           EDemoCanonicalEvidence.T43_PENSION_MOL_V01,
                                           EDemoDocument::createDemoResponseExtractMultiEvidence,
                                           DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_UNEMPLOYMENT_MOL_V01 ("usi-resp-do-dt",
                                                "USI Data Response DO to DT (C4 -> C3)",
                                                "/response/evidence",
                                                EDemoDocumentType.RESPONSE,
                                                EDemoCanonicalEvidence.T43_UNEMPLOYMENT_MOL_V01,
                                                EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DO_DT_T43_WORKING_LIFE_MOL_V01 ("usi-resp-do-dt",
                                                "USI Data Response DO to DT (C4 -> C3)",
                                                "/response/evidence",
                                                EDemoDocumentType.RESPONSE,
                                                EDemoCanonicalEvidence.T43_WORKING_LIFE_MOL_V01,
                                                EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                DE4ACoreMarshaller::dtResponseExtractMultiEvidenceMarshaller),

  // DT-DO (C3 -> C4)
  USI_RESP_DATA_ERROR_DT_DO ("usi-resp-error-dt-do",
                             "USI Data Response Error from DT to DO (C3 -> C4)",
                             null,
                             EDemoDocumentType.RESPONSE,
                             EDemoDocument::createDemoResponseError,
                             DE4ACoreMarshaller.defResponseErrorMarshaller ()),

  // DR-DE (C2 -> C1)
  USI_RESP_DATA_DR_DE_T41_HIGHER_EDUCATION_DIPLOMA_2021_04_13 ("usi-resp-dr-de",
                                                               "USI Data Response DR to DE (C2 -> C1)",
                                                               "/response/evidence",
                                                               EDemoDocumentType.RESPONSE,
                                                               EDemoCanonicalEvidence.T41_HIGHER_EDUCATION_DIPLOMA_2021_04_13,
                                                               EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                               DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T41_SECONDARY_EDUCATION_DIPLOMA_2022_05_12 ("usi-resp-dr-de",
                                                                  "USI Data Response DR to DE (C2 -> C1)",
                                                                  "/response/evidence",
                                                                  EDemoDocumentType.RESPONSE,
                                                                  EDemoCanonicalEvidence.T41_SECONDARY_EDUCATION_DIPLOMA_2022_05_12,
                                                                  EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                                  DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T41_DISABILITY_2022_05_12 ("usi-resp-dr-de",
                                                 "USI Data Response DR to DE (C2 -> C1)",
                                                 "/response/evidence",
                                                 EDemoDocumentType.RESPONSE,
                                                 EDemoCanonicalEvidence.T41_DISABILITY_2022_05_12,
                                                 EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                 DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T41_LARGE_FAMILY_2022_05_12 ("usi-resp-dr-de",
                                                   "USI Data Response DR to DE (C2 -> C1)",
                                                   "/response/evidence",
                                                   EDemoDocumentType.RESPONSE,
                                                   EDemoCanonicalEvidence.T41_LARGE_FAMILY_2022_05_12,
                                                   EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                   DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_BIRTH_V17 ("usi-resp-dr-de",
                                     "USI Data Response DR to DE (C2 -> C1)",
                                     "/response/evidence",
                                     EDemoDocumentType.RESPONSE,
                                     EDemoCanonicalEvidence.T43_BIRTH_V17,
                                     EDemoDocument::createDemoResponseExtractMultiEvidence,
                                     DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_DOMREG_V17 ("usi-resp-dr-de",
                                      "USI Data Response DR to DE (C2 -> C1)",
                                      "/response/evidence",
                                      EDemoDocumentType.RESPONSE,
                                      EDemoCanonicalEvidence.T43_DOMREG_V17,
                                      EDemoDocument::createDemoResponseExtractMultiEvidence,
                                      DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_MARRIAGE_V17 ("usi-resp-dr-de",
                                        "USI Data Response DR to DE (C2 -> C1)",
                                        "/response/evidence",
                                        EDemoDocumentType.RESPONSE,
                                        EDemoCanonicalEvidence.T43_MARRIAGE_V17,
                                        EDemoDocument::createDemoResponseExtractMultiEvidence,
                                        DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_PENSION_MOL_V01 ("usi-resp-dr-de",
                                           "USI Data Response DR to DE (C2 -> C1)",
                                           "/response/evidence",
                                           EDemoDocumentType.RESPONSE,
                                           EDemoCanonicalEvidence.T43_PENSION_MOL_V01,
                                           EDemoDocument::createDemoResponseExtractMultiEvidence,
                                           DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_UNEMPLOYMENT_MOL_V01 ("usi-resp-dr-de",
                                                "USI Data Response DR to DE (C2 -> C1)",
                                                "/response/evidence",
                                                EDemoDocumentType.RESPONSE,
                                                EDemoCanonicalEvidence.T43_UNEMPLOYMENT_MOL_V01,
                                                EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  USI_RESP_DATA_DR_DE_T43_WORKING_LIFE_MOL_V01 ("usi-resp-dr-de",
                                                "USI Data Response DR to DE (C2 -> C1)",
                                                "/response/evidence",
                                                EDemoDocumentType.RESPONSE,
                                                EDemoCanonicalEvidence.T43_WORKING_LIFE_MOL_V01,
                                                EDemoDocument::createDemoResponseExtractMultiEvidence,
                                                DE4ACoreMarshaller::deResponseExtractMultiEvidenceMarshaller),
  // DE-DR Response (C1->C2)
  USI_RESP_DATA_ERROR_DE_DR ("usi-resp-error-de-dr",
                             "USI Data Response Error from DE to DR (C1 -> C2)",
                             null,
                             EDemoDocumentType.RESPONSE,
                             EDemoDocument::createDemoResponseError,
                             DE4ACoreMarshaller.defResponseErrorMarshaller ());

  private final String m_sID;
  private final String m_sDisplayName;
  private final String m_sRelativeURL;
  private final EDemoDocumentType m_eDocType;
  private final Supplier <Object> m_aDemoRequestCreator;
  private final Function <Object, String> m_aToString;
  private final DE4ACoreMarshaller <Object> m_aMarshaller;

  <T> EDemoDocument (@Nonnull @Nonempty final String sIDPrefix,
                     @Nonnull @Nonempty final String sDisplayNamePrefix,
                     @Nonnull @Nonempty final String sRelativeURL,
                     @Nonnull final EDemoDocumentType eDocType,
                     @Nonnull final EDemoCanonicalEvidence eCE,
                     @Nonnull final Function <Element, T> aDemoRequestCreator,
                     @Nonnull final Function <EDE4ACanonicalEvidenceType, DE4ACoreMarshaller <T>> aMarshallerProvider)
  {
    this (sIDPrefix + "-" + eCE.getCEType ().getID (),
          sDisplayNamePrefix + " - " + eCE.getCEType ().getDisplayName (),
          sRelativeURL,
          eDocType,
          () -> aDemoRequestCreator.apply (eCE.createExampleElement ()),
          aMarshallerProvider.apply (eCE.getCEType ()));
  }

  <T> EDemoDocument (@Nonnull @Nonempty final String sID,
                     @Nonnull @Nonempty final String sDisplayName,
                     @Nonnull @Nonempty final String sRelativeURL,
                     @Nonnull final EDemoDocumentType eDocType,
                     @Nonnull final Supplier <T> aDemoRequestCreator,
                     @Nonnull final DE4ACoreMarshaller <T> aMarshaller)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_sRelativeURL = sRelativeURL;
    m_eDocType = eDocType;
    m_aDemoRequestCreator = GenericReflection.uncheckedCast (aDemoRequestCreator);
    final Function <T, String> aToString = aMarshaller.formatted ()::getAsString;
    m_aToString = GenericReflection.uncheckedCast (aToString);
    m_aMarshaller = GenericReflection.uncheckedCast (aMarshaller);
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

  @Nullable
  public String getRelativeURL ()
  {
    // Only for requests
    return m_sRelativeURL;
  }

  public boolean hasRelativeURL ()
  {
    return StringHelper.hasText (m_sRelativeURL);
  }

  @Nonnull
  public EDemoDocumentType getDocumentType ()
  {
    return m_eDocType;
  }

  @Nonnull
  public Object createDemoRequest ()
  {
    return m_aDemoRequestCreator.get ();
  }

  @Nonnull
  public String getAnyMessageAsString (@Nonnull final Object aObj)
  {
    // Throws exception if the type does not match so be careful
    return m_aToString.apply (aObj);
  }

  @Nonnull
  public String getDemoMessageAsString ()
  {
    return m_aToString.apply (m_aDemoRequestCreator.get ());
  }

  @Nonnull
  public IErrorList validateMessage (@Nonnull final String sMsg)
  {
    final ErrorList ret = new ErrorList ();
    final IValidationEventHandlerFactory aOld = m_aMarshaller.getValidationEventHandlerFactory ();
    m_aMarshaller.setValidationEventHandlerFactory (x -> new WrappedCollectingValidationEventHandler (ret));
    m_aMarshaller.read (sMsg);
    m_aMarshaller.setValidationEventHandlerFactory (aOld);
    return ret;
  }

  @Nonnull
  public Object parseMessage (@Nonnull final String sMsg)
  {
    return m_aMarshaller.read (sMsg);
  }

  @Nullable
  public static EDemoDocument getFromIDOrNull (final String sID)
  {
    return EnumHelper.getFromIDOrNull (EDemoDocument.class, sID);
  }

  @Nonnull
  private static AgentType _createAgent ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final AgentType ret = new AgentType ();
    ret.setAgentUrn ("Urn-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setAgentName ("Maxi Musterfrau " + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  static <T> T random (@Nonnull final T [] a)
  {
    return a[ThreadLocalRandom.current ().nextInt (a.length)];
  }

  @Nonnull
  static <T extends Enum <T>> T random (@Nonnull final Class <T> a)
  {
    return random (a.getEnumConstants ());
  }

  @Nonnull
  private static NaturalPersonIdentifierType _createNP ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final NaturalPersonIdentifierType ret = new NaturalPersonIdentifierType ();
    ret.setPersonIdentifier ("ID-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setFirstName ("FirstName-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setFamilyName ("FamilyName-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDateOfBirth (PDTFactory.getCurrentLocalDate ().minusYears (18 + aTLR.nextInt (50)));
    ret.setGender (random (GenderType.values ()));
    // Ignore the optional stuff
    return ret;
  }

  @Nonnull
  private static LegalPersonIdentifierType _createLP ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final LegalPersonIdentifierType ret = new LegalPersonIdentifierType ();
    ret.setLegalPersonIdentifier ("LPI-ID-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setLegalName ("LegalName-" + MathHelper.abs (aTLR.nextInt ()));
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
      ret.setExplicitRequest (random (ExplicitRequestType.values ()));
    return ret;
  }

  @Nonnull
  private static RequestEvidenceItemType _createRequestEvidenceIMItemType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestEvidenceItemType ret = new RequestEvidenceItemType ();
    ret.setRequestItemId (UUID.randomUUID ().toString ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setRequestGrounds (_createRequestGrounds ());
    ret.setCanonicalEvidenceTypeId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    // No additional parameter
    return ret;
  }

  @Nonnull
  public static RequestExtractMultiEvidenceIMType createDemoRequestExtractMultiEvidenceIM ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestExtractMultiEvidenceIMType ret = new RequestExtractMultiEvidenceIMType ();
    ret.setRequestId (UUID.randomUUID ().toString ());
    ret.setSpecificationId (CIEM.SPECIFICATION_ID);
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.addRequestEvidenceIMItem (_createRequestEvidenceIMItemType ());
    if (aTLR.nextBoolean ())
      ret.addRequestEvidenceIMItem (_createRequestEvidenceIMItemType ());
    return ret;
  }

  @Nonnull
  private static RequestEvidenceUSIItemType _createRequestEvidenceUSIItemType ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestEvidenceUSIItemType ret = new RequestEvidenceUSIItemType ();
    ret.setRequestItemId (UUID.randomUUID ().toString ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setRequestGrounds (_createRequestGrounds ());
    ret.setCanonicalEvidenceTypeId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    // No additional parameter
    ret.setDataEvaluatorURL ("https://de.example.org/de4a-" + MathHelper.abs (aTLR.nextLong ()));
    return ret;
  }

  @Nonnull
  public static RequestExtractMultiEvidenceUSIType createDemoRequestExtractMultiEvidenceUSI ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestExtractMultiEvidenceUSIType ret = new RequestExtractMultiEvidenceUSIType ();
    ret.setRequestId (UUID.randomUUID ().toString ());
    ret.setSpecificationId (CIEM.SPECIFICATION_ID);
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.addRequestEvidenceUSIItem (_createRequestEvidenceUSIItemType ());
    if (aTLR.nextBoolean ())
      ret.addRequestEvidenceUSIItem (_createRequestEvidenceUSIItemType ());
    return ret;
  }

  private static byte [] _createPDF (final String sWhat)
  {
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      final FontSpec r16 = new FontSpec (PreloadFont.REGULAR, 16);
      final PLPageSet aPS1 = new PLPageSet (PDRectangle.A4);

      aPS1.addElement (new PLText ("Dummy DE4A " +
                                   sWhat +
                                   " - Current time: " +
                                   PDTFactory.getCurrentLocalDateTime ().toString (),
                                   r16).setBorder (Color.BLUE));

      final PageLayoutPDF aPageLayout = new PageLayoutPDF ().setCompressPDF (true);
      aPageLayout.addPageSet (aPS1);
      aPageLayout.renderTo (aBAOS);

      // Store as Base64 encoded value
      return aBAOS.getBufferOrCopy ();
    }
    catch (final PDFCreationException ex)
    {
      throw new RuntimeException (ex);
    }
  }

  @Nonnull
  private static Element _createAny (@Nonnull final byte [] aPDF)
  {
    final Document doc = XMLFactory.newDocument ();
    final Element e = (Element) doc.appendChild (doc.createElement ("bla"));
    e.appendChild (doc.createTextNode (Base64.safeEncodeBytes (aPDF)));
    return e;
  }

  @Nonnull
  private static CanonicalEvidenceType _createCanonicalEvidence (@Nonnull final Element aCanonicalEvidence)
  {
    final CanonicalEvidenceType ret = new CanonicalEvidenceType ();
    // Strict
    ret.setAny (aCanonicalEvidence);
    return ret;
  }

  @Nonnull
  private static DomesticEvidenceType _createDomesticEvidence ()
  {
    final DomesticEvidenceType ret = new DomesticEvidenceType ();
    ret.setIssuingType (random (IssuingTypeType.values ()));
    ret.setMimeType ("application/xml");
    ret.setDataLanguage ("en");
    ret.setEvidenceData (_createPDF ("DomesticEvidence"));
    return ret;
  }

  @Nonnull
  private static ICommonsList <DomesticEvidenceType> _createDomesticEvidenceList ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ICommonsList <DomesticEvidenceType> ret = new CommonsArrayList <> ();
    ret.add (_createDomesticEvidence ());
    if (aTLR.nextBoolean ())
      ret.add (_createDomesticEvidence ());
    return ret;
  }

  @Nonnull
  private static ICommonsList <ErrorType> _createErrorList ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ICommonsList <ErrorType> ret = new CommonsArrayList <> ();
    // Max length 10
    ret.add (DE4AResponseDocumentHelper.createError ("Code-" + aTLR.nextInt (100_000), "Ooops - something went wrong"));
    if (aTLR.nextBoolean ())
      ret.add (DE4AResponseDocumentHelper.createError ("Code-" + aTLR.nextInt (100_000),
                                                       "Ooops - something else also went wrong"));
    return ret;
  }

  @Nonnull
  public static ResponseErrorType createDemoResponseError ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseErrorType ret = new ResponseErrorType ();
    if (aTLR.nextBoolean ())
    {
      ret.setAck (true);
    }
    else
    {
      ret.setAck (false);
      ret.getError ().addAll (_createErrorList ());
    }
    return ret;
  }

  @Nonnull
  private static ResponseExtractEvidenceItemType _createResponseExtractEvidenceItem (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseExtractEvidenceItemType ret = new ResponseExtractEvidenceItemType ();
    ret.setRequestItemId ("RequestItem-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataRequestSubject (_createDRS ());
    ret.setCanonicalEvidenceTypeId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    if (aTLR.nextBoolean ())
    {
      ret.setCanonicalEvidence (_createCanonicalEvidence (aCanonicalEvidence));
      if (aTLR.nextBoolean ())
        ret.getDomesticEvidence ().addAll (_createDomesticEvidenceList ());
    }
    else
      ret.getError ().addAll (_createErrorList ());
    return ret;
  }

  @Nonnull
  public static ResponseExtractMultiEvidenceType createDemoResponseExtractMultiEvidence (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseExtractMultiEvidenceType ret = new ResponseExtractMultiEvidenceType ();
    ret.setRequestId (UUID.randomUUID ().toString ());
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.addResponseExtractEvidenceItem (_createResponseExtractEvidenceItem (aCanonicalEvidence));
    if (aTLR.nextBoolean ())
      ret.addResponseExtractEvidenceItem (_createResponseExtractEvidenceItem (aCanonicalEvidence));
    return ret;
  }

  @Nonnull
  public static RedirectUserType createDemoUSIRedirectUser ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RedirectUserType ret = new RedirectUserType ();
    ret.setRequestId (UUID.randomUUID ().toString ());
    ret.setSpecificationId (CIEM.SPECIFICATION_ID);
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    // TOD remove?
    ret.setCanonicalEvidenceTypeId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setRedirectUrl ("https://de.example.org/preview?key=" + MathHelper.abs (aTLR.nextLong ()));
    // NO errors
    return ret;
  }
}
