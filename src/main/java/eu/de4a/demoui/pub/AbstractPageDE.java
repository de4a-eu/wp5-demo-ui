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

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.cev.de4a.t42.v0_6.DE4AT42Marshaller;
import eu.de4a.iem.core.jaxb.common.AgentType;
import eu.de4a.iem.core.jaxb.common.CanonicalEvidenceType;
import eu.de4a.iem.core.jaxb.common.DataRequestSubjectCVType;
import eu.de4a.iem.core.jaxb.common.LegalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.NaturalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;
import eu.de4a.iem.jaxb.t42.v0_6.ActivityType;
import eu.de4a.iem.jaxb.t42.v0_6.AddressType;
import eu.de4a.iem.jaxb.t42.v0_6.LegalEntityType;

public abstract class AbstractPageDE extends AbstractAppWebPage
{
  /**
   * DE/DO data
   *
   * @author Philip Helger
   */
  @Immutable
  protected static class Agent
  {
    private final String m_sPID;
    private final String m_sName;
    private final String m_sCountryCode;

    public Agent (@Nonnull @Nonempty final String sPID,
                  @Nonnull @Nonempty final String sName,
                  @Nonnull @Nonempty final String sCountryCode)
    {
      ValueEnforcer.notEmpty (sPID, "PID");
      ValueEnforcer.notEmpty (sName, "Name");
      ValueEnforcer.notEmpty (sCountryCode, "CountryCode");
      m_sPID = sPID;
      m_sName = sName;
      m_sCountryCode = sCountryCode;
    }

    @Nonnull
    @Nonempty
    public String getPID ()
    {
      return m_sPID;
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
      private String m_sPID;
      private String m_sName;
      private String m_sCountryCode;

      public Builder ()
      {}

      @Nonnull
      public Builder pid (@Nullable final String s)
      {
        m_sPID = s;
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
        return new Agent (m_sPID, m_sName, m_sCountryCode);
      }
    }
  }

  @Nonnull
  @Nonempty
  protected static final String getTargetURLTestDR (@Nullable final EPatternType ePattern)
  {
    final String sBaseUrl = AppConfig.getDRBaseUrl ();
    if (ePattern == EPatternType.IM)
      return sBaseUrl + EDemoDocument.IM_REQ_DE_DR.getRelativeURL ();
    if (ePattern == EPatternType.USI)
      return sBaseUrl + EDemoDocument.USI_REQ_DE_DR.getRelativeURL ();
    throw new IllegalStateException ("Unsupported pattern " + ePattern);
  }

  protected final EPatternType m_ePattern;
  protected final String TARGET_URL_TEST_DR;

  public AbstractPageDE (@Nonnull @Nonempty final String sID,
                         @Nonnull @Nonempty final String sDisplayName,
                         @Nonnull final EPatternType ePattern)
  {
    super (sID, sDisplayName);
    m_ePattern = ePattern;
    TARGET_URL_TEST_DR = getTargetURLTestDR (ePattern);
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
  protected static IHCNode _createNaturalPerson (@Nonnull final NaturalPersonIdentifierType aNP,
                                                 @Nonnull final Locale aDisplayLocale)
  {
    final BootstrapTable aTable2 = new BootstrapTable (HCCol.fromString ("170"), HCCol.star ());
    aTable2.addBodyRow ().addCell ("Person Identifier:").addCell (_code (aNP.getPersonIdentifier ()));
    aTable2.addBodyRow ().addCell ("First Name:").addCell (_text (aNP.getFirstNameValue ()));
    aTable2.addBodyRow ().addCell ("Family Identifier:").addCell (_text (aNP.getFamilyNameValue ()));
    aTable2.addBodyRow ()
           .addCell ("Date of Birth:")
           .addCell (_text (PDTToString.getAsString (aNP.getDateOfBirthLocal (), aDisplayLocale)));
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
  protected static IHCNode _createDRS (@Nonnull final DataRequestSubjectCVType aDRS,
                                       @Nonnull final Locale aDisplayLocale)
  {
    if (aDRS == null)
      return _text (null);

    final BootstrapTable aTable = new BootstrapTable (HCCol.fromString ("120"), HCCol.star ());
    aTable.addHeaderRow ().addCell ("Field").addCell ("Value");

    if (aDRS.getDataSubjectPerson () != null)
    {
      aTable.addBodyRow ()
            .addCell ("Natural Person")
            .addCell (_createNaturalPerson (aDRS.getDataSubjectPerson (), aDisplayLocale));
    }
    if (aDRS.getDataSubjectCompany () != null)
    {
      aTable.addBodyRow ().addCell ("Company").addCell (_createLegalPerson (aDRS.getDataSubjectCompany ()));
    }
    if (aDRS.getDataSubjectRepresentative () != null)
    {
      aTable.addBodyRow ()
            .addCell ("Representative")
            .addCell (_createNaturalPerson (aDRS.getDataSubjectRepresentative (), aDisplayLocale));
    }
    return aTable;
  }

  @Nonnull
  private static IHCNode _createDBAActivity (@Nonnull final ActivityType a)
  {
    final HCDiv aNaceCodes = new HCDiv ();
    if (a.hasNaceCodeEntries ())
      aNaceCodes.addChild ("NACE codes: ")
                .addChild (StringHelper.imploder ().source (a.getNaceCode ()).separator (", ").build ());

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
  private static IHCNode _createCE_DBA (@Nonnull final LegalEntityType aLegalEntity,
                                        @Nonnull final Locale aDisplayLocale)
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
      aTable.addBodyRow ()
            .addCell ("Company Activity:")
            .addCell (_createDBAActivity (aLegalEntity.getCompanyActivity ()));
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
                                           aLegalEntity.getHasBranch ()
                                                       .getBranchName ()
                                                       .getLegalEntityLegalName ()
                                                       .getValue ()));
      aNL.addChild (new HCDiv ().addChild ("EUID: ").addChild (_code (aLegalEntity.getHasBranch ().getBranchEUID ())));
      if (aLegalEntity.getHasBranch ().getBranchActivity () != null)
        aNL.addChild (new HCDiv ().addChild ("Activity: ")
                                  .addChild (_createDBAActivity (aLegalEntity.getHasBranch ().getBranchActivity ())));
      if (aLegalEntity.getHasBranch ().getBranchRegistredAddress () != null)
        aNL.addChild (new HCDiv ().addChild ("Registered Address: ")
                                  .addChild (_createDBAAddresss (aLegalEntity.getHasBranch ()
                                                                             .getBranchRegistredAddress ())));
      if (aLegalEntity.getHasBranch ().getBranchPostalAddress () != null)
        aNL.addChild (new HCDiv ().addChild ("Postal Address: ")
                                  .addChild (_createDBAAddresss (aLegalEntity.getHasBranch ()
                                                                             .getBranchPostalAddress ())));
      aTable.addBodyRow ().addCell ("Branch:").addCell (aNL);
    }

    return aTable;
  }

  @Nonnull
  protected static IHCNode _createCE (@Nonnull final CanonicalEvidenceType aCanonicalEvidence,
                                      @Nonnull final Locale aDisplayLocale)
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
                                             @Nonnull final ResponseExtractMultiEvidenceType aResponseObj)
  {
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    final BootstrapViewForm aTable = new BootstrapViewForm ();
    aTable.setSplitting (BootstrapGridSpec.create (-1, -1, -1, 2, 2), BootstrapGridSpec.create (-1, -1, -1, 10, 10));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Request ID")
                                                  .setCtrl (_code (aResponseObj.getRequestId ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Time stamp")
                                                  .setCtrl (_text (PDTToString.getAsString (aResponseObj.getTimeStamp (),
                                                                                            aDisplayLocale))));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator")
                                                  .setCtrl (_createAgent (aResponseObj.getDataEvaluator ())));
    aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner")
                                                  .setCtrl (_createAgent (aResponseObj.getDataOwner ())));

    for (final ResponseExtractEvidenceItemType aItem : aResponseObj.getResponseExtractEvidenceItem ())
    {
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject")
                                                    .setCtrl (_createDRS (aItem.getDataRequestSubject (),
                                                                          aDisplayLocale)));
      aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type ID")
                                                    .setCtrl (_code (aItem.getCanonicalEvidenceTypeId ())));
      if (aItem.getCanonicalEvidence () != null)
      {
        // TODO
        aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence")
                                                      .setCtrl (_createCE (aItem.getCanonicalEvidence (),
                                                                           aDisplayLocale)));
      }
      if (aItem.hasDomesticEvidenceEntries ())
      {
        // TODO
        aTable.addFormGroup (new BootstrapFormGroup ().setLabel ("Domestic Evidences")
                                                      .setCtrl (_text (aItem.getDomesticEvidenceCount () +
                                                                       " present, but not shown yet")));
      }
    }
    return aTable;
  }
}
