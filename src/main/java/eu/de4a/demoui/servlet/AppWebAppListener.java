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
package eu.de4a.demoui.servlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.bridge.SLF4JBridgeHandler;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.vendor.VendorInfo;
import com.helger.httpclient.HttpDebugger;
import com.helger.photon.ajax.IAjaxRegistry;
import com.helger.photon.api.IAPIRegistry;
import com.helger.photon.bootstrap4.servlet.WebAppListenerBootstrap;
import com.helger.photon.core.appid.CApplicationID;
import com.helger.photon.core.appid.PhotonGlobalState;
import com.helger.photon.core.locale.ILocaleManager;
import com.helger.photon.core.menu.MenuTree;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.AppSecurity;
import eu.de4a.demoui.CAjax;
import eu.de4a.demoui.CApp;
import eu.de4a.demoui.api.APIExecutorGetStatus;
import eu.de4a.demoui.api.DemoUIAPI;
import eu.de4a.demoui.pub.MenuPublic;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.kafkaclient.DE4AKafkaClient;
import eu.de4a.kafkaclient.DE4AKafkaSettings;

/**
 * This listener is invoked during the servlet initialization. This is basically
 * a ServletContextListener.
 *
 * @author Philip Helger
 */
public final class AppWebAppListener extends WebAppListenerBootstrap
{
  @Override
  protected String getInitParameterDebug (@Nonnull final ServletContext aSC)
  {
    return AppConfig.getGlobalDebug ();
  }

  @Override
  protected String getInitParameterProduction (@Nonnull final ServletContext aSC)
  {
    return AppConfig.getGlobalProduction ();
  }

  @Override
  protected String getDataPath (@Nonnull final ServletContext aSC)
  {
    return AppConfig.getDataPath ();
  }

  @Override
  protected boolean shouldCheckFileAccess (@Nonnull final ServletContext aSC)
  {
    return AppConfig.isCheckFileAccess ();
  }

  @Override
  protected void initGlobalSettings ()
  {
    APIExecutorGetStatus.setInitDT (getInitializationStartDT ());

    // Disable DNS caching
    HttpDebugger.setEnabled (false);

    // JUL to SLF4J
    SLF4JBridgeHandler.removeHandlersForRootLogger ();
    SLF4JBridgeHandler.install ();

    VendorInfo.setVendorName ("Philip Helger");
    VendorInfo.setVendorURL ("http://www.helger.com");
    VendorInfo.setVendorEmail ("philip@helger.com");
    VendorInfo.setVendorLocation ("Vienna, Austria");
    VendorInfo.setInceptionYear (2021);

    DE4AKafkaSettings.setKafkaEnabled (true);
    DE4AKafkaSettings.setKafkaTopic ("wp5-demoui");
    DE4AKafkaSettings.defaultProperties ().put ("bootstrap.servers", AppConfig.getKafkaEndpoint());
  }

  @Override
  protected void initLocales (@Nonnull final ILocaleManager aLocaleMgr)
  {
    aLocaleMgr.registerLocale (CApp.LOCALE_EN);
    aLocaleMgr.setDefaultLocale (CApp.DEFAULT_LOCALE);
  }

  @Override
  protected void initAjax (@Nonnull final IAjaxRegistry aAjaxRegistry)
  {
    aAjaxRegistry.registerFunction (CAjax.DATATABLES);
    aAjaxRegistry.registerFunction (CAjax.DATATABLES_I18N);
  }

  @Override
  protected void initMenu ()
  {
    // Create all menu items
    {
      final MenuTree aMenuTree = new MenuTree ();
      MenuPublic.init (aMenuTree);
      PhotonGlobalState.state (CApplicationID.APP_ID_PUBLIC).setMenuTree (aMenuTree);
    }
  }

  @Override
  protected void initSecurity ()
  {
    // Set all security related stuff
    AppSecurity.init ();
  }

  @Override
  protected void initUI ()
  {
    // UI stuff
    AppCommonUI.init ();
  }

  @Override
  protected void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    DemoUIAPI.initAPI (aAPIRegistry);
  }

  @Override
  protected void afterContextInitialized (final ServletContext aSC)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI started with public URL '" + AppConfig.getPublicURL () + "'");
  }

  @Override
  protected void beforeContextDestroyed (final ServletContext aSC)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI is shutting down");
    DE4AKafkaClient.close ();
  }
}
