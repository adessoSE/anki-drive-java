package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Notifies the controller that the vehicle deviated from its desired driving lane.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class OffsetFromRoadCenterUpdateMessage extends Message {
  public static final int TYPE = 0x2d;
  
  private float offsetFromRoadCenter; // float
  private int laneChangeId; // unsigned byte
  
  public OffsetFromRoadCenterUpdateMessage() {
    this.type = TYPE;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.offsetFromRoadCenter = buffer.getFloat();
    this.laneChangeId = Byte.toUnsignedInt(buffer.get());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.putFloat(this.offsetFromRoadCenter);
    buffer.put((byte) this.laneChangeId);
  }
}
