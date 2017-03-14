package de.adesso.anki.messages;

import java.nio.ByteBuffer;

import com.google.common.base.MoreObjects;

/**
 * Notifies the controller that the vehicle left the current roadpiece.
 * This message is sent every time the vehicle passes a transition bar.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class LocalizationTransitionUpdateMessage extends Message {
  public static final int TYPE = 0x29;
  
  private int roadPieceId; // unsigned byte
  private int prevRoadPieceId; // unsigned byte
  private float offsetFromRoadCenter; // float
  
  private int drivingDirection; // unsigned byte

  private int lastReceivedLaneChangeId; // unsigned byte
  private int lastExecutedLaneChangeId; // unsigned byte
  private int lastDesiredHorizontalSpeed; // unsigned short
  private int lastDesiredSpeed; // unsigned short
  
  private int uphillCounter; // unsigned byte
  private int downhillCounter; // unsigned byte
  
  private int leftWheelDistance; // unsigned byte
  private int rightWheelDistance; // unsigned byte
  
  public LocalizationTransitionUpdateMessage() {
    this.type = TYPE;
    
    // ...
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.roadPieceId = Byte.toUnsignedInt(buffer.get());
    this.prevRoadPieceId = Byte.toUnsignedInt(buffer.get());
    this.offsetFromRoadCenter = buffer.getFloat();
    
    // Anki removed this field in the latest API version
    if (buffer.remaining() == 11)
      this.drivingDirection = Byte.toUnsignedInt(buffer.get());
    
    this.lastReceivedLaneChangeId = Byte.toUnsignedInt(buffer.get());
    this.lastExecutedLaneChangeId = Byte.toUnsignedInt(buffer.get());
    this.lastDesiredHorizontalSpeed = Short.toUnsignedInt(buffer.getShort());
    this.lastDesiredSpeed = Short.toUnsignedInt(buffer.getShort());

    this.uphillCounter = Byte.toUnsignedInt(buffer.get());
    this.downhillCounter = Byte.toUnsignedInt(buffer.get());

    this.leftWheelDistance = Byte.toUnsignedInt(buffer.get());
    this.rightWheelDistance = Byte.toUnsignedInt(buffer.get());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) this.roadPieceId);
    buffer.put((byte) this.prevRoadPieceId);
    buffer.putFloat(this.offsetFromRoadCenter);

    buffer.put((byte) this.drivingDirection);

    buffer.put((byte) this.lastReceivedLaneChangeId);
    buffer.put((byte) this.lastExecutedLaneChangeId);
    buffer.putShort((short) this.lastDesiredHorizontalSpeed);
    buffer.putShort((short) this.lastDesiredSpeed);

    buffer.put((byte) this.uphillCounter);
    buffer.put((byte) this.downhillCounter);
    
    buffer.put((byte) this.leftWheelDistance);
    buffer.put((byte) this.rightWheelDistance);
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("offset", this.offsetFromRoadCenter)
        .add("drivingDirection", this.drivingDirection)
        .add("leftWheelDistance", this.leftWheelDistance)
        .add("rightWheelDistance", this.rightWheelDistance)
        .toString();
  }

  public int getRoadPieceId() {
    return roadPieceId;
  }

  public int getPrevRoadPieceId() {
    return prevRoadPieceId;
  }

  public float getOffsetFromRoadCenter() {
    return offsetFromRoadCenter;
  }

  public int getDrivingDirection() {
    return drivingDirection;
  }

  public int getLastReceivedLaneChangeId() {
    return lastReceivedLaneChangeId;
  }

  public int getLastExecutedLaneChangeId() {
    return lastExecutedLaneChangeId;
  }

  public int getLastDesiredHorizontalSpeed() {
    return lastDesiredHorizontalSpeed;
  }

  public int getLastDesiredSpeed() {
    return lastDesiredSpeed;
  }

  public int getUphillCounter() {
    return uphillCounter;
  }

  public int getDownhillCounter() {
    return downhillCounter;
  }

  public int getLeftWheelDistance() {
    return leftWheelDistance;
  }

  public int getRightWheelDistance() {
    return rightWheelDistance;
  }
  
}
