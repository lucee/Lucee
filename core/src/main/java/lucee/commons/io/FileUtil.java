/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.commons.io;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Helper methods for file objects
 */
public final class FileUtil {
    /**
     * Field <code>FILE_SEPERATOR</code>
     */
    public static final char FILE_SEPERATOR=File.separatorChar; 
    /**
     * Field <code>FILE_ANTI_SEPERATOR</code>
     */
    public static final char FILE_ANTI_SEPERATOR=(FILE_SEPERATOR=='/')?'\\':'/';
    
    /**
     * Field <code>TYPE_DIR</code>
     */
    public static final short TYPE_DIR=0;
    
    /**
     * Field <code>TYPE_FILE</code>
     */
    public static final short TYPE_FILE=1;

    /**
     * Field <code>LEVEL_FILE</code>
     */
    public static final short LEVEL_FILE=0;
    /**
     * Field <code>LEVEL_PARENT_FILE</code>
     */
    public static final short LEVEL_PARENT_FILE=1;
    /**
     * Field <code>LEVEL_GRAND_PARENT_FILE</code>
     */
    public static final short LEVEL_GRAND_PARENT_FILE=2;
    
    /** 
     * create a file from path
     * @param path
     * @return new File Object
     */
    public static File toFile(String path) { 
        return new File(path.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR)); 
    } 

    /**
     * create a File from parent file and string
     * @param parent 
     * @param path 
     * @return new File Object
     */
    public static File toFile(File parent, String path) {
        return new File(parent,path.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR));
    }

    /**
     * create a File from parent file and string
     * @param parent 
     * @param path 
     * @return new File Object
     */
    public static File toFile(String parent, String path) {
        return new File(
        		parent.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR),
        		path.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR));
    }
    
    /* *
     * create a file object from a file object (parent) and realpath, in difference to the same constructor
     * of the File Object this method ignore the diffrent path seperators on the different plattforms
     * @param parent
     * @param realpath
     * @return new FIle Object matching on arguments
     * /
    public static File toFile2(File parent, String realpath) {
        realpath=realpath.replace(FILE_ANTI_SEPERATOR,FILE_SEPERATOR);
        while(realpath.startsWith("../")) {
            parent=parent.getParentFile();
            realpath=realpath.substring(3);
        }
        if(realpath.startsWith("./")) realpath=realpath.substring(2);
            
        return FileUtil.toFile(parent,realpath);
    }*/

    
    

    
    
	/**
	 * translate a URL to a File Object
	 * @param url
	 * @return matching file object
	 * @throws MalformedURLException
	 */
	public static final File URLToFile(URL url) throws MalformedURLException {
	    if (!"file".equals(url.getProtocol()))
	        throw new MalformedURLException("URL protocol must be 'file'.");
	    return new File(URIToFilename(url.getFile()));
	}
	
	  /**
	   * Fixes a platform dependent filename to standard URI form.
	   * @param str The string to fix.
	   * @return Returns the fixed URI string.
	   */
	  public static final String URIToFilename(String str) {
	    // Windows fix
	    if (str.length() >= 3) {
	      if (str.charAt(0) == '/' && str.charAt(2) == ':') {
		char ch1 = Character.toUpperCase(str.charAt(1));
		if (ch1 >= 'A' && ch1 <= 'Z') str = str.substring(1);
	      }
	    }
	    // handle platform dependent strings
	    str = str.replace('/', java.io.File.separatorChar);
	    return str;
	  }




}