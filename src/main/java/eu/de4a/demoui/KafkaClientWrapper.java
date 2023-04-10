/*
 * Copyright (C) 2023, Partners of the EU funded DE4A project consortium
 *   (https://www.de4a.eu/consortium), under Grant Agreement No.870635
 * Author:
 *   Austrian Federal Computing Center (BRZ)
 *   Spanish Ministry of Economic Affairs and Digital Transformation -
 *     General Secretariat for Digital Administration (MAETD - SGAD)
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
