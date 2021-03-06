/*******************************************************************************
 * Copyright (C) 2018 OTK Software
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * The license allows developers and companies to use and integrate a software 
 * component released under the LGPL into their own (even proprietary) software 
 * without being required by the terms of a strong copyleft license to release the 
 * source code of their own components. However, any developer who modifies 
 * an LGPL-covered component is required to make their modified version 
 * available under the same LGPL license. For proprietary software, code under 
 * the LGPL is usually used in the form of a shared library, so that there is a clear 
 * separation between the proprietary and LGPL components.
 * 
 * The GNU Lesser General Public License allows you also to freely redistribute the 
 * libraries under the same license, if you provide the terms of the GNU Lesser 
 * General Public License with them and add the following copyright notice at the 
 * appropriate place (with a link to http://javacollection.net/reflectionui/ web site 
 * when possible).
 ******************************************************************************/
package xy.reflect.ui.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

	public static File createTempDirectory() throws Exception {
		File baseDir = new File(System.getProperty("java.io.tmpdir"));
		String baseName = System.currentTimeMillis() + "-";
		int TEMP_DIR_ATTEMPTS = 10000;
		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, baseName + counter);
			if (tempDir.mkdir()) {
				return tempDir;
			}
		}
		throw new Exception("Failed to create directory within " + TEMP_DIR_ATTEMPTS + " attempts (tried " + baseName
				+ "0 to " + baseName + (TEMP_DIR_ATTEMPTS - 1) + ')');
	}

	public static String read(File file) throws Exception {
		return new String(readBinary(file));
	}

	public static String read(InputStream in) throws Exception {
		return new String(readBinary(in));
	}

	public static byte[] readBinary(InputStream in) throws Exception {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];
			while ((nRead = in.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			throw new Exception("Error while reading input stream: " + e.getMessage(), e);
		}
	}

	public static byte[] readBinary(File file) throws Exception {
		FileInputStream in = null;
		try {
			in = new FileInputStream(file);
			return readBinary(in);
		} catch (IOException e) {
			throw new Exception("Unable to read file : '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public static void write(File file, String text, boolean append) throws Exception {
		writeBinary(file, text.getBytes(), append);
	}

	public static void writeBinary(File file, byte[] bytes, boolean append) throws Exception {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file, append);
			out.write(bytes);
			out.flush();
			out.close();
		} catch (IOException e) {
			throw new Exception("Unable to write file : '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}

	}

	public static void historicizeFile(String filePath, int historySize) throws Exception {
		for (int i = (historySize - 1); i >= 0; i--) {
			File file;
			if (i == 0) {
				file = new File(filePath);
			} else {
				file = new File(getRotatedFilePath(filePath, i));
			}
			if (file.exists()) {
				if (i == (historySize - 1)) {
					delete(file);
				} else {
					File nextRotatedFile = new File(getRotatedFilePath(filePath, i + 1));
					try {
						rename(file, nextRotatedFile.getName());
					} catch (Exception e) {
						copy(file, nextRotatedFile);
						delete(file);
					}
				}
			}
		}
	}

	public static String getRotatedFilePath(String filePath, int i) {
		String fileNameExtension = getFileNameExtension(filePath);
		if ((fileNameExtension != null) && (fileNameExtension.length() > 0)) {
			String fileNameWithoutExtension = removeFileNameExtension(filePath);
			return fileNameWithoutExtension + "-" + i + "." + fileNameExtension;
		} else {
			return filePath + "-" + i;
		}
	}

	public static String removeFileNameExtension(String fileName) {
		String extension = getFileNameExtension(fileName);
		if (extension.length() > 0) {
			return fileName.substring(0, fileName.length() - ("." + extension).length());
		} else {
			return fileName;
		}
	}

	public static String getFileNameExtension(String fileName) {
		int lastDotIndex = fileName.lastIndexOf(".");
		if (lastDotIndex == -1) {
			return "";
		} else if (lastDotIndex == 0) {
			return "";
		} else {
			return fileName.substring(lastDotIndex + 1);
		}
	}

	public static void copy(File src, File dst) throws Exception {
		copy(src, dst, true);
	}

	public static void copy(File src, File dst, boolean recusrsively) throws Exception {
		copy(src, dst, recusrsively, null, null);
	}

	public static void copy(File src, File dst, boolean recusrsively, FilenameFilter filenameFilter,
			Listener<Pair<File, Exception>> errorHandler) throws Exception {
		try {
			if (src.isDirectory()) {
				try {
					createDirectory(dst);
				} catch (Exception e) {
					if (errorHandler != null) {
						errorHandler.handle(new Pair<File, Exception>(src, e));
					} else {
						throw e;
					}
				}
				if (recusrsively) {
					for (File srcChild : src.listFiles(filenameFilter)) {
						copy(srcChild, new File(dst, srcChild.getName()), recusrsively, filenameFilter, errorHandler);
					}
				}
			} else if (src.isFile()) {
				try {
					writeBinary(dst, readBinary(src), false);
				} catch (Exception e) {
					if (errorHandler != null) {
						errorHandler.handle(new Pair<File, Exception>(src, e));
					} else {
						throw e;
					}
				}
			} else {
				throw new Exception("File not found: '" + src + "'", null);
			}
		} catch (Exception e) {
			throw new Exception("Unable to copy resource: '" + src.getAbsolutePath() + "' > '" + dst.getAbsolutePath()
					+ "': " + e.getMessage(), e);
		}
	}

	public static void createDirectory(File dir) throws Exception {
		if (dir.isDirectory()) {
			return;
		}
		final boolean success;
		try {
			success = dir.mkdir();
		} catch (Exception e) {
			throw new Exception("Failed to create directory: '" + dir.getAbsolutePath() + "': " + e.getMessage(), e);
		}
		if (!success) {
			throw new Exception("Unable to create directory: '" + dir.getAbsolutePath() + "'", null);
		}
	}

	public static String getRelativePath(File child, File ancestor) {
		File relativeFile = relativizeFile(ancestor, child);
		if (relativeFile == null) {
			return null;
		}
		return relativeFile.getPath();
	}

	public static boolean canonicallyEquals(File file1, File file2) {
		try {
			return file1.getCanonicalFile().equals(file2.getCanonicalFile());
		} catch (IOException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}

	public static File getCanonicalParent(File file) {
		try {
			return file.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void delete(File file) throws Exception {
		delete(file, null, null);
	}

	public static void delete(File file, FilenameFilter filter, Listener<Pair<File, Exception>> errorHandler)
			throws Exception {
		if (file.isDirectory()) {
			for (File childFile : file.listFiles(filter)) {
				delete(childFile, filter, errorHandler);
			}
			if (file.listFiles().length > 0) {
				return;
			}
		}
		boolean success;
		try {
			success = file.delete();
			if (!success) {
				throw new Exception("System error");
			}
		} catch (Exception e) {
			e = new Exception("Failed to delete resource: '" + file.getAbsolutePath() + "': " + e.getMessage(), e);
			if (errorHandler != null) {
				errorHandler.handle(new Pair<File, Exception>(file, e));
			} else {
				throw e;
			}
		}
	}

	public static void rename(File file, String destFileName) throws Exception {
		try {
			if (new File(destFileName).getParent() != null) {
				throw new Exception("Destination file name is not is not a local name: '" + destFileName + "'");
			}
			File destFile = new File(file.getParent(), destFileName);
			boolean success = file.renameTo(destFile);
			if (!success) {
				throw new Exception("System error");
			}
		} catch (Exception e) {
			throw new Exception("Failed to rename resource: '" + file.getAbsolutePath() + "' to '" + destFileName
					+ "': " + e.getMessage(), e);
		}
	}

	public static boolean hasFileNameExtension(String fileName, String[] extensions) {
		for (String ext : extensions) {
			if (ext.toLowerCase().equals(getFileNameExtension(fileName).toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAncestor(File ancestor, File file) {
		File mayBeAncestor = getCanonicalParent(file);
		while (true) {
			if (mayBeAncestor == null) {
				return false;
			}
			if (canonicallyEquals(mayBeAncestor, ancestor)) {
				return true;
			}
			mayBeAncestor = getCanonicalParent(mayBeAncestor);
		}
	}

	public static void createFile(File file) throws Exception {
		try {
			if (!file.isFile()) {
				if (!file.createNewFile()) {
					throw new Exception("System error");
				}
			}
		} catch (IOException e) {
			throw new Exception("Failed to create the file '" + file + "': " + e.toString(), e);
		}

	}

	public static File createTemporaryFile() throws Exception {
		try {
			File tmpFile = File.createTempFile("tmp", null);
			tmpFile.deleteOnExit();
			return tmpFile;
		} catch (IOException e) {
			throw new Exception("Failed to create temporary file: " + e.getMessage(), e);
		}
	}

	public static File relativizeFile(File ancestor, File file) {
		try {
			ancestor = ancestor.getCanonicalFile();
			file = file.getCanonicalFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (!FileUtils.isAncestor(ancestor, file)) {
			return null;
		}
		String relativePath = file.getPath().substring(ancestor.getPath().length(), file.getPath().length());
		if (relativePath.startsWith("/") || relativePath.startsWith("\\")) {
			relativePath = relativePath.substring(1);
		}
		return new File(relativePath);
	}

	public static File getStreamAsFile(InputStream in) throws IOException {
		File tempFile = File.createTempFile(String.valueOf(in.hashCode()), ".tmp");
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		try {
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
		} finally {
			out.close();
		}
		return tempFile;
	}


}
