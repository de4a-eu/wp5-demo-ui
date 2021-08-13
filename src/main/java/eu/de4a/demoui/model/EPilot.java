package eu.de4a.demoui.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.commons.name.IHasDisplayName;

public enum EPilot implements IHasID <String>, IHasDisplayName
{
  STUDYING_ABROAD ("t41", "Studying Abroad"),
  DOING_BUSINESS_ABROAD ("t42", "Doing Business Abroad"),
  MOVING_ABROAD ("t43", "Moving Abroad");

  private final String m_sID;
  private final String m_sDisplayName;

  EPilot (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sDisplayName)
  {
    m_sID = sID;
    m_sDisplayName = sDisplayName;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nonnull
  @Nonempty
  public String getDisplayName ()
  {
    return m_sDisplayName;
  }

  @Nullable
  public static EPilot getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EPilot.class, sID);
  }
}
