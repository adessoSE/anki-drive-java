package de.adesso.anki.messages;

/**
 * Requests the vehicle to send its current firmware version.
 * Vehicle will respond with VersionResponseMessage.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class VersionRequestMessage extends Message {
  public static final int TYPE = 0x18;
  
  public VersionRequestMessage() {
    this.type = TYPE;
  }
}
