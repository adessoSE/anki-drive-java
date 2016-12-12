package de.adesso.anki.messages;

import java.nio.ByteBuffer;

public class VehicleInfoMessage extends Message {
	public static final int TYPE = 0x3f;

	boolean charging;
	boolean onTrack;
	// TODO: we don't know the meaning of the following 2 bytes...
	byte _reserved1;
	byte _reserved2;

	public VehicleInfoMessage() {
		this.type = TYPE;
	}

	@Override
	protected void parsePayload(ByteBuffer buffer) {
		this.onTrack = getBoolean(buffer);
		this.charging = getBoolean(buffer);
		this._reserved1 = buffer.get();
		this._reserved2 = buffer.get();
	}

	private boolean getBoolean(ByteBuffer buffer) {
		return buffer.get() != 0x00;
	}

	@Override
	protected void preparePayload(ByteBuffer buffer) {
		putBoolean(buffer, onTrack);
		putBoolean(buffer, charging);
		buffer.put(_reserved1);
		buffer.put(_reserved2);
	}

	private void putBoolean(ByteBuffer buffer, boolean boolValue) {
		byte value = 0x00;
		if (boolValue) {
			value = 0x01;
		}
		buffer.put(value);
	}
}
