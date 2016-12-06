package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Sets some of the vehicle's configuration parameters
 * to improve track parsing on third party tracks.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class SetConfigParamsMessage extends Message {
  public static final int TYPE = 0x45;
  
  private byte superCodeParseMask; // unsigned byte
  private int trackMaterial; // unsigned byte
  
  public SetConfigParamsMessage() {
    this.type = TYPE;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.superCodeParseMask = buffer.get();
    this.trackMaterial = Byte.toUnsignedInt(buffer.get());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put(this.superCodeParseMask);
    buffer.put((byte) this.trackMaterial);
  }
}
