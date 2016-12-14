package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Requests the vehicle to change its current speed to the given parameters.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class SetSpeedMessage extends Message {
  public static final int TYPE = 0x24;
  
  private int speed;
  private int acceleration;
  private boolean respectRoadPieceSpeedLimit;
  
  protected SetSpeedMessage() {
    this.type = TYPE;
  }
      
  public SetSpeedMessage(int speed, int acceleration) {
    this();
    
    this.speed = speed;
    this.acceleration = acceleration;
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.putShort((short) this.speed);
    buffer.putShort((short) this.acceleration);
    buffer.put((byte) (this.respectRoadPieceSpeedLimit ? 1 : 0));
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.speed = buffer.getShort();
    this.acceleration = buffer.getShort();
    this.respectRoadPieceSpeedLimit = buffer.get() == 1;
  }
}
