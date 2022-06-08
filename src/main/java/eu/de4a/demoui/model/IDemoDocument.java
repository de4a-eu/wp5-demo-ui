package eu.de4a.demoui.model;

import javax.annotation.Nonnull;

import com.helger.commons.error.list.IErrorList;

public interface IDemoDocument
{
  @Nonnull
  Object createDemoRequest ();

  @Nonnull
  String getAnyMessageAsString (@Nonnull Object aObj);

  @Nonnull
  IErrorList validateMessage (@Nonnull String sMsg);

  @Nonnull
  Object parseMessage (@Nonnull String sMsg);

  @Nonnull
  IErrorList validateMessageBackwards (@Nonnull String sMsg);

  @Nonnull
  Object parseMessageBackwards (@Nonnull String sMsg);
}
