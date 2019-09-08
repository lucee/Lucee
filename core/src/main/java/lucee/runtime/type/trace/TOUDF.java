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
package lucee.runtime.type.trace;

import lucee.runtime.Component;
import lucee.runtime.ComponentImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.debug.Debugger;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFPlus;
import lucee.runtime.type.util.ComponentUtil;

public class TOUDF extends TOObjects implements UDF {

	private UDF udf;

	protected TOUDF(Debugger debugger, UDF udf, int type, String category, String text) {
		super(debugger, udf, type, category, text);
		this.udf = udf;
	}

	@Override
	public int getModifier() {
		log(null);
		return udf.getModifier();
	}

	@Override
	public int getAccess() {
		log(null);
		return udf.getAccess();
	}

	public void setAccess(int access) {
		log(ComponentUtil.toStringAccess(access, null));
		if (udf instanceof UDFPlus) ((UDFPlus) udf).setAccess(access);
	}

	@Override
	public Object getValue() {
		log(null);
		return udf.getValue();
	}

	@Override
	public Object implementation(PageContext pageContext) throws Throwable {
		log(null);
		return udf.implementation(pageContext);
	}

	@Override
	public FunctionArgument[] getFunctionArguments() {
		log(null);
		return udf.getFunctionArguments();
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index) throws PageException {
		log(null);
		return udf.getDefaultValue(pc, index);
	}

	@Override
	public Object getDefaultValue(PageContext pc, int index, Object defaultValue) throws PageException {
		log(null);
		return udf.getDefaultValue(pc, index, defaultValue);
	}

	@Override
	public String getFunctionName() {
		log(null);
		return udf.getFunctionName();
	}

	@Override
	public boolean getOutput() {
		log(null);
		return udf.getOutput();
	}

	@Override
	public int getReturnType() {
		log(null);
		return udf.getReturnType();
	}

	@Override
	public String id() {
		log(null);
		return udf.id();
	}

	@Override
	public int getReturnFormat() {
		log(null);
		return udf.getReturnFormat();
	}

	@Override
	public int getReturnFormat(int defaultValue) {
		log(null);
		return udf.getReturnFormat(defaultValue);
	}

	@Override
	public Boolean getSecureJson() {
		log(null);
		return udf.getSecureJson();
	}

	@Override
	public Boolean getVerifyClient() {
		log(null);
		return udf.getVerifyClient();
	}

	@Override
	public String getReturnTypeAsString() {
		log(null);
		return udf.getReturnTypeAsString();
	}

	@Override
	public String getDescription() {
		log(null);
		return udf.getDescription();
	}

	@Override
	public Object callWithNamedValues(PageContext pageContext, Struct values, boolean doIncludePath) throws PageException {
		log(null);
		return udf.callWithNamedValues(pageContext, values, doIncludePath);
	}

	@Override
	public Object callWithNamedValues(PageContext pageContext, Collection.Key calledName, Struct values, boolean doIncludePath) throws PageException {
		log(null);
		return udf.callWithNamedValues(pageContext, calledName, values, doIncludePath);
	}

	@Override
	public Object call(PageContext pageContext, Object[] args, boolean doIncludePath) throws PageException {
		log(null);
		return udf.call(pageContext, args, doIncludePath);
	}

	@Override
	public Object call(PageContext pageContext, Collection.Key calledName, Object[] args, boolean doIncludePath) throws PageException {
		log(null);
		return udf.call(pageContext, calledName, args, doIncludePath);
	}

	@Override
	public String getDisplayName() {
		log(null);
		return udf.getDisplayName();
	}

	@Override
	public String getHint() {
		log(null);
		return udf.getHint();
	}

	/*
	 * @Override public PageSource getPageSource() { log(null); return udf.getPageSource(); }
	 */

	@Override
	public boolean equals(Object other) {
		return udf.equals(other);
	}

	@Override
	public String getSource() {
		log(null);
		return udf.getSource();
	}

	@Override
	public int getIndex() {
		log(null);
		return udf.getIndex();
	}

	@Override
	public Struct getMetaData(PageContext pc) throws PageException {
		log(null);
		return udf.getMetaData(pc);
	}

	@Override
	public UDF duplicate() {
		log(null);
		return udf.duplicate();
	}

	@Override
	public boolean getBufferOutput(PageContext pc) {
		log(pc);
		return udf.getBufferOutput(pc);
	}

	@Override
	public Component getOwnerComponent() {
		log(null);
		return udf.getOwnerComponent();
	}

	public void setOwnerComponent(ComponentImpl cfc) {
		log(null);
		if (udf instanceof UDFPlus) ((UDFPlus) udf).setOwnerComponent(cfc);
	}

	@Override
	public PageSource getPageSource() {
		return udf.getPageSource();
	}
}