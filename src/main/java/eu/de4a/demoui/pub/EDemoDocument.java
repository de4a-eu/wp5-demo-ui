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

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.base64.Base64;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.id.IHasID;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.lang.GenericReflection;
import com.helger.commons.math.MathHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.pdflayout4.PDFCreationException;
import com.helger.pdflayout4.PageLayoutPDF;
import com.helger.pdflayout4.base.PLPageSet;
import com.helger.pdflayout4.element.text.PLText;
import com.helger.pdflayout4.spec.FontSpec;
import com.helger.pdflayout4.spec.PreloadFont;
import com.helger.xml.XMLFactory;

import eu.de4a.edm.jaxb.common.idtypes.LegalEntityIdentifierType;
import eu.de4a.edm.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.edm.jaxb.common.types.*;
import eu.de4a.edm.jaxb.eidas.np.GenderType;
import eu.de4a.edm.jaxb.t42.ContactPointType;
import eu.de4a.edm.xml.de4a.DE4AMarshaller;
import eu.de4a.edm.xml.de4a.DE4AResponseDocumentHelper;
import eu.de4a.edm.xml.de4a.EDE4ACanonicalEvidenceType;
import eu.de4a.edm.xml.de4a.t42.DE4AT42Marshaller;
import un.unece.uncefact.codelist.specification.ianamimemediatype._2003.BinaryObjectMimeCodeContentType;

public enum EDemoDocument implements IHasID <String>, IHasDisplayName
{
  DE_USI_REQ ("de-usi-req",
              "Request to DE (USI)",
              "/de1/usi/forwardevidence",
              true,
              EDemoDocument::createDemoDE_USI,
              DE4AMarshaller.deUsiRequestMarshaller (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO).formatted ()::getAsString),
  DE_USI_RESP ("de-usi-resp",
               "Response from DE (USI)",
               null,
               false,
               EDemoDocument::createResponseError,
               DE4AMarshaller.deUsiResponseMarshaller ().formatted ()::getAsString),
  DR_IM_REQ ("dr-im-req",
             "Request to DR (IM)",
             "/dr1/im/transferevidence",
             true,
             EDemoDocument::createDemoDR_IM,
             DE4AMarshaller.drImRequestMarshaller ().formatted ()::getAsString),
  DR_IM_RESP ("dr-im-resp",
              "Response from DR (IM)",
              null,
              false,
              EDemoDocument::createResponseTransferEvidence,
              DE4AMarshaller.drImResponseMarshaller (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO).formatted ()::getAsString),
  DR_USI_REQ ("dr-usi-req",
              "Request to DR (USI)",
              "/dr1/usi/transferevidence",
              true,
              EDemoDocument::createDemoDR_USI,
              DE4AMarshaller.drUsiRequestMarshaller ().formatted ()::getAsString),
  DR_USI_RESP ("dr-usi-resp",
               "Response from DR (USI)",
               null,
               false,
               EDemoDocument::createResponseError,
               DE4AMarshaller.drUsiResponseMarshaller ().formatted ()::getAsString),
  DT_USI_REQ ("dt-usi",
              "Request to DT (USI)",
              "/dt1/usi/transferevidence",
              true,
              EDemoDocument::createDemoDT_USI,
              DE4AMarshaller.dtUsiRequestMarshaller (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO).formatted ()::getAsString),
  DT_USI_RESP ("dt-usi-resp",
               "Response from DT (USI)",
               null,
               false,
               EDemoDocument::createResponseError,
               DE4AMarshaller.dtUsiResponseMarshaller ().formatted ()::getAsString),
  DO_IM_REQ ("do-im-req",
             "Request to DO (IM)",
             "/do1/im/extractevidence",
             true,
             EDemoDocument::createDemoDO_IM,
             DE4AMarshaller.doImRequestMarshaller ().formatted ()::getAsString),
  DO_IM_RESP ("do-im-resp",
              "Response from DO (IM)",
              null,
              false,
              EDemoDocument::createResponseExtractEvidence,
              DE4AMarshaller.doImResponseMarshaller (EDE4ACanonicalEvidenceType.T42_COMPANY_INFO).formatted ()::getAsString),
  DO_USI_REQ ("do-usi-req",
              "Request to DO (USI)",
              "/do1/usi/extractevidence",
              true,
              EDemoDocument::createDemoDO_USI,
              DE4AMarshaller.doUsiRequestMarshaller ().formatted ()::getAsString),
  DO_USI_RESP ("do-usi-resp",
               "Response from DO (USI)",
               null,
               false,
               EDemoDocument::createResponseError,
               DE4AMarshaller.doUsiResponseMarshaller ().formatted ()::getAsString);

  private String m_sID;
  private String m_sDisplayName;
  private String m_sRelativeURL;
  private boolean m_bIsRequest;
  private Supplier <Object> m_aDemoRequestCreator;
  private Function <Object, String> m_aToString;

  <T> EDemoDocument (@Nonnull @Nonempty final String sID,
                     @Nonnull @Nonempty final String sDisplayName,
                     @Nonnull @Nonempty final String sRelativeURL,
                     final boolean bIsRequest,
                     @Nonnull final Supplier <T> aDemoRequestCreator,
                     @Nonnull final Function <T, String> aToString)
  {
    if (bIsRequest)
      ValueEnforcer.isTrue (sRelativeURL.startsWith ("/"), "Relative URL must start with a slash");
    m_sID = sID;
    m_sDisplayName = sDisplayName;
    m_sRelativeURL = sRelativeURL;
    m_bIsRequest = bIsRequest;
    m_aDemoRequestCreator = GenericReflection.uncheckedCast (aDemoRequestCreator);
    m_aToString = GenericReflection.uncheckedCast (aToString);
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

  public boolean isRequest ()
  {
    return m_bIsRequest;
  }

  @Nonnull
  public String getDemoMessageAsString ()
  {
    return m_aToString.apply (m_aDemoRequestCreator.get ());
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
  public static RequestTransferEvidenceIMType createDemoDR_IM ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestTransferEvidenceIMType ret = new RequestTransferEvidenceIMType ();
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
  public static RequestTransferEvidenceUSIDRType createDemoDR_USI ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestTransferEvidenceUSIDRType ret = new RequestTransferEvidenceUSIDRType ();
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
  private static CanonicalEvidenceType _createCanonicalEvidence ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final CanonicalEvidenceType ret = new CanonicalEvidenceType ();
    {
      // Strict
      final ContactPointType p = new ContactPointType ();
      p.addEmail ("example@example_" + MathHelper.abs (aTLR.nextInt ()) + ".org");
      ret.setAny (DE4AT42Marshaller.contactPoint ().getAsDocument (p).getDocumentElement ());
    }
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
  public static RequestForwardEvidenceType createDemoDE_USI ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final RequestForwardEvidenceType ret = new RequestForwardEvidenceType ();
    ret.setRequestId ("Request-" + MathHelper.abs (aTLR.nextInt ()));
    ret.setTimeStamp (PDTFactory.getCurrentLocalDateTime ());
    ret.setPreviewResponse (_createPreviewResponse ());
    ret.setCanonicalEvidence (_createCanonicalEvidence ());
    ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    return ret;
  }

  @Nonnull
  public static RequestTransferEvidenceUSIDTType createDemoDT_USI ()
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
    ret.setCanonicalEvidence (_createCanonicalEvidence ());
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
      // TODO remove - should be optional
      ret.setErrorList (_createErrorList ());
    }
    else
    {
      ret.setAck (AckType.KO);
      ret.setErrorList (_createErrorList ());
    }
    return ret;
  }

  @Nonnull
  public static ResponseTransferEvidenceType createResponseTransferEvidence ()
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
      ret.setCanonicalEvidence (_createCanonicalEvidence ());
      if (aTLR.nextBoolean ())
        ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    }
    else
      ret.setErrorList (_createErrorList ());
    return ret;
  }

  @Nonnull
  public static ResponseExtractEvidenceType createResponseExtractEvidence ()
  {
    final ThreadLocalRandom aTLR = ThreadLocalRandom.current ();
    final ResponseExtractEvidenceType ret = new ResponseExtractEvidenceType ();
    if (aTLR.nextBoolean ())
    {
      ret.setCanonicalEvidence (_createCanonicalEvidence ());
      if (aTLR.nextBoolean ())
        ret.setDomesticEvidenceList (_createDomesticEvidenceList ());
    }
    else
      ret.setErrorList (_createErrorList ());
    return ret;
  }
}
