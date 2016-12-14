package de.adesso.anki.messages;

/**
 * Requests the vehicle to cancel the last received lane change request.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class CancelLaneChangeMessage extends Message {
  public static final int TYPE = 0x26;
  
  public CancelLaneChangeMessage() {
    this.type = TYPE;
  }
}
