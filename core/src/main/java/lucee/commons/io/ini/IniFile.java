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
package lucee.commons.io.ini;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

/**
 * read an ini file and allow to modifie and read the data
 */
public final class IniFile {

	private Map sections;
	private final Resource file;

	private static Map newMap() {
		return new LinkedHashMap();
	}

	/**
	 * Constructor for the IniFile object
	 *
	 * @param file ini FIle
	 * @throws IOException
	 */
	public IniFile(Resource file) throws IOException {
		this.file = file;
		sections = newMap();
		InputStream is = null;
		if (!file.exists()) file.createFile(false);
		try {
			load(is = file.getInputStream());
		}
		finally {
			IOUtil.close(is);
		}
	}

	public IniFile(InputStream is) throws IOException {
		sections = newMap();
		load(is);
		file = null;
	}

	/**
	 * Sets the KeyValue attribute of the IniFile object
	 *
	 * @param strSection the section to set
	 * @param key the key of the new value
	 * @param value the value to set
	 */
	public void setKeyValue(String strSection, String key, String value) {
		Map section = getSectionEL(strSection);
		if (section == null) {
			section = newMap();
			sections.put(strSection.toLowerCase(), section);
		}
		section.put(key.toLowerCase(), value);
	}

	/**
	 * Gets the Sections attribute of the IniFile object
	 *
	 * @return The Sections value
	 */
	public Map getSections() {
		return sections;
	}

	/**
	 * Gets the Section attribute of the IniFile object
	 *
	 * @param strSection section name to get
	 * @return The Section value
	 * @throws IOException
	 */
	public Map getSection(String strSection) throws IOException {
		Object o = sections.get(strSection.toLowerCase());
		if (o == null) throw new IOException("section with name " + strSection + " does not exist");
		return (Map) o;
	}

	/**
	 * Gets the Section attribute of the IniFile object, return null if section not exist
	 *
	 * @param strSection section name to get
	 * @return The Section value
	 */
	public Map getSectionEL(String strSection) {
		Object o = sections.get(strSection.toLowerCase());
		if (o == null) return null;
		return (Map) o;
	}

	/**
	 * Gets the NullOrEmpty attribute of the IniFile object
	 *
	 * @param section section to check
	 * @param key key to check
	 * @return is empty or not
	 */
	public boolean isNullOrEmpty(String section, String key) {
		String value = getKeyValueEL(section, key);
		return (value == null || value.length() == 0);
	}

	/**
	 * Gets the KeyValue attribute of the IniFile object
	 *
	 * @param strSection section to get
	 * @param key key to get
	 * @return matching alue
	 * @throws IOException
	 */
	public String getKeyValue(String strSection, String key) throws IOException {
		Object o = getSection(strSection).get(key.toLowerCase());
		if (o == null) throw new IOException("key " + key + " doesn't exist in section " + strSection);
		return (String) o;

	}

	/**
	 * Gets the KeyValue attribute of the IniFile object, if not exist return null
	 *
	 * @param strSection section to get
	 * @param key key to get
	 * @return matching alue
	 */
	public String getKeyValueEL(String strSection, String key) {
		Map map = getSectionEL(strSection);
		if (map == null) return null;
		Object o = map.get(key.toLowerCase());
		if (o == null) return null;
		return (String) o;

	}

	/**
	 * loads the ini file
	 * 
	 * @param in inputstream to read
	 * @throws IOException
	 */
	public void load(InputStream in) throws IOException {

		BufferedReader input = IOUtil.toBufferedReader(new InputStreamReader(in));
		String read;
		Map section = null;
		String sectionName;
		while ((read = input.readLine()) != null) {
			if (read.startsWith(";") || read.startsWith("#")) {
				continue;
			}
			else if (read.startsWith("[")) {
				// new section
				sectionName = read.substring(1, read.indexOf("]")).trim().toLowerCase();
				section = getSectionEL(sectionName);
				if (section == null) {
					section = newMap();
					sections.put(sectionName, section);
				}
			}
			else if (read.indexOf("=") != -1 && section != null) {
				// new key
				String key = read.substring(0, read.indexOf("=")).trim().toLowerCase();
				String value = read.substring(read.indexOf("=") + 1).trim();
				section.put(key, value);
			}
		}

	}

	/**
	 * save back content to ini file
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		if (!file.exists()) file.createFile(true);
		OutputStream out = IOUtil.toBufferedOutputStream(file.getOutputStream());
		Iterator it = sections.keySet().iterator();
		PrintWriter output = new PrintWriter(out);
		try {
			while (it.hasNext()) {
				String strSection = (String) it.next();
				output.println("[" + strSection + "]");
				Map section = getSectionEL(strSection);
				Iterator iit = section.keySet().iterator();
				while (iit.hasNext()) {
					String key = (String) iit.next();
					output.println(key + "=" + section.get(key));
				}
			}
		}
		finally {
			IOUtil.flushEL(output);
			IOUtil.closeEL(output);
			IOUtil.flushEL(out);
			IOUtil.closeEL(out);
		}
	}

	/**
	 * removes a selection
	 *
	 * @param strSection section to remove
	 */
	public void removeSection(String strSection) {
		sections.remove(strSection);
	}

	/**
	 * 
	 * @param file
	 * @return return a struct with all section an dkey list as value
	 * @throws IOException
	 */
	public static Struct getProfileSections(Resource file) throws IOException {
		IniFile ini = new IniFile(file);
		Struct rtn = new StructImpl(Struct.TYPE_SYNC);
		Map sections = ini.getSections();
		Iterator it = sections.keySet().iterator();
		while (it.hasNext()) {
			String strSection = (String) it.next();
			Map section = ini.getSectionEL(strSection);
			Iterator iit = section.keySet().iterator();
			StringBuilder sb = new StringBuilder();
			while (iit.hasNext()) {
				if (sb.length() != 0) sb.append(',');
				sb.append(iit.next());
			}
			rtn.setEL(strSection, sb.toString());
		}
		return rtn;
	}
}