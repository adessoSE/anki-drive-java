package de.adesso.anki;

import java.io.IOException;
import java.time.LocalTime;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

import de.adesso.anki.messages.Message;

// TODO: Manage connection status and fail gracefully if disconnected

/**
 * Represents a vehicle and allows communicating with it.
 *
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class Vehicle {
  
  private String address;
  private AdvertisementData advertisement;
  
  private AnkiConnector anki;
  
  private Multimap<Class<? extends Message>, MessageListener> listeners;
  private MessageListener defaultListener;
  
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public AdvertisementData getAdvertisement() {
    return advertisement;
  }

  public void setAdvertisement(AdvertisementData advertisement) {
    this.advertisement = advertisement;
  }

  @Override
  public String toString() {
    return advertisement.toString();
  }
  
  public void connect() {
    try {
      int count = 0;
      int maxTries = 5;
      boolean connected = false;
      
      while (!connected) {
        try {
          anki.connect(this);
          connected = true;
        } catch (RuntimeException e) {
          if (++count == maxTries)
            throw e;
        }
      }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    defaultListener = (message) -> fireMessageReceived(message);
    anki.addMessageListener(this, defaultListener);
  }
  
  public void disconnect() {
	anki.removeMessageListener(this, defaultListener);
    anki.disconnect(this);
  }
  
  public void sendMessage(Message message) {
    anki.sendMessage(this, message);
    System.out.println(String.format("[%s] > %s: %s", LocalTime.now(), this, message));
  }
  
  @Deprecated
  public void addMessageListener(MessageListener listener) {
    this.addMessageListener(Message.class, listener);
  }
  
  @Deprecated
  public void removeMessageListener(MessageListener listener) {
    this.removeMessageListener(Message.class, listener);
  }
  
  public <T extends Message> void addMessageListener(Class<T> klass, MessageListener<T> listener) {
    this.listeners.put(klass, listener);
  }
  
  public <T extends Message> void removeMessageListener(Class<T> klass, MessageListener<T> listener) {
    this.listeners.remove(klass, listener);
  }
  
  private <T extends Message> void fireMessageReceived(T message) {
    for (MessageListener<T> l : this.listeners.get(Message.class)) {
      l.messageReceived(message);
    }
    if (message.getClass() != Message.class) {
      for (MessageListener<T> l : this.listeners.get(message.getClass())) {
        l.messageReceived(message);
      }
    }
  }
  
  public Vehicle(AnkiConnector anki, String address, String manufacturerData, String localName) {
    try {
		this.anki = new AnkiConnector(anki);
	} catch (IOException e) {
		this.anki = anki;
	}
    this.address = address;
    this.advertisement = new AdvertisementData(manufacturerData, localName);
    
    this.listeners = LinkedListMultimap.create();
  }

  /**
   * Returns the color of the vehicle.
   * Update 3/31/19: added functionality to cope with missing color attribute in previously unknown models.
   * @author Yannick Eckey <yannick.eckey@adesso.de>
   * @author Bastian Tenbergen <bastian.tenbergen@oswego.edu>
   * @return The color of the vehicle or some error string.
   * @version 2019-03-31
   */
  public String getColor() {
    if (advertisement == null) return "ERROR! Advertisement is null.";
    else if (advertisement.getModel() == null) return "ERROR! unknown model";
    else if (advertisement.getModel().getColor() == null) return "unkown";
    else return advertisement.getModel().getColor();
  }
  
  @Override
  public int hashCode() {
	return address.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Vehicle){
    	return ((Vehicle) obj).getAddress().equals(this.getAddress());
    }
    return false;
  }
}
