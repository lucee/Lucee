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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.jsp.tagext.BodyContent;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.cache.tag.CacheHandler;
import lucee.runtime.cache.tag.CacheHandlerCollectionImpl;
import lucee.runtime.cache.tag.CacheHandlerPro;
import lucee.runtime.cache.tag.CacheItem;
import lucee.runtime.cache.tag.udf.UDFCacheItem;
import lucee.runtime.component.MemberSupport;
import lucee.runtime.config.Config;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.UDFCasterException;
import lucee.runtime.listener.ApplicationContextSupport;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentIntKey;
import lucee.runtime.type.scope.Local;
import lucee.runtime.type.scope.LocalImpl;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.runtime.writer.BodyContentUtil;

/**
 * defines an abstract class for a User defined Functions
 */
public class UDFImpl extends MemberSupport implements UDFPlus, Externalizable {

	private static final long serialVersionUID = -7288148349256615519L; // do not change

	protected Component ownerComponent;
	public UDFPropertiesBase properties;

	/**
	 * DO NOT USE THIS CONSTRUCTOR! this constructor is only for deserialize process
	 */
	public UDFImpl() {
		super(0);
	}

	public UDFImpl(UDFProperties properties) {
		super(properties.getAccess(), properties.getModifier());

		this.properties = (UDFPropertiesBase) properties;
	}

	public UDFImpl(UDFProperties properties, Component owner) {
		super(properties.getAccess(), properties.getModifier());
		this.properties = (UDFPropertiesBase) properties;
		setOwnerComponent(owner);
	}

	public UDF duplicate(Component cfc) {
		UDFImpl udf = new UDFImpl(properties);
		udf.ownerComponent = cfc;
		udf.setAccess(getAccess());
		return udf;
	}

	@Override
	public UDF duplicate(boolean deepCopy) {
		return duplicate(ownerComponent);
	}

	@Override
	public UDF duplicate() {
		return duplicate(ownerComponent);
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		return properties.getPage(pageContext).udfCall(pageContext, this, properties.getIndex());
	}

	private final Object castToAndClone(PageContext pc, FunctionArgument arg, Object value, int index) throws PageException {
		if (value == null && ((PageContextImpl) pc).getFullNullSupport()) return value;

		if (!((PageContextImpl) pc).getTypeChecking() || Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value))
			return arg.isPassByReference() ? value : Duplicator.duplicate(value, true);
		throw new UDFCasterException(this, arg, value, index);
	}

	private final Object castTo(PageContext pc, FunctionArgument arg, Object value, int index) throws PageException {
		if (Decision.isCastableTo(pc, arg.getType(), arg.getTypeAsString(), value)) return value;
		throw new UDFCasterException(this, arg, value, index);
	}

	private void defineArguments(PageContext pc, FunctionArgument[] funcArgs, Object[] args, Argument newArgs) throws PageException {
		// define argument scope
		boolean fns = NullSupportHelper.full(pc);
		Object _null = NullSupportHelper.NULL(fns);

		for (int i = 0; i < funcArgs.length; i++) {
			// argument defined
			if (args.length > i && (args[i] != null || fns)) {
				newArgs.setEL(funcArgs[i].getName(), castToAndClone(pc, funcArgs[i], args[i], i + 1));
			}
			// argument not defined
			else {
				Object d = getDefaultValue(pc, i, _null);
				if (d == _null) {
					if (funcArgs[i].isRequired()) {
						throw new ExpressionException("The parameter [" + funcArgs[i].getName() + "] to function [" + getFunctionName() + "] is required but was not passed in.");
					}
					if (!fns) newArgs.setEL(funcArgs[i].getName(), Argument.NULL);
				}
				else {
					newArgs.setEL(funcArgs[i].getName(), castTo(pc, funcArgs[i], d, i + 1));
				}
			}
		}
		for (int i = funcArgs.length; i < args.length; i++) {
			newArgs.setEL(ArgumentIntKey.init(i + 1), args[i]);
		}
	}

	private void defineArguments(PageContext pageContext, FunctionArgument[] funcArgs, Struct values, Argument newArgs) throws PageException {
		// argumentCollection
		UDFUtil.argumentCollection(values, funcArgs);
		// print.out(values.size());
		Object value;
		Collection.Key name;
		Object _null = NullSupportHelper.NULL(pageContext);

		for (int i = 0; i < funcArgs.length; i++) {
			// argument defined
			name = funcArgs[i].getName();
			value = values.remove(name, _null);
			if (value != _null) {
				newArgs.set(name, castToAndClone(pageContext, funcArgs[i], value, i + 1));
				continue;
			}
			value = values.remove(ArgumentIntKey.init(i + 1), _null);
			if (value != _null) {
				newArgs.set(name, castToAndClone(pageContext, funcArgs[i], value, i + 1));
				continue;
			}

			// default argument or exception
			Object defaultValue = getDefaultValue(pageContext, i, _null);// funcArgs[i].getDefaultValue();
			if (defaultValue == _null) {
				if (funcArgs[i].isRequired()) {
					throw new ExpressionException("The parameter [" + funcArgs[i].getName() + "] to function [" + getFunctionName() + "] is required but was not passed in.");
				}
				if (pageContext.getCurrentTemplateDialect() == CFMLEngine.DIALECT_CFML && !pageContext.getConfig().getFullNullSupport()) newArgs.set(name, Argument.NULL);
			}
			else newArgs.set(name, castTo(pageContext, funcArgs[i], defaultValue, i + 1));
		}

		Iterator<Entry<Key, Object>> it = values.entryIterator();
		Entry<Key, Object> e;
		while (it.hasNext()) {
			e = it.next();
			newArgs.set(e.getKey(), e.getValue());
		}
	}

	public static Collection.Key toKey(Object obj) {
		if (obj == null) return null;
		if (obj instanceof Collection.Key) return (Collection.Key) obj;
		String str = Caster.toString(obj, null);
		if (str == null) return KeyImpl.init(obj.toString());
		return KeyImpl.init(str);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Struct values, boolean doIncludePath) throws PageException {
		return hasCachedWithin(pc) ? _callCachedWithin(pc, null, null, values, doIncludePath) : _call(pc, null, null, values, doIncludePath);
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Collection.Key calledName, Struct values, boolean doIncludePath) throws PageException {
		return hasCachedWithin(pc) ? _callCachedWithin(pc, calledName, null, values, doIncludePath) : _call(pc, calledName, null, values, doIncludePath);
	}

	@Override
	public Object call(PageContext pc, Object[] args, boolean doIncludePath) throws PageException {
		return hasCachedWithin(pc) ? _callCachedWithin(pc, null, args, null, doIncludePath) : _call(pc, null, args, null, doIncludePath);
	}

	@Override
	public Object call(PageContext pc, Collection.Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		return hasCachedWithin(pc) ? _callCachedWithin(pc, calledName, args, null, doIncludePath) : _call(pc, calledName, args, null, doIncludePath);
	}

	private boolean hasCachedWithin(PageContext pc) {
		return this.properties.getCachedWithin() != null || pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION) != null;
		// Maybe better return !StringUtil.isEmpty(this.properties.cachedWithin) ||
		// !StringUtil.isEmpty(pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION));
	}

	private Object getCachedWithin(PageContext pc) {
		if (this.properties.getCachedWithin() != null) return this.properties.getCachedWithin();
		return pc.getCachedWithin(Config.CACHEDWITHIN_FUNCTION);
	}

	private Object _callCachedWithin(PageContext pc, Collection.Key calledName, Object[] args, Struct values, boolean doIncludePath) throws PageException {

		PageContextImpl pci = (PageContextImpl) pc;

		Object cachedWithin = getCachedWithin(pc);
		String cacheId = CacheHandlerCollectionImpl.createId(this, args, values);
		CacheHandler cacheHandler = pc.getConfig().getCacheHandlerCollection(Config.CACHE_TYPE_FUNCTION, null).getInstanceMatchingObject(getCachedWithin(pc), null);

		if (cacheHandler instanceof CacheHandlerPro) {
			CacheItem cacheItem = ((CacheHandlerPro) cacheHandler).get(pc, cacheId, cachedWithin);
			if (cacheItem instanceof UDFCacheItem) {
				UDFCacheItem entry = (UDFCacheItem) cacheItem;
				try {
					pc.write(entry.output);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
				return entry.returnValue;
			}
		}
		else if (cacheHandler != null) { // TODO this else block can be removed when all cache handlers implement CacheHandlerPro
			CacheItem cacheItem = cacheHandler.get(pc, cacheId);
			if (cacheItem instanceof UDFCacheItem) {
				UDFCacheItem entry = (UDFCacheItem) cacheItem;
				// if(entry.creationdate+properties.cachedWithin>=System.currentTimeMillis()) {
				try {
					pc.write(entry.output);
				}
				catch (IOException e) {
					throw Caster.toPageException(e);
				}
				return entry.returnValue;
				// }
				// cache.remove(id);
			}
		}

		// cached item not found, process and cache result if needed
		long start = System.nanoTime();

		// execute the function
		BodyContent bc = pci.pushBody();

		try {
			Object rtn = _call(pci, calledName, args, values, doIncludePath);

			if (cacheHandler != null) {
				String out = bc.getString();
				cacheHandler.set(pc, cacheId, cachedWithin, new UDFCacheItem(out, rtn, getFunctionName(), getSource(), System.nanoTime() - start));
			}
			return rtn;
		}
		finally {
			BodyContentUtil.flushAndPop(pc, bc);
		}
	}

	private Object _call(PageContext pc, Collection.Key calledName, Object[] args, Struct values, boolean doIncludePath) throws PageException {

		// print.out(count++);
		PageContextImpl pci = (PageContextImpl) pc;
		Argument newArgs = pci.getScopeFactory().getArgumentInstance();
		newArgs.setFunctionArgumentNames(properties.getArgumentsSet());
		LocalImpl newLocal = pci.getScopeFactory().getLocalInstance();

		Undefined undefined = pc.undefinedScope();
		Argument oldArgs = pc.argumentsScope();
		Local oldLocal = pc.localScope();
		Collection.Key oldCalledName = pci.getActiveUDFCalledName();

		pc.setFunctionScopes(newLocal, newArgs);
		pci.setActiveUDFCalledName(calledName);

		int oldCheckArgs = undefined.setMode(pc.getCurrentTemplateDialect() == CFMLEngine.DIALECT_CFML
				? (properties.getLocalMode() == null ? pc.getApplicationContext().getLocalMode() : properties.getLocalMode().intValue())
				: Undefined.MODE_LOCAL_OR_ARGUMENTS_ALWAYS);

		PageSource ps = null;
		PageSource psInc = null;
		try {
			ps = properties.getPageSource();
			if (doIncludePath) psInc = ps;
			if (doIncludePath && getOwnerComponent() != null) {
				psInc = ComponentUtil.getPageSource(getOwnerComponent());
				if (psInc == pci.getCurrentTemplatePageSource()) {
					psInc = null;
				}
			}
			if (ps != null) pci.addPageSource(ps, psInc);
			pci.addUDF(this);

			//////////////////////////////////////////
			BodyContent bc = null;
			Boolean wasSilent = null;
			boolean bufferOutput = getBufferOutput(pci);
			if (!getOutput()) {
				if (bufferOutput) bc = pci.pushBody();
				else wasSilent = pc.setSilent() ? Boolean.TRUE : Boolean.FALSE;
			}

			UDF parent = null;
			if (ownerComponent != null) {
				parent = pci.getActiveUDF();
				pci.setActiveUDF(this);
			}
			Object returnValue = null;

			try {

				if (args != null) defineArguments(pc, getFunctionArguments(), args, newArgs);
				else defineArguments(pc, getFunctionArguments(), values, newArgs);

				returnValue = implementation(pci);
				if (ownerComponent != null) pci.setActiveUDF(parent);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				if (ownerComponent != null) pci.setActiveUDF(parent);
				if (!getOutput()) {
					if (bufferOutput) BodyContentUtil.flushAndPop(pc, bc);
					else if (!wasSilent) pc.unsetSilent();
				}
				// BodyContentUtil.flushAndPop(pc,bc);
				throw Caster.toPageException(t);
			}
			if (!getOutput()) {
				if (bufferOutput) BodyContentUtil.clearAndPop(pc, bc);
				else if (!wasSilent) pc.unsetSilent();
			}
			// BodyContentUtil.clearAndPop(pc,bc);

			if (returnValue == null && ((PageContextImpl) pc).getFullNullSupport()) return returnValue;
			if (properties.getReturnType() == CFTypes.TYPE_ANY || !((PageContextImpl) pc).getTypeChecking()) return returnValue;
			if (Decision.isCastableTo(properties.getReturnTypeAsString(), returnValue, false, false, -1)) return returnValue;
			throw new UDFCasterException(this, properties.getReturnTypeAsString(), returnValue);

			// REALCAST return Caster.castTo(pageContext,returnType,returnValue,false);
			//////////////////////////////////////////

		}
		finally {
			if (ps != null) pc.removeLastPageSource(psInc != null);
			pci.removeUDF();
			pci.setFunctionScopes(oldLocal, oldArgs);
			pci.setActiveUDFCalledName(oldCalledName);
			undefined.setMode(oldCheckArgs);
			pci.getScopeFactory().recycle(pci, newArgs);
			pci.getScopeFactory().recycle(pci, newLocal);
		}
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return UDFUtil.toDumpData(pageContext, maxlevel, dp, this, UDFUtil.TYPE_UDF);
	}

	@Override
	public String getDisplayName() {
		return properties.getDisplayName();
	}

	@Override
	public String getHint() {
		return properties.getHint();
	}

	/*
	 * @Override public PageSource getPageSource() { return properties.pageSource; }
	 */

	@Override
	public String getSource() {
		return properties.getPageSource() != null ? properties.getPageSource().getDisplayPath() : "";
	}

	public Struct getMeta() {
		return properties.getMeta();
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		return ComponentUtil.getMetaData(pc, properties, null);
		// return getMetaData(pc, this);
	}

	@Override
	public Object getValue() {
		return this;
	}

	/**
	 * @param component the componentImpl to set
	 */
	@Override
	public void setOwnerComponent(Component component) {
		this.ownerComponent = component;
	}

	@Override
	public Component getOwnerComponent() {
		return ownerComponent;// +++
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(properties.getFunctionName());
		sb.append("(");
		int optCount = 0;
		FunctionArgument[] args = properties.getFunctionArguments();
		for (int i = 0; i < args.length; i++) {
			if (i > 0) sb.append(", ");
			if (!args[i].isRequired()) {
				sb.append("[");
				optCount++;
			}
			sb.append(args[i].getTypeAsString());
			sb.append(" ");
			sb.append(args[i].getName());
		}
		for (int i = 0; i < optCount; i++) {
			sb.append("]");
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Boolean getSecureJson() {
		return properties.getSecureJson();
	}

	@Override
	public Boolean getVerifyClient() {
		return properties.getVerifyClient();
	}

	@Override
	public Object clone() {
		return duplicate();
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		return properties.getFunctionArguments();
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		return getDefaultValue(pc, index, null);
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		return properties.getPage(pc).udfDefaultValue(pc, properties.getIndex(), index, defaultValue);
	}

	// public abstract Object getDefaultValue(PageContext pc,int index) throws PageException;

	@Override
	public String getFunctionName() {
		return properties.getFunctionName();
	}

	@Override
	public boolean getOutput() {
		return properties.getOutput();
	}

	public Boolean getBufferOutput() {
		return properties.getBufferOutput();
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		if (properties.getBufferOutput() != null) return properties.getBufferOutput().booleanValue();
		return ((ApplicationContextSupport) pc.getApplicationContext()).getBufferOutput();
	}

	@Override
	public int getReturnType() {
		return properties.getReturnType();
	}

	@Override
	public String getReturnTypeAsString() {
		return properties.getReturnTypeAsString();
	}

	@Override
	public String getDescription() {
		return properties.getDescription();
	}

	@Override
	public int getReturnFormat() {
		if (properties.getReturnFormat() < 0) return UDF.RETURN_FORMAT_WDDX;
		return properties.getReturnFormat();
	}

	@Override
	public int getReturnFormat(int defaultValue) {
		if (properties.getReturnFormat() < 0) return defaultValue;
		return properties.getReturnFormat();
	}

	public final String getReturnFormatAsString() {
		return properties.getReturnFormatAsString();
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// access
		setAccess(in.readInt());

		// properties
		properties = (UDFPropertiesBase) in.readObject();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// access
		out.writeInt(getAccess());

		// properties
		out.writeObject(properties);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof UDF)) return false;
		return equals(this, (UDF) obj);
	}

	public static boolean equals(UDF left, UDF right) {
		// print.e(left.getFunctionName()+":"+right.getFunctionName());
		if (!left.id().equals(right.id()) || !_eq(left.getFunctionName(), right.getFunctionName()) || left.getAccess() != right.getAccess()
				|| !_eq(left.getFunctionName(), right.getFunctionName()) || left.getOutput() != right.getOutput() || left.getReturnFormat() != right.getReturnFormat()
				|| left.getReturnType() != right.getReturnType() || !_eq(left.getReturnTypeAsString(), right.getReturnTypeAsString())
				|| !_eq(left.getSecureJson(), right.getSecureJson()) || !_eq(left.getVerifyClient(), right.getVerifyClient()))
			return false;

		// Arguments
		FunctionArgument[] largs = left.getFunctionArguments();
		FunctionArgument[] rargs = right.getFunctionArguments();
		if (largs.length != rargs.length) return false;
		for (int i = 0; i < largs.length; i++) {
			if (!largs[i].equals(rargs[i])) return false;
		}

		return true;
	}

	private static boolean _eq(Object left, Object right) {
		if (left == null) return right == null;
		return left.equals(right);
	}

	@Override
	public int getIndex() {
		return properties.getIndex();
	}

	@Override
	public String id() {
		return properties.id();
	}

	@Override
	public PageSource getPageSource() {
		return this.properties.getPageSource();
	}

}
