/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.SystemOut;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.text.xml.XMLUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class XMLConfigFactory {
	static boolean doNew(Resource contextDir) {
		lucee.Info info = CFMLEngineFactory.getInstance().getInfo();
		final boolean readonly = false;
		try {
			
			Resource version = contextDir.getRealResource("version");
			String v = info.getVersion() + "-" + info.getRealeaseTime();
			if (!version.exists()) {
				if (!readonly) {
					version.createNewFile();
					IOUtil.write(version, v, SystemUtil.getCharset(), false);
				}
				return true;
			}
			else if (!IOUtil.toString(version, SystemUtil.getCharset()).equals(v)) {
				if (!readonly)
					IOUtil.write(version, v, SystemUtil.getCharset(), false);

				return true;
			}
		}
		catch (Throwable t) {
		}
		return false;
	}

	/**
	 * load XML Document from XML File
	 * 
	 * @param xmlFile
	 *            XML File to read
	 * @return returns the Document
	 * @throws SAXException
	 * @throws IOException
	 */
	static Document loadDocument(Resource xmlFile) throws SAXException, IOException {
		InputStream is = null;
		try {
			return _loadDocument(is = IOUtil.toBufferedInputStream(xmlFile.getInputStream()));
		}
		finally {
			IOUtil.closeEL(is);
		}
	}
	
	static Document loadDocumentCreateIfFails(Resource configFile, String type) throws SAXException, IOException {
		try {
			InputStream is = null;
			try {
				return _loadDocument(is = IOUtil.toBufferedInputStream(configFile.getInputStream()));
			}
			finally {
				IOUtil.closeEL(is);
			}
		}
		catch (Exception e) {
			// rename buggy config files
			if (configFile.exists()) {
				SystemOut.printDate(SystemUtil.getPrintWriter(SystemUtil.OUT), "config file " + configFile + " was not valid and has been replaced");
				int count = 1;
				Resource bugFile;
				Resource configDir = configFile.getParentResource();
				while ((bugFile = configDir.getRealResource("lucee-"+type+"." + (count++) + ".buggy")).exists()) {
				}
				IOUtil.copy(configFile, bugFile);
				configFile.delete();
			}
			createConfigFile(type, configFile);
			return loadDocument(configFile);
		}
		
	}


	/**
	 * creates the Config File, if File not exist
	 * 
	 * @param xmlName
	 * @param configFile
	 * @throws IOException
	 */
	static void createConfigFile(String xmlName, Resource configFile) throws IOException {
		configFile.createFile(true);
		createFileFromResource("/resource/config/" + xmlName + ".xml", configFile.getAbsoluteResource());
	}

	/**
	 * load XML Document from XML File
	 * 
	 * @param is
	 *            InoutStream to read
	 * @return returns the Document
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document _loadDocument(InputStream is) throws SAXException, IOException {
		InputSource source = new InputSource(is);
		
		return XMLUtil.parse(source, null, false);
	}
	

	/**
	 * return first direct child Elements of a Element with given Name
	 * 
	 * @param parent
	 * @param nodeName
	 * @return matching children
	 */
	static Element getChildByName(Node parent, String nodeName) {
		return getChildByName(parent, nodeName, false);
	}

	static Element getChildByName(Node parent, String nodeName, boolean insertBefore) {
		return getChildByName(parent, nodeName, insertBefore, false);
	}

	static Element getChildByName(Node parent, String nodeName, boolean insertBefore, boolean doNotCreate) {
		if (parent == null)
			return null;
		NodeList list = parent.getChildNodes();
		int len = list.getLength();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				return (Element) node;
			}
		}
		if (doNotCreate)
			return null;

		Element newEl = parent.getOwnerDocument().createElement(nodeName);
		if (insertBefore)
			parent.insertBefore(newEl, parent.getFirstChild());
		else
			parent.appendChild(newEl);

		return newEl;
	}

	/**
	 * return all direct child Elements of a Element with given Name
	 * 
	 * @param parent
	 * @param nodeName
	 * @return matching children
	 */
	static Element[] getChildren(Node parent, String nodeName) {
		if (parent == null)
			return new Element[0];
		NodeList list = parent.getChildNodes();
		int len = list.getLength();
		ArrayList<Element> rtn = new ArrayList<Element>();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				rtn.add((Element)node);
			}
		}
		return rtn.toArray(new Element[rtn.size()]);
	}

	/**
	 * creates a File and his content froma a resurce
	 * 
	 * @param resource
	 * @param file
	 * @param password
	 * @throws IOException
	 */
	static void createFileFromResource(String resource, Resource file, String password) throws IOException {
		SystemOut.printDate(SystemUtil.getPrintWriter(SystemUtil.OUT), "write file:" + file);
		file.delete();
		
		InputStream is = InfoImpl.class.getResourceAsStream(resource);
		if(is==null) throw new IOException("file ["+resource+"] does not exist.");
		file.createNewFile();
		IOUtil.copy(is, file, true);
	}
	

	/**
	 * creates a File and his content froma a resurce
	 * 
	 * @param resource
	 * @param file
	 * @throws IOException
	 */
	static void createFileFromResource(String resource, Resource file) throws IOException {
		createFileFromResource(resource, file, null);
	}

	public static void createFileFromResourceEL(String resource, Resource file) {
		try {
			createFileFromResource(resource, file, null);
		}
		catch (Throwable e) {
			SystemOut.printDate(ExceptionUtil.getStacktrace(e, true), SystemUtil.ERR);
		}
	}

	static void create(String srcPath, String[] names, Resource dir, boolean doNew) {
		for(int i=0;i<names.length;i++){
			create(srcPath, names[i], dir, doNew);
		}
	}
		
	static Resource create(String srcPath, String name, Resource dir, boolean doNew) {
		if(!dir.exists())dir.mkdirs();
		
		Resource f = dir.getRealResource(name);
		if (!f.exists() || doNew)
			XMLConfigFactory.createFileFromResourceEL(srcPath+name, f);
		return f;
		
	}

	static void delete(Resource dbDir, String[] names) {
		for(int i=0;i<names.length;i++){
			delete(dbDir, names[i]);
		}
	}

	static void delete(Resource dbDir, String name) {
		Resource f = dbDir.getRealResource(name);
		if (f.exists()) {
			SystemOut.printDate(SystemUtil.getPrintWriter(SystemUtil.OUT), "delete file:" + f);
			
			f.delete();
		}
		
	}
	
}