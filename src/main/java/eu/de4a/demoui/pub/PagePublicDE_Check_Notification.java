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

import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.ResponseMapEventNotification;
import eu.de4a.demoui.model.IDemoDocument;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.EventNotificationType;

public class PagePublicDE_Check_Notification extends AbstractPageDE
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_Check_Notification.class);

  // We're doing a DR-IM request
  public static final IDemoDocument DEMO_DOC_TYPE = EDemoDocument.USI_REQ_DE_DR;

  public static final String PARAM_REQUEST_ID = "requestid";
  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicDE_Check_Notification (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Check received event", EPatternType.USI);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final ResponseMapEventNotification map = ResponseMapEventNotification.getInstance ();

    final String requestId = map.getFirstRequestID ();
    if (StringHelper.hasText (requestId))
    {
      final EventNotificationType event = map.getAndRemove (requestId);

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Getting from map the notification Id: " + event.getNotificationId ());

      final DE4ACoreMarshaller <EventNotificationType> marshaller = DE4ACoreMarshaller.deEventNotificationMarshaller ();

      final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                               prettyPrintByTransformer (marshaller.getAsString (event),
                                                                                         true))).setRows (25)
                                                                                                .setCols (150)
                                                                                                .setReadOnly (true)
                                                                                                .addClass (CBootstrapCSS.TEXT_MONOSPACE)
                                                                                                .addClass (CBootstrapCSS.FORM_CONTROL);

      aNodeList.addChild (aTA);
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("No event found");
    }
  }
}
