package file.frag;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import file.frag.exception.ImpossibleFileFragmentationException;

public class FileFragmenter {
	/**
	 * Fragments a file in a specified number of pieces and saves the fragmented pieces to memory
	 * @param file - The <code>File</code> to be fragmented
	 * @param pieces - The number of pieces to fragment
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException The specified number of pieces is smaller than the actual file size
	 * @throws IOException The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFile(File file, int pieces) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFile(file, pieces, true, null, null);
	}
	
	/**
	 * Fragments a file in a specified number of pieces and saves the fragmented pieces to the HD with the default file name: <pre>"frag_" + Block.getID() + "-" + Block.getIndex()</pre>
	 * @param file - The <code>File</code> to be fragmented
	 * @param pieces - The number of pieces to fragment
	 * @param saveFolder - The folder that will contain all the fragmented pieces
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException If the specified number of pieces is smaller than the actual file size
	 * @throws IOException If the file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFile(File file, int pieces, Path saveFolder) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFile(file, pieces, false, saveFolder, null);
	}
	
	/**
	 * Fragments a file in a specified number of pieces and saves the fragmented pieces to the HD with a specified file base name
	 * @param file - The <code>File</code> to be fragmented
	 * @param pieces - The number of pieces to fragment
	 * @param saveFolder - The folder that will contain all the fragmented pieces
	 * @param fileName - The base file name for the fragmented pieces (the individual block ID of each piece will be appended to this file name)
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException If the specified number of pieces is smaller than the actual file size
	 * @throws IOException If the file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFile(File file, int pieces, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFile(file, pieces, false, saveFolder, fileName);
	}
	
	/**
	 * Fragments a file dynamically, creating how many fragments necessary with each having a defined maximum size, and saves the fragmented pieces to memory
	 * @param file - The <code>File</code> to be fragmented
	 * @param maxBlockSize - The maximum size that each block/fragment of the file may have
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException The specified maximum block size is negative, zero or NaN
	 * @throws IOException The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFileDynamically(File file, int maxBlockSize) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, maxBlockSize, true, null, null);
	}
	
	/**
	 * Fragments a file dynamically, creating how many fragments necessary with each having a defined maximum size, and saves the fragmented pieces to the HD
	 * @param file - The <code>File</code> to be fragmented
	 * @param maxBlockSize - The maximum size that each block/fragment of the file may have
	 * @param saveFolder - The folder that will contain all the fragmented pieces
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException The specified maximum block size is negative, zero or NaN
	 * @throws IOException The file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFileDynamically(File file, int maxBlockSize, Path saveFolder) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, maxBlockSize, false, saveFolder, null);
	}
	
	public static Block[] fragmentFileDynamically(File file, int maxBlockSize, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, maxBlockSize, false, saveFolder, fileName);
	}
	
	public static File defragmentFile(Path fragmentsFolder, Path saveFile) throws IOException{
		File folder = fragmentsFolder.toFile();
		File[] frags = folder.listFiles();
		if(!folder.exists() || !folder.isDirectory() || !folder.canRead() || frags == null)
			throw new IOException();
		boolean complete;
		do{
			complete = true;
			for(int x = 0; x < frags.length - 1; x++){
				if(getIndexOf(frags[x]) > getIndexOf(frags[x+1])){
					File tmp = frags[x];
					frags[x] = frags[x+1];
					frags[x+1] = tmp;
					complete = false;
				}
			}
		} while(!complete);
		File file = saveFile.toFile();
		if(!file.exists())
			Files.createFile(saveFile);
		if(!file.canRead() || !file.isFile())
			throw new IOException();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		for(int x = 0; x < frags.length; x++)
			for(String s : Files.readAllLines(frags[x].toPath())){
				bw.append(s);
				if(x != frags.length-1)
					bw.newLine();
			}
		bw.close();
		return file;
	}
	
	public static File defragmentFile(Block[] blocks, Path saveFile) throws IOException{
		File file = saveFile.toFile();
		if(!file.exists())
			Files.createFile(saveFile);
		if(!file.canRead() || !file.isFile())
			throw new IOException();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
		for(Block b : blocks)
			bw.append(new String(b.getContents()));
		bw.close();
		return file;
	}
	
	private static Block[] fragmentFile(File file, int pieces, boolean saveToMemory, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		if(!file.exists() || !file.canRead() || !file.isFile())
			throw new IOException();
		long size = file.length();
		String fileID = "";
		try{
			fileID = new BigInteger(1, MessageDigest.getInstance("MD5").digest(file.getAbsolutePath().getBytes())).toString(16).substring(20);
		} catch(NoSuchAlgorithmException e){}
		if(size < pieces)
			throw new ImpossibleFileFragmentationException("Number of pieces larger than file size");
		if(saveFolder != null && !Files.exists(saveFolder))
			Files.createDirectory(saveFolder);
		return splitIntoBlocks(file, (int) Math.floor((double)size/pieces), pieces, (int) (size % pieces), saveToMemory, false, saveFolder, fileName, fileID);
	}
	
	private static Block[] fragmentFileDynamically(File file, long maxBlockSize, boolean saveToMemory, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		if(!file.exists() || !file.canRead() || !file.isFile())
			throw new IOException();
		if(maxBlockSize <= 0)
			throw new ImpossibleFileFragmentationException("Invalid max block size '" + maxBlockSize + "'");
		if(saveFolder != null && !Files.exists(saveFolder))
			Files.createDirectory(saveFolder);
		long size = file.length();
		String fileID = "";
		try{
			fileID = new BigInteger(1, MessageDigest.getInstance("MD5").digest(file.getAbsolutePath().getBytes())).toString(16).substring(20);
		} catch(NoSuchAlgorithmException e){}
		if(size <= maxBlockSize){
			BufferedReader br = new BufferedReader(new FileReader(file));
			int blockSize = (int) size;
			char[] block = new char[blockSize];
			br.read(block);
			br.close();
			return new Block[]{saveToMemory ? new Block(blockSize, 0, fileID) : (fileName == null) ? new Block(blockSize, saveFolder, 0, fileID) : new Block(blockSize, saveFolder, fileName, 0, fileID)};
		}
		return splitIntoBlocks(file, (int) maxBlockSize, (int) Math.floor((double)size/maxBlockSize), (int) (size % maxBlockSize), saveToMemory, true, saveFolder, fileName, fileID);
	}
	
	private static Block[] splitIntoBlocks(File file, int blockSize, int pieces, int extra, boolean saveToMemory, boolean splitExtra, Path saveFolder, String fileName, String fileID) throws IOException{
		Block[] blocks = new Block[(splitExtra && extra != 0) ? pieces+1 : pieces];
		BufferedReader br = new BufferedReader(new FileReader(file));
		for(int b = 0; b < blocks.length; b++){
			if(b == blocks.length-1){
				if(splitExtra){
					char[] block = new char[extra];
					br.read(block);
					blocks[b] = saveToMemory ? new Block(extra, b, fileID) : (fileName == null) ? new Block(extra, saveFolder, b, fileID) : new Block(extra, saveFolder, fileName, b, fileID);
					blocks[b].setContents(block);
					br.close();
					return blocks;
				} else
					blockSize += extra;
			}
			char[] block = new char[blockSize];
			br.read(block);
			blocks[b] = saveToMemory ? new Block(blockSize, b, fileID) : (fileName == null) ? new Block(blockSize, saveFolder, b, fileID) : new Block(blockSize, saveFolder, fileName, b, fileID);
			blocks[b].setContents(block);
		}
		br.close();
		return blocks;
	}
	
	private static int getIndexOf(File frag){
		String[] parts = frag.getName().split("-");
		return Integer.parseInt(parts[parts.length-1]);
	}
}