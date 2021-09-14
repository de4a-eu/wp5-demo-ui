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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.factory.SimpleIdentifierFactory;

public enum EUseCase implements IHasID <String>, IHasDisplayName
{
  HIGHER_EDUCATION_DIPLOMA (EPilot.STUDYING_ABROAD,
                            "uc1",
                            "Application to public higher education",
                            EPatternType.USI,
                            EDataRequestSubjectType.PERSON,
                            SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                           "HigherEducationDiploma")),
  COMPANY_REGISTRATION (EPilot.DOING_BUSINESS_ABROAD,
                        "cr",
                        "Starting a business in another Member State",
                        EPatternType.IM,
                        EDataRequestSubjectType.COMPANY,
                        SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                       "CompanyRegistration")),
  BIRTH_EVIDENCE (EPilot.MOVING_ABROAD,
                  "birth",
                  "Birth Evidence",
                  EPatternType.USI,
                  EDataRequestSubjectType.PERSON,
                  SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType", "BirthEvidence")),
  DOMREG_EVIDENCE (EPilot.MOVING_ABROAD,
                   "domreg",
                   "Domicile Registration Evidence",
                   EPatternType.USI,
                   EDataRequestSubjectType.PERSON,
                   SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                  "DomicileRegistrationEvidence")),
  MARRIAGE_EVIDENCE (EPilot.MOVING_ABROAD,
                     "marriage",
                     "Marriage Evidence",
                     EPatternType.USI,
                     EDataRequestSubjectType.PERSON,
                     SimpleIdentifierFactory.INSTANCE.createDocumentTypeIdentifier ("urn:de4a-eu:CanonicalEvidenceType",
                                                                                    "MarriageEvidence"));

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
