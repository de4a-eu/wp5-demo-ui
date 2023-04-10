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

import java.time.Month;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.string.StringHelper;

public enum EMockDataOwner implements IHasID <String>, IHasDisplayName
{
  // T4.1
  T41_ES (EPilot.T41_STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:ess2833002e-mock-it2",
          "ES",
          "(MPTFP-SGAD) Secretaría General de Administración Digital",
          MDSPerson.builder ()
                   .id ("53377873W")
                   .firstName ("Francisco José")
                   .familyName ("Aragó Monzonís")
                   .birthday (1984, Month.JULY, 24)
                   .build (),
          null),
  T41_PT (EPilot.T41_STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:pt990000101-it2",
          "PT",
          "Portuguese IST, University of Lisbon",
          MDSPerson.builder ()
                   .id ("123456789")
                   .firstName ("Alice")
                   .familyName ("Alves")
                   .birthday (1997, Month.JANUARY, 1)
                   .build (),
          null),
  T41_SI (EPilot.T41_STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:si000000016-it2",
          "SI",
          "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
          MDSPerson.builder ()
                   .id ("123456")
                   .firstName ("Marjeta")
                   .familyName ("Maček")
                   .birthday (1999, Month.SEPTEMBER, 16)
                   .build (),
          null),
  // T4.2
  T42_AT (EPilot.T42_DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:at000000271-mock-it2",
          "AT",
          "(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort",
          null,
          MDSCompany.builder ().id ("???").name ("Carl-Markus Piswanger e.U.").build ()),
  T42_SE (EPilot.T42_DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:se000000013-mock-it2",
          "SE",
          "(BVE) BOLAGSVERKET (Companies Registration Office)",
          null,
          MDSCompany.builder ().id ("5591674170").name ("Företag Ett AB").build ()),
  T42_RO (EPilot.T42_DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:ro000000006-it2",
          "RO",
          "(ORNC) Oficiul National B22 Al Registrului Comertului",
          null,
          MDSCompany.builder ().id ("J40/12487/1998").name ("ELVILA SA").build ()),
  T42_NL (EPilot.T42_DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:nl990000106-it2",
          "NL",
          "(KVK) Chamber of Commerce of Netherlands",
          null,
          MDSCompany.builder ().id ("90000471").name ("Regional Tris-ice Coöperatie").build ()),
  // T4.3
  T43_ES (EPilot.T43_MOVING_ABROAD,
          "iso6523-actorid-upis::9999:ess2833002e-it1",
          "ES",
          "(MPTFP-SGAD) Secretaría General de Administración Digital",
          MDSPerson.builder ()
                   .id ("99999142H")
                   .firstName ("Nombre")
                   .familyName ("ApellidoPrimero ApellidoSegundo")
                   .birthday (1984, Month.JULY, 24)
                   .build (),
          null),
  T43_PT (EPilot.T43_MOVING_ABROAD,
          "iso6523-actorid-upis::9999:pt000000026-mock-it2",
          "PT",
          "(AMA IP) Agencia para a Modernizacao Administrativa IP (Administration Modernization Agency)",
          MDSPerson.builder ()
                   .id ("12345678")
                   .firstName ("Stavros")
                   .familyName ("Karakolis")
                   .birthday (1987, Month.DECEMBER, 17)
                   .build (),
          null),
  MOCK_DO_MA (EPilot.T43_MOVING_ABROAD,
              "iso6523-actorid-upis::9999:mock-do-localhost-it2",
              "ES",
              "Mocked DO (Localhost)",
              MDSPerson.builder ()
                       .id ("87654320")
                       .firstName ("Stavros")
                       .familyName ("Karakolis")
                       .birthday (1987, Month.JANUARY, 1)
                       .build (),
              MDSCompany.builder ().id ("87654320").name ("Acme Mock Limited").build ()),
  MOCK_DO_SA (EPilot.T41_STUDYING_ABROAD,
              "iso6523-actorid-upis::9999:mock-do-localhost-it2",
              "ES",
              "Mocked DO (Localhost)",
              MDSPerson.builder ()
                       .id ("87654320")
                       .firstName ("Stavros")
                       .familyName ("Karakolis")
                       .birthday (1987, Month.JANUARY, 1)
                       .build (),
              MDSCompany.builder ().id ("87654320").name ("Acme Mock Limited").build ());

  private final EPilot m_ePilot;
  private final String m_sParticipantID;
  private final String m_sCountryCode;
  private final String m_sDisplayName;
  private final MDSPerson m_aPerson;
  private final MDSCompany m_aCompany;

  EMockDataOwner (@Nonnull final EPilot ePilot,
                  @Nonnull @Nonempty final String sParticipantID,
                  @Nonnull @Nonempty final String sCountryCode,
                  @Nonnull @Nonempty final String sDisplayName,
                  @Nullable final MDSPerson aPerson,
                  @Nullable final MDSCompany aCompany)
  {
    ValueEnforcer.isTrue (aPerson != null || aCompany != null, "Person or company details must be present");
    m_ePilot = ePilot;
    m_sParticipantID = sParticipantID;
    m_sCountryCode = sCountryCode;
    m_sDisplayName = sDisplayName;
    // Either or must be set
    m_aPerson = aPerson;
    m_aCompany = aCompany;
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

  public boolean supportsPilot (@Nullable final EPilot ePilot)
  {
    return ePilot != null && m_ePilot.equals (ePilot);
  }

  @Nonnull
  @Nonempty
  public String getParticipantID ()
  {
    return m_sParticipantID;
  }

  @Nonnull
  @Nonempty
  public String getDisplayName ()
  {
    return m_sDisplayName;
  }

  @Nonnull
  @Nonempty
  public String getCountryCode ()
  {
    return m_sCountryCode;
  }

  @Nullable
  public MDSPerson getMDSPerson ()
  {
    return m_aPerson;
  }

  @Nullable
  public MDSCompany getMDSCompany ()
  {
    return m_aCompany;
  }

  @Nullable
  public static EMockDataOwner getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EMockDataOwner.class, sID);
  }

  @Nullable
  public static EMockDataOwner getFromPilotAndPIDOrNull (@Nullable final EPilot ePilot,
                                                         @Nullable final String sParticipantID)
  {
    if (ePilot != null && StringHelper.hasText (sParticipantID))
      for (final EMockDataOwner e : values ())
        if (e.m_ePilot.equals (ePilot) && e.m_sParticipantID.equals (sParticipantID))
          return e;
    return null;
  }
}
