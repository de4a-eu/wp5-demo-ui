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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.StringHelper;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.model.ResponseMapEvidence;
import eu.de4a.demoui.ui.AbstractAppWebPage;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.IDE4ACanonicalEvidenceType;
import eu.de4a.iem.core.jaxb.common.ResponseExtractMultiEvidenceType;

public class PagePublicDE_USI_Check_Evidence extends AbstractAppWebPage
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_USI_Check_Evidence.class);
  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicDE_USI_Check_Evidence (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Check received evidence");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final ResponseMapEvidence aMap = ResponseMapEvidence.getInstance ();

    final String sRequestId = aMap.getFirstRequestID ();
    if (StringHelper.hasText (sRequestId))
    {
      aNodeList.addChild (warn ("This data is not persisted - if you need this data, copy it!"));

      final ResponseExtractMultiEvidenceType evidence = aMap.removeAndGet (sRequestId);
      if (evidence != null)
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Getting from map the evidence Id: " + evidence.getRequestId ());

        final DE4ACoreMarshaller <ResponseExtractMultiEvidenceType> marshaller = DE4ACoreMarshaller.deResponseTransferEvidenceMarshaller (IDE4ACanonicalEvidenceType.NONE)
                                                                                                   .formatted ();
        final String sResponse = marshaller.getAsString (evidence);

        if (StringHelper.hasText (sResponse))
        {
          final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                                   sResponse)).setRows (25)
                                                                              .setCols (150)
                                                                              .setReadOnly (true)
                                                                              .addClass (CBootstrapCSS.TEXT_MONOSPACE)
                                                                              .addClass (CBootstrapCSS.FORM_CONTROL);

          aNodeList.addChild (aTA);
        }
        else
          aNodeList.addChild (error ("Failed to serialize object:\n" + evidence));
      }
      else
        aNodeList.addChild (error ("Failed to get Evidence with ID '" + sRequestId + "' from internal map :("));
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("No evidence found");

      aNodeList.addChild (info ("Currently no received evidence is available"));
    }
  }
}
