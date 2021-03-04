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

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.base64.Base64;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.id.IHasID;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.locale.country.ECountry;
import com.helger.commons.math.MathHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.pdflayout4.PDFCreationException;
import com.helger.pdflayout4.PageLayoutPDF;
import com.helger.pdflayout4.base.PLPageSet;
import com.helger.pdflayout4.element.text.PLText;
import com.helger.pdflayout4.spec.FontSpec;
import com.helger.pdflayout4.spec.PreloadFont;
import com.helger.xml.XMLFactory;

import eu.de4a.iem.jaxb.common.idtypes.LegalEntityIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.*;
import eu.de4a.iem.jaxb.eidas.np.GenderType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.DE4AResponseDocumentHelper;
import eu.de4a.iem.xml.de4a.EDE4ACanonicalEvidenceType;
import un.unece.uncefact.codelist.specification.ianamimemediatype._2003.BinaryObjectMimeCodeContentType;

public enum EDemoDocument implements IHasID <String>, IHasDisplayName
{
  DE_USI_REQ_DBA_V04 ("de-usi-req-dba-v04",
                      "Request to DE (USI) - DBA v0.4",
                      "/de1/usi/forwardevidence",
                      EDemoDocumentType.REQUEST,
                      EDemoCE.T42_COMPANY_INFO_V04,
                      EDemoDocument::createDemoDE_USI,
                      DE4AMarshaller::deUsiRequestMarshaller),
  DE_USI_REQ_DBA_V05 ("de-usi-req-dba-v05",
                      "Request to DE (USI) - DBA v0.5",
                      "/de1/usi/forwardevidence",
                      EDemoDocumentType.REQUEST,
                      EDemoCE.T42_COMPANY_INFO_V05,
                      EDemoDocument::createDemoDE_USI,
                      DE4AMarshaller::deUsiRequestMarshaller),
  DE_USI_RESP ("de-usi-resp",
               "Response from DE (USI)",
               null,
               EDemoDocumentType.RESPONSE,
               EDemoDocument::createResponseError,
               DE4AMarshaller.deUsiResponseMarshaller ()),
  DR_IM_REQ ("dr-im-req",
             "Request to DR (IM)",
             "/dr1/im/transferevidence",
             EDemoDocumentType.REQUEST,
             EDemoDocument::createDemoDR,
             DE4AMarshaller.drImRequestMarshaller ()),
  DR_IM_RESP_DBA_V04 ("dr-im-resp-dba-v04",
                      "Response from DR (IM) - DBA v0.4",
                      null,
                      EDemoDocumentType.RESPONSE,
                      EDemoCE.T42_COMPANY_INFO_V04,
                      EDemoDocument::createResponseTransferEvidence,
                      DE4AMarshaller::drImResponseMarshaller),
  DR_IM_RESP_DBA_V05 ("dr-im-resp-dba-v05",
                      "Response from DR (IM) - DBA v0.5",
                      null,
                      EDemoDocumentType.RESPONSE,
                      EDemoCE.T42_COMPANY_INFO_V05,
                      EDemoDocument::createResponseTransferEvidence,
                      DE4AMarshaller::drImResponseMarshaller),
  DR_USI_REQ ("dr-usi-req",
              "Request to DR (USI)",
              "/dr1/usi/transferevidence",
              EDemoDocumentType.REQUEST,
              EDemoDocument::createDemoDR,
              DE4AMarshaller.drUsiRequestMarshaller ()),
  DR_USI_RESP ("dr-usi-resp",
               "Response from DR (USI)",
               null,
               EDemoDocumentType.RESPONSE,
               EDemoDocument::createResponseError,
               DE4AMarshaller.drUsiResponseMarshaller ()),
  DT_USI_REQ_DBA_V04 ("dt-usi-req-dba-v04",
                      "Request to DT (USI) - DBA v0.4",
                      "/dt1/usi/transferevidence",
                      EDemoDocumentType.REQUEST,
                      EDemoCE.T42_COMPANY_INFO_V04,
                      EDemoDocument::createDemoDT_USI,
                      DE4AMarshaller::dtUsiRequestMarshaller),
  DT_USI_REQ_DBA_V05 ("dt-usi-req-dba-v05",
                      "Request to DT (USI) - DBA v0.5",
                      "/dt1/usi/transferevidence",
                      EDemoDocumentType.REQUEST,
                      EDemoCE.T42_COMPANY_INFO_V05,
                      EDemoDocument::createDemoDT_USI,
                      DE4AMarshaller::dtUsiRequestMarshaller),
  DT_USI_RESP ("dt-usi-resp",
               "Response from DT (USI)",
               null,
               EDemoDocumentType.RESPONSE,
               EDemoDocument::createResponseError,
               DE4AMarshaller.dtUsiResponseMarshaller ()),
  DO_IM_REQ ("do-im-req",
             "Request to DO (IM)",
             "/do1/im/extractevidence",
             EDemoDocumentType.REQUEST,
             EDemoDocument::createDemoDO_IM,
             DE4AMarshaller.doImRequestMarshaller ()),
  DO_IM_RESP_DBA_V04 ("do-im-resp-dba-v04",
                      "Response from DO (IM) - DBA v0.4",
                      null,
                      EDemoDocumentType.RESPONSE,
                      EDemoCE.T42_COMPANY_INFO_V04,
                      EDemoDocument::createResponseExtractEvidence,
                      DE4AMarshaller::doImResponseMarshaller),
  DO_IM_RESP_DBA_V05 ("do-im-resp-dba-v05",
                      "Response from DO (IM) - DBA v0.5",
                      null,
                      EDemoDocumentType.RESPONSE,
                      EDemoCE.T42_COMPANY_INFO_V05,
                      EDemoDocument::createResponseExtractEvidence,
                      DE4AMarshaller::doImResponseMarshaller),
  DO_USI_REQ ("do-usi-req",
              "Request to DO (USI)",
              "/do1/usi/extractevidence",
              EDemoDocumentType.REQUEST,
              EDemoDocument::createDemoDO_USI,
              DE4AMarshaller.doUsiRequestMarshaller ()),
  DO_USI_RESP ("do-usi-resp",
               "Response from DO (USI)",
               null,
               EDemoDocumentType.RESPONSE,
               EDemoDocument::createResponseError,
               DE4AMarshaller.doUsiResponseMarshaller ()),
  IDK_LOOKUP_ROUTING_INFO_REQUEST ("idk-lri-req",
                                   "IDK routing information lookup request",
                                   null,
                                   EDemoDocumentType.IDK_REQUEST,
                                   EDemoDocument::createIDKRequestLookupRoutingInformation,
                                   DE4AMarshaller.idkRequestLookupRoutingInformationMarshaller ()),
  IDK_LOOKUP_ROUTING_INFO_RESPONSE ("idk-lri-resp",
                                    "IDK routing information lookup response",
                                    null,
                                    EDemoDocumentType.IDK_RESPONSE,
                                    EDemoDocument::createIDKResponseLookupRoutingInformation,
                                    DE4AMarshaller.idkResponseLookupRoutingInformationMarshaller ()),
  IDK_LOOKUP_EVIDENCE_SERVICE_DATA_REQUEST ("idk-lesd-req",
                                            "IDK evidence service data lookup request",
                                            null,
                                            EDemoDocumentType.IDK_REQUEST,
                                            EDemoDocument::createIDKRequestLookupEvidenceServiceData,
                                            DE4AMarshaller.idkRequestLookupEvidenceServiceDataMarshaller ()),
  IDK_LOOKUP_EVIDENCE_SERVICE_DATA_RESPONSE ("idk-lesd-resp",
                                             "IDK evidence service data lookup response",
                                             null,
                                             EDemoDocumentType.IDK_RESPONSE,
                                             EDemoDocument::createIDKResponseLookupEvidenceServiceData,
                                             DE4AMarshaller.idkResponseLookupEvidenceServiceDataMarshaller ());

  private final String m_sID;
  private final String m_sDisplayName;
  private final String m_sRelativeURL;
  private final EDemoDocumentType m_eDocType;
  private final Supplier <Object> m_aDemoRequestCreator;
  private final Function <Object, String> m_aToString;
  private final BiConsumer <String, ErrorList> m_aReader;

  <T> EDemoDocument (@Nonnull @Nonempty final String sID,
                     @Nonnull @Nonempty final String sDisplayName,
                     @Nonnull @Nonempty final String sRelativeURL,
                     @Nonnull final EDemoDocumentType eDocType,
                     @Nonnull final EDemoCE eCE,
                     @Nonnull final Function <Element, T> aDemoRequestCreator,
                     @Nonnull final Function <EDE4ACanonicalEvidenceType, DE4AMarshaller <T>> aMarshallerProvider)
  {
    this (sID,
          sDisplayName,
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
                     @Nonnull final DE4AMarshaller <T> aMarshaller)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_sRelativeURL = sRelativeURL;
    m_eDocType = eDocType;
    m_aDemoRequestCreator = GenericReflection.uncheckedCast (aDemoRequestCreator);
    final Function <T, String> aToString = aMarshaller.formatted ()::getAsString;
    m_aToString = GenericReflection.uncheckedCast (aToString);
    m_aReader = aMarshaller::readAndValidate;
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

  public EDemoDocumentType getDocumentType ()
  {
    return m_eDocType;
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
    m_aReader.accept (sMsg, ret);
    return ret;
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
  public static RequestExtractEvidenceIMType createDemoDO_IM ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestExtractEvidenceIMType ret = new RequestExtractEvidenceIMType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setRequestGrounds (_createRequestGrounds ());
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setEvidenceServiceData (_createEvidenceServiceData ());
    return ret;
  }

  @Nonnull
  public static RequestExtractEvidenceUSIType createDemoDO_USI ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestExtractEvidenceUSIType ret = new RequestExtractEvidenceUSIType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setRequestGrounds (_createRequestGrounds ());
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setEvidenceServiceData (_createEvidenceServiceData ());
    ret.setReturnServiceId ("ReturnService-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  public static RequestTransferEvidenceUSIIMDRType createDemoDR ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestTransferEvidenceUSIIMDRType ret = new RequestTransferEvidenceUSIIMDRType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setRequestGrounds (_createRequestGrounds ());
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setEvidenceServiceData (_createEvidenceServiceData ());
    ret.setReturnServiceId ("ReturnService-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  private static byte [] _createPDF (final String sWhat)
  {
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      final FontSpec r16 = new FontSpec (PreloadFont.REGULAR, 16);
      final PLPageSet aPS1 = new PLPageSet (PDRectangle.A4);

      aPS1.addElement (new PLText ("Dummy DE4A " + sWhat + " - Current time: " + PDTFactory.getCurrentLocalDateTime ().toString (),
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
  private static PreviewResponseType _createPreviewResponse ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final PreviewResponseType ret = new PreviewResponseType ();
    // Lax
    ret.setAny (_createAny (_createPDF ("Preview-" + MathHelper.abs (aTLR.nextInt ()))));
    return ret;
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
    ret.setIssuingType (_random (IssuingTypeType.values ()));
    ret.setMimeType (_random (BinaryObjectMimeCodeContentType.values ()));
    ret.setDataLanguage ("en");
    ret.setEvidenceData (_createPDF ("DomesticEvidence"));
    return ret;
  }

  @Nonnull
  private static DomesticsEvidencesType _createDomesticEvidenceList ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final DomesticsEvidencesType ret = new DomesticsEvidencesType ();
    ret.addDomesticEvidence (_createDomesticEvidence ());
    if (aTLR.nextBoolean ())
      ret.addDomesticEvidence (_createDomesticEvidence ());
    return ret;
  }

  @Nonnull
  public static RequestForwardEvidenceType createDemoDE_USI (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestForwardEvidenceType ret = new RequestForwardEvidenceType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setPreviewResponse (_createPreviewResponse ());
    ret.setCanonicalEvidence (_createCanonicalEvidence (aCanonicalEvidence));
    ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    return ret;
  }

  @Nonnull
  public static RequestTransferEvidenceUSIDTType createDemoDT_USI (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestTransferEvidenceUSIDTType ret = new RequestTransferEvidenceUSIDTType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setPreviewResponse (_createPreviewResponse ());
    ret.setCanonicalEvidence (_createCanonicalEvidence (aCanonicalEvidence));
    ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    return ret;
  }

  @Nonnull
  private static ErrorListType _createErrorList ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ErrorListType ret = new ErrorListType ();
    // Max length 10
    ret.addError (DE4AResponseDocumentHelper.createError ("Code-" + aTLR.nextInt (100_000), "Ooops - something went wrong"));
    if (aTLR.nextBoolean ())
      ret.addError (DE4AResponseDocumentHelper.createError ("Code-" + aTLR.nextInt (100_000), "Ooops - something else also went wrong"));
    return ret;
  }

  @Nonnull
  public static ResponseErrorType createResponseError ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseErrorType ret = new ResponseErrorType ();
    if (aTLR.nextBoolean ())
    {
      ret.setAck (AckType.OK);
    }
    else
    {
      ret.setAck (AckType.KO);
      ret.setErrorList (_createErrorList ());
    }
    return ret;
  }

  @Nonnull
  public static ResponseTransferEvidenceType createResponseTransferEvidence (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseTransferEvidenceType ret = new ResponseTransferEvidenceType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setSpecificationId ("Specification-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setProcedureId ("Procedure-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataEvaluator (_createAgent ());
    ret.setDataOwner (_createAgent ());
    ret.setDataRequestSubject (_createDRS ());
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    if (aTLR.nextBoolean ())
    {
      ret.setCanonicalEvidence (_createCanonicalEvidence (aCanonicalEvidence));
      if (aTLR.nextBoolean ())
        ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    }
    else
      ret.setErrorList (_createErrorList ());
    return ret;
  }

  @Nonnull
  public static ResponseExtractEvidenceType createResponseExtractEvidence (@Nonnull final Element aCanonicalEvidence)
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseExtractEvidenceType ret = new ResponseExtractEvidenceType ();
    if (aTLR.nextBoolean ())
    {
      ret.setCanonicalEvidence (_createCanonicalEvidence (aCanonicalEvidence));
      if (aTLR.nextBoolean ())
        ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    }
    else
      ret.setErrorList (_createErrorList ());
    return ret;
  }

  @Nonnull
  public static RequestLookupRoutingInformationType createIDKRequestLookupRoutingInformation ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestLookupRoutingInformationType ret = new RequestLookupRoutingInformationType ();
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setCountryCode (_random (ECountry.values ()).getISOCountryCode ());
    return ret;
  }

  @Nonnull
  private static IaOrganisationalStructureType _createOrganisationalStructure ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ElementType e = new ElementType ();
    e.setAtuLevel (_random (AtuLevelType.values ()));
    e.setAtuPath ("Path-" + MathHelper.abs (aTLR.nextInt ()));
    e.setAtuCode ("Code-" + MathHelper.abs (aTLR.nextInt ()));
    e.setAtuName ("Name-" + MathHelper.abs (aTLR.nextInt ()));
    e.setAtuLatinName ("LatinName-" + MathHelper.abs (aTLR.nextInt ()));

    final IaOrganisationalStructureType ret = new IaOrganisationalStructureType ();
    ret.setElement (e);
    return ret;
  }

  @Nonnull
  private static IssuingAuthorityType _createIssuingAuthority ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final IssuingAuthorityType ret = new IssuingAuthorityType ();
    ret.setEvidenceTypeId (_random (EvidenceTypeIdType.values ()));
    ret.setCountryCode (_random (ECountry.values ()).getISOCountryCode ());
    ret.setIaLevelPath ("IaLevel-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setIaTotalNum (aTLR.nextInt (101));
    ret.setIaOrganisationalStructure (_createOrganisationalStructure ());
    return ret;
  }

  @Nonnull
  public static DataOwnerType _createDataOwner ()
  {
    final DataOwnerType ret = new DataOwnerType ();
    ret.setAgent (_createAgent ());
    return ret;
  }

  @Nonnull
  public static DataTransferorType _createDataTransferor ()
  {
    final DataTransferorType ret = new DataTransferorType ();
    ret.setAgent (_createAgent ());
    return ret;
  }

  @Nonnull
  public static TextType _createText ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final TextType ret = new TextType ();
    ret.setLang ("Lang-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setLabel ("Label" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDefinition ("Def-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  public static TextsType _createTexts ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final TextsType ret = new TextsType ();
    ret.addText (_createText ());
    if (aTLR.nextBoolean ())
      ret.addText (_createText ());
    return ret;
  }

  @Nonnull
  public static ParameterType _createParameter ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ParameterType ret = new ParameterType ();
    ret.setItemId ("ItemId-" + MathHelper.abs (aTLR.nextInt ()));
    if (aTLR.nextBoolean ())
      ret.setItemType ("ItemType-" + MathHelper.abs (aTLR.nextInt ()));
    if (aTLR.nextBoolean ())
      ret.setDataType (_random (DataTypeType.values ()));
    if (aTLR.nextBoolean ())
      ret.setConstraints ("Constraints-" + MathHelper.abs (aTLR.nextInt ()));
    if (aTLR.nextBoolean ())
      ret.setTexts (_createTexts ());
    return ret;
  }

  @Nonnull
  public static ParametersType _createParameters ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ParametersType ret = new ParametersType ();
    ret.addParameter (_createParameter ());
    if (aTLR.nextBoolean ())
      ret.addParameter (_createParameter ());
    if (aTLR.nextBoolean ())
      ret.addParameter (_createParameter ());
    return ret;
  }

  @Nonnull
  public static InputParameterSetsType _createInputParameterSets ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final InputParameterSetsType ret = new InputParameterSetsType ();
    ret.setSerialNumber (aTLR.nextInt (1, 101));
    ret.setTitle ("Title-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setRecordMatchingAssurance (_random (RecordMatchingAssuranceType.values ()));
    ret.setParameters (_createParameters ());
    return ret;
  }

  @Nonnull
  public static EvidenceServiceType _createEvidenceService ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final EvidenceServiceType ret = new EvidenceServiceType ();
    ret.setService ("Service-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setCanonicalEvidenceType ("CanonEvidenceType-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setDataOwner (_createDataOwner ());
    ret.setDataTransferor (_createDataTransferor ());
    ret.setInputParameterSets (_createInputParameterSets ());
    return ret;
  }

  @Nonnull
  public static ResponseLookupRoutingInformationType createIDKResponseLookupRoutingInformation ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseLookupRoutingInformationType ret = new ResponseLookupRoutingInformationType ();
    switch (aTLR.nextInt (3))
    {
      case 0:
        ret.setIssuingAuthority (_createIssuingAuthority ());
        break;
      case 1:
        ret.setEvidenceService (_createEvidenceService ());
        break;
      case 2:
        ret.setErrorList (_createErrorList ());
        break;
    }
    return ret;
  }

  @Nonnull
  public static RequestLookupEvidenceServiceDataType createIDKRequestLookupEvidenceServiceData ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestLookupEvidenceServiceDataType ret = new RequestLookupEvidenceServiceDataType ();
    ret.setCanonicalEvidenceId ("CanonicalEvidence-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setCountryCode (_random (ECountry.values ()).getISOCountryCode ());
    ret.setAdminTerritorialUnit ("ATU-" + MathHelper.abs (aTLR.nextInt ()));
    return ret;
  }

  @Nonnull
  public static ResponseLookupEvidenceServiceDataType createIDKResponseLookupEvidenceServiceData ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseLookupEvidenceServiceDataType ret = new ResponseLookupEvidenceServiceDataType ();
    switch (aTLR.nextInt (2))
    {
      case 0:
        ret.setEvidenceService (_createEvidenceService ());
        break;
      case 1:
        ret.setErrorList (_createErrorList ());
        break;
    }
    return ret;
  }
}
