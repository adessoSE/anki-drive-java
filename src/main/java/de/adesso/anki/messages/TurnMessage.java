package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Requests the vehicle to perform a turn operation.
 * Currently only immediate u-turns are supported by Anki firmware.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class TurnMessage extends Message {
  public static final int TYPE = 0x32;
  
  private int turnType; // unsigned byte
  private int trigger; // unsigned byte
  
  public TurnMessage() {
    this.type = TYPE;
  }
  
  /**
   * Allows the user to request the vehicle to turn. Currently, according to the manufacturer's API, only U-turns (180deg turns) are supported by the ANKI vehicles.
   * @param turnType The "type" of turn. U-Turns are type 3. Other types currently unkown.
   * @param trigger When to turn. "1" means immediately, "0" means upon transition to next RoadPiece.
   */
  public TurnMessage(int turnType, int trigger) {
    this();
    
    this.turnType = turnType;
    this.trigger = trigger;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.turnType = Byte.toUnsignedInt(buffer.get());
    this.trigger = Byte.toUnsignedInt(buffer.get());
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) this.turnType);
    buffer.put((byte) this.trigger);
  }
  
  public String toMqttString(){
	  return "turn";
  }
}
