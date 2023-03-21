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

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.html.hc.html.tabular.HCRow;
import com.helger.html.hc.html.textlevel.HCA;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonToolbar;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.bootstrap4.uictrls.datatables.BootstrapDataTables;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.photon.uictrls.datatables.column.DTCol;

import eu.de4a.demoui.model.ResponseMapRedirect;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.core.jaxb.common.RedirectUserType;

public class PagePublicDE_ReceivedRedirects extends AbstractAppWebPage
{
  public PagePublicDE_ReceivedRedirects (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Received Redirects");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();

    {
      final BootstrapButtonToolbar aToolbar = aNodeList.addAndReturnChild (new BootstrapButtonToolbar (aWPEC));
      aToolbar.addChild (new BootstrapButton ().setIcon (EDefaultIcon.REFRESH)
                                               .setOnClick (aWPEC.getSelfHref ())
                                               .addChild ("Refresh"));
    }

    final BootstrapTable aTable = new BootstrapTable (new DTCol ("Request ID"),
                                                      new DTCol ("DO"),
                                                      new DTCol ("Redirect URL")).setID (getID ());
    final ResponseMapRedirect aMap = ResponseMapRedirect.getInstance ();
    for (final Map.Entry <String, RedirectUserType> aEntry : aMap.getAll ().entrySet ())
    {
      final RedirectUserType aRedirect = aEntry.getValue ();
      final HCRow aRow = aTable.addBodyRow ();
      aRow.addCell (aEntry.getKey ());
      aRow.addCell (aRedirect.getDataOwner ().getAgentNameValue ());
      aRow.addCell (HCA.createLinkedWebsite (aRedirect.getRedirectUrl ()));
    }

    if (aTable.hasBodyRows ())
    {
      aNodeList.addChild (aTable).addChild (BootstrapDataTables.createDefaultDataTables (aWPEC, aTable));
    }
    else
      aNodeList.addChild (info ("No redirects have been found yet"));
  }
}
