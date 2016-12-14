package de.adesso.anki.messages;

/**
 * Requests a vehicle to respond with a PingResponseMessage.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class PingRequestMessage extends Message {
  public static final int TYPE = 0x16;
  
  public PingRequestMessage() {
    this.type = TYPE;
  }
}
