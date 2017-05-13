package file.frag;

public enum ByteSize {
	KB(1024), MB(KB.bytes() * 1024),GB(MB.bytes() * 1024),TB(GB.bytes() * 1024);
	
	private final int size;
	ByteSize(int size) { this.size = size; }
	public int bytes(){ return size; }
}