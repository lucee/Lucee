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
package lucee.commons.io.res.type.cfml;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.commons.io.res.util.ResourceLockImpl;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;

public class CFMLResourceProvider implements ResourceProviderPro {

	private static final Object[] ZERO_ARGS = new Object[0];
	
	private int lockTimeout=20000;
	private final ResourceLockImpl lock=new ResourceLockImpl(lockTimeout,false);
	private String scheme;
	private Map args;
	//private ResourceProvider provider;
	private Resources resources;

	private String componentPath;

	private Component component;

	private boolean useStreams=false;
 
	

	@Override
	public ResourceProvider init(String scheme, Map args) {
		this.scheme=scheme;
		this.args=args;
		
		// CFC Path
		componentPath=Caster.toString(args.get("cfc"),null);
		if(StringUtil.isEmpty(componentPath,true))
			componentPath=Caster.toString(args.get("component"),null);
		if(StringUtil.isEmpty(componentPath,true))
			componentPath=Caster.toString(args.get("class"),null);
		
		// use Streams for data
		Boolean _useStreams = Caster.toBoolean(args.get("use-streams"),null);
		if(_useStreams==null)_useStreams = Caster.toBoolean(args.get("usestreams"),null);
		
		if(_useStreams!=null)useStreams=_useStreams.booleanValue();
		
		return this;
	}

	@Override
	public Resource getResource(String path) {
		path=ResourceUtil.removeScheme(scheme,path);
		path=ResourceUtil.prettifyPath(path);
		if(!StringUtil.startsWith(path,'/'))path="/"+path;
		return callResourceRTE(null, null, "getResource", new Object[]{path},false);
	}

	@Override
	public String getScheme() {
		return scheme;
	}

	@Override
	public Map getArguments() {
		return args;
	}

	@Override
	public void setResources(Resources resources) {
		this.resources=resources;
	}


	@Override
	public boolean isCaseSensitive() {
		return callbooleanRTE(null,null, "isCaseSensitive", ZERO_ARGS);
	}

	@Override
	public boolean isModeSupported() {
		return callbooleanRTE(null,null, "isModeSupported", ZERO_ARGS);
	}

	@Override
	public boolean isAttributesSupported() {
		return callbooleanRTE(null,null, "isAttributesSupported", ZERO_ARGS);
	}
	
	public int getLockTimeout() {
		return lockTimeout;
	}
	
	
	
	@Override
	public void lock(Resource res) throws IOException {
		lock.lock(res);
	}

	@Override
	public void unlock(Resource res) {
		lock.unlock(res);
	}

	@Override
	public void read(Resource res) throws IOException {
		lock.read(res);
	}

	public boolean isUseStreams() {
		return useStreams;
	}
	
	
	Resource callResourceRTE(PageContext pc,Component component,String methodName, Object[] args, boolean allowNull) {
		pc = ThreadLocalPageContext.get(pc);
		try {
			Object res = call(pc,getCFC(pc,component), methodName, args);
			if(allowNull && res==null) return null;
			return new CFMLResource(this,Caster.toComponent(res));
		} 
		catch (PageException pe) {pe.printStackTrace();
			throw new PageRuntimeException(pe);
		} 
	}

	Resource[] callResourceArrayRTE(PageContext pc,Component component,String methodName, Object[] args) {
		pc = ThreadLocalPageContext.get(pc);
		try {
			Array arr = Caster.toArray(call(pc,getCFC(pc,component), methodName, args));
			Iterator<Object> it = arr.valueIterator();
			CFMLResource[] resources=new CFMLResource[arr.size()];
			int index=0;
			while(it.hasNext()){
				resources[index++]=new CFMLResource(this,Caster.toComponent(it.next()));
			}
			return resources;
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
	

	int callintRTE(PageContext pc,Component component,String methodName, Object[] args) {
		try {
			return callint(pc,component, methodName, args);
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
	int callint(PageContext pc,Component component,String methodName, Object[] args) throws PageException {
		return Caster.toIntValue(call(pc,component,methodName, args));
	}

	long calllongRTE(PageContext pc,Component component,String methodName, Object[] args) {
		try {
			return calllong(pc,component, methodName, args);
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
	long calllong(PageContext pc,Component component,String methodName, Object[] args) throws PageException {
		return Caster.toLongValue(call(pc,component,methodName, args));
	}
	
	boolean callbooleanRTE(PageContext pc,Component component,String methodName, Object[] args) {
		try {
			return callboolean(pc,component, methodName, args);
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}
	boolean callboolean(PageContext pc,Component component,String methodName, Object[] args) throws PageException {
		return Caster.toBooleanValue(call(pc,component,methodName, args));
	}

	String callStringRTE(PageContext pc,Component component,String methodName, Object[] args) {
		try {
			return Caster.toString(call(pc,component,methodName, args));
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	String callString(PageContext pc,Component component,String methodName, Object[] args) throws PageException {
		return Caster.toString(call(pc,component,methodName, args));
	}

	Object callRTE(PageContext pc,Component component,String methodName, Object[] args) {
		try {
			return call(pc,component,methodName, args);
		} 
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	Object call(PageContext pc,Component component,String methodName, Object[] args) throws PageException {
		pc = ThreadLocalPageContext.get(pc);
		return getCFC(pc, component).call(pc, methodName, args);
	}

	private Component getCFC(PageContext pc,Component component) throws PageException {
		if(component!=null) return component;
		if(this.component!=null) return this.component;
		
		if(StringUtil.isEmpty(componentPath,true))throw new ApplicationException("you need to define the argument [component] for the [CFMLResourceProvider]");
		componentPath=componentPath.trim();
		this.component=pc.loadComponent(componentPath);
		call(pc, this.component, "init", new Object[]{scheme,Caster.toStruct(args)});
		
		return this.component;
	}

	@Override
	public char getSeparator() {
		try {
			String str=callStringRTE(null,component,"getSeparator",ZERO_ARGS);
			if(StringUtil.length(str,true)==1) return str.charAt(0);
		}
		catch(Throwable t){
			// fallback to default "/"
		}
		return '/';
	}

}