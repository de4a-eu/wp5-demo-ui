package eu.de4a.demoui.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.builder.IBuilder;

/**
 * Minimum Data Set for a Company
 *
 * @author Philip Helger
 */
@Immutable
public class MDSCompany
{
  private final String m_sID;
  private final String m_sName;

  public MDSCompany (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sName)
  {
    ValueEnforcer.notEmpty (sID, "ID");
    ValueEnforcer.notEmpty (sName, "Name");
    m_sID = sID;
    m_sName = sName;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nonnull
  @Nonempty
  public String getName ()
  {
    return m_sName;
  }

  @Nonnull
  public static MDSCompany.Builder builder ()
  {
    return new MDSCompany.Builder ();
  }

  public static class Builder implements IBuilder <MDSCompany>
  {
    private String m_sID;
    private String m_sName;

    public Builder ()
    {}

    @Nonnull
    public MDSCompany.Builder id (@Nullable final String s)
    {
      m_sID = s;
      return this;
    }

    @Nonnull
    public MDSCompany.Builder name (@Nullable final String s)
    {
      m_sName = s;
      return this;
    }

    @Nonnull
    public MDSCompany build ()
    {
      return new MDSCompany (m_sID, m_sName);
    }
  }
}
