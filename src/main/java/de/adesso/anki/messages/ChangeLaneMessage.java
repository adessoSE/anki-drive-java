package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Requests the vehicle to change its driving lane according to the given parameters.
 * It is recommended to send SetOffsetFromRoadCenterMessage before sending this message.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class ChangeLaneMessage extends Message {
  public static final int TYPE = 0x25;

  private int horizontalSpeed; // unsigned short
  private int horizontalAcceleration; // unsigned short
  private float offsetFromCenter; // float

  private boolean hopIntent; // unsigned byte
  private int tag; // unsigned byte

  protected ChangeLaneMessage() {
    this.type = TYPE;
  }

  /**
   * Creates a new SetOffsetFromRoadCenterMessage with the given parameters.
   * 
   * @param offsetFromCenter desired offset from road center in mm
   * @param horizontalSpeed desired horizontal speed in mm/s
   * @param horizontalAccel desired horizontal acceleration in mm/s^2
   */
  public ChangeLaneMessage(float offsetFromCenter, int horizontalSpeed, int horizontalAccel) {
    this.type = TYPE;

    this.horizontalSpeed = horizontalSpeed;
    this.horizontalAcceleration = horizontalAccel;
    this.offsetFromCenter = offsetFromCenter;
  }

  public int getHorizontalSpeed() {
    return horizontalSpeed;
  }

  public int getHorizontalAcceleration() {
    return horizontalAcceleration;
  }

  public float getOffsetFromCenter() {
    return offsetFromCenter;
  }

  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.horizontalSpeed = Short.toUnsignedInt(buffer.getShort());
    this.horizontalAcceleration = Short.toUnsignedInt(buffer.getShort());
    this.offsetFromCenter = buffer.getFloat();

    this.hopIntent = buffer.get() == 1;
    this.tag = Byte.toUnsignedInt(buffer.get());
  }

  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.putShort((short) this.horizontalSpeed);
    buffer.putShort((short) this.horizontalAcceleration);
    buffer.putFloat(this.offsetFromCenter);

    buffer.put((byte) (this.hopIntent ? 1 : 0));
    buffer.put((byte) this.tag);
  }
}
