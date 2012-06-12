package com.redhat.contentspec.client.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.redhat.ecs.commonutils.ExceptionUtilities;

public class ZipUtilities extends com.redhat.ecs.commonutils.ZipUtilities {

	/**
	 * Unzips a Zip File in byte array format to a specified directory. 
	 * 
	 * @param zipBytes The zip file.
	 * @param directory The directory to unzip the file to.
	 * @return true if the file was successfully unzipped otherwise false.
	 */
	public static boolean unzipFileIntoDirectory(File zipFile, String directory) {
		Map<ZipEntry, byte[]> zipEntries = new HashMap<ZipEntry, byte[]>();
		ZipInputStream zis;
		try {
			zis = new ZipInputStream(new FileInputStream(zipFile));
		} catch (FileNotFoundException ex) {
			ExceptionUtilities.handleException(ex);
			return false;
		}
		ZipEntry entry = null;
		byte[] buffer = new byte[1024];
		try {
			int read;
			while ((entry = zis.getNextEntry()) != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((read = zis.read(buffer)) > 0) {
			        bos.write(buffer, 0, read);
			    }
				zipEntries.put(entry, bos.toByteArray());
			}
		} catch (IOException ex) {
			ExceptionUtilities.handleException(ex);
			return false;
		}
		return unzipFileIntoDirectory(zipEntries, directory);
	}
	
	/**
	 * 
     * @param zipFile
     * @param directory
     */
	private static boolean unzipFileIntoDirectory(Map<ZipEntry, byte[]> zipEntries, String directory) {
		// Check the directory specified is a directory
		File dir = new File(directory);
		if (!dir.isDirectory()) return false;
		
		File file = null;
		FileOutputStream fos = null;
    
		for (ZipEntry entry: zipEntries.keySet()) {
			try {
  
				file = new File(dir.getAbsolutePath() + File.separator + stripZipFileName(entry.getName()));
        
				// If the ZipEntry is a directory then create it
				if (entry.isDirectory()) {
					file.mkdirs();
					continue;
				// Else the ZipEntry is a file then make sure its directory exists
				// and then create the file
				} else {
					file.getParentFile().mkdirs();
					file.createNewFile();
				}
        
				// Write the contents of the zip entry into the File
				fos = new FileOutputStream(file);
				fos.write(zipEntries.get(entry));
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	private static String stripZipFileName(String name) {
		return name.replaceFirst("^[^/]*/", "");
	}

}
