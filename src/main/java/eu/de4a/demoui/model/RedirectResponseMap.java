package eu.de4a.demoui.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.string.StringHelper;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import eu.de4a.iem.jaxb.common.types.ResponseUserRedirectionType;

public class RedirectResponseMap extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RedirectResponseMap.class);
  private final ICommonsMap <String, ResponseUserRedirectionType> m_aMap = new CommonsHashMap <> ();

  @Deprecated
  @UsedViaReflection
  public RedirectResponseMap ()
  {}

  @Nonnull
  public static RedirectResponseMap getInstance ()
  {
    return getGlobalSingleton (RedirectResponseMap.class);
  }

  public void register (@Nonnull final ResponseUserRedirectionType aResponse)
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
  public ResponseUserRedirectionType get (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;
    return m_aRWLock.readLockedGet ( () -> m_aMap.get (sID));
  }

  @Nullable
  public ResponseUserRedirectionType getAndRemove (@Nullable final String sID)
  {
    if (StringHelper.hasNoText (sID))
      return null;
    return m_aRWLock.writeLockedGet ( () -> m_aMap.remove (sID));
  }
}
