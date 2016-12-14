package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Notifies the controller about the vehicle's current battery level.
 * Can be requested by sending BatteryLevelRequestMessage.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class BatteryLevelResponseMessage extends Message {
  public static final int TYPE = 0x1b;

  private int batteryLevel; // unsigned short

  public BatteryLevelResponseMessage() {
    this.type = TYPE;
  }

  /**
   * Returns the vehicle's current battery level.
   * 
   * @return battery level in mV
   */
  public int getBatteryLevel() {
    return batteryLevel;
  }

  @Override
  public void parsePayload(ByteBuffer buffer) {
    this.batteryLevel = Short.toUnsignedInt(buffer.getShort());
  }

  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) batteryLevel);
  }
}
