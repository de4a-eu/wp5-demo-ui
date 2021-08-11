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
/**
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

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.SimpleURL;
import com.helger.commons.url.URLHelper;
import com.helger.html.hc.html.forms.HCCheckBox;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCHiddenField;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.script.HCScriptInline;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.jquery.JQueryAjaxBuilder;
import com.helger.html.js.EJSEvent;
import com.helger.html.jscode.JSAnonymousFunction;
import com.helger.html.jscode.JSArray;
import com.helger.html.jscode.JSAssocArray;
import com.helger.html.jscode.JSBlock;
import com.helger.html.jscode.JSConditional;
import com.helger.html.jscode.JSFunction;
import com.helger.html.jscode.JSPackage;
import com.helger.html.jscode.JSReturn;
import com.helger.html.jscode.JSVar;
import com.helger.html.jscode.html.JSHtml;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.pdflayout4.PageLayoutPDF;
import com.helger.pdflayout4.base.IPLRenderableObject;
import com.helger.pdflayout4.base.PLPageSet;
import com.helger.pdflayout4.element.box.PLBox;
import com.helger.pdflayout4.element.table.PLTable;
import com.helger.pdflayout4.element.table.PLTableCell;
import com.helger.pdflayout4.element.text.PLText;
import com.helger.pdflayout4.spec.FontSpec;
import com.helger.pdflayout4.spec.PreloadFont;
import com.helger.pdflayout4.spec.WidthSpec;
import com.helger.photon.ajax.decl.AjaxFunctionDeclaration;
import com.helger.photon.api.servlet.PhotonAPIServlet;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.button.BootstrapSubmitButton;
import com.helger.photon.bootstrap4.button.EBootstrapButtonType;
import com.helger.photon.bootstrap4.buttongroup.BootstrapButtonGroup;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
import com.helger.photon.bootstrap4.grid.BootstrapGridSpec;
import com.helger.photon.bootstrap4.table.BootstrapTable;
import com.helger.photon.bootstrap4.uictrls.datetimepicker.BootstrapDateTimePicker;
import com.helger.photon.core.form.FormErrorList;
import com.helger.photon.core.form.RequestField;
import com.helger.photon.core.form.RequestFieldBoolean;
import com.helger.photon.icon.fontawesome.EFontAwesome5Icon;
import com.helger.photon.uicore.html.select.HCCountrySelect;
import com.helger.photon.uicore.html.select.HCExtSelect;
import com.helger.photon.uicore.icon.EDefaultIcon;
import com.helger.photon.uicore.page.WebPageExecutionContext;
import com.helger.scope.singleton.AbstractSessionSingleton;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.CApp;
import eu.de4a.demoui.api.APIExecutorPostUSIRedirectResponse;
import eu.de4a.demoui.api.DemoUIAPI;
import eu.de4a.demoui.model.EDemoDocument;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.MDSCompany;
import eu.de4a.demoui.model.MDSPerson;
import eu.de4a.demoui.model.RedirectResponseMap;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.iem.jaxb.common.idtypes.LegalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.idtypes.NaturalPersonIdentifierType;
import eu.de4a.iem.jaxb.common.types.AckType;
import eu.de4a.iem.jaxb.common.types.AgentType;
import eu.de4a.iem.jaxb.common.types.DataRequestSubjectCVType;
import eu.de4a.iem.jaxb.common.types.ErrorListType;
import eu.de4a.iem.jaxb.common.types.ErrorType;
import eu.de4a.iem.jaxb.common.types.ExplicitRequestType;
import eu.de4a.iem.jaxb.common.types.ProvisionItemType;
import eu.de4a.iem.jaxb.common.types.ProvisionType;
import eu.de4a.iem.jaxb.common.types.RequestGroundsType;
import eu.de4a.iem.jaxb.common.types.RequestLookupRoutingInformationType;
import eu.de4a.iem.jaxb.common.types.RequestTransferEvidenceUSIIMDRType;
import eu.de4a.iem.jaxb.common.types.RequestUserRedirectionType;
import eu.de4a.iem.jaxb.common.types.ResponseErrorType;
import eu.de4a.iem.jaxb.common.types.ResponseLookupRoutingInformationType;
import eu.de4a.iem.jaxb.common.types.ResponseTransferEvidenceType;
import eu.de4a.iem.jaxb.common.types.ResponseUserRedirectionType;
import eu.de4a.iem.jaxb.common.types.SourceType;
import eu.de4a.iem.xml.de4a.DE4AMarshaller;
import eu.de4a.iem.xml.de4a.IDE4ACanonicalEvidenceType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public abstract class AbstractPageDE_User extends AbstractPageDE
{
  public static final String ACTION_USIBRB = "usibrb";
  private static final String PARAM_DIRECTION = "dir";
  private static final String DIRECTION_BACK = "back";
  private static final String DIRECTION_NEXT = "next";
  private static final String DIRECTION_RESET = "reset";

  // Select process
  private static final String FIELD_PROCESS = "process";
  // Select DE
  private static final String FIELD_DE_ID = "de_id";
  private static final String FIELD_DE_NAME = "de_name";
  private static final String FIELD_DE_COUNTRY_CODE = "de_cc";
  // Select DO
  private static final String FIELD_DO_ID = "do_id";
  private static final String FIELD_DO_NAME = "do_name";
  private static final String FIELD_DO_COUNTRY_CODE = "do_cc";
  private static final String FIELD_DO_REDIRECT_URL = "do_rurl";
  // Select DRS
  private static final String FIELD_DRS_ID = "id";
  private static final String FIELD_DRS_NAME = "name";
  private static final String FIELD_DRS_FIRSTNAME = "firstname";
  private static final String FIELD_DRS_FAMILYNAME = "familyname";
  private static final String FIELD_DRS_BIRTHDAY = "birthday";
  // Request
  private static final String FIELD_REQUEST_XML = "requestxml";
  private static final String FIELD_TARGET_URL = "targeturl";
  private static final String FIELD_CONFIRM = "confirm";

  private static final String REGEX_COUNTRY_CODE = "[A-Z]{2}";

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractPageDE_User.class);
  private static final AjaxFunctionDeclaration AJAX_CALL_IDK;
  private static final AjaxFunctionDeclaration AJAX_CALL_DOWNLOAD_DATA;

  @Nonnull
  private static String _fixURL (@Nullable final String s)
  {
    if (s == null)
      return "";
    // TODO typo in example URLs
    String ret = s;
    if (ret.endsWith ("?requestId={requestId}&backUrl={backUrl}"))
      ret = ret.substring (0, ret.length () - "?requestId={requestId}&backUrl={backUrl}".length ());
    return ret;
  }

  @Nonnull
  protected static final GenericJAXBMarshaller <RequestTransferEvidenceUSIIMDRType> createMarshaller (@Nullable final EPatternType ePattern,
                                                                                                      @Nullable final ErrorList aEL)
  {
    final DE4AMarshaller <RequestTransferEvidenceUSIIMDRType> m;
    if (ePattern == EPatternType.IM)
      m = DE4AMarshaller.drImRequestMarshaller ();
    else
      m = DE4AMarshaller.drUsiRequestMarshaller ();
    return m.setFormattedOutput (true)
            .setValidationEventHandlerFactory (aEL == null ? null : x -> new WrappedCollectingValidationEventHandler (aEL));
  }

  static
  {
    AJAX_CALL_IDK = addAjax ( (aRequestScope, aAjaxResponse) -> {
      final SessionState aState = SessionState.getInstance ();
      final RequestLookupRoutingInformationType aReq = new RequestLookupRoutingInformationType ();
      aReq.setCanonicalEvidenceTypeId (aState.m_eUseCase.getDocumentTypeID ().getURIEncoded ());
      aReq.setCountryCode (aRequestScope.params ().getAsString ("cc"));
      final String sPayload = DE4AMarshaller.idkRequestLookupRoutingInformationMarshaller ().getAsString (aReq);

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("IDK request:\n" + sPayload);

      ResponseLookupRoutingInformationType aResponse = null;
      String sErrorMsg = null;
      final HttpClientSettings aHCS = new HttpClientSettings ();
      try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
      {
        final String sTargetURL = CApp.CONNECTOR_BASE_URL + EDemoDocument.IDK_LOOKUP_ROUTING_INFO_REQUEST.getRelativeURL ();

        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("Calling IDK '" +
                       sTargetURL +
                       "' for country '" +
                       aReq.getCountryCode () +
                       "' and CET '" +
                       aReq.getCanonicalEvidenceTypeId () +
                       "'");

        final HttpPost aPost = new HttpPost (sTargetURL);
        aPost.setEntity (new StringEntity (sPayload, ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
        final byte [] aResponseBytes = aHCM.execute (aPost, new ResponseHandlerByteArray ());

        if (LOGGER.isInfoEnabled ())
          LOGGER.info ("IDK response:\n" + new String (aResponseBytes, StandardCharsets.UTF_8));

        aResponse = DE4AMarshaller.idkResponseLookupRoutingInformationMarshaller ().read (aResponseBytes);
        if (aResponse == null)
          sErrorMsg = "Failed to parse " + aResponseBytes.length + " response bytes as ResponseLookupRoutingInformationType";
      }
      catch (final IOException ex)
      {
        LOGGER.error ("Failed to query IDK", ex);
        sErrorMsg = ex.getClass ().getName () + " - " + ex.getMessage ();
      }
      final IJsonObject aJson = new JsonObject ();
      if (aResponse == null || aResponse.getErrorList () != null)
      {
        aJson.addIfNotNull ("errormsg", sErrorMsg);
        if (aResponse != null && aResponse.getErrorList () != null)
          for (final ErrorType aError : aResponse.getErrorList ().getError ())
            aJson.add ("error", StringHelper.getConcatenatedOnDemand (aError.getCode (), " - ", aError.getText ()));
        if (false)
        {
          // Causes JS popup
          aAjaxResponse.setStatus (CHttp.HTTP_BAD_REQUEST).setAllowContentOnStatusCode (true);
        }
        else
        {
          aJson.add ("id", "");
          aJson.add ("name", "");
          aJson.add ("redirecturl", "");
        }
      }
      else
      {
        // Use the first one with a redirect URL
        // Fallback to index 0
        final SourceType aSource = CollectionHelper.findFirst (aResponse.getAvailableSources ().getSource (),
                                                               x -> x.getProvisionItems ()
                                                                     .getProvisionItemAtIndex (0)
                                                                     .getProvision () != null &&
                                                                    StringHelper.hasText (x.getProvisionItems ()
                                                                                           .getProvisionItemAtIndex (0)
                                                                                           .getProvision ()
                                                                                           .getRedirectURL ()),
                                                               aResponse.getAvailableSources ().getSourceAtIndex (0));

        final ProvisionItemType aPI = aSource.getProvisionItems ().getProvisionItemAtIndex (0);
        aJson.add ("id", aPI.getDataOwnerId ().toLowerCase (Locale.ROOT));
        aJson.add ("name", aPI.getDataOwnerPrefLabel ());
        final ProvisionType aP = aPI.getProvision ();
        aJson.add ("redirecturl", aP != null ? _fixURL (aP.getRedirectURL ()) : "");
      }

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Out: " + aJson.getAsJsonString (JsonWriterSettings.DEFAULT_SETTINGS_FORMATTED));

      aAjaxResponse.json (aJson);
    });

    AJAX_CALL_DOWNLOAD_DATA = addAjax ( (aRequestScope, aAjaxResponse) -> {
      final SessionState aState = SessionState.getInstance ();
      final Locale aDisplayLocale = CApp.DEFAULT_LOCALE;
      // Passed via URL param
      final EPatternType ePattern = EPatternType.getFromIDOrNull (aRequestScope.params ().getAsString ("pattern"));
      LOGGER.info ("Trying to download request data as PDF for " + ePattern + " pattern");

      final FontSpec c10 = new FontSpec (PreloadFont.MONOSPACE, 10);
      final FontSpec r10 = new FontSpec (PreloadFont.REGULAR, 10);
      final FontSpec r10i = new FontSpec (PreloadFont.REGULAR_ITALIC, 10);
      final FontSpec r12b = new FontSpec (PreloadFont.REGULAR_BOLD, 12);

      final float fMargin = 5f;
      final WidthSpec aCol1 = WidthSpec.perc (25);
      final WidthSpec aCol2 = WidthSpec.star ();

      final PLPageSet aPS1 = new PLPageSet (PDRectangle.A4);

      final Function <String, PLText> _code = s -> StringHelper.hasNoText (s) ? new PLText ("none", r10i).setFillColor (Color.LIGHT_GRAY)
                                                                              : new PLText (s, c10);

      // Headline
      final String sTitle = "Preview of DE4A request data before sending";
      aPS1.addElement (new PLText (sTitle, r12b).setMarginLeft (fMargin).setMarginTop (fMargin));
      aPS1.addElement (new PLText ("Date and time of creation of this report: " + PDTFactory.getCurrentZonedDateTimeUTC ().toString (),
                                   r10).setMarginLeft (fMargin));

      // Evidence type
      final PLTable aTable = new PLTable (aCol1, aCol2).setMargin (fMargin);
      aTable.addAndReturnRow (new PLTableCell (new PLText ("Canonical Evidence Type:", r10)),
                              new PLTableCell (new PLText (aState.m_eUseCase.getDisplayName (), r10)))
            .setMarginTop (fMargin)
            .setMarginBottom (fMargin);

      // DE
      {
        final PLTable aInnerTable = new PLTable (aCol1, aCol2);
        aInnerTable.addRow (new PLTableCell (new PLText ("Name:", r10)),
                            new PLTableCell (new PLText (aState.getDataEvaluatorName (), r10)));
        aInnerTable.addRow (new PLTableCell (new PLText ("ID:", r10)), new PLTableCell (_code.apply (aState.getDataEvaluatorID ())));
        final Locale aDECountry = CountryCache.getInstance ().getCountry (aState.getDataEvaluatorCountryCode ());
        aInnerTable.addRow (new PLTableCell (new PLText ("Country:", r10)),
                            new PLTableCell (new PLText (aDECountry != null ? aDECountry.getDisplayCountry (aDisplayLocale)
                                                                            : aState.getDataEvaluatorCountryCode (),
                                                         r10)));
        aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Evaluator:", r10)), new PLTableCell (aInnerTable))
              .setMarginTop (fMargin)
              .setMarginBottom (fMargin);
      }

      // DO
      {
        final PLTable aInnerTable = new PLTable (aCol1, aCol2);
        aInnerTable.addRow (new PLTableCell (new PLText ("Name:", r10)), new PLTableCell (new PLText (aState.getDataOwnerName (), r10)));
        aInnerTable.addRow (new PLTableCell (new PLText ("ID:", r10)), new PLTableCell (_code.apply (aState.getDataOwnerID ())));
        final Locale aDOCountry = CountryCache.getInstance ().getCountry (aState.getDataOwnerCountryCode ());
        aInnerTable.addRow (new PLTableCell (new PLText ("Country:", r10)),
                            new PLTableCell (new PLText (aDOCountry != null ? aDOCountry.getDisplayCountry (aDisplayLocale)
                                                                            : aState.getDataOwnerCountryCode (),
                                                         r10)));
        if (ePattern == EPatternType.USI)
        {
          aInnerTable.addRow (new PLTableCell (new PLText ("Redirect URL:", r10)),
                              new PLTableCell (_code.apply (aState.getDataOwnerRedirectURL ())));
        }

        aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Owner:", r10)), new PLTableCell (aInnerTable))
              .setMarginTop (fMargin)
              .setMarginBottom (fMargin);
      }

      // DRS
      switch (aState.m_eUseCase.getDRSType ())
      {
        case PERSON:
        {
          final PLTable aInnerTable = new PLTable (aCol1, aCol2);
          aInnerTable.addRow (new PLTableCell (new PLText ("Person ID:", r10)),
                              new PLTableCell (_code.apply (aState.m_aDRSPerson.getID ())));
          aInnerTable.addRow (new PLTableCell (new PLText ("First Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSPerson.getFirstName (), r10)));
          aInnerTable.addRow (new PLTableCell (new PLText ("Family Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSPerson.getFamilyName (), r10)));
          aInnerTable.addRow (new PLTableCell (new PLText ("Birthday:", r10)),
                              new PLTableCell (new PLText (PDTToString.getAsString (aState.m_aDRSPerson.getBirthday (), aDisplayLocale),
                                                           r10)));
          aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Request Subject:", r10)), new PLTableCell (aInnerTable))
                .setMarginTop (fMargin)
                .setMarginBottom (fMargin);
          break;
        }
        case COMPANY:
        {
          final PLTable aInnerTable = new PLTable (aCol1, aCol2);
          aInnerTable.addRow (new PLTableCell (new PLText ("Company ID:", r10)),
                              new PLTableCell (_code.apply (aState.m_aDRSCompany.getID ())));
          aInnerTable.addRow (new PLTableCell (new PLText ("Company Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSCompany.getName (), r10)));
          aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Request Subject:", r10)), new PLTableCell (aInnerTable))
                .setMarginTop (fMargin)
                .setMarginBottom (fMargin);
          break;
        }
        default:
          throw new IllegalStateException ();
      }

      // Created XML
      {
        final IPLRenderableObject <?> aXML;
        if (aState.m_aRequest == null)
          aXML = new PLBox (new PLText ("Failed to create Request Object", r10i).setFillColor (new Color (0xf4, 0xd5, 0xce)));
        else
          aXML = new PLText (createMarshaller (ePattern, null).getAsString (aState.m_aRequest), c10);
        aTable.addAndReturnRow (new PLTableCell (new PLText ("Created XML:", r10)), new PLTableCell (aXML))
              .setMarginTop (fMargin)
              .setMarginBottom (fMargin);
      }

      // Target URL
      aTable.addAndReturnRow (new PLTableCell (new PLText ("Default target URL:", r10)),
                              new PLTableCell (new PLText (getTargetURLTestDR (ePattern), c10)))
            .setMarginTop (fMargin)
            .setMarginBottom (fMargin);

      aPS1.addElement (aTable);

      // Write the PDF
      try (NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
      {
        new PageLayoutPDF ().addPageSet (aPS1)
                            .setCompressPDF (true)
                            .setDocumentAuthor ("DE4A Demo UI")
                            .setDocumentCreationDateTime (PDTFactory.getCurrentLocalDateTime ())
                            .setDocumentTitle (sTitle)
                            .renderTo (aBAOS);
        aAjaxResponse.pdf (aBAOS, "de4a-request-preview.pdf");
      }

      LOGGER.info ("Finished rendering PDF");
    });
  }

  private enum EStep
  {
    // Order matters
    SELECT_PROCESS,
    SELECT_DATA_EVALUATOR,
    SELECT_DATA_OWNER,
    SELECT_DATA_REQUEST_SUBJECT,
    EXPLICIT_CONSENT,
    SEND_REQUEST;

    public boolean isFirst ()
    {
      return ordinal () == 0;
    }

    public boolean isNextSendRequest ()
    {
      return ordinal () == SEND_REQUEST.ordinal () - 1;
    }

    public boolean isLast ()
    {
      return ordinal () == values ().length - 1;
    }

    public boolean wasRequestSent ()
    {
      return ordinal () >= SEND_REQUEST.ordinal ();
    }

    public boolean isLT (@Nonnull final EStep eOther)
    {
      return ordinal () < eOther.ordinal ();
    }

    public boolean isGT (@Nonnull final EStep eOther)
    {
      return ordinal () > eOther.ordinal ();
    }

    @Nullable
    public EStep prev ()
    {
      if (isFirst ())
        return null;
      return values ()[ordinal () - 1];
    }

    @Nullable
    public EStep next ()
    {
      if (isLast ())
        return null;
      return values ()[ordinal () + 1];
    }

    @Nonnull
    public static EStep first ()
    {
      return values ()[0];
    }

    @Nonnull
    public static EStep min (@Nonnull final EStep e1, @Nonnull final EStep e2)
    {
      return e1.ordinal () < e2.ordinal () ? e1 : e2;
    }
  }

  public static final class SessionState extends AbstractSessionSingleton
  {
    private EPatternType m_ePattern;
    private EStep m_eStep = EStep.first ();
    private String m_sRequestID = UUID.randomUUID ().toString ();

    // Process
    EUseCase m_eUseCase;
    // DE
    Agent m_aDE;
    // DO
    Agent m_aDO;
    // DRS
    MDSCompany m_aDRSCompany;
    MDSPerson m_aDRSPerson;
    // Consent to send this
    RequestTransferEvidenceUSIIMDRType m_aRequest;
    String m_sTargetURL;
    boolean m_bConfirmedToSend;
    // Response received
    ResponseTransferEvidenceType m_aResponseIM;
    public ResponseErrorType m_aResponseUSI;

    @Deprecated
    @UsedViaReflection
    public SessionState ()
    {}

    @Nonnull
    public static SessionState getInstance ()
    {
      return getSessionSingleton (SessionState.class);
    }

    public void validate (@Nonnull final EPatternType eExpectedPattern)
    {
      if (m_eStep == null)
        throw new IllegalStateException ("No step");
      if (m_ePattern == null)
      {
        // First time init
        m_ePattern = eExpectedPattern;
      }
      else
        if (m_ePattern != eExpectedPattern)
        {
          // Switch between IM and USI
          m_ePattern = eExpectedPattern;
          reset ();
        }

      if (m_eUseCase == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_PROCESS);
      if (m_aDE == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_EVALUATOR);
      if (m_aDO == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_OWNER);
      if (_allDRSNull ())
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_REQUEST_SUBJECT);
      if (m_aRequest == null || !m_bConfirmedToSend)
        m_eStep = EStep.min (m_eStep, EStep.EXPLICIT_CONSENT);
    }

    private void _onBack ()
    {
      if (m_eStep.isLT (EStep.SELECT_PROCESS))
        m_eUseCase = null;
      if (m_eStep.isLT (EStep.SELECT_DATA_EVALUATOR))
        m_aDE = null;
      if (m_eStep.isLT (EStep.SELECT_DATA_OWNER))
        m_aDO = null;
      if (m_eStep.isLT (EStep.SELECT_DATA_REQUEST_SUBJECT))
        resetDRS ();
      if (m_eStep.isLT (EStep.EXPLICIT_CONSENT))
      {
        m_aRequest = null;
        m_sTargetURL = null;
        m_bConfirmedToSend = false;
      }
    }

    @Nonnull
    protected EStep step ()
    {
      return m_eStep;
    }

    public void moveBack ()
    {
      m_eStep = m_eStep.prev ();
      _onBack ();
    }

    public void moveForward ()
    {
      m_eStep = m_eStep.next ();
    }

    /**
     * Restart the whole wizard to the start state
     */
    public void reset ()
    {
      m_eStep = EStep.first ();
      m_sRequestID = UUID.randomUUID ().toString ();
      _onBack ();
    }

    @Nullable
    public String getProcessID ()
    {
      return m_eUseCase == null ? null : m_eUseCase.getID ();
    }

    @Nullable
    public String getDataEvaluatorID ()
    {
      return m_aDE == null ? null : m_aDE.getID ();
    }

    @Nullable
    public String getDataEvaluatorName ()
    {
      return m_aDE == null ? null : m_aDE.getName ();
    }

    @Nullable
    public String getDataEvaluatorCountryCode ()
    {
      return m_aDE == null ? null : m_aDE.getCountryCode ();
    }

    @Nullable
    public String getDataOwnerID ()
    {
      return m_aDO == null ? null : m_aDO.getID ();
    }

    @Nullable
    public String getDataOwnerName ()
    {
      return m_aDO == null ? null : m_aDO.getName ();
    }

    @Nullable
    public String getDataOwnerCountryCode ()
    {
      return m_aDO == null ? null : m_aDO.getCountryCode ();
    }

    @Nullable
    public String getDataOwnerRedirectURL ()
    {
      return m_aDO == null ? null : m_aDO.getRedirectURL ();
    }

    private boolean _allDRSNull ()
    {
      return m_aDRSCompany == null && m_aDRSPerson == null;
    }

    public void resetDRS ()
    {
      m_aDRSCompany = null;
      m_aDRSPerson = null;
    }

    @Nullable
    public LocalDate getBirthDayOr (@Nullable final LocalDate aFallbackDate)
    {
      return m_aDRSPerson != null ? m_aDRSPerson.getBirthday () : aFallbackDate;
    }

    @Nonnull
    public RequestTransferEvidenceUSIIMDRType buildRequest ()
    {
      final RequestTransferEvidenceUSIIMDRType aRequest = new RequestTransferEvidenceUSIIMDRType ();
      aRequest.setRequestId (m_sRequestID);
      // TODO
      aRequest.setSpecificationId ("SpecificationId");
      aRequest.setTimeStamp (PDTFactory.getCurrentXMLOffsetDateTimeMillisOnly ());
      // TODO
      aRequest.setProcedureId ("ProcedureId");
      {
        final AgentType aDE = new AgentType ();
        aDE.setAgentUrn (m_aDE.getID ());
        aDE.setAgentName (m_aDE.getName ());
        aRequest.setDataEvaluator (aDE);
      }
      {
        final AgentType aDO = new AgentType ();
        aDO.setAgentUrn (m_aDO.getID ());
        aDO.setAgentName (m_aDO.getName ());
        aRequest.setDataOwner (aDO);
      }
      {
        final DataRequestSubjectCVType aDRS = new DataRequestSubjectCVType ();
        switch (m_eUseCase.getDRSType ())
        {
          case PERSON:
          {
            final NaturalPersonIdentifierType aPerson = new NaturalPersonIdentifierType ();
            aPerson.setPersonIdentifier (m_aDRSPerson.getID ());
            aPerson.setFirstName (m_aDRSPerson.getFirstName ());
            aPerson.setFamilyName (m_aDRSPerson.getFamilyName ());
            aPerson.setDateOfBirth (m_aDRSPerson.getBirthday ());
            // Ignore the optional stuff
            aDRS.setDataSubjectPerson (aPerson);
            break;
          }
          case COMPANY:
          {
            final LegalPersonIdentifierType aCompany = new LegalPersonIdentifierType ();
            aCompany.setLegalPersonIdentifier (m_aDRSCompany.getID ());
            aCompany.setLegalName (m_aDRSCompany.getName ());
            // Ignore the optional stuff
            aDRS.setDataSubjectCompany (aCompany);
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        aRequest.setDataRequestSubject (aDRS);
      }
      {
        final RequestGroundsType aRG = new RequestGroundsType ();
        // TODO okay for now?
        aRG.setExplicitRequest (ExplicitRequestType.SDGR_14);
        aRequest.setRequestGrounds (aRG);
      }
      aRequest.setCanonicalEvidenceTypeId (m_eUseCase.getDocumentTypeID ().getURIEncoded ());
      return aRequest;
    }
  }

  public AbstractPageDE_User (@Nonnull @Nonempty final String sID,
                              @Nonnull @Nonempty final String sDisplayName,
                              @Nonnull final EPatternType ePattern)
  {
    super (sID, sDisplayName, ePattern);
  }

  @Override
  protected void fillContent (@Nonnull final WebPageExecutionContext aWPEC)
  {
    final HCNodeList aNodeList = aWPEC.getNodeList ();
    final Locale aDisplayLocale = aWPEC.getDisplayLocale ();
    final IRequestWebScopeWithoutResponse aRequestScope = aWPEC.getRequestScope ();
    final SessionState aState = SessionState.getInstance ();

    final String sDir = aWPEC.params ().getAsStringTrimmed (PARAM_DIRECTION);
    final boolean bGoBack = DIRECTION_BACK.equals (sDir);
    final boolean bGoNext = !bGoBack && DIRECTION_NEXT.equals (sDir);

    if (DIRECTION_RESET.equals (sDir))
      aState.reset ();

    if (m_ePattern == EPatternType.USI)
    {
      if (aWPEC.hasAction (ACTION_USIBRB) && aWPEC.params ().containsKey (APIExecutorPostUSIRedirectResponse.PARAM_REQUEST_ID))
      {
        // We came back from the DE
        final String sRequestID = aWPEC.params ().getAsString (APIExecutorPostUSIRedirectResponse.PARAM_REQUEST_ID);
        final ResponseUserRedirectionType aResponse = RedirectResponseMap.getInstance ().getAndRemove (sRequestID);

        if (aResponse != null)
        {
          String sText = "";
          switch (aResponse.getEvidenceStatus ())
          {
            case AGREE:
              sText = "The user approved to use the data";
              break;
            case DISAGREE:
              sText = "The user denied to use the data";
              break;
            case ERROR:
              sText = "An error occurred and the user could not make a decision";
              break;
          }
          aNodeList.addChild (info ("The USI based status for request ID '" + sRequestID + "' is: " + sText));
        }
        else
        {
          aNodeList.addChild (error ("Not expecting any result for request ID '" + sRequestID + "'"));
        }

        final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC));
        aForm.addChild (new HCHiddenField (PARAM_DIRECTION, DIRECTION_RESET));
        aForm.addChild (new BootstrapSubmitButton ().addChild ("New request"));
        return;
      }
    }

    final CountryCache aCC = CountryCache.getInstance ();
    final ICommonsList <Locale> aAllowedCountries = new CommonsArrayList <> (aCC.getCountry ("AT"),
                                                                             aCC.getCountry ("ES"),
                                                                             aCC.getCountry ("LU"),
                                                                             aCC.getCountry ("NL"),
                                                                             aCC.getCountry ("PT"),
                                                                             aCC.getCountry ("RO"),
                                                                             aCC.getCountry ("SI"),
                                                                             aCC.getCountry ("SE"));

    // Grab input parameters
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bIsSubmitted = bGoBack || bGoNext;
    if (bGoNext)
      switch (aState.step ())
      {
        case SELECT_PROCESS:
        {
          final String sProcessID = aWPEC.params ().getAsStringTrimmed (FIELD_PROCESS, aState.getProcessID ());
          final EUseCase eProcess = EUseCase.getFromIDOrNull (sProcessID);

          if (StringHelper.hasNoText (sProcessID))
            aFormErrors.addFieldError (FIELD_PROCESS, "Select a process");
          else
            if (eProcess == null)
              aFormErrors.addFieldError (FIELD_PROCESS, "Select valid a process");

          if (aFormErrors.isEmpty ())
          {
            aState.m_eUseCase = eProcess;
          }
          break;
        }
        case SELECT_DATA_EVALUATOR:
        {
          final String sDEID = aWPEC.params ().getAsStringTrimmed (FIELD_DE_ID, aState.getDataEvaluatorID ());
          final String sDEName = aWPEC.params ().getAsStringTrimmed (FIELD_DE_NAME, aState.getDataEvaluatorName ());
          final String sDECC = aWPEC.params ().getAsStringTrimmed (FIELD_DE_COUNTRY_CODE, aState.getDataEvaluatorCountryCode ());

          if (StringHelper.hasNoText (sDEID))
            aFormErrors.addFieldError (FIELD_DE_ID, "A Data Evaluator ID is needed");

          if (StringHelper.hasNoText (sDEName))
            aFormErrors.addFieldError (FIELD_DE_NAME, "A Data Evaluator name is needed");

          if (StringHelper.hasNoText (sDECC))
            aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "A Data Evaluator country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern (REGEX_COUNTRY_CODE, sDECC))
              aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "The Data Evaluator country code is invalid");

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDE = Agent.builder ().id (sDEID).name (sDEName).countryCode (sDECC).build ();
          }
          break;
        }
        case SELECT_DATA_OWNER:
        {
          final String sDOID = aWPEC.params ().getAsStringTrimmed (FIELD_DO_ID, aState.getDataOwnerID ());
          final String sDOName = aWPEC.params ().getAsStringTrimmed (FIELD_DO_NAME, aState.getDataOwnerName ());
          final String sDOCC = aWPEC.params ().getAsStringTrimmed (FIELD_DO_COUNTRY_CODE, aState.getDataOwnerCountryCode ());
          final String sDORedirectURL = aWPEC.params ().getAsStringTrimmed (FIELD_DO_REDIRECT_URL, aState.getDataOwnerRedirectURL ());

          if (StringHelper.hasNoText (sDOID))
            aFormErrors.addFieldError (FIELD_DO_ID, "A Data Owner ID is needed");

          if (StringHelper.hasNoText (sDOName))
            aFormErrors.addFieldError (FIELD_DO_NAME, "A Data Owner name is needed");

          if (StringHelper.hasNoText (sDOCC))
            aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "A Data Owner country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern (REGEX_COUNTRY_CODE, sDOCC))
              aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "The Data Owner country code is invalid");

          if (m_ePattern == EPatternType.USI)
          {
            if (StringHelper.hasNoText (sDORedirectURL))
              aFormErrors.addFieldError (FIELD_DO_REDIRECT_URL, "A Data Owner redirect URL is needed");
            else
              if (URLHelper.getAsURL (sDORedirectURL) == null)
                aFormErrors.addFieldError (FIELD_DO_REDIRECT_URL, "A valid Data Owner redirect URL must be provided");
          }

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDO = Agent.builder ().id (sDOID).name (sDOName).countryCode (sDOCC).redirectURL (sDORedirectURL).build ();
          }
          break;
        }
        case SELECT_DATA_REQUEST_SUBJECT:
        {
          switch (aState.m_eUseCase.getDRSType ())
          {
            case PERSON:
            {
              final String sDRSID = aWPEC.params ()
                                         .getAsStringTrimmed (FIELD_DRS_ID,
                                                              aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getID () : null);
              final String sDRSFirstName = aWPEC.params ()
                                                .getAsStringTrimmed (FIELD_DRS_FIRSTNAME,
                                                                     aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFirstName ()
                                                                                                 : null);
              final String sDRSFamilyName = aWPEC.params ()
                                                 .getAsStringTrimmed (FIELD_DRS_FAMILYNAME,
                                                                      aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFamilyName ()
                                                                                                  : null);
              LocalDate aDRSBirthday = aWPEC.params ().getAsLocalDate (FIELD_DRS_BIRTHDAY, aDisplayLocale);
              if (aDRSBirthday == null)
                aDRSBirthday = aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getBirthday () : null;

              if (StringHelper.hasNoText (sDRSID))
                aFormErrors.addFieldError (FIELD_DRS_ID, "A person ID must be provided");
              if (StringHelper.hasNoText (sDRSFirstName))
                aFormErrors.addFieldError (FIELD_DRS_FIRSTNAME, "A person first name must be provided");
              if (StringHelper.hasNoText (sDRSFamilyName))
                aFormErrors.addFieldError (FIELD_DRS_FAMILYNAME, "A person family name must be provided");
              if (aDRSBirthday == null)
                aFormErrors.addFieldError (FIELD_DRS_BIRTHDAY, "A person birthday name must be provided");

              aState.resetDRS ();
              if (aFormErrors.isEmpty ())
              {
                aState.m_aDRSPerson = MDSPerson.builder ()
                                               .id (sDRSID)
                                               .firstName (sDRSFirstName)
                                               .familyName (sDRSFamilyName)
                                               .birthday (aDRSBirthday)
                                               .build ();
              }
              break;
            }
            case COMPANY:
            {
              final String sDRSID = aWPEC.params ()
                                         .getAsStringTrimmed (FIELD_DRS_ID,
                                                              aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getID () : null);
              final String sDRSName = aWPEC.params ()
                                           .getAsStringTrimmed (FIELD_DRS_NAME,
                                                                aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getName () : null);

              if (StringHelper.hasNoText (sDRSID))
                aFormErrors.addFieldError (FIELD_DRS_ID, "A company ID must be provided");
              if (StringHelper.hasNoText (sDRSName))
                aFormErrors.addFieldError (FIELD_DRS_NAME, "A company name must be provided");

              aState.resetDRS ();
              if (aFormErrors.isEmpty ())
              {
                aState.m_aDRSCompany = MDSCompany.builder ().id (sDRSID).name (sDRSName).build ();
              }
              break;
            }
            default:
              throw new IllegalStateException ();
          }
          break;
        }
        case EXPLICIT_CONSENT:
        {
          final String sTargetURL = aWPEC.params ().getAsStringTrimmed (FIELD_TARGET_URL);
          final boolean bConfirm = aWPEC.params ().isCheckBoxChecked (FIELD_CONFIRM, false);

          if (StringHelper.hasNoText (sTargetURL))
            aFormErrors.addFieldError (FIELD_TARGET_URL, "A target URL is required");
          else
            if (URLHelper.getAsURL (sTargetURL, false) == null)
              aFormErrors.addFieldError (FIELD_TARGET_URL, "The target URL must be valid URL");

          if (!bConfirm)
            aFormErrors.addFieldError (FIELD_CONFIRM, "Confirmation is required");

          aState.m_sTargetURL = null;
          aState.m_bConfirmedToSend = bConfirm;
          if (aFormErrors.isEmpty ())
          {
            aState.m_sTargetURL = sTargetURL;
          }

          break;
        }
        case SEND_REQUEST:
        {
          // Nothing
          break;
        }
        default:
          aNodeList.addChild (error ("Unsupported step " + aState.step ()));
      }

    // Change step now
    final boolean bMoved;
    if (bGoBack && !aState.step ().isFirst ())
    {
      LOGGER.info ("One step backwards from " + aState.step ());
      aState.moveBack ();
      bMoved = true;
    }
    else
      if (bGoNext && !aState.step ().isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        LOGGER.info ("One step forward from " + aState.step ());
        aState.moveForward ();
        bMoved = true;
      }
      else
      {
        bMoved = false;
      }

    final boolean bIsResubmitted = bIsSubmitted && !bMoved;

    // Check the requirements for the current step are fulfilled
    aState.validate (m_ePattern);

    // UI form
    final BootstrapForm aForm = aNodeList.addAndReturnChild (new BootstrapForm (aWPEC).ensureID ());
    aForm.setSplitting (BootstrapGridSpec.create (-1, -1, 3, 2, 2), BootstrapGridSpec.create (-1, -1, 9, 10, 10));

    // Show "input error" if necessary
    if (aFormErrors.isNotEmpty ())
      aForm.addChild (getUIHandler ().createIncorrectInputBox (aWPEC));

    // Show the current process as H2
    if (aState.step ().isGT (EStep.SELECT_PROCESS))
      aForm.addChild (h2 ("Running use case " + aState.m_eUseCase.getDisplayName ()));

    // Add some global JS
    final JSFunction jFuncSetDE;
    final JSFunction jFuncSetDO;
    {
      final HCScriptInline aScript = new HCScriptInline ();
      final JSPackage aJS = new JSPackage ();
      jFuncSetDE = aJS.function ("_setMDE");
      {
        final JSVar jID = jFuncSetDE.param ("id");
        final JSVar jElementID = jFuncSetDE.param ("eid");
        final JSVar jElementName = jFuncSetDE.param ("en");
        final JSVar jElementCC = jFuncSetDE.param ("ecc");
        final JSArray jMDE = new JSArray ();
        for (final EMockDataEvaluator e : EMockDataEvaluator.values ())
          jMDE.add (new JSAssocArray ().add ("id", e.getID ()).add ("n", e.getDisplayName ()).add ("cc", e.getCountryCode ()));
        final JSVar jArray = jFuncSetDE.body ().var ("array", jMDE);
        final JSVar jCallbackParam = new JSVar ("x");
        final JSVar jFound = jFuncSetDE.body ()
                                       .var ("f",
                                             jArray.invoke ("find")
                                                   .arg (new JSAnonymousFunction (jCallbackParam,
                                                                                  new JSReturn (jID.eq (jCallbackParam.ref ("id"))))));
        final JSBlock jIfFound = jFuncSetDE.body ()._if (jFound)._then ();
        jIfFound.add (JQuery.idRef (jElementID).val (jFound.component ("id")));
        jIfFound.add (JQuery.idRef (jElementName).val (jFound.component ("n")));
        jIfFound.add (JQuery.idRef (jElementCC).val (jFound.component ("cc")));
      }
      jFuncSetDO = aJS.function ("_setMDO");
      {
        final JSVar jID = jFuncSetDO.param ("id");
        final JSVar jElementID = jFuncSetDO.param ("eid");
        final JSVar jElementName = jFuncSetDO.param ("en");
        final JSVar jElementCC = jFuncSetDO.param ("ecc");
        final JSArray jMDO = new JSArray ();
        for (final EMockDataOwner e : EMockDataOwner.values ())
          jMDO.add (new JSAssocArray ().add ("id", e.getID ()).add ("n", e.getDisplayName ()).add ("cc", e.getCountryCode ()));
        final JSVar jArray = jFuncSetDO.body ().var ("array", jMDO);
        final JSVar jCallbackParam = new JSVar ("x");
        final JSVar jFound = jFuncSetDO.body ()
                                       .var ("f",
                                             jArray.invoke ("find")
                                                   .arg (new JSAnonymousFunction (jCallbackParam,
                                                                                  new JSReturn (jID.eq (jCallbackParam.ref ("id"))))));
        final JSBlock jIfFound = jFuncSetDO.body ()._if (jFound)._then ();
        jIfFound.add (JQuery.idRef (jElementID).val (jFound.component ("id")));
        jIfFound.add (JQuery.idRef (jElementName).val (jFound.component ("n")));
        jIfFound.add (JQuery.idRef (jElementCC).val (jFound.component ("cc")));
      }
      aScript.setJSCodeProvider (aJS);

      aForm.addChild (aScript);
    }

    // Handle current step
    switch (aState.step ())
    {
      case SELECT_PROCESS:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_PROCESS, aState.getProcessID ()));
        for (final EUseCase e : CollectionHelper.getSorted (EUseCase.values (), IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.getPatternType () == m_ePattern)
            aSelect.addOption (e.getID (), e.getDisplayName ());
        if (aSelect.getOptionCount () > 1)
          aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Use Case")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_PROCESS)));
        break;
      }
      case SELECT_DATA_EVALUATOR:
      {
        final HCExtSelect aMockDESelect = new HCExtSelect (new RequestField ("mockde", aState.getDataEvaluatorID ()));
        for (final EMockDataEvaluator e : CollectionHelper.getSorted (EMockDataEvaluator.values (),
                                                                      IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eUseCase))
            aMockDESelect.addOption (e.getID (), e.getDisplayName ());
        aMockDESelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Evaluator to be used").setCtrl (aMockDESelect));

        // Country
        final HCCountrySelect aCountrySelect = new HCCountrySelect (new RequestField (FIELD_DE_COUNTRY_CODE,
                                                                                      aState.getDataEvaluatorCountryCode ()),
                                                                    aDisplayLocale,
                                                                    aAllowedCountries);
        aCountrySelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator country")
                                                     .setCtrl (aCountrySelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_COUNTRY_CODE)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DE_NAME, aState.getDataEvaluatorName ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_NAME)));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DE_ID, aState.getDataEvaluatorID ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_ID)));

        // JS
        final JSPackage aJSOnChange = new JSPackage ();
        aJSOnChange.add (jFuncSetDE.invoke ()
                                   .arg (JSHtml.getSelectSelectedValue ())
                                   .arg (aEditID.getID ())
                                   .arg (aEditName.getID ())
                                   .arg (aCountrySelect.getID ()));
        aMockDESelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        break;
      }
      case SELECT_DATA_OWNER:
      {

        /**
         * request
         *
         * <pre>
        <RequestLookupRoutingInformation xmlns=
        "http://www.de4a.eu/2020/data/requestor/idk" xmlns:eilp=
        "http://eidas.europa.eu/attributes/legalperson" xmlns:einp=
        "http://eidas.europa.eu/attributes/naturalperson" xmlns:de4aid=
        "http://www.de4a.eu/2020/commons/identity/type" xmlns:de4a=
        "http://www.de4a.eu/2020/commons/type">
        <de4a:CanonicalEvidenceTypeId>CompanyRegistration</de4a:CanonicalEvidenceTypeId>
        <de4a:CountryCode>AT</de4a:CountryCode>
        </RequestLookupRoutingInformation>
         * </pre>
         *
         * response:
         *
         * <pre>
        <ResponseLookupRoutingInformation xmlns=
        "http://www.de4a.eu/2020/data/requestor/idk" xmlns:eilp=
        "http://eidas.europa.eu/attributes/legalperson" xmlns:einp=
        "http://eidas.europa.eu/attributes/naturalperson" xmlns:de4aid=
        "http://www.de4a.eu/2020/commons/identity/type" xmlns:de4a=
        "http://www.de4a.eu/2020/commons/type">
        <de4a:AvailableSources>
        <de4a:Source>
        <de4a:CountryCode>AT</de4a:CountryCode>
        <de4a:AtuLevel>nuts0</de4a:AtuLevel>
        <de4a:ProvisionItems>
        <de4a:ProvisionItem>
        <de4a:AtuCode>AT</de4a:AtuCode>
                <de4a:AtuLatinName>ÖSTERREICH</de4a:AtuLatinName>
                <de4a:DataOwnerId>iso6523-actorid-upis::9999:AT000000271</de4a:DataOwnerId>
                <de4a:DataOwnerPrefLabel>BUNDESMINISTERIUM FUER DIGITALISIERUNG UND WIRTSCHAFTSSTANDORT (BMDW)</de4a:DataOwnerPrefLabel>
                <de4a:Provision>
          <de4a:ProvisionType>ip</de4a:ProvisionType>
        </de4a:Provision>
        </de4a:ProvisionItem>
        </de4a:ProvisionItems>
        </de4a:Source>
        </de4a:AvailableSources>
        </ResponseLookupRoutingInformation>
         * </pre>
         */

        final boolean bUseMockDOByDefault = m_ePattern == EPatternType.IM;

        // Mock or IDK?
        final RequestFieldBoolean aRF = new RequestFieldBoolean ("usemockdo", bUseMockDOByDefault);
        final boolean bUseMockDO = aRF.isChecked (aRequestScope.params ());
        final HCCheckBox aCB = new HCCheckBox (aRF);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelForCheckBox ("Check to use Mock DO, uncheck to use IDK").setCtrl (aCB));

        // Mock DO
        final HCExtSelect aMockDOSelect = new HCExtSelect (new RequestField ("mockdo", aState.getDataOwnerID ()));
        for (final EMockDataOwner e : CollectionHelper.getSorted (EMockDataOwner.values (),
                                                                  IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsProcess (aState.m_eUseCase) && !e.getID ().equals (aState.getDataEvaluatorID ()))
            aMockDOSelect.addOption (e.getID (), e.getDisplayName ());
        aMockDOSelect.addOptionPleaseSelect (aDisplayLocale);
        aMockDOSelect.setDisabled (!bUseMockDO);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Owner to be used").setCtrl (aMockDOSelect));

        // Country
        final HCCountrySelect aCountrySelect = new HCCountrySelect (new RequestField (FIELD_DO_COUNTRY_CODE,
                                                                                      aState.getDataOwnerCountryCode ()),
                                                                    aDisplayLocale,
                                                                    aAllowedCountries);
        aCountrySelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner country")
                                                     .setCtrl (aCountrySelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_COUNTRY_CODE)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DO_NAME, aState.getDataOwnerName ())).ensureID ()
                                                                                                          .setReadOnly (!bUseMockDO);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_NAME)));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DO_ID, aState.getDataOwnerID ())).ensureID ().setReadOnly (!bUseMockDO);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_ID)));

        // Redirect URL (USI only)
        HCEdit aEditRedirectURL = null;
        if (m_ePattern == EPatternType.USI)
        {
          aEditRedirectURL = new HCEdit (new RequestField (FIELD_DO_REDIRECT_URL,
                                                           aState.getDataOwnerRedirectURL ())).ensureID ().setReadOnly (!bUseMockDO);
          aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner redirect URL")
                                                       .setCtrl (aEditRedirectURL)
                                                       .setErrorList (aFormErrors.getListOfField (FIELD_DO_REDIRECT_URL)));
        }

        // JS
        {
          // Checkbox
          final JSPackage aJSHandler = new JSPackage ();
          final JSVar jChecked = aJSHandler.var ("c", JQuery.idRef (aCB).propChecked ());
          aJSHandler.add (JQuery.idRef (aMockDOSelect).setDisabled (jChecked.not ()));
          aJSHandler.add (JQuery.idRef (aEditName).setReadOnly (jChecked.not ()));
          aJSHandler.add (JQuery.idRef (aEditID).setReadOnly (jChecked.not ()));
          if (aEditRedirectURL != null)
            aJSHandler.add (JQuery.idRef (aEditRedirectURL).setReadOnly (jChecked.not ()));
          aCB.setEventHandler (EJSEvent.CHANGE, aJSHandler);
        }

        {
          // Mock DO
          final JSPackage aJSOnChange = new JSPackage ();
          final JSVar jChecked = aJSOnChange.var ("c", JQuery.idRef (aCB).propChecked ());
          final JSConditional jIf = aJSOnChange._if (jChecked);
          jIf._then ()
             .add (jFuncSetDO.invoke ()
                             .arg (JSHtml.getSelectSelectedValue ())
                             .arg (aEditID.getID ())
                             .arg (aEditName.getID ())
                             .arg (aCountrySelect.getID ()));
          aMockDOSelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        }

        {
          // Country select
          final JSPackage aJSOnChange = new JSPackage ();
          final JSVar jChecked = aJSOnChange.var ("c", JQuery.idRef (aCB).propChecked ());
          final JSBlock jIf = aJSOnChange._if (jChecked.not ())._then ();

          final JSAnonymousFunction jsSetValues = new JSAnonymousFunction ();
          {
            final JSVar aJSAppendData = jsSetValues.param ("data");
            jsSetValues.body ().add (JQuery.idRef (aEditName).val (aJSAppendData.ref ("name")));
            jsSetValues.body ().add (JQuery.idRef (aEditID).val (aJSAppendData.ref ("id")));
            if (aEditRedirectURL != null)
              jsSetValues.body ().add (JQuery.idRef (aEditRedirectURL).val (aJSAppendData.ref ("redirecturl")));
          }
          final JSAnonymousFunction jsSeEmpty = new JSAnonymousFunction ();
          {
            jsSeEmpty.body ().add (JQuery.idRef (aEditName).val (""));
            jsSeEmpty.body ().add (JQuery.idRef (aEditID).val (""));
            if (aEditRedirectURL != null)
              jsSeEmpty.body ().add (JQuery.idRef (aEditRedirectURL).val (""));
          }
          jIf.add (new JQueryAjaxBuilder ().url (AJAX_CALL_IDK.getInvocationURI (aRequestScope))
                                           .data (new JSAssocArray ().add ("cc", JQuery.idRef (aCountrySelect).val ()))
                                           .success (jsSetValues)
                                           .error (jsSeEmpty)
                                           .build ());
          aCountrySelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        }

        break;
      }
      case SELECT_DATA_REQUEST_SUBJECT:
      {
        switch (aState.m_eUseCase.getDRSType ())
        {
          case PERSON:
          {
            aForm.addChild (info ("The selected use case " +
                                  aState.m_eUseCase.getDisplayName () +
                                  " requires a person as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromIDOrNull (aState.getDataOwnerID ());

            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getID ()
                                                                                                                                                                        : null,
                                                                                                                                            aState.getDataOwnerCountryCode () +
                                                                                                                                                                                "/" +
                                                                                                                                                                                aState.getDataEvaluatorCountryCode () +
                                                                                                                                                                                "/" +
                                                                                                                                                                                (eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                                                          .getID ()
                                                                                                                                                                                                 : "")))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person First Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FIRSTNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFirstName ()
                                                                                                                                                                        : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                     .getFirstName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FIRSTNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Family Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_FAMILYNAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getFamilyName ()
                                                                                                                                                                        : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                     .getFamilyName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_FAMILYNAME)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Person Birthday")
                                                         .setCtrl (BootstrapDateTimePicker.create (FIELD_DRS_BIRTHDAY,
                                                                                                   bIsResubmitted ? null
                                                                                                                  : aState.getBirthDayOr (eMockDO != null ? eMockDO.getMDSPerson ()
                                                                                                                                                                   .getBirthday ()
                                                                                                                                                          : null),
                                                                                                   aDisplayLocale))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_BIRTHDAY)));
            break;
          }
          case COMPANY:
          {
            aForm.addChild (info ("The selected use case " +
                                  aState.m_eUseCase.getDisplayName () +
                                  " requires a company as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromIDOrNull (aState.getDataOwnerID ());

            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company ID")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_ID,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getID ()
                                                                                                                                                                         : null,
                                                                                                                                            aState.getDataOwnerCountryCode () +
                                                                                                                                                                                 "/" +
                                                                                                                                                                                 aState.getDataEvaluatorCountryCode () +
                                                                                                                                                                                 "/" +
                                                                                                                                                                                 (eMockDO != null ? eMockDO.getMDSCompany ()
                                                                                                                                                                                                           .getID ()
                                                                                                                                                                                                  : "")))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_ID)));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Company Name")
                                                         .setCtrl (new HCEdit (new RequestField (FIELD_DRS_NAME,
                                                                                                 bIsResubmitted ? null
                                                                                                                : StringHelper.getNotEmpty (aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getName ()
                                                                                                                                                                         : null,
                                                                                                                                            eMockDO != null ? eMockDO.getMDSCompany ()
                                                                                                                                                                     .getName ()
                                                                                                                                                            : null))))
                                                         .setErrorList (aFormErrors.getListOfField (FIELD_DRS_NAME)));
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        break;
      }
      case EXPLICIT_CONSENT:
      {
        // Create request
        final RequestTransferEvidenceUSIIMDRType aRequest = aState.buildRequest ();

        // Check against XSD
        final ErrorList aErrorList = new ErrorList ();
        final byte [] aRequestBytes = createMarshaller (m_ePattern, aErrorList).getAsBytes (aRequest);
        if (aRequestBytes == null)
        {
          aState.m_aRequest = null;
          for (final IError a : aErrorList)
            aFormErrors.add (SingleError.builder (a).errorFieldName (FIELD_REQUEST_XML).build ());
        }
        else
        {
          aState.m_aRequest = aRequest;
        }

        // First column for all nested tables
        final HCCol aCol1 = new HCCol (150);

        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type").setCtrl (aState.m_eUseCase.getDisplayName ()));

        {
          final Locale aDECountry = CountryCache.getInstance ().getCountry (aState.getDataEvaluatorCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataEvaluatorName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataEvaluatorID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDECountry != null ? aDECountry.getDisplayCountry (aDisplayLocale) : aState.getDataEvaluatorCountryCode ());
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator").setCtrl (t));
        }

        {
          final Locale aDOCountry = CountryCache.getInstance ().getCountry (aState.getDataOwnerCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataOwnerName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataOwnerID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDOCountry != null ? aDOCountry.getDisplayCountry (aDisplayLocale) : aState.getDataOwnerCountryCode ());
          if (m_ePattern == EPatternType.USI)
          {
            t.addBodyRow ().addCell (strong ("Redirect URL:")).addCell (code (aState.getDataOwnerRedirectURL ()));
          }
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner").setCtrl (t));
        }

        switch (aState.m_eUseCase.getDRSType ())
        {
          case PERSON:
          {
            final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
            t.addBodyRow ().addCell (strong ("Person ID:")).addCell (aState.m_aDRSPerson.getID ());
            t.addBodyRow ().addCell (strong ("First Name:")).addCell (aState.m_aDRSPerson.getFirstName ());
            t.addBodyRow ().addCell (strong ("Family Name:")).addCell (aState.m_aDRSPerson.getFamilyName ());
            t.addBodyRow ()
             .addCell (strong ("Birthday:"))
             .addCell (PDTToString.getAsString (aState.m_aDRSPerson.getBirthday (), aDisplayLocale));
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          case COMPANY:
          {
            final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
            t.addBodyRow ().addCell (strong ("Company ID:")).addCell (aState.m_aDRSCompany.getID ());
            t.addBodyRow ().addCell (strong ("Company Name:")).addCell (aState.m_aDRSCompany.getName ());
            aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Request Subject").setCtrl (t));
            break;
          }
          default:
            throw new IllegalStateException ();
        }
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Created XML")
                                                     .setCtrl (aState.m_aRequest == null ? error ("Failed to create Request Object")
                                                                                         : new HCTextArea (new RequestField (FIELD_REQUEST_XML,
                                                                                                                             createMarshaller (m_ePattern,
                                                                                                                                               null).getAsString (aState.m_aRequest))).setRows (10)
                                                                                                                                                                                      .setReadOnly (true)
                                                                                                                                                                                      .addClass (CBootstrapCSS.FORM_CONTROL)
                                                                                                                                                                                      .addClass (CBootstrapCSS.TEXT_MONOSPACE))
                                                     .setHelpText ("This is the technical request. It is just shown for helping developers")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_REQUEST_XML)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Target URL")
                                                     .setCtrl (new HCEdit (new RequestField (FIELD_TARGET_URL, TARGET_URL_TEST_DR)))
                                                     .setHelpText (span ("The URL to send the request to. Use ").addChild (code (TARGET_URL_MOCK_DO))
                                                                                                                .addChild (" for the mock DO, or ")
                                                                                                                .addChild (code (TARGET_URL_TEST_DR))
                                                                                                                .addChild (" for the test DE4A Connector"))
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_TARGET_URL)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelForCheckBox ("Confirmation to send request")
                                                     .setCtrl (new HCCheckBox (new RequestFieldBoolean (FIELD_CONFIRM, false)))
                                                     .setHelpText ("You need to give your explicit consent here to proceed")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_CONFIRM)));

        aForm.addFormGroup (new BootstrapFormGroup ().setCtrl (new BootstrapButton ().addChild ("Download request data as PDF")
                                                                                     .setOnClick (AJAX_CALL_DOWNLOAD_DATA.getInvocationURL (aRequestScope)
                                                                                                                         .add ("pattern",
                                                                                                                               m_ePattern.getID ()))));

        break;
      }
      case SEND_REQUEST:
      {
        aForm.addChild (info ("Sending the request to ").addChild (code (aState.m_sTargetURL)));

        final StopWatch aSW = StopWatch.createdStarted ();

        // Basic Http client settings
        final HttpClientSettings aHCS = new HttpClientSettings ();
        // Here we need a 2 minute timeout (required for USI)
        aHCS.setConnectionRequestTimeoutMS (2 * (int) CGlobal.MILLISECONDS_PER_MINUTE);
        aHCS.setSocketTimeoutMS (2 * (int) CGlobal.MILLISECONDS_PER_MINUTE);
        try
        {
          // Required for Spain
          aHCS.setSSLContextTrustAll ();
        }
        catch (final GeneralSecurityException ex)
        {}

        try
        {
          final byte [] aResponseBytesRequest1;
          try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
          {
            DE4AKafkaClient.send (EErrorLevel.INFO,
                                  "DemoUI sending " +
                                                    m_ePattern.getDisplayName () +
                                                    " request '" +
                                                    aState.m_aRequest.getRequestId () +
                                                    "' to '" +
                                                    aState.m_sTargetURL +
                                                    "'");
            final HttpPost aPost = new HttpPost (aState.m_sTargetURL);

            final byte [] aRequestBytes = createMarshaller (m_ePattern, null).getAsBytes (aState.m_aRequest);
            LOGGER.info ("Request to be send (in UTF-8): " + new String (aRequestBytes, StandardCharsets.UTF_8));

            aPost.setEntity (new ByteArrayEntity (aRequestBytes, ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
            aPost.setHeader (CHttpHeader.CONTENT_TYPE, CMimeType.APPLICATION_XML.getAsString ());

            // Main POST to DR
            aResponseBytesRequest1 = aHCM.execute (aPost, new ResponseHandlerByteArray ());
            if (aResponseBytesRequest1 == null)
            {
              DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI received no response content");
            }
            else
            {
              DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI received response content (" + aResponseBytesRequest1.length + " bytes)");
              LOGGER.info ("Response received (in UTF-8): " + new String (aResponseBytesRequest1, StandardCharsets.UTF_8));
            }
          }

          final ErrorListType aErrorList;
          if (m_ePattern == EPatternType.IM)
          {
            // IM request
            // -> preview on our (DE) side
            // -> we already have the response and can preview it

            final ResponseTransferEvidenceType aResponseObj = DE4AMarshaller.drImResponseMarshaller (IDE4ACanonicalEvidenceType.NONE)
                                                                            .read (aResponseBytesRequest1);
            if (aResponseObj == null)
              throw new IOException ("Failed to parse IM response XML - see log for details");

            aState.m_aResponseIM = aResponseObj;
            aErrorList = aResponseObj.getErrorList ();
            if (aErrorList == null)
            {
              // Just some fake UI to let the user decide
              aForm.addChild (h2 ("Preview of the response data"));

              // The main preview of the response Canonical Evidence
              aForm.addChild (_createPreviewIM (aWPEC, aResponseObj));

              // Fake selection for the user - Accept or Reject (no action)
              final BootstrapButtonGroup aDiv = aForm.addAndReturnChild (new BootstrapButtonGroup ());
              aDiv.addChild (new BootstrapButton (EBootstrapButtonType.SUCCESS).addChild ("Accept data")
                                                                               .setIcon (EDefaultIcon.YES)
                                                                               .setOnClick (JSHtml.windowAlert ("Okay, you accepted")));
              aDiv.addChild (new BootstrapButton (EBootstrapButtonType.OUTLINE_DANGER).addChild ("Reject data")
                                                                                      .setIcon (EDefaultIcon.NO)
                                                                                      .setOnClick (JSHtml.windowAlert ("Okay, you rejected")));
            }
          }
          else
          {
            // USI request
            // -> preview happens on DO side

            final ResponseErrorType aResponseObj = DE4AMarshaller.drUsiResponseMarshaller ().read (aResponseBytesRequest1);
            if (aResponseObj == null)
              throw new IOException ("Failed to parse USI response XML - see log for details");

            // Remember response
            aState.m_aResponseUSI = aResponseObj;
            aErrorList = aResponseObj.getErrorList ();

            if (aResponseObj.getAck () == AckType.OK && aErrorList == null)
            {
              // Redirect user on AS4 success only
              final RequestUserRedirectionType aRequestRedirect = new RequestUserRedirectionType ();
              aRequestRedirect.setRequestId (aState.m_sRequestID);
              // Our DE URL that we send to the DO, so that he can redirect
              // back to us later (this is the API where we take the POST
              // request and forward back to this page)
              aRequestRedirect.setRedirectURL (AppConfig.getPublicURL () +
                                               PhotonAPIServlet.SERVLET_DEFAULT_PATH +
                                               DemoUIAPI.API_USI_REDIRECT_RESPONSE);
              final byte [] aRedirectRequestBytes = DE4AMarshaller.deUsiRedirectRequestMarshaller ()
                                                                  .setFormattedOutput (true)
                                                                  .getAsBytes (aRequestRedirect);

              LOGGER.info ("Redirect request to be send (in UTF-8): " + new String (aRedirectRequestBytes, StandardCharsets.UTF_8));

              final String sPost2URL = aState.getDataOwnerRedirectURL ();

              // Important to not follow redirects, because we are
              // investigating the HTTP header used for redirects
              final HttpClientSettings aHCS2 = aHCS.getClone ().setFollowRedirects (false).setUseKeepAlive (false);

              final String sGetLocation;
              try (final HttpClientManager aHCM2 = HttpClientManager.create (aHCS2))
              {
                LOGGER.info ("Sending redirect request to the DO redirect URL '" + sPost2URL + "'");
                DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI sending redirect request to '" + sPost2URL + "'");

                final HttpPost aPost2 = new HttpPost (sPost2URL);
                aPost2.setEntity (new ByteArrayEntity (aRedirectRequestBytes));
                aPost2.setHeader (CHttpHeader.CONTENT_TYPE, CMimeType.APPLICATION_XML.getAsString ());

                // Main POST
                sGetLocation = aHCM2.execute (aPost2, aHttpResponse -> {
                  final StatusLine aStatusLine = aHttpResponse.getStatusLine ();
                  String ret = null;

                  // Allow 301, 302, 303 and 307
                  if (aStatusLine.getStatusCode () == CHttp.HTTP_MOVED_PERMANENTLY ||
                      aStatusLine.getStatusCode () == CHttp.HTTP_MOVED_TEMPORARY ||
                      aStatusLine.getStatusCode () == CHttp.HTTP_SEE_OTHER ||
                      aStatusLine.getStatusCode () == CHttp.HTTP_TEMPORARY_REDIRECT)
                  {
                    final Header aLocationHeader = aHttpResponse.getFirstHeader (CHttpHeader.LOCATION);
                    if (aLocationHeader != null)
                    {
                      LOGGER.info ("Found the header '" + CHttpHeader.LOCATION + "' with value '" + aLocationHeader.getValue () + "'");
                      ret = aLocationHeader.getValue ();
                    }
                    else
                    {
                      final String sMsg = "HTTP Response to '" + sPost2URL + "' has no '" + CHttpHeader.LOCATION + "' header";
                      LOGGER.error (sMsg);
                      aForm.addChild (error (sMsg));
                    }
                  }
                  else
                  {
                    final String sMsg = "HTTP Response to '" + sPost2URL + "' has unexpected status code: " + aStatusLine.toString ();
                    LOGGER.error (sMsg);
                    aForm.addChild (error (sMsg));
                  }

                  final byte [] aResponseBytesRequest2 = EntityUtils.toByteArray (aHttpResponse.getEntity ());

                  DE4AKafkaClient.send (EErrorLevel.INFO,
                                        "DemoUI received redirect response content (" + aResponseBytesRequest2.length + " bytes)");
                  LOGGER.info ("Redirect response received (in UTF-8): " + new String (aResponseBytesRequest2, StandardCharsets.UTF_8));

                  return ret;
                });
              }

              if (sGetLocation != null)
              {
                final URL aURL = URLHelper.getAsURL (sGetLocation);
                if (aURL != null && !aURL.getHost ().equals ("localhost") && !aURL.getHost ().equals ("127.0.0.1"))
                {
                  DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI redirecting the user to '" + sGetLocation + "'");
                  aWPEC.postRedirectGetExternal (new SimpleURL (sGetLocation));
                }
                else
                  aForm.addChild (error ("Received an invalid redirection URL ").addChild (code (sGetLocation)));
              }
            }
          }

          if (aErrorList != null)
          {
            final HCUL aUL = new HCUL ();
            aErrorList.getError ().forEach (x -> {
              final String sMsg = "[" + x.getCode () + "] " + x.getText ();
              aUL.addItem (sMsg);
              LOGGER.info ("Response error: " + sMsg);
            });
            aForm.addChild (error (div ("The data could not be fetched from the Data Owner")).addChild (aUL));
          }
        }
        catch (final IOException ex)
        {
          aForm.addChild (error ().addChild (div ("Error sending request to ").addChild (code (aState.m_sTargetURL)))
                                  .addChild (AppCommonUI.getTechnicalDetailsUI (ex, true)));
        }
        finally
        {
          aSW.stop ();
          aForm.addChild (info ("It took " + aSW.getMillis () + " milliseconds to get the result"));
        }

        break;
      }
      default:
        aForm.addChild (error ("Unsupported step " + aState.step ()));
    }

    // Buttons
    {
      final HCDiv aRow = aForm.addAndReturnChild (div ());

      // Back?
      {
        if (aState.step ().isFirst () /* || aState.step ().wasRequestSent () */)
        {
          // Disable and no-action
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setDisabled (true));
        }
        else
        {
          final JSPackage aFunc = new JSPackage ();
          aFunc.add (JQuery.idRef (aForm)
                           .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='" + DIRECTION_BACK + "'></input>")
                           .submit ());
          aFunc._return (false);
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
        }
      }

      // Next?
      if (aState.step ().isLast ())
      {
        aRow.addChild (new BootstrapButton ().addChild ("Next").setIcon (EDefaultIcon.NEXT).setDisabled (true));
      }
      else
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='" + DIRECTION_NEXT + "'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.step ().isNextSendRequest () ? "Send Request" : "Next")
                                             .setIcon (aState.step ().isNextSendRequest () ? EDefaultIcon.YES : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }

      // Restart?
      if (aState.step ().wasRequestSent ())
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" + PARAM_DIRECTION + "' value='" + DIRECTION_RESET + "'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild ("Restart").setIcon (EFontAwesome5Icon.UNDO).setOnClick (aFunc));
      }
    }
  }
}
