package de.adesso.anki.messages;

import java.nio.ByteBuffer;

/**
 * Requests the vehicle to change into SDK mode.
 * The controller MUST send this message directly after connecting.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class SdkModeMessage extends Message {
  public static final int TYPE = 0x90;
  
  private boolean on; // unsigned byte
  private byte flags; // unsigned byte
  
  public SdkModeMessage() {
    this.type = TYPE;
    
    this.on = true;
    this.flags = 1;
  }
  
  public SdkModeMessage(boolean on, byte flags) {
    this();
    
    this.on = on;
    this.flags = flags;
  }
  
  @Override
  protected void parsePayload(ByteBuffer buffer) {
    this.on = buffer.get() == 1;
    this.flags = buffer.get();
  }
  
  @Override
  protected void preparePayload(ByteBuffer buffer) {
    buffer.put((byte) (this.on ? 1 : 0));
    buffer.put(this.flags);
  }
}
