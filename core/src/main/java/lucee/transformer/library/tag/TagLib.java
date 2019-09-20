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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xml.sax.Attributes;

import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.transformer.cfml.ExprTransformer;
import lucee.transformer.cfml.expression.CFMLExprTransformer;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.Lib;

/**
 * Die Klasse TagLib repaesentiert eine Komplette TLD, mit ihrer Hilfe kann man alle Informationen,
 * zu einer TLD Abfragen.
 */
public class TagLib implements Cloneable, Lib {

	public static final short STATUS_IMPLEMENTED = 0;
	public static final short STATUS_DEPRECATED = 1;
	public static final short STATUS_UNIMPLEMENTED = 2;
	public static final short STATUS_HIDDEN = 4;

	/**
	 * Field <code>EXPR_TRANSFORMER</code>
	 */
	public static ClassDefinition<? extends ExprTransformer> EXPR_TRANSFORMER = new ClassDefinitionImpl<>(CFMLExprTransformer.class);

	private String shortName = "";
	private String displayName = null;
	private String type = "cfml";
	private String nameSpace;
	private String nameSpaceSeperator = ":";
	private ClassDefinition<? extends ExprTransformer> ELClass = EXPR_TRANSFORMER;
	private HashMap<String, TagLibTag> tags = new HashMap<String, TagLibTag>();
	private HashMap<String, TagLibTag> appendixTags = new HashMap<String, TagLibTag>();
	private ExprTransformer exprTransformer;

	private char[] nameSpaceAndNameSpaceSeperator;

	private boolean isCore;

	private String source;

	private URI uri;

	private String description;
	private TagLibTag[] scriptTags;
	private boolean ignoreUnknowTags;

	/**
	 * Geschuetzer Konstruktor ohne Argumente.
	 */
	protected TagLib(boolean isCore) {
		this.isCore = isCore;
	}

	protected TagLib() {
		this(false);
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Gibt den Name-Space einer TLD als String zurueck.
	 * 
	 * @return String Name der TLD.
	 */
	public String getNameSpace() {

		return nameSpace;
	}

	/**
	 * Gibt den Trenner zwischen Name-Space und Name einer TLD zurueck.
	 * 
	 * @return Name zwischen Name-Space und Name.
	 */
	public String getNameSpaceSeparator() {
		return nameSpaceSeperator;
	}

	/**
	 * Gibt den Name-Space inkl. dem Seperator zurueck.
	 * 
	 * @return String
	 */
	public String getNameSpaceAndSeparator() {
		return nameSpace + nameSpaceSeperator;
	}

	/**
	 * Gibt den Name-Space inkl. dem Seperator zurueck.
	 * 
	 * @return String
	 */
	public char[] getNameSpaceAndSeperatorAsCharArray() {
		if (nameSpaceAndNameSpaceSeperator == null) {
			nameSpaceAndNameSpaceSeperator = getNameSpaceAndSeparator().toCharArray();
		}
		return nameSpaceAndNameSpaceSeperator;
	}

	/**
	 * Gibt einen Tag (TagLibTag)zurueck, dessen Name mit dem uebergebenen Wert uebereinstimmt, falls
	 * keine uebereinstimmung gefunden wird, wird null zurueck gegeben.
	 * 
	 * @param name Name des Tag das zurueck gegeben werden soll.
	 * @return TagLibTag Tag das auf den Namen passt.
	 */
	public TagLibTag getTag(String name) {
		return tags.get(name.toLowerCase());
	}

	public TagLibTag getTag(Class clazz) {
		Iterator<TagLibTag> _tags = tags.values().iterator();
		TagLibTag tlt;
		while (_tags.hasNext()) {
			tlt = _tags.next();
			if (tlt.getTagClassDefinition().isClassNameEqualTo(clazz.getName(), true)) {
				return tlt;
			}
		}
		return null;
	}

	/**
	 * Gibt einen Tag (TagLibTag)zurueck, welches definiert hat, dass es einen Appendix besitzt. D.h.
	 * dass der Name des Tag mit weiteren Buchstaben erweitert sein kann, also muss nur der erste Teil
	 * des Namen vom Tag mit dem uebergebenen Namen uebereinstimmen. Wenn keine uebereinstimmung
	 * gefunden wird, wird null zurueck gegeben.
	 * 
	 * @param name Name des Tag inkl. Appendix das zurueck gegeben werden soll.
	 * @return TagLibTag Tag das auf den Namen passt.
	 */
	public TagLibTag getAppendixTag(String name) {
		Iterator<String> it = appendixTags.keySet().iterator();
		String match = "";
		while (it.hasNext()) {
			String tagName = StringUtil.toStringNative(it.next(), "");
			if (match.length() < tagName.length() && name.indexOf(tagName) == 0) {
				match = tagName;
			}
		}
		return getTag(match);
	}

	/**
	 * Gibt alle Tags (TagLibTag) als HashMap zurueck.
	 * 
	 * @return Alle Tags als HashMap.
	 */
	public Map<String, TagLibTag> getTags() {
		return tags;
	}

	/**
	 * Gibt die Klasse des ExprTransformer als Zeichenkette zurueck.
	 * 
	 * @return String
	 */
	public ClassDefinition<? extends ExprTransformer> getELClassDefinition() {
		return ELClass;
	}

	/**
	 * Laedt den innerhalb der TagLib definierten ExprTransfomer und gibt diesen zurueck. Load
	 * Expression Transfomer defined in the tag library and return it.
	 * 
	 * @return ExprTransformer
	 * @throws TagLibException
	 */
	public ExprTransformer getExprTransfomer() throws TagLibException {
		// Class cls;
		if (exprTransformer != null) return exprTransformer;

		try {
			exprTransformer = (ExprTransformer) ClassUtil.loadInstance(ELClass.getClazz());
			// exprTransformer = (ExprTransformer) cls.newInstance();
		}
		catch (Exception e) {
			throw new TagLibException(e);
		}
		return exprTransformer;
	}

	/**
	 * Fuegt der TagLib einen weiteren Tag hinzu. Diese Methode wird durch die Klasse TagLibFactory
	 * verwendet.
	 * 
	 * @param tag Neuer Tag.
	 */
	public void setTag(TagLibTag tag) {
		tag.setTagLib(this);
		tags.put(tag.getName(), tag);

		if (tag.hasAppendix()) appendixTags.put(tag.getName(), tag);
		else if (appendixTags.containsKey(tag.getName())) appendixTags.remove(tag.getName());
	}

	/**
	 * Fuegt der TagLib die Evaluator Klassendefinition als Zeichenkette hinzu. Diese Methode wird durch
	 * die Klasse TagLibFactory verwendet.
	 * 
	 * @param eLClass Zeichenkette der Evaluator Klassendefinition.
	 */
	protected void setELClass(String eLClass, Identification id, Attributes attributes) {
		this.ELClass = ClassDefinitionImpl.toClassDefinition(eLClass, id, attributes);
	}

	protected void setELClassDefinition(ClassDefinition cd) {
		this.ELClass = cd;
	}

	/**
	 * Fuegt der TagLib die die Definition des Name-Space hinzu. Diese Methode wird durch die Klasse
	 * TagLibFactory verwendet.
	 * 
	 * @param nameSpace Name-Space der TagLib.
	 */
	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace.toLowerCase();
	}

	/**
	 * Fuegt der TagLib die die Definition des Name-Space-Seperator hinzu. Diese Methode wird durch die
	 * Klasse TagLibFactory verwendet.
	 * 
	 * @param nameSpaceSeperator Name-Space-Seperator der TagLib.
	 */
	public void setNameSpaceSeperator(String nameSpaceSeperator) {
		this.nameSpaceSeperator = nameSpaceSeperator;
	}

	/**
	 * @return Returns the displayName.
	 */
	public String getDisplayName() {
		if (displayName == null) return shortName;
		return displayName;
	}

	/**
	 * @param displayName The displayName to set.
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return Returns the shortName.
	 */
	public String getShortName() {
		return shortName;
	}

	/**
	 * @param shortName The shortName to set.
	 */
	public void setShortName(String shortName) {
		this.shortName = shortName;
		if (nameSpace == null) nameSpace = shortName.toLowerCase();
	}

	public void setIgnoreUnknowTags(boolean ignoreUnknowTags) {
		this.ignoreUnknowTags = ignoreUnknowTags;
	}

	public boolean getIgnoreUnknowTags() {
		return ignoreUnknowTags;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDisplayName() + ":" + getShortName() + ":" + super.toString();
	}

	public String getHash() {
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = tags.keySet().iterator();
		while (it.hasNext()) {
			// "__filename"

			sb.append((tags.get(it.next())).getHash() + "\n");
		}
		try {
			return Md5.getDigestAsString(sb.toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public boolean isCore() {
		return isCore;
	}

	public void setIsCore(boolean isCore) {
		this.isCore = isCore;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return duplicate(false);
	}

	/**
	 * duplicate the taglib, does not
	 * 
	 * @param deepCopy duplicate also the children (TagLibTag) of this TagLib
	 * @return clone of this taglib
	 */
	public TagLib duplicate(boolean deepCopy) {
		TagLib tl = new TagLib(isCore);
		tl.appendixTags = duplicate(this.appendixTags, deepCopy);
		tl.displayName = this.displayName;
		tl.ELClass = this.ELClass;
		tl.exprTransformer = this.exprTransformer;
		tl.isCore = this.isCore;
		tl.nameSpace = this.nameSpace;
		tl.nameSpaceAndNameSpaceSeperator = this.nameSpaceAndNameSpaceSeperator;
		tl.nameSpaceSeperator = this.nameSpaceSeperator;
		tl.shortName = this.shortName;
		tl.tags = duplicate(this.tags, deepCopy);
		tl.type = this.type;
		tl.source = this.source;
		tl.ignoreUnknowTags = this.ignoreUnknowTags;

		return tl;
	}

	/**
	 * duplcate a hashmap with TagLibTag's
	 * 
	 * @param tags
	 * @param deepCopy
	 * @return cloned map
	 */
	private HashMap<String, TagLibTag> duplicate(HashMap<String, TagLibTag> tags, boolean deepCopy) {
		if (deepCopy) throw new PageRuntimeException(new ExpressionException("deep copy not supported"));

		Iterator<Entry<String, TagLibTag>> it = tags.entrySet().iterator();
		HashMap<String, TagLibTag> cm = new HashMap<String, TagLibTag>();
		Entry<String, TagLibTag> entry;
		while (it.hasNext()) {
			entry = it.next();
			cm.put(entry.getKey(), deepCopy ? entry.getValue() : // TODO add support for deepcopy ((TagLibTag)entry.getValue()).duplicate(deepCopy):
					entry.getValue());
		}
		return cm;
	}

	public String getSource() {
		return source;
	}

	public URI getUri() {
		// TODO Auto-generated method stub
		return uri;
	}

	public void setUri(String strUri) throws URISyntaxException {
		this.uri = new URI(strUri);
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public TagLibTag[] getScriptTags() {
		if (scriptTags == null) {
			Iterator<TagLibTag> it = getTags().values().iterator();
			TagLibTag tag;
			TagLibTagScript script;
			List<TagLibTag> tags = new ArrayList<TagLibTag>();
			while (it.hasNext()) {
				tag = it.next();
				script = tag.getScript();
				if (script != null && script.getType() != TagLibTagScript.TYPE_NONE) {
					tags.add(tag);
					// print.o(tag.getName()+":"+tag.getScript().getType());
				}
			}
			scriptTags = tags.toArray(new TagLibTag[tags.size()]);
		}
		return scriptTags;
	}

}