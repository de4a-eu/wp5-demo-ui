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
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
import com.helger.commons.url.SimpleURL;
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
import com.helger.photon.icon.bootstrapicons.EBootstrapIcon;
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
import eu.de4a.iem.core.jaxb.common.RedirectUserType;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceUSIType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.kafkaclient.DE4AKafkaClient;
import eu.de4a.kafkaclient.model.ELogMessage;

public class PagePublicDE_USI_Expert extends AbstractPageDE
{
  // We're doing a DR-USI request
  private static final IDemoDocument DEMO_DOC_TYPE = EDemoDocument.USI_REQ_DE_DR;

  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_USI_Expert.class);

  private static final String FIELD_TARGET_URL = "targeturl";
  private static final String FIELD_PAYLOAD = "payload";

  private static final AjaxFunctionDeclaration CREATE_NEW_REQUEST;

  @Nonnull
  private static RequestExtractMultiEvidenceUSIType _createDemoRequest ()
  {
    RequestExtractMultiEvidenceUSIType ret;
    {
      // We want a subject person
      while (true)
      {
        ret = (RequestExtractMultiEvidenceUSIType) DEMO_DOC_TYPE.createDemoRequest ();
        if (ret.getRequestEvidenceUSIItemAtIndex (0).getDataRequestSubject ().getDataSubjectPerson () != null)
          break;
      }

      // Set default DE/DO
      if (true)
        ret.getDataEvaluator ().setAgentUrn (AppConfig.getDEParticipantID ());
      else
        ret.getDataEvaluator ().setAgentUrn (EMockDataEvaluator.T41_SI2.getParticipantID ());
      ret.getDataOwner ().setAgentUrn (EMockDataOwner.T41_ES.getParticipantID ());

      final RequestEvidenceItemType item = ret.getRequestEvidenceUSIItemAtIndex (0);
      item.setCanonicalEvidenceTypeId (EUseCase.HIGHER_EDUCATION_DIPLOMA.getDocumentTypeID ().getURIEncoded ());
      item.getDataRequestSubject ().getDataSubjectPerson ().setPersonIdentifier ("ES/SI/53377873W");
    }
    return ret;
  }

  static
  {
    CREATE_NEW_REQUEST = addAjax ( (aRequestScope, aAjaxResponse) -> {
      aAjaxResponse.text (DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()));
    });
  }

  public PagePublicDE_USI_Expert (@Nonnull @Nonempty final String sID)
  {
    super (sID, "USI Exchange (Expert)", EPatternType.USI);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final IRequestWebScopeWithoutResponse aRequestScope = aWPEC.getRequestScope ();

    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bShowForm = true;
    CompletableFuture <RedirectUserType> aFutureGetReceivedRedirect = null;
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
        final IErrorList aErrorList = DEMO_DOC_TYPE.validateMessage (sPayload);
        if (aErrorList.containsAtLeastOneError ())
        {
          aResNL.addChild (error ("The provided document is not XSD compliant"));
          for (final IError aError : aErrorList)
            if (aError.getErrorLevel ().isError ())
              aResNL.addChild (error (aError.getAsString (aDisplayLocale)));
            else
              aResNL.addChild (warn (aError.getAsString (aDisplayLocale)));
        }
        else
        {
          // Send only valid documents
          final RequestExtractMultiEvidenceUSIType aParsedRequest = (RequestExtractMultiEvidenceUSIType) DEMO_DOC_TYPE.parseMessage (sPayload);

          DE4AKafkaClient.send (EErrorLevel.INFO,
                                "DemoUI sending USI request '" +
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

            // Start polling for the result - wait at max 30 seconds
            aFutureGetReceivedRedirect = CompletableFuture.supplyAsync (new USIRedirectSupplier (aParsedRequest.getRequestId ()));

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
                aResponseObj.getError ().forEach (x -> {
                  final String sMsg = "[" + x.getCode () + "] " + x.getText ();
                  aUL.addItem (sMsg);
                  LOGGER.warn ("Response error: " + sMsg);
                });
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
          aResNL.addChild (info ("It took " + aSW.getMillis () + " milliseconds to get the response"));
        }
        aNodeList.addChild (aResNL);

        // Check for async redirect
        RedirectUserType aReceivedRedirect = null;
        try
        {
          aReceivedRedirect = aFutureGetReceivedRedirect == null ? null : aFutureGetReceivedRedirect.get ();
        }
        catch (final InterruptedException ex)
        {
          Thread.currentThread ().interrupt ();
        }
        catch (final ExecutionException ex)
        {
          // Ignore
        }

        if (aReceivedRedirect != null)
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("redirection to: " + aReceivedRedirect.getRedirectUrl ());

          aNodeList.addChild (success ().addChild (div ("Received the redirect URL ").addChild (code (aReceivedRedirect.getRedirectUrl ())))
                                        .addChild (div (new BootstrapButton ().addChild ("Go to DO Preview")
                                                                              .setIcon (EBootstrapIcon.PAPERCLIP)
                                                                              .setOnClick (new SimpleURL (aReceivedRedirect.getRedirectUrl ())))));

        }
        else
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("no redirect message found");

          // No need for UI
          aNodeList.addChild (warn ("A redirect URL was not returned during a decent time frame. Sorry."));
        }
      }
    }

    if (bShowForm)
    {
      KafkaClientWrapper.send (EErrorLevel.INFO,
                               ELogMessage.LOG_DE_PROCESS_STARTED,
                               "[USI] DE4A pilot process started");

      aNodeList.addChild (info ("This page lets you create arbitrary USI messages and send them to a WP5 Connector. This simulates the DE-DR interface."));

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
      aForm.addFormGroup (new BootstrapFormGroup ().setCtrl (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES)
                                                                                         .addChild ("Send USI request")));

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("getting the request ID, iterate map");
    }
  }
}
