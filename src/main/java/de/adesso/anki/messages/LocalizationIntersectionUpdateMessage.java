package de.adesso.anki.messages;

import java.nio.ByteBuffer;

import com.google.common.base.MoreObjects;

/**
 * Notifies the controller that the vehicle has entered / exited an intersection area.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class LocalizationIntersectionUpdateMessage extends Message {
  public static final int TYPE = 0x2a;
  
  private int roadPieceId; // unsigned byte
  private float offsetFromRoadCenter; // float
  
  private int drivingDirection; // unsigned byte
  private int intersectionCode; // unsigned byte
  private int intersectionTurn; // unsigned byte
  private boolean isExiting; // unsigned byte
  
  public LocalizationIntersectionUpdateMessage() {
    this.type = TYPE;
    
    // ...
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.roadPieceId = Byte.toUnsignedInt(buffer.get());
    this.offsetFromRoadCenter = buffer.getFloat();

    this.drivingDirection = Byte.toUnsignedInt(buffer.get());
    this.intersectionCode = Byte.toUnsignedInt(buffer.get());
    this.intersectionTurn = Byte.toUnsignedInt(buffer.get());
    this.isExiting = buffer.get() == 1;
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) this.roadPieceId);
    buffer.putFloat(this.offsetFromRoadCenter);

    buffer.put((byte) this.drivingDirection);
    buffer.put((byte) this.intersectionCode);
    buffer.put((byte) this.intersectionTurn);
    buffer.put((byte) (this.isExiting ? 1 : 0));
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("roadPieceId", this.roadPieceId)
        .add("drivingDirection", this.drivingDirection)
        .add("intersectionCode", this.intersectionCode)
        .add("intersectionTurn", this.intersectionTurn)
        .add("exiting?", isExiting)
        .toString();
  }

  public int getRoadPieceId() {
    return roadPieceId;
  }

  public float getOffsetFromRoadCenter() {
    return offsetFromRoadCenter;
  }

  public int getDrivingDirection() {
    return drivingDirection;
  }

  public int getIntersectionCode() {
    return intersectionCode;
  }

  public int getIntersectionTurn() {
    return intersectionTurn;
  }

  public boolean isExiting() {
    return isExiting;
  }
  
}
