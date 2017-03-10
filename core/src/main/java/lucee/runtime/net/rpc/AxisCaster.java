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
package lucee.runtime.net.rpc;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.PhysicalClassLoader;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.ComponentSpecificAccess;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.component.PropertyImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.reflection.Reflector;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.ObjectWrap;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryColumn;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;

import org.apache.axis.Constants;
import org.apache.axis.types.Day;
import org.apache.axis.types.Duration;
import org.apache.axis.types.Entities;
import org.apache.axis.types.Entity;
import org.apache.axis.types.Language;
import org.apache.axis.types.Month;
import org.apache.axis.types.MonthDay;
import org.apache.axis.types.NCName;
import org.apache.axis.types.NMToken;
import org.apache.axis.types.NMTokens;
import org.apache.axis.types.Name;
import org.apache.axis.types.Token;
import org.apache.axis.types.URI;
import org.apache.axis.types.URI.MalformedURIException;
import org.apache.axis.types.Year;
import org.apache.axis.types.YearMonth;
import org.apache.axis.wsdl.symbolTable.TypeEntry;

import coldfusion.xml.rpc.QueryBean;

/**
 * Axis Type Caster
 */
public final class AxisCaster {

	/**
     * cast a value to a Axis Compatible Type
     * @param type
     * @param value
     * @return Axis Compatible Type
     * @throws PageException
     */
    public static Object toAxisType(TypeMapping tm,TimeZone tz,TypeEntry typeEntry,QName type, Object value) throws PageException {
    	return _toAxisType(tm, tz, typeEntry,type,null, value,new HashSet<Object>());
	}

    public static Object toAxisType(TypeMapping tm,Object value, Class targetClass) throws PageException {
    	return _toAxisType(tm,null,null,null,targetClass, value, new HashSet<Object>());
    }
	
    /**
     * cast a value to a Axis Compatible Type
     * @param type
     * @param value
     * @return Axis Compatible Type
     * @throws PageException
     */
    private static Object _toAxisType(TypeMapping tm,TimeZone tz,TypeEntry typeEntry,QName type, Class targetClass, Object value,Set<Object> done) throws PageException {
        
    	// first make sure we have no wrapper
    	if(value instanceof ObjectWrap) {
    		value=((ObjectWrap)value).getEmbededObject();
    	}
    	
    	
    	if(done.contains(value)){
			return null;// TODO not sure what in this case is the best solution.
		}
        
    	done.add(value);
    	try{
    		if(type!=null) {

    			// Array Of
    			if(type.getLocalPart().startsWith("ArrayOf")) {
    				return toArray(tm,typeEntry,type,value,done);
    	    	}
    			
    			
		        // XSD
		        for(int i=0;i<Constants.URIS_SCHEMA_XSD.length;i++) {
		        	if(Constants.URIS_SCHEMA_XSD[i].equals(type.getNamespaceURI())) {
		                return toAxisTypeXSD(tm,tz,type.getLocalPart(), value,done);
		            }
		        }
		        if(StringUtil.startsWithIgnoreCase(type.getLocalPart(),"xsd_")) {
		        	return toAxisTypeXSD(tm,tz,type.getLocalPart().substring(4), value,done);
		        }

		        //SOAP
		        if(type.getNamespaceURI().indexOf("soap")!=-1) {
		            return toAxisTypeSoap(tm,type.getLocalPart(), value,done);
		        }
		        if(StringUtil.startsWithIgnoreCase(type.getLocalPart(),"soap_")) {
		        	return toAxisTypeSoap(tm,type.getLocalPart().substring(5), value,done);
		        }
	        }
	    	return _toDefinedType(tm,typeEntry,type,targetClass,value,done);
        
    	}
    	finally{
    		done.remove(value);
    	}
    }
    
    private static Object toArray(TypeMapping tm, TypeEntry typeEntry,QName type, Object value, Set<Object> done) throws PageException {
    	if(type==null || !type.getLocalPart().startsWith("ArrayOf"))
    		throw new ApplicationException("invalid call of the functionn toArray");
    	
    	// get component Type
		String tmp = type.getLocalPart().substring(7);
		QName componentType=null;
		
		// no arrayOf embeded anymore
		if(tmp.indexOf("ArrayOf")==-1 && typeEntry!=null) {
			TypeEntry ref = typeEntry.getRefType();
			componentType=ref.getQName();
		}
		if(componentType==null) {
			if(tmp.startsWith("_tns1_"))tmp=tmp.substring(6);
			componentType=new QName(type.getNamespaceURI(), tmp);
		}
    	Object[] objs = Caster.toNativeArray(value);
    	Object[] rtns;
    	List<Object> list=new ArrayList<Object>();
    	
    	
    	Class componentClass=null;
    	Object v;
    	for(int i=0;i<objs.length;i++) {
	    	v=_toAxisType(tm,null,typeEntry,componentType,null,objs[i],done);
	    	list.add(v);
	    	if(i==0) {
	    		if(v!=null) componentClass=v.getClass();
	    	}
	    	else {
	    		if(v==null || v.getClass()!=componentClass) componentClass=null;
	    	}
	    	
	    }

    	if(componentClass!=null) {
        	componentClass=toAxisTypeClass(componentClass);
        	rtns = (Object[]) java.lang.reflect.Array.newInstance(componentClass, objs.length);
        }
        else 
        	rtns = new Object[objs.length];
    	
    	
    	
    	return list.toArray(rtns);
    }

    
    

	private static Object toAxisTypeSoap(TypeMapping tm,String local, Object value, Set<Object> done) throws PageException {
        if(local.equals(Constants.SOAP_ARRAY.getLocalPart())) return toArrayList(tm,value,done);
        if(local.equals(Constants.SOAP_ARRAY12.getLocalPart())) return toArrayList(tm,value,done);
        if(local.equals(Constants.SOAP_ARRAY_ATTRS11.getLocalPart())) return toArrayList(tm,value,done);
        if(local.equals(Constants.SOAP_ARRAY_ATTRS12.getLocalPart())) return toArrayList(tm,value,done);
        if(local.equals(Constants.SOAP_BASE64.getLocalPart())) return Caster.toBinary(value);
        if(local.equals(Constants.SOAP_BASE64BINARY.getLocalPart())) return Caster.toBinary(value);
        if(local.equals(Constants.SOAP_BOOLEAN.getLocalPart())) return Caster.toBoolean(value);
        if(local.equals(Constants.SOAP_BYTE.getLocalPart())) return Caster.toByte(value);
        if(local.equals(Constants.SOAP_DECIMAL.getLocalPart())) return new BigDecimal(Caster.toDoubleValue(value));
        if(local.equals(Constants.SOAP_DOUBLE.getLocalPart())) return Caster.toDouble(value);
        if(local.equals(Constants.SOAP_FLOAT.getLocalPart())) return new Float(Caster.toDoubleValue(value));
        if(local.equals(Constants.SOAP_INT.getLocalPart())) return Caster.toInteger(value);
        if(local.equals(Constants.SOAP_INTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equals(Constants.SOAP_LONG.getLocalPart())) return Caster.toLong(value);
        if(local.equals(Constants.SOAP_MAP.getLocalPart())) return toMap(tm,value,done);
        if(local.equals(Constants.SOAP_SHORT.getLocalPart())) return Caster.toShort(value);
        if(local.equals(Constants.SOAP_STRING.getLocalPart())) return Caster.toString(value);
        if(local.equals(Constants.SOAP_VECTOR.getLocalPart())) return toVector(tm,value,done);
        
        return _toDefinedType(tm,null,null,null,value,done);
        
        
    }

    private static Object toAxisTypeXSD(TypeMapping tm,TimeZone tz,String local, Object value, Set<Object> done) throws PageException {
        //if(local.equals(Constants.XSD_ANY.getLocalPart())) return value;
        if(local.equalsIgnoreCase(Constants.XSD_ANYSIMPLETYPE.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_ANYURI.getLocalPart())) return toURI(value);
        if(local.equalsIgnoreCase(Constants.XSD_STRING.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_BASE64.getLocalPart())) return Caster.toBinary(value);
        if(local.equalsIgnoreCase(Constants.XSD_BOOLEAN.getLocalPart())) return Caster.toBoolean(value);
        if(local.equalsIgnoreCase(Constants.XSD_BYTE.getLocalPart())) return Caster.toByte(value);
        if(local.equalsIgnoreCase(Constants.XSD_DATE.getLocalPart())) return Caster.toDate(value,null);
        if(local.equalsIgnoreCase(Constants.XSD_DATETIME.getLocalPart())) return Caster.toDate(value,null);
        if(local.equalsIgnoreCase(Constants.XSD_DAY.getLocalPart())) return toDay(value);
        if(local.equalsIgnoreCase(Constants.XSD_DECIMAL.getLocalPart())) return new BigDecimal(Caster.toDoubleValue(value));
        if(local.equalsIgnoreCase(Constants.XSD_DOUBLE.getLocalPart())) return Caster.toDouble(value);
        if(local.equalsIgnoreCase(Constants.XSD_DURATION.getLocalPart())) return toDuration(value);
        if(local.equalsIgnoreCase(Constants.XSD_ENTITIES.getLocalPart())) return toEntities(value);
        if(local.equalsIgnoreCase(Constants.XSD_ENTITY.getLocalPart())) return toEntity(value);
        if(local.equalsIgnoreCase(Constants.XSD_FLOAT.getLocalPart())) return new Float(Caster.toDoubleValue(value));
        if(local.equalsIgnoreCase(Constants.XSD_HEXBIN.getLocalPart())) return Caster.toBinary(value);
        if(local.equalsIgnoreCase(Constants.XSD_ID.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_IDREF.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_IDREFS.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_INT.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_INTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_LANGUAGE.getLocalPart())) return toLanguage(value);
        if(local.equalsIgnoreCase(Constants.XSD_LONG.getLocalPart())) return Caster.toLong(value);
        if(local.equalsIgnoreCase(Constants.XSD_MONTH.getLocalPart())) return toMonth(value);
        if(local.equalsIgnoreCase(Constants.XSD_MONTHDAY.getLocalPart())) return toMonthDay(value);
        if(local.equalsIgnoreCase(Constants.XSD_NAME.getLocalPart())) return toName(value);
        if(local.equalsIgnoreCase(Constants.XSD_NCNAME.getLocalPart())) return toNCName(value);
        if(local.equalsIgnoreCase(Constants.XSD_NEGATIVEINTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_NMTOKEN.getLocalPart())) return toNMToken(value);
        if(local.equalsIgnoreCase(Constants.XSD_NMTOKENS.getLocalPart())) return toNMTokens(value);
        if(local.equalsIgnoreCase(Constants.XSD_NONNEGATIVEINTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_NONPOSITIVEINTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_NORMALIZEDSTRING.getLocalPart())) return Caster.toString(value);
        if(local.equalsIgnoreCase(Constants.XSD_POSITIVEINTEGER.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_QNAME.getLocalPart())) return toQName(value);
        if(local.equalsIgnoreCase(Constants.XSD_SCHEMA.getLocalPart())) return toQName(value);
        if(local.equalsIgnoreCase(Constants.XSD_SHORT.getLocalPart())) return Caster.toShort(value);
        if(local.equalsIgnoreCase(Constants.XSD_TIME.getLocalPart())) return DateCaster.toTime(tz,value);
        if(local.equalsIgnoreCase(Constants.XSD_TIMEINSTANT1999.getLocalPart())) return DateCaster.toTime(tz,value);
        if(local.equalsIgnoreCase(Constants.XSD_TIMEINSTANT2000.getLocalPart())) return DateCaster.toTime(tz,value);
        if(local.equalsIgnoreCase(Constants.XSD_TOKEN.getLocalPart())) return toToken(value);
        if(local.equalsIgnoreCase(Constants.XSD_UNSIGNEDBYTE.getLocalPart())) return Caster.toByte(value);
        if(local.equalsIgnoreCase(Constants.XSD_UNSIGNEDINT.getLocalPart())) return Caster.toInteger(value);
        if(local.equalsIgnoreCase(Constants.XSD_UNSIGNEDLONG.getLocalPart())) return Caster.toLong(value);
        if(local.equalsIgnoreCase(Constants.XSD_UNSIGNEDSHORT.getLocalPart())) return Caster.toShort(value);
        if(local.equalsIgnoreCase(Constants.XSD_YEAR.getLocalPart())) return toYear(value);
        if(local.equalsIgnoreCase(Constants.XSD_YEARMONTH.getLocalPart())) return toYearMonth(value);
        return _toDefinedType(tm, null,null,null, value, done);
    }

    private static ArrayList<Object> toArrayList(TypeMapping tm,Object value, Set<Object> done) throws PageException {
        Array arr = Caster.toArray(value);
        ArrayList<Object> al=new ArrayList<Object>();
        int len=arr.size();
        Object o;
        for(int i=0;i<len;i++) {
            o=arr.get(i+1,null);
            al.add(i,_toAxisType(tm,null,null,null,null,o,done));
        }
        return al;
    }
    
    private static Object[] toNativeArray(TypeMapping tm,Class targetClass,Object value, Set<Object> done) throws PageException {
        	Object[] objs = Caster.toNativeArray(value);
        	Object[] rtns;

        	Class<?> componentClass = null;
        	if(targetClass!=null) {
            	componentClass = targetClass.getComponentType();
            }
        	
            if(componentClass!=null) {
            	componentClass=toAxisTypeClass(componentClass);
            	rtns = (Object[]) java.lang.reflect.Array.newInstance(componentClass, objs.length);
            }
            else 
            	rtns = new Object[objs.length];
            
        	try{
    	        for(int i=0;i<objs.length;i++) {
    	        	rtns[i]=_toAxisType(tm,null,null,null,componentClass,objs[i],done);
    	        }
        	}
        	// just in case something goes wrong with typed array
        	catch(ArrayStoreException ase){
        		rtns = new Object[objs.length];
        		for(int i=0;i<objs.length;i++) {
    	        	rtns[i]=_toAxisType(tm,null,null,null,componentClass,objs[i],done);
    	        }
        	}
        	
	        return rtns;
        }

    private static Vector<Object> toVector(TypeMapping tm,Object value, Set<Object> done) throws PageException {
        Array arr = Caster.toArray(value);
        Vector<Object> v=new Vector<Object>();
        int len=arr.size();
        Object o;
        for(int i=0;i<len;i++) {
            o=arr.get(i+1,null);
            v.set(i,_toAxisType(tm,null,null,null,null,o,done));
        }
        return v;
	}

	public static Component toComponent(PageContext pc, Pojo pojo, String compPath , Component defaultValue) {
		try {
			Component cfc = pc.loadComponent(compPath);
			Property[] props = cfc.getProperties(false, true, false, false);
			PojoIterator it=new PojoIterator(pojo);
			// only when the same amount of properties
			if(props.length==it.size()) {
				Map<Collection.Key, Property> propMap = toMap(props);
				Property p;
				Pair<Collection.Key,Object> pair;
				ComponentScope scope = cfc.getComponentScope();
				while(it.hasNext()){
					pair=it.next();
					p=propMap.get(pair.getName());
					if(p==null) return defaultValue;
					Object val = null;
					try {
						val = Caster.castTo(pc, p.getType(), pair.getValue(), false);
					} catch (PageException e) { 	}
					
					// store in variables and this scope
					scope.setEL(pair.getName(), val);
					cfc.setEL(pair.getName(), val);
				}
				return cfc;
			}
		}
		catch (PageException e) {}
		return defaultValue;
	}

    private static Map<Collection.Key,Property> toMap(Property[] props) {
    	Map<Collection.Key,Property> map=new HashMap<Collection.Key, Property>();
		for(int i=0;i<props.length;i++){
			map.put(KeyImpl.init(props[i].getName()), props[i]);
		}
		return map;
	}

	public static Pojo toPojo(Pojo pojo, TypeMapping tm,TypeEntry typeEntry,QName type,Component comp, Set<Object> done) throws PageException {
    	PageContext pc = ThreadLocalPageContext.get(); 
	    try {
	    	return _toPojo(pc,pojo, tm,typeEntry,type, comp,done);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}
	
	private static Pojo _toPojo(PageContext pc, Pojo pojo, TypeMapping tm,TypeEntry typeEntry,QName type,Component comp, Set<Object> done) throws PageException {//print.ds();System.exit(0);
    	comp=ComponentSpecificAccess.toComponentSpecificAccess(Component.ACCESS_PRIVATE,comp);
		ComponentScope scope = comp.getComponentScope();
    	
		// create Pojo
		if(pojo==null) {
	    	try {
				pojo = (Pojo) ClassUtil.loadInstance(ComponentUtil.getComponentPropertiesClass(pc,comp));
			} catch (ClassException e) {
				throw Caster.toPageException(e);
			}
		}
    	
    	// initialize Pojo
		Property[] props=comp.getProperties(false, true, false, false);
		_initPojo(pc,typeEntry,type,pojo,props,scope,comp,tm,done);

    	return pojo;
    }
	
	public static Pojo toPojo(Pojo pojo, TypeMapping tm,TypeEntry typeEntry,QName type,Struct sct, Set<Object> done) throws PageException {
    	PageContext pc = ThreadLocalPageContext.get(); 
	    try {
	    	return _toPojo(pc,pojo, tm,typeEntry,type, sct,done);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	private static Pojo _toPojo(PageContext pc, Pojo pojo, TypeMapping tm,TypeEntry typeEntry,QName type,Struct sct, Set<Object> done) throws PageException {//print.ds();System.exit(0);
		if(pojo==null) {
			try {
				PhysicalClassLoader cl=(PhysicalClassLoader) pc.getConfig().getRPCClassLoader(false);
	    		pojo = (Pojo) ClassUtil.loadInstance(ComponentUtil.getStructPropertiesClass(pc,sct,cl));
			}
			catch (ClassException e) {
				throw Caster.toPageException(e);
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}
    	
    	// initialize
		List<Property> props=new ArrayList<Property>();
		Iterator<Entry<Key, Object>> it = sct.entryIterator();
		Entry<Key, Object> e;
		PropertyImpl p;
		while(it.hasNext()){
			e = it.next();
			p=new PropertyImpl();
			p.setAccess(Component.ACCESS_PUBLIC);
			p.setName(e.getKey().getString());
			p.setType(e.getValue()==null?"any":Caster.toTypeName(e.getValue())); 
			props.add(p);
		}
		
		_initPojo(pc,typeEntry,type,pojo,props.toArray(new Property[props.size()]),sct,null,tm,done);

    	return pojo;
    }
    
    private static void _initPojo(PageContext pc, TypeEntry typeEntry, QName type, Pojo pojo, Property[] props, Struct sct, Component comp, TypeMapping tm, Set<Object> done) throws PageException {
    	Property p;
    	Object v;
    	Collection.Key k;
		CFMLExpressionInterpreter interpreter = new CFMLExpressionInterpreter(false);
		
		
		
    	for(int i=0;i<props.length;i++){
    		p=props[i];
    		k=Caster.toKey(p.getName());
    	// value
    		v=sct.get(k,null);
    		if(v==null && comp!=null)v=comp.get(k, null);
    		
    	// default
    		
    		if(v!=null)v=Caster.castTo(pc, p.getType(), v, false);
    		else{
	    		if(!StringUtil.isEmpty(p.getDefault())){
	    			try {
	    				v=Caster.castTo(pc, p.getType(), p.getDefault(), false);
	    				
	    			}
	        		catch(PageException pe) {
	        			try {
	        				v=interpreter.interpret(pc, p.getDefault());
	        				v=Caster.castTo(pc, p.getType(), v, false);
	        			}
	            		catch(PageException pe2) {
	        				throw new ExpressionException("can not use default value ["+p.getDefault()+"] for property ["+p.getName()+"] with type ["+p.getType()+"]");
	            		}
	        		}
	    		}
    		}
    		
    	// set or throw
    		if(v==null) {
    			if(p.isRequired())throw new ExpressionException("required property ["+p.getName()+"] is not defined");
    		}
    		else {
    			TypeEntry childTE=null;
    			QName childT=null;
    			if(typeEntry!=null) {
	    			childTE = AxisUtil.getContainedElement(typeEntry,p.getName(),null);
	    			if(childTE!=null) childT=childTE.getQName();
	    			
    			}
    			Reflector.callSetter(pojo, p.getName().toLowerCase(), _toAxisType(tm,null,childTE,childT,null,v,done));	
    		}
    	}
	}

	private static QueryBean toQueryBean(TypeMapping tm,Object value, Set<Object> done) throws PageException {
    	Query query = Caster.toQuery(value);
		int recordcount=query.getRecordcount();
        String[] columnList = query.getColumns();
        QueryColumn[] columns=new QueryColumn[columnList.length];
        Object[][] data = new Object[recordcount][columnList.length];
        
        for(int i=0;i<columnList.length;i++) {
        	columns[i]=query.getColumn(columnList[i]);
        }
        
        int row;
        for(row=1;row<=recordcount;row++) {
            for(int i=0;i<columns.length;i++) {
            	data[row-1][i]=_toAxisType(tm,null,null,null,null,columns[i].get(row,null),done);
            }
        }
    	
    	QueryBean qb = new QueryBean();
    	qb.setColumnList(columnList);
    	qb.setData(data);
    	return qb;
    	
    }
    

    
    private static Map<String,Object> toMap(TypeMapping tm,Object value, Set<Object> done) throws PageException {
        Struct src = Caster.toStruct(value);
        
        HashMap<String,Object> trg=new HashMap<String,Object>();
        Iterator<Entry<Key, Object>> it = src.entryIterator();
        Entry<Key, Object> e;
        while(it.hasNext()) {
        	e = it.next();
            trg.put(e.getKey().getString(),_toAxisType(tm,null,null,null,null,e.getValue(),done));

        }
        return trg;
        
    }

    private static Object _toDefinedType(TypeMapping tm,TypeEntry typeEntry,QName type,Class targetClass,Object value,Set<Object> done) throws PageException {
    	
    	// Date
    	if(value instanceof Date) {// not set to Decision.isDate(value)
        	return new Date(((Date)value).getTime());
    	}
    	
    	
    	
    	
    	Class clazz=type==null?null:((org.apache.axis.encoding.TypeMapping)tm).getClassForQName(type);
    	// Pojo
    	if(clazz!=null && Reflector.isInstaneOf(clazz,Pojo.class)) {
    		Pojo pojo;
    		try{
    			pojo=(Pojo) ClassUtil.loadInstance(clazz);
    		}
    		catch(Throwable t){
    			ExceptionUtil.rethrowIfNecessary(t);
    			throw Caster.toPageException(t);
    		}
    		// Struct
            if(Decision.isStruct(value)) {
            	
            	if(value instanceof Component) 
            		return toPojo(pojo,tm,typeEntry,type,(Component)value,done);
            	return toPojo(pojo,tm,typeEntry,type,Caster.toStruct(value),done);
            }
    	}

    	// No Mapping found
    	
    	
    	// Array
    	if(Decision.isArray(value) && !(value instanceof Argument)) {
    		if(value instanceof byte[]) return value;
    		return toNativeArray(tm,targetClass,value,done);
    	}
    	// Struct
        if(Decision.isStruct(value)) {
        	if(value instanceof Component) {
        		Object pojo= toPojo(null,tm,null,null,(Component)value,done);
        		try	{
        			if(type==null || type.getLocalPart().equals("anyType")) {
        				type= new QName(getRequestDefaultNameSpace(),pojo.getClass().getName());
        				//type= new QName(getRequestNameSpace(),pojo.getClass().getName());
        				//print.ds("missing type for "+pojo.getClass().getName());
        			}
        			TypeMappingUtil.registerBeanTypeMapping(tm, pojo.getClass(), type);
	        		
        		}
        		catch(Throwable fault){
        			ExceptionUtil.rethrowIfNecessary(fault);
        			throw Caster.toPageException(fault);
        		}
        		return pojo;
        	}
        	/*if(type!=null && !type.getLocalPart().equals("anyType")) {
        		Object pojo= toPojo(null,tm,Caster.toStruct(value),targetClass,done);
        		
        		//Map<String, Object> map = toMap(tm,value,targetClass,done);
	    		//TypeMappingUtil.registerMapTypeMapping(tm, map.getClass(), type);
    			TypeMappingUtil.registerBeanTypeMapping(tm, pojo.getClass(), type);
	    		return pojo;
        	}*/
        	return toMap(tm,value,done);
        	
        	
        }
        // Query
        if(Decision.isQuery(value)) return toQueryBean(tm,value,done);
        // Other
        return value;
    }
    
    public static Class toAxisTypeClass(Class clazz) {
    	if(clazz.isArray()) {
    		return ClassUtil.toArrayClass(toAxisTypeClass(clazz.getComponentType()));
    	}
    	
    	if(Query.class==clazz) return QueryBean.class;
    	if(Array.class==clazz) return Object[].class;
        if(Struct.class==clazz) return Map.class;
        //if(Struct[].class==clazz) return Map[].class;
        //if(Query[].class==clazz) return QueryBean[].class;
        
        return clazz;
    }
    
    private static Object toURI(Object value) throws PageException {
        if(value instanceof URI) return value;
        if(value instanceof java.net.URI) return value;
        try {
            return new URI(Caster.toString(value));
        } catch (MalformedURIException e) {
            throw Caster.toPageException(e);
        }
    }

    private static Token toToken(Object value) throws PageException {
        if(value instanceof Token) return (Token) value;
        return new Token(Caster.toString(value));
    }
    
    private static QName toQName(Object value) throws PageException {
        if(value instanceof QName) return (QName) value;
        return new QName(Caster.toString(value));
    }

    private static NMTokens toNMTokens(Object value) throws PageException {
        if(value instanceof NMTokens) return (NMTokens) value;
        return new NMTokens(Caster.toString(value));
    }
    
    private static NMToken toNMToken(Object value) throws PageException {
        if(value instanceof NMToken) return (NMToken) value;
        return new NMToken(Caster.toString(value));
    }
    private static NCName toNCName(Object value) throws PageException {
        if(value instanceof NCName) return (NCName) value;
        return new NCName(Caster.toString(value));
    }

    private static Name toName(Object value) throws PageException {
        if(value instanceof Name) return (Name) value;
        return new Name(Caster.toString(value));
    }

    private static Language toLanguage(Object value) throws PageException {
        if(value instanceof Language) return (Language) value;
        return new Language(Caster.toString(value));
    }

    private static Entities toEntities(Object value) throws PageException {
        if(value instanceof Entities) return (Entities) value;
        return new Entities(Caster.toString(value));
    }
    
    private static Entity toEntity(Object value) throws PageException {
        if(value instanceof Entity) return (Entity) value;
        return new Entity(Caster.toString(value));
    }

    private static Day toDay(Object value) throws PageException {
        if(value instanceof Day) return (Day) value;
        if(Decision.isDateSimple(value,false)) {
            return new Day(Caster.toDate(value,null).getDate());
        }
        
        try {
            return new Day(Caster.toIntValue(value));
        } 
        catch (Exception e) {
            try {
                return new Day(Caster.toString(value));
            } catch (NumberFormatException nfe) {
                throw Caster.toPageException(nfe);
            } 
            catch (ExpressionException ee) {
                throw ee;
            }
        }
    }

    private static Year toYear(Object value) throws PageException {
        if(value instanceof Year) return (Year) value;
        if(Decision.isDateSimple(value,false)) {
            return new Year(Caster.toDate(value,null).getYear());
        }
        try {
            return new Year(Caster.toIntValue(value));
        } 
        catch (Exception e) {
            try {
                return new Year(Caster.toString(value));
            } catch (NumberFormatException nfe) {
                throw Caster.toPageException(nfe);
            } 
            catch (ExpressionException ee) {
                throw ee;
            }
        }
    }

    private static Month toMonth(Object value) throws PageException {
        if(value instanceof Month) return (Month) value;
        if(Decision.isDateSimple(value,false)) {
            return new Month(Caster.toDate(value,null).getMonth());
        }
        try {
            return new Month(Caster.toIntValue(value));
        } 
        catch (Exception e) {
            try {
                return new Month(Caster.toString(value));
            } catch (NumberFormatException nfe) {
                throw Caster.toPageException(nfe);
            } 
            catch (ExpressionException ee) {
                throw ee;
            }
        }
    }

    private static YearMonth toYearMonth(Object value) throws PageException {
        if(value instanceof YearMonth) return (YearMonth) value;
        if(Decision.isDateSimple(value,false)) {
            DateTime dt = Caster.toDate(value,null);
            return new YearMonth(dt.getYear(),dt.getMonth());
        }
        
        try {
            return new YearMonth(Caster.toString(value));
        } catch (NumberFormatException nfe) {
            throw Caster.toPageException(nfe);
        } 
        catch (ExpressionException ee) {
            throw ee;
        }
    }

    private static MonthDay toMonthDay(Object value) throws PageException {
        if(value instanceof MonthDay) return (MonthDay) value;
        if(Decision.isDateSimple(value,false)) {
            DateTime dt = Caster.toDate(value,null);
            return new MonthDay(dt.getMonth(),dt.getDate());
        }
        
        try {
            return new MonthDay(Caster.toString(value));
        } catch (NumberFormatException nfe) {
            throw Caster.toPageException(nfe);
        } 
        catch (ExpressionException ee) {
            throw ee;
        }
    }

    private static Duration toDuration(Object value) throws PageException, IllegalArgumentException {
        if(value instanceof Duration) return (Duration) value;
        try {
            TimeSpan ts = Caster.toTimespan(value);
            return new Duration(true, 0, 0, ts.getDay(), ts.getHour(), ts.getMinute(), ts.getSecond());
        } catch (PageException e) {
            return new Duration(Caster.toString(value));
        }
    }
    

    public static Object toLuceeType(PageContext pc, Object value) throws PageException {
    	return toLuceeType(pc, null, value);
    }
    

    public static Object toLuceeType(PageContext pc, String customType, Object value) throws PageException {
    	pc=ThreadLocalPageContext.get(pc);
    	if(pc!=null && value instanceof Pojo) {
    		if(!StringUtil.isEmpty(customType)){
    			Component cfc = toComponent(pc, (Pojo)value,customType, null);
    			if(cfc!=null) return cfc;
    		}
    		/*
    		// try package/class name as component name
    		String compPath=value.getClass().getName();
    		Component cfc = toComponent(pc, (Pojo)value, compPath, null);
    		if(cfc!=null) return cfc;
    		
    		// try class name as component name
    		compPath=ListUtil.last(compPath, '.');
    		cfc = toComponent(pc, (Pojo)value, compPath, null);
    		if(cfc!=null) return cfc;
    		*/
        }
        if(value instanceof Date || value instanceof Calendar) {// do not change to caster.isDate
        	return Caster.toDate(value,null);
        }
        if(value instanceof Object[]) {
        	Object[] arr=(Object[]) value;
        	if(!ArrayUtil.isEmpty(arr)){
        		boolean allTheSame=true;
        		// byte
        		if(arr[0] instanceof Byte){
        			for(int i=1;i<arr.length;i++){
        				if(!(arr[i] instanceof Byte)){
        					allTheSame=false;
        					break;
        				}
        			}
        			if(allTheSame){
        				byte[] bytes=new byte[arr.length];
        				for(int i=0;i<arr.length;i++){
            				bytes[i]=Caster.toByteValue(arr[i]);
            			}
        				return bytes;
        			}
        		}
        	}
        }
        if(value instanceof Byte[]) {
        	Byte[] arr=(Byte[]) value;
        	if(!ArrayUtil.isEmpty(arr)){
				byte[] bytes=new byte[arr.length];
				for(int i=0;i<arr.length;i++){
    				bytes[i]=arr[i].byteValue();
    			}
				return bytes;
        	}
        }
        if(value instanceof byte[]) {
        	return value;
        }
        if(Decision.isArray(value)) {
        	
            Array a = Caster.toArray(value);
            int len=a.size();
            Object o;
            String ct;
            for(int i=1;i<=len;i++) {
                o=a.get(i,null);
                if(o!=null) {
                	ct=customType!=null && customType.endsWith("[]")?customType.substring(0,customType.length()-2):null;
                	a.setEL(i,toLuceeType(pc,ct,o));
                }
            }
            return a;
        }
        if(value instanceof Map) {
        	Struct sct = new StructImpl();
            Iterator it=((Map)value).entrySet().iterator();
            Map.Entry entry;
            while(it.hasNext()) {
                entry=(Entry) it.next();
                sct.setEL(Caster.toString(entry.getKey()),toLuceeType(pc,null,entry.getValue()));
            }
            return sct;
        	
        	
        	//return StructUtil.copyToStruct((Map)value);
        }
        if(isQueryBean(value)) {
        	QueryBean qb = (QueryBean) value;
            String[] strColumns = qb.getColumnList();
            Object[][] data = qb.getData();
            int recorcount=data.length;
            Query qry=new QueryImpl(strColumns,recorcount,"QueryBean");
            QueryColumn[] columns=new QueryColumn[strColumns.length];
            for(int i=0;i<columns.length;i++) {
            	columns[i]=qry.getColumn(strColumns[i]);
            }
            
            int row;
            for(row=1;row<=recorcount;row++) {
            	for(int i=0;i<columns.length;i++) {
            		columns[i].set(row,toLuceeType(pc,null,data[row-1][i]));
                }
            }
            return qry;
        }
        if(Decision.isQuery(value)) {
            Query q = Caster.toQuery(value);
            int recorcount=q.getRecordcount();
            String[] strColumns = q.getColumns();
            
            QueryColumn col;
            int row;
            for(int i=0;i<strColumns.length;i++) {
                col=q.getColumn(strColumns[i]);
                for(row=1;row<=recorcount;row++) {
                    col.set(row,toLuceeType(pc,null,col.get(row,null)));
                }
            }
            return q;
        }
        return value;
    }

	private static boolean isQueryBean(Object value) {
		return (value instanceof QueryBean);
	}

	public static QName toComponentType(QName qName, QName defaultValue) {
		String lp = qName.getLocalPart();
		String uri = qName.getNamespaceURI();
		if(lp.startsWith("ArrayOf"))
			return new QName(uri, lp.substring(7));
		return defaultValue;
	}

	public static String getRequestNameSpace() {
		String rawURL = ReqRspUtil.getRequestURL(ThreadLocalPageContext.get().getHttpServletRequest(),false);
		String urlPath ="";
		try {
			urlPath = new java.net.URL(rawURL).getPath();
		}
		catch (MalformedURLException e) {}
		String pathWithoutContext = urlPath.replaceFirst("/[^/]*", "");
			
		
		return lucee.runtime.config.Constants.WEBSERVICE_NAMESPACE_URI + pathWithoutContext.toLowerCase();
	}
	public static String getRequestDefaultNameSpace() {
		return lucee.runtime.config.Constants.WEBSERVICE_NAMESPACE_URI;
	}
	
}