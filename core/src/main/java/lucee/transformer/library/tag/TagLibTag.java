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
package lucee.transformer.library.tag;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

//import org.objectweb.asm.Type;
import org.osgi.framework.BundleException;
import org.osgi.framework.Version;
import org.xml.sax.Attributes;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.type.util.ArrayUtil;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.cast.CastOther;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagOther;
import lucee.transformer.cfml.attributes.AttributeEvaluator;
import lucee.transformer.cfml.attributes.AttributeEvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.TagEvaluator;
import lucee.transformer.cfml.tag.TagDependentBodyTransformer;
import lucee.transformer.expression.Expression;
import lucee.transformer.library.ClassDefinitionImpl;

/**
 * Die Klasse TagLibTag repaesentiert ein einzelne Tag Definition einer TagLib, beschreibt also alle
 * Informationen die man zum validieren eines Tags braucht.
 */
public final class TagLibTag {

	public final static int ATTRIBUTE_TYPE_FIXED = 0;
	public final static int ATTRIBUTE_TYPE_DYNAMIC = 1;
	public final static int ATTRIBUTE_TYPE_NONAME = 3;
	public final static int ATTRIBUTE_TYPE_MIXED = 4;

	/**
	 * Definition des Attribut Type
	 */
	// public final static int ATTRIBUTE_TYPE_FULLDYNAMIC=2; deprecated
	/**
	 * Definition des Attribut Type
	 */

	private final static Class[] CONSTRUCTOR_PARAMS3 = new Class[] { Factory.class, Position.class, Position.class };

	private int attributeType;
	private String name;
	private boolean hasBody = true;

	private boolean isBodyReq = false;
	private boolean isTagDependent = false;
	private boolean bodyFree = true;

	private boolean parseBody;
	private boolean hasAppendix;
	private String description = "";
	private ClassDefinition tagCD;
	private ClassDefinition tteCD;
	private ClassDefinition tdbtCD;
	private ClassDefinition tttCD;
	private int min;
	private int max;
	private TagLib tagLib;
	private TagEvaluator eval;
	private TagDependentBodyTransformer tdbt;

	private Map<String, TagLibTagAttr> attributes = new LinkedHashMap<String, TagLibTagAttr>();
	private Map<String, String> setters = new HashMap<String, String>();
	private TagLibTagAttr attrFirst;
	private TagLibTagAttr attrLast;

	private ClassDefinition<? extends AttributeEvaluator> cdAttributeEvaluator;
	private boolean handleException;
	private boolean hasDefaultValue = false;
	// private Type tagType;
	private Constructor tttConstructor;
	private boolean allowRemovingLiteral;
	private TagLibTagAttr defaultAttribute;
	private short status = TagLib.STATUS_IMPLEMENTED;
	private Class clazz;
	private TagLibTagScript script;
	private final static TagLibTagAttr UNDEFINED = new TagLibTagAttr(null);
	private TagLibTagAttr singleAttr = UNDEFINED;
	private Object attrUndefinedValue;
	private String bundleName;
	private Version bundleVersion;
	private Version introduced;

	public TagLibTag duplicate(boolean cloneAttributes) {
		TagLibTag tlt = new TagLibTag(tagLib);

		tlt.attributeType = attributeType;
		tlt.name = name;
		tlt.hasBody = hasBody;
		tlt.isBodyReq = isBodyReq;
		tlt.isTagDependent = isTagDependent;
		tlt.bodyFree = bodyFree;
		tlt.parseBody = parseBody;
		tlt.hasAppendix = hasAppendix;
		tlt.description = description;
		tlt.tagCD = tagCD;
		tlt.bundleName = bundleName;
		tlt.bundleVersion = bundleVersion;
		tlt.tteCD = tteCD;
		tlt.eval = eval;
		tlt.tdbtCD = tdbtCD;
		tlt.min = min;
		tlt.max = max;
		tlt.cdAttributeEvaluator = cdAttributeEvaluator;
		tlt.handleException = handleException;
		tlt.hasDefaultValue = hasDefaultValue;
		// tlt.tagType=tagType;
		tlt.tttCD = tttCD;
		tlt.tttConstructor = tttConstructor;
		tlt.allowRemovingLiteral = allowRemovingLiteral;
		tlt.status = status;

		tlt.eval = null;
		tlt.tdbt = null;

		Iterator<Entry<String, TagLibTagAttr>> it = attributes.entrySet().iterator();
		if (cloneAttributes) {
			while (it.hasNext()) {
				tlt.setAttribute(it.next().getValue().duplicate(tlt));
			}
			if (defaultAttribute != null) tlt.defaultAttribute = defaultAttribute.duplicate(tlt);
		}
		else {
			while (it.hasNext()) {
				tlt.setAttribute(it.next().getValue());
				tlt.attrFirst = attrFirst;
				tlt.attrLast = attrLast;
			}
			tlt.defaultAttribute = defaultAttribute;
		}

		// setter
		Iterator<Entry<String, String>> sit = setters.entrySet().iterator();
		Entry<String, String> se;
		while (sit.hasNext()) {
			se = sit.next();
			tlt.setters.put(se.getKey(), se.getValue());
		}

		/*
		 * private Map attributes=new HashMap(); private TagLibTagAttr attrFirst; private TagLibTagAttr
		 * attrLast;
		 * 
		 * private Map setters=new HashMap(); private TagLibTagAttr defaultAttribute;
		 */
		return tlt;
	}

	/**
	 * Geschuetzer Konstruktor ohne Argumente.
	 * 
	 * @param tagLib
	 */
	public TagLibTag(TagLib tagLib) {
		this.tagLib = tagLib;
	}

	/**
	 * Gibt alle Attribute (TagLibTagAttr) eines Tag als HashMap zurueck.
	 * 
	 * @return HashMap Attribute als HashMap.
	 */
	public Map<String, TagLibTagAttr> getAttributes() {
		return attributes;
	}

	/**
	 * Gibt ein bestimmtes Attribut anhand seines Namens zurueck, falls dieses Attribut nicht existiert
	 * wird null zurueckgegeben.
	 * 
	 * @param name Name des Attribut das zurueckgegeben werden soll.
	 * @return Attribute das angfragt wurde oder null.
	 */
	public TagLibTagAttr getAttribute(String name) {
		return getAttribute(name, false);
	}

	public TagLibTagAttr getAttribute(String name, boolean checkAlias) {
		TagLibTagAttr attr = attributes.get(name);
		// checking alias
		if (attr == null && checkAlias) return getAttributeByAlias(name);
		return attr;

	}

	public TagLibTagAttr getAttributeByAlias(String alias) {
		Iterator<TagLibTagAttr> it = attributes.values().iterator();
		TagLibTagAttr attr;
		String[] aliases;
		while (it.hasNext()) {
			attr = it.next();
			if (ArrayUtil.isEmpty(attr.getAlias())) continue;
			aliases = attr.getAlias();
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].equalsIgnoreCase(alias)) return attr;
			}
		}
		return null;
	}

	/**
	 * Gibt das erste Attribut, welches innerhalb des Tag definiert wurde, zurueck.
	 * 
	 * @return Attribut das angfragt wurde oder null.
	 */
	public TagLibTagAttr getFirstAttribute() {
		return attrFirst;
	}

	/**
	 * Gibt das letzte Attribut, welches innerhalb des Tag definiert wurde, zurueck.
	 * 
	 * @return Attribut das angfragt wurde oder null.
	 */
	public TagLibTagAttr getLastAttribute() {
		return attrLast;
	}

	/**
	 * Gibt den Namen des Tag zurueck.
	 * 
	 * @return String Name des Tag.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gibt den kompletten Namen des Tag zurueck, inkl. Name-Space und Trenner.
	 * 
	 * @return String Kompletter Name des Tag.
	 */
	public String getFullName() {
		String fullName;
		if (tagLib != null) {
			fullName = tagLib.getNameSpaceAndSeparator() + name;
		}
		else {
			fullName = name;
		}
		return fullName;
	}

	public ClassDefinition getTagClassDefinition() {
		return tagCD;

	}

	public void setTagClassDefinition(String tagClass, Identification id, Attributes attributes) {
		this.tagCD = ClassDefinitionImpl.toClassDefinition(tagClass, id, attributes);
	}

	public void setTagClassDefinition(ClassDefinition cd) {
		this.tagCD = cd;
	}

	/*
	 * public Type getTagTypeX() throws ClassException, BundleException { if(tagType==null) {
	 * tagType=Type.getType(getTagClassDefinition().getClazz()); } return tagType; }
	 */

	/**
	 * @return the status
	 *         (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
	 */
	public short getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 *            (TagLib.,TagLib.STATUS_IMPLEMENTED,TagLib.STATUS_DEPRECATED,TagLib.STATUS_UNIMPLEMENTED)
	 */
	public void setStatus(short status) {
		this.status = status;
	}

	/**
	 * Gibt die Klassendefinition, der Klasse die den Evaluator (Translation Time Evaluator)
	 * implementiert, als Zeichenkette zurueck. Falls kein Evaluator definiert ist wird null
	 * zurueckgegeben.
	 * 
	 * @return String Zeichenkette der Klassendefinition.
	 */
	private ClassDefinition getTTEClassDefinition() {
		return tteCD;
	}

	public ClassDefinition getTTTClassDefinition() {
		return tttCD;
	}

	/**
	 * Gibt den Evaluator (Translation Time Evaluator) dieser Klasse zurueck. Falls kein Evaluator
	 * definiert ist, wird null zurueckgegeben.
	 * 
	 * @return Implementation des Evaluator zu dieser Klasse.
	 * @throws EvaluatorException Falls die Evaluator-Klasse nicht geladen werden kann.
	 */
	public TagEvaluator getEvaluator() throws EvaluatorException {
		if (!hasTTE()) return null;
		if (eval != null) return eval;
		try {
			eval = (TagEvaluator) ClassUtil.newInstance(getTTEClassDefinition().getClazz());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new EvaluatorException(t.getMessage());
		}
		return eval;
	}

	/**
	 * Gibt den TagDependentBodyTransformer dieser Klasse zurueck. Falls kein
	 * TagDependentBodyTransformer definiert ist, wird null zurueckgegeben.
	 * 
	 * @return Implementation des TagDependentBodyTransformer zu dieser Klasse.
	 * @throws TagLibException Falls die TagDependentBodyTransformer-Klasse nicht geladen werden kann.
	 */
	public TagDependentBodyTransformer getBodyTransformer() throws TagLibException {
		if (!hasTDBTClassDefinition()) return null;
		if (tdbt != null) return tdbt;
		try {
			tdbt = (TagDependentBodyTransformer) ClassUtil.newInstance(tdbtCD.getClazz());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new TagLibException(t);
		}
		return tdbt;
	}

	/**
	 * Gibt zurueck ob Exception durch die implementierte Klasse abgehandelt werden oder nicht
	 * 
	 * @return Wird eine Exception abgehandelt?
	 */
	public boolean handleException() {
		return handleException;
	}

	/**
	 * Gibt zurueck, ob eine Klassendefinition der Klasse die den Evaluator (Translation Time Evaluator)
	 * implementiert existiert.
	 * 
	 * @return Ob eine Evaluator definiert ist.
	 */
	public boolean hasTTE() {
		return tteCD != null || eval != null;
	}

	public boolean hasTTTClassDefinition() {
		return tttCD != null;
	}

	/**
	 * Gibt zurueck, ob eine Klassendefinition der Klasse die den TagDependentBodyTransformer
	 * implementiert existiert.
	 * 
	 * @return Ob eine Evaluator definiert ist.
	 */
	public boolean hasTDBTClassDefinition() {
		return tdbtCD != null;
	}

	/**
	 * Gibt den Attributetyp der Klasse zurueck. ( ATTRIBUTE_TYPE_FIX, ATTRIBUTE_TYPE_DYNAMIC,
	 * ATTRIBUTE_TYPE_NONAME)
	 * 
	 * @return int
	 */
	public int getAttributeType() {
		return attributeType;
	}

	/**
	 * Gibt zurueck, ob das Tag einen Body haben kann oder nicht.
	 * 
	 * @return Kann das Tag einen Body haben.
	 */
	public boolean getHasBody() {
		return hasBody;
	}

	/**
	 * Gibt die maximale Anzahl Attribute zurueck, die das Tag haben kann.
	 * 
	 * @return Maximale moegliche Anzahl Attribute.
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Gibt die minimale Anzahl Attribute zurueck, die das Tag haben muss.
	 * 
	 * @return Minimal moegliche Anzahl Attribute.
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Gibt die TagLib zurueck zu der das Tag gehoert.
	 * 
	 * @return TagLib Zugehoerige TagLib.
	 */
	public TagLib getTagLib() {
		return tagLib;
	}

	/**
	 * Gibt zurueck ob das Tag seinen Body parsen soll oder nicht.
	 * 
	 * @return Soll der Body geparst werden.
	 */
	public boolean getParseBody() {
		return parseBody;
	}

	/**
	 * Gibt zurueck, ob das Tag einen Appendix besitzen kann oder nicht.
	 * 
	 * @return Kann das Tag einen Appendix besitzen.
	 */
	public boolean hasAppendix() {
		return hasAppendix;
	}

	/**
	 * Fragt ab ob der Body eines Tag freiwillig ist oder nicht.
	 * 
	 * @return is required
	 */
	public boolean isBodyReq() {
		return isBodyReq;
	}

	/**
	 * Fragt ab ob die verarbeitung des Inhaltes eines Tag mit einem eigenen Transformer vorgenommen
	 * werden soll.
	 * 
	 * @return Fragt ab ob die verarbeitung des Inhaltes eines Tag mit einem eigenen Transformer
	 *         vorgenommen werden soll.
	 */
	public boolean isTagDependent() {
		return isTagDependent;
	}

	/**
	 * Setzt die TagLib des Tag. Diese Methode wird durch die Klasse TagLibFactory verwendet.
	 * 
	 * @param tagLib TagLib des Tag.
	 */
	protected void setTagLib(TagLib tagLib) {
		this.tagLib = tagLib;
	}

	/**
	 * Setzt ein einzelnes Attribut (TagLibTagAttr) eines Tag. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param attribute Attribute eines Tag.
	 */
	public void setAttribute(TagLibTagAttr attribute) {
		attributes.put(attribute.getName(), attribute);
		if (attrFirst == null) attrFirst = attribute;
		attrLast = attribute;
	}

	/**
	 * Setzt den Attributtyp eines Tag. ( ATTRIBUTE_TYPE_FIX, ATTRIBUTE_TYPE_DYNAMIC,
	 * ATTRIBUTE_TYPE_FULLDYNAMIC, ATTRIBUTE_TYPE_NONAME) Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param attributeType The attributeType to set
	 */
	public void setAttributeType(int attributeType) {

		this.attributeType = attributeType;
	}

	/**
	 * Setzt die Information, was fuer ein BodyContent das Tag haben kann. Diese Methode wird durch die
	 * Klasse TagLibFactory verwendet.
	 * 
	 * @param value BodyContent Information.
	 */
	public void setBodyContent(String value) {
		// empty, free, must, tagdependent
		value = value.toLowerCase().trim();
		// if(value.equals("jsp")) value="free";

		this.hasBody = !value.equals("empty");
		this.isBodyReq = !value.equals("free");
		this.isTagDependent = value.equals("tagdependent");
		bodyFree = value.equals("free");
	}

	/**
	 * Setzt wieviele Attribute das Tag maximal haben darf. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param max The max to set
	 */
	protected void setMax(int max) {
		this.max = max;
	}

	/**
	 * Setzt wieviele Attribute das Tag minimal haben darf. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param min The min to set
	 */
	protected void setMin(int min) {
		this.min = min;
	}

	/**
	 * Setzt den Namen des Tag. Diese Methode wird durch die Klasse TagLibFactory verwendet.
	 * 
	 * @param name Name des Tag.
	 */
	public void setName(String name) {
		this.name = name.toLowerCase();
	}

	public void setBundleName(String bundleName) {
		this.bundleName = bundleName.trim();
	}

	public void setBundleVersion(String bundleVersion) {
		// TODO allow 1.0.0.0-2.0.0.0,3.0.0.0
		this.bundleVersion = OSGiUtil.toVersion(bundleVersion.trim(), null);
	}

	/**
	 * Setzt die implementierende Klassendefinition des Evaluator. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param tteClass Klassendefinition der Evaluator-Implementation.
	 */
	protected void setTTEClassDefinition(String tteClass, Identification id, Attributes attr) {
		this.tteCD = ClassDefinitionImpl.toClassDefinition(tteClass, id, attr);
	}

	protected void setTagEval(TagEvaluator eval) {
		this.eval = eval;
	}

	/**
	 * Setzt die implementierende Klassendefinition des Evaluator. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param tteClass Klassendefinition der Evaluator-Implementation.
	 */
	public void setTTTClassDefinition(String tttClass, Identification id, Attributes attr) {
		this.tttCD = ClassDefinitionImpl.toClassDefinition(tttClass, id, attr);
		this.tttConstructor = null;
	}

	/**
	 * Setzt die implementierende Klassendefinition des TagDependentBodyTransformer. Diese Methode wird
	 * durch die Klasse TagLibFactory verwendet.
	 * 
	 * @param tdbtClass Klassendefinition der TagDependentBodyTransformer-Implementation.
	 */
	public void setTDBTClassDefinition(String tdbtClass, Identification id, Attributes attr) {
		this.tdbtCD = ClassDefinitionImpl.toClassDefinition(tdbtClass, id, attr);
		this.tdbt = null;
	}

	/**
	 * Setzt, ob der Body des Tag geparst werden soll oder nicht. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param parseBody Soll der Body geparst werden.
	 */
	public void setParseBody(boolean parseBody) {
		this.parseBody = parseBody;
	}

	/**
	 * Setzt ob das Tag einen Appendix besitzen kann oder nicht. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param hasAppendix Kann das Tag einen Appendix besitzen.
	 */
	public void setAppendix(boolean hasAppendix) {
		this.hasAppendix = hasAppendix;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the bodyIsFree.
	 */
	public boolean isBodyFree() {
		return bodyFree;
	}

	public boolean hasBodyMethodExists() {
		Class clazz = getTagClassDefinition().getClazz(null);
		if (clazz == null) return false;

		try {
			java.lang.reflect.Method method = clazz.getMethod("hasBody", new Class[] { boolean.class });
			if (method == null) return false;
			return method.getReturnType() == void.class;
		}
		catch (Exception e) {
		}
		return false;
	}

	/**
	 * @return Gibt zurueck ob ein Attribut Evaluator definiert ist oder nicht.
	 */
	public boolean hasAttributeEvaluator() {
		return cdAttributeEvaluator != null;
	}

	/**
	 * @return Gibt den AttributeEvaluator zum Tag zurueck
	 * @throws AttributeEvaluatorException
	 */
	public AttributeEvaluator getAttributeEvaluator() throws AttributeEvaluatorException {
		if (!hasAttributeEvaluator()) return null;
		try {
			return (AttributeEvaluator) ClassUtil.loadInstance(cdAttributeEvaluator.getClazz());

		}
		catch (Exception e) {
			throw new AttributeEvaluatorException(e.getMessage());
		}
	}

	/**
	 * Setzt den Namen der Klasse welche einen AttributeEvaluator implementiert.
	 * 
	 * @param value Name der AttributeEvaluator Klassse
	 */
	public void setAttributeEvaluatorClassDefinition(String className, Identification id, Attributes attr) {
		cdAttributeEvaluator = ClassDefinitionImpl.toClassDefinition(className, id, attr);
	}

	/**
	 * sets if tag handle exception inside his body or not
	 * 
	 * @param handleException handle it or not
	 */
	public void setHandleExceptions(boolean handleException) {
		this.handleException = handleException;
	}

	/**
	 * @return
	 */
	public boolean hasDefaultValue() {
		return hasDefaultValue;
	}

	/**
	 * @param hasDefaultValue The hasDefaultValue to set.
	 */
	public void setHasDefaultValue(boolean hasDefaultValue) {
		this.hasDefaultValue = hasDefaultValue;
	}

	/**
	 * return ASM Tag for this tag
	 * 
	 * @param line
	 * @return
	 * 
	 */
	public Tag getTag(Factory f, Position start, Position end) throws TagLibException {
		if (StringUtil.isEmpty(tttCD)) return new TagOther(f, start, end);
		try {
			return _getTag(f, start, end);
		}
		catch (ClassException e) {
			throw new TagLibException(e.getMessage());
		}
		catch (NoSuchMethodException e) {
			throw new TagLibException(e.getMessage());
		}
		catch (Throwable e) {
			ExceptionUtil.rethrowIfNecessary(e);
			throw new TagLibException(e);
		}
	}

	private Tag _getTag(Factory f, Position start, Position end) throws ClassException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException, BundleException {
		if (tttConstructor == null) {
			Class clazz = tttCD.getClazz();
			tttConstructor = clazz.getConstructor(CONSTRUCTOR_PARAMS3);
		}
		return (Tag) tttConstructor.newInstance(new Object[] { f, start, end });
	}

	public void setAllowRemovingLiteral(boolean allowRemovingLiteral) {
		this.allowRemovingLiteral = allowRemovingLiteral;
	}

	/**
	 * @return the allowRemovingLiteral
	 */
	public boolean isAllowRemovingLiteral() {
		return allowRemovingLiteral;
	}

	public String getAttributeNames() {
		Iterator<String> it = attributes.keySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (it.hasNext()) {
			if (sb.length() > 0) sb.append(",");
			sb.append(it.next());
		}
		return sb.toString();
	}

	public String getSetter(Attribute attr, String typeClassName) {
		if (tagLib.isCore()) return "set" + StringUtil.ucFirst(attr.getName());

		String setter = setters.get(attr.getName());
		if (setter != null) return setter;
		setter = "set" + StringUtil.ucFirst(attr.getName());
		Class clazz;
		try {
			if (StringUtil.isEmpty(typeClassName)) typeClassName = CastOther.getType(attr.getType()).getClassName();
			clazz = getTagClassDefinition().getClazz();
			java.lang.reflect.Method m = ClassUtil.getMethodIgnoreCase(clazz, setter, new Class[] { ClassUtil.loadClass(typeClassName) });
			setter = m.getName();
		}
		catch (Exception e) {
			LogUtil.log((PageContext) null, TagLibTag.class.getName(), e);
		}
		setters.put(attr.getName(), setter);
		return setter;
	}

	public String getHash() {
		StringBuilder sb = new StringBuilder();
		sb.append(tagCD);
		sb.append(this.getAttributeNames());
		sb.append(this.getAttributeType());
		sb.append(this.getMax());
		sb.append(this.getMin());
		sb.append(this.getName());
		sb.append(this.getParseBody());
		sb.append(getTTEClassDefinition());
		sb.append(getTTTClassDefinition());
		Iterator<Entry<String, TagLibTagAttr>> it = this.getAttributes().entrySet().iterator();
		Entry<String, TagLibTagAttr> entry;
		while (it.hasNext()) {
			entry = it.next();
			sb.append(entry.getKey());
			sb.append(entry.getValue().getHash());
		}

		try {
			return Md5.getDigestAsString(sb.toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public TagLibTagAttr getDefaultAttribute() {
		return defaultAttribute;
	}

	public void setDefaultAttribute(TagLibTagAttr defaultAttribute) {
		this.defaultAttribute = defaultAttribute;
	}

	public void setScript(TagLibTagScript script) {
		this.script = script;
	}

	/**
	 * @return the script
	 */
	public TagLibTagScript getScript() {
		return script;
	}

	public TagLibTagAttr getSingleAttr() {

		if (singleAttr == UNDEFINED) {
			singleAttr = null;
			Iterator<TagLibTagAttr> it = getAttributes().values().iterator();
			TagLibTagAttr attr;
			while (it.hasNext()) {
				attr = it.next();
				if (attr.getNoname()) {
					singleAttr = attr;
					break;
				}
			}
		}
		return singleAttr;
	}

	/**
	 * attribute value set, if the attribute has no value defined
	 * 
	 * @return
	 */
	public Expression getAttributeUndefinedValue(Factory factory) {
		if (attrUndefinedValue == null) return factory.TRUE();
		return factory.createLiteral(attrUndefinedValue, factory.TRUE());
	}

	public void setAttributeUndefinedValue(String undefinedValue) {
		this.attrUndefinedValue = toUndefinedValue(undefinedValue);
	}

	public static Object toUndefinedValue(String undefinedValue) {
		undefinedValue = undefinedValue.trim();
		// boolean
		if (StringUtil.startsWithIgnoreCase(undefinedValue, "boolean:")) {
			String str = undefinedValue.substring(8).trim();
			Boolean b = Caster.toBoolean(str, null);
			if (b != null) return b;
		}
		// number
		else if (StringUtil.startsWithIgnoreCase(undefinedValue, "number:")) {
			String str = undefinedValue.substring(7).trim();
			Double d = Caster.toDouble(str, null);
			if (d != null) return d;

		}
		return undefinedValue;
	}

	public void setIntroduced(String introduced) {
		this.introduced = OSGiUtil.toVersion(introduced, null);
	}

	public Version getIntroduced() {
		return introduced;
	}

}