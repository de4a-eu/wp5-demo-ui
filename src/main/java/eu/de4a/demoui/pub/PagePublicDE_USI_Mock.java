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

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.IError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.SimpleURL;
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
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.photon.ajax.decl.AjaxFunctionDeclaration;
import com.helger.photon.bootstrap4.CBootstrapCSS;
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

import eu.de4a.demoui.CApp;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.jaxb.common.types.AckType;
import eu.de4a.iem.jaxb.common.types.RequestExtractEvidenceUSIType;
import eu.de4a.iem.jaxb.common.types.ResponseErrorType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_USI_Mock extends AbstractPageDE
{
  // We're doing a DO-USI request
  private static final EDemoDocument DEMO_DOC_TYPE = EDemoDocument.DO_USI_REQ;

  private static final String FIELD_PAYLOAD = "payload";

  private static final AjaxFunctionDeclaration CREATE_NEW_REQUEST;

  @Nonnull
  private static RequestExtractEvidenceUSIType _createDemoRequest ()
  {
    RequestExtractEvidenceUSIType aDemoRequest;
    while (true)
    {
      aDemoRequest = (RequestExtractEvidenceUSIType) DEMO_DOC_TYPE.createDemoRequest ();
      if (aDemoRequest.getDataRequestSubject ().getDataSubjectPerson () != null)
        break;
    }
    aDemoRequest.getDataOwner ().setAgentUrn (EMockDataOwner.PT.getID ());
    aDemoRequest.getDataRequestSubject ().getDataSubjectPerson ().setPersonIdentifier ("PT/NL/123456789");
    aDemoRequest.setCanonicalEvidenceTypeId (EUseCase.HIGHER_EDUCATION_DIPLOMA.getDocumentTypeID ().getURIEncoded ());
    return aDemoRequest;
  }

  static
  {
    CREATE_NEW_REQUEST = addAjax ( (aRequestScope, aAjaxResponse) -> {
      aAjaxResponse.text (DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()));
    });
  }

  public PagePublicDE_USI_Mock (@Nonnull @Nonempty final String sID)
  {
    super (sID, "USI Exchange (Mock)");
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final IRequestWebScopeWithoutResponse aRequestScope = aWPEC.getRequestScope ();

    final String sTargetURL = CApp.MOCK_BASE_URL + DEMO_DOC_TYPE.getRelativeURL ();

    final FormErrorList aFormErrors = new FormErrorList ();
    boolean bShowForm = true;

    if (aWPEC.params ().hasStringValue ("is-response", "true"))
    {
      // Show result of USI preview
      DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI received USI preview result");

      final String sRequestID = aWPEC.params ().getAsString ("requestId");
      final boolean bAccept = aWPEC.params ().getAsBoolean ("accept", false);

      if (StringHelper.hasText (sRequestID))
      {
        if (bAccept)
          aNodeList.addChild (success ("The user accepted the USI preview on DP side and the message will arrive here"));
        else
          aNodeList.addChild (error ("The user rejected the USI preview on DP side and the message will arrive here"));
      }

      aNodeList.addChild (info ("This is a mock and nothing will happen here - you can follow it up in the Tracker though"));

      bShowForm = false;
    }
    else
      if (aWPEC.hasAction (CPageParam.ACTION_PERFORM))
      {
        final String sPayload = aWPEC.params ().getAsStringTrimmed (FIELD_PAYLOAD);
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
            final RequestExtractEvidenceUSIType aParsedRequest = (RequestExtractEvidenceUSIType) DEMO_DOC_TYPE.parseMessage (sPayload);

            DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI sending USI request '" + aParsedRequest.getRequestId () + "'");

            final HttpClientSettings aHCS = new HttpClientSettings ();
            try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
            {
              final HttpPost aPost = new HttpPost (sTargetURL);
              aPost.setEntity (new StringEntity (sPayload, ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
              final byte [] aResponse = aHCM.execute (aPost, new ResponseHandlerByteArray ());
              DE4AKafkaClient.send (EErrorLevel.INFO, "Response content received (" + aResponse.length + " bytes)");
              final ResponseErrorType aResponseObj = DE4AMarshaller.doUsiResponseMarshaller ().read (aResponse);
              if (aResponseObj == null)
                throw new IOException ("Failed to parse response XML");
              if (aResponseObj.getAck () == AckType.OK && aResponseObj.getErrorList () == null)
              {
                final String sBackURL = new SimpleURL ("https://de4a-dev-mock.egovlab.eu/public/menuitem-deusi").add ("is-response", "true")
                                                                                                                .add ("requestId",
                                                                                                                      aParsedRequest.getRequestId ())
                                                                                                                .getAsStringWithEncodedParameters ();
                DE4AKafkaClient.send (EErrorLevel.INFO, "Return URL is '" + sBackURL + "'");
                aWPEC.postRedirectGetExternal (new SimpleURL ("https://de4a-dev-mock.egovlab.eu/do1/preview/index").add ("requestId",
                                                                                                                         aParsedRequest.getRequestId ())
                                                                                                                   .add ("backUrl",
                                                                                                                         sBackURL));
              }
              else
              {
                if (aResponseObj.getErrorList () != null)
                {
                  final HCUL aUL = new HCUL ();
                  aResponseObj.getErrorList ().getError ().forEach (x -> aUL.addItem ("[" + x.getCode () + "] " + x.getText ()));
                  aResNL.addChild (error (aUL));
                }
                else
                {
                  aResNL.addChild (error ("An unspecified error was returned from the server"));
                }
              }
            }
            catch (final IOException ex)
            {
              aResNL.addChild (error ().addChild (div ("Error sending request to ").addChild (code (sTargetURL)))
                                       .addChild (AppCommonUI.getTechnicalDetailsUI (ex, true)));
            }
          }
          aNodeList.addChild (aResNL);
        }
      }

    if (bShowForm)
    {
      final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC));
      aForm.setSplitting (BootstrapGridSpec.create (-1, -1, 2, 2, 2), BootstrapGridSpec.create (-1, -1, 10, 10, 10));
      aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Target URL").setCtrl (code (sTargetURL)));
      {
        final HCTextArea aTA = new HCTextArea (new RequestField (FIELD_PAYLOAD,
                                                                 DEMO_DOC_TYPE.getAnyMessageAsString (_createDemoRequest ()))).setRows (10)
                                                                                                                              .addClass (CBootstrapCSS.TEXT_MONOSPACE);
        final JSAnonymousFunction aJSAppend = new JSAnonymousFunction ();
        final JSVar aJSAppendData = aJSAppend.param ("data");
        aJSAppend.body ().add (JQuery.idRef (aTA).val (aJSAppendData));

        final JSPackage aOnClick = new JSPackage ();
        aOnClick.add (new JQueryAjaxBuilder ().url (CREATE_NEW_REQUEST.getInvocationURL (aRequestScope)).success (aJSAppend).build ());
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Payload")
                                                     .setCtrl (aTA,
                                                               new BootstrapButton ().addChild ("Other message")
                                                                                     .setIcon (EDefaultIcon.REFRESH)
                                                                                     .setOnClick (aOnClick))
                                                     .setHelpText ("The message you want to send. By default a randomly generated message is created")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PAYLOAD)));
      }
      aForm.addChild (new HCHiddenField (CPageParam.PARAM_ACTION, CPageParam.ACTION_PERFORM));
      aForm.addChild (new BootstrapSubmitButton ().setIcon (EDefaultIcon.YES).addChild ("Send USI request"));
    }
  }
}
