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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.peppolid.IDocumentTypeIdentifier;

public enum EUseCase implements IHasID <String>, IHasDisplayName
{
  HIGHER_EDUCATION_DIPLOMA (EPilot.T41_STUDYING_ABROAD,
                            "higheredu",
                            "Application to public higher education",
                            EPatternType.USI,
                            EDataRequestSubjectType.PERSON,
                            DcngConfig.getIdentifierFactory ()
                                      .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                     "HigherEducationDiploma:2.0")),
  SECONDARY_EDUCATION_DIPLOMA (EPilot.T41_STUDYING_ABROAD,
                               "secondaryedu",
                               "Application to public secondary education",
                               EPatternType.USI,
                               EDataRequestSubjectType.PERSON,
                               DcngConfig.getIdentifierFactory ()
                                         .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                        "SecondaryEducationDiploma:1.0")),
  DISABILITY (EPilot.T41_STUDYING_ABROAD,
              "disability",
              "Application to something about Disability",
              EPatternType.USI,
              EDataRequestSubjectType.PERSON,
              DcngConfig.getIdentifierFactory ()
                        .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                       "DisabilityEvidence:1.0")),
  LARGE_FAMILY (EPilot.T41_STUDYING_ABROAD,
                "disability",
                "Application to something about Large Family",
                EPatternType.USI,
                EDataRequestSubjectType.PERSON,
                DcngConfig.getIdentifierFactory ()
                          .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                         "LargeFamilyEvidence:1.0")),
  COMPANY_REGISTRATION (EPilot.T42_DOING_BUSINESS_ABROAD,
                        "cr",
                        "Starting a business in another Member State",
                        EPatternType.IM,
                        EDataRequestSubjectType.COMPANY,
                        DcngConfig.getIdentifierFactory ()
                                  .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                 "CompanyRegistration:1.0")),

  COMPANY_REGISTRATION_IT1 (EPilot.T42_DOING_BUSINESS_ABROAD,
                            "cr",
                            "Starting a business in another Member State",
                            EPatternType.IM_IT1,
                            EDataRequestSubjectType.COMPANY,
                            DcngConfig.getIdentifierFactory ()
                                      .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                     "CompanyRegistration")),

  BIRTH (EPilot.T43_MOVING_ABROAD,
         "birth",
         "Birth Certificate",
         EPatternType.USI,
         EDataRequestSubjectType.PERSON,
         DcngConfig.getIdentifierFactory ()
                   .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                  "BirthCertificate:1.0")),
  DOMICILE_REGISTRATION (EPilot.T43_MOVING_ABROAD,
                         "domreg",
                         "Residence Registration",
                         EPatternType.USI,
                         EDataRequestSubjectType.PERSON,
                         DcngConfig.getIdentifierFactory ()
                                   .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                  "ResidenceRegistration:1.0")),
  MARRIAGE (EPilot.T43_MOVING_ABROAD,
            "marriage",
            "Marriage Registration",
            EPatternType.USI,
            EDataRequestSubjectType.PERSON,
            DcngConfig.getIdentifierFactory ()
                      .createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                     "MarriageEvidence:1.0"));

  private final EPilot m_ePilot;
  private final String m_sID;
  private final String m_sDisplayName;
  private final EPatternType m_ePatternType;
  private final EDataRequestSubjectType m_eDRSType;
  private final IDocumentTypeIdentifier m_aDocTypeID;

  EUseCase (@Nonnull final EPilot ePilot,
            @Nonnull @Nonempty final String sIDSuffix,
            @Nonnull @Nonempty final String sDisplayNameSuffix,
            @Nonnull final EPatternType ePatternType,
            @Nonnull final EDataRequestSubjectType eDRSType,
            @Nonnull final IDocumentTypeIdentifier aDocTypeID)
  {
    m_ePilot = ePilot;
    m_sID = ePilot.getID () + "-" + sIDSuffix;
    m_sDisplayName = ePilot.getDisplayName () + " - " + sDisplayNameSuffix;
    m_ePatternType = ePatternType;
    m_eDRSType = eDRSType;
    m_aDocTypeID = aDocTypeID;
  }

  /**
   * @return The underlying pilot area. Never <code>null</code>.
   */
  @Nonnull
  public EPilot getPilot ()
  {
    return m_ePilot;
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

  /**
   * @return The exchange pattern type. Never <code>null</code>.
   */
  @Nonnull
  public EPatternType getPatternType ()
  {
    return m_ePatternType;
  }

  /**
   * @return The date request subject type. Never <code>null</code>.
   */
  @Nonnull
  public EDataRequestSubjectType getDRSType ()
  {
    return m_eDRSType;
  }

  /**
   * @return The document type identifier used for the exchange. Never
   *         <code>null</code>.
   */
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
