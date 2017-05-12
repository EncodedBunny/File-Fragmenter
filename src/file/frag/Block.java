package file.frag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Block {
	private String memoryData, blockID;
	private Path hdData;
	private boolean saveToMemory;
	private int size;
	
	public Block(int size){
		this(size, true, null, null);
	}
	
	public Block(int size, Path saveFolder){
		this(size, false, saveFolder, "frag_");
	}
	
	public Block(int size, Path saveFolder, String fileName){
		this(size, false, saveFolder, fileName);
	}
	
	private Block(int size, boolean saveToMemory, Path hdFolder, String fileName){
		this.saveToMemory = saveToMemory;
		this.size = size;
		try {
			blockID = new BigInteger(1, MessageDigest.getInstance("MD5").digest(Utils.longToBytes(System.nanoTime()))).toString(16).substring(24);
		} catch (NoSuchAlgorithmException e) {} // Not going to happen :)
		if(saveToMemory)
			hdData = hdFolder.resolve(fileName + blockID);
	}
	
	/**
	 * Sets the contents of this <code>Block</code>, normally this method should only be invoked by the {@link file.frag.FileFragmenter FileFragmenter} class
	 * @param block The contents to be written to this <code>Block</code>
	 * @return This instance of <code>Block</code> for chaining
	 * @throws IOException If this <code>Block</code> is saving it's contents to a file and an exception occurs, this exception can be safely ignored if this <code>Block</code> is saving it's contents to memory
	 */
	public Block setContents(char[] block) throws IOException{
		if(saveToMemory)
			memoryData = new String(block);
		else{
			if(Files.exists(hdData))
				throw new IOException("Could not create '" + hdData.getFileName().toString() + "': File already exists");
			hdData = Files.createFile(hdData);
			BufferedWriter bw = Files.newBufferedWriter(hdData);
			bw.write(block);
			bw.close();
		}
		return this;
	}
	
	/**
	 * Gets the contents of this <code>Block</code>, either from memory or from the HD
	 * @return The contents of this <code>Block</code>, or <code>null</code> in case an error occurs
	 */
	public char[] getContents(){
		if(saveToMemory)
			return memoryData.toCharArray();
		else{
			char[] contents = new char[size];
			BufferedReader br;
			try{
				br = Files.newBufferedReader(hdData);
				br.read(contents);
				br.close();
			} catch(IOException e){
				return null;
			}
			return contents;
		}
	}
	
	/**
	 * Gets the unique ID for this <code>Block</code>
	 * <p>
	 * <b>Note:</b> This ID is added to the end of the save file name in case this <code>Block</code> is saving it's information in the HD
	 * @return The last 8 characters of a MD5 hash of the <code>System.nanoTime()</code> invoked at this <code>Block</code> constructor
	 */
	public String getID(){
		return blockID;
	}
}