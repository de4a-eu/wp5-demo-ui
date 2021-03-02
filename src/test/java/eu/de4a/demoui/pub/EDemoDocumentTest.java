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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EDemoDocumentTest
{

  private static final Logger LOGGER = LoggerFactory.getLogger (EDemoDocumentTest.class);

  @Test
  public void testBasic ()
  {
    for (final EDemoDocument e : EDemoDocument.values ())
    {
      LOGGER.info (e.name ());
      assertSame (e, EDemoDocument.getFromIDOrNull (e.getID ()));
      for (int i = 0; i < 100; ++i)
        assertNotNull (e.getDemoMessageAsString ());
    }
  }
}