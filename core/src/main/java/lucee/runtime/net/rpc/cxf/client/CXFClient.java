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
package lucee.runtime.net.rpc.cxf.client;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.rpc.Call;
import javax.xml.soap.SOAPHeaderElement;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;


public final class CXFClient implements WSClient {
	

	public CXFClient(String strWsdlUrl, String username, String password, ProxyData proxyData) {
		
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String castToString() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String castToString(String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double castToDoubleValue() throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(String str) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(double d) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<Key> keyIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Object> valueIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addHeader(SOAPHeaderElement header) throws PageException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Call getLastCall() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object callWithNamedValues(Config config, Key methodName, Struct arguments) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct arguments) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}
	
}

/*
final class CXFClient extends WSClient {
	
	private static final long serialVersionUID = 7329491172855348435L;
	
	private Client client;
	
	private URL wsdlUrl;

	public CXFClient(String strWsdlUrl, String username, String password, ProxyData proxyData) throws PageException {
		DynamicClientFactory dcf=DynamicClientFactory.newInstance();
		
		
		
		//JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
		try {
			this.wsdlUrl=HTTPUtil.toURL(strWsdlUrl, true);
		}
		catch (MalformedURLException e) {
			throw Caster.toPageException(e);
		}
		client = dcf.createClient(this.wsdlUrl);
		
		if(!Util.isEmpty(username)) {
			if(password==null)password="";
			// TODO set username/password
			
	        
		}
		// TODO proxyData
	}
	

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable box = new DumpTable("webservice","#99cccc","#ccffff","#000000");
        box.setTitle("Web Service (CXF)");
        DumpTable functions = box;
        
        
		
		BindingInfo bi = client.getEndpoint().getBinding().getBindingInfo();
		
		String doc=bi.getDocumentation();
		box.setComment(StringUtil.isEmpty(doc)?wsdlUrl.toExternalForm():wsdlUrl.toExternalForm()+"\n"+doc);
        
		Collection<BindingOperationInfo> ops = bi.getOperations();
		Iterator<BindingOperationInfo> it = ops.iterator();
		OperationInfo oi;
		BindingOperationInfo boi;
		String funcName;
		MessageInfo in,out;
		while(it.hasNext()){
			boi=it.next();
			oi=boi.getOperationInfo(); 
			funcName=oi.getName().getLocalPart();
			doc=oi.getDocumentation();
			if(oi.isUnwrapped() || (oi.isUnwrappedCapable() && (oi=oi.getUnwrappedOperation())!=null)) {
				in = oi.getInput();
				out = oi.getOutput();
				
				DumpTable table = new DumpTable("#99cccc","#ccffff","#000000");
				table.setTitle(funcName);
				if(!StringUtil.isEmpty(doc))table.setComment(doc);
				DumpTable attributes = new DumpTable("#99cccc","#ccffff","#000000");
		    	attributes.appendRow(3, new SimpleDumpData("Name"), new SimpleDumpData("Type"));

				// attributes/input
				List<MessagePartInfo> parts = in.getMessageParts();
				Iterator<MessagePartInfo> itt = parts.iterator();
				while(itt.hasNext()){
					MessagePartInfo mpi = itt.next();
					attributes.appendRow(0,new SimpleDumpData(mpi.getName().getLocalPart()),new SimpleDumpData(toLuceeType(mpi.getTypeQName(),mpi.getTypeClass())));
				}
				
				// return value/output
				String rtn="";
				parts = out.getMessageParts();
				itt = parts.iterator();
				while(itt.hasNext()){
					MessagePartInfo mpi = itt.next();
					rtn=toLuceeType(mpi.getTypeQName(),mpi.getTypeClass());
				}
				
				//if(!StringUtil.isEmpty(doc))table.appendRow(1,new SimpleDumpData("hint"),new SimpleDumpData(doc));
		        table.appendRow(1,new SimpleDumpData("arguments"),attributes);
		        table.appendRow(1,new SimpleDumpData("return type"),new SimpleDumpData(rtn));
		        
				
				
				functions.appendRow(1,new SimpleDumpData(funcName),table);
			}
			else functions.appendRow(1,new SimpleDumpData(funcName));
		
		}
		
		//box.appendRow(1,new SimpleDumpData(""),functions);
	    return box;
	}

	private String toLuceeType(QName type, Class<?> typeClass) {
		
    	String strType = type.getLocalPart();
    	String lcStrType=strType.toLowerCase();
    	
    	if(lcStrType.startsWith("array")) return "array";
    	else if(lcStrType.equals("map")) return "struct";
    	else if(lcStrType.startsWith("query")) return "query";
    	else if(lcStrType.equals("double")) return "numeric";
    	else if(lcStrType.startsWith("any")) return "any";
    	else if(lcStrType.equals("date")) return "date";
        
    	
    	return strType+" ("+typeClass.getName()+")";
	}


	@Override
	public Iterator<String> keysAsStringIterator() {
		return new KeyAsStringIterator(keyIterator());
	}
	
	@Override
	public Iterator<Key> keyIterator() {
		Collection<BindingOperationInfo> ops = client.getEndpoint().getBinding().getBindingInfo().getOperations();
		Iterator<BindingOperationInfo> it = ops.iterator();
		List<Key> keys=new ArrayList<Key>();
		while(it.hasNext()){
			keys.add(KeyImpl.init(it.next().getOperationInfo().getName().getLocalPart()));
		}
		return keys.iterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return new ObjectsIterator(keyIterator(),this);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return new ObjectsEntryIterator(keyIterator(), this);
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
        return call(pc,KeyImpl.init("get"+key.getString()), ArrayUtil.OBJECT_EMPTY);
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		try {
            return call(pc,KeyImpl.init("get"+StringUtil.ucFirst(key.getString())), ArrayUtil.OBJECT_EMPTY);
        } catch (PageException e) {
            return defaultValue;
        }
	}
	
	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
        return call(pc,KeyImpl.init("set"+propertyName.getString()), new Object[]{value});
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		try {
            return call(pc,KeyImpl.init("set"+propertyName.getString()), new Object[]{value});
        } catch (PageException e) {
            return null;
        }
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		Collection<BindingOperationInfo> ops = client.getEndpoint().getBinding().getBindingInfo().getOperations();
		Iterator<BindingOperationInfo> it = ops.iterator();
		String name=null;
		BindingOperationInfo boi;
		OperationInfo oi;
		while(it.hasNext()){
			boi = it.next();
			oi = boi.getOperationInfo();
			name=oi.getName().getLocalPart();
			if(name.equalsIgnoreCase(methodName.getString())) {
				break;
			}
			name=null;
		}
		if(name == null)
			throw new RPCException("Cannot locate operation " + methodName + " in webservice " + wsdlUrl);
		try{
			Object[] rtn = client.invoke(name,new Object[]{});
			if(rtn.length==1)return toCFML(pc,rtn[0]);
			return toCFML(pc,rtn);
		}
		catch(Exception e){
			throw Caster.toPageException(e);
		}
	}


	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws ApplicationException {
		// TODO impl
		throw new ApplicationException("not implemented yet");
	} 
	@Override
	public Object callWithNamedValues(Config config, Key methodName, Struct arguments) throws PageException {
		// TODO Auto-generated method stub
		throw new ApplicationException("not implemented yet");
	}
	
	

	@Override
    public String castToString() throws ExpressionException {
        throw new RPCException("can't cast Webservice to a string");
    }

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

    @Override
    public boolean castToBooleanValue() throws ExpressionException {
        throw new RPCException("can't cast Webservice to a boolean");
    }
    
    @Override
    public Boolean castToBoolean(Boolean defaultValue) {
        return defaultValue;
    }

    @Override
    public double castToDoubleValue() throws ExpressionException {
        throw new RPCException("can't cast Webservice to a number");
    }
    
    @Override
    public double castToDoubleValue(double defaultValue) {
        return defaultValue;
    }

    @Override
    public DateTime castToDateTime() throws RPCException {
        throw new RPCException("can't cast Webservice to a Date Object");
    }
    
    @Override
    public DateTime castToDateTime(DateTime defaultValue) {
        return defaultValue;
    }

	@Override
	public int compareTo(boolean b) throws ExpressionException {
		throw new ExpressionException("can't compare Webservice Object with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Webservice Object with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Webservice Object with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Webservice Object with a String");
	}


	@Override
	public void addHeader(SOAPHeaderElement header) throws ApplicationException {
		throw new ApplicationException("not supported with CXF Client");
	}


	@Override
	public Call getLastCall() throws ApplicationException {
		throw new ApplicationException("not supported with CXF Client");
	}
	


	private Object toCFML(PageContext pc,Object value) throws PageException {
		
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
            for(int i=1;i<=len;i++) {
                o=a.get(i,null);
                if(o!=null)a.setEL(i,toCFML(pc,o));
            }
            return a;
        }
        if(value instanceof Map) {
        	Struct sct = new StructImpl();
            Iterator it=((Map)value).entrySet().iterator();
            Map.Entry entry;
            while(it.hasNext()) {
                entry=(Entry) it.next();
                sct.setEL(Caster.toString(entry.getKey()),toCFML(pc,entry.getValue()));
            }
            return sct;
        }

        Class<? extends Object> clazz = value.getClass();
        String name=ListUtil.last(clazz.getName(), '.');
        
        if(name.startsWith("ArrayOf")) {
        	MethodInstance mi = Reflector.getGetterEL(clazz, name.substring(7));
        	try {
				return toCFML(pc, mi.invoke(value));
			}
			catch (Exception e) {
				throw Caster.toPageException(e);
			}
        }
        
		return value;
	}
}*/