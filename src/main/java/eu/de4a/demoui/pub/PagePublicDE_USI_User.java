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

import com.helger.commons.annotation.Nonempty;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.uicore.page.WebPageExecutionContext;

public class PagePublicDE_USI_User extends AbstractPageDE4ARequest
{
  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicDE_USI_User (@Nonnull @Nonempty final String sID)
  {
    super (sID, "USI Exchange (User)");
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();

    // TODO
    aNodeList.addChild (info ("TODO"));
  }
}
