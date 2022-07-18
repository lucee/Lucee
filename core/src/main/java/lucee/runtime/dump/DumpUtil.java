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
package lucee.runtime.dump;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;

import org.apache.felix.framework.BundleWiringImpl.BundleClassLoader;
import org.osgi.framework.Bundle;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lucee.commons.date.TimeZoneUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.CharSet;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.IDGenerator;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.coder.Base64Coder;
import lucee.runtime.converter.WDDXConverter;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.i18n.LocaleFactory;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.osgi.OSGiUtil;
import lucee.runtime.osgi.OSGiUtil.BundleDefinition;
import lucee.runtime.osgi.OSGiUtil.PackageQuery;
import lucee.runtime.osgi.OSGiUtil.VersionDefinition;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Pojo;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.scope.CookieImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.UDFUtil;

public class DumpUtil {

	public static final DumpData MAX_LEVEL_REACHED;

	static {

		MAX_LEVEL_REACHED = new DumpTable("Max Level Reached", "#e0e0e0", "#ffcc99", "#888888");
		((DumpTable) MAX_LEVEL_REACHED).appendRow(new DumpRow(1, new SimpleDumpData("[Max Dump Level Reached]")));
	}

	// FUTURE add to interface
	public static DumpData toDumpData(Object o, PageContext pageContext, int maxlevel, DumpProperties props) {
		if (maxlevel < 0) return MAX_LEVEL_REACHED;

		// null
		if (o == null) {
			DumpTable table = new DumpTable("null", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(new DumpRow(0, new SimpleDumpData("Empty:null")));
			return table;
		}
		if (o instanceof DumpData) {
			return ((DumpData) o);
		}
		// Date
		if (o instanceof Date) {
			return new DateTimeImpl((Date) o).toDumpData(pageContext, maxlevel, props);
		}
		// Calendar
		if (o instanceof Calendar) {
			Calendar c = (Calendar) o;

			SimpleDateFormat df = new SimpleDateFormat("EE, dd MMM yyyy HH:mm:ss zz", Locale.ENGLISH);
			df.setTimeZone(c.getTimeZone());

			DumpTable table = new DumpTable("date", "#ff9900", "#ffcc00", "#000000");
			table.setTitle("java.util.Calendar");
			table.appendRow(1, new SimpleDumpData("Timezone"), new SimpleDumpData(TimeZoneUtil.toString(c.getTimeZone())));
			table.appendRow(1, new SimpleDumpData("Time"), new SimpleDumpData(df.format(c.getTime())));

			return table;
		}
		// StringBuffer
		if (o instanceof StringBuffer) {
			DumpTable dt = (DumpTable) toDumpData(o.toString(), pageContext, maxlevel, props);
			if (StringUtil.isEmpty(dt.getTitle())) dt.setTitle(Caster.toClassName(o));
			return dt;
		}
		// StringBuilder
		if (o instanceof StringBuilder) {
			DumpTable dt = (DumpTable) toDumpData(o.toString(), pageContext, maxlevel, props);
			if (StringUtil.isEmpty(dt.getTitle())) dt.setTitle(Caster.toClassName(o));
			return dt;
		}
		// String
		if (o instanceof String) {
			String str = (String) o;
			if (str.trim().startsWith("<wddxPacket ")) {
				try {
					WDDXConverter converter = new WDDXConverter(pageContext.getTimeZone(), false, true);
					converter.setTimeZone(pageContext.getTimeZone());
					Object rst = converter.deserialize(str, false);
					DumpData data = toDumpData(rst, pageContext, maxlevel, props);

					DumpTable table = new DumpTable("string", "#cc9999", "#ffffff", "#000000");
					table.setTitle("WDDX");
					table.appendRow(1, new SimpleDumpData("encoded"), data);
					table.appendRow(1, new SimpleDumpData("raw"), new SimpleDumpData(str));
					return table;
				}
				catch (Exception e) {
					// this dump entry is optional, so if it is not possible to create the decoded wddx entry, we simply
					// don't do it
				}
			}
			DumpTable table = new DumpTable("string", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("string"), new SimpleDumpData(str));
			return table;
		}
		// Character
		if (o instanceof Character) {
			DumpTable table = new DumpTable("character", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("character"), new SimpleDumpData(o.toString()));
			return table;
		}
		// Number
		if (o instanceof Number) {
			DumpTable table = new DumpTable("numeric", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("number"), new SimpleDumpData(Caster.toString(((Number) o))));
			return table;
		}
		// Charset
		if (o instanceof Charset) {
			DumpTable table = new DumpTable("charset", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("charset"), new SimpleDumpData(((Charset) o).name()));
			return table;
		}
		// CharSet
		if (o instanceof CharSet) {
			DumpTable table = new DumpTable("charset", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("charset"), new SimpleDumpData(((CharSet) o).name()));
			return table;
		}
		// Locale
		if (o instanceof Locale) {
			Locale l = (Locale) o;
			Locale env = ThreadLocalPageContext.getLocale();
			DumpTable table = new DumpTable("locale", "#ff6600", "#ffcc99", "#000000");
			table.setTitle("Locale " + LocaleFactory.getDisplayName(l));
			table.appendRow(1, new SimpleDumpData("Code (ISO-3166)"), new SimpleDumpData(l.toString()));
			table.appendRow(1, new SimpleDumpData("Country"), new SimpleDumpData(l.getDisplayCountry(env)));
			table.appendRow(1, new SimpleDumpData("Language"), new SimpleDumpData(l.getDisplayLanguage(env)));
			return table;
		}
		// TimeZone
		if (o instanceof TimeZone) {
			DumpTable table = new DumpTable("numeric", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("TimeZone"), new SimpleDumpData(TimeZoneUtil.toString(((TimeZone) o))));
			return table;
		}
		// Boolean
		if (o instanceof Boolean) {
			DumpTable table = new DumpTable("boolean", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("boolean"), new SimpleDumpData(((Boolean) o).booleanValue()));
			return table;
		}
		// File
		if (o instanceof File) {
			DumpTable table = new DumpTable("file", "#ffcc00", "#ffff66", "#000000");
			table.appendRow(1, new SimpleDumpData("File"), new SimpleDumpData(o.toString()));
			return table;
		}
		// Cookie
		if (o instanceof Cookie) {
			Cookie c = (Cookie) o;
			DumpTable table = new DumpTable("Cookie", "#979EAA", "#DEE9FB", "#000000");
			table.setTitle("Cookie (" + c.getClass().getName() + ")");
			table.appendRow(1, new SimpleDumpData("name"), new SimpleDumpData(c.getName()));
			table.appendRow(1, new SimpleDumpData("value"), new SimpleDumpData(c.getValue()));
			table.appendRow(1, new SimpleDumpData("path"), new SimpleDumpData(c.getPath()));
			table.appendRow(1, new SimpleDumpData("secure"), new SimpleDumpData(c.getSecure()));
			table.appendRow(1, new SimpleDumpData("maxAge"), new SimpleDumpData(c.getMaxAge()));
			table.appendRow(1, new SimpleDumpData("version"), new SimpleDumpData(c.getVersion()));
			table.appendRow(1, new SimpleDumpData("domain"), new SimpleDumpData(c.getDomain()));
			table.appendRow(1, new SimpleDumpData("httpOnly"), new SimpleDumpData(CookieImpl.isHTTPOnly(c)));
			table.appendRow(1, new SimpleDumpData("comment"), new SimpleDumpData(c.getComment()));
			return table;
		}
		// Resource
		if (o instanceof Resource) {
			DumpTable table = new DumpTable("resource", "#ffcc00", "#ffff66", "#000000");
			table.appendRow(1, new SimpleDumpData("Resource"), new SimpleDumpData(o.toString()));
			return table;
		}
		// byte[]
		if (o instanceof byte[]) {
			byte[] bytes = (byte[]) o;
			int max = 5000;
			DumpTable table = new DumpTable("array", "#ff9900", "#ffcc00", "#000000");
			table.setTitle("Native Array  (" + Caster.toClassName(o) + ")");
			StringBuilder sb = new StringBuilder("[");
			for (int i = 0; i < bytes.length; i++) {
				if (i != 0) sb.append(",");
				sb.append(bytes[i]);
				if (i == max) {
					sb.append(", ...truncated");
					break;
				}
			}
			sb.append("]");
			table.appendRow(1, new SimpleDumpData("Raw" + (bytes.length < max ? "" : " (truncated)")), new SimpleDumpData(sb.toString()));

			if (bytes.length < max) {
				// base64
				table.appendRow(1, new SimpleDumpData("Base64 Encoded"), new SimpleDumpData(Base64Coder.encode(bytes)));
				/*
				 * try { table.appendRow(1,new SimpleDumpData("CFML expression"),new
				 * SimpleDumpData("evaluateJava('"+JavaConverter.serialize(bytes)+"')"));
				 * 
				 * } catch (IOException e) {}
				 */
			}

			return table;
		}
		// Collection.Key
		if (o instanceof Collection.Key) {
			Collection.Key key = (Collection.Key) o;
			DumpTable table = new DumpTable("string", "#ff6600", "#ffcc99", "#000000");
			table.appendRow(1, new SimpleDumpData("Collection.Key"), new SimpleDumpData(key.getString()));
			return table;
		}

		String id = "" + IDGenerator.intId();
		String refid = ThreadLocalDump.get(o);
		if (refid != null) {
			DumpTable table = new DumpTable("ref", "#ffffff", "#cccccc", "#000000");
			table.appendRow(1, new SimpleDumpData("Reference"), new SimpleDumpData(refid));
			table.setRef(refid);
			return setId(id, table);
		}

		ThreadLocalDump.set(o, id);
		try {

			int top = props.getMaxlevel();

			// Dumpable
			if (o instanceof Dumpable) {
				DumpData dd = ((Dumpable) o).toDumpData(pageContext, maxlevel, props);
				if (dd != null) return setId(id, dd);
			}
			if (o instanceof UDF) {
				return UDFUtil.toDumpData(pageContext, maxlevel, props, (UDF) o, UDFUtil.TYPE_UDF);
			}
			// Map
			if (o instanceof Map) {
				Map map = (Map) o;
				Iterator it = map.keySet().iterator();

				DumpTable table = new DumpTable("struct", "#ff9900", "#ffcc00", "#000000");
				table.setTitle("Map (" + Caster.toClassName(o) + ")");

				while (it.hasNext()) {
					Object next = it.next();
					table.appendRow(1, toDumpData(next, pageContext, maxlevel, props), toDumpData(map.get(next), pageContext, maxlevel, props));
				}
				return setId(id, table);
			}

			// List
			if (o instanceof List) {
				List list = (List) o;
				ListIterator it = list.listIterator();

				DumpTable table = new DumpTable("array", "#ff9900", "#ffcc00", "#000000");
				table.setTitle("Array (List)");
				if (list.size() > top) table.setComment("Rows: " + list.size() + " (showing top " + top + ")");

				int i = 0;
				while (it.hasNext() && i++ < top) {
					table.appendRow(1, new SimpleDumpData(it.nextIndex() + 1), toDumpData(it.next(), pageContext, maxlevel, props));
				}
				return setId(id, table);
			}

			// Set
			if (o instanceof Set) {
				Set set = (Set) o;
				Iterator it = set.iterator();

				DumpTable table = new DumpTable("array", "#ff9900", "#ffcc00", "#000000");
				table.setTitle("Set (" + set.getClass().getName() + ")");

				int i = 0;
				while (it.hasNext() && i++ < top) {
					table.appendRow(1, toDumpData(it.next(), pageContext, maxlevel, props));
				}
				return setId(id, table);
			}

			// Resultset
			if (o instanceof ResultSet) {
				try {
					DumpData dd = new QueryImpl((ResultSet) o, "query", pageContext.getTimeZone()).toDumpData(pageContext, maxlevel, props);
					if (dd instanceof DumpTable) ((DumpTable) dd).setTitle(Caster.toClassName(o));
					return setId(id, dd);
				}
				catch (PageException e) {

				}
			}
			// Enumeration
			if (o instanceof Enumeration) {
				Enumeration e = (Enumeration) o;

				DumpTable table = new DumpTable("enumeration", "#ff9900", "#ffcc00", "#000000");
				table.setTitle("Enumeration");

				int i = 0;
				while (e.hasMoreElements() && i++ < top) {
					table.appendRow(0, toDumpData(e.nextElement(), pageContext, maxlevel, props));
				}
				return setId(id, table);
			}
			// Object[]
			if (Decision.isNativeArray(o)) {
				Array arr;
				try {
					arr = Caster.toArray(o);
					DumpTable htmlBox = new DumpTable("array", "#ff9900", "#ffcc00", "#000000");
					htmlBox.setTitle("Native Array (" + Caster.toClassName(o) + ")");

					int length = arr.size();

					for (int i = 1; i <= length; i++) {
						Object ox = null;
						try {
							ox = arr.getE(i);
						}
						catch (Exception e) {
						}
						htmlBox.appendRow(1, new SimpleDumpData(i), toDumpData(ox, pageContext, maxlevel, props));
					}
					return setId(id, htmlBox);
				}
				catch (PageException e) {
					return setId(id, new SimpleDumpData(""));
				}
			}
			// Node
			if (o instanceof Node) {
				return setId(id, XMLCaster.toDumpData((Node) o, pageContext, maxlevel, props));
			}
			// ObjectWrap
			if (o instanceof ObjectWrap) {
				maxlevel++;
				return setId(id, toDumpData(((ObjectWrap) o).getEmbededObject(null), pageContext, maxlevel, props));
			}
			// NodeList
			if (o instanceof NodeList) {
				NodeList list = (NodeList) o;
				int len = list.getLength();
				DumpTable table = new DumpTable("xml", "#cc9999", "#ffffff", "#000000");
				for (int i = 0; i < len; i++) {
					table.appendRow(1, new SimpleDumpData(i), toDumpData(list.item(i), pageContext, maxlevel, props));
				}
				return setId(id, table);

			}
			// AttributeMap
			if (o instanceof NamedNodeMap) {
				NamedNodeMap attr = (NamedNodeMap) o;
				int len = attr.getLength();
				DumpTable dt = new DumpTable("array", "#ff9900", "#ffcc00", "#000000");
				dt.setTitle("NamedNodeMap (" + Caster.toClassName(o) + ")");

				for (int i = 0; i < len; i++) {
					dt.appendRow(1, new SimpleDumpData(i), toDumpData(attr.item(i), pageContext, maxlevel, props));
				}
				return setId(id, dt);
			}
			// HttpSession
			if (o instanceof HttpSession) {
				HttpSession hs = (HttpSession) o;
				Enumeration e = hs.getAttributeNames();

				DumpTable htmlBox = new DumpTable("httpsession", "#9999ff", "#ccccff", "#000000");
				htmlBox.setTitle("HttpSession");
				while (e.hasMoreElements()) {
					String key = e.nextElement().toString();
					htmlBox.appendRow(1, new SimpleDumpData(key), toDumpData(hs.getAttribute(key), pageContext, maxlevel, props));
				}
				return setId(id, htmlBox);
			}

			if (o instanceof Pojo) {
				DumpTable table = new DumpTable(o.getClass().getName(), "#ff99cc", "#ffccff", "#000000");

				Class clazz = o.getClass();
				if (o instanceof Class) clazz = (Class) o;
				String fullClassName = clazz.getName();
				int pos = fullClassName.lastIndexOf('.');
				String className = pos == -1 ? fullClassName : fullClassName.substring(pos + 1);

				table.setTitle("Java Bean - " + className + " (" + fullClassName + ")");
				table.appendRow(3, new SimpleDumpData("Property Name"), new SimpleDumpData("Value"));

				// collect the properties
				Method[] methods = clazz.getMethods();
				String propName;
				Object value;
				String exName = null;
				String exValue = null;
				for (int i = 0; i < methods.length; i++) {
					Method method = methods[i];
					if (Object.class == method.getDeclaringClass()) continue;
					propName = method.getName();
					if (propName.startsWith("get") && method.getParameterTypes().length == 0) {
						propName = StringUtil.lcFirst(propName.substring(3));
						value = null;
						try {
							value = method.invoke(o, new Object[0]);

							if (exName == null && value instanceof String && ((String) value).length() < 20) {
								exName = propName;
								exValue = value.toString();
							}

						}
						catch (Throwable t) {
							ExceptionUtil.rethrowIfNecessary(t);
							value = "not able to retrieve the data:" + t.getMessage();
						}

						table.appendRow(0, new SimpleDumpData(propName), toDumpData(value, pageContext, maxlevel, props));
					}
				}

				if (exName == null) {
					exName = "LastName";
					exValue = "Sorglos";
				}

				table.setComment("JavaBeans are reusable software components for Java." + "\nThey are classes that encapsulate many objects into a single object (the bean)."
						+ "\nThey allow access to properties using getter and setter methods or directly.");

				/*
				 * "\n\nExample:\n" + "   x=myBean.get"+exName+"(); // read a property with a getter method\n" +
				 * "   x=myBean."+exName+"; // read a property directly\n" +
				 * "   myBean.set"+exName+"(\""+exValue+"\"); // write a property with a setter method\n" +
				 * "   myBean."+exName+"=\""+exValue+"\"; // write a property directly");
				 */

				return setId(id, table);

			}

			// reflect
			// else {
			DumpTable table = new DumpTable(o.getClass().getName(), "#6289a3", "#dee3e9", "#000000");

			Class clazz = o.getClass();
			if (o instanceof Class) clazz = (Class) o;
			String fullClassName = clazz.getName();
			int pos = fullClassName.lastIndexOf('.');
			String className = pos == -1 ? fullClassName : fullClassName.substring(pos + 1);

			table.setTitle(className);
			table.appendRow(1, new SimpleDumpData("class"), new SimpleDumpData(fullClassName));

			// Fields
			Field[] fields = clazz.getFields();
			DumpTable fieldDump = new DumpTable("#6289a3", "#dee3e9", "#000000");
			fieldDump.appendRow(-1, new SimpleDumpData("name"), new SimpleDumpData("pattern"), new SimpleDumpData("value"));
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				DumpData value;
				try {// print.out(o+":"+maxlevel);
					value = new SimpleDumpData(Caster.toString(field.get(o), ""));
				}
				catch (Exception e) {
					value = new SimpleDumpData("");
				}
				fieldDump.appendRow(0, new SimpleDumpData(field.getName()), new SimpleDumpData(field.toString()), value);
			}
			if (fields.length > 0) table.appendRow(1, new SimpleDumpData("fields"), fieldDump);

			// Constructors
			Constructor[] constructors = clazz.getConstructors();
			DumpTable constrDump = new DumpTable("#6289a3", "#dee3e9", "#000000");
			constrDump.appendRow(-1, new SimpleDumpData("interface"), new SimpleDumpData("exceptions"));
			for (int i = 0; i < constructors.length; i++) {
				Constructor constr = constructors[i];

				// exceptions
				StringBuilder sbExp = new StringBuilder();
				Class[] exceptions = constr.getExceptionTypes();
				for (int p = 0; p < exceptions.length; p++) {
					if (p > 0) sbExp.append("\n");
					sbExp.append(Caster.toClassName(exceptions[p]));
				}

				// parameters
				StringBuilder sbParams = new StringBuilder("<init>");
				sbParams.append('(');
				Class[] parameters = constr.getParameterTypes();
				for (int p = 0; p < parameters.length; p++) {
					if (p > 0) sbParams.append(", ");
					sbParams.append(Caster.toClassName(parameters[p]));
				}
				sbParams.append(')');

				constrDump.appendRow(0, new SimpleDumpData(sbParams.toString()), new SimpleDumpData(sbExp.toString()));
			}
			if (constructors.length > 0) table.appendRow(1, new SimpleDumpData("constructors"), constrDump);

			// Methods
			StringBuilder objMethods = new StringBuilder();
			Method[] methods = clazz.getMethods();
			DumpTable methDump = new DumpTable("#6289a3", "#dee3e9", "#000000");
			methDump.appendRow(-1, new SimpleDumpData("return"), new SimpleDumpData("interface"), new SimpleDumpData("exceptions"));
			for (int i = 0; i < methods.length; i++) {
				Method method = methods[i];

				if (Object.class == method.getDeclaringClass()) {
					if (objMethods.length() > 0) objMethods.append(", ");
					objMethods.append(method.getName());
					continue;
				}

				// exceptions
				StringBuilder sbExp = new StringBuilder();
				Class[] exceptions = method.getExceptionTypes();
				for (int p = 0; p < exceptions.length; p++) {
					if (p > 0) sbExp.append("\n");
					sbExp.append(Caster.toClassName(exceptions[p]));
				}

				// parameters
				StringBuilder sbParams = new StringBuilder(method.getName());
				sbParams.append('(');
				Class[] parameters = method.getParameterTypes();
				for (int p = 0; p < parameters.length; p++) {
					if (p > 0) sbParams.append(", ");
					sbParams.append(Caster.toClassName(parameters[p]));
				}
				sbParams.append(')');

				methDump.appendRow(0, new SimpleDumpData(Caster.toClassName(method.getReturnType())),

						new SimpleDumpData(sbParams.toString()), new SimpleDumpData(sbExp.toString()));
			}
			if (methods.length > 0) table.appendRow(1, new SimpleDumpData("methods"), methDump);

			DumpTable inherited = new DumpTable("#6289a3", "#dee3e9", "#000000");
			inherited.appendRow(7, new SimpleDumpData("Methods inherited from java.lang.Object"));
			inherited.appendRow(0, new SimpleDumpData(objMethods.toString()));
			table.appendRow(1, new SimpleDumpData(""), inherited);

			// Bundle Info
			ClassLoader cl = clazz.getClassLoader();
			if (cl instanceof BundleClassLoader) {
				try {
					BundleClassLoader bcl = (BundleClassLoader) cl;
					Bundle b = bcl.getBundle();
					if (b != null) {
						Struct sct = new StructImpl();
						sct.setEL(KeyConstants._id, b.getBundleId());
						sct.setEL(KeyConstants._name, b.getSymbolicName());
						sct.setEL(KeyConstants._location, b.getLocation());
						sct.setEL(KeyConstants._version, b.getVersion().toString());

						DumpTable bd = new DumpTable("#6289a3", "#dee3e9", "#000000");
						bd.appendRow(1, new SimpleDumpData("id"), new SimpleDumpData(b.getBundleId()));
						bd.appendRow(1, new SimpleDumpData("symbolic-name"), new SimpleDumpData(b.getSymbolicName()));
						bd.appendRow(1, new SimpleDumpData("version"), new SimpleDumpData(b.getVersion().toString()));
						bd.appendRow(1, new SimpleDumpData("location"), new SimpleDumpData(b.getLocation()));
						requiredBundles(bd, b);
						table.appendRow(1, new SimpleDumpData("bundle-info"), bd);
					}
				}
				catch (NoSuchMethodError e) {
				}
			}

			return setId(id, table);
			// }
		}
		finally {
			ThreadLocalDump.remove(o);
		}
	}

	private static Bundle getBundle(ClassLoader cl) {
		try {
			Method m = cl.getClass().getMethod("getBundle", new Class[0]);
			return (Bundle) m.invoke(cl, new Object[0]);
		}
		catch (Exception e) {
			return null;
		}
	}

	private static void requiredBundles(DumpTable parent, Bundle b) {
		try {
			List<BundleDefinition> list = OSGiUtil.getRequiredBundles(b);
			if (list.isEmpty()) return;
			DumpTable dt = new DumpTable("#6289a3", "#dee3e9", "#000000");
			dt.appendRow(-1, new SimpleDumpData("name"), new SimpleDumpData("version"), new SimpleDumpData("operator"));

			Iterator<BundleDefinition> it = list.iterator();
			BundleDefinition bd;
			VersionDefinition vd;
			String v, op;
			while (it.hasNext()) {
				bd = it.next();
				vd = bd.getVersionDefiniton();
				if (vd != null) {
					v = vd.getVersionAsString();
					op = vd.getOpAsString();
				}
				else {
					v = "";
					op = "";
				}
				dt.appendRow(0, new SimpleDumpData(bd.getName()), new SimpleDumpData(v), new SimpleDumpData(op));

			}
			parent.appendRow(1, new SimpleDumpData("required-bundles"), dt);

		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private static Array toArray2(List<PackageQuery> list) {
		Struct sct, _sct;
		Array arr = new ArrayImpl(), _arr;
		Iterator<PackageQuery> it = list.iterator();
		PackageQuery pd;
		Iterator<VersionDefinition> _it;
		VersionDefinition vd;
		while (it.hasNext()) {
			pd = it.next();
			sct = new StructImpl();
			sct.setEL(KeyConstants._package, pd.getName());
			sct.setEL("versions", _arr = new ArrayImpl());

			_it = pd.getVersionDefinitons().iterator();
			while (_it.hasNext()) {
				vd = _it.next();
				_sct = new StructImpl();
				_sct.setEL(KeyConstants._bundleVersion, vd.getVersion().toString());
				_sct.setEL("operator", vd.getOpAsString());
				_arr.appendEL(_sct);
			}
			arr.appendEL(sct);
		}
		return arr;
	}

	private static DumpData setId(String id, DumpData data) {
		if (data instanceof DumpTable) {
			((DumpTable) data).setId(id);
		}
		// TODO Auto-generated method stub
		return data;
	}

	public static boolean keyValid(DumpProperties props, int level, String key) {
		if (props.getMaxlevel() - level > 1) return true;

		// show
		Set set = props.getShow();
		if (set != null && !set.contains(StringUtil.toLowerCase(key))) return false;

		// hide
		set = props.getHide();
		if (set != null && set.contains(StringUtil.toLowerCase(key))) return false;

		return true;
	}

	public static boolean keyValid(DumpProperties props, int level, Collection.Key key) {
		if (props.getMaxlevel() - level > 1) return true;

		// show
		Set set = props.getShow();
		if (set != null && !set.contains(key.getLowerString())) return false;

		// hide
		set = props.getHide();
		if (set != null && set.contains(key.getLowerString())) return false;

		return true;
	}

	public static DumpProperties toDumpProperties() {
		return DumpProperties.DEFAULT;
	}
}