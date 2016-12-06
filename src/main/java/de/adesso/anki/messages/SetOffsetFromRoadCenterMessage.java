package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Calibrates the vehicle's offset from road center to the given value.
 * This message will NOT request the vehicle to change its driving lane.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class SetOffsetFromRoadCenterMessage extends Message {
  public static final int TYPE = 0x2c;
  
  private float offsetFromRoadCenter; // float
  
  public SetOffsetFromRoadCenterMessage() {
    this.type = TYPE;
  }
  
  public SetOffsetFromRoadCenterMessage(float offset) {
    this();
    
    this.offsetFromRoadCenter = offset;
  }

  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.offsetFromRoadCenter = buffer.getFloat();
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.putFloat(this.offsetFromRoadCenter);
  }
}
