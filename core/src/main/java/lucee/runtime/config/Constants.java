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
package lucee.runtime.config;

import java.net.MalformedURLException;
import java.net.URL;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.runtime.extension.RHExtensionProvider;
import lucee.runtime.type.util.ArrayUtil;

public class Constants {
	private static final String CFML_SCRIPT_EXTENSION = "cfs";
	private static final String CFML_COMPONENT_EXTENSION = "cfc";

	public static final String CFML_NAME = "CFML";

	public static final String NAME = "Lucee";

	public static final String[] CFML_ALIAS_NAMES = new String[] { "CFML", "CFM" };

	public static final String GATEWAY_COMPONENT_EXTENSION = "cfc"; // MUST remove

	private static final String CFML_TEMPLATE_MAIN_EXTENSION = "cfm";

	public static final String[] CFML_MIMETYPES = new String[] { "text/cfml", "application/cfml" };

	public static final String[] DTDS_FLD = new String[] { "-//Lucee//DTD CFML Function Library 1.0//EN", "-//Railo//DTD CFML Function Library 1.0//EN" };

	public static final String[] DTDS_TLD = new String[] { "-//Lucee//DTD CFML Tag Library 1.0//EN", "-//Railo//DTD CFML Tag Library 1.0//EN" };

	public static final String CFML_APPLICATION_EVENT_HANDLER = "Application." + CFML_COMPONENT_EXTENSION;
	public static final String CFML_CLASSIC_APPLICATION_EVENT_HANDLER = "Application." + CFML_TEMPLATE_MAIN_EXTENSION;
	public static final String CFML_CLASSIC_APPLICATION_END_EVENT_HANDLER = "OnRequestEnd." + CFML_TEMPLATE_MAIN_EXTENSION;

	public static final String CFML_APPLICATION_TAG_NAME = "cfapplication";

	public static final String DEFAULT_PACKAGE = "org.lucee.cfml";
	public static final String WEBSERVICE_NAMESPACE_URI = "http://rpc.xml.coldfusion";

	public static URL DEFAULT_UPDATE_URL;
	private static String[] extensions;
	private static String[] cfmlExtensions;
	private static String[] componentExtensions;
	private static String[] templateExtensions;
	static {
		try {
			DEFAULT_UPDATE_URL = new URL("https://update.lucee.org");
		}
		catch (MalformedURLException e) {
		}
	}
	public static String DEFAULT_UPDATE_TYPE = "manual";

	public static final RHExtensionProvider[] RH_EXTENSION_PROVIDERS = new RHExtensionProvider[] {
			new RHExtensionProvider(HTTPUtil.toURL("https://extension.lucee.org", HTTPUtil.ENCODED_NO, null), true),
			new RHExtensionProvider(HTTPUtil.toURL("https://www.forgebox.io", HTTPUtil.ENCODED_NO, null), true) };

	public static final String CFML_SCRIPT_TAG_NAME = "script";

	public static final String CFML_SET_TAG_NAME = "set";

	public static final String CFML_COMPONENT_TAG_NAME = "component";

	public static final String CFML_CLASS_SUFFIX = "$cf";
	public static final String SUB_COMPONENT_APPENDIX = "inline";

	// TODO load this based on the servlet mapping
	public static final String[] cte = new String[] { CFML_TEMPLATE_MAIN_EXTENSION };

	public static String[] getCFMLTemplateExtensions() {
		return cte;
	}

	public static String getCFMLScriptExtension() {
		return CFML_SCRIPT_EXTENSION;
	}

	public static String getCFMLComponentExtension() {
		return CFML_COMPONENT_EXTENSION;
	}

	// merge methods above
	public static String[] getScriptExtensions() {
		return new String[] { getCFMLScriptExtension() };
	}

	public static String[] getTemplateExtensions() {
		if (templateExtensions == null) templateExtensions = getCFMLTemplateExtensions();
		return templateExtensions;
	}

	public static String[] getComponentExtensions() {
		if (componentExtensions == null) componentExtensions = new String[] { getCFMLComponentExtension() };
		return componentExtensions;
	}

	public static String[] getCFMLExtensions() {
		if (cfmlExtensions == null) cfmlExtensions = ArrayUtil.toArray(getCFMLTemplateExtensions(), getCFMLComponentExtension(), getCFMLScriptExtension());
		return cfmlExtensions;
	}

	public static String[] getExtensions() {
		if (extensions == null) extensions = ArrayUtil.toArray(getComponentExtensions(), getTemplateExtensions(), getScriptExtensions());
		return extensions;
	}

	public static boolean isCFMLComponentExtension(String extension) {
		if (StringUtil.isEmpty(extension)) return false;
		if (extension.startsWith(".")) extension = extension.substring(1);
		return getCFMLComponentExtension().trim().equalsIgnoreCase(extension);
	}

	public static boolean isCFMLScriptExtension(String extension) {
		if (StringUtil.isEmpty(extension)) return false;
		if (extension.startsWith(".")) extension = extension.substring(1);
		return getCFMLScriptExtension().trim().equalsIgnoreCase(extension);
	}

	public static boolean isComponentExtension(String extension) {
		if (StringUtil.isEmpty(extension)) return false;
		if (extension.startsWith(".")) extension = extension.substring(1);
		return getCFMLComponentExtension().trim().equalsIgnoreCase(extension);
	}

	public static boolean isCFML(Resource file) {
		for (String ext: getCFMLExtensions()) {
			if (file.getName().endsWith("." + ext)) return true;
		}
		return false;
	}
}