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
package eu.de4a.demoui.ui;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.lang.ClassHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.type.ITypedObject;
import com.helger.commons.url.ISimpleURL;
import com.helger.html.css.DefaultCSSClassProvider;
import com.helger.html.css.ICSSClassProvider;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.hc.impl.HCTextNode;
import com.helger.html.jquery.JQueryAjaxBuilder;
import com.helger.html.jscode.JSAssocArray;
import com.helger.photon.bootstrap4.ext.BootstrapSystemMessage;
import com.helger.photon.bootstrap4.pages.BootstrapPagesMenuConfigurator;
import com.helger.photon.bootstrap4.uictrls.datatables.BootstrapDataTables;
import com.helger.photon.core.menu.IMenuObject;
import com.helger.photon.core.requestparam.RequestParameterHandlerURLPathNamed;
import com.helger.photon.core.requestparam.RequestParameterManager;
import com.helger.photon.security.role.IRole;
import com.helger.photon.security.user.IUser;
import com.helger.photon.security.usergroup.IUserGroup;
import com.helger.photon.security.util.SecurityHelper;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.page.IWebPageExecutionContext;
import com.helger.photon.uictrls.datatables.DataTablesLengthMenu;
import com.helger.photon.uictrls.datatables.EDataTablesFilterType;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTables;
import com.helger.photon.uictrls.datatables.ajax.AjaxExecutorDataTablesI18N;
import com.helger.photon.uictrls.datatables.plugins.DataTablesPluginSearchHighlight;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.CAjax;

@Immutable
public final class AppCommonUI
{
  public static final ICSSClassProvider CSS_CLASS_LOGO1 = DefaultCSSClassProvider.create ("logo1");
  public static final ICSSClassProvider CSS_CLASS_LOGO2 = DefaultCSSClassProvider.create ("logo2");

  private static final DataTablesLengthMenu LENGTH_MENU = new DataTablesLengthMenu ().addItem (25).addItem (50).addItem (100).addItemAll ();
  private static final Logger LOGGER = LoggerFactory.getLogger (AppCommonUI.class);

  private AppCommonUI ()
  {}

  public static void init ()
  {
    RequestParameterManager.getInstance ().setParameterHandler (new RequestParameterHandlerURLPathNamed ());

    BootstrapDataTables.setConfigurator ( (aLEC, aTable, aDataTables) -> {
      final IRequestWebScopeWithoutResponse aRequestScope = aLEC.getRequestScope ();
      aDataTables.setAutoWidth (false)
                 .setLengthMenu (LENGTH_MENU)
                 .setAjaxBuilder (new JQueryAjaxBuilder ().url (CAjax.DATATABLES.getInvocationURL (aRequestScope))
                                                          .data (new JSAssocArray ().add (AjaxExecutorDataTables.OBJECT_ID,
                                                                                          aTable.getID ())))
                 .setServerFilterType (EDataTablesFilterType.ALL_TERMS_PER_ROW)
                 .setTextLoadingURL (CAjax.DATATABLES_I18N.getInvocationURL (aRequestScope), AjaxExecutorDataTablesI18N.LANGUAGE_ID)
                 .addPlugin (new DataTablesPluginSearchHighlight ());
    });
    // By default allow markdown in system message
    BootstrapSystemMessage.setDefaultUseMarkdown (true);
  }

  @Nonnull
  public static ISimpleURL getViewLink (@Nonnull final IWebPageExecutionContext aWPEC,
                                        @Nonnull @Nonempty final String sMenuItemID,
                                        @Nonnull final ITypedObject <String> aObject)
  {
    ValueEnforcer.notNull (aObject, "Object");

    return getViewLink (aWPEC, sMenuItemID, aObject.getID ());
  }

  @Nonnull
  public static ISimpleURL getViewLink (@Nonnull final IWebPageExecutionContext aWPEC,
                                        @Nonnull @Nonempty final String sMenuItemID,
                                        @Nonnull final String sObjectID)
  {
    return aWPEC.getLinkToMenuItem (sMenuItemID)
                .add (CPageParam.PARAM_ACTION, CPageParam.ACTION_VIEW)
                .add (CPageParam.PARAM_OBJECT, sObjectID);
  }

  @Nonnull
  public static IHCNode createViewLink (@Nonnull final IWebPageExecutionContext aWPEC, @Nullable final ITypedObject <String> aObject)
  {
    return createViewLink (aWPEC, aObject, null);
  }

  @Nonnull
  public static IHCNode createViewLink (@Nonnull final IWebPageExecutionContext aWPEC,
                                        @Nullable final ITypedObject <String> aObject,
                                        @Nullable final String sDisplayName)
  {
    if (aObject == null)
      return HCTextNode.createOnDemand (sDisplayName);

    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    if (aObject instanceof IRole)
    {
      final IRole aTypedObj = (IRole) aObject;
      final String sRealDisplayName = sDisplayName != null ? sDisplayName : aTypedObj.getName ();
      final String sMenuItemID = BootstrapPagesMenuConfigurator.MENU_ADMIN_SECURITY_ROLE;
      final IMenuObject aObj = aWPEC.getMenuTree ().getItemDataWithID (sMenuItemID);
      if (aObj != null && aObj.matchesDisplayFilter ())
        return new HCA (getViewLink (aWPEC, sMenuItemID, aTypedObj)).addChild (sRealDisplayName)
                                                                    .setTitle ("Show details of role '" + sRealDisplayName + "'");
      return new HCTextNode (sRealDisplayName);
    }

    if (aObject instanceof IUser)
    {
      final IUser aTypedObj = (IUser) aObject;
      final String sRealDisplayName = sDisplayName != null ? sDisplayName : SecurityHelper.getUserDisplayName (aTypedObj, aDisplayLocale);
      final String sMenuItemID = BootstrapPagesMenuConfigurator.MENU_ADMIN_SECURITY_USER;
      final IMenuObject aObj = aWPEC.getMenuTree ().getItemDataWithID (sMenuItemID);
      if (aObj != null && aObj.matchesDisplayFilter ())
        return new HCA (getViewLink (aWPEC, sMenuItemID, aTypedObj)).addChild (sRealDisplayName)
                                                                    .setTitle ("Show details of user '" + sRealDisplayName + "'");
      return new HCTextNode (sRealDisplayName);
    }
    if (aObject instanceof IUserGroup)
    {
      final IUserGroup aTypedObj = (IUserGroup) aObject;
      final String sRealDisplayName = sDisplayName != null ? sDisplayName : aTypedObj.getName ();
      final String sMenuItemID = BootstrapPagesMenuConfigurator.MENU_ADMIN_SECURITY_USER_GROUP;
      final IMenuObject aObj = aWPEC.getMenuTree ().getItemDataWithID (sMenuItemID);
      if (aObj != null && aObj.matchesDisplayFilter ())
        return new HCA (getViewLink (aWPEC, sMenuItemID, aTypedObj)).addChild (sRealDisplayName)
                                                                    .setTitle ("Show details of user group '" + sRealDisplayName + "'");
      return new HCTextNode (sRealDisplayName);
    }

    // add other types as desired
    throw new IllegalArgumentException ("Unsupported object: " + aObject);
  }

  @Nonnull
  private static String _getString (@Nonnull final Throwable t)
  {
    return StringHelper.getConcatenatedOnDemand (ClassHelper.getClassLocalName (t.getClass ()), " - ", t.getMessage ());
  }

  @Nullable
  public static HCNodeList getTechnicalDetailsUI (@Nullable final Throwable t, final boolean bLogException)
  {
    if (t == null)
      return null;

    if (bLogException)
      LOGGER.warn ("Technical details", t);

    final HCNodeList ret = new HCNodeList ();
    Throwable aCur = t;
    while (aCur != null)
    {
      if (ret.hasNoChildren ())
        ret.addChild (new HCDiv ().addChild ("Technical details: " + _getString (aCur)));
      else
        ret.addChild (new HCDiv ().addChild ("Caused by: " + _getString (aCur)));
      aCur = aCur.getCause ();
    }
    return ret;
  }

  @Nullable
  public static String getTechnicalDetailsString (@Nullable final Throwable t, final boolean bLogException)
  {
    if (t == null)
      return null;

    if (bLogException)
      LOGGER.warn ("Technical details", t);

    final StringBuilder ret = new StringBuilder ();
    Throwable aCur = t;
    while (aCur != null)
    {
      if (ret.length () == 0)
        ret.append ("Technical details: ").append (_getString (aCur));
      else
        ret.append ("\nCaused by: ").append (_getString (aCur));
      aCur = aCur.getCause ();
    }
    return ret.toString ();
  }
}
