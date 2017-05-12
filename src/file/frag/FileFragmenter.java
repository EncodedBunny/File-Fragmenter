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
	 * @param file
	 * @param pieces
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
	 * @param file The <code>File</code> to be fragmented
	 * @param pieces The number of pieces to fragment
	 * @param saveFolder The folder that will contain all the fragmented pieces
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
	 * @param file The <code>File</code> to be fragmented
	 * @param pieces The number of pieces to fragment
	 * @param saveFolder The folder that will contain all the fragmented pieces
	 * @param fileName The base file name for the fragmented pieces (the individual block ID of each piece will be appended to this file name)
	 * @return A <code>Block</code> array containing the fragmented versions of the original file
	 * @throws ImpossibleFileFragmentationException If the specified number of pieces is smaller than the actual file size
	 * @throws IOException If the file to be fragmented is not a file, cannot be open or does not exist or the fragmented files cannot be written to their destinations
	 * @author EncodedBunny
	 */
	public static Block[] fragmentFile(File file, int pieces, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		return fragmentFile(file, pieces, false, saveFolder, fileName);
	}
	
	private static Block[] fragmentFile(File file, int pieces, boolean saveToMemory, Path saveFolder, String fileName) throws ImpossibleFileFragmentationException, IOException{
		if(!file.exists() || !file.canRead() || !file.isFile())
			throw new IOException();
		if(file.length() < pieces)
			throw new ImpossibleFileFragmentationException("Number of pieces larger than file size");
		if(saveFolder != null)
			Files.createDirectory(saveFolder);
		long size = file.length();
		int blockSize = (int) Math.floor((double)size/pieces);
		int extra = (int) (size % pieces);
		Block[] blocks = new Block[pieces];
		for(int b = 0; b < pieces; b++){
			if(b == pieces-1)
				blockSize += extra;
			BufferedReader br = new BufferedReader(new FileReader(file));
			char[] block = new char[blockSize];
			br.read(block);
			blocks[b] = saveToMemory ? new Block(blockSize) : (fileName == null) ? new Block(blockSize, saveFolder) : new Block(blockSize, saveFolder, fileName);
			br.close();
		}
		return blocks;
	}
}