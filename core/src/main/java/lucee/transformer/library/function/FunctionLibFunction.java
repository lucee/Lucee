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
package lucee.transformer.library.function;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.osgi.framework.Version;
import org.xml.sax.Attributes;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.Identification;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.functions.BIFProxy;
import lucee.runtime.op.Caster;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.cfml.evaluator.FunctionEvaluator;
import lucee.transformer.library.ClassDefinitionImpl;
import lucee.transformer.library.tag.TagLib;

/**
 * Eine FunctionLibFunction repraesentiert eine einzelne Funktion innerhalb einer FLD.
 */
public final class FunctionLibFunction {

	/**
	 * Dynamischer Argument Typ
	 */
	public static final int ARG_DYNAMIC = 0;
	/**
	 * statischer Argument Typ
	 */
	public static final int ARG_FIX = 1;

	private FunctionLib functionLib;
	private String name;
	private ArrayList<FunctionLibFunctionArg> argument = new ArrayList<FunctionLibFunctionArg>();

	private int argMin = 0;
	private int argMax = -1;
	private int argType = ARG_FIX;
	private String strReturnType;
	private ClassDefinition clazz;
	private String description;
	private boolean hasDefaultValues;
	private FunctionEvaluator eval;
	private ClassDefinition tteCD;
	private short status = TagLib.STATUS_IMPLEMENTED;
	private String[] memberNames;
	private int memberPosition = 1;
	private short memberType = CFTypes.TYPE_UNKNOW;
	private boolean memberChaining;
	private BIF bif;
	private String[] keywords;
	private ClassDefinition functionCD;
	private Version introduced;
	private final boolean core;

	/**
	 * Geschuetzer Konstruktor ohne Argumente.
	 */
	/*
	 * public FunctionLibFunction() { this.core=false; }
	 */

	public FunctionLibFunction(boolean core) {
		this.core = core;
	}

	public FunctionLibFunction(FunctionLib functionLib, boolean core) {
		this.functionLib = functionLib;
		this.core = core;
	}

	/**
	 * Gibt den Namen der Funktion zurueck.
	 * 
	 * @return name Name der Funktion.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gibt alle Argumente einer Funktion als ArrayList zurueck.
	 * 
	 * @return Argumente der Funktion.
	 */
	public ArrayList<FunctionLibFunctionArg> getArg() {
		return argument;
	}

	/**
	 * Gibt zurueck wieviele Argumente eine Funktion minimal haben muss.
	 * 
	 * @return Minimale Anzahl Argumente der Funktion.
	 */
	public int getArgMin() {
		return argMin;
	}

	/**
	 * Gibt zurueck wieviele Argumente eine Funktion minimal haben muss.
	 * 
	 * @return Maximale Anzahl Argumente der Funktion.
	 */
	public int getArgMax() {
		return argMax;
	}

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
	 * Gibt die argument art zurueck.
	 * 
	 * @return argument art
	 */
	public int getArgType() {
		return argType;
	}

	/**
	 * Gibt die argument art als String zurueck.
	 * 
	 * @return argument art
	 */
	public String getArgTypeAsString() {
		if (argType == ARG_DYNAMIC) return "dynamic";
		return "fixed";
	}

	/**
	 * Gibt zurueck von welchem Typ der Rueckgabewert dieser Funktion sein muss (query, string, struct,
	 * number usw.).
	 * 
	 * @return Typ des Rueckgabewert.
	 */
	public String getReturnTypeAsString() {
		return strReturnType;
	}

	/**
	 * Gibt die Klasse zurueck, welche diese Funktion implementiert.
	 * 
	 * @return Klasse der Function.
	 * @throws ClassException
	 */
	public ClassDefinition getFunctionClassDefinition() {
		return functionCD;
	}

	/**
	 * Gibt die Beschreibung der Funktion zurueck.
	 * 
	 * @return String
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gibt die FunctionLib zurueck, zu der die Funktion gehoert.
	 * 
	 * @return Zugehoerige FunctionLib.
	 */
	public FunctionLib getFunctionLib() {
		return functionLib;
	}

	/**
	 * Setzt den Namen der Funktion.
	 * 
	 * @param name Name der Funktion.
	 */
	public void setName(String name) {
		this.name = name.toLowerCase();
	}

	/**
	 * Fuegt der Funktion ein Argument hinzu.
	 * 
	 * @param arg Argument zur Funktion.
	 */
	public void addArg(FunctionLibFunctionArg arg) {
		arg.setFunction(this);
		argument.add(arg);
		if (arg.getDefaultValue() != null) hasDefaultValues = true;
	}

	/**
	 * Fuegt der Funktion ein Argument hinzu, alias fuer addArg.
	 * 
	 * @param arg Argument zur Funktion.
	 */
	public void setArg(FunctionLibFunctionArg arg) {
		addArg(arg);
	}

	/**
	 * Setzt wieviele Argumente eine Funktion minimal haben muss.
	 * 
	 * @param argMin Minimale Anzahl Argumente der Funktion.
	 */
	public void setArgMin(int argMin) {
		this.argMin = argMin;
	}

	/**
	 * Setzt wieviele Argumente eine Funktion minimal haben muss.
	 * 
	 * @param argMax Maximale Anzahl Argumente der Funktion.
	 */
	public void setArgMax(int argMax) {
		this.argMax = argMax;
	}

	/**
	 * Setzt den Rueckgabewert der Funktion (query,array,string usw.)
	 * 
	 * @param value
	 */
	public void setReturn(String value) {
		strReturnType = value;
	}

	/**
	 * Setzt die Klassendefinition als Zeichenkette, welche diese Funktion implementiert.
	 * 
	 * @param value Klassendefinition als Zeichenkette.
	 */
	public void setFunctionClass(String value, Identification id, Attributes attrs) {
		functionCD = ClassDefinitionImpl.toClassDefinition(value, id, attrs);
	}

	public void setFunctionClass(ClassDefinition cd) {
		functionCD = cd;
	}

	/**
	 * Setzt die Beschreibung der Funktion.
	 * 
	 * @param description Beschreibung der Funktion.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Setzt die zugehoerige FunctionLib.
	 * 
	 * @param functionLib Zugehoerige FunctionLib.
	 */
	public void setFunctionLib(FunctionLib functionLib) {
		this.functionLib = functionLib;
	}

	/**
	 * sets the argument type of the function
	 * 
	 * @param argType
	 */
	public void setArgType(int argType) {
		this.argType = argType;
	}

	public String getHash() {

		StringBuilder sb = new StringBuilder();
		sb.append(this.getArgMax());
		sb.append(this.getArgMin());
		sb.append(this.getArgType());
		sb.append(this.getArgTypeAsString());
		sb.append(getFunctionClassDefinition().toString());
		sb.append(tteCD);
		sb.append(this.getName());
		sb.append(this.getReturnTypeAsString());

		Iterator<FunctionLibFunctionArg> it = this.getArg().iterator();
		FunctionLibFunctionArg arg;
		while (it.hasNext()) {
			arg = it.next();
			sb.append(arg.getHash());
		}

		try {
			return Md5.getDigestAsString(sb.toString());
		}
		catch (IOException e) {
			return "";
		}
	}

	public boolean hasDefaultValues() {
		return hasDefaultValues;
	}

	public boolean hasTteClass() {
		return tteCD != null;
	}

	public FunctionEvaluator getEvaluator() throws TemplateException {
		if (!hasTteClass()) return null;
		if (eval != null) return eval;
		try {
			eval = (FunctionEvaluator) tteCD.getClazz().newInstance();
		}
		catch (Exception e) {
			throw new TemplateException(e.getMessage());
		}
		return eval;
	}

	public void setTTEClass(String tteClass, Identification id, Attributes attrs) {
		this.tteCD = ClassDefinitionImpl.toClassDefinition(tteClass, id, attrs);
	}

	public void setMemberName(String memberNames) {
		if (StringUtil.isEmpty(memberNames, true)) return;
		this.memberNames = ListUtil.trimItems(ListUtil.listToStringArray(memberNames, ','));
	}

	public String[] getMemberNames() {
		return memberNames;
	}

	public void setKeywords(String keywords) {
		this.keywords = ListUtil.trimItems(ListUtil.listToStringArray(keywords, ','));
	}

	public String[] getKeywords() {
		return keywords;
	}

	public boolean isCore() {
		return core;
	}

	public void setMemberPosition(int pos) {
		this.memberPosition = pos;
	}

	public int getMemberPosition() {
		return memberPosition;
	}

	public void setMemberChaining(boolean memberChaining) {
		this.memberChaining = memberChaining;
	}

	public boolean getMemberChaining() {
		return memberChaining;
	}

	public void setMemberType(String memberType) {
		this.memberType = CFTypes.toShortStrict(memberType, CFTypes.TYPE_UNKNOW);
	}

	public short getMemberType() {
		if (memberNames != null && memberType == CFTypes.TYPE_UNKNOW) {
			ArrayList<FunctionLibFunctionArg> args = getArg();
			if (args.size() >= 1) {
				memberType = CFTypes.toShortStrict(args.get(getMemberPosition() - 1).getTypeAsString(), CFTypes.TYPE_UNKNOW);
			}
		}
		return memberType;
	}

	public String getMemberTypeAsString() {
		return CFTypes.toString(getMemberType(), "any");
	}

	public BIF getBIF() {
		if (bif != null) return bif;

		Class clazz = null;
		try {
			clazz = getFunctionClassDefinition().getClazz();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new PageRuntimeException(Caster.toPageException(t));
		}

		if (Reflector.isInstaneOf(clazz, BIF.class, false)) {
			try {
				bif = (BIF) ClassUtil.newInstance(clazz);
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw new RuntimeException(t);
			}
		}
		else {
			return new BIFProxy(clazz);
		}
		return bif;
	}

	public void setIntroduced(String introduced) {
		this.introduced = OSGiUtil.toVersion(introduced, null);
	}

	public Version getIntroduced() {
		return introduced;
	}
}