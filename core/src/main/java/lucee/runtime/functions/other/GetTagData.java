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
/**
 * Implements the CFML Function getfunctiondescription
 */
package lucee.runtime.functions.other;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.customtag.InitFile;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.tag.CFTagCore;
import lucee.runtime.tag.TagUtil;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibFactory;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.library.tag.TagLibTagScript;

public final class GetTagData implements Function {

	private static final long serialVersionUID = -4928080244340202246L;

	public static Struct call(PageContext pc, String nameSpace, String strTagName) throws PageException {
		return _call(pc, nameSpace, strTagName, pc.getCurrentTemplateDialect());
	}

	public static Struct call(PageContext pc, String nameSpace, String strTagName, String strDialect) throws PageException {
		int dialect = ConfigWebUtil.toDialect(strDialect, -1);
		if (dialect == -1) throw new FunctionException(pc, "GetTagData", 3, "dialect", "invalid dialect [" + strDialect + "] definition");

		return _call(pc, nameSpace, strTagName, dialect);
	}

	private static Struct _call(PageContext pc, String nameSpace, String strTagName, int dialect) throws PageException {
		TagLibTag tlt = TagUtil.getTagLibTag(pc, dialect, nameSpace, strTagName);
		if (tlt == null) throw new ExpressionException("tag [" + nameSpace + strTagName + "] is not a built in tag");

		// CFML Based Function
		Class clazz = null;
		try {
			clazz = tlt.getTagClassDefinition().getClazz();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		if (clazz == CFTagCore.class) {
			PageContextImpl pci = (PageContextImpl) pc;
			boolean prior = pci.useSpecialMappings(true);
			try {
				return cfmlBasedTag(pc, tlt.getTagLib(), tlt);
			}
			finally {
				pci.useSpecialMappings(prior);
			}

		}
		return javaBasedTag(tlt.getTagLib(), tlt);
	}

	private static Struct cfmlBasedTag(PageContext pc, TagLib tld, TagLibTag tag) throws PageException {

		// Map attrs = tag.getAttributes();

		TagLibTagAttr attrFilename = tag.getAttribute("__filename");
		TagLibTagAttr attrMapping = tag.getAttribute("__mapping");
		TagLibTagAttr attrIsWeb = tag.getAttribute("__isweb");

		String filename = Caster.toString(attrFilename.getDefaultValue());
		String name = Caster.toString(attrFilename.getDefaultValue());
		String mapping = Caster.toString(attrMapping.getDefaultValue());
		if (StringUtil.isEmpty(mapping)) mapping = "mapping-tag";
		boolean isWeb = Caster.toBooleanValue(attrIsWeb.getDefaultValue());
		InitFile source = CFTagCore.createInitFile(pc, isWeb, filename, mapping);
		String callPath = ResourceUtil.removeExtension(source.getFilename(), source.getFilename());

		Component cfc = ComponentLoader.loadComponent(pc, source.getPageSource(), callPath, false, true);
		ComponentSpecificAccess cw = ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE, cfc);
		Struct metadata = Caster.toStruct(cw.get("metadata", null), null, false);

		Struct sct = new StructImpl();
		sct.set("nameSpaceSeperator", tld.getNameSpaceSeparator());
		sct.set("nameSpace", tld.getNameSpace());
		sct.set(KeyConstants._name, name.substring(0, name.lastIndexOf('.')));
		sct.set("hasNameAppendix", Boolean.FALSE);
		sct.set(KeyConstants._status, "implemented");
		sct.set(KeyConstants._type, "cfml");

		sct.set("bodyType", getBodyType(tag));
		sct.set("attrMin", Caster.toDouble(0));
		sct.set("attrMax", Caster.toDouble(0));
		sct.set("attributeCollection", getSupportAttributeCollection(tag));

		// TODO add support for script for cfml tags
		Struct scp = new StructImpl();
		sct.set(KeyConstants._script, scp);
		scp.set("rtexpr", Boolean.FALSE);
		scp.set(KeyConstants._type, "none");

		if (metadata != null) {
			sct.set(KeyConstants._description, metadata.get("hint", ""));
			sct.set("attributeType", metadata.get("attributeType", ""));
			sct.set("parseBody", Caster.toBoolean(metadata.get("parseBody", Boolean.FALSE), Boolean.FALSE));

			Struct _attrs = new StructImpl(Struct.TYPE_LINKED);
			sct.set(KeyConstants._attributes, _attrs);

			Struct srcAttrs = Caster.toStruct(metadata.get(KeyConstants._attributes, null), null, false);
			Struct src;
			if (srcAttrs != null) {
				// Key[] keys = srcAttrs.keys();
				Iterator<Entry<Key, Object>> it = srcAttrs.entryIterator();
				Entry<Key, Object> e;
				while (it.hasNext()) {
					e = it.next();
					src = Caster.toStruct(e.getValue(), null, false);
					if (Caster.toBooleanValue(src.get(KeyConstants._hidden, null), false)) continue;
					Struct _attr = new StructImpl();
					_attr.set(KeyConstants._status, "implemented");
					_attr.set(KeyConstants._description, src.get(KeyConstants._hint, ""));
					_attr.set(KeyConstants._type, src.get(KeyConstants._type, "any"));
					_attr.set(KeyConstants._required, Caster.toBoolean(src.get(KeyConstants._required, ""), null));
					_attr.set("scriptSupport", "none");
					_attrs.setEL(e.getKey().getLowerString(), _attr);

				}
			}

		}

		/*
		 * /////////////////////
		 * 
		 * 
		 * Map atts = tag.getAttributes(); Iterator it = atts.keySet().iterator();
		 * 
		 * while(it.hasNext()) { Object key = it.next(); TagLibTagAttr attr=(TagLibTagAttr) atts.get(key);
		 * if(attr.getHidden()) continue; //for(int i=0;i<args.size();i++) { Struct _arg=new StructImpl();
		 * _arg.set("status",TagLibFactory.toStatus(attr.getStatus()));
		 * _arg.set("description",attr.getDescription()); _arg.set("type",attr.getType());
		 * _arg.set("required",attr.isRequired()?Boolean.TRUE:Boolean.FALSE);
		 * _args.setEL(attr.getName(),_arg); }
		 */

		return sct;
	}

	private static Struct javaBasedTag(TagLib tld, TagLibTag tag) throws PageException {
		Struct sct = new StructImpl();
		sct.set("nameSpaceSeperator", tld.getNameSpaceSeparator());
		sct.set("nameSpace", tld.getNameSpace());
		sct.set(KeyConstants._name, tag.getName());
		sct.set(KeyConstants._description, tag.getDescription());
		sct.set(KeyConstants._status, TagLibFactory.toStatus(tag.getStatus()));

		sct.set("attributeType", getAttributeType(tag));
		sct.set("parseBody", Caster.toBoolean(tag.getParseBody()));
		sct.set("bodyType", getBodyType(tag));
		sct.set("attrMin", Caster.toDouble(tag.getMin()));
		sct.set("attrMax", Caster.toDouble(tag.getMax()));
		sct.set("hasNameAppendix", Caster.toBoolean(tag.hasAppendix()));
		sct.set("attributeCollection", getSupportAttributeCollection(tag));
		if (tag.getIntroduced() != null) sct.set(GetFunctionData.INTRODUCED, tag.getIntroduced().toString());

		// script
		TagLibTagScript script = tag.getScript();
		if (script != null) {
			Struct scp = new StructImpl();
			sct.set(KeyConstants._script, scp);
			scp.set("rtexpr", Caster.toBoolean(script.getRtexpr()));
			scp.set(KeyConstants._type, TagLibTagScript.toType(script.getType(), "none"));
			if (script.getType() == TagLibTagScript.TYPE_SINGLE) {
				TagLibTagAttr attr = script.getSingleAttr();
				if (attr != null) scp.set("singletype", attr.getScriptSupportAsString());
				else scp.set("singletype", "none");
			}
		}

		sct.set(KeyConstants._type, "java");

		Struct _args = new StructImpl();
		sct.set(KeyConstants._attributes, _args);

		// Map<String,TagLibTagAttr> atts = tag.getAttributes();
		Iterator<Entry<String, TagLibTagAttr>> it = tag.getAttributes().entrySet().iterator();
		Entry<String, TagLibTagAttr> e;
		while (it.hasNext()) {
			e = it.next();
			TagLibTagAttr attr = e.getValue();
			if (attr.getHidden()) continue;
			// for(int i=0;i<args.size();i++) {
			Struct _arg = new StructImpl();
			_arg.set(KeyConstants._status, TagLibFactory.toStatus(attr.getStatus()));
			_arg.set(KeyConstants._description, attr.getDescription());
			_arg.set(KeyConstants._type, attr.getType());
			if (attr.getAlias() != null) _arg.set(KeyConstants._alias, ListUtil.arrayToList(attr.getAlias(), ","));
			if (attr.getValues() != null) _arg.set(KeyConstants._values, Caster.toArray(attr.getValues()));
			if (attr.getDefaultValue() != null) _arg.set("defaultValue", attr.getDefaultValue());
			_arg.set(KeyConstants._required, attr.isRequired() ? Boolean.TRUE : Boolean.FALSE);
			_arg.set("scriptSupport", attr.getScriptSupportAsString());
			if (attr.getIntroduced() != null) _arg.set(GetFunctionData.INTRODUCED, attr.getIntroduced().toString());

			_args.setEL(attr.getName(), _arg);
		}
		return sct;
	}

	private static String getBodyType(TagLibTag tag) {
		if (!tag.getHasBody()) return "prohibited";
		if (tag.isBodyFree()) return "free";
		return "required";
	}

	private static String getAttributeType(TagLibTag tag) {
		int type = tag.getAttributeType();
		if (TagLibTag.ATTRIBUTE_TYPE_DYNAMIC == type) return "dynamic";
		if (TagLibTag.ATTRIBUTE_TYPE_FIXED == type) return "fixed";
		if (TagLibTag.ATTRIBUTE_TYPE_MIXED == type) return "mixed";
		if (TagLibTag.ATTRIBUTE_TYPE_NONAME == type) return "noname";

		return "fixed";
	}

	private static Boolean getSupportAttributeCollection(TagLibTag tag) {
		return !tag.hasTTTClassDefinition() ? Boolean.TRUE : Boolean.FALSE;
	}
}
