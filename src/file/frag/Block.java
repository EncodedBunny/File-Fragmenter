package file.frag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Block {
	private String memoryData, blockID;
	private Path hdData;
	private boolean saveToMemory;
	private int size, index;
	
	public Block(int size, int index, String id){
		this(size, true, null, null, index, id);
	}
	
	public Block(int size, Path saveFolder, int index, String id){
		this(size, false, saveFolder, "frag_", index, id);
	}
	
	public Block(int size, Path saveFolder, String fileName, int index, String id){
		this(size, false, saveFolder, fileName, index, id);
	}
	
	private Block(int size, boolean saveToMemory, Path hdFolder, String fileName, int index, String id){
		this.saveToMemory = saveToMemory;
		this.size = size;
		this.blockID = id;
		this.index = index;
		if(!saveToMemory)
			hdData = hdFolder.resolve(fileName + blockID + "-" + index);
	}
	
	/**
	 * Sets the contents of this <code>Block</code>, normally this method should only be invoked by the {@link file.frag.FileFragmenter FileFragmenter} class
	 * @param block - The contents to be written to this <code>Block</code>
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
	 * Gets the ID used by this <code>Block</code>
	 * <p>
	 * <b>Note:</b> This ID is added to the middle of the save file name in case this <code>Block</code> is saving it's information in the HD
	 * @return The last 12 characters of a MD5 hash of the <code>File.getAbsolutePath().getBytes()</code> invoked at this <code>Block</code> instantiation
	 */
	public String getID(){
		return blockID;
	}
	
	/**
	 * Gets the index of this <code>Block</code> in respect to it's original <code>File</code>
	 * <p>
	 * <b>Note:</b> The value of this index ranges from 0 to the number of fragments - 1
	 * @return The index representing this <code>Block</code>
	 */
	public int getIndex(){
		return index;
	}
}