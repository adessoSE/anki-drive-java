package de.adesso.anki.messages;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Requests the vehicle to change its light pattern according to the given
 * config.
 * 
 * @author Yannick Eckey <yannick.eckey@adesso.de>
 */
public class LightsPatternMessage extends Message {
	public static final int TYPE = 0x33;

	private List<LightConfig> channelConfig;

	public LightsPatternMessage() {
		this.type = TYPE;
		this.channelConfig = new ArrayList<LightConfig>();
	}

	public void add(LightConfig config) {
		this.channelConfig.add(config);
	}

	@Override
	protected void parsePayload(ByteBuffer buffer) {
		int channelCount = Byte.toUnsignedInt(buffer.get());
		for (int i = 0; i < channelCount; i++) {
			byte[] rawConfig = new byte[5];
			buffer.get(rawConfig, 0, 5);
			this.channelConfig.add(LightConfig.fromBytes(rawConfig));
		}
	}

	@Override
	protected void preparePayload(ByteBuffer buffer) {
		buffer.put((byte) this.channelConfig.size());
		for (LightConfig config : channelConfig) {
			byte[] rawConfig = config.toBytes();
			buffer.put(rawConfig);
		}
	}

	public static class LightConfig {

		private LightChannel channel;
		private LightEffect effect;
		private int start;
		private int end;
		private int cycles;

		public LightConfig(LightChannel channel, LightEffect effect, int start, int end, int cycles) {
			this.channel = channel;
			this.effect = effect;
			this.start = start;
			this.end = end;
			this.cycles = cycles;
		}

		public static LightConfig fromBytes(byte[] rawConfig) {
			return new LightConfig(LightChannel.fromByte(rawConfig[0]), LightEffect.fromByte(rawConfig[1]),
					Byte.toUnsignedInt(rawConfig[2]), Byte.toUnsignedInt(rawConfig[3]),
					Byte.toUnsignedInt(rawConfig[4]));
		}

		public byte[] toBytes() {
			return new byte[] { (byte) channel.ordinal(), (byte) effect.ordinal(), (byte) start, (byte) end,
					(byte) cycles };
		}
	}

	public enum LightChannel {
		ENGINE_RED, TAIL, ENGINE_BLUE, ENGINE_GREEN, FRONT_RED, FRONT_GREEN;

		private static final LightChannel[] VALUES = LightChannel.values();

		public static LightChannel fromByte(byte b) {
			return VALUES[Byte.toUnsignedInt(b)];
		}
	}

	public enum LightEffect {
		STEADY, // Simply set the light intensity to 'start' value
		FADE, // Fade intensity from 'start' to 'end'
		THROB, // Fade intensity from 'start' to 'end' and back to 'start'
		FLASH, // Turn on LED between time 'start' and time 'end' inclusive
		STROBE; // Flash the LED erratically - ignoring start/end

		private static final LightEffect[] VALUES = LightEffect.values();

		public static LightEffect fromByte(byte b) {
			return VALUES[Byte.toUnsignedInt(b)];
		}
	}
}
