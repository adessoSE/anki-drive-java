package de.adesso.anki.messages;

import java.nio.ByteBuffer;

public class VehicleInfoMessage extends Message {
	public static final int TYPE = 0x3f;

	boolean charging;
	boolean onTrack;
	
	//TODO: There are two more unknown bytes...

	public VehicleInfoMessage() {
		this.type = TYPE;
	}

	@Override
	protected void parsePayload(ByteBuffer buffer) {
		this.onTrack = getBoolean(buffer);
		this.charging = getBoolean(buffer);
	}

	private boolean getBoolean(ByteBuffer buffer) {
		return buffer.get() != 0x00;
	}

	@Override
	protected void preparePayload(ByteBuffer buffer) {
		putBoolean(buffer, onTrack);
	}

	private void putBoolean(ByteBuffer buffer, boolean boolValue) {
		byte value = 0x00;
		if(boolValue){
			value = 0x01;
		}
		buffer.put(value);
	}
}
