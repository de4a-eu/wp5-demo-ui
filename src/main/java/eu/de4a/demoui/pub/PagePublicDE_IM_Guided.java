package eu.de4a.demoui.pub;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.CommonsHashSet;
import com.helger.commons.collection.impl.CommonsLinkedHashSet;
import com.helger.commons.collection.impl.CommonsTreeSet;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.collection.impl.ICommonsSortedSet;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.datetime.PDTToString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.locale.country.CountryCache;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.name.IHasDisplayName;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.commons.url.URLHelper;
import com.helger.datetime.util.PDTIOHelper;
import com.helger.dcng.core.http.DcngHttpClientSettings;
import com.helger.dcng.core.ial.DcngIALClientRemote;
import com.helger.html.hc.html.forms.HCCheckBox;
import com.helger.html.hc.html.forms.HCEdit;
import com.helger.html.hc.html.forms.HCTextArea;
import com.helger.html.hc.html.grouping.HCDiv;
import com.helger.html.hc.html.grouping.HCUL;
import com.helger.html.hc.html.script.HCScriptInline;
import com.helger.html.hc.html.tabular.HCCol;
import com.helger.html.hc.impl.HCNodeList;
import com.helger.html.jquery.JQuery;
import com.helger.html.js.EJSEvent;
import com.helger.html.jscode.JSAnonymousFunction;
import com.helger.html.jscode.JSArray;
import com.helger.html.jscode.JSAssocArray;
import com.helger.html.jscode.JSBlock;
import com.helger.html.jscode.JSFunction;
import com.helger.html.jscode.JSPackage;
import com.helger.html.jscode.JSReturn;
import com.helger.html.jscode.JSVar;
import com.helger.html.jscode.html.JSHtml;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ExtendedHttpResponseException;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;
import com.helger.pdflayout.PageLayoutPDF;
import com.helger.pdflayout.base.IPLRenderableObject;
import com.helger.pdflayout.base.PLPageSet;
import com.helger.pdflayout.element.box.PLBox;
import com.helger.pdflayout.element.table.PLTable;
import com.helger.pdflayout.element.table.PLTableCell;
import com.helger.pdflayout.element.text.PLText;
import com.helger.pdflayout.spec.FontSpec;
import com.helger.pdflayout.spec.PreloadFont;
import com.helger.pdflayout.spec.WidthSpec;
import com.helger.photon.ajax.decl.AjaxFunctionDeclaration;
import com.helger.photon.bootstrap4.CBootstrapCSS;
import com.helger.photon.bootstrap4.alert.BootstrapErrorBox;
import com.helger.photon.bootstrap4.button.BootstrapButton;
import com.helger.photon.bootstrap4.form.BootstrapForm;
import com.helger.photon.bootstrap4.form.BootstrapFormGroup;
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
import com.helger.photon.uictrls.famfam.EFamFamIcon;
import com.helger.scope.singleton.AbstractSessionSingleton;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.demoui.AppConfig;
import eu.de4a.demoui.CApp;
import eu.de4a.demoui.model.EMockDataEvaluator;
import eu.de4a.demoui.model.EMockDataOwner;
import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.EPilot;
import eu.de4a.demoui.model.EUseCase;
import eu.de4a.demoui.model.MDSCompany;
import eu.de4a.demoui.model.MDSPerson;
import eu.de4a.demoui.ui.AppCommonUI;
import eu.de4a.ial.api.jaxb.ProvisionType;
import eu.de4a.ial.api.jaxb.ResponseItemType;
import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;
import eu.de4a.ial.api.jaxb.ResponsePerCountryType;
import eu.de4a.iem.core.CIEM;
import eu.de4a.iem.core.DE4ACoreMarshaller;
import eu.de4a.iem.core.jaxb.common.AgentType;
import eu.de4a.iem.core.jaxb.common.DataRequestSubjectCVType;
import eu.de4a.iem.core.jaxb.common.ExplicitRequestType;
import eu.de4a.iem.core.jaxb.common.LegalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.NaturalPersonIdentifierType;
import eu.de4a.iem.core.jaxb.common.RequestEvidenceItemType;
import eu.de4a.iem.core.jaxb.common.RequestExtractMultiEvidenceIMType;
import eu.de4a.iem.core.jaxb.common.RequestGroundsType;
import eu.de4a.iem.core.jaxb.common.ResponseErrorType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

public class PagePublicDE_IM_Guided extends AbstractPageDE
{
  /**
   * Defines the different steps of the wizard
   *
   * @author Philip Helger
   */
  private enum EStep
  {
    // Order matters
    SELECT_USE_CASE,
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

  /**
   * Defines the per-session state of this page
   *
   * @author Philip Helger
   */
  public static final class SessionState extends AbstractSessionSingleton
  {
    private static final Supplier <String> NEW_REQUEST_ID_PROVIDER = () -> UUID.randomUUID ().toString ();

    private EPatternType m_ePattern;
    private EStep m_eStep = EStep.first ();
    private String m_sRequestID = NEW_REQUEST_ID_PROVIDER.get ();
    // use case
    private EUseCase m_eUseCase;
    private ResponseLookupRoutingInformationType m_aIALResponse;
    private ICommonsSet <String> m_aDOCountries;
    // DE
    private Agent m_aDE;
    // DO
    private Agent m_aDO;
    // DRS
    private MDSCompany m_aDRSCompany;
    private MDSPerson m_aDRSPerson;

    private RequestExtractMultiEvidenceIMType m_aIMRequest;
    private String m_sRequestTargetURL;
    private boolean m_bConfirmedToSendRequest;

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
        m_eStep = EStep.min (m_eStep, EStep.SELECT_USE_CASE);
      if (m_aDE == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_EVALUATOR);
      if (m_aDO == null)
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_OWNER);
      if (_allDRSNull ())
        m_eStep = EStep.min (m_eStep, EStep.SELECT_DATA_REQUEST_SUBJECT);
      if (m_aIMRequest == null || !m_bConfirmedToSendRequest)
        m_eStep = EStep.min (m_eStep, EStep.EXPLICIT_CONSENT);
    }

    private void _onBack ()
    {
      if (m_eStep.isLT (EStep.SELECT_USE_CASE))
      {
        m_eUseCase = null;
        m_aIALResponse = null;
        m_aDOCountries = null;
      }
      if (m_eStep.isLT (EStep.SELECT_DATA_EVALUATOR))
        m_aDE = null;
      if (m_eStep.isLT (EStep.SELECT_DATA_OWNER))
        m_aDO = null;
      if (m_eStep.isLT (EStep.SELECT_DATA_REQUEST_SUBJECT))
        resetDRS ();
      if (m_eStep.isLT (EStep.EXPLICIT_CONSENT))
      {
        m_aIMRequest = null;
        m_sRequestTargetURL = null;
        m_bConfirmedToSendRequest = false;
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
      m_sRequestID = NEW_REQUEST_ID_PROVIDER.get ();
      m_eUseCase = null;
      m_aIALResponse = null;
      m_aDOCountries = null;
      _onBack ();
    }

    @Nonnull
    public String getRequestID ()
    {
      return m_sRequestID;
    }

    @Nullable
    public EPilot getPilot ()
    {
      return m_eUseCase == null ? null : m_eUseCase.getPilot ();
    }

    @Nullable
    public EUseCase getUseCase ()
    {
      return m_eUseCase;
    }

    @Nullable
    public String getUseCaseID ()
    {
      return m_eUseCase == null ? null : m_eUseCase.getID ();
    }

    @Nullable
    public String getDataEvaluatorPID ()
    {
      return m_aDE == null ? null : m_aDE.getPID ();
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
    public String getDataOwnerPID ()
    {
      return m_aDO == null ? null : m_aDO.getPID ();
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
    public RequestExtractMultiEvidenceIMType buildIMRequest ()
    {
      final RequestExtractMultiEvidenceIMType aRequest = new RequestExtractMultiEvidenceIMType ();
      aRequest.setRequestId (m_sRequestID);
      aRequest.setSpecificationId (CIEM.SPECIFICATION_ID);
      aRequest.setTimeStamp (PDTFactory.getCurrentXMLOffsetDateTimeMillisOnly ());
      // TODO what to use instead of ProcedureId ?
      aRequest.setProcedureId ("ProcedureId");
      {
        final AgentType aDE = new AgentType ();
        if (true)
        {
          // Must be constant to receive answers
          aDE.setAgentUrn (AppConfig.getDEParticipantID ());
        }
        else
          aDE.setAgentUrn (m_aDE.getPID ());
        aDE.setAgentName (m_aDE.getName ());
        aRequest.setDataEvaluator (aDE);
      }
      {
        final AgentType aDO = new AgentType ();
        aDO.setAgentUrn (m_aDO.getPID ());
        aDO.setAgentName (m_aDO.getName ());
        aRequest.setDataOwner (aDO);
      }

      final RequestEvidenceItemType aItem = new RequestEvidenceItemType ();
      // TODO is this okay to create a random one?
      aItem.setRequestItemId (NEW_REQUEST_ID_PROVIDER.get ());
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
        aItem.setDataRequestSubject (aDRS);
      }
      {
        final RequestGroundsType aRG = new RequestGroundsType ();
        // TODO okay for now?
        aRG.setExplicitRequest (ExplicitRequestType.SDGR_14);
        aItem.setRequestGrounds (aRG);
      }
      aItem.setCanonicalEvidenceTypeId (m_eUseCase.getDocumentTypeID ().getURIEncoded ());
      aRequest.addRequestEvidenceIMItem (aItem);
      return aRequest;
    }
  }

  private static class MiniDO
  {
    private String m_sCountryCode;
    private String m_sName;
    private String m_sParticipantID;

    public String getCountryCode ()
    {
      return m_sCountryCode;
    }

    public String getDisplayName ()
    {
      return m_sName;
    }

    public String getParticipantID ()
    {
      return m_sParticipantID;
    }

    @Nonnull
    public static MiniDO create (final String sCountryCode, final ProvisionType aProvision)
    {
      final MiniDO ret = new MiniDO ();
      ret.m_sCountryCode = sCountryCode;
      ret.m_sName = aProvision.getDataOwnerPrefLabel ();
      ret.m_sParticipantID = aProvision.getDataOwnerId ();
      return ret;
    }
  }

  private static final Logger LOGGER = LoggerFactory.getLogger (PagePublicDE_IM_Guided.class);
  private static final String PARAM_DIRECTION = "dir";
  private static final String DIRECTION_BACK = "back";
  private static final String DIRECTION_NEXT = "next";
  private static final String DIRECTION_RESET = "reset";

  // Select use case
  private static final String FIELD_USE_CASE = "usecase";
  // Select DE
  private static final String FIELD_DE_PID = "de_id";
  private static final String FIELD_DE_NAME = "de_name";
  private static final String FIELD_DE_COUNTRY_CODE = "de_cc";
  // Select DO
  private static final String FIELD_DO_PID = "do_id";
  private static final String FIELD_DO_NAME = "do_name";
  private static final String FIELD_DO_COUNTRY_CODE = "do_cc";
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

  // All country codes for which mock data is available
  private static final ICommonsOrderedSet <String> ALLOWED_COUNTRIES_STRING;
  private static final ICommonsOrderedSet <Locale> ALLOWED_COUNTRIES_LOCALE;
  static
  {
    // Get from all Mock DE and DO
    final ICommonsSortedSet <String> aAllCCs = new CommonsTreeSet <> ();
    for (final EMockDataEvaluator aDE : EMockDataEvaluator.values ())
      aAllCCs.add (aDE.getCountryCode ());
    for (final EMockDataOwner aDO : EMockDataOwner.values ())
      aAllCCs.add (aDO.getCountryCode ());

    ALLOWED_COUNTRIES_STRING = new CommonsLinkedHashSet <> (aAllCCs);
    final CountryCache aCC = CountryCache.getInstance ();
    ALLOWED_COUNTRIES_LOCALE = new CommonsLinkedHashSet <> (aAllCCs, aCC::getCountry);
  }

  private static final String REGEX_COUNTRY_CODE = "[A-Z]{2}";

  private static final AjaxFunctionDeclaration AJAX_CALL_DOWNLOAD_REQUEST_DATA;
  static
  {
    AJAX_CALL_DOWNLOAD_REQUEST_DATA = addAjax ( (aRequestScope, aAjaxResponse) -> {
      final SessionState aState = SessionState.getInstance ();
      final Locale aDisplayLocale = CApp.DEFAULT_LOCALE;
      // Passed via URL param
      final EPatternType ePattern = EPatternType.getFromIDOrNull (aRequestScope.params ().getAsString ("pattern"));
      LOGGER.info ("Trying to download request data as PDF for " + ePattern + " pattern");

      // Create the PDF on the fly
      final FontSpec c10 = new FontSpec (PreloadFont.MONOSPACE, 10);
      final FontSpec r10 = new FontSpec (PreloadFont.REGULAR, 10);
      final FontSpec r10i = new FontSpec (PreloadFont.REGULAR_ITALIC, 10);
      final FontSpec r12b = new FontSpec (PreloadFont.REGULAR_BOLD, 12);

      final float fMargin = 5f;
      final WidthSpec aCol1 = WidthSpec.perc (25);
      final WidthSpec aCol2 = WidthSpec.star ();

      final PLPageSet aPS1 = new PLPageSet (PDRectangle.A4);

      final Function <String, PLText> makeCode = s -> StringHelper.hasNoText (s) ? new PLText ("none",
                                                                                               r10i).setFillColor (Color.LIGHT_GRAY)
                                                                                 : new PLText (s, c10);

      // Headline
      final String sTitle = "Preview of DE4A Iteration 2 request data before sending";
      aPS1.addElement (new PLText (sTitle, r12b).setMarginLeft (fMargin).setMarginTop (fMargin));
      aPS1.addElement (new PLText ("Date and time of creation of this report: " +
                                   PDTFactory.getCurrentZonedDateTimeUTC ().toString (),
                                   r10).setMarginLeft (fMargin));

      // Evidence type
      final PLTable aTable = new PLTable (aCol1, aCol2).setMargin (fMargin);
      aTable.addAndReturnRow (new PLTableCell (new PLText ("Canonical Evidence Type:", r10)),
                              new PLTableCell (new PLText (aState.getUseCase ().getDisplayName (), r10)))
            .setMarginTop (fMargin)
            .setMarginBottom (fMargin);

      // DE
      {
        final PLTable aInnerTable = new PLTable (aCol1, aCol2);
        aInnerTable.addRow (new PLTableCell (new PLText ("Name:", r10)),
                            new PLTableCell (new PLText (aState.getDataEvaluatorName (), r10)));
        aInnerTable.addRow (new PLTableCell (new PLText ("ID:", r10)),
                            new PLTableCell (makeCode.apply (aState.getDataEvaluatorPID ())));
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
        aInnerTable.addRow (new PLTableCell (new PLText ("Name:", r10)),
                            new PLTableCell (new PLText (aState.getDataOwnerName (), r10)));
        aInnerTable.addRow (new PLTableCell (new PLText ("ID:", r10)),
                            new PLTableCell (makeCode.apply (aState.getDataOwnerPID ())));
        final Locale aDOCountry = CountryCache.getInstance ().getCountry (aState.getDataOwnerCountryCode ());
        aInnerTable.addRow (new PLTableCell (new PLText ("Country:", r10)),
                            new PLTableCell (new PLText (aDOCountry != null ? aDOCountry.getDisplayCountry (aDisplayLocale)
                                                                            : aState.getDataOwnerCountryCode (),
                                                         r10)));
        aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Owner:", r10)), new PLTableCell (aInnerTable))
              .setMarginTop (fMargin)
              .setMarginBottom (fMargin);
      }

      // DRS
      switch (aState.getUseCase ().getDRSType ())
      {
        case PERSON:
        {
          final PLTable aInnerTable = new PLTable (aCol1, aCol2);
          aInnerTable.addRow (new PLTableCell (new PLText ("Person ID:", r10)),
                              new PLTableCell (makeCode.apply (aState.m_aDRSPerson.getID ())));
          aInnerTable.addRow (new PLTableCell (new PLText ("First Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSPerson.getFirstName (), r10)));
          aInnerTable.addRow (new PLTableCell (new PLText ("Family Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSPerson.getFamilyName (), r10)));
          aInnerTable.addRow (new PLTableCell (new PLText ("Birthday:", r10)),
                              new PLTableCell (new PLText (PDTToString.getAsString (aState.m_aDRSPerson.getBirthday (),
                                                                                    aDisplayLocale),
                                                           r10)));
          aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Request Subject:", r10)),
                                  new PLTableCell (aInnerTable))
                .setMarginTop (fMargin)
                .setMarginBottom (fMargin);
          break;
        }
        case COMPANY:
        {
          final PLTable aInnerTable = new PLTable (aCol1, aCol2);
          aInnerTable.addRow (new PLTableCell (new PLText ("Company ID:", r10)),
                              new PLTableCell (makeCode.apply (aState.m_aDRSCompany.getID ())));
          aInnerTable.addRow (new PLTableCell (new PLText ("Company Name:", r10)),
                              new PLTableCell (new PLText (aState.m_aDRSCompany.getName (), r10)));
          aTable.addAndReturnRow (new PLTableCell (new PLText ("Data Request Subject:", r10)),
                                  new PLTableCell (aInnerTable))
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
        if (aState.m_aIMRequest != null)
          aXML = new PLText (DE4ACoreMarshaller.drRequestTransferEvidenceIMMarshaller ()
                                               .formatted ()
                                               .getAsString (aState.m_aIMRequest),
                             c10);
        else
          aXML = new PLBox (new PLText ("Failed to create Request Object",
                                        r10i).setFillColor (new Color (0xf4, 0xd5, 0xce)));
        aTable.addAndReturnRow (new PLTableCell (new PLText ("Created XML:", r10)), new PLTableCell (aXML))
              .setMarginTop (fMargin)
              .setMarginBottom (fMargin);
      }

      // Target URL
      aTable.addAndReturnRow (new PLTableCell (new PLText ("Default target URL:", r10)),
                              new PLTableCell (new PLText (getDefaultTargetURL (ePattern), c10)))
            .setMarginTop (fMargin)
            .setMarginBottom (fMargin);

      aPS1.addElement (aTable);

      // Write the PDF
      try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
      {
        new PageLayoutPDF ().addPageSet (aPS1)
                            .setCompressPDF (true)
                            .setDocumentAuthor ("DE4A Demo UI")
                            .setDocumentCreationDateTime (PDTFactory.getCurrentLocalDateTime ())
                            .setDocumentTitle (sTitle)
                            .renderTo (aBAOS);
        aAjaxResponse.pdf (aBAOS, "de4a-request-preview-" + PDTIOHelper.getCurrentLocalDateTimeForFilename () + ".pdf");
      }

      LOGGER.info ("Finished rendering PDF");
    });
  }

  public PagePublicDE_IM_Guided (@Nonnull @Nonempty final String sID)
  {
    super (sID, "IM Exchange (Guided)", EPatternType.IM);
  }

  @Override
  protected void fillContent (final WebPageExecutionContext aWPEC)
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

    // Grab input parameters
    final FormErrorList aFormErrors = new FormErrorList ();
    final boolean bIsSubmitted = bGoBack || bGoNext;
    if (bGoNext)
      switch (aState.step ())
      {
        case SELECT_USE_CASE:
        {
          final String sUseCaseID = aWPEC.params ().getAsStringTrimmed (FIELD_USE_CASE, aState.getUseCaseID ());
          final EUseCase eUseCase = EUseCase.getFromIDOrNull (sUseCaseID);

          if (StringHelper.hasNoText (sUseCaseID))
            aFormErrors.addFieldError (FIELD_USE_CASE, "Select a use case");
          else
            if (eUseCase == null)
              aFormErrors.addFieldError (FIELD_USE_CASE, "Select valid a use case");

          ResponseLookupRoutingInformationType aIALResponse = null;
          ICommonsSet <String> aCountries = null;
          if (aFormErrors.isEmpty ())
          {
            // Query IAL for all countries that support the selected COT
            final DcngIALClientRemote aClient = DcngIALClientRemote.createDefaultInstance ();
            final String sCOTID = eUseCase.getDocumentTypeID ().getURIEncoded ();
            aIALResponse = aClient.queryIAL (new CommonsLinkedHashSet <> (sCOTID));
            if (aIALResponse != null)
            {
              final ResponseItemType aRIT = CollectionHelper.findFirst (aIALResponse.getResponseItem (),
                                                                        x -> sCOTID.equals (x.getCanonicalObjectTypeId ()));
              if (aRIT != null)
              {
                // Limit results to pilot countries
                aCountries = CommonsHashSet.createFiltered (aRIT.getResponsePerCountry (),
                                                            ResponsePerCountryType::getCountryCode,
                                                            ALLOWED_COUNTRIES_STRING::contains);
              }
              else
                LOGGER.error ("Found no matching ResponseItem from IAL (" +
                              aIALResponse.getResponseItemCount () +
                              " response items)");
            }
            else
              LOGGER.error ("Got nothing back from IAL");

            if (LOGGER.isInfoEnabled ())
              LOGGER.info ("IAL response countries: " + aCountries);
            if (aCountries == null || aCountries.isEmpty ())
              aFormErrors.addFieldError (FIELD_USE_CASE,
                                         "Found no participant in the IAL yet that supports this Document Type '" +
                                                         sCOTID +
                                                         "'");
          }
          if (aFormErrors.isEmpty ())
          {
            aState.m_eUseCase = eUseCase;
            // Remember to avoid performing another remote query
            aState.m_aIALResponse = aIALResponse;
            aState.m_aDOCountries = aCountries;
          }

          break;
        }
        case SELECT_DATA_EVALUATOR:
        {
          final String sDEPID = aWPEC.params ().getAsStringTrimmed (FIELD_DE_PID, aState.getDataEvaluatorPID ());
          final String sDEName = aWPEC.params ().getAsStringTrimmed (FIELD_DE_NAME, aState.getDataEvaluatorName ());
          final String sDECC = aWPEC.params ()
                                    .getAsStringTrimmed (FIELD_DE_COUNTRY_CODE, aState.getDataEvaluatorCountryCode ());

          if (StringHelper.hasNoText (sDEPID))
            aFormErrors.addFieldError (FIELD_DE_PID, "A Data Evaluator ID is needed");

          if (StringHelper.hasNoText (sDEName))
            aFormErrors.addFieldError (FIELD_DE_NAME, "A Data Evaluator name is needed");

          if (StringHelper.hasNoText (sDECC))
            aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "A Data Evaluator country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern (REGEX_COUNTRY_CODE, sDECC))
              aFormErrors.addFieldError (FIELD_DE_COUNTRY_CODE, "The Data Evaluator country code is invalid");

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDE = Agent.builder ().pid (sDEPID).name (sDEName).countryCode (sDECC).build ();
          }
          break;
        }
        case SELECT_DATA_OWNER:
        {
          final String sDOPID = aWPEC.params ().getAsStringTrimmed (FIELD_DO_PID, aState.getDataOwnerPID ());
          final String sDOName = aWPEC.params ().getAsStringTrimmed (FIELD_DO_NAME, aState.getDataOwnerName ());
          final String sDOCC = aWPEC.params ()
                                    .getAsStringTrimmed (FIELD_DO_COUNTRY_CODE, aState.getDataOwnerCountryCode ());

          if (StringHelper.hasNoText (sDOPID))
            aFormErrors.addFieldError (FIELD_DO_PID, "A Data Owner ID is needed");

          if (StringHelper.hasNoText (sDOName))
            aFormErrors.addFieldError (FIELD_DO_NAME, "A Data Owner name is needed");

          if (StringHelper.hasNoText (sDOCC))
            aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "A Data Owner country code is needed");
          else
            if (!RegExHelper.stringMatchesPattern (REGEX_COUNTRY_CODE, sDOCC))
              aFormErrors.addFieldError (FIELD_DO_COUNTRY_CODE, "The Data Owner country code is invalid");

          if (aFormErrors.isEmpty ())
          {
            aState.m_aDO = Agent.builder ().pid (sDOPID).name (sDOName).countryCode (sDOCC).build ();
          }
          break;
        }
        case SELECT_DATA_REQUEST_SUBJECT:
        {
          switch (aState.getUseCase ().getDRSType ())
          {
            case PERSON:
            {
              final String sDRSID = aWPEC.params ()
                                         .getAsStringTrimmed (FIELD_DRS_ID,
                                                              aState.m_aDRSPerson != null ? aState.m_aDRSPerson.getID ()
                                                                                          : null);
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
                                                              aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getID ()
                                                                                           : null);
              final String sDRSName = aWPEC.params ()
                                           .getAsStringTrimmed (FIELD_DRS_NAME,
                                                                aState.m_aDRSCompany != null ? aState.m_aDRSCompany.getName ()
                                                                                             : null);

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

          aState.m_sRequestTargetURL = null;
          aState.m_bConfirmedToSendRequest = bConfirm;
          if (aFormErrors.isEmpty ())
          {
            aState.m_sRequestTargetURL = sTargetURL;
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
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("One step backwards from " + aState.step ());
      aState.moveBack ();
      bMoved = true;
    }
    else
      if (bGoNext && !aState.step ().isLast () && aFormErrors.isEmpty ())
      {
        // Forward moving only if no errors are found
        if (LOGGER.isInfoEnabled ())
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
    final BootstrapForm aForm = aNodeList.addAndReturnChild (getUIHandler ().createFormSelf (aWPEC).ensureID ());
    aForm.setLeft (-1, -1, 3, 2, 2);

    // Show "input error" if necessary
    if (aFormErrors.isNotEmpty ())
      aForm.addChild (getUIHandler ().createIncorrectInputBox (aWPEC));

    // Show the current use case as H2
    if (aState.step ().isGT (EStep.SELECT_USE_CASE))
      aForm.addChild (h2 ("Running use case " + aState.getUseCase ().getDisplayName ()));

    // Add some global JS placeholder
    final JSPackage aGlobalJS = new JSPackage ();
    aForm.addChild (new HCScriptInline (aGlobalJS));

    // Handle current step
    switch (aState.step ())
    {
      case SELECT_USE_CASE:
      {
        final HCExtSelect aSelect = new HCExtSelect (new RequestField (FIELD_USE_CASE, aState.getUseCaseID ()));
        for (final EUseCase e : CollectionHelper.getSorted (EUseCase.values (),
                                                            IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.getPatternType () == m_ePattern)
            aSelect.addOption (e.getID (), e.getDisplayName ());
        if (aSelect.getOptionCount () > 1)
          aSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Use Case")
                                                     .setCtrl (aSelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_USE_CASE)));
        break;
      }
      case SELECT_DATA_EVALUATOR:
      {
        // Mock DE
        final HCExtSelect aMockDESelect = new HCExtSelect (new RequestField ("mockde", aState.getDataEvaluatorPID ()));
        for (final EMockDataEvaluator e : CollectionHelper.getSorted (EMockDataEvaluator.values (),
                                                                      IHasDisplayName.getComparatorCollating (aDisplayLocale)))
          if (e.supportsPilot (aState.getPilot ()))
            aMockDESelect.addOption (e.getID (), e.getDisplayName ());
        aMockDESelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Evaluator to be used")
                                                     .setCtrl (aMockDESelect));

        // Country
        final HCCountrySelect aCountrySelect = new HCCountrySelect (new RequestField (FIELD_DE_COUNTRY_CODE,
                                                                                      aState.getDataEvaluatorCountryCode ()),
                                                                    aDisplayLocale,
                                                                    ALLOWED_COUNTRIES_LOCALE);
        aCountrySelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator country")
                                                     .setCtrl (aCountrySelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_COUNTRY_CODE)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DE_NAME,
                                                               aState.getDataEvaluatorName ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_NAME)));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DE_PID, aState.getDataEvaluatorPID ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Evaluator ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DE_PID)));

        final JSFunction jFuncSetDE = aGlobalJS.function ("_setMDE");
        {
          final JSVar jID = jFuncSetDE.param ("id");
          final JSVar jElementID = jFuncSetDE.param ("eid");
          final JSVar jElementName = jFuncSetDE.param ("en");
          final JSVar jElementCC = jFuncSetDE.param ("ecc");
          final JSArray jMDE = new JSArray ();
          for (final EMockDataEvaluator e : EMockDataEvaluator.values ())
            jMDE.add (new JSAssocArray ().add ("id", e.getParticipantID ())
                                         .add ("n", e.getDisplayName ())
                                         .add ("cc", e.getCountryCode ()));
          final JSVar jArray = jFuncSetDE.body ().var ("array", jMDE);
          final JSVar jCallbackParam = new JSVar ("x");
          final JSVar jFound = jFuncSetDE.body ()
                                         .var ("f",
                                               jArray.invoke ("find")
                                                     .arg (new JSAnonymousFunction (jCallbackParam,
                                                                                    new JSReturn (jID.invoke ("endsWith")
                                                                                                     .arg (jCallbackParam.ref ("id"))))));
          final JSBlock jIfFound = jFuncSetDE.body ()._if (jFound)._then ();
          jIfFound.add (JQuery.idRef (jElementID).val (jFound.component ("id")));
          jIfFound.add (JQuery.idRef (jElementName).val (jFound.component ("n")));
          jIfFound.add (JQuery.idRef (jElementCC).val (jFound.component ("cc")));
        }

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
        final ICommonsList <MiniDO> aAllowedDOs = new CommonsArrayList <> ();
        final ResponseItemType aIALItem = CollectionHelper.findFirst (aState.m_aIALResponse.getResponseItem (),
                                                                      x -> aState.m_eUseCase.getDocumentTypeID ()
                                                                                            .getURIEncoded ()
                                                                                            .equals (x.getCanonicalObjectTypeId ()));
        for (final ResponsePerCountryType aPC : aIALItem.getResponsePerCountry ())
          for (final ProvisionType aProv : aPC.getProvision ())
          {
            final MiniDO aDO = MiniDO.create (aPC.getCountryCode (), aProv);
            if (!aDO.getParticipantID ().equals (aState.getDataEvaluatorPID ()))
              if (ALLOWED_COUNTRIES_STRING.contains (aDO.getCountryCode ()))
                aAllowedDOs.add (aDO);
          }

        // All DOs from IAL
        final HCExtSelect aMockDOSelect = new HCExtSelect (new RequestField ("mockdo", aState.getDataOwnerPID ()));
        for (final MiniDO aDO : aAllowedDOs)
          aMockDOSelect.addOption (aDO.getParticipantID (), aDO.getDisplayName () + " (" + aDO.getCountryCode () + ")");
        aMockDOSelect.addOptionPleaseSelect (aDisplayLocale);
        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Mock Data Owner to be used")
                                                     .setCtrl (aMockDOSelect)
                                                     .setHelpText ("This list was retrieved from the IAL, by filtering for all participants that match the select Canonical Evidence Type"));

        // Country
        final HCCountrySelect aCountrySelect = new HCCountrySelect (new RequestField (FIELD_DO_COUNTRY_CODE,
                                                                                      aState.getDataOwnerCountryCode ()),
                                                                    aDisplayLocale,
                                                                    ALLOWED_COUNTRIES_LOCALE);
        aCountrySelect.ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner country")
                                                     .setCtrl (aCountrySelect)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_COUNTRY_CODE)));

        // Name
        final HCEdit aEditName = new HCEdit (new RequestField (FIELD_DO_NAME, aState.getDataOwnerName ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner name")
                                                     .setCtrl (aEditName)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_NAME)));

        // ID
        final HCEdit aEditID = new HCEdit (new RequestField (FIELD_DO_PID, aState.getDataOwnerPID ())).ensureID ();
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Data Owner ID")
                                                     .setCtrl (aEditID)
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_DO_PID)));

        final JSFunction jFuncSetDO = aGlobalJS.function ("_setMDO");
        {
          final JSVar jID = jFuncSetDO.param ("id");
          final JSVar jElementID = jFuncSetDO.param ("eid");
          final JSVar jElementName = jFuncSetDO.param ("en");
          final JSVar jElementCC = jFuncSetDO.param ("ecc");
          final JSArray jMDO = new JSArray ();
          for (final MiniDO aDO : aAllowedDOs)
          {
            jMDO.add (new JSAssocArray ().add ("id", aDO.getParticipantID ())
                                         .add ("n", aDO.getDisplayName ())
                                         .add ("cc", aDO.getCountryCode ()));
          }
          final JSVar jArray = jFuncSetDO.body ().var ("array", jMDO);
          final JSVar jCallbackParam = new JSVar ("x");
          final JSVar jFound = jFuncSetDO.body ()
                                         .var ("f",
                                               jArray.invoke ("find")
                                                     .arg (new JSAnonymousFunction (jCallbackParam,
                                                                                    new JSReturn (jID.invoke ("endsWith")
                                                                                                     .arg (jCallbackParam.ref ("id"))))));
          final JSBlock jIfFound = jFuncSetDO.body ()._if (jFound)._then ();
          jIfFound.add (JQuery.idRef (jElementID).val (jFound.component ("id")));
          jIfFound.add (JQuery.idRef (jElementName).val (jFound.component ("n")));
          jIfFound.add (JQuery.idRef (jElementCC).val (jFound.component ("cc")));
        }

        {
          // Mock DO - set values from enum
          final JSPackage aJSOnChange = new JSPackage ();
          aJSOnChange.invoke (jFuncSetDO)
                     .arg (JSHtml.getSelectSelectedValue ())
                     .arg (aEditID.getID ())
                     .arg (aEditName.getID ())
                     .arg (aCountrySelect.getID ());
          aMockDOSelect.setEventHandler (EJSEvent.CHANGE, aJSOnChange);
        }

        break;
      }
      case SELECT_DATA_REQUEST_SUBJECT:
      {
        switch (aState.getUseCase ().getDRSType ())
        {
          case PERSON:
          {
            aForm.addChild (info ("The selected use case " +
                                  aState.getUseCase ().getDisplayName () +
                                  " requires a person as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromPilotAndPIDOrNull (aState.getPilot (),
                                                                                    aState.getDataOwnerPID ());
            if (eMockDO == null)
              LOGGER.warn ("Failed to resolve Mock DO for " + aState.getPilot () + " and " + aState.getDataOwnerPID ());

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
                                  aState.getUseCase ().getDisplayName () +
                                  " requires a company as Data Request Subject"));

            final EMockDataOwner eMockDO = EMockDataOwner.getFromPilotAndPIDOrNull (aState.getPilot (),
                                                                                    aState.getDataOwnerPID ());
            if (eMockDO == null)
              LOGGER.warn ("Failed to resolve Mock DO for " + aState.getPilot () + " and " + aState.getDataOwnerPID ());

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
        final RequestExtractMultiEvidenceIMType aRequest = aState.buildIMRequest ();

        // Check against XSD
        final ErrorList aErrorList = new ErrorList ();
        final DE4ACoreMarshaller <RequestExtractMultiEvidenceIMType> m = DE4ACoreMarshaller.drRequestTransferEvidenceIMMarshaller ();
        m.setFormattedOutput (true)
         .setValidationEventHandlerFactory (x -> new WrappedCollectingValidationEventHandler (aErrorList));

        final byte [] aRequestBytes = m.getAsBytes (aRequest);
        if (aRequestBytes == null)
        {
          aState.m_aIMRequest = null;
          for (final IError a : aErrorList)
            aFormErrors.add (SingleError.builder (a).errorFieldName (FIELD_REQUEST_XML).build ());
        }
        else
        {
          aState.m_aIMRequest = aRequest;
        }

        // First column for all nested tables
        final HCCol aCol1 = new HCCol (150);

        aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Canonical Evidence Type")
                                                     .setCtrl (aState.getUseCase ().getDisplayName ()));

        {
          final Locale aDECountry = CountryCache.getInstance ().getCountry (aState.getDataEvaluatorCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataEvaluatorName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataEvaluatorPID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDECountry != null ? aDECountry.getDisplayCountry (aDisplayLocale)
                                        : aState.getDataEvaluatorCountryCode ());
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Evaluator").setCtrl (t));
        }

        {
          final Locale aDOCountry = CountryCache.getInstance ().getCountry (aState.getDataOwnerCountryCode ());
          final BootstrapTable t = new BootstrapTable (aCol1, HCCol.star ());
          t.addBodyRow ().addCell (strong ("Name:")).addCell (aState.getDataOwnerName ());
          t.addBodyRow ().addCell (strong ("ID:")).addCell (code (aState.getDataOwnerPID ()));
          t.addBodyRow ()
           .addCell (strong ("Country:"))
           .addCell (aDOCountry != null ? aDOCountry.getDisplayCountry (aDisplayLocale)
                                        : aState.getDataOwnerCountryCode ());
          aForm.addFormGroup (new BootstrapFormGroup ().setLabel ("Data Owner").setCtrl (t));
        }

        switch (aState.getUseCase ().getDRSType ())
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
                                                     .setCtrl (aState.m_aIMRequest == null ? error ("Failed to create Request Object")
                                                                                           : new HCTextArea (new RequestField (FIELD_REQUEST_XML,
                                                                                                                               m.getAsString (aState.m_aIMRequest))).setRows (10)
                                                                                                                                                                    .setReadOnly (true)
                                                                                                                                                                    .addClass (CBootstrapCSS.FORM_CONTROL)
                                                                                                                                                                    .addClass (CBootstrapCSS.TEXT_MONOSPACE))
                                                     .setHelpText ("This is the technical request. It is just shown for helping developers")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_REQUEST_XML)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelMandatory ("Target URL")
                                                     .setCtrl (new HCEdit (new RequestField (FIELD_TARGET_URL,
                                                                                             m_sDefaultTargetURL)))
                                                     .setHelpText (span ("The URL to send the request to. Use something like ").addChild (code (m_sDefaultTargetURL))
                                                                                                                               .addChild (" for the test DE4A Connector"))
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_TARGET_URL)));
        aForm.addFormGroup (new BootstrapFormGroup ().setLabelForCheckBox ("Confirmation to send request")
                                                     .setCtrl (new HCCheckBox (new RequestFieldBoolean (FIELD_CONFIRM,
                                                                                                        false)))
                                                     .setHelpText ("You need to give your explicit consent here to proceed")
                                                     .setErrorList (aFormErrors.getListOfField (FIELD_CONFIRM)));

        EFamFamIcon.registerResourcesForThisRequest ();
        // Open link in new window
        aForm.addFormGroup (new BootstrapFormGroup ().setCtrl (new BootstrapButton ().addChild ("Download request data as PDF")
                                                                                     .setIcon (EFamFamIcon.PAGE_WHITE_ACROBAT)
                                                                                     .setOnClick (JSHtml.windowOpen ()
                                                                                                        .arg (AJAX_CALL_DOWNLOAD_REQUEST_DATA.getInvocationURL (aRequestScope)
                                                                                                                                             .add ("pattern",
                                                                                                                                                   m_ePattern.getID ())
                                                                                                                                             .getAsStringWithEncodedParameters ()))));

        break;
      }
      case SEND_REQUEST:
      {
        aForm.addChild (info ("Sending the request to ").addChild (code (aState.m_sRequestTargetURL)));

        final StopWatch aSW = StopWatch.createdStarted ();

        // Basic Http client settings
        final DcngHttpClientSettings aHCS = new DcngHttpClientSettings ();
        // Here we need a 2 minute timeout (required for USI)
        aHCS.setConnectionRequestTimeoutMS (2 * (int) CGlobal.MILLISECONDS_PER_MINUTE);
        aHCS.setSocketTimeoutMS (2 * (int) CGlobal.MILLISECONDS_PER_MINUTE);

        try
        {
          final DE4ACoreMarshaller <RequestExtractMultiEvidenceIMType> m = DE4ACoreMarshaller.drRequestTransferEvidenceIMMarshaller ();
          m.setFormattedOutput (true);

          byte [] aResponseBytesRequest1 = null;
          final BootstrapErrorBox aErrorBox = aForm.addAndReturnChild (error ());
          try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
          {
            if (LOGGER.isInfoEnabled ())
              LOGGER.info ("HTTP POST to '" + aState.m_sRequestTargetURL + "'");

            DE4AKafkaClient.send (EErrorLevel.INFO,
                                  "DemoUI sending " +
                                                    m_ePattern.getDisplayName () +
                                                    " request '" +
                                                    aState.m_aIMRequest.getRequestId () +
                                                    "' to '" +
                                                    aState.m_sRequestTargetURL +
                                                    "'");
            final HttpPost aPost = new HttpPost (aState.m_sRequestTargetURL);

            final byte [] aRequestBytes = m.getAsBytes (aState.m_aIMRequest);
            if (LOGGER.isInfoEnabled ())
              LOGGER.info ("Request to be send (in UTF-8): " + new String (aRequestBytes, StandardCharsets.UTF_8));

            aPost.setEntity (new ByteArrayEntity (aRequestBytes,
                                                  ContentType.APPLICATION_XML.withCharset (StandardCharsets.UTF_8)));
            aPost.setHeader (CHttpHeader.CONTENT_TYPE, CMimeType.APPLICATION_XML.getAsString ());

            // Main POST to DR
            aResponseBytesRequest1 = aHCM.execute (aPost, new ResponseHandlerByteArray ());
            if (aResponseBytesRequest1 == null)
            {
              DE4AKafkaClient.send (EErrorLevel.INFO, "DemoUI received no response content");
            }
            else
            {
              DE4AKafkaClient.send (EErrorLevel.INFO,
                                    "DemoUI received response content (" + aResponseBytesRequest1.length + " bytes)");
              if (LOGGER.isInfoEnabled ())
                LOGGER.info ("Response received (in UTF-8):\n" +
                             new String (aResponseBytesRequest1, StandardCharsets.UTF_8));
            }
          }
          catch (final ExtendedHttpResponseException ex)
          {
            LOGGER.error ("HTTP query returned an error", ex);
            aErrorBox.addChild (div ("HTTP error received from DR: " + ex.getMessagePartStatusLine ()));
            aResponseBytesRequest1 = ex.getResponseBody ();
          }

          // IM request
          // -> preview on our (DE) side
          // -> we already have the response and can preview it
          final ResponseErrorType aResponseObj = DE4ACoreMarshaller.defResponseMarshaller ()
                                                                   .read (aResponseBytesRequest1);
          if (aResponseObj == null)
            throw new IOException ("Failed to parse response XML - see log for details");

          if (aResponseObj.isAck ())
          {
            DE4AKafkaClient.send (EErrorLevel.INFO, "Read response as 'ResponseErrorType' and ACK");

            // Just some fake UI to let the user decide
            aForm.addChild (success ("The data was received by the DR and forwarded to the DT. The response will be received asynchronously."));
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
            aErrorBox.addChild (div ("The request could not be accepted by the DR because of the following reasons:"))
                     .addChild (aUL);
          }
        }
        catch (final IOException ex)
        {
          LOGGER.error ("Error executing HTTP request", ex);
          aForm.addChild (error ().addChild (div ("Error sending request to ").addChild (code (aState.m_sRequestTargetURL)))
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
                           .append ("<input type='hidden' name='" +
                                    PARAM_DIRECTION +
                                    "' value='" +
                                    DIRECTION_BACK +
                                    "'></input>")
                           .submit ());
          aFunc._return (false);
          aRow.addChild (new BootstrapButton ().addChild ("Back").setIcon (EDefaultIcon.BACK).setOnClick (aFunc));
        }
      }

      // Next?
      aRow.addChild (" ");
      if (aState.step ().isLast ())
      {
        aRow.addChild (new BootstrapButton ().addChild ("Next").setIcon (EDefaultIcon.NEXT).setDisabled (true));
      }
      else
      {
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" +
                                  PARAM_DIRECTION +
                                  "' value='" +
                                  DIRECTION_NEXT +
                                  "'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild (aState.step ().isNextSendRequest () ? "Send Request" : "Next")
                                             .setIcon (aState.step ().isNextSendRequest () ? EDefaultIcon.YES
                                                                                           : EDefaultIcon.NEXT)
                                             .setOnClick (aFunc));
      }

      // Restart?
      if (aState.step ().wasRequestSent ())
      {
        aRow.addChild (" ");
        final JSPackage aFunc = new JSPackage ();
        aFunc.add (JQuery.idRef (aForm)
                         .append ("<input type='hidden' name='" +
                                  PARAM_DIRECTION +
                                  "' value='" +
                                  DIRECTION_RESET +
                                  "'></input>")
                         .submit ());
        aFunc._return (false);
        aRow.addChild (new BootstrapButton ().addChild ("Restart").setIcon (EFontAwesome5Icon.UNDO).setOnClick (aFunc));
      }
    }
  }
}
