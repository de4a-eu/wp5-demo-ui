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
package eu.de4a.demoui.pub;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.photon.core.menu.IMenuItemPage;
import com.helger.photon.core.menu.IMenuTree;
import com.helger.photon.uicore.page.system.BasePageShowChildren;

/**
 * Menu for the Demo UI.
 *
 * @author Philip Helger
 */
@Immutable
public final class MenuPublic
{
  public static final String MENU_DE = "de";

  public static final String MENU_DE_IM_GUIDED = "de-im-guided";
  public static final String MENU_DE_IM_EXPERT = "de-im-expert";
  public static final String MENU_DE_IM_EXPERT_BACKWARD = "de-im-expert-bw";
  public static final String MENU_DE_USI_GUIDED = "de-usi-guided";
  public static final String MENU_DE_USI_EXPERT = "de-usi-expert";
  public static final String MENU_DE_SUBSCRIPTION_EXPERT = "de-subscription-expert";
  public static final String MENU_DE_NOTIFICATION_EXPERT = "de-notification-expert";
  public static final String MENU_DE_CHECK_EVIDENCE = "de-check-evidence";
  public static final String MENU_DE_CHECK_EVENT = "de-evidence";
  public static final String MENU_DE_CHECK_SUBSCRIPTION = "de-subscription";
  public static final String MENU_DE_RECEIVED_REDIRECTS = "de-received-redirects";

  public static final String MENU_DEMO_UI = "demoui";
  public static final String MENU_CREATE_RANDOM_MESSAGE = "createrandommsg";
  public static final String MENU_SEND_MESSAGE = "sendmessage";
  public static final String MENU_VALIDATE_MESSAGE = "validatemsg";

  private MenuPublic ()
  {}

  public static void init (@Nonnull final IMenuTree aMenuTree)
  {
    // DE stuff
    {
      final IMenuItemPage aDE = aMenuTree.createRootItem (new BasePageShowChildren <> (MENU_DE,
                                                                                       "Data Evaluator",
                                                                                       aMenuTree));
      aMenuTree.createItem (aDE, new PagePublicDE_IM_Guided (MENU_DE_IM_GUIDED));
      aMenuTree.createItem (aDE, new PagePublicDE_IM_Expert (MENU_DE_IM_EXPERT));
      aMenuTree.createItem (aDE, new PagePublicDE_IM_Expert_Backwards (MENU_DE_IM_EXPERT_BACKWARD));
      aMenuTree.createSeparator (aDE);
      aMenuTree.createItem (aDE, new PagePublicDE_USI_Guided (MENU_DE_USI_GUIDED));
      aMenuTree.createItem (aDE, new PagePublicDE_USI_Expert (MENU_DE_USI_EXPERT));
      aMenuTree.createSeparator (aDE);
      aMenuTree.createItem (aDE, new PagePublicDE_Subscription_Expert (MENU_DE_SUBSCRIPTION_EXPERT));
      aMenuTree.createItem (aDE, new PagePublicDE_Notification_Expert (MENU_DE_NOTIFICATION_EXPERT));
      aMenuTree.createSeparator (aDE);
      aMenuTree.createItem (aDE, new PagePublicDE_USI_Check_Evidence (MENU_DE_CHECK_EVIDENCE));
      aMenuTree.createItem (aDE, new PagePublicDE_Check_Notification (MENU_DE_CHECK_EVENT));
      aMenuTree.createItem (aDE, new PagePublicDE_USI_Check_Subscription(MENU_DE_CHECK_SUBSCRIPTION));
      aMenuTree.createItem (aDE, new PagePublicDE_ReceivedRedirects (MENU_DE_RECEIVED_REDIRECTS));
    }

    // Demo UI stuff
    {
      final IMenuItemPage aDemoUI = aMenuTree.createRootItem (new BasePageShowChildren <> (MENU_DEMO_UI,
                                                                                           "Supporting Actions",
                                                                                           aMenuTree));
      aMenuTree.createItem (aDemoUI, new PagePublicCreateRandomMessage (MENU_CREATE_RANDOM_MESSAGE));
      aMenuTree.createItem (aDemoUI, new PagePublicSendMessage (MENU_SEND_MESSAGE));
      aMenuTree.createItem (aDemoUI, new PagePublicValidateMessage (MENU_VALIDATE_MESSAGE));
    }

    // Set default
    aMenuTree.setDefaultMenuItemID (MENU_DE);
  }
}
