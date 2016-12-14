package de.adesso.anki.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.reflections.Reflections;

import com.google.common.base.MoreObjects;

/**
 * Represents a Message following the Anki Communication Protocol that can be sent / received via
 * the Bluetooth LE connection.
 * 
 * This class is subtyped for each currently documented message type.
 * Unknown message types are represented by their type identifier and their binary payload.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class Message {
  private final static Reflections reflections = new Reflections("de.adesso.anki.messages");

  protected int type;
  private byte[] payload;

  protected Message() {}

  protected Message(int type) {
    this.type = type;
  }

  public Message(int type, byte[] payload) {
    this.type = type;
    this.payload = payload;
  }

  public static Message parse(String hexMessage) {
    byte[] data = DatatypeConverter.parseHexBinary(hexMessage);
    ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);

    int size = Byte.toUnsignedInt(buffer.get());
    int type = Byte.toUnsignedInt(buffer.get());

    Message m = Message.createByType(type);

    m.payload = new byte[buffer.remaining()];
    buffer.get(m.payload).position(2);
    m.parsePayload(buffer);

    return m;
  }

  private static Message createByType(int type) {
    Set<Class<? extends Message>> messages = reflections.getSubTypesOf(Message.class);

    for (Class<? extends Message> message : messages) {
      try {
        if (message.getField("TYPE").getInt(null) == type) {
          return message.newInstance();
        }
      } catch (NoSuchFieldException | IllegalAccessException | InstantiationException e) {
        // just skip the Message subclass if there is no TYPE constant
        // or it cannot be instantiated
      }
    }

    return new Message(type);
  }

  public String toHex() {
    ByteBuffer buffer = ByteBuffer.allocate(20).order(ByteOrder.LITTLE_ENDIAN);
    buffer.position(2);
    preparePayload(buffer);
    buffer.flip();

    buffer.put((byte) (buffer.remaining() - 1));
    buffer.put((byte) type);

    byte[] data = new byte[buffer.limit()];
    buffer.rewind();
    buffer.get(data);

    return DatatypeConverter.printHexBinary(data);
  }

  protected void preparePayload(ByteBuffer buffer) {
    if (this.getClass() == Message.class) {
      buffer.put(payload);
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("type", Integer.toHexString(this.type))
        .add("payload", payload == null ? null : DatatypeConverter.printHexBinary(payload))
        .toString();
  }

  protected void parsePayload(ByteBuffer buffer) {
    this.payload = new byte[buffer.remaining()];
    buffer.get(this.payload);
  }

  public static void main(String[] args) {
    Message m = new Message(0x18, new byte[0]);
    Message m2 = Message.parse("10272114b5f386c2f401470000fa00f401");
    System.out.println(m2.toHex());
    Message.parse("122900007b1495c200ffff0000000000002221");
  }
}
