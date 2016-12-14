package de.adesso.anki.messages;

/**
 * Requests the vehicle to send its current battery level.
 * Vehicle will respond with BatteryLevelResponseMessage.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class BatteryLevelRequestMessage extends Message {
  public static final int TYPE = 0x1a;
  
  public BatteryLevelRequestMessage() {
    this.type = TYPE;
  }
}
