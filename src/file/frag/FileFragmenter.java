package file.frag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
	 * Fragments a file in a specified number of pieces and saves the fragmented pieces to the HD with the default file name: <pre>"frag_" + Block.getID()</pre>
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
	
	public static Block[] fragmentFileDynamically(File file, int pieces) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, pieces, true, null, null);
	}
	
	public static Block[] fragmentFileDynamically(File file, int pieces, Path saveFolder) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, pieces, false, saveFolder, null);
	}
	
	public static Block[] fragmentFileDynamically(File file, int pieces, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFileDynamically(file, pieces, false, saveFolder, fileName);
	}
	
	private static Block[] fragmentFile(File file, int pieces, boolean saveToMemory, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		if(!file.exists() || !file.canRead() || !file.isFile())
			throw new IOException();
		long size = file.length();
		if(size < pieces)
			throw new ImpossibleFileFragmentationException("Number of pieces larger than file size");
		if(saveFolder != null && !Files.exists(saveFolder))
			Files.createDirectory(saveFolder);
		return splitIntoBlocks(file, (int) Math.floor((double)size/pieces), pieces, (int) (size % pieces), saveToMemory, false, saveFolder, fileName);
	}
	
	private static Block[] fragmentFileDynamically(File file, long maxBlockSize, boolean saveToMemory, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		if(!file.exists() || !file.canRead() || !file.isFile())
			throw new IOException();
		if(maxBlockSize <= 0)
			throw new ImpossibleFileFragmentationException("Invalid max block size '" + maxBlockSize + "'");
		if(saveFolder != null && !Files.exists(saveFolder))
			Files.createDirectory(saveFolder);
		long size = file.length();
		if(size <= maxBlockSize){
			BufferedReader br = new BufferedReader(new FileReader(file));
			int blockSize = (int) size;
			char[] block = new char[blockSize];
			br.read(block);
			br.close();
			return new Block[]{saveToMemory ? new Block(blockSize, 0) : (fileName == null) ? new Block(blockSize, saveFolder, 0) : new Block(blockSize, saveFolder, fileName, 0)};
		}
		return splitIntoBlocks(file, (int) maxBlockSize, (int) Math.floor((double)size/maxBlockSize), (int) (size % maxBlockSize), saveToMemory, true, saveFolder, fileName);
	}
	
	private static Block[] splitIntoBlocks(File file, int blockSize, int pieces, int extra, boolean saveToMemory, boolean splitExtra, Path saveFolder, String fileName) throws IOException{
		Block[] blocks = new Block[(splitExtra && extra != 0) ? pieces+1 : pieces]; // TODO INCREASE SIZE BY ONE BECAUSA OF EXTRA BLOCK
		BufferedReader br = new BufferedReader(new FileReader(file));
		for(int b = 0; b < blocks.length; b++){
			if(b == blocks.length-1){
				if(splitExtra){
					char[] block = new char[extra];
					br.read(block);
					blocks[b] = saveToMemory ? new Block(extra, b) : (fileName == null) ? new Block(extra, saveFolder, b) : new Block(extra, saveFolder, fileName, b);
					blocks[b].setContents(block);
				} else
					blockSize += extra;
			}
			char[] block = new char[blockSize];
			br.read(block);
			blocks[b] = saveToMemory ? new Block(blockSize, b) : (fileName == null) ? new Block(blockSize, saveFolder, b) : new Block(blockSize, saveFolder, fileName, b);
			blocks[b].setContents(block);
		}
		br.close();
		return blocks;
	}
}