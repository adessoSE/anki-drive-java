package de.adesso.anki.messages;

/**
 * Notifies the controller that the vehicle has lost its localization on the track.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class VehicleDelocalizedMessage extends Message {
  public static final int TYPE = 0x2b;

  public VehicleDelocalizedMessage() {
    this.type = TYPE;
  }
}
