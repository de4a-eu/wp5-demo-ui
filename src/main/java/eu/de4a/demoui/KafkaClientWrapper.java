package eu.de4a.demoui;

import javax.annotation.Nonnull;

import com.helger.commons.error.level.EErrorLevel;

import eu.de4a.kafkaclient.DE4AKafkaClient;
import eu.de4a.kafkaclient.model.ELogMessage;

public final class KafkaClientWrapper
{
  private KafkaClientWrapper ()
  {}

  public static void send (@Nonnull final EErrorLevel eErrorLevel,
                           @Nonnull final ELogMessage eLogMessage,
                           @Nonnull final String sMsg)
  {
    DE4AKafkaClient.send (eErrorLevel, "[" + eLogMessage.getLogCode () + "] [DemoUI] " + sMsg);
  }
}
