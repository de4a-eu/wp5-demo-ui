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
import com.helger.commons.name.IHasDisplayName;

public enum EMockDataEvaluator implements IHasID <String>, IHasDisplayName
{
  // T4.1
  T41_ES (EPilot.STUDYING_ABROAD, "iso6523-actorid-upis::9999:esq6250003h-it2", "ES", "(UJI) Universitat Jaume I de Castellón"),
  T41_PT (EPilot.STUDYING_ABROAD, "iso6523-actorid-upis::9999:pt990000101-it2", "PT", "Portuguese IST, University of Lisbon"),
  T41_SI2 (EPilot.STUDYING_ABROAD, "iso6523-actorid-upis::9999:si000000018-it2", "SI", "(JSI) Institut Jozef Stefan"),
  // T4.2
  T42_AT (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:at000000271-it2",
          "AT",
          "(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort"),
  T42_SE (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:se000000013-it2",
          "SE",
          "(BVE) BOLAGSVERKET (Companies Registration Office)"),
  T42_RO (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:ro000000006-it2",
          "RO",
          "(ORNC) Oficiul National B22 Al Registrului Comertului"),
  T42_NL (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:nl000000024-it2",
          "NL",
          "(RVO) Rijksdienst voor Ondernemend Nederland (Netherlands Enterprise Agency)"),
  // T4.3
  T43_LU (EPilot.MOVING_ABROAD,
          "iso6523-actorid-upis::9999:lu000000025-it2",
          "LU",
          "(CTIE) Centre des Technologies de l'Information de l'Etat (State Information Technology Center)");

  private final EPilot m_ePilot;
  private final String m_sParticipantID;
  private final String m_sCountryCode;
  private final String m_sDisplayName;

  EMockDataEvaluator (@Nonnull final EPilot ePilot,
                      @Nonnull @Nonempty final String sParticipantID,
                      @Nonnull @Nonempty final String sCountryCode,
                      @Nonnull @Nonempty final String sDisplayName)
  {
    m_ePilot = ePilot;
    m_sParticipantID = sParticipantID;
    m_sCountryCode = sCountryCode;
    m_sDisplayName = sDisplayName;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_ePilot.getID () + "-" + m_sParticipantID;
  }

  @Nonnull
  public EPilot getPilot ()
  {
    return m_ePilot;
  }

  @Nonnull
  @Nonempty
  public String getParticipantID ()
  {
    return m_sParticipantID;
  }

  @Nonnull
  @Nonempty
  public String getCountryCode ()
  {
    return m_sCountryCode;
  }

  @Nonnull
  @Nonempty
  public String getDisplayName ()
  {
    return m_sDisplayName;
  }

  public boolean supportsPilot (@Nullable final EPilot ePilot)
  {
    return ePilot != null && m_ePilot.equals (ePilot);
  }
}
