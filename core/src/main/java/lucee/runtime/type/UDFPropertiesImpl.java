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
package lucee.runtime.type;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ExternalizableUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.Mapping;
import lucee.runtime.MappingImpl;
import lucee.runtime.MappingImpl.SerMapping;
import lucee.runtime.Page;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.PageSourceImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebPro;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.UDFUtil;

public final class UDFPropertiesImpl extends UDFPropertiesBase {
	private static final long serialVersionUID = 8679484452640746605L; // do not change

	public String functionName;
	public int returnType;
	public String strReturnType;
	public boolean output;
	public Boolean bufferOutput;
	public String hint;
	public String displayName;
	public int index;
	public FunctionArgument[] arguments;
	public Struct meta;
	public String description;
	public Boolean secureJson;
	public Boolean verifyClient;
	public String strReturnFormat;
	public int returnFormat;
	public Set<Collection.Key> argumentsSet;
	public int access;
	public Object cachedWithin;
	public Integer localMode;
	public int modifier;

	/**
	 * NEVER USE THIS CONSTRUCTOR, this constructor is only for deserialize this object from stream
	 */
	public UDFPropertiesImpl() {

	}

	public UDFPropertiesImpl(Page page, PageSource pageSource, int startLine, int endLine, FunctionArgument[] arguments, int index, String functionName, String strReturnType,
			String strReturnFormat, boolean output, int access, Boolean bufferOutput, String displayName, String description, String hint, Boolean secureJson, Boolean verifyClient,
			Object cachedWithin, Integer localMode, int modifier, StructImpl meta) {
		this(page, pageSource, startLine, endLine, arguments, index, functionName, CFTypes.toShortStrict(strReturnType, CFTypes.TYPE_UNKNOW), strReturnType, strReturnFormat,
				output, access, bufferOutput, displayName, description, hint, secureJson, verifyClient, cachedWithin, localMode, modifier, meta);
	}

	public UDFPropertiesImpl(Page page, PageSource pageSource, int startLine, int endLine, FunctionArgument[] arguments, int index, String functionName, short returnType,
			String strReturnFormat, boolean output, int access, Boolean bufferOutput, String displayName, String description, String hint, Boolean secureJson, Boolean verifyClient,
			Object cachedWithin, Integer localMode, int modifier, StructImpl meta) {
		this(page, pageSource, startLine, endLine, arguments, index, functionName, returnType, CFTypes.toString(returnType, "any"), strReturnFormat, output, access, bufferOutput,
				displayName, description, hint, secureJson, verifyClient, cachedWithin, localMode, modifier, meta);
	}

	public UDFPropertiesImpl(Page page, PageSource pageSource, int startLine, int endLine, FunctionArgument[] arguments, int index, String functionName, short returnType,
			String strReturnFormat, boolean output, int access) {
		this(page, pageSource, startLine, endLine, arguments, index, functionName, returnType, CFTypes.toString(returnType, "any"), strReturnFormat, output, access, null, "", "",
				"", null, null, null, null, Component.MODIFIER_NONE, null);
	}

	private UDFPropertiesImpl(Page page, PageSource pageSource, int startLine, int endLine, FunctionArgument[] arguments, int index, String functionName, short returnType,
			String strReturnType, String strReturnFormat, boolean output, int access, Boolean bufferOutput, String displayName, String description, String hint, Boolean secureJson,
			Boolean verifyClient, Object cachedWithin, Integer localMode, int modifier, StructImpl meta) {
		super(page, pageSource, startLine, endLine);
		// this happens when an active is based on older source code

		if (arguments.length > 0) {
			this.argumentsSet = new HashSet<Collection.Key>();
			for (int i = 0; i < arguments.length; i++) {
				argumentsSet.add(arguments[i].getName());
			}
		}
		else this.argumentsSet = null;

		this.arguments = arguments;
		this.description = description;
		this.displayName = displayName;
		this.functionName = functionName;
		this.hint = hint;
		this.index = index;
		this.meta = meta;
		this.output = output;
		this.bufferOutput = bufferOutput;

		this.strReturnType = strReturnType;
		this.returnType = returnType;

		this.strReturnFormat = strReturnFormat;
		this.returnFormat = UDFUtil.toReturnFormat(strReturnFormat, -1);

		this.secureJson = secureJson;
		this.verifyClient = verifyClient;
		this.access = access;
		this.cachedWithin = cachedWithin instanceof Long ? TimeSpanImpl.fromMillis(((Long) cachedWithin).longValue()) : cachedWithin;
		this.localMode = localMode;
		this.modifier = modifier;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public int getModifier() {
		return modifier;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			SerMapping sm = (SerMapping) in.readObject();
			Mapping mapping = sm == null ? null : sm.toMapping();
			String relPath = ExternalizableUtil.readString(in);
			String relPathwV = ExternalizableUtil.readString(in);

			PageContextImpl pc = (PageContextImpl) ThreadLocalPageContext.get();
			ConfigWebPro cw = (ConfigWebPro) ThreadLocalPageContext.getConfig(pc);

			ps = toPageSource(pc, cw, mapping, relPath, relPathwV);
		}
		catch (Exception e) {
			throw ExceptionUtil.toIOException(e);
		}
		startLine = in.readInt();
		endLine = in.readInt();

		arguments = (FunctionArgument[]) in.readObject();
		access = in.readInt();
		index = in.readInt();
		returnFormat = in.readInt();
		returnType = in.readInt();
		description = ExternalizableUtil.readString(in);
		displayName = ExternalizableUtil.readString(in);
		functionName = ExternalizableUtil.readString(in);
		hint = ExternalizableUtil.readString(in);
		meta = (Struct) in.readObject();
		output = in.readBoolean();
		bufferOutput = ExternalizableUtil.readBoolean(in);
		secureJson = ExternalizableUtil.readBoolean(in);
		strReturnFormat = ExternalizableUtil.readString(in);
		strReturnType = ExternalizableUtil.readString(in);
		verifyClient = ExternalizableUtil.readBoolean(in);
		cachedWithin = StringUtil.emptyAsNull(ExternalizableUtil.readString(in), true);
		int tmp = in.readInt();
		localMode = tmp == -1 ? null : tmp;

		if (arguments != null && arguments.length > 0) {
			this.argumentsSet = new HashSet<Collection.Key>();
			for (int i = 0; i < arguments.length; i++) {
				argumentsSet.add(arguments[i].getName());
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		Mapping m = getPageSource().getMapping();
		Config c = m.getConfig();
		SerMapping sm = null;
		if (c instanceof ConfigWebPro) {
			ConfigWebPro cwi = (ConfigWebPro) c;
			if (m instanceof MappingImpl && cwi.isApplicationMapping(m)) {
				sm = ((MappingImpl) m).toSerMapping();
			}
		}
		out.writeObject(sm);
		out.writeObject(getPageSource().getRealpath());
		out.writeObject(getPageSource().getRealpathWithVirtual());
		out.writeInt(getStartLine());
		out.writeInt(getEndLine());
		out.writeObject(arguments);
		out.writeInt(access);
		out.writeInt(index);
		out.writeInt(returnFormat);
		out.writeInt(returnType);
		ExternalizableUtil.writeString(out, description);
		ExternalizableUtil.writeString(out, displayName);
		ExternalizableUtil.writeString(out, functionName);
		ExternalizableUtil.writeString(out, hint);
		out.writeObject(meta);
		out.writeBoolean(output);
		ExternalizableUtil.writeBoolean(out, bufferOutput);
		ExternalizableUtil.writeBoolean(out, secureJson);
		ExternalizableUtil.writeString(out, strReturnFormat);
		ExternalizableUtil.writeString(out, strReturnType);
		ExternalizableUtil.writeBoolean(out, verifyClient);

		ExternalizableUtil.writeString(out, Caster.toString(cachedWithin, null));

		out.writeInt(localMode == null ? -1 : localMode.intValue());
	}

	@Override
	public String getFunctionName() {
		return functionName;
	}

	@Override
	public boolean getOutput() {
		return output;
	}

	@Override
	public Boolean getBufferOutput() {
		return bufferOutput;
	}

	@Override
	public int getReturnType() {
		return returnType;
	}

	@Override
	public String getReturnTypeAsString() {
		return strReturnType;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getReturnFormat() {
		return returnFormat;
	}

	@Override
	public String getReturnFormatAsString() {
		return strReturnFormat;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Object getCachedWithin() {
		return cachedWithin;
	}

	@Override
	public Boolean getSecureJson() {
		return secureJson;
	}

	@Override
	public Boolean getVerifyClient() {
		return verifyClient;
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		return arguments;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String getHint() {
		return hint;
	}

	@Override
	public Struct getMeta() {
		return meta;
	}

	@Override
	public Integer getLocalMode() {
		return localMode;
	}

	@Override
	public Set<Key> getArgumentsSet() {
		return argumentsSet;
	}

	public static PageSource toPageSource(PageContextImpl pc, ConfigPro config, Mapping mapping, String relPath, String relPathwV) throws PageException {
		if (mapping != null) return mapping.getPageSource(relPath);

		PageSource ps = PageSourceImpl.best(config.getPageSources(pc, null, relPathwV, false, true, true));
		if (ps != null) return ps;

		throw new ApplicationException("File [" + relPath + "] not found");
	}
}