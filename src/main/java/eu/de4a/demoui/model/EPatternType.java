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

/**
 * IM or USI exchange?
 *
 * @author Philip Helger
 */
public enum EPatternType implements IHasID <String>, IHasDisplayName
{
  /** Intermediation */
  IM ("im", "IM"),
  /** Intermediation for Iteration 1 */
  IM_IT1 ("im_it1", "IM (Iteration 1)"),
  /** User supported Intermediation */
  USI ("usi", "USI"),
  /** Event Subscription */
  SUBSCRIPTION ("subscription", "Event Subscription"),
  /** Event Notification */
  NOTIFICATION ("notification", "Event Notification"),
  /** Lookup **/
  LOOKUP ("lookup", "Lookup");

  private final String m_sID;
  private final String m_sDisplayName;

  EPatternType (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sDisplayName)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
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

  public boolean isUSI ()
  {
    return this == USI;
  }

  @Nullable
  public static EPatternType getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EPatternType.class, sID);
  }
}
