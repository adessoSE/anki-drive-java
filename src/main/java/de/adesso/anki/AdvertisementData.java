package de.adesso.anki;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.xml.bind.DatatypeConverter;

/**
 * Represents the vehicle information published as Bluetooth LE advertisement data.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class AdvertisementData {

  private int identifier;
  private int modelId;
  private int productId;
  private int _reserved;

  private boolean isCharging;

  public AdvertisementData(String manufacturerData, String localName) {

    byte[] data = DatatypeConverter.parseHexBinary(manufacturerData);
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

    productId = buffer.getShort();
    _reserved = buffer.get();
    modelId = buffer.get();
    identifier = buffer.getInt();

    byte[] name = DatatypeConverter.parseHexBinary(localName);
    buffer = ByteBuffer.wrap(name).order(ByteOrder.LITTLE_ENDIAN);

    isCharging = (buffer.get() & 0x40) == 0x40;
  }

  public int getIdentifier() {
    return identifier;
  }

  public int getModelId() {
    return modelId;
  }

  public int getProductId() {
    return productId;
  }

  public Model getModel() {
    return Model.fromId(modelId);
  }

  public boolean isCharging() {
    return isCharging;
  }

  public String toString() {
    return String.format("%s %X", getModel(), Integer.divideUnsigned(getIdentifier(), 0x1000000));
  }
}
