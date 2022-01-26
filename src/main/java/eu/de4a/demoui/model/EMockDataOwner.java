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
  T41_ES (EPilot.STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:ess2833002e-it1",
          "ES",
          "(MPTFP-SGAD) Secretaría General de Administración Digital",
          "https://pre-smp-dr-de4a.redsara.es/de4a-mock-connector/preview/index",
          MDSPerson.builder ()
                   .id ("53377873W")
                   .firstName ("Francisco José")
                   .familyName ("Aragó Monzonís")
                   .birthday (1984, Month.JULY, 24)
                   .build (),
          null),
  T41_PT (EPilot.STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:pt990000101-it1",
          "PT",
          "Portuguese IST, University of Lisbon",
          "https://pre-smp-dr-de4a.redsara.es/de4a-mock-connector/preview/index",
          MDSPerson.builder ().id ("123456789").firstName ("Alice").familyName ("Alves").birthday (1997, Month.JANUARY, 1).build (),
          null),
  T41_SI (EPilot.STUDYING_ABROAD,
          "iso6523-actorid-upis::9999:si000000016-it1",
          "SI",
          "(MIZS) Ministrstvo za Izobrazevanje, Znanost in Sport (Ministry of Education, Science and Sport)",
          "https://pre-smp-dr-de4a.redsara.es/de4a-mock-connector/preview/index",
          MDSPerson.builder ().id ("123456").firstName ("Marjeta").familyName ("Maček").birthday (1999, Month.SEPTEMBER, 16).build (),
          null),
  // T4.2
  T42_AT (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:at000000271-it1",
          "AT",
          "(BMDW) Bundesministerium für Digitalisierung und Wirtschaftsstandort",
          null,
          null,
          MDSCompany.builder ().id ("???").name ("Carl-Markus Piswanger e.U.").build ()),
  T42_SE (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:se000000013-it1",
          "SE",
          "(BVE) BOLAGSVERKET (Companies Registration Office)",
          null,
          null,
          MDSCompany.builder ().id ("5591674170").name ("Företag Ett AB").build ()),
  T42_RO (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:ro000000006-it1",
          "RO",
          "(ORNC) Oficiul National B22 Al Registrului Comertului",
          null,
          null,
          MDSCompany.builder ().id ("J40/12487/1998").name ("ELVILA SA").build ()),
  T42_NL (EPilot.DOING_BUSINESS_ABROAD,
          "iso6523-actorid-upis::9999:nl990000106-it1",
          "NL",
          "(KVK) Chamber of Commerce of Netherlands",
          null,
          null,
          MDSCompany.builder ().id ("90000471").name ("Regional Tris-ice Coöperatie").build ()),
  // T4.3
  T43_ES (EPilot.MOVING_ABROAD,
          "iso6523-actorid-upis::9999:ess2833002e-it1",
          "ES",
          "(MPTFP-SGAD) Secretaría General de Administración Digital",
          "https://pre-smp-dr-de4a.redsara.es/de4a-mock-connector/preview/index",
          MDSPerson.builder ()
                   .id ("99999142H")
                   .firstName ("Nombre")
                   .familyName ("ApellidoPrimero ApellidoSegundo")
                   .birthday (1984, Month.JULY, 24)
                   .build (),
          null),
  T43_PT (EPilot.MOVING_ABROAD,
          "iso6523-actorid-upis::9999:pt000000026-it1",
          "PT",
          "(AMA IP) Agencia para a Modernizacao Administrativa IP (Administration Modernization Agency)",
          "https://pre-smp-dr-de4a.redsara.es/de4a-mock-connector/preview/index",
          MDSPerson.builder ().id ("12345678").firstName ("Stavros").familyName ("Karakolis").birthday (1987, Month.DECEMBER, 17).build (),
          null);

  private final EPilot m_ePilot;
  private final String m_sParticipantID;
  private final String m_sCountryCode;
  private final String m_sDisplayName;
  private String m_sUSIRedirectURL;
  private final MDSPerson m_aPerson;
  private final MDSCompany m_aCompany;

  EMockDataOwner (@Nonnull final EPilot ePilot,
                  @Nonnull @Nonempty final String sParticipantID,
                  @Nonnull @Nonempty final String sCountryCode,
                  @Nonnull @Nonempty final String sDisplayName,
                  @Nullable final String sUSIRedirectURL,
                  @Nullable final MDSPerson aPerson,
                  @Nullable final MDSCompany aCompany)
  {
    ValueEnforcer.isTrue (aPerson != null || aCompany != null, "Person or company details must be present");
    m_ePilot = ePilot;
    m_sParticipantID = sParticipantID;
    m_sCountryCode = sCountryCode;
    m_sDisplayName = sDisplayName;
    m_sUSIRedirectURL = sUSIRedirectURL;
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

  @Nullable
  public String getUSIRedirectURL ()
  {
    return m_sUSIRedirectURL;
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
  public static EMockDataOwner getFromPilotAndPIDOrNull (@Nullable final EPilot ePilot, @Nullable final String sParticipantID)
  {
    if (ePilot != null && StringHelper.hasText (sParticipantID))
      for (final EMockDataOwner e : values ())
        if (e.m_ePilot.equals (ePilot) && e.m_sParticipantID.equals (sParticipantID))
          return e;
    return null;
  }
}
