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
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import eu.de4a.iem.core.jaxb.common.EventNotificationType;

/**
 * This class contains all instances of the {@link EventNotificationType}.
 *
 * @author Philip Helger
 */
public final class ResponseMapEventNotification extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseMapEventNotification.class);

  @GuardedBy ("m_aRWLock")
  private final ICommonsMap <String, EventNotificationType> m_aMap = new CommonsHashMap <> ();

  @Deprecated
  @UsedViaReflection
  public ResponseMapEventNotification ()
  {}

  @Nonnull
  public static ResponseMapEventNotification getInstance ()
  {
    return getGlobalSingleton (ResponseMapEventNotification.class);
  }

  public void register (@Nonnull final EventNotificationType aResponse)
  {
    ValueEnforcer.notNull (aResponse, "Response");
    final String sKey = aResponse.getNotificationId ();
    m_aRWLock.writeLocked ( () -> {
      if (m_aMap.containsKey (sKey))
        LOGGER.warn ("Overwriting Evidence for '" + sKey + "'");
      m_aMap.put (sKey, aResponse);
    });
  }

  @Nullable
  public EventNotificationType get (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;
    return m_aRWLock.readLockedGet ( () -> m_aMap.get (sID));
  }

  public void remove (@Nullable final String sID)
  {
    if (StringHelper.hasText (sID))
      m_aRWLock.writeLocked ( () -> m_aMap.remove (sID));
  }

  @Nullable
  public String getFirstRequestID ()
  {
    return m_aRWLock.readLockedGet (m_aMap::getFirstKey);
  }

  public void cleanMap ()
  {
    m_aRWLock.writeLocked (m_aMap::clear);
  }
}
