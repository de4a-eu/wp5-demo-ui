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
import eu.de4a.iem.core.jaxb.common.ResponseEventSubscriptionType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;

/**
 * This class contains all instances of the {@link EventNotificationType}.
 *
 * @author Philip Helger
 */
public final class ResponseMapEventSubscription extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ResponseMapEventSubscription.class);

  @GuardedBy ("m_aRWLock")
  private final ICommonsMap <String, ResponseEventSubscriptionType> m_aMap = new CommonsHashMap <> ();

  @Deprecated
  @UsedViaReflection
  public ResponseMapEventSubscription ()
  {}

  @Nonnull
  public static ResponseMapEventSubscription getInstance ()
  {
    return getGlobalSingleton (ResponseMapEventSubscription.class);
  }

  public void register (@Nonnull final ResponseEventSubscriptionType aResponse)
  {
    ValueEnforcer.notNull (aResponse, "Response");
    final String sKey = aResponse.getRequestId ();
    m_aRWLock.writeLocked ( () -> {
      if (m_aMap.containsKey (sKey))
        LOGGER.warn ("Overwriting Evidence for '" + sKey + "'");
      m_aMap.put (sKey, aResponse);
    });
  }

  @Nullable
  public ResponseEventSubscriptionType get (@Nullable final String sID)
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
  
  @Nullable
  public ResponseEventSubscriptionType removeAndGet (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;
    return m_aRWLock.writeLockedGet ( () -> m_aMap.remove (sID));
  }
}
