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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test class for class {@link EDemoCanonicalEvidence}.
 *
 * @author Philip Helger
 */
public final class EDemoCanonicalEvidenceTest
{
  @Test
  public void testBasic ()
  {
    assertNotNull (EDemoCanonicalEvidence.createSA_HigherEducation_v2021_04_13 ());
    assertNotNull (EDemoCanonicalEvidence.createSA_SecondaryEducation_v2022_05_12 ());
    assertNotNull (EDemoCanonicalEvidence.createSA_Disability_v2022_05_12 ());
    assertNotNull (EDemoCanonicalEvidence.createSA_LargeFamily_v2022_05_12 ());

    assertNotNull (EDemoCanonicalEvidence.createDBA_LegalEntity_v06 ());

    assertNotNull (EDemoCanonicalEvidence.createMA_Birth_v1_7 ());
    assertNotNull (EDemoCanonicalEvidence.createMA_DomesticRegistration_v1_7 ());
    assertNotNull (EDemoCanonicalEvidence.createMA_Marriage_v1_7 ());
  }
}
