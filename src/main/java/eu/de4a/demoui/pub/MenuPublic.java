/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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

import com.helger.photon.core.menu.IMenuTree;

@Immutable
public final class MenuPublic
{
  public static final String MENU_DEMO_CLIENT = "democlient";
  public static final String MENU_EXAMPLE_MESSAGES = "examplemsgs";

  private MenuPublic ()
  {}

  public static void init (@Nonnull final IMenuTree aMenuTree)
  {
    // Common stuff
    aMenuTree.createRootItem (new PagePublicDemoClient (MENU_DEMO_CLIENT));
    aMenuTree.createRootItem (new PagePublicExampleMessage (MENU_EXAMPLE_MESSAGES));

    // Set default
    aMenuTree.setDefaultMenuItemID (MENU_DEMO_CLIENT);
  }
}
