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

import javax.annotation.Nonnull;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.error.IError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.url.URLHelper;
import com.helger.css.property.ECSSProperty;
import com.helger.dcng.core.http.DcngHttpClientSettings;
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

import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EMockDataEvaluator;
import eu.de4a.demoui.model.EMockDataOwner;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.EUseCase;
import eu.de4a.demoui.model.IDemoDocument;
import eu.de4a.demoui.model.ResponseMapRedirect;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.EventSubscripRequestItemType;
import eu.de4a.iem.core.jaxb.common.LegalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.RedirectUserType;
import eu.de4a.iem.core.jaxb.common.RequestEventSubscriptionType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_Subscription_Expert extends AbstractPageDE
{
  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_Subscription_Expert.class);

  // We're doing a DR-USI request
  public static final IDemoDocument DEMO_DOC_TYPE = EDemoDocument.SUBS_REQ;

  private static final String FIELD_TARGET_URL = "targeturl";
  private static final String FIELD_PAYLOAD = "payload";
  private boolean isRequestSent = false;

  private static final AjaxFunctionDeclaration CREATE_NEW_REQUEST;

  @Nonnull
  private static RequestEventSubscriptionType _createDemoRequest ()
  {
    RequestEventSubscriptionType ret;
    {
      // We want a subject person
      while (true)
      {
        ret = (RequestEventSubscriptionType) DEMO_DOC_TYPE.createDemoRequest ();
        if (ret.getEventSubscripRequestItemAtIndex (0).getDataRequestSubject ().getDataSubjectPerson () != null)
          break;
      }
      ret.getDataEvaluator ().setAgentUrn (EMockDataEvaluator.T42_NL.getParticipantID ());
      ret.getDataOwner ().setAgentUrn (EMockDataOwner.T42_SE.getParticipantID ());

      final EventSubscripRequestItemType item = ret.getEventSubscripRequestItemAtIndex (0);
      item.setCanonicalEventCatalogUri (EUseCase.COMPANY_REGISTRATION.getDocumentTypeID ().getURIEncoded ());
      item.getDataRequestSubject().setDataSubjectPerson(null);
      item.getDataRequestSubject().setDataSubjectCompany(new LegalPersonIdentifierType());
      item.getDataRequestSubject ().getDataSubjectCompany().setLegalPersonIdentifier("NL/SE/5591674170");
      item.getDataRequestSubject ().getDataSubjectCompany().setLegalName("LegalName-1602842249");

    }
    return ret;
  }

  static
  {
    CREATE_NEW_REQUEST = addAjax ( (aRequestScope, aAjaxResponse) -> {
      aAjaxResponse.text (DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()));
    });
  }

  public PagePublicDE_Subscription_Expert (@Nonnull @Nonempty final String sID)
  {
    super (sID, "Subscription Exchange (Expert)", EPatternType.SUBSCRIPTION);
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
          final RequestEventSubscriptionType aParsedRequest = (RequestEventSubscriptionType) DEMO_DOC_TYPE.parseMessage (sPayload);

          DE4AKafkaClient.send (EErrorLevel.INFO,
                                "DemoUI sending Subscription request '" +
                                                  aParsedRequest.getRequestId () +
                                                  "' to '" +
                                                  sTargetURL +
                                                  "'");

          final StopWatch aSW = StopWatch.createdStarted ();
          final DcngHttpClientSettings aHCS = new DcngHttpClientSettings ();
          aHCS.setConnectionRequestTimeoutMS (120_000);
          aHCS.setSocketTimeoutMS (120_000);

          byte [] aResponseBytes = null;
          final BootstrapErrorBox aErrorBox = aResNL.addAndReturnChild (error ());
          try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
          {
            // Start HTTP POST
            final HttpPost aPost = new HttpPost (sTargetURL);
            aPost.setEntity (new StringEntity (sPayload,
                                               ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
            aResponseBytes = aHCM.execute (aPost, new ResponseHandlerByteArray ());
            isRequestSent = true;

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
            final ResponseErrorType aErrorObj = DE4ACoreMarshaller.defResponseMarshaller ().read (aResponseBytes);
            if (aErrorObj != null)
            {
              DE4AKafkaClient.send (EErrorLevel.WARN, "Read response as 'ResponseErrorType'");
              if (aErrorObj.isAck ())
              {
                aResNL.addChild (success (div ("The request was accepted by the Connector.")));
              }
              else
              {
                final HCUL aUL = new HCUL ();
                aErrorObj.getError ().forEach (x -> aUL.addItem ("[" + x.getCode () + "] " + x.getText ()));
                aErrorBox.addChild (div ("The data could not be fetched from the Connector")).addChild (aUL);
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
        }
        aNodeList.addChild (aResNL);
      }
    }

    if (bShowForm)
    {
      aNodeList.addChild (info ("This page lets you create arbitrary Event Subscription messages and send them to a WP5 Connector. This simulates the DE-DR and the DT-DO interface."));

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
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Send Subscription request"));

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("getting the request ID, iterate map");

      if (isRequestSent)
      {
        ThreadHelper.sleepSeconds (2);
      }

      final ResponseMapRedirect map = ResponseMapRedirect.getInstance ();

      final String sRequestID = map.getFirstRequestID ();
      if (StringHelper.hasText (sRequestID))
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("getting the response for request Id: " + sRequestID);

        final RedirectUserType aResponse = map.getAndRemove (sRequestID);

        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("redirection to: " + aResponse.getRedirectUrl ());
        aForm.addChild (new BootstrapButton ().addChild ("Manage received redirection messages")
                                              .setIcon (EDefaultIcon.INFO)
                                              .addStyle (ECSSProperty.MARGIN_LEFT, "16px")
                                              .setOnClick (new SimpleURL (aResponse.getRedirectUrl ())));

      }
      else
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("no redirect message found");

        // No need for UI
        if (false)
          aNodeList.addChild (info ("Currently no received redirect is available"));
      }
    }
  }
}
