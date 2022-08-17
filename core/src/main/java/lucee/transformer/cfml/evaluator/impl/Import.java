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
package lucee.transformer.cfml.evaluator.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagImport;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.CustomTagLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;
import lucee.transformer.library.tag.TagLibFactory;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * 
 */
public final class Import extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
	}

	@Override
	public TagLib execute(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, Data data) throws TemplateException {
		TagImport ti = (TagImport) tag;
		Attribute p = tag.getAttribute("prefix");
		Attribute t = tag.getAttribute("taglib");
		Attribute path = tag.getAttribute("path");

		if (p != null || t != null) {
			if (p == null) throw new TemplateException(data.srcCode, "Wrong Context, missing attribute [prefix] for tag [" + tag.getFullname() + "]");
			if (t == null) throw new TemplateException(data.srcCode, "Wrong Context, missing attribute [taglib] for tag [" + tag.getFullname() + "]");

			if (path != null) throw new TemplateException(data.srcCode, "Wrong context, you have an invalid combination of attributes for the tag [" + tag.getFullname() + "], "
					+ "you cannot mix attribute [path] with attributes [taglib] and [prefix]");

			return executePT(config, tag, libTag, flibs, data.srcCode);
		}
		if (path == null) throw new TemplateException(data.srcCode, "Wrong context, you have an invalid combination of attributes for the tag [" + tag.getFullname() + "], "
				+ "you need to define the attributes [prefix] and [taglib], the attribute [path] or simply define an attribute value");

		String strPath = ASMUtil.getAttributeString(tag, "path", null);
		if (strPath == null) throw new TemplateException(data.srcCode, "attribute [path] must be a constant value");
		ti.setPath(strPath);

		return null;

	}

	private TagLib executePT(Config config, Tag tag, TagLibTag libTag, FunctionLib[] flibs, SourceCode sc) throws TemplateException {

		// Attribute prefix
		String nameSpace = ASMUtil.getAttributeString(tag, "prefix", null);
		if (nameSpace == null) throw new TemplateException(sc, "attribute [prefix] must be a constant value");
		nameSpace = nameSpace.trim();
		String nameSpaceSeparator = StringUtil.isEmpty(nameSpace) ? "" : ":";

		// Attribute taglib
		String textTagLib = ASMUtil.getAttributeString(tag, "taglib", null);
		if (textTagLib == null) throw new TemplateException(sc, "attribute [taglib] must be a constant value");

		textTagLib = textTagLib.replace('\\', '/');
		textTagLib = ConfigWebUtil.replacePlaceholder(textTagLib, config);
		// File TagLib
		String ext = ResourceUtil.getExtension(textTagLib, null);
		boolean hasTldExtension = "tld".equalsIgnoreCase(ext) || "tldx".equalsIgnoreCase(ext);

		Resource absFile = config.getResource(textTagLib);
		// TLD
		if (absFile.isFile()) return _executeTLD(config, absFile, nameSpace, nameSpaceSeparator, sc);
		// CTD
		// else if(absFile.isDirectory()) return _executeCTD(absFile,textPrefix);

		// Second Change
		if (textTagLib.startsWith("/")) {
			// config.getPhysical(textTagLib);
			PageSource ps = config.getPageSourceExisting(null, null, textTagLib, false, false, true, false);

			// config.getConfigDir()
			if (ps != null) {
				if (ps.physcalExists()) {
					Resource file = ps.getPhyscalFile();
					// TLD
					if (file.isFile()) return _executeTLD(config, file, nameSpace, nameSpaceSeparator, sc);
				}
				// CTD
				if (!hasTldExtension) return _executeCTD(textTagLib, nameSpace, nameSpaceSeparator);
			}
		}
		else {
			PageSource ps = sc instanceof PageSourceCode ? ((PageSourceCode) sc).getPageSource() : null;
			Resource sourceFile = ps == null ? null : ps.getPhyscalFile();
			if (sourceFile != null) {
				Resource file = sourceFile.getParentResource().getRealResource(textTagLib);
				// TLD
				if (file.isFile()) return _executeTLD(config, file, nameSpace, nameSpaceSeparator, sc);
				// CTD
				if (!hasTldExtension) return _executeCTD(textTagLib, nameSpace, nameSpaceSeparator);
			}
		}
		throw new TemplateException(sc, "invalid definition of the attribute taglib [" + textTagLib + "]");
	}

	/**
	 * @param fileTagLib
	 * @return
	 * @throws EvaluatorException
	 */
	private TagLib _executeTLD(Config config, Resource fileTagLib, String nameSpace, String nameSpaceSeparator, SourceCode cfml) throws TemplateException {
		// change extesnion
		String ext = ResourceUtil.getExtension(fileTagLib, null);
		if ("jar".equalsIgnoreCase(ext)) {
			// check anchestor file
			Resource newFileTagLib = ResourceUtil.changeExtension(fileTagLib, "tld");
			if (newFileTagLib.exists()) fileTagLib = newFileTagLib;
			// check inside jar
			else {
				Resource tmp = getTLDFromJarAsFile(config, fileTagLib);
				if (tmp != null) fileTagLib = tmp;
			}
		}

		try {

			TagLib taglib = TagLibFactory.loadFromFile(fileTagLib, config.getIdentification());
			taglib.setNameSpace(nameSpace);
			taglib.setNameSpaceSeperator(nameSpaceSeparator);
			return taglib;
		}
		catch (TagLibException e) {

			throw new TemplateException(cfml, e.getMessage());
		}
	}

	private Resource getTLDFromJarAsFile(Config config, Resource jarFile) {
		Resource jspTagLibDir = config.getTempDirectory().getRealResource("jsp-taglib");
		if (!jspTagLibDir.exists()) jspTagLibDir.mkdirs();

		String filename = null;
		try {
			filename = Md5.getDigestAsString(ResourceUtil.getCanonicalPathEL(jarFile) + jarFile.lastModified());
		}
		catch (IOException e) {
		}

		Resource tldFile = jspTagLibDir.getRealResource(filename + ".tld");
		if (tldFile.exists()) return tldFile;
		tldFile = jspTagLibDir.getRealResource(filename + ".tldx");
		if (tldFile.exists()) return tldFile;

		byte[] barr = getTLDFromJarAsBarr(config, jarFile);
		if (barr == null) return null;

		try {
			IOUtil.copy(new ByteArrayInputStream(barr), tldFile, true);
		}
		catch (IOException e) {
		}
		return tldFile;
	}

	private byte[] getTLDFromJarAsBarr(Config c, Resource jarFile) {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(IOUtil.toBufferedInputStream(jarFile.getInputStream()));

			byte[] buffer = new byte[0xffff];
			int bytes_read;

			ZipEntry ze;
			byte[] barr;
			while ((ze = zis.getNextEntry()) != null) {
				if (!ze.isDirectory() && (StringUtil.endsWithIgnoreCase(ze.getName(), ".tld") || StringUtil.endsWithIgnoreCase(ze.getName(), ".tldx"))) {
					LogUtil.log(ThreadLocalPageContext.get(c), lucee.commons.io.log.Log.LEVEL_INFO, Import.class.getName(),
							"found tld in file [" + jarFile + "] at position " + ze.getName());
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while ((bytes_read = zis.read(buffer)) != -1)
						baos.write(buffer, 0, bytes_read);
					// String name = ze.getName().replace('\\', '/');
					barr = baos.toByteArray();
					zis.closeEntry();
					baos.close();
					return barr;
				}
			}
		}
		catch (IOException ioe) {
		}
		finally {
			IOUtil.closeEL(zis);
		}
		return null;
	}

	/**
	 * @param textTagLib
	 * @param nameSpace
	 * @return
	 */
	private TagLib _executeCTD(String textTagLib, String nameSpace, String nameSpaceSeparator) {
		return new CustomTagLib(textTagLib, nameSpace, nameSpaceSeparator);
	}

}