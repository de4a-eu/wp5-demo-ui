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
package eu.de4a.demoui.pub;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.string.StringHelper;
import com.helger.commons.url.ISimpleURL;
import com.helger.commons.url.SimpleURL;
import com.helger.css.property.CCSSProperties;
import com.helger.html.hc.IHCNode;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCP;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.html.textlevel.HCSpan;
import com.helger.html.hc.html.textlevel.HCStrong;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.app.url.LinkHelper;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.breadcrumb.BootstrapBreadcrumb;
import com.helger.photon.bootstrap4.breadcrumb.BootstrapBreadcrumbProvider;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.ext.BootstrapSystemMessage;
import com.helger.photon.bootstrap4.grid.BootstrapRow;
import com.helger.photon.bootstrap4.layout.BootstrapContainer;
import com.helger.photon.bootstrap4.navbar.BootstrapNavbar;
import com.helger.photon.bootstrap4.navbar.BootstrapNavbarToggleable;
import com.helger.photon.bootstrap4.pages.BootstrapWebPageUIHandler;
import com.helger.photon.bootstrap4.uictrls.ext.BootstrapMenuItemRenderer;
import com.helger.photon.bootstrap4.uictrls.ext.BootstrapMenuItemRendererHorz;
import com.helger.photon.core.EPhotonCoreText;
import com.helger.photon.core.execcontext.ILayoutExecutionContext;
import com.helger.photon.core.execcontext.LayoutExecutionContext;
import com.helger.photon.core.html.CLayout;
import com.helger.photon.core.menu.IMenuItemExternal;
import com.helger.photon.core.menu.IMenuItemPage;
import com.helger.photon.core.menu.IMenuObject;
import com.helger.photon.core.menu.IMenuSeparator;
import com.helger.photon.core.menu.IMenuTree;
import com.helger.photon.core.menu.MenuItemDeterminatorCallback;
import com.helger.photon.core.servlet.AbstractSecureApplicationServlet;
import com.helger.photon.core.servlet.LogoutServlet;
import com.helger.photon.security.login.LoggedInUserManager;
import com.helger.photon.security.user.IUser;
import com.helger.photon.security.util.SecurityHelper;
import com.helger.photon.uicore.html.twitter.HCTweet;
import com.helger.photon.uicore.page.IWebPage;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xservlet.forcedredirect.ForcedRedirectManager;

import eu.de4a.demoui.AppHelper;
import eu.de4a.demoui.ui.AppCommonUI;

/**
 * The viewport renderer (menu + content area)
 *
 * @author Philip Helger
 */
public final class LayoutAreaContentProviderPublic
{
  private static final String PARAM_HTTP_ERROR = "httpError";
  private static final String VALUE_HTTP_ERROR = "true";
  private static final String PARAM_HTTP_STATUS_CODE = "httpStatusCode";
  private static final String PARAM_HTTP_STATUS_MESSAGE = "httpStatusMessage";
  private static final String PARAM_HTTP_REQUEST_URI = "httpRequestUri";

  private LayoutAreaContentProviderPublic ()
  {}

  @Nonnull
  private static BootstrapNavbar _getNavbar (final LayoutExecutionContext aLEC)
  {
    final ISimpleURL aLinkToStartPage = aLEC.getLinkToMenuItem (aLEC.getMenuTree ().getDefaultMenuItemID ());
    final Locale aDisplayLocale = aLEC.getDisplayLocale ();
    final IRequestWebScopeWithoutResponse aRequestScope = aLEC.getRequestScope ();
    final IUser aUser = LoggedInUserManager.getInstance ().getCurrentUser ();

    final BootstrapNavbar aNavbar = new BootstrapNavbar ();
    aNavbar.addBrand (new HCSpan ().addClass (AppCommonUI.CSS_CLASS_LOGO1).addChild (AppHelper.getApplicationTitle ()), aLinkToStartPage);

    final BootstrapNavbarToggleable aToggleable = aNavbar.addAndReturnToggleable ();

    if (aUser != null)
    {
      aToggleable.addAndReturnText ()
                 .addClass (CBootstrapCSS.ML_AUTO)
                 .addClass (CBootstrapCSS.MX_2)
                 .addChild ("Welcome ")
                 .addChild (new HCStrong ().addChild (SecurityHelper.getUserDisplayName (aUser, aDisplayLocale)));
      aToggleable.addChild (new BootstrapButton ().setOnClick (LinkHelper.getURLWithContext (AbstractSecureApplicationServlet.SERVLET_DEFAULT_PATH))
                                                  .addChild ("Administration")
                                                  .addClass (CBootstrapCSS.MX_2));

      aToggleable.addChild (new BootstrapButton ().setOnClick (LinkHelper.getURLWithContext (aRequestScope,
                                                                                             LogoutServlet.SERVLET_DEFAULT_PATH))
                                                  .addChild (EPhotonCoreText.LOGIN_LOGOUT.getDisplayText (aDisplayLocale))
                                                  .addClass (CBootstrapCSS.MX_2));
    }
    return aNavbar;
  }

  @Nonnull
  public static IHCNode getMenuContent (@Nonnull final LayoutExecutionContext aLEC)
  {
    // Main menu
    final IMenuTree aMenuTree = aLEC.getMenuTree ();
    final MenuItemDeterminatorCallback aCallback = new MenuItemDeterminatorCallback (aMenuTree, aLEC.getSelectedMenuItemID ());
    return BootstrapMenuItemRenderer.createSideBarMenu (aLEC, aCallback);
  }

  @SuppressWarnings ("unchecked")
  @Nonnull
  public static IHCNode getPageContent (@Nonnull final LayoutExecutionContext aLEC)
  {
    final IRequestWebScopeWithoutResponse aRequestScope = aLEC.getRequestScope ();

    // Get the requested menu item
    final IMenuItemPage aSelectedMenuItem = aLEC.getSelectedMenuItem ();

    // Resolve the page of the selected menu item (if found)
    IWebPage <WebPageExecutionContext> aDisplayPage;
    if (aSelectedMenuItem.matchesDisplayFilter ())
    {
      // Only if we have display rights!
      aDisplayPage = (IWebPage <WebPageExecutionContext>) aSelectedMenuItem.getPage ();
    }
    else
    {
      // No rights -> goto start page
      aDisplayPage = (IWebPage <WebPageExecutionContext>) aLEC.getMenuTree ().getDefaultMenuItem ().getPage ();
    }

    final WebPageExecutionContext aWPEC = new WebPageExecutionContext (aLEC, aDisplayPage);

    // Build page content: header + content
    final HCNodeList aPageContainer = new HCNodeList ();

    // System message always
    aPageContainer.addChild (BootstrapSystemMessage.createDefault ());

    // Handle 404 case here (see error404.jsp)
    if (VALUE_HTTP_ERROR.equals (aRequestScope.params ().getAsString (PARAM_HTTP_ERROR)))
    {
      final String sHttpStatusCode = aRequestScope.params ().getAsString (PARAM_HTTP_STATUS_CODE);
      final String sHttpStatusMessage = aRequestScope.params ().getAsString (PARAM_HTTP_STATUS_MESSAGE);
      final String sHttpRequestURI = aRequestScope.params ().getAsString (PARAM_HTTP_REQUEST_URI);
      aPageContainer.addChild (new BootstrapErrorBox ().addChild ("HTTP error " +
                                                                  sHttpStatusCode +
                                                                  " (" +
                                                                  sHttpStatusMessage +
                                                                  ")" +
                                                                  (StringHelper.hasText (sHttpRequestURI) ? " for request URI " +
                                                                                                            sHttpRequestURI
                                                                                                          : "")));
    }
    else
    {
      // Add the forced redirect content here
      if (aWPEC.params ().containsKey (ForcedRedirectManager.REQUEST_PARAMETER_PRG_ACTIVE))
        aPageContainer.addChild ((IHCNode) ForcedRedirectManager.getLastForcedRedirectContent (aDisplayPage.getID ()));
    }

    final String sHeaderText = aDisplayPage.getHeaderText (aWPEC);
    {
      final BootstrapRow aRow = new BootstrapRow ();
      aRow.createColumn (10).addChild (BootstrapWebPageUIHandler.INSTANCE.createPageHeader (sHeaderText));
      aRow.createColumn (2).addChild (HCTweet.createShareButton ());
      aPageContainer.addChild (aRow);
    }

    // Main fill content
    aDisplayPage.getContent (aWPEC);
    // Add result
    aPageContainer.addChild (aWPEC.getNodeList ());

    return aPageContainer;
  }

  @Nullable
  private static IHCNode _getRenderedFooterMenuObj (@Nonnull final ILayoutExecutionContext aLEC,
                                                    @Nonnull final BootstrapMenuItemRendererHorz aRenderer,
                                                    @Nullable final IMenuObject aMenuObj)
  {
    if (aMenuObj == null)
      return null;

    if (aMenuObj instanceof IMenuSeparator)
      return aRenderer.renderSeparator (aLEC, (IMenuSeparator) aMenuObj);

    if (aMenuObj instanceof IMenuItemPage)
      return aRenderer.renderMenuItemPage (aLEC, (IMenuItemPage) aMenuObj, false, false, false);

    if (aMenuObj instanceof IMenuItemExternal)
      return aRenderer.renderMenuItemExternal (aLEC, (IMenuItemExternal) aMenuObj, false, false, false);

    throw new IllegalStateException ("Unsupported menu object type: " + aMenuObj);
  }

  @Nonnull
  public static IHCNode getContent (@Nonnull final LayoutExecutionContext aLEC)
  {
    final HCNodeList ret = new HCNodeList ();

    // Header
    ret.addChild (_getNavbar (aLEC));

    final BootstrapContainer aOuterContainer = ret.addAndReturnChild (new BootstrapContainer ().setFluid (true));

    // Breadcrumbs
    {
      final BootstrapBreadcrumb aBreadcrumbs = BootstrapBreadcrumbProvider.createBreadcrumb (aLEC);
      aBreadcrumbs.addClasses (CBootstrapCSS.D_NONE, CBootstrapCSS.D_SM_BLOCK);
      aOuterContainer.addChild (aBreadcrumbs);
    }

    // Content
    {
      final HCDiv aRow = aOuterContainer.addAndReturnChild (new HCDiv ().addClass (CBootstrapCSS.D_MD_FLEX));
      final HCDiv aCol1 = aRow.addAndReturnChild (new HCDiv ().addClass (CBootstrapCSS.D_MD_FLEX));
      final HCDiv aCol2 = aRow.addAndReturnChild (new HCDiv ().addClass (CBootstrapCSS.ML_3).addClass (CBootstrapCSS.FLEX_FILL));

      // left
      // We need a wrapper span for easy AJAX content replacement
      aCol1.addClass (CBootstrapCSS.D_PRINT_NONE)
           .addChild (new HCSpan ().setID (CLayout.LAYOUT_AREAID_MENU)
                                   .addStyle (CCSSProperties.MIN_WIDTH.newValue ("15rem"))
                                   .addChild (getMenuContent (aLEC)));
      aCol1.addChild (new HCDiv ().setID (CLayout.LAYOUT_AREAID_SPECIAL));

      // content
      aCol2.addChild (getPageContent (aLEC));
    }

    // Footer
    {
      final BootstrapContainer aFooter = new BootstrapContainer ().setFluid (true).setID (CLayout.LAYOUT_AREAID_FOOTER);
      aFooter.addClass (CBootstrapCSS.BG_LIGHT);
      aFooter.addClass (CBootstrapCSS.PT_3);
      aFooter.addClass (CBootstrapCSS.PB_1);
      aFooter.addClass (CBootstrapCSS.MT_3);

      aFooter.addChild (new HCP ().addChild ("This is a Demo page for DE4A internal develoment." + " The official DE4A website is ")
                                  .addChild (new HCA (new SimpleURL ("https://www.de4a.eu")).addChild ("www.de4a.eu").setTargetBlank ()));

      ret.addChild (aFooter);
    }

    return ret;
  }
}
