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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import eu.de4a.iem.core.jaxb.common.RedirectUserType;

/**
 * This class contains all instances of the {@link RedirectUserType}. This
 * stores the Post-Redirect-Get response for the USI-back-redirection.
 *
 * @author Philip Helger
 */
public final class ResponseMapRedirect extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseMapRedirect.class);
  private final ICommonsMap <String, RedirectUserType> m_aMap = new CommonsHashMap <> ();

  @Deprecated
  @UsedViaReflection
  public ResponseMapRedirect ()
  {}

  @Nonnull
  public static ResponseMapRedirect getInstance ()
  {
    return getGlobalSingleton (ResponseMapRedirect.class);
  }

  public void register (@Nonnull final RedirectUserType aResponse)
  {
    ValueEnforcer.notNull (aResponse, "Response");
    final String sKey = aResponse.getRequestId ();
    m_aRWLock.writeLocked ( () -> {
      if (m_aMap.containsKey (sKey))
        LOGGER.warn ("Overwriting Response Redirect URL for '" + sKey + "'");
      m_aMap.put (sKey, aResponse);
    });
  }

  @Nullable
  public RedirectUserType removeAndGet (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;
    return m_aRWLock.readLockedGet ( () -> m_aMap.remove (sID));
  }

  @Nonnull
  @ReturnsMutableCopy
  public ICommonsMap <String, RedirectUserType> getAll ()
  {
    return m_aRWLock.readLockedGet (m_aMap::getClone);
  }
}
