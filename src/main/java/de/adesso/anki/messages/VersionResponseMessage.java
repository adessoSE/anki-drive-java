package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Notifies the controller about the vehicle's current firmware version.
 * Can be requested by sending VersionRequestMessage.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class VersionResponseMessage extends Message {
  public static final int TYPE = 0x19;
  
  private int version; // unsigned short
  
  public VersionResponseMessage() {
    this.type = TYPE;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.version = Short.toUnsignedInt(buffer.getShort());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.putShort((short) this.version);
  }
}
