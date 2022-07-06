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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.EvidenceResponseMap;
import eu.de4a.demoui.model.IDemoDocument;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.IDE4ACanonicalEvidenceType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;

public class PagePublicDE_USI_Check_Evidence extends AbstractPageDE
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_USI_Check_Evidence.class);

  // We're doing a DR-IM request
  public static final IDemoDocument DEMO_DOC_TYPE = EDemoDocument.USI_REQ_DE_DR;

  public static final String PARAM_REQUEST_ID = "requestid";
  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicDE_USI_Check_Evidence (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Check received evidence", EPatternType.USI);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    ResponseExtractMultiEvidenceType evidence = null;
    final EvidenceResponseMap map = EvidenceResponseMap.getInstance ();

    String requestId = "";

    if (map.getM_aMap ().size () > 0)
    {
      for (final Map.Entry <String, ResponseExtractMultiEvidenceType> entry : map.getM_aMap ().entrySet ())
      {
        requestId = entry.getKey ();
      }

      evidence = map.getAndRemove (requestId);

      LOGGER.debug ("Getting from map the evidence Id: " + evidence.getRequestId ());

      final DE4ACoreMarshaller <ResponseExtractMultiEvidenceType> marshaller = DE4ACoreMarshaller.deResponseTransferEvidenceMarshaller (IDE4ACanonicalEvidenceType.NONE);

      final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                               prettyPrintByTransformer (marshaller.getAsString (evidence),
                                                                                         2,
                                                                                         true))).setRows (25)
                                                                                                .setCols (150)
                                                                                                .setReadOnly (true)
                                                                                                .addClass (CBootstrapCSS.TEXT_MONOSPACE)
                                                                                                .addClass (CBootstrapCSS.FORM_CONTROL);

      aNodeList.addChild (aTA);

    }
    else
    {
      LOGGER.debug ("No evidence found");
    }
  }
}
