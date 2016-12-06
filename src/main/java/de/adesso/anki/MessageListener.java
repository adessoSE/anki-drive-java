package de.adesso.anki;

import java.util.EventListener;

import de.adesso.anki.messages.Message;

/**
 * The listener interface for receiving messages from vehicles.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public interface MessageListener<T extends Message> extends EventListener {
  public void messageReceived(T message);
}
