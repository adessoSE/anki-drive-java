package de.adesso.anki.messages;

import java.nio.ByteBuffer;

import com.google.common.base.MoreObjects;

/**
 * Notifies the controller about the vehicle's current position.
 * This message is sent every time the vehicle parses a location code.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class LocalizationPositionUpdateMessage extends Message {
  public static final int TYPE = 0x27;
  
  private int locationId; // unsigned byte
  private int roadPieceId; // unsigned byte
  private float offsetFromRoadCenter; // float
  private int speed; // unsigned short
  private byte parsingFlags; // unsigned byte
  
  private int lastReceivedLaneChangeId; // unsigned byte
  private int lastExecutedLaneChangeId; // unsigned byte
  private int lastDesiredHorizontalSpeed; // unsigned short
  private int lastDesiredSpeed; // unsigned short
  
  public LocalizationPositionUpdateMessage() {
    this.type = TYPE;
    
    // ...
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.locationId = Byte.toUnsignedInt(buffer.get());
    this.roadPieceId = Byte.toUnsignedInt(buffer.get());
    this.offsetFromRoadCenter = buffer.getFloat();
    this.speed = Short.toUnsignedInt(buffer.getShort());
    this.parsingFlags = buffer.get();
    
    this.lastReceivedLaneChangeId = Byte.toUnsignedInt(buffer.get());
    this.lastExecutedLaneChangeId = Byte.toUnsignedInt(buffer.get());
    this.lastDesiredHorizontalSpeed = Short.toUnsignedInt(buffer.getShort());
    this.lastDesiredSpeed = Short.toUnsignedInt(buffer.getShort());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) this.locationId);
    buffer.put((byte) this.roadPieceId);
    buffer.putFloat(this.offsetFromRoadCenter);
    buffer.putShort((short) this.speed);
    buffer.put(this.parsingFlags);

    buffer.put((byte) this.lastReceivedLaneChangeId);
    buffer.put((byte) this.lastExecutedLaneChangeId);
    buffer.putShort((short) this.lastDesiredHorizontalSpeed);
    buffer.putShort((short) this.lastDesiredSpeed);
  }
  
  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("locationId", this.locationId)
        .add("roadPieceId", this.roadPieceId)
        .add("offset", this.offsetFromRoadCenter)
        .add("speed", this.speed)
        .add("reverse", isParsedReverse())
        .toString();
  }

  public int getLocationId() {
    return locationId;
  }

  public int getRoadPieceId() {
    return roadPieceId;
  }

  public float getOffsetFromRoadCenter() {
    return offsetFromRoadCenter;
  }

  public int getSpeed() {
    return speed;
  }

  public byte getParsingFlags() {
    return parsingFlags;
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
  
  public boolean isParsedReverse() {
    return (parsingFlags & 0x40) == 0x40;
  }
  
  
}
