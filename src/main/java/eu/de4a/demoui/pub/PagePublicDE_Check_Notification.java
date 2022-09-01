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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nonnull;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.css.property.ECSSProperty;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.js.IHasJSCode;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ExtendedHttpResponseException;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.button.BootstrapSubmitButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;

import eu.de4a.demoui.AppHttpClientSettings;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.ResponseMapEventNotification;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.AdditionalParameterType;
import eu.de4a.iem.core.jaxb.common.AdditionalParameterTypeType;
import eu.de4a.iem.core.jaxb.common.EventNotificationType;
import eu.de4a.iem.core.jaxb.common.ExplicitRequestType;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceLUItemType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceLUType;
import eu.de4a.iem.core.jaxb.common.RequestGroundsType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_Check_Notification extends AbstractPageDE
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_Check_Notification.class);

  private static final String FIELD_PAYLOAD = "payload";

  public PagePublicDE_Check_Notification (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Check received event", EPatternType.LOOKUP);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final ResponseMapEventNotification aMap = ResponseMapEventNotification.getInstance ();

    final String sRequestId = aMap.getFirstRequestID ();
    if (StringHelper.hasText (sRequestId))
    {
      final EventNotificationType aEventNotification = aMap.get (sRequestId);

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Getting from map the notification Id: " + aEventNotification.getNotificationId ());

      final BootstrapForm aForm = aNodeList.addAndReturnChild (getUIHandler ().createFormSelf (aWPEC).ensureID ());
      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));

      final DE4ACoreMarshaller <EventNotificationType> marshaller = DE4ACoreMarshaller.deEventNotificationMarshaller ()
                                                                                      .formatted ();

      final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                               marshaller.getAsString (aEventNotification))).setRows (25)
                                                                                                            .setCols (150)
                                                                                                            .setReadOnly (true)
                                                                                                            .addClass (CBootstrapCSS.TEXT_MONOSPACE)
                                                                                                            .addClass (CBootstrapCSS.FORM_CONTROL);

      // Fill LU Request
      final RequestExtractMultiEvidenceLUType aLURequest = new RequestExtractMultiEvidenceLUType ();
      aLURequest.setSpecificationId (aEventNotification.getSpecificationId ());
      aLURequest.setDataEvaluator (aEventNotification.getDataEvaluator ());
      aLURequest.setDataOwner (aEventNotification.getDataOwner ());
      aLURequest.setRequestId (aEventNotification.getNotificationId ());
      aLURequest.setTimeStamp (aEventNotification.getTimeStamp ());
      aEventNotification.getEventNotificationItem ().forEach (item -> {
        final RequestEvidenceLUItemType evidence = new RequestEvidenceLUItemType ();
        evidence.setRequestItemId (item.getEventId ());
        evidence.setDataRequestSubject (item.getEventSubject ());
        evidence.setCanonicalEvidenceTypeId (item.getCanonicalEventCatalogUri ());

        final AdditionalParameterType param = new AdditionalParameterType ();
        param.setLabel ("lookup");
        param.setType (AdditionalParameterTypeType.INPUT_TEXT);
        param.setValue ("lookup");
        evidence.setAdditionalParameter (new CommonsArrayList <> (param));

        evidence.setEventNotificationRef (item.getEventId ());

        final RequestGroundsType rg = new RequestGroundsType ();
        rg.setExplicitRequest (ExplicitRequestType.SDGR_14);
        evidence.setRequestGrounds (rg);
        aLURequest.addRequestEvidenceLUItem (evidence);
      });

      aForm.addChild (aTA);

      if (!aWPEC.hasAction (CPageParam.ACTION_PERFORM))
      {
        aNodeList.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES)
                                                        .addChild ("Send Lookup request to check the evidence")
                                                        .addStyle (ECSSProperty.MARGIN_TOP, "5px")
                                                        .addStyle (ECSSProperty.MARGIN_BOTTOM, "5px"));
      }
      else
      {
        aMap.remove (sRequestId);
        this.sendLURequest (aLURequest);
        aNodeList.addChildAt (0,
                              success (div ("The request was accepted by the DR. The response will be received asynchronously.")));
      }

      aNodeList.addChild (warn ("This data is not persisted - if you need this data, copy it!"));

    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("No event found");

      aNodeList.addChild (info ("Currently no received event is available"));
    }
  }

  private IHasJSCode sendLURequest (final RequestExtractMultiEvidenceLUType request)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          "DemoUI sending LU request '" +
                                            request.getRequestId () +
                                            "' to '" +
                                            m_sDefaultTargetURL +
                                            "'");

    // UNMARSHALLING
    final DE4ACoreMarshaller <RequestExtractMultiEvidenceLUType> marshaller = DE4ACoreMarshaller.drRequestTransferEvidenceLUMarshaller ();
    final byte [] aRequestBytes = marshaller.getAsBytes (request);

    final StopWatch aSW = StopWatch.createdStarted ();

    byte [] aResponseBytes = null;
    final HCNodeList aResNL = new HCNodeList ();
    final BootstrapErrorBox aErrorBox = aResNL.addAndReturnChild (error ());
    try (final HttpClientManager aHCM = HttpClientManager.create (new AppHttpClientSettings ()))
    {
      // Start HTTP POST
      final HttpPost aPost = new HttpPost (m_sDefaultTargetURL);
      aPost.setEntity (new ByteArrayEntity (aRequestBytes, ContentType.APPLICATION_XML));
      aResponseBytes = aHCM.execute (aPost, new ResponseHandlerByteArray ());
      DE4AKafkaClient.send (EErrorLevel.INFO, "Response content received (" + aResponseBytes.length + " bytes)");
    }
    catch (final ExtendedHttpResponseException ex)
    {
      aErrorBox.addChild (div ("Error sending HTTP request to ").addChild (code (m_sDefaultTargetURL)))
               .addChild (div ("HTTP response: " + ex.getMessagePartStatusLine ()));
      aResponseBytes = ex.getResponseBody ();
      if (aResponseBytes != null)
        DE4AKafkaClient.send (EErrorLevel.INFO,
                              "Error response content received (" + aResponseBytes.length + " bytes)");
    }
    catch (final IOException ex)
    {
      aErrorBox.addChild (div ("Error sending request to ").addChild (code (m_sDefaultTargetURL)))
               .addChild (AppCommonUI.getTechnicalDetailsUI (ex, true));
    }
    finally
    {
      aSW.stop ();
    }

    if (aResponseBytes != null)
    {
      // Try reading the data as the default response
      final ResponseErrorType aResponseObj = DE4ACoreMarshaller.defResponseMarshaller ().read (aResponseBytes);
      if (aResponseObj != null)
      {
        if (aResponseObj.isAck ())
        {
          DE4AKafkaClient.send (EErrorLevel.INFO, "Read response as 'ResponseErrorType' and ACK");
          aResNL.addChild (success (div ("The request was accepted by the DR. The response will be received asynchronously.")));
        }
        else
        {
          DE4AKafkaClient.send (EErrorLevel.WARN, "Read response as 'ResponseErrorType' and FAILURE");
          final HCUL aUL = new HCUL ();
          aResponseObj.getError ().forEach (x -> aUL.addItem ("[" + x.getCode () + "] " + x.getText ()));
          aErrorBox.addChild (div ("The data could not be fetched from the Data Owner")).addChild (aUL);
        }
      }
      else
      {
        // Unknown payload.
        String sFirstBytes = new String (aResponseBytes, StandardCharsets.UTF_8);
        DE4AKafkaClient.send (EErrorLevel.ERROR, "Failed to interpret synchronous response:\n" + sFirstBytes);
        if (sFirstBytes.length () > 100)
          sFirstBytes = sFirstBytes.substring (0, 100);
        aErrorBox.addChild (div ("The return data has an unsupported format. The payload starts with ").addChild (code (sFirstBytes)));
      }
    }
    aResNL.addChild (info ("It took " + aSW.getMillis () + " milliseconds to get the result"));
    return null;
  }
}
