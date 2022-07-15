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
package eu.de4a.demoui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.config.ConfigFactory;
import com.helger.config.IConfig;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import eu.de4a.demoui.pub.MenuPublic;

/**
 * This class provides access to the settings as contained in the
 * <code>webapp.properties</code> file.
 *
 * @author Philip Helger
 */
public final class AppConfig extends AbstractGlobalSingleton
{
  /** The name of the file containing the settings */
  private static final IConfig CF = ConfigFactory.getDefaultConfig ();

  @Deprecated
  @UsedViaReflection
  private AppConfig ()
  {}

  @Nonnull
  public static IConfig getConfig ()
  {
    return CF;
  }

  @Nullable
  public static String getGlobalDebug ()
  {
    return getConfig ().getAsString ("global.debug");
  }

  @Nullable
  public static String getGlobalProduction ()
  {
    return getConfig ().getAsString ("global.production");
  }

  @Nullable
  public static String getDataPath ()
  {
    return getConfig ().getAsString ("webapp.datapath");
  }

  public static boolean isCheckFileAccess ()
  {
    return getConfig ().getAsBoolean ("webapp.checkfileaccess", true);
  }

  @Nullable
  public static String getPublicURL ()
  {
    return getConfig ().getAsString ("webapp.publicurl");
  }

  public static boolean isStatusEnabled ()
  {
    return getConfig ().getAsBoolean ("webapp.status.enabled", true);
  }

  public static boolean isKafkaEnabled ()
  {
    return getConfig ().getAsBoolean ("de4a.kafka.enabled", false);
  }

  @Nullable
  public static String getKafkaEndpoint ()
  {
    return getConfig ().getAsString ("de4a.kafka.url");
  }

  @Nullable
  public static String getDRBaseUrl ()
  {
    return getConfig ().getAsString ("webapp.dr.baseurl");
  }

  /**
   * @return The participant ID that should be used for the DE. This PID must be
   *         registered in an SMP to receive something. For localhost testing,
   *         the identifier
   *         <code>iso6523-actorid-upis::9999:demoui-localhost-it2</code> should
   *         be used. It assumes a Connector running on
   *         <code>localhost:8080</code> (the final certificate choice has not
   *         been made).
   */
  @Nullable
  public static String getDEParticipantID ()
  {
    return getConfig ().getAsString ("webapp.de.pid");
  }

  @Nullable
  public static String getDEXmlWriteTo ()
  {
    return getConfig ().getAsString ("webapp.de.file.xml");
  }

  @Nonnull
  @Nonempty
  public static String getDataEvaluatorURL ()
  {
    // No additional parameter
    return getPublicURL () + "/public/menuitem-" + MenuPublic.MENU_DE_CHECK_USI_EVIDENCE;
  }
}
