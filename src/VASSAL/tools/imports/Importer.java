/*
 * Copyright (c) 2007 by Michael Kiefte
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License (LGPL) as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this library; if not, copies are available
 * at http://www.opensource.org.
 */

package VASSAL.tools.imports;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import VASSAL.build.GameModule;
import VASSAL.tools.ArchiveWriter;
import VASSAL.tools.DataArchive;

/**
 * Abstract parent of all importer classes.
 * 
 * @author Michael Kiefte
 *
 */
public abstract class Importer {
	
	/*
	 * We need this for the getCaseInsensitiveFile method.
	 */
	protected ImportAction action;
	
	/**
	 * The method that actually loads the file and creates the classes containing information needed for the archive. 
	 * The archive is written in <code>writeToArchive</code>.
	 * 
	 * @param f            The base file to be imported.
	 * @throws IOException
	 */
	protected abstract void load(File f) throws IOException;
	
	/**
	 * Create the VASSAL module based on the classes created by <code>load</code>. This should not be called directly
	 * but rather <code>importFile</code>.
	 * 
	 * @throws IOException
	 */
	public abstract void writeToArchive() throws IOException;
	
	/**
	 * Two methods are needed to import a file. <code>Importer.importFile</code> initializes the game module
	 * and calls <code>load</code> which must be overridden by descendents.
	 * 
	 * @param action       <code>ImportAction</code> which creates the <code>Importer</code>. This is needed for
	 *                     file dialogs that may be called by <code>Importer</code> methods.
	 * @param f            The base file to be imported.
	 * @throws IOException
	 */
	public void importFile(ImportAction action, File f) throws IOException {
		this.action = action;
		load(f);		
	}

	/**
	 * Return a file name without the extension.
	 */
	public static String stripExtension(String s) {
		if (s.equals(".") || s.equals(".."))
			return s;
		final int index = s.lastIndexOf('.');
		final int pathIdx = s.lastIndexOf(File.separatorChar);
		if (index == -1 || index < pathIdx)
			return s;
		else
			return s.substring(0, index);
	}

	/**
	 * Read a null-terminated string representing a Windows file name and convert
	 * Windows separator characters <tt>'\\'</tt> to the local separator character.
	 * This is the default file name format for many imported modules and should be used
	 * whenever a filename is read as a null-terminated string in order to ensure
	 * platform independence.
	 */
	public static String readWindowsFileName(InputStream in) throws IOException {
		final StringBuilder sb = new StringBuilder();
		char ch;
		do {
			ch = (char) in.read();
			if (ch == '\\')
				sb.append(File.separatorChar);
			else if (ch != 0)
				sb.append(ch);
		} while (ch != 0);
		return sb.toString();
	}

	/**
	 * Read a null-terminated string from a file up to a maximum length which includes
	 * the null termination. If the actual string is longer, no more bytes will be read.
	 */
	public static String readNullTerminatedString(InputStream in, int maxLen) throws IOException {
		final StringBuilder sb;
		if (maxLen == 0)
			sb = new StringBuilder();
		else
			sb = new StringBuilder(maxLen);
		char ch;
		for (int i = 0; maxLen == 0 || i < maxLen; ++i) {
			ch = (char) in.read();
			if (ch != 0)
				sb.append(ch);
			else
				break;
		}
		return sb.toString();
	}

	/**
	 * Return a null-terminated string from an input stream.
	 */
	public static String readNullTerminatedString(InputStream in) throws IOException {
		return Importer.readNullTerminatedString(in, 0);
	}

	/**
	 * Get a unique file name for an image in the game module archive. This function
	 * tests the provided name against those that are already present in the archive
	 * and if the file name already exists, creates an alternate, unique file name.
	 * If an alternate is created, it is of the form <code><tt>name</tt> + "(<tt>n</tt>)"</code>
	 * where <tt>n</tt> is an integer.
	 */
	public static String getUniqueImageFileName(String s) {
		String t = s;
		int index = 0;
		final ArchiveWriter writer = GameModule.getGameModule().getArchiveWriter();
		while (writer.isImageAdded(DataArchive.IMAGE_DIR + t))
			t = s + '(' + (++index) + ')';
		return t;
	}

	/**
	 * Get the file name without the qualifying path.
	 */
	public static String getFileName(String s) {
		if (s.equals(".") || s.equals(".."))
			return s;
		final int pathIdx = s.lastIndexOf(File.separatorChar);
		return s.substring(pathIdx + 1, s.length());
	}

	/**
	 * Get the extension from a file name.
	 */
	public static String getExtension(String s) {
		if (s.equals(".") || s.equals(".."))
			return "";
		final int extIdx = s.lastIndexOf('.');
		final int pathIdx = s.lastIndexOf(File.separatorChar);
		if (extIdx == -1 || extIdx < pathIdx || extIdx >= s.length() - 1)
			return "";
		else
			return s.substring(extIdx + 1);
	}

	/**
	 * Strip the extension from a filename and replace with the given extension.
	 */
	public static String forceExtension(String s, String ext) {
		if (s.equals(".") || s.equals(".."))
			return s;
		return Importer.stripExtension(s) + '.' + ext;
	}		
}
