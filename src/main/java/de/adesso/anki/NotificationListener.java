package de.adesso.anki;

import java.util.EventListener;

/**
 * The listener interface for receiving lines from NotificationReader.
 *
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public interface NotificationListener extends EventListener {
  public void onReceive(String line);
}
