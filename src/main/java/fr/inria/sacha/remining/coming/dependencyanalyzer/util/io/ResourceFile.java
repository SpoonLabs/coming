package fr.inria.sacha.remining.coming.dependencyanalyzer.util.io;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import spoon.compiler.SpoonFile;
import spoon.compiler.SpoonFolder;
import spoon.support.compiler.VirtualFolder;

/**
 * Is a resource file for a Spoon compiler
 * 
 * @author Romain Philippon
 *
 */
public class ResourceFile implements SpoonFile {
	/**
	 * Is a java.io.File instance representing an instance of this class
	 */
	private File file;
	/**
	 * Is the file name
	 */
	private String fileName;
	/**
	 * Containing the file content
	 */
	private String content;
	
	/**
	 * Build a new ResourceFile object
	 * @param fileName is the filename
	 * @param content is the file content
	 * @throws IOException is raised if the file can't be saved on hard disk
	 */
	public ResourceFile(String fileName, String content) throws IOException {
		this.fileName = fileName;
		this.content = content;
		
		this.file = new File(this.fileName);
		 
		if (!this.file.exists()) {
			this.file.createNewFile();
		}
		
		try {
			this.save();
		}
		catch(IOException ioe) {
			throw ioe;
		}
	}
	
	/**
	 * Saves the file
	 * @throws IOException is raised if the file can't be saved on hard disk 
	 */
	private void save() throws IOException {
		try {
			FileWriter fw = new FileWriter(this.file.getAbsoluteFile());
			
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		} catch (IOException e) {
			throw new IOException("Impossible to save "+ this.fileName);
		}
	}
	
	/**
	 * Deletes the file
	 */
	public void erase() {
		if(this.file.delete())
			System.out.println(this.fileName +" deleted");
		else
			System.out.println(this.fileName +" not deleted");
	}

	@Override
	public File getFileSystemParent() {
		return null;
	}

	@Override
	public String getName() {
		return this.fileName;
	}

	@Override
	public SpoonFolder getParent() {
		return new VirtualFolder();
	}

	@Override
	public String getPath() {
		return this.fileName;
	}

	@Override
	public boolean isArchive() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public File toFile() {
		return this.file.getAbsoluteFile();
	}

	@Override
	public InputStream getContent() {
		try {
			return new ByteArrayInputStream(this.content.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	@Override
	public boolean isActualFile() {
		return false;
	}

	@Override
	public boolean isJava() {
		return true;
	}
}
