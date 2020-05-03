package xyz.rgbsec.backend;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import io.lettuce.core.codec.RedisCodec;


public class StringByteCodec implements RedisCodec<String, byte[]> {

	private final byte[] EMPTY = new byte[0];
	private final Charset charset = Charset.forName("UTF-8");

	
	@Override
	public String decodeKey(final ByteBuffer bytes) {
		return charset.decode(bytes).toString();
	}

	@Override
	public byte[] decodeValue(final ByteBuffer bytes) {
		return getBytes(bytes);
	}

	@Override
	public ByteBuffer encodeKey(final String key) {
		return charset.encode(key);
	}

	@Override
	public ByteBuffer encodeValue(final byte[] value) {
		if (value == null) {
			return ByteBuffer.wrap(EMPTY);
		}

		return ByteBuffer.wrap(value);
	}

	private byte[] getBytes(final ByteBuffer buffer) {
		final byte[] b = new byte[buffer.remaining()];
		buffer.get(b);
		return b;
	}

}