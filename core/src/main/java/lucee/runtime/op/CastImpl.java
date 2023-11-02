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
package lucee.runtime.op;

import java.awt.Color;
import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.color.ColorCaster;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.security.Credentials;
import lucee.commons.security.CredentialsImpl;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.dt.TimeSpanImpl;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.util.Cast;

/**
 * Implementation of the cast interface
 */
public final class CastImpl implements Cast {

	private static CastImpl singelton;

	@Override
	public Object castTo(PageContext pc, short type, Object o) throws PageException {
		return Caster.castTo(pc, type, o);
	}

	@Override
	public Object castTo(PageContext pc, short type, String strType, Object o) throws PageException {
		return Caster.castTo(pc, type, strType, o);
	}

	@Override
	public Object castTo(PageContext pc, Class trgClass, Object obj) throws PageException {
		return Caster.castTo(pc, trgClass, obj);
	}

	@Override
	public Object castTo(PageContext pc, String type, Object o) throws PageException {
		return Caster.castTo(pc, type, o, false);
	}

	@Override
	public Object castTo(PageContext pc, String type, Object o, boolean alsoPattern) throws PageException {
		return Caster.castTo(pc, type, o, alsoPattern);
	}

	@Override
	public Array toArray(Object obj, Array defaultValue) {
		return Caster.toArray(obj, defaultValue);
	}

	@Override
	public Array toArray(Object obj) throws PageException {
		return Caster.toArray(obj);
	}

	@Override
	public Object[] toNativeArray(Object obj) throws PageException {
		return Caster.toNativeArray(obj);
	}

	@Override
	public Object[] toNativeArray(Object obj, Object[] defaultValue) {
		return Caster.toNativeArray(obj, defaultValue);
	}

	@Override
	public String toBase64(Object o, String defaultValue) {
		return Caster.toBase64(o, null, defaultValue);
	}

	@Override
	public String toBase64(Object o) throws PageException {
		return Caster.toBase64(o, null);
	}

	@Override
	public byte[] toBinary(Object obj, byte[] defaultValue) {
		return Caster.toBinary(obj, defaultValue);
	}

	@Override
	public byte[] toBinary(Object obj) throws PageException {
		return Caster.toBinary(obj);
	}

	@Override
	public Boolean toBoolean(boolean b) {
		return Caster.toBoolean(b);
	}

	@Override
	public Boolean toBoolean(char c) {
		return Caster.toBoolean(c);
	}

	@Override
	public Boolean toBoolean(double d) {
		return Caster.toBoolean(d);
	}

	@Override
	public Boolean toBoolean(Object o, Boolean defaultValue) {
		return Caster.toBoolean(o, defaultValue);
	}

	@Override
	public Boolean toBoolean(Object o) throws PageException {
		return Caster.toBoolean(o);
	}

	@Override
	public Boolean toBoolean(String str, Boolean defaultValue) {
		return Caster.toBoolean(str, defaultValue);
	}

	@Override
	public Boolean toBoolean(String str) throws PageException {
		return Caster.toBoolean(str);
	}

	@Override
	public boolean toBooleanValue(boolean b) {
		return Caster.toBooleanValue(b);
	}

	@Override
	public boolean toBooleanValue(char c) {
		return Caster.toBooleanValue(c);
	}

	@Override
	public boolean toBooleanValue(double d) {
		return Caster.toBooleanValue(d);
	}

	@Override
	public boolean toBooleanValue(Object o, boolean defaultValue) {
		return Caster.toBooleanValue(o, defaultValue);
	}

	@Override
	public boolean toBooleanValue(Object o) throws PageException {
		return Caster.toBooleanValue(o);
	}

	@Override
	public boolean toBooleanValue(String str, boolean defaultValue) {
		return Caster.toBooleanValue(str, defaultValue);
	}

	@Override
	public boolean toBooleanValue(String str) throws PageException {
		return Caster.toBooleanValue(str);
	}

	@Override
	public Byte toByte(boolean b) {
		return Caster.toByte(b);
	}

	@Override
	public Byte toByte(char c) {
		return Caster.toByte(c);
	}

	@Override
	public Byte toByte(double d) {
		return Caster.toByte(d);
	}

	@Override
	public Byte toByte(Object o, Byte defaultValue) {
		return Caster.toByte(o, defaultValue);
	}

	@Override
	public Byte toByte(Object o) throws PageException {
		return Caster.toByte(o);
	}

	@Override
	public byte toByteValue(boolean b) {
		return Caster.toByteValue(b);
	}

	@Override
	public byte toByteValue(char c) {
		return Caster.toByteValue(c);
	}

	@Override
	public byte toByteValue(double d) {
		return Caster.toByteValue(d);
	}

	@Override
	public byte toByteValue(Object o, byte defaultValue) {
		return Caster.toByteValue(o, defaultValue);
	}

	@Override
	public byte toByteValue(Object o) throws PageException {
		return Caster.toByteValue(o);
	}

	@Override
	public Character toCharacter(boolean b) {
		return Caster.toCharacter(b);
	}

	@Override
	public Character toCharacter(char c) {
		return Caster.toCharacter(c);
	}

	@Override
	public Character toCharacter(double d) {
		return Caster.toCharacter(d);
	}

	@Override
	public Character toCharacter(Object o, Character defaultValue) {
		return Caster.toCharacter(o, defaultValue);
	}

	@Override
	public Character toCharacter(Object o) throws PageException {
		return Caster.toCharacter(o);
	}

	@Override
	public char toCharValue(boolean b) {
		return Caster.toCharValue(b);
	}

	@Override
	public char toCharValue(char c) {
		return Caster.toCharValue(c);
	}

	@Override
	public char toCharValue(double d) {
		return Caster.toCharValue(d);
	}

	@Override
	public char toCharValue(Object o, char defaultValue) {
		return Caster.toCharValue(o, defaultValue);
	}

	@Override
	public char toCharValue(Object o) throws PageException {
		return Caster.toCharValue(o);
	}

	@Override
	public Collection toCollection(Object o, Collection defaultValue) {
		return Caster.toCollection(o, defaultValue);
	}

	@Override
	public Collection toCollection(Object o) throws PageException {
		return Caster.toCollection(o);
	}

	@Override
	public Color toColor(Object o) throws PageException {
		if (o instanceof Color) return (Color) o;
		else if (o instanceof CharSequence) return ColorCaster.toColor(o.toString());
		else if (o instanceof Number) return ColorCaster.toColor(Integer.toHexString(((Number) o).intValue()));
		throw new CasterException(o, Color.class);
	}

	@Override
	public DateTime toDate(boolean b, TimeZone tz) {
		return Caster.toDate(b, tz);
	}

	@Override
	public DateTime toDate(char c, TimeZone tz) {
		return Caster.toDate(c, tz);
	}

	@Override
	public DateTime toDate(double d, TimeZone tz) {
		return Caster.toDate(d, tz);
	}

	@Override
	public DateTime toDate(Locale locale, String str, TimeZone tz, DateTime defaultValue) {
		return DateCaster.toDateTime(locale, str, tz, defaultValue, true);
	}

	@Override
	public DateTime toDate(Locale locale, String str, TimeZone tz) throws PageException {
		return DateCaster.toDateTime(locale, str, tz, true);
	}

	@Override
	public DateTime toDate(Object o, boolean alsoNumbers, TimeZone tz, DateTime defaultValue) {
		return Caster.toDate(o, alsoNumbers, tz, defaultValue);
	}

	@Override
	public DateTime toDate(Object o, TimeZone tz) throws PageException {
		return Caster.toDate(o, tz);
	}

	@Override
	public DateTime toDate(String str, boolean alsoNumbers, TimeZone tz, DateTime defaultValue) {
		return Caster.toDate(str, alsoNumbers, tz, defaultValue);
	}

	@Override
	public DateTime toDate(String str, TimeZone tz) throws PageException {
		return Caster.toDate(str, tz);
	}

	@Override
	public DateTime toDatetime(Object o, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, tz);
	}

	@Override
	public DateTime toDateTime(Object o, TimeZone tz) throws PageException {
		return DateCaster.toDateAdvanced(o, tz);
	}

	@Override
	public DateTime toDateTime(Object o, TimeZone tz, DateTime defaultValue) {
		return DateCaster.toDateAdvanced(o, tz, defaultValue);
	}

	@Override
	public String toDecimal(boolean b) {
		return Caster.toDecimal(b);
	}

	@Override
	public String toDecimal(char c) {
		return Caster.toDecimal(c, true);
	}

	@Override
	public String toDecimal(double d) {
		return Caster.toDecimal(d, true);
	}

	@Override
	public String toDecimal(Object value, String defaultValue) {
		return Caster.toDecimal(value, true, defaultValue);
	}

	@Override
	public String toDecimal(Object value) throws PageException {
		return Caster.toDecimal(value, true);
	}

	@Override
	public Double toDouble(boolean b) {
		return Caster.toDouble(b);
	}

	@Override
	public Double toDouble(char c) {
		return Caster.toDouble(c);
	}

	@Override
	public Double toDouble(double d) {
		return Caster.toDouble(d);
	}

	@Override
	public Double toDouble(Object o, Double defaultValue) {
		return Caster.toDouble(o, defaultValue);
	}

	@Override
	public Double toDouble(Object o) throws PageException {
		return Caster.toDouble(o);
	}

	@Override
	public Double toDouble(String str, Double defaultValue) {
		return Caster.toDouble(str, defaultValue);
	}

	@Override
	public Double toDouble(String str) throws PageException {
		return Caster.toDouble(str);
	}

	@Override
	public double toDoubleValue(boolean b) {
		return Caster.toDoubleValue(b);
	}

	@Override
	public double toDoubleValue(char c) {
		return Caster.toDoubleValue(c);
	}

	@Override
	public double toDoubleValue(double d) {
		return Caster.toDoubleValue(d);
	}

	@Override
	public double toDoubleValue(Object o, double defaultValue) {
		return Caster.toDoubleValue(o, true, defaultValue);
	}

	@Override
	public double toDoubleValue(Object o) throws PageException {
		return Caster.toDoubleValue(o);
	}

	@Override
	public double toDoubleValue(String str, double defaultValue) {
		return Caster.toDoubleValue(str, true, defaultValue);
	}

	@Override
	public double toDoubleValue(String str) throws PageException {
		return Caster.toDoubleValue(str);
	}

	@Override
	public File toFile(Object obj, File defaultValue) {
		return Caster.toFile(obj, defaultValue);
	}

	@Override
	public File toFile(Object obj) throws PageException {
		return Caster.toFile(obj);
	}

	@Override
	public Integer toInteger(boolean b) {
		return Caster.toInteger(b);
	}

	@Override
	public Integer toInteger(char c) {
		return Caster.toInteger(c);
	}

	@Override
	public Integer toInteger(double d) {
		return Caster.toInteger(d);
	}

	@Override
	public Integer toInteger(Object o, Integer defaultValue) {
		return Caster.toInteger(o, defaultValue);
	}

	@Override
	public Integer toInteger(Object o) throws PageException {
		return Caster.toInteger(o);
	}

	@Override
	public int toIntValue(boolean b) {
		return Caster.toIntValue(b);
	}

	@Override
	public int toIntValue(char c) {
		return Caster.toIntValue(c);
	}

	@Override
	public int toIntValue(double d) {
		return Caster.toIntValue(d);
	}

	@Override
	public int toIntValue(Object o, int defaultValue) {
		return Caster.toIntValue(o, defaultValue);
	}

	@Override
	public int toIntValue(Object o) throws PageException {
		return Caster.toIntValue(o);
	}

	@Override
	public int toIntValue(String str, int defaultValue) {
		return Caster.toIntValue(str, defaultValue);
	}

	@Override
	public int toIntValue(String str) throws PageException {
		return Caster.toIntValue(str);
	}

	@Override
	public Iterator toIterator(Object o) throws PageException {
		return Caster.toIterator(o);
	}

	@Override
	public List toList(Object o, boolean duplicate, List defaultValue) {
		return Caster.toList(o, duplicate, defaultValue);
	}

	@Override
	public List toList(Object o, boolean duplicate) throws PageException {
		return Caster.toList(o, duplicate);
	}

	@Override
	public List toList(Object o, List defaultValue) {
		return Caster.toList(o, defaultValue);
	}

	@Override
	public List toList(Object o) throws PageException {
		return Caster.toList(o);
	}

	@Override
	public Locale toLocale(String strLocale, Locale defaultValue) {
		return Caster.toLocale(strLocale, defaultValue);
	}

	@Override
	public Locale toLocale(String strLocale) throws PageException {
		return Caster.toLocale(strLocale);
	}

	@Override
	public Long toLong(boolean b) {
		return Caster.toLong(b);
	}

	@Override
	public Long toLong(char c) {
		return Caster.toLong(c);
	}

	@Override
	public Long toLong(double d) {
		return Caster.toLong(d);
	}

	@Override
	public Long toLong(Object o, Long defaultValue) {
		return Caster.toLong(o, defaultValue);
	}

	@Override
	public Long toLong(Object o) throws PageException {
		return Caster.toLong(o);
	}

	@Override
	public long toLongValue(boolean b) {
		return Caster.toLongValue(b);
	}

	@Override
	public long toLongValue(char c) {
		return Caster.toLongValue(c);
	}

	@Override
	public long toLongValue(double d) {
		return Caster.toLongValue(d);
	}

	@Override
	public long toLongValue(Object o, long defaultValue) {
		return Caster.toLongValue(o, defaultValue);
	}

	@Override
	public long toLongValue(Object o) throws PageException {
		return Caster.toLongValue(o);
	}

	@Override
	public Map toMap(Object o, boolean duplicate, Map defaultValue) {
		return Caster.toMap(o, duplicate, defaultValue);
	}

	@Override
	public Map toMap(Object o, boolean duplicate) throws PageException {
		return Caster.toMap(o, duplicate);
	}

	@Override
	public Map toMap(Object o, Map defaultValue) {
		return Caster.toMap(o, defaultValue);
	}

	@Override
	public Map toMap(Object o) throws PageException {
		return Caster.toMap(o);
	}

	// @Override
	public Node toNode(Object o, Node defaultValue) {
		return Caster.toNode(o, defaultValue);
	}

	// @Override
	public Node toNode(Object o) throws PageException {
		return Caster.toNode(o);
	}

	// @Override
	public NodeList toNodeList(Object o, NodeList defaultValue) {
		return Caster.toNodeList(o, defaultValue);
	}

	// @Override
	public NodeList toNodeList(Object o) throws PageException {
		return Caster.toNodeList(o);
	}

	@Override
	public Object toNull(Object value, Object defaultValue) {
		return Caster.toNull(value, defaultValue);
	}

	@Override
	public Object toNull(Object value) throws PageException {
		return Caster.toNull(value);
	}

	@Override
	public Collection.Key toKey(Object o) throws PageException {
		return Caster.toKey(o);
	}

	@Override
	public Collection.Key toKey(String str) {
		return KeyImpl.init(str);
	}

	@Override
	public Collection.Key toKey(Object o, Collection.Key defaultValue) {
		return Caster.toKey(o, defaultValue);
	}

	@Override
	public PageException toPageException(Throwable t) {
		return Caster.toPageException(t);
	}

	@Override
	public Query toQuery(Object o, boolean duplicate, Query defaultValue) {
		return Caster.toQuery(o, duplicate, defaultValue);
	}

	@Override
	public Query toQuery(Object o, boolean duplicate) throws PageException {
		return Caster.toQuery(o, duplicate);
	}

	@Override
	public Query toQuery(Object o, Query defaultValue) {
		return Caster.toQuery(o, defaultValue);
	}

	@Override
	public Query toQuery(Object o) throws PageException {
		return Caster.toQuery(o);
	}

	@Override
	public Boolean toRef(boolean b) {
		return Caster.toRef(b);
	}

	@Override
	public Byte toRef(byte b) {
		return Caster.toRef(b);
	}

	@Override
	public String toRef(char c) {
		return Caster.toRef(c);
	}

	@Override
	public Collection toRef(Collection o) {
		return Caster.toRef(o);
	}

	@Override
	public Double toRef(double d) {
		return Caster.toRef(d);
	}

	@Override
	public Float toRef(float f) {
		return Caster.toRef(f);
	}

	@Override
	public Integer toRef(int i) {
		return Caster.toRef(i);
	}

	@Override
	public Long toRef(long l) {
		return Caster.toRef(l);
	}

	@Override
	public Object toRef(Object o) {
		return Caster.toRef(o);
	}

	@Override
	public Short toRef(short s) {
		return Caster.toRef(s);
	}

	@Override
	public String toRef(String str) {
		return Caster.toRef(str);
	}

	@Override
	public Short toShort(boolean b) {
		return Caster.toShort(b);
	}

	@Override
	public Short toShort(char c) {
		return Caster.toShort(c);
	}

	@Override
	public Short toShort(double d) {
		return Caster.toShort(d);
	}

	@Override
	public Short toShort(Object o, Short defaultValue) {
		return Caster.toShort(o, defaultValue);
	}

	@Override
	public Short toShort(Object o) throws PageException {
		return Caster.toShort(o);
	}

	@Override
	public short toShortValue(boolean b) {
		return Caster.toShortValue(b);
	}

	@Override
	public short toShortValue(char c) {
		return Caster.toShortValue(c);
	}

	@Override
	public short toShortValue(double d) {
		return Caster.toShortValue(d);
	}

	@Override
	public short toShortValue(Object o, short defaultValue) {
		return Caster.toShortValue(o, defaultValue);
	}

	@Override
	public short toShortValue(Object o) throws PageException {
		return Caster.toShortValue(o);
	}

	@Override
	public String toString(boolean b) {
		return Caster.toString(b);
	}

	@Override
	public String toString(double d) {
		return Caster.toString(d);
	}

	@Override
	public String toString(int i) {
		return Caster.toString(i);
	}

	@Override
	public String toString(long l) {
		return Caster.toString(l);
	}

	@Override
	public String toString(Object o, String defaultValue) {
		return Caster.toString(o, defaultValue);
	}

	@Override
	public String toString(Object o) throws PageException {
		return Caster.toString(o);
	}

	@Override
	public Struct toStruct(Object o, Struct defaultValue, boolean caseSensitive) {
		return Caster.toStruct(o, defaultValue, caseSensitive);
	}

	@Override
	public Struct toStruct(Object o, Struct defaultValue) {
		return Caster.toStruct(o, defaultValue, true);
	}

	@Override
	public Struct toStruct(Object o) throws PageException {
		return Caster.toStruct(o);
	}

	@Override
	public TimeSpan toTimespan(Object o, TimeSpan defaultValue) {
		return Caster.toTimespan(o, defaultValue);
	}

	@Override
	public TimeSpan toTimespan(Object o) throws PageException {
		return Caster.toTimespan(o);
	}

	@Override
	public TimeSpan toTimespan(long millis) {
		return TimeSpanImpl.fromMillis(millis);
	}

	@Override
	public String toTypeName(Object o) {
		return Caster.toTypeName(o);
	}

	@Override
	public Object toUUId(Object o, Object defaultValue) {
		return Caster.toUUId(o, defaultValue);
	}

	@Override
	public Object toUUId(Object o) throws PageException {
		return Caster.toUUId(o);
	}

	@Override
	public Object toVariableName(Object obj, Object defaultValue) {
		String res = Caster.toVariableName(obj, null);
		if (res == null) return defaultValue;
		return res;
	}

	@Override
	public String toVariableName(Object obj, String defaultValue) {
		return Caster.toVariableName(obj, defaultValue);
	}

	@Override
	public String toVariableName(Object o) throws PageException {
		return Caster.toVariableName(o);
	}

	@Override
	public Object toVoid(Object o, Object defaultValue) {
		return Caster.toVoid(o, defaultValue);
	}

	@Override
	public Object toVoid(Object o) throws PageException {
		return Caster.toVoid(o);
	}

	// @Override
	public Node toXML(Object value, Node defaultValue) {
		return Caster.toXML(value, defaultValue);
	}

	// @Override
	public Node toXML(Object value) throws PageException {
		return Caster.toXML(value);
	}

	public static Cast getInstance() {
		if (singelton == null) singelton = new CastImpl();
		return singelton;
	}

	@Override
	public Resource toResource(Object obj) throws PageException {
		if (obj instanceof Resource) return (Resource) obj;
		if (obj instanceof File) return ResourceUtil.toResource((File) obj);
		return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), toString(obj));
	}

	@Override
	public Resource toResource(Object obj, Resource defaultValue) {
		if (obj instanceof Resource) return (Resource) obj;
		String path = toString(obj, null);
		if (path == null) return defaultValue;
		return ResourceUtil.toResourceNotExisting(ThreadLocalPageContext.get(), path);
	}

	@Override
	public Object to(String type, Object o, boolean alsoPattern) throws PageException {
		return Caster.castTo(ThreadLocalPageContext.get(), type, o, alsoPattern);
	}

	@Override
	public Serializable toSerializable(Object obj) throws PageException {
		return Caster.toSerializable(obj);
	}

	@Override
	public Serializable toSerializable(Object object, Serializable defaultValue) {
		return Caster.toSerializable(object, defaultValue);
	}

	@Override
	public Charset toCharset(String strCharset) {
		return CharsetUtil.toCharset(strCharset);
	}

	@Override
	public Charset toCharset(String strCharset, Charset defaultValue) {
		return CharsetUtil.toCharset(strCharset, defaultValue);
	}

	@Override
	public RuntimeException toPageRuntimeException(Throwable t) {
		return new PageRuntimeException(toPageException(t));
	}

	@Override
	public Float toFloat(Object o) throws PageException {
		return Caster.toFloat(o);
	}

	@Override
	public Float toFloat(Object o, Float defaultValue) {
		return Caster.toFloat(o, defaultValue);
	}

	@Override
	public float toFloatValue(Object o) throws PageException {
		return Caster.toFloatValue(o);
	}

	@Override
	public float toFloatValue(Object o, float defaultValue) {
		return Caster.toFloatValue(o, defaultValue);
	}

	@Override
	public BigDecimal toBigDecimal(Object obj) throws PageException {
		return Caster.toBigDecimal(obj);
	}

	@Override
	public BigDecimal toBigDecimal(Object obj, BigDecimal defaultValue) {
		try {
			return Caster.toBigDecimal(obj);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return defaultValue;
		}
	}

	@Override
	public Component toComponent(Object obj) throws PageException {
		return Caster.toComponent(obj);
	}

	@Override
	public Component toComponent(Object obj, Component defaultValue) {
		return Caster.toComponent(obj, defaultValue);
	}

	@Override
	public TimeZone toTimeZone(Object obj) throws PageException {
		return Caster.toTimeZone(obj);
	}

	@Override
	public TimeZone toTimeZone(Object obj, TimeZone defaultValue) {
		return Caster.toTimeZone(obj, defaultValue);
	}

	@Override
	public Calendar toCalendar(long time, TimeZone tz, Locale l) {
		return Caster.toCalendar(time, tz, l);
	}

	@Override
	public DumpData toDumpTable(Struct sct, String title, PageContext pageContext, int maxlevel, DumpProperties dp) {
		return StructUtil.toDumpTable(sct, title, pageContext, maxlevel, dp);
	}

	// FUTURE add to interface
	public Credentials toCredentials(String username, String password) {
		return CredentialsImpl.toCredentials(username, password);
	}
}