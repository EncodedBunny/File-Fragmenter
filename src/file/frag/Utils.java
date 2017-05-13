package file.frag;

import java.nio.ByteBuffer;

public class Utils {
	private static ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
	
	public static byte[] longToBytes(long l){
		byte[] b = byteBuffer.putLong(l).array();
		byteBuffer.clear();
		return b;
	}
	
	public static long bytesToLong(byte[] b){
		long l = byteBuffer.put(b).getLong();
		byteBuffer.clear();
		return l;
	}
	
	public static int byteValueOf(int value, ByteSize size){
		return value * size.bytes();
	}
}