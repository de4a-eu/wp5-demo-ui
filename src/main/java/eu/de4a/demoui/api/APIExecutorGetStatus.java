package eu.de4a.demoui.api;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.datetime.PDTWebDateHelper;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.MimeType;
import com.helger.commons.system.SystemProperties;
import com.helger.config.source.res.IConfigurationSourceResource;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.CAppVersion;

public class APIExecutorGetStatus implements IAPIExecutor
{
  private static final Logger LOGGER = LoggerFactory.getLogger (APIExecutorGetStatus.class);
  private static LocalDateTime s_aInitializationStartDT;

  public static void setInitDT (final LocalDateTime aInitializationStartDT)
  {
    s_aInitializationStartDT = aInitializationStartDT;
  }

  @Nonnull
  @ReturnsMutableCopy
  public static IJsonObject getDefaultStatusData ()
  {
    final IJsonObject aStatusData = new JsonObject ();
    aStatusData.add ("build.version", CAppVersion.BUILD_VERSION);
    aStatusData.add ("build.datetime", CAppVersion.BUILD_TIMESTAMP);
    aStatusData.add ("startup.datetime", PDTWebDateHelper.getAsStringXSD (s_aInitializationStartDT));
    aStatusData.add ("status.datetime", PDTWebDateHelper.getAsStringXSD (PDTFactory.getCurrentZonedDateTimeUTC ()));
    aStatusData.add ("java.version", SystemProperties.getJavaVersion ());
    aStatusData.add ("global.debug", GlobalDebug.isDebugMode ());
    aStatusData.add ("global.production", GlobalDebug.isProductionMode ());

    // add all configuration items to status (all except passwords)
    final ICommonsOrderedMap <String, String> aVals = new CommonsLinkedHashMap <> ();
    AppConfig.getConfig ().forEachConfigurationValueProvider ( (aCVP, nPriority) -> {
      if (aCVP instanceof IConfigurationSourceResource)
      {
        final ICommonsOrderedMap <String, String> aAll = ((IConfigurationSourceResource) aCVP).getAllConfigItems ();
        for (final Map.Entry <String, String> aEntry : aAll.entrySet ())
        {
          // Never override, because highest priority values come first
          if (!aVals.containsKey (aEntry.getKey ()))
            aVals.put (aEntry);
        }
      }
    });

    // Maintain the retrieved order
    for (final Map.Entry <String, String> aEntry : aVals.entrySet ())
    {
      final String sKey = aEntry.getKey ();
      if (sKey.contains ("password"))
        aStatusData.add (aEntry.getKey (), "***");
      else
        aStatusData.add (aEntry.getKey (), aEntry.getValue ());
    }

    return aStatusData;
  }

  public void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                         @Nonnull @Nonempty final String sPath,
                         @Nonnull final Map <String, String> aPathVariables,
                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                         @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Status information requested");

    // Build data to provide
    final IJsonObject aStatusData;
    if (AppConfig.isStatusEnabled ())
      aStatusData = getDefaultStatusData ();
    else
    {
      // Status is disabled in the configuration
      aStatusData = new JsonObject ();
      aStatusData.add ("status.enabled", false);
    }

    // Put JSON on response
    aUnifiedResponse.disableCaching ();
    aUnifiedResponse.setMimeType (new MimeType (CMimeType.APPLICATION_JSON).addParameter (CMimeType.PARAMETER_NAME_CHARSET,
                                                                                          StandardCharsets.UTF_8.name ()));
    aUnifiedResponse.setContentAndCharset (aStatusData.getAsJsonString (), StandardCharsets.UTF_8);

    if (LOGGER.isTraceEnabled ())
      LOGGER.trace ("Return status JSON: " + aStatusData.getAsJsonString ());
  }
}
