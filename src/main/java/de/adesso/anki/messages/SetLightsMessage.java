package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Directly sets the vehicle's light state to the given value.
 * It is recommended to use LightsPatternMessage instead.
 * 
 * (not yet implemented)
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class SetLightsMessage extends Message {
  public static final int TYPE = 0x1d;
  
  private byte lightMask; // unsigned byte
  
  public SetLightsMessage() {
    this.type = TYPE;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.lightMask = buffer.get();
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put(lightMask);
  }
}
