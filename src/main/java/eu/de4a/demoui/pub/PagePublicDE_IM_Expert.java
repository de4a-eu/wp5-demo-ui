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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import javax.annotation.Nonnull;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.IError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.URLHelper;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jquery.JQueryAjaxBuilder;
import com.helger.html.jscode.JSAnonymousFunction;
import com.helger.html.jscode.JSPackage;
import com.helger.html.jscode.JSVar;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ExtendedHttpResponseException;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.photon.ajax.decl.AjaxFunctionDeclaration;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.button.BootstrapSubmitButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.uicore.css.CPageParam;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.AppHttpClientSettings;
import eu.de4a.demoui.KafkaClientWrapper;
import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EMockDataEvaluator;
import eu.de4a.demoui.model.EMockDataOwner;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.EUseCase;
import eu.de4a.demoui.model.IDemoDocument;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceIMType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.kafkaclient.DE4AKafkaClient;
import eu.de4a.kafkaclient.model.ELogMessage;

public class PagePublicDE_IM_Expert extends AbstractPageDE
{
  // We're doing a DR-IM request
  private static final IDemoDocument DEMO_DOC_TYPE = EDemoDocument.IM_REQ_DE_DR;

  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_Expert.class);
  private static final String FIELD_TARGET_URL = "targeturl";
  private static final String FIELD_PAYLOAD = "payload";

  private static final AjaxFunctionDeclaration CREATE_NEW_REQUEST;

  @Nonnull
  private static RequestExtractMultiEvidenceIMType _createDemoRequest ()
  {
    RequestExtractMultiEvidenceIMType aDemoRequest;
    {
      // We want a natural person
      while (true)
      {
        aDemoRequest = (RequestExtractMultiEvidenceIMType) DEMO_DOC_TYPE.createDemoRequest ();
        if (aDemoRequest.getRequestEvidenceIMItemAtIndex (0).getDataRequestSubject ().getDataSubjectPerson () != null)
          break;
      }

      if (false)
        aDemoRequest.getDataEvaluator ().setAgentUrn (AppConfig.getDEParticipantID ());
      else
        aDemoRequest.getDataEvaluator ().setAgentUrn (EMockDataEvaluator.T42_SE.getParticipantID ());
      aDemoRequest.getDataOwner ().setAgentUrn (EMockDataOwner.T43_PT.getParticipantID ());

      aDemoRequest.getRequestEvidenceIMItemAtIndex (0)
                  .setCanonicalEvidenceTypeId (EUseCase.MARRIAGE.getDocumentTypeID ().getURIEncoded ());
      aDemoRequest.getRequestEvidenceIMItemAtIndex (1)
                  .setCanonicalEvidenceTypeId (EUseCase.BIRTH.getDocumentTypeID ().getURIEncoded ());

      final RequestEvidenceItemType item = aDemoRequest.getRequestEvidenceIMItemAtIndex (0);
      if (false)
        item.setCanonicalEvidenceTypeId (EUseCase.MARRIAGE.getDocumentTypeID ().getURIEncoded ());
      item.getDataRequestSubject ().getDataSubjectPerson ().setPersonIdentifier ("PT/SE/12345678");
    }
    return aDemoRequest;
  }

  static
  {
    CREATE_NEW_REQUEST = addAjax ( (aRequestScope, aAjaxResponse) -> {
      aAjaxResponse.text (DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()));
    });
  }

  public PagePublicDE_IM_Expert (@Nonnull @Nonempty final String sID)
  {
    super (sID, "IM Exchange (Expert)", EPatternType.IM);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final IRequestWebScopeWithoutResponse aRequestScope = aWPEC.getRequestScope ();

    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bShowForm = true;
    if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
    {
      final String sTargetURL = aWPEC.params ().getAsStringTrimmed (FIELD_TARGET_URL);
      final String sPayload = aWPEC.params ().getAsStringTrimmed (FIELD_PAYLOAD);

      if (StringHelper.hasNoText (sTargetURL))
        aFormErrors.addFieldError (FIELD_TARGET_URL, "A target URL is required");
      else
        if (URLHelper.getAsURL (sTargetURL, false) == null)
          aFormErrors.addFieldError (FIELD_TARGET_URL, "The target URL must be valid URL");

      if (StringHelper.hasNoText (sPayload))
        aFormErrors.addFieldError (FIELD_PAYLOAD, "Payload must be provided");

      if (aFormErrors.isEmpty ())
      {
        final HCNodeList aResNL = new HCNodeList ();

        // Check if document is valid
        final IErrorList aEL = DEMO_DOC_TYPE.validateMessage (sPayload);
        if (aEL.containsAtLeastOneError ())
        {
          aResNL.addChild (error ("The provided document is not XSD compliant"));
          for (final IError e : aEL)
            if (e.getErrorLevel ().isError ())
              aResNL.addChild (error (e.getAsString (aDisplayLocale)));
            else
              aResNL.addChild (warn (e.getAsString (aDisplayLocale)));
        }
        else
        {
          // Send only valid documents
          final RequestExtractMultiEvidenceIMType aParsedRequest = (RequestExtractMultiEvidenceIMType) DEMO_DOC_TYPE.parseMessage (sPayload);

          DE4AKafkaClient.send (EErrorLevel.INFO,
                                "DemoUI sending IM request '" +
                                                  aParsedRequest.getRequestId () +
                                                  "' to '" +
                                                  sTargetURL +
                                                  "'");

          final StopWatch aSW = StopWatch.createdStarted ();

          byte [] aResponseBytes = null;
          final BootstrapErrorBox aErrorBox = aResNL.addAndReturnChild (error ());
          try (final HttpClientManager aHCM = HttpClientManager.create (new AppHttpClientSettings ()))
          {
            if (LOGGER.isInfoEnabled ())
              LOGGER.info ("HTTP POST to '" + sTargetURL + "'");

            // Start HTTP POST
            final HttpPost aPost = new HttpPost (sTargetURL);
            aPost.setEntity (new StringEntity (sPayload,
                                               ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
            aResponseBytes = aHCM.execute (aPost, new ResponseHandlerByteArray ());
            DE4AKafkaClient.send (EErrorLevel.INFO, "Response content received (" + aResponseBytes.length + " bytes)");
          }
          catch (final ExtendedHttpResponseException ex)
          {
            aErrorBox.addChild (div ("Error sending HTTP request to ").addChild (code (sTargetURL)))
                     .addChild (div ("HTTP response: " + ex.getMessagePartStatusLine ()));
            aResponseBytes = ex.getResponseBody ();
            if (aResponseBytes != null)
              DE4AKafkaClient.send (EErrorLevel.INFO,
                                    "Error response content received (" + aResponseBytes.length + " bytes)");
          }
          catch (final IOException ex)
          {
            aErrorBox.addChild (div ("Error sending request to ").addChild (code (sTargetURL)))
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
              if (sFirstBytes.length () > ERROR_SRC_MAX_LEN)
                sFirstBytes = sFirstBytes.substring (0, ERROR_SRC_MAX_LEN);
              aErrorBox.addChild (div ("The return data has an unsupported format. The payload starts with ").addChild (code (sFirstBytes)));
            }
          }
          aResNL.addChild (info ("It took " + aSW.getMillis () + " milliseconds to get the result"));
        }
        aNodeList.addChild (aResNL);
      }
    }

    if (bShowForm)
    {
      KafkaClientWrapper.send (EErrorLevel.INFO, ELogMessage.LOG_DE_PROCESS_STARTED, "[IM] DE4A pilot process started");

      aNodeList.addChild (info ("This page lets you create arbitrary IM messages and send them to a WP5 Connector. This simulates the DE-DR interface."));

      final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC));
      aForm.setSplitting (BootstrapGridSpec.create (-1, -1, 2, 2, 2), BootstrapGridSpec.create (-1, -1, 10, 10, 10));
      aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Target URL")
                                                   .setCtrl (new HCEdit (new RequestField (FIELD_TARGET_URL,
                                                                                           m_sDefaultTargetURL)))
                                                   .setHelpText (span ("The URL to send the request to. Use something like ").addChild (code (m_sDefaultTargetURL))
                                                                                                                             .addChild (" for the test DE4A Connector"))
                                                   .setErrorList (aFormErrors.getListOfField (FIELD_TARGET_URL)));
      {
        final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                                 DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()))).setRows (10)
                                                                                                                              .addClass (CBootstrapCSS.TEXT_MONOSPACE);
        final JSAnonymousFunction aJSAppend = new JSAnonymousFunction ();
        final JSVar aJSAppendData = aJSAppend.param ("data");
        aJSAppend.body ().add (JQuery.idRef (aTA).val (aJSAppendData));

        final JSPackage aOnClick = new JSPackage ();
        aOnClick.add (new JQueryAjaxBuilder ().url (CREATE_NEW_REQUEST.getInvocationURL (aRequestScope))
                                              .success (aJSAppend)
                                              .build ());
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Payload")
                                                     .setCtrl (aTA,
                                                               new BootstrapButton ().addChild ("Other message")
                                                                                     .setIcon (EDefaultIcon.REFRESH)
                                                                                     .setOnClick (aOnClick))
                                                     .setHelpText ("The message you want to send. By default a randomly generated message is created")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PAYLOAD)));
      }

      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Send IM request"));
    }
  }
}
