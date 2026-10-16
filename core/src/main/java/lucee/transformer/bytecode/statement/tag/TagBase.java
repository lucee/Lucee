/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
package lucee.transformer.bytecode.statement.tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.statement.FlowControlFinal;
import lucee.transformer.bytecode.statement.StatementBase;
import lucee.transformer.bytecode.visitor.ParseBodyVisitor;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.statement.tag.Attribute;
import lucee.transformer.statement.tag.Tag;

public abstract class TagBase extends StatementBase implements Tag {

	private Body body = null;
	private String appendix;
	private String fullname;
	private TagLibTag tagLibTag;
	Map<String, Attribute> attributes = new LinkedHashMap<String, Attribute>();
	// Map<String,String> missingAttributes=new HashMap<String,String>();
	HashSet<TagLibTagAttr> missingAttributes = new HashSet<TagLibTagAttr>();
	private boolean scriptBase = false;

	private Map<String, Attribute> metadata;
	// private Label finallyLabel;

	public TagBase(Factory factory, Position start, Position end) {
		super(factory, start, end);
	}

	/**
	 * @see lucee.transformer.statement.tag.Tag#getAppendix()
	 */
	@Override
	public String getAppendix() {
		return appendix;
	}

	@Override
	public Map<String, Attribute> getAttributes() {
		return attributes;
	}

	@Override
	public String getFullname() {
		return fullname;
	}

	@Override
	public TagLibTag getTagLibTag() {
		return tagLibTag;
	}

	@Override
	public void setAppendix(String appendix) {
		this.appendix = appendix;
	}

	@Override
	public void setFullname(String fullname) {
		this.fullname = fullname;
	}

	@Override
	public void setTagLibTag(TagLibTag tagLibTag) {
		this.tagLibTag = tagLibTag;
	}

	@Override
	public void addAttribute(Attribute attribute) {
		attributes.put(attribute.getName().toLowerCase(), attribute);
	}

	@Override
	public boolean containsAttribute(String name) {
		return attributes.containsKey(name.toLowerCase());
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public void setBody(Body body) {
		this.body = body;
		body.setParent(this);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		_writeOut(bc, true, null);
	}

	public void _writeOut(BytecodeContext bc, boolean doReuse) throws TransformerException {
		_writeOut(bc, doReuse, null);
	}

	protected void _writeOut(BytecodeContext bc, boolean doReuse, final FlowControlFinal fcf) throws TransformerException {
		// _writeOut(bc, true);
		boolean output = tagLibTag.getParseBody() || Caster.toBooleanValue(getAttribute("output"), false);

		if (output) {
			ParseBodyVisitor pbv = new ParseBodyVisitor();
			pbv.visitBegin(bc);
			TagHelper.writeOut(this, bc, doReuse, fcf);
			pbv.visitEnd(bc);
		}
		else TagHelper.writeOut(this, bc, doReuse, fcf);
	}

	@Override
	public Attribute getAttribute(String name) {
		return attributes.get(name.toLowerCase());
	}

	@Override
	public Attribute removeAttribute(String name) {
		return attributes.remove(name);
	}

	@Override
	public String toString() {
		return appendix + ":" + fullname + ":" + super.toString();
	}

	@Override
	public boolean isScriptBase() {
		return scriptBase;
	}

	@Override
	public void setScriptBase(boolean scriptBase) {
		this.scriptBase = scriptBase;
	}

	@Override
	public void addMissingAttribute(TagLibTagAttr attr) {
		missingAttributes.add(attr);
	}

	@Override
	public TagLibTagAttr[] getMissingAttributes() {

		return missingAttributes.toArray(new TagLibTagAttr[missingAttributes.size()]);
	}

	@Override
	public void addMetaData(Attribute metadata) {
		if (this.metadata == null) this.metadata = new HashMap<String, Attribute>();
		this.metadata.put(metadata.getName(), metadata);
	}

	@Override
	public Map<String, Attribute> getMetaData() {
		return metadata;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "CFMLTag");

		// Check if this is a built-in tag
		// Custom tags have name="_" with an appendix (cf_tagname) or custom namespace
		boolean isCustomTag = ("_".equals(tagLibTag.getName()) && appendix != null) ||
		                      !"cf".equals(tagLibTag.getTagLib().getNameSpace());

		if (!isCustomTag && tagLibTag != null && tagLibTag.getTagLib() != null && tagLibTag.getTagLib().isCore()) {
			sct.setEL("isBuiltIn", Boolean.TRUE);
		}

		sct.setEL(KeyConstants._name, tagLibTag.getName());
		sct.setEL(KeyConstants._nameSpace, tagLibTag.getTagLib().getNameSpace());
		sct.setEL(KeyConstants._nameSpaceSeparator, tagLibTag.getTagLib().getNameSpaceSeparator());
		if (appendix != null) sct.setEL(KeyConstants._appendix, appendix);
		if (fullname != null) sct.setEL(KeyConstants._fullname, fullname);

		// attributes
		Array arrAttrs = new ArrayImpl(8, false);
		sct.setEL(KeyConstants._attributes, arrAttrs);
		for (Entry<String, Attribute> entry: attributes.entrySet()) {
			Attribute attr = entry.getValue();
			Struct sctAttr = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			arrAttrs.appendEL(sctAttr);
			sctAttr.setEL(KeyConstants._name, attr.getName());
			sctAttr.setEL(KeyConstants._type, "Attribute");

			Struct val = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			attr.getValue().dump(val);
			sctAttr.setEL(KeyConstants._value, val);
		}
		// body
		if (this.body != null) {
			Struct body = new StructImpl(StructImpl.TYPE_LINKED_NOT_SYNC, 8);
			this.body.dump(body);
			sct.setEL(KeyConstants._body, body);
		}
	}
}