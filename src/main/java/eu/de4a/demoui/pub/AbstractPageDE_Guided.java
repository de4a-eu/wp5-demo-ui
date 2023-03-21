package eu.de4a.demoui.pub;

import java.time.LocalDate;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.scope.singleton.AbstractSessionSingleton;

import eu.de4a.demoui.model.EPatternType;
import eu.de4a.demoui.model.EPilot;
import eu.de4a.demoui.model.EUseCase;
import eu.de4a.demoui.model.MDSCompany;
import eu.de4a.demoui.model.MDSPerson;
import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;

public abstract class AbstractPageDE_Guided extends AbstractPageDE
{
  /**
   * Defines the different steps of the wizard
   *
   * @author Philip Helger
   */
  protected enum EStep
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
   * @param <REQT>
   *        Request type
   */
  protected static class AbstractSessionState <REQT> extends AbstractSessionSingleton
  {
    protected static final Supplier <String> NEW_REQUEST_ID_PROVIDER = () -> UUID.randomUUID ().toString ();

    protected EPatternType m_ePattern;
    protected EStep m_eStep = EStep.first ();
    protected String m_sRequestID = NEW_REQUEST_ID_PROVIDER.get ();
    // use case
    protected EUseCase m_eUseCase;
    protected ResponseLookupRoutingInformationType m_aIALResponse;
    protected ICommonsSet <String> m_aDOCountries;
    // DE
    protected Agent m_aDE;
    // DO
    protected Agent m_aDO;
    // DRS
    protected MDSCompany m_aDRSCompany;
    protected MDSPerson m_aDRSPerson;

    protected REQT m_aRequestObj;
    protected String m_sRequestTargetURL;
    protected boolean m_bConfirmedToSendRequest;

    protected AbstractSessionState ()
    {}

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
      if (m_aRequestObj == null || !m_bConfirmedToSendRequest)
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
        m_aRequestObj = null;
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
  }

  protected AbstractPageDE_Guided (@Nonnull @Nonempty final String sID,
                                   @Nonnull @Nonempty final String sDisplayName,
                                   @Nonnull final EPatternType ePattern)
  {
    super (sID, sDisplayName, ePattern);
  }

}
