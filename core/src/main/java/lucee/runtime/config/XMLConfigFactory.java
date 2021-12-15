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

import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.engine.InfoImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.text.xml.XMLUtil;

public abstract class XMLConfigFactory {

	public static final int NEW_NONE = 0;
	public static final int NEW_MINOR = 1;
	public static final int NEW_FRESH = 2;
	public static final int NEW_FROM4 = 3;

	public static UpdateInfo getNew(CFMLEngine engine, Resource contextDir, final boolean readOnly, UpdateInfo defaultValue) {
		try {
			return getNew(engine, contextDir, readOnly);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	public static UpdateInfo getNew(CFMLEngine engine, Resource contextDir, final boolean readOnly) throws IOException, BundleException {
		lucee.Info info = engine.getInfo();

		String strOldVersion;
		final Resource resOldVersion = contextDir.getRealResource("version");
		String strNewVersion = info.getVersion() + "-" + info.getRealeaseTime();
		// fresh install
		if (!resOldVersion.exists()) {
			if (!readOnly) {
				resOldVersion.createNewFile();
				IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false);
			}
			return UpdateInfo.NEW_FRESH;
		}
		// changed version
		else if (!(strOldVersion = IOUtil.toString(resOldVersion, SystemUtil.getCharset())).equals(strNewVersion)) {
			if (!readOnly) {
				IOUtil.write(resOldVersion, strNewVersion, SystemUtil.getCharset(), false);
			}
			Version oldVersion = OSGiUtil.toVersion(strOldVersion);

			return new UpdateInfo(oldVersion, oldVersion.getMajor() < 5 ? NEW_FROM4 : NEW_MINOR);
		}
		return UpdateInfo.NEW_NONE;
	}

	public static class UpdateInfo {

		public static final UpdateInfo NEW_NONE = new UpdateInfo(XMLConfigWebFactory.NEW_NONE);
		public static final UpdateInfo NEW_FRESH = new UpdateInfo(XMLConfigWebFactory.NEW_FRESH);

		public final Version oldVersion;
		public final int updateType;

		public UpdateInfo(int updateType) {
			this.oldVersion = null;
			this.updateType = updateType;
		}

		public UpdateInfo(Version oldVersion, int updateType) {
			this.oldVersion = oldVersion;
			this.updateType = updateType;
		}

		public String getUpdateTypeAsString() {
			if (updateType == XMLConfigWebFactory.NEW_NONE) return "new-none";
			if (updateType == XMLConfigWebFactory.NEW_FRESH) return "new-fresh";
			if (updateType == XMLConfigWebFactory.NEW_FROM4) return "new-from4";
			if (updateType == XMLConfigWebFactory.NEW_MINOR) return "new-minor";
			return "unkown:" + updateType;
		}

	}

	public static void updateRequiredExtension(CFMLEngine engine, Resource contextDir, Log log) {
		lucee.Info info = engine.getInfo();
		try {
			Resource res = contextDir.getRealResource("required-extension");
			String str = info.getVersion() + "-" + info.getRealeaseTime();
			if (!res.exists()) res.createNewFile();
			IOUtil.write(res, str, SystemUtil.getCharset(), false);

		}
		catch (Exception e) {
			if (log != null) log.error("required-extension", e);
		}
	}

	public static boolean isRequiredExtension(CFMLEngine engine, Resource contextDir, Log log) {
		lucee.Info info = engine.getInfo();
		try {
			Resource res = contextDir.getRealResource("required-extension");
			if (!res.exists()) return false;

			String writtenVersion = IOUtil.toString(res, SystemUtil.getCharset());
			String currVersion = info.getVersion() + "-" + info.getRealeaseTime();
			return writtenVersion.equals(currVersion);
		}
		catch (Exception e) {
			if (log != null) log.error("required-extension", e);
		}
		return false;
	}

	/**
	 * load XML Document from XML File
	 * 
	 * @param xmlFile XML File to read
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
			IOUtil.close(is);
		}
	}

	static Document loadDocumentCreateIfFails(Resource configFile, String type) throws SAXException, IOException {
		try {
			InputStream is = null;
			try {
				return _loadDocument(is = IOUtil.toBufferedInputStream(configFile.getInputStream()));
			}
			finally {
				IOUtil.close(is);
			}
		}
		catch (Exception e) {
			// rename buggy config files
			if (configFile.exists()) {
				LogUtil.log(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigFactory.class.getName(),
						"Config file [" + configFile + "] was not valid and has been replaced");
				LogUtil.log(ThreadLocalPageContext.getConfig(), XMLConfigFactory.class.getName(), e);
				int count = 1;
				Resource bugFile;
				Resource configDir = configFile.getParentResource();
				while ((bugFile = configDir.getRealResource("lucee-" + type + "." + (count++) + ".buggy")).exists()) {
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
		createFileFromResource("/resource/config/" + xmlName + ".xml", configFile.getAbsoluteResource());
	}

	/**
	 * load XML Document from XML File
	 * 
	 * @param is InoutStream to read
	 * @return returns the Document
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document _loadDocument(InputStream is) throws SAXException, IOException {
		InputSource source = new InputSource(is);

		return XMLUtil.parse(source, null, false);
	}

	/**
	 * return first direct child Elements of an Element with given Name
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
		if (parent == null) return null;
		NodeList list = parent.getChildNodes();
		int len = list.getLength();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				return (Element) node;
			}
		}
		if (doNotCreate) return null;

		Element newEl = XMLUtil.getDocument(parent).createElement(nodeName);
		if (insertBefore) parent.insertBefore(newEl, parent.getFirstChild());
		else parent.appendChild(newEl);

		return newEl;
	}

	/**
	 * return all direct child Elements of an Element with given Name
	 * 
	 * @param parent
	 * @param nodeName
	 * @return matching children
	 */
	static Element[] getChildren(Node parent, String nodeName) {
		if (parent == null) return new Element[0];
		NodeList list = parent.getChildNodes();
		int len = list.getLength();
		ArrayList<Element> rtn = new ArrayList<Element>();

		for (int i = 0; i < len; i++) {
			Node node = list.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equalsIgnoreCase(nodeName)) {
				rtn.add((Element) node);
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
		LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigFactory.class.getName(), "Write file: [" + file + "]");
		if (file.exists()) file.delete();

		InputStream is = InfoImpl.class.getResourceAsStream(resource);
		if (is == null) throw new IOException("File [" + resource + "] does not exist.");
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
		catch (Exception e) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), XMLConfigFactory.class.getName(), e);
		}
	}

	static void create(String srcPath, String[] names, Resource dir, boolean doNew) {
		for (int i = 0; i < names.length; i++) {
			create(srcPath, names[i], dir, doNew);
		}
	}

	static Resource create(String srcPath, String name, Resource dir, boolean doNew) {
		if (!dir.exists()) dir.mkdirs();

		Resource f = dir.getRealResource(name);
		if (!f.exists() || doNew) XMLConfigFactory.createFileFromResourceEL(srcPath + name, f);
		return f;

	}

	static void delete(Resource dbDir, String[] names) {
		for (int i = 0; i < names.length; i++) {
			delete(dbDir, names[i]);
		}
	}

	static void delete(Resource dbDir, String name) {
		Resource f = dbDir.getRealResource(name);
		if (f.exists()) {
			LogUtil.logGlobal(ThreadLocalPageContext.getConfig(), Log.LEVEL_INFO, XMLConfigFactory.class.getName(), "Delete file: [" + f + "]");

			f.delete();
		}

	}

}
