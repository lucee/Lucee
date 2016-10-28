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
package lucee.runtime;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lucee.commons.collection.HashMapPro;
import lucee.commons.collection.MapFactory;
import lucee.commons.collection.MapPro;
import lucee.commons.digest.Hash;
import lucee.commons.io.DevNullOutputStream;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.component.AbstractFinal;
import lucee.runtime.component.ComponentLoader;
import lucee.runtime.component.DataMember;
import lucee.runtime.component.Member;
import lucee.runtime.component.MetaDataSoftReference;
import lucee.runtime.component.MetadataUtil;
import lucee.runtime.component.Property;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.debug.DebugEntryTemplate;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpRow;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.DumpUtil;
import lucee.runtime.dump.SimpleDumpData;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.dynamicEvaluation.EvaluateComponent;
import lucee.runtime.functions.system.ContractPath;
import lucee.runtime.interpreter.CFMLExpressionInterpreter;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.op.Operator;
import lucee.runtime.op.ThreadLocalDuplication;
import lucee.runtime.op.date.DateCaster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFGSProperty;
import lucee.runtime.type.UDFImpl;
import lucee.runtime.type.UDFPlus;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.type.UDFPropertiesBase;
import lucee.runtime.type.cfc.ComponentEntryIterator;
import lucee.runtime.type.cfc.ComponentValueIterator;
import lucee.runtime.type.comparator.ArrayOfStructComparator;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.it.ComponentIterator;
import lucee.runtime.type.it.StringIterator;
import lucee.runtime.type.scope.Argument;
import lucee.runtime.type.scope.ArgumentImpl;
import lucee.runtime.type.scope.ArgumentIntKey;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.type.util.PropertyFactory;
import lucee.runtime.type.util.StructSupport;
import lucee.runtime.type.util.StructUtil;
import lucee.runtime.type.util.UDFUtil;

/**
 * %**%
 * MUST add handling for new attributes (style, namespace, serviceportname, porttypename, wsdlfile, bindingname, and output)
 */ 
public final class ComponentImpl extends StructSupport implements Externalizable,Component,coldfusion.runtime.TemplateProxy {
	private static final long serialVersionUID = -245618330485511484L; // do not change this


	private static final Interface[] EMPTY = new Interface[0];


	/*
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * Any change here must be changed in the method writeExternal,readExternal as well
	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * */
	ComponentProperties properties;
	private MapPro<Key,Member> _data;
    private MapPro<Key,UDF> _udfs;

	ComponentImpl top=this;
    ComponentImpl base;
    private PageSource pageSource;
    private ComponentScope scope;
    
    // for all the same
    private int dataMemberDefaultAccess;
	//private final Boolean _triggerDataMember=null;
    	
	// state control of component
	boolean isInit=false;

	//private AbstractCollection abstrCollection;


	private boolean useShadow;
	private boolean entity;
	boolean afterConstructor;
	//private Map<Key,UDF> constructorUDFs;
	private boolean loaded;
	private boolean hasInjectedFunctions;
	private boolean isExtended; // is this component extended by a other component?


	StaticScope _static;


	boolean insideStaticConstr;


	private AbstractFinal absFin;
	
    /**
     * Constructor of the Component, USED ONLY FOR DESERIALIZE
     */
	 public ComponentImpl() {
	 }

	/**
	 * constructor of the class
	 * @param componentPage
	 * @param output
	 * @param _synchronized
	 * @param extend
	 * @param implement
	 * @param hint
	 * @param dspName
	 * @param callPath
	 * @param realPath
	 * @param style
	 * @param persistent
	 * @param accessors
	 * @param modifier
	 * @param meta
	 * @throws ApplicationException
	 */
    public ComponentImpl(ComponentPageImpl componentPage,Boolean output,boolean _synchronized, 
    		String extend, String implement, String hint, String dspName,String callPath, boolean realPath, 
    		String style,boolean persistent,boolean accessors,int modifier,boolean isExtended,StructImpl meta) throws ApplicationException {
    	this.properties=new ComponentProperties(dspName,extend.trim(),implement,hint,output,callPath,realPath,_synchronized,null,persistent,accessors,modifier,meta);
    	//this.componentPage=componentPage instanceof ComponentPageProxy?componentPage:PageProxy.toProxy(componentPage);
    	this.pageSource=componentPage.getPageSource();
    	//if(modifier!=0)
    	if(!StringUtil.isEmpty(style) && !"rpc".equals(style))
    		throw new ApplicationException("style ["+style+"] is not supported, only the following styles are supported: [rpc]");
    	this.isExtended=isExtended;
    }
    

    @Override

    public Collection duplicate(boolean deepCopy) {
    	ComponentImpl top= _duplicate(deepCopy,true);
    	setTop(top,top);
    	
		
		return top;
    }
	
    
    
    
    private ComponentImpl _duplicate( boolean deepCopy, boolean isTop) {
    	ComponentImpl trg=new ComponentImpl();
    	boolean inside=ThreadLocalDuplication.set(this, trg);
    	try{
			// attributes
	    	trg.pageSource=pageSource;
	        //trg._triggerDataMember=_triggerDataMember;
	        trg.useShadow=useShadow;
	        trg._static=_static;
	        trg.entity=entity;
	        trg.hasInjectedFunctions=hasInjectedFunctions;
	        trg.isExtended=isExtended;
	        trg.afterConstructor=afterConstructor;
	        trg.dataMemberDefaultAccess=dataMemberDefaultAccess;
			trg.properties=properties.duplicate();
			trg.isInit=isInit;
			trg.absFin=absFin;
	    	
			boolean useShadow=scope instanceof ComponentScopeShadow;
			if(!useShadow)trg.scope=new ComponentScopeThis(trg);
			
	    	if(base!=null){
				trg.base=base._duplicate(deepCopy,false);
				
				trg._data=trg.base._data;
				trg._udfs=duplicateUTFMap(this,trg, _udfs,new HashMapPro<Key,UDF>(trg.base._udfs));

	    		if(useShadow) trg.scope=new ComponentScopeShadow(trg,(ComponentScopeShadow)trg.base.scope,false);
			}
	    	else {
	    		// clone data member, ignore udfs for the moment
	    		trg._data=duplicateDataMember(trg, _data, new HashMapPro(), deepCopy);
	    		trg._udfs=duplicateUTFMap(this,trg, _udfs,new HashMapPro<Key, UDF>());
	    		
	    		if(useShadow) {
	    			ComponentScopeShadow css = (ComponentScopeShadow)scope;
	    			trg.scope=new ComponentScopeShadow(trg,duplicateDataMember(trg,css.getShadow(),MapFactory.getConcurrentMap(),deepCopy));
	    		}
	    	}

	    	// at the moment this makes no sense, becuae this map is no more used after constructor has runned and for a clone the constructo is not executed, but perhaps this is used in future
	    	/*if(constructorUDFs!=null){
	    		trg.constructorUDFs=new HashMap<Collection.Key, UDF>();
	    		addUDFS(trg, constructorUDFs, trg.constructorUDFs);
	    	}*/
	    	
	    	if(isTop) {
	    		setTop(trg,trg);
	    		
	    		addUDFS(trg,_data,trg._data);
	    		if(useShadow){
	    			addUDFS(trg,((ComponentScopeShadow)scope).getShadow(),((ComponentScopeShadow)trg.scope).getShadow());
	    		}
	    	}
	    }
    	finally {
    		if(!inside)ThreadLocalDuplication.reset();
    	}
    	
		return trg;
	}
    
    
    private static void addUDFS(ComponentImpl trgComp, Map src, Map trg) {
		Iterator it = src.entrySet().iterator();
		Map.Entry entry;
		Object key,value;
		UDF udf;
		ComponentImpl comp,owner;
		boolean done;
    	while(it.hasNext()){
    		entry=(Entry) it.next();
    		key=entry.getKey();
    		value=entry.getValue();
    		if(value instanceof UDF) {
    			udf=(UDF) value;
    			done=false;
    			// get udf from _udf
    			owner = (ComponentImpl)udf.getOwnerComponent();
    			if(owner!=null) {
	    			comp=trgComp;
	    			do{
	    				if(owner.pageSource==comp.pageSource)
	    					break;
	    			}
	    			while((comp=comp.base)!=null);
	    			if(comp!=null) {
	    				value=comp._udfs.get(key);
	    				trg.put(key, value);
	    				done=true;
	    			}
    			}
    			// udf with no owner
    			if(!done) 
    				trg.put(key, udf.duplicate());
    			
    			//print.o(owner.pageSource.getComponentName()+":"+udf.getFunctionName());
    		}
    	}
	}

    /**
     * duplicate the datamember in the map, ignores the udfs
     * @param c
     * @param map
     * @param newMap
     * @param deepCopy
     * @return
     */
    public static MapPro duplicateDataMember(ComponentImpl c,MapPro map,MapPro newMap,boolean deepCopy){
        Iterator it=map.entrySet().iterator();
        Map.Entry entry;
        Object value;
        while(it.hasNext()) {
            entry=(Entry) it.next();
        	value=entry.getValue();
            
            if(!(value instanceof UDF)) {
            	if(deepCopy) value=Duplicator.duplicate(value,deepCopy);
            	newMap.put(entry.getKey(),value);
            }
        }
        return newMap;
    }
    
    public static MapPro<Key, UDF> duplicateUTFMap(ComponentImpl src,ComponentImpl trg,MapPro<Key,UDF> srcMap, MapPro<Key, UDF> trgMap){
    	Iterator<Entry<Key, UDF>> it = srcMap.entrySet().iterator();
        Entry<Key, UDF> entry;
        UDF udf;
        while(it.hasNext()) {
            entry=it.next();
            udf=entry.getValue();
        	
            if(udf.getOwnerComponent()==src) {
            	UDFPlus clone=(UDFPlus) entry.getValue().duplicate();
        		clone.setOwnerComponent(trg);
        		clone.setAccess(udf.getAccess());
        		trgMap.put(entry.getKey(),clone);	
            }
        	
        }
        return trgMap;
    }
    

	/**
     * initalize the Component
     * @param pageContext
     * @param componentPage
     * @throws PageException
     */    
    public void init(PageContext pageContext, ComponentPageImpl componentPage,boolean executeConstr) throws PageException {
    	this.pageSource=componentPage.getPageSource();

        // extends
	    if(!StringUtil.isEmpty(properties.extend)) {
			base= ComponentLoader.searchComponent(pageContext,componentPage.getPageSource(),properties.extend,Boolean.TRUE,null,true,executeConstr);
		}
	    else {
	    	CIPage p=((ConfigWebImpl)pageContext.getConfig()).getBaseComponentPage(pageSource.getDialect(),pageContext);
	    	if(!componentPage.getPageSource().equals(p.getPageSource())) {
            	base=ComponentLoader.loadComponent(pageContext,p,"Component",false,false,true,executeConstr);
	        }
	    }

	    if(base!=null){
	    	this.dataMemberDefaultAccess=base.dataMemberDefaultAccess;
	    	this._static=new StaticScope(base._static,this,componentPage,dataMemberDefaultAccess);
	    	//this._triggerDataMember=base._triggerDataMember;
	    	this.absFin=base.absFin;
	    	_data=base._data;
	    	_udfs=new HashMapPro<Key,UDF>(base._udfs);
	    	setTop(this,base);
	    }
	    else {
	    	this.dataMemberDefaultAccess=pageContext.getCurrentTemplateDialect()==CFMLEngine.DIALECT_CFML?
	    			pageContext.getConfig().getComponentDataMemberDefaultAccess():
	    			Component.ACCESS_PRIVATE;
	    	this._static=new StaticScope(null,this,componentPage,dataMemberDefaultAccess);
	    	// TODO get per CFC setting this._triggerDataMember=pageContext.getConfig().getTriggerComponentDataMember();
		    _udfs=new HashMapPro<Key,UDF>();
		    _data=MapFactory.getConcurrentMap();
	    }
	    // implements
	    if(!StringUtil.isEmpty(properties.implement)) {
	    	if(absFin==null)absFin=new AbstractFinal();
	    	absFin.add(InterfaceImpl.loadInterfaces(pageContext, getPageSource(), properties.implement));
	    	//abstrCollection.implement(pageContext,getPageSource(),properties.implement);
	    }
	    
	    
	    /*print.e("--------------------------------------");
	    print.e(_getPageSource().getDisplayPath());
	    print.e(abstrCollection.getUdfs());*/
	    
	    // scope
	    useShadow=base==null?
	    	(pageSource.getDialect()==CFMLEngine.DIALECT_CFML?pageContext.getConfig().useComponentShadow():false):
	    	base.useShadow;
	    if(useShadow) {
	        if(base==null) scope=new ComponentScopeShadow(this,MapFactory.getConcurrentMap());
		    else scope=new ComponentScopeShadow(this,(ComponentScopeShadow)base.scope,false);
	    }
	    else {
	    	scope=new ComponentScopeThis(this);
	    }
	    initProperties();
	    
	    
	    // invoke static constructor
	    if(!componentPage._static.isInit()) {
	    	componentPage._static.setInit(true);// this needs to happen before the call
	    	try {
	    		componentPage.staticConstructor(pageContext,this);
	    	}
	    	catch(Throwable t){
	    		componentPage._static.setInit(false);
	    		throw Caster.toPageException(t);
	    	}
	    }
	}
    
    public void checkInterface(PageContext pc, ComponentPageImpl componentPage) throws PageException {
    	/*print.e("xxxxxxxxxxxxxxxxxxxxxxxxxxxxx "+ComponentUtil.toModifier(getModifier(), "none")+" xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
    	print.e(componentPage.getPageSource().getDisplayPath());
    	print.e(getModifier()==MODIFIER_ABSTRACT);
    	if(_abstract!=null){
    		//print.e(_abstract.lastUpdate());
    		//print.e(componentPage.lastCheck());
    		//print.e(_abstract.lastUpdate()<=componentPage.lastCheck());
    		print.e(_abstract.getInterfaces());
    		print.e("has-udfs:"+_abstract.hasAbstractUDFs());
    	}*/
    	
    	// no records == nothing to do
    	if(absFin==null || !absFin.hasUDFs()) return;
    	
    	//if(getModifier()==MODIFIER_ABSTRACT || _abstract==null || !_abstract.hasAbstractUDFs()) return; // MUST add again cache, but also check change in all udfs from abstract cfc || _abstract.lastUpdate()<=componentPage.lastCheck()

		// ABSTRACT: check if the component define all functions defined in interfaces and abstract components
		if(getModifier()!=MODIFIER_ABSTRACT && absFin.hasAbstractUDFs()) {
			Map<Key, UDF> udfs = absFin.removeAbstractUDFs();
			//print.e(udfs);
			Iterator<Entry<Key, UDF>> it = udfs.entrySet().iterator();
			Entry<Key, UDF> entry;
			UDFPlus iUdf,cUdf;
			while(it.hasNext()){
				entry = it.next();
				iUdf=(UDFPlus) entry.getValue();
				cUdf=(UDFPlus) _udfs.get(entry.getKey());
				checkFunction(pc,componentPage,iUdf,cUdf);
			}
		}
		
		// FINAL: does a function overwrite a final method
		if(absFin.hasFinalUDFs()) {
			Map<Key, UDF> udfs = absFin.getFinalUDFs();
			Iterator<Entry<Key, UDF>> it = udfs.entrySet().iterator();
			Entry<Key, UDF> entry;
			UDFPlus iUdf,cUdf;
			while(it.hasNext()){
				entry = it.next();
				iUdf=(UDFPlus) entry.getValue();
				cUdf=(UDFPlus) _udfs.get(entry.getKey());
				
				// if this is not the same, it was overwritten
				if(iUdf!=cUdf)
					throw new ApplicationException("the function ["+entry.getKey()+"] from component ["+
						cUdf.getSource()+
						"] tries to override a final method with the same name from component ["+
						iUdf.getSource()+"]");
			}
		}
		
		
		
		// MUST componentPage.ckecked();
	}

	private void checkFunction(PageContext pc, ComponentPageImpl componentPage,UDFPlus iUdf, UDFPlus cUdf) throws ApplicationException {
		FunctionArgument[] iFA,cFA;
		
		
		// UDF does not exist
		if(cUdf==null ) {
			throw new ApplicationException(
  					 "component ["+componentPage.getComponentName()+
  					 "] does not implement the function ["+iUdf.toString().toLowerCase()+"] of the "+
  					("abstract component/interface")+" ["+
  					 iUdf.getSource()+"]");
		}
		
		iFA=iUdf.getFunctionArguments();
		cFA=cUdf.getFunctionArguments();
// access
		if(cUdf.getAccess()>Component.ACCESS_PUBLIC){
			throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
				"access ["+ComponentUtil.toStringAccess(cUdf.getAccess())+"] has to be at least [public]");
		}
		
// return type
		if(iUdf.getReturnType()!=cUdf.getReturnType()){
			throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
				"return type ["+cUdf.getReturnTypeAsString()+"] does not match the "+("abstract component/interface")+" function return type ["+iUdf.getReturnTypeAsString()+"]");
		}
		// none base types
		if(iUdf.getReturnType()==CFTypes.TYPE_UNKNOW && !iUdf.getReturnTypeAsString().equalsIgnoreCase(cUdf.getReturnTypeAsString())) {
			throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
				"return type ["+cUdf.getReturnTypeAsString()+"] does not match the "+("abstract component/interface")+" function return type ["+iUdf.getReturnTypeAsString()+"]");
		}

// arguments
		if(iFA.length!=cFA.length) {
			throw new ApplicationException( _getErrorMessage(cUdf,iUdf),"not the same argument count");
			}
		
		for(int i=0;i<iFA.length;i++) {
			// type
			if(iFA[i].getType()!=cFA[i].getType()){
				throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
					"argument type ["+cFA[i].getTypeAsString()+"] does not match the "+("abstract component/interface")+" function argument type ["+iFA[i].getTypeAsString()+"]");
			}
			// none base types
			if(iFA[i].getType()==CFTypes.TYPE_UNKNOW && !iFA[i].getTypeAsString().equalsIgnoreCase(cFA[i].getTypeAsString())) {
				throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
    					"argument type ["+cFA[i].getTypeAsString()+"] does not match the "+("abstract component/interface")+" function argument type ["+iFA[i].getTypeAsString()+"]");
    		}
			// name
			if(!iFA[i].getName().equalsIgnoreCase(cFA[i].getName())){
				throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
					"argument name ["+cFA[i].getName()+"] does not match the "+("abstract component/interface")+" function argument name ["+iFA[i].getName()+"]");
			}
			// required
			if(iFA[i].isRequired()!=cFA[i].isRequired()){
				throw new ApplicationException( _getErrorMessage(cUdf,iUdf),
					"argument ["+cFA[i].getName()+"] should "+(iFA[i].isRequired()?"":"not ")+"be required");
			}
		}
	}

	private String _getErrorMessage(UDFPlus cUdf,UDFPlus iUdf) {
		return "function ["+cUdf.toString().toLowerCase()+"] of component " +
			 "["+pageSource.getDisplayPath()+"]" +
			 " does not match the function declaration ["+iUdf.toString().toLowerCase()+"] of the "+("abstract component/interface")+" " +
			 "["+iUdf.getSource()+"]";
	}


	private static void setTop(ComponentImpl top,ComponentImpl trg) {
		while(trg!=null){
			trg.top=top;
			trg=trg.base;
		}
	}

    Object _call(PageContext pc, Collection.Key key, Struct namedArgs, Object[] args,boolean superAccess) throws PageException {
    	
    	Member member=getMember(pc,key,false, superAccess);
    	
    	if(member instanceof UDFPlus) {
    		return _call(pc,key,(UDFPlus)member,namedArgs,args);
        }
        return onMissingMethod(pc, -1, member, key.getString(), args, namedArgs, superAccess);
    }

    Object _call(PageContext pc, int access, Collection.Key key, Struct namedArgs, Object[] args,boolean superAccess) throws PageException {
        Member member=getMember(access,key,false,superAccess);
        if(member instanceof UDF) {
            return _call(pc,key,(UDFPlus)member,namedArgs,args);
        }
        return onMissingMethod(pc, access, member, key.getString(), args, namedArgs, superAccess);
    }
    
    public Object onMissingMethod(PageContext pc, int access,Member member,String name,Object _args[],Struct _namedArgs, boolean superAccess) throws PageException {
    	Member ommm = access==-1?
    			getMember(pc,KeyConstants._onmissingmethod,false, superAccess):
    			getMember(access,KeyConstants._onmissingmethod,false, superAccess);
        if(ommm instanceof UDF) {
        	Argument args=new ArgumentImpl();
        	if(_args!=null) {
        		for(int i=0;i<_args.length;i++) {
        			args.setEL(ArgumentIntKey.init(i+1), _args[i]);
        		}
        	}
        	else if(_namedArgs!=null) {
        		UDFUtil.argumentCollection(_namedArgs, new FunctionArgument[]{});
        		
        		Iterator<Entry<Key, Object>> it = _namedArgs.entryIterator();
        		Entry<Key, Object> e;
        		while(it.hasNext()){
        			e = it.next();
        			args.setEL(e.getKey(),e.getValue());
        		}
        		
        	}
        	
        	//Struct newArgs=new StructImpl(StructImpl.TYPE_SYNC);
        	//newArgs.setEL(MISSING_METHOD_NAME, name);
        	//newArgs.setEL(MISSING_METHOD_ARGS, args); 
        	Object[] newArgs=new Object[]{name,args};
        	
        	return _call(pc,KeyConstants._onmissingmethod,(UDFPlus)ommm,null,newArgs);
        }
        if(member==null)throw ComponentUtil.notFunction(this, KeyImpl.init(name), null,access);
        throw ComponentUtil.notFunction(this, KeyImpl.init(name), member.getValue(),access);
    }
	
	Object _call(PageContext pc, Collection.Key calledName,UDFPlus udf, Struct namedArgs, Object[] args) throws PageException {
			
		Object rtn=null;
		Variables parent=null;
        
		// INFO duplicate code is for faster execution -> less contions
		
		
		// debug yes
		if(pc.getConfig().debug()) {
		    DebugEntryTemplate debugEntry=pc.getDebugger().getEntry(pc,pageSource,udf.getFunctionName());//new DebugEntry(src,udf.getFunctionName());
			long currTime=pc.getExecutionTime();
			long time=System.nanoTime();
			
			// sync yes
			if(top.properties._synchronized){
				synchronized (this) {
					try {
						parent=beforeCall(pc);
						if(args!=null)rtn=udf.call(pc,calledName,args,true);
						else rtn=udf.callWithNamedValues(pc,calledName,namedArgs,true);
					}		
					finally {
						pc.setVariablesScope(parent);
						long diff= ((System.nanoTime()-time)-(pc.getExecutionTime()-currTime));
						pc.setExecutionTime(pc.getExecutionTime()+diff);
						debugEntry.updateExeTime(diff);	
					}	
				}
			}

			// sync no
			else {
				try {
					parent=beforeCall(pc);
					if(args!=null)rtn=udf.call(pc,calledName,args,true);
					else rtn=udf.callWithNamedValues(pc,calledName,namedArgs,true);
				}		
				finally {
					pc.setVariablesScope(parent);
					long diff= ((System.nanoTime()-time)-(pc.getExecutionTime()-currTime));
					pc.setExecutionTime(pc.getExecutionTime()+diff);
					debugEntry.updateExeTime(diff);	
				}	
			}
			
			
		}
		
		// debug no
		else {
			
			// sync yes
			if(top.properties._synchronized){
				synchronized (this) {
				    try {
		            	parent=beforeCall(pc); 
		            	if(args!=null)rtn=udf.call(pc,calledName,args,true);
						else rtn=udf.callWithNamedValues(pc,calledName,namedArgs,true);
					}		
					finally {
						pc.setVariablesScope(parent);
					}
				}
			}
			
			// sync no 385|263
			else {
			    try {
	            	parent=beforeCall(pc);
	            	if(args!=null)rtn=udf.call(pc,calledName,args,true);
					else rtn=udf.callWithNamedValues(pc,calledName,namedArgs,true);
				}		
				finally {
					pc.setVariablesScope(parent);
				}
			}
		}
		return rtn;
	}
	

	@Override
	public Variables beforeStaticConstructor(PageContext pc){
		insideStaticConstr=true;
    	Variables parent=pc.variablesScope();
		pc.setVariablesScope(_static);
    	return parent;
	}

	@Override
	public void afterStaticConstructor(PageContext pc, Variables parent){
		insideStaticConstr=false;
    	pc.setVariablesScope(parent);
	}
	
	/**
     * will be called before executing method or constructor
     * @param pc
     * @return the old scope map
     */
	public Variables beforeCall(PageContext pc) {
    	Variables parent=pc.variablesScope();
    	pc.setVariablesScope(scope);
    	return parent;
    }
	
	/**
     * will be called after invoking constructor, only invoked by constructor (component body execution)
	 * @param pc
	 * @param parent
	 * @throws ApplicationException 
	 */
    public void afterConstructor(PageContext pc, Variables parent) throws ApplicationException {
    	pc.setVariablesScope(parent);
    	this.afterConstructor=true;
    	
    	/*if(constructorUDFs!=null){
    		Iterator<Entry<Key, UDF>> it = constructorUDFs.entrySet().iterator();
    		Map.Entry<Key, UDF> entry;
    		Key key;
    		UDFPlus udf;
    		PageSource ps;
    		while(it.hasNext()){
    			entry=it.next();
    			key=entry.getKey();
    			udf=(UDFPlus) entry.getValue();
    			ps=udf.getPageSource();
    			//if(ps!=null && ps.equals(getPageSource()))continue; // TODO can we avoid that udfs from the compinent itself are here?
    			registerUDF(key, udf,false,true);
    		}
    	}*/
	}
    
    /**
     * this function may be called by generated code inside a ra file
     * @deprecated replaced with <code>afterConstructor(PageContext pc, Variables parent)</code>
     * @param pc
     * @param parent
     * @throws ApplicationException 
     */
    public void afterCall(PageContext pc, Variables parent) throws ApplicationException {
    	afterConstructor(pc, parent);
	}
	
    /**
     * sets the callpath
     * @param callPath
     * /
    public void setCallPath(String callPath) {
		properties.callPath=callPath;
	}*/

	/**
     * rerturn the size
	 * @param access
	 * @return size
	 */
    public int size(int access) {
	    return keys(access).length;
	}

    /**
     * list of keys
     * @param c 
     * @param access
     * @param doBase 
     * @return key set
     */
	public Set<Key> keySet(int access) {
		
    	Set<Key> set=new LinkedHashSet<Key>();
        Map.Entry<Key, Member> entry;    
        Iterator<Entry<Key, Member>> it = _data.entrySet().iterator();
        while(it.hasNext()) {
            entry=it.next();
            if(entry.getValue().getAccess()<=access)set.add(entry.getKey());
        }
        return set;
    }
    
    /*protected Set<Key> udfKeySet(int access) {
    	Set<Key> set=new HashSet<Key>();
        Member m;
        Map.Entry<Key, UDF> entry;
        Iterator<Entry<Key, UDF>> it = _udfs.entrySet().iterator();
        while(it.hasNext()) {
            entry= it.next();
            m=entry.getValue();
            if(m.getAccess()<=access)set.add(entry.getKey());
        }
        return set;
    }*/
    
    
    protected java.util.List<Member> getMembers(int access) {
        java.util.List<Member> members=new ArrayList<Member>();
        Member e;
        Iterator<Entry<Key, Member>> it = _data.entrySet().iterator();
        while(it.hasNext()) {
        	e=it.next().getValue();
            if(e.getAccess()<=access)members.add(e);
        }
        return members;
    }


    @Override
	public Iterator<Collection.Key> keyIterator(int access) {
        return keySet(access).iterator();
    }
    
    @Override
	public Iterator<String> keysAsStringIterator(int access) {
        return new StringIterator(keys(access));
    }

	@Override
	public Iterator<Entry<Key, Object>> entryIterator(int access) {
		return new ComponentEntryIterator(this, keys(access),access);
	}

	@Override
	public Iterator<Object> valueIterator(int access) {
		return new ComponentValueIterator(this,keys(access),access);
	}

	
	@Override
	public Iterator<Object> valueIterator() {
		return valueIterator(getAccess(ThreadLocalPageContext.get()));
	}
	
	@Override
	public java.util.Iterator<?> getIterator() {
    	PageContext pc = ThreadLocalPageContext.get();
    	// do we have functions _hasNext,_next,_reset
    	if(
    			getMember(pc,KeyConstants.__hasNext,false,false)!=null && 
    			getMember(pc,KeyConstants.__next,false,false)!=null)
        	return new ComponentIterator(this);
        
    	
    	return keysAsStringIterator();
    	
    	
    } 

	@Override
	public Collection.Key[] keys(int access) {
        Set<Key> set = keySet(access);
        return set.toArray(new Collection.Key[set.size()]);
    }
	
	@Override
	public void clear() {
		_data.clear();
		_udfs.clear();
	}

	@Override
	public Member getMember(int access,Collection.Key key, boolean dataMember,boolean superAccess) {
    	// check super
        if(dataMember && access==ACCESS_PRIVATE && key.equalsIgnoreCase(KeyConstants._super)) {
        	Component ac =ComponentUtil.getActiveComponent(ThreadLocalPageContext.get(),this);
        	return SuperComponent.superMember((ComponentImpl)ac.getBaseComponent());
            //return SuperComponent . superMember(base);
        }
    	if(superAccess) {
    		return  _udfs.get(key);
        }
        // check data
        Member member=_data.get(key);
        if(member!=null) {
            if(member.getAccess()<=access)return member;
            return null;
        }
        return null;
    }


	/**
     * get entry matching key
     * @param access 
     * @param keyLowerCase key lower case (case sensitive)
     * @param doBase do check also base component
     * @param dataMember do also check if key super
     * @return matching entry if exists otherwise null
     */
	protected Member getMember(PageContext pc, Collection.Key key, boolean dataMember,boolean superAccess) {
        // check super
        if(dataMember && isPrivate(pc) && key.equalsIgnoreCase(KeyConstants._super)) {
        	Component ac = ComponentUtil.getActiveComponent(pc,this);
        	return SuperComponent.superMember((ComponentImpl)ac.getBaseComponent());
        }
        if(superAccess) 
        	return  _udfs.get(key);
        
        // check data
        Member member=_data.get(key);
        if(isAccessible(pc,member)) return member;
        return null;
    }
    
    boolean isAccessible(PageContext pc, Member member) {
        // TODO geschwindigkeit 
    	if(member!=null) {
    		int access=member.getAccess();
    		if(access<=ACCESS_PUBLIC) return true;
    		else if(access==ACCESS_PRIVATE && isPrivate(pc)) return true;
    		else if(access==ACCESS_PACKAGE && isPackage(pc)) return true;
        }
        return false;
	}
    
    boolean isAccessible(PageContext pc, int access) {
    	
    	if(access<=ACCESS_PUBLIC) return true;
		else if(access==ACCESS_PRIVATE && isPrivate(pc)) return true;
		else if(access==ACCESS_PACKAGE && isPackage(pc)) return true;
    	return false;
	}

    /**
     * @param pc
     * @return returns if is private
     */
    private boolean isPrivate(PageContext pc) {
    	pc=ThreadLocalPageContext.get(pc);
    	if(pc==null) return true;
    	Component ac = pc.getActiveComponent();
    	
    	
    	return (ac!=null && (ac==this || 
                ((ComponentImpl)ac).top.pageSource.equals(this.top.pageSource))) ;
    }
    /**
     * @param pc
     * @return returns if is package
     */
    private boolean isPackage(PageContext pc) {
    	pc=ThreadLocalPageContext.get(pc);
    	if(pc==null) return true;
    	Component ac = pc.getActiveComponent();
        if(ac!=null) {
            if(ac==this) return true;
            ComponentImpl aci = ((ComponentImpl)ac);
            if(aci.top.pageSource.equals(this.top.pageSource))return true;
            
            int index;
            String other=aci.top.getAbsName();
            index=other.lastIndexOf('.');
            if(index==-1)other="";
            else other=other.substring(0,index);
            
            String my=this.top.getAbsName();
            index=my.lastIndexOf('.');
            if(index==-1)my="";
            else my=my.substring(0,index);
            
            return my.equalsIgnoreCase(other);
        }
        return false;
    }
    
	/**
	 * return the access of a member
	 * @param key
	 * @return returns the access (Component.ACCESS_REMOTE, ACCESS_PUBLIC, ACCESS_PACKAGE,Component.ACCESS_PRIVATE)
	 */
	private int getAccess(Collection.Key key){
        Member member=getMember(ACCESS_PRIVATE,key,false,false);
        if(member==null) return Component.ACCESS_PRIVATE;
        return member.getAccess();
	}
    
    /** 
     * returns current access to this component
     * @param pc
     * @return access
     */
    int getAccess(PageContext pc) {
        if(pc==null) return ACCESS_PUBLIC;
        
        if(isPrivate(pc)) return ACCESS_PRIVATE;
        if(isPackage(pc)) return ACCESS_PACKAGE;
        return ACCESS_PUBLIC;
    }
	
    @Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		return toDumpData(pageContext,maxlevel,dp,getAccess(pageContext));
    }
    

    @Override
    public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access) {
    	boolean isCFML=getPageSource().getDialect()==CFMLEngine.DIALECT_CFML;
	    DumpTable table = isCFML?new DumpTable("component","#ff4542","#ff9aad","#000000"):new DumpTable("component","#ca6b50","#e9bcac","#000000");
        table.setTitle((isCFML?"Component":"Class")+" "+getCallPath()+""+(" "+StringUtil.escapeHTML(top.properties.dspName)));
        table.setComment("Only the functions and data members that are accessible from your location are displayed");
        
        // Extends
        if(!StringUtil.isEmpty(top.properties.extend,true))
        	table.appendRow(1,new SimpleDumpData("Extends"),new SimpleDumpData(top.properties.extend));
        
        // Interfaces
        if(!StringUtil.isEmpty(top.properties.implement,true))
        	table.appendRow(1,new SimpleDumpData("Implements"),new SimpleDumpData(top.properties.implement));
        
        
        if(top.properties.modifier!=Member.MODIFIER_NONE)
        	table.appendRow(1,new SimpleDumpData("Modifier"),new SimpleDumpData(ComponentUtil.toModifier(top.properties.modifier,"")));
        if(top.properties.hint.trim().length()>0)
        	table.appendRow(1,new SimpleDumpData("Hint"),new SimpleDumpData(top.properties.hint));
        
        // this
        DumpTable thisScope = thisScope(top,pageContext,maxlevel,dp,access);
        if(!thisScope.isEmpty())table.appendRow(1,new SimpleDumpData("this"),thisScope);
        
        // static
        DumpTable staticScope = _static._toDumpData(top,pageContext, maxlevel, dp,getAccess(pageContext));
        if(!staticScope.isEmpty())table.appendRow(1,new SimpleDumpData("static"),staticScope);
        
        return table;
    }
    
	static DumpTable thisScope(ComponentImpl ci,PageContext pc, int maxlevel, DumpProperties dp,int access) {

		maxlevel--;
		ComponentSpecificAccess cw = new ComponentSpecificAccess(Component.ACCESS_PRIVATE, ci);
		Collection.Key[] keys = cw.keys();

		DumpTable[] dtAccess = new DumpTable[4];
		dtAccess[Component.ACCESS_PRIVATE] = new DumpTable("#ff6633","#ff9966","#000000");
		dtAccess[Component.ACCESS_PRIVATE].setTitle("private");
		dtAccess[Component.ACCESS_PRIVATE].setWidth("100%");

		dtAccess[Component.ACCESS_PACKAGE] = new DumpTable("#ff9966","#ffcc99","#000000");
		dtAccess[Component.ACCESS_PACKAGE].setTitle("package");
		dtAccess[Component.ACCESS_PACKAGE].setWidth("100%");

		dtAccess[Component.ACCESS_PUBLIC] = new DumpTable("#ffcc99","#ffffcc","#000000");
		dtAccess[Component.ACCESS_PUBLIC].setTitle("public");
		dtAccess[Component.ACCESS_PUBLIC].setWidth("100%");

		dtAccess[Component.ACCESS_REMOTE] = new DumpTable("#ccffcc","#ffffff","#000000");
		dtAccess[Component.ACCESS_REMOTE].setTitle("remote");
		dtAccess[Component.ACCESS_REMOTE].setWidth("100%");
		
		Collection.Key key;
		for (int i=0; i<keys.length; i++){

			key = keys[i];
			int a = ci.getAccess(key);
			DumpTable box = dtAccess[a];
			Object o = cw.get(key, null);

			if (o==ci)
				o="[this]";

			if (DumpUtil.keyValid(dp, maxlevel, key)){
				String memberName = (o instanceof UDF) ? ((UDF)o).getFunctionName() : key.toString();
				box.appendRow(1, new SimpleDumpData(memberName), DumpUtil.toDumpData(o, pc, maxlevel, dp));
			}
		}

		DumpTable table=new DumpTable("#ffffff","#cccccc","#000000");
		
		// properties
		if(ci.top.properties.persistent || ci.top.properties.accessors){

			Property[] properties=ci.getProperties(false,true,false,false);
			DumpTable prop = new DumpTable("#99cc99","#ccffcc","#000000");

			prop.setTitle("Properties");
			prop.setWidth("100%");

			Property p;
			Object child;
			DumpData dd;
			for (int i=0; i<properties.length; i++){

				p=properties[i];
				child = ci.scope.get(KeyImpl.init(p.getName()), null);

				if (child instanceof Component){
					DumpTable t = new DumpTable("component","#99cc99","#ffffff","#000000");
					t.appendRow(1, new SimpleDumpData(((Component)child).getPageSource().getDialect() == CFMLEngine.DIALECT_CFML ? "Component" : "Class")
							, new SimpleDumpData(((Component)child).getCallName()));
					dd=t;
				}
				else {
					dd = DumpUtil.toDumpData(child, pc, maxlevel - 1, dp);
				}

				prop.appendRow(1, new SimpleDumpData(p.getName()),dd);
			}
			
			if (access >= ACCESS_PUBLIC && !prop.isEmpty())
				table.appendRow(0,prop);
		}

		if (!dtAccess[ACCESS_REMOTE].isEmpty())
			table.appendRow(0,dtAccess[Component.ACCESS_REMOTE]);

		if (!dtAccess[ACCESS_PUBLIC].isEmpty())
			table.appendRow(0,dtAccess[Component.ACCESS_PUBLIC]);

		if (!dtAccess[ACCESS_PACKAGE].isEmpty())
			table.appendRow(0,dtAccess[Component.ACCESS_PACKAGE]);

		if (!dtAccess[ACCESS_PRIVATE].isEmpty())
			table.appendRow(0,dtAccess[Component.ACCESS_PRIVATE]);

		for (DumpTable dt : dtAccess){
			dt.sortRows(
				new Comparator<DumpRow>() {
					@Override
					public int compare(DumpRow o1, DumpRow o2) {

						DumpData[] rowItems1 = o1.getItems();
						DumpData[] rowItems2 = o2.getItems();

						if (rowItems1.length >= 0 && rowItems2.length > 0 && rowItems1[0] instanceof SimpleDumpData && rowItems2[0] instanceof SimpleDumpData){
							return String.CASE_INSENSITIVE_ORDER.compare(rowItems1[0].toString(), rowItems2[0].toString());
						}

						return 0;
					}
				}
			);
		}

		return table;
	}
	
	/**
	 * @return return call path
	 */
	protected String getCallPath() {
		if(StringUtil.isEmpty(top.properties.callPath)) return getName();
		try {
            return "("+ListUtil.arrayToList(ListUtil.listToArrayTrim(top.properties.callPath.replace('/','.').replace('\\','.'),"."),".")+")";
        } catch (PageException e) {
            return top.properties.callPath;
        }
	}

    @Override
    public String getDisplayName() {
		return top.properties.dspName;
	}

    @Override
    public String getExtends() {
		return top.properties.extend;
	}
    @Override
    public int getModifier() {
		return properties.modifier;
	}
    public String getBaseAbsName() {
		return top.base.pageSource.getComponentName();
	}
    
    public boolean isBasePeristent() {
		return top.base!=null && top.base.properties.persistent;
	}
	
	
    @Override
    public String getHint() {
		return top.properties.hint;
	}
    
    @Override
    public String getWSDLFile() {
		return top.properties.getWsdlFile();
	}

    @Override
    public String getName() {
	    if(top.properties.callPath==null) return "";
	    return ListUtil.last(top.properties.callPath,"./",true);
	}
    public String _getName() { // MUST nicht so toll
	    if(properties.callPath==null) return "";
	    return ListUtil.last(properties.callPath,"./",true);
	}
    public PageSource _getPageSource() {
    	return pageSource;
	}
	
    @Override
    public String getCallName() {
	    return top.properties.callPath;
	}
    
    @Override
    public String getAbsName() {
    	return top.pageSource.getComponentName();
	}
    

    @Override
    public boolean getOutput() {
    	if(top.properties.output==null) return true;
        return top.properties.output.booleanValue();
    }

    @Override
    public boolean instanceOf(String type) {
    	
    	ComponentImpl c=top;
    	do {
        	if(type.equalsIgnoreCase(c.properties.callPath)) return true;
            if(type.equalsIgnoreCase(c.pageSource.getComponentName())) return true;
            if(type.equalsIgnoreCase(c._getName())) return true;       
            
    		// check interfaces
    		if(c.absFin!=null){
    			Iterator<InterfaceImpl> it = c.absFin.getInterfaceIt();
	    		while(it.hasNext()){
	    			if(it.next().instanceOf(type))return true;
	    		}
    		}
    		c=c.base;
    	}
    	while(c!=null);
    	if(StringUtil.endsWithIgnoreCase(type, "component")){
    		if(type.equalsIgnoreCase("component"))							return true;
    		if(type.equalsIgnoreCase("web-inf.cftags.component"))			return true;
    		//if(type.equalsIgnoreCase("web-inf.lucee.context.component"))	return true;
    		
    	}
    	return false;
    }
    
    public boolean equalTo(String type) {
    	ComponentImpl c=top;
    	
    	if(type.equalsIgnoreCase(c.properties.callPath)) return true;
        if(type.equalsIgnoreCase(c.pageSource.getComponentName())) return true;
        if(type.equalsIgnoreCase(c._getName())) return true;       
            
		// check interfaces
		if(c.absFin!=null){
    		Iterator<InterfaceImpl> it = c.absFin.getInterfaceIt();
    		while(it.hasNext()){
        		if(it.next().instanceOf(type))return true;
        	}
		}
		
    	if(StringUtil.endsWithIgnoreCase(type, "component")){
    		if(type.equalsIgnoreCase("component"))							return true;
    		if(type.equalsIgnoreCase("web-inf.cftags.component"))			return true;
    	}
    	return false;
    }
    

    @Override
    public boolean isValidAccess(int access) {
    	return access==ACCESS_PRIVATE || access==ACCESS_PACKAGE || access==ACCESS_PUBLIC || access==ACCESS_REMOTE;
	}
    
    @Override
    public PageSource getPageSource() {
        return top.pageSource;
    }
    

    @Override
    public String castToString() throws PageException {
    	return castToString(false);
    }
    
	@Override
	public String castToString(String defaultValue) {
		return castToString(false,defaultValue);
	}
    
    String castToString(boolean superAccess) throws PageException {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toString,true,superAccess);
			//Object o = get(pc,"_toString",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_STRING && udf.getFunctionArguments().length==0) {
					return Caster.toString(_call(pc, KeyConstants.__toString,udf, null, new Object[0]));
				}
			}
		}
		
		
		throw ExceptionUtil.addHint(new ExpressionException("Can't cast Component ["+getName()+"] to String"),"Add a User-Defined-Function to Component with the following pattern [_toString():String] to cast it to a String or use Built-In-Function \"serialize(Component):String\" to convert it to a serialized String");
        
    }
    
    
    
    
    String castToString(boolean superAccess,String defaultValue) {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toString,true,superAccess);
			//Object o = get(pc,"_toString",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_STRING && udf.getFunctionArguments().length==0) {
					try {
						return Caster.toString(_call(pc,KeyConstants.__toString, udf, null, new Object[0]),defaultValue);
					} catch (PageException e) {
						return defaultValue;
					}
				}
			}
		}
		return defaultValue;
    }

    @Override
    public boolean castToBooleanValue() throws PageException {
    	return castToBooleanValue(false);
    }
    
    @Override
    public Boolean castToBoolean(Boolean defaultValue) {
        return castToBoolean(false, defaultValue);
    }

    boolean castToBooleanValue(boolean superAccess) throws PageException {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toBoolean,true,superAccess);
			//Object o = get(pc,"_toBoolean",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_BOOLEAN && udf.getFunctionArguments().length==0) {
					return Caster.toBooleanValue(_call(pc, KeyConstants.__toBoolean,udf, null, new Object[0]));
				}
			}
		}
    	
        throw ExceptionUtil.addHint(new ExpressionException("Can't cast Component ["+getName()+"] to a boolean value"),
                "Add a User-Defined-Function to Component with the following pattern [_toBoolean():boolean] to cast it to a boolean value");
    }
    
    Boolean castToBoolean(boolean superAccess,Boolean defaultValue) {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toBoolean,true,superAccess);
			//Object o = get(pc,"_toBoolean",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_BOOLEAN && udf.getFunctionArguments().length==0) {
					try {
						return Caster.toBoolean(_call(pc,KeyConstants.__toBoolean, udf, null, new Object[0]),defaultValue);
					} catch (PageException e) {
						return defaultValue;
					}
				}
			}
		}
    	return defaultValue;
    }

    @Override
    public double castToDoubleValue() throws PageException {
    	return castToDoubleValue(false);
    }
    
    @Override
    public double castToDoubleValue(double defaultValue) {
        return castToDoubleValue(false, defaultValue);
    }
    
    
    double castToDoubleValue(boolean superAccess) throws PageException {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toNumeric,true,superAccess);
			//Object o = get(pc,"_toNumeric",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length==0) {
					return Caster.toDoubleValue(_call(pc, KeyConstants.__toNumeric,udf, null, new Object[0]));
				}
			}
		}
    
        throw ExceptionUtil.addHint(new ExpressionException("Can't cast Component ["+getName()+"] to a numeric value"),
                "Add a User-Defined-Function to Component with the following pattern [_toNumeric():numeric] to cast it to a numeric value");
    }
    double castToDoubleValue(boolean superAccess,double defaultValue) {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toNumeric,true,superAccess);
			//Object o = get(pc,"_toNumeric",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_NUMERIC && udf.getFunctionArguments().length==0) {
					try {
						return Caster.toDoubleValue(_call(pc, KeyConstants.__toNumeric,udf, null, new Object[0]),true,defaultValue);
					} catch (PageException e) {
						return defaultValue;
					}
				}
			}
		}
		return defaultValue;
    }

    @Override
    public DateTime castToDateTime() throws PageException {
    	return castToDateTime(false);
    }
    
    @Override
    public DateTime castToDateTime(DateTime defaultValue) {
        return castToDateTime(false, defaultValue);
    }

    DateTime castToDateTime(boolean superAccess) throws PageException {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toDateTime,true,superAccess);
			//Object o = get(pc,"_toDateTime",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_DATETIME && udf.getFunctionArguments().length==0) {
					return Caster.toDate(_call(pc, KeyConstants.__toDateTime,udf, null, new Object[0]),pc.getTimeZone());
				}
			}
		}
    
		throw ExceptionUtil.addHint(new ExpressionException("Can't cast Component ["+getName()+"] to a date"),
                "Add a User-Defined-Function to Component with the following pattern [_toDateTime():datetime] to cast it to a date");
    }
    DateTime castToDateTime(boolean superAccess,DateTime defaultValue) {
    	// magic function
    	PageContext pc = ThreadLocalPageContext.get();
		if(pc!=null) {
			Member member = getMember(pc,KeyConstants.__toDateTime,true,superAccess);
			//Object o = get(pc,"_toDateTime",null);
			if(member instanceof UDFPlus) {
				UDFPlus udf = (UDFPlus)member;
				if(udf.getReturnType()==CFTypes.TYPE_DATETIME && udf.getFunctionArguments().length==0) {
					
					try {
						return DateCaster.toDateAdvanced(
								_call(pc, KeyConstants.__toDateTime,udf, null, new Object[0])
								,DateCaster.CONVERTING_TYPE_OFFSET
								,pc.getTimeZone()
								,defaultValue);
					} catch (PageException e) {
						return defaultValue;
					}
					
				}
			}
		}
		return defaultValue;
    }

    @Override
    public synchronized Struct getMetaData(PageContext pc) throws PageException {
    	return getMetaData(ACCESS_PRIVATE,pc,top,false);
    }
    

    public synchronized Object getMetaStructItem(Collection.Key name) {
    	if(top.properties.meta!=null) {
        	return top.properties.meta.get(name,null);
        }
    	return null;
    }

    protected static Struct getMetaData(int access,PageContext pc, ComponentImpl comp, boolean ignoreCache) throws PageException {
    	// Cache
    	Page page=MetadataUtil.getPageWhenMetaDataStillValid(pc, comp, ignoreCache);
    	if(page!=null && page.metaData!=null && page.metaData.get()!=null){
    		return page.metaData.get();
    	}
    	long creationTime=System.currentTimeMillis(); 
    	StructImpl sct=new StructImpl();
    	
        // fill udfs
        metaUDFs(pc, comp, sct,access);
        
        // meta
        if(comp.properties.meta!=null) 
        	StructUtil.copy(comp.properties.meta, sct, true);
            
        String hint=comp.properties.hint;
        String displayname=comp.properties.dspName;
        if(!StringUtil.isEmpty(hint))sct.set(KeyConstants._hint,hint);
        if(!StringUtil.isEmpty(displayname))sct.set(KeyConstants._displayname,displayname);
        
        sct.set(KeyConstants._persistent,comp.properties.persistent);
        sct.set(KeyConstants._hashCode,comp.hashCode());
        sct.set(KeyConstants._accessors,comp.properties.accessors);
        sct.set(KeyConstants._synchronized,comp.properties._synchronized);
        if(comp.properties.output!=null)
        sct.set(KeyConstants._output,comp.properties.output);
            
        // extends
        Struct ex=null;
        if(comp.base!=null) ex=getMetaData(access,pc,comp.base,true);
        if(ex!=null)sct.set(KeyConstants._extends,ex);
        
        // implements
        if(comp.absFin!=null){
        	Set<String> set = ListUtil.listToSet(comp.properties.implement, ",",true);
            if(comp.absFin.hasInterfaces()){
	            Iterator<InterfaceImpl> it = comp.absFin.getInterfaceIt();
	            Struct imp=new StructImpl();
	            InterfaceImpl inter;
            	while(it.hasNext()) {
            		inter=it.next();
            		if(!set.contains(inter.getCallPath())) continue;
            		imp.setEL(KeyImpl.init(inter.getCallPath()), inter.getMetaData(pc,true));
	            }
	            sct.set(KeyConstants._implements,imp);
            }
        }
         
        // PageSource
        PageSource ps = comp.pageSource;
        sct.set(KeyConstants._fullname,ps.getComponentName());
        sct.set(KeyConstants._name,ps.getComponentName());
        sct.set(KeyConstants._path,ps.getDisplayPath());
        sct.set(KeyConstants._type,"component");
        int dialect=comp.getPageSource().getDialect();
                
        boolean supressWSBeforeArg=dialect!=CFMLEngine.DIALECT_CFML || pc.getConfig().getSuppressWSBeforeArg();
        
        Class<?> skeleton = comp.getJavaAccessClass(pc,
        		new RefBooleanImpl(false),
        		((ConfigImpl)pc.getConfig()).getExecutionLogEnabled(),
        		false,false,
        		supressWSBeforeArg);
        if(skeleton !=null)sct.set(KeyConstants._skeleton, skeleton);
        
        HttpServletRequest req = pc.getHttpServletRequest();
            try {
            	String path=ContractPath.call(pc, ps.getDisplayPath()); // MUST better impl !!!
				sct.set("remoteAddress",""+new URL(req.getScheme(),req.getServerName(),req.getServerPort(),req.getContextPath()+path+"?wsdl"));
			} catch (Throwable t) {}
            
        
        // Properties
        if(comp.properties.properties!=null) {
        	ArrayImpl parr = new ArrayImpl();
        	Property p;
        	Iterator<Entry<String, Property>> pit = comp.properties.properties.entrySet().iterator();
        	while(pit.hasNext()){
        		p=pit.next().getValue();
        		parr.append(p.getMetaData());
        	}
        	parr.sortIt(new ArrayOfStructComparator(KeyConstants._name));
        	sct.set(KeyConstants._properties,parr);
        }

        page.metaData=new MetaDataSoftReference<Struct>(sct,creationTime);
        return sct;
    }    



	

	private static void metaUDFs(PageContext pc,ComponentImpl comp,Struct sct, int access) throws PageException {
    	ArrayImpl arr=new ArrayImpl();
    	//Collection.Key name;
    	Page page = comp._getPageSource().loadPage(pc, false);
    	// Page page = ((PageSourceImpl)comp._getPageSource()).getPage();
    	if(page!=null && page.udfs!=null){
    		for(int i=0;i<page.udfs.length;i++){
    			if(page.udfs[i].getAccess()>access) continue;
        		arr.append(ComponentUtil.getMetaData(pc,(UDFPropertiesBase) page.udfs[i]));
    		}
    	}
    	
    	// property functions
    	Iterator<Entry<Key, UDF>> it = comp._udfs.entrySet().iterator();
        Entry<Key, UDF> entry;
		UDF udf;
		while(it.hasNext()) {
    		entry= it.next();
    		udf=entry.getValue();
            if(udf.getAccess()>access || !(udf instanceof UDFGSProperty)) continue;
    			if(comp.base!=null) {
            		if(udf==comp.base.getMember(access,entry.getKey(),true,true))
            			continue;
            	}
            	arr.append(udf.getMetaData(pc));
            
        }
        if(arr.size()!=0)sct.set(KeyConstants._functions,arr);
	}

	public boolean isInitalized() {
        return isInit;
    }
    
    public void setInitalized(boolean isInit) {
        this.isInit=isInit;;
    }
    
        
    /**
     * sets a value to the current Component, dont to base Component
     * @param key
     * @param value
     * @return value set
     * @throws ExpressionException 
     */
    private synchronized Object _set(PageContext pc,Collection.Key key, Object value) throws ExpressionException {
    	if(value instanceof Member) {
    		if(value instanceof UDFPlus) {
            	UDFPlus udf = (UDFPlus)((UDFPlus)value).duplicate();
            	//udf.isComponentMember(true);///+++
            	udf.setOwnerComponent(this);
            	if(udf.getAccess()>Component.ACCESS_PUBLIC)
            		udf.setAccess(Component.ACCESS_PUBLIC);
            	_data.put(key,udf);
            	_udfs.put(key,udf);
            	hasInjectedFunctions=true;
            }
    		else _data.put(key,(Member)value);
    	}
        else {
        	Member existing = _data.get(key);
        	if(loaded && !isAccessible(pc, existing!=null?existing.getAccess():dataMemberDefaultAccess))
        		throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name ["+key+"]","enable [trigger data member] in admininistrator to also invoke getters and setters");
            _data.put(key,new DataMember(existing!=null?existing.getAccess():dataMemberDefaultAccess,existing!=null?existing.getModifier():Member.MODIFIER_NONE,value));
        }
        return value;
    }

    
/*
    public void reg(Collection.Key key, UDFPlus udf) throws ApplicationException {
    	registerUDF(key, udf,useShadow,false);
    }
    public void reg(String key, UDFPlus udf) throws ApplicationException {
    	registerUDF(KeyImpl.init(key), udf,useShadow,false);
    }*/

    @Override
    public void registerUDF(Collection.Key key, UDF udf) throws ApplicationException {
    	registerUDF(key, (UDFPlus) udf,useShadow,false);
    }

    @Override
    public void registerUDF(Collection.Key key, UDFProperties prop) throws ApplicationException {
    	registerUDF(key, new UDFImpl( prop),useShadow,false);
    }
    
    /*
     *  @deprecated injected is not used
     */
    public void registerUDF(Collection.Key key, UDFPlus udf,boolean useShadow,boolean injected) throws ApplicationException {
    	udf.setOwnerComponent(this);
    	if(insideStaticConstr) {
    		_static.put(key, udf);
    		return;
    	}
    	
    	// Abstact UDF
    	if(udf.getModifier()==MODIFIER_ABSTRACT) {
    		// abstract methods are not allowed
    		if(getModifier()!=MODIFIER_ABSTRACT) {
    			throw new ApplicationException("the abstract function ["+key+"] is not allowed within the no abstract component ["+_getPageSource().getDisplayPath()+"]");
    		}
    		if(absFin==null)absFin=new AbstractFinal();
    		absFin.add(key,udf);
    		
    		return; // abstract methods are not registered here
    	}
    	// Final UDF
    	else if(udf.getModifier()==MODIFIER_FINAL) {
    		if(absFin==null)absFin=new AbstractFinal();
    		absFin.add(key,udf);
    		
    	}
    	
    	
    	_udfs.put(key,udf);
    	_data.put(key,udf);
    	if(useShadow)scope.setEL(key, udf);
    }
    
	@Override
	public Object remove(Key key) throws PageException {
    	return _data.remove(key);
	}

    public Object removeEL(Collection.Key key) {
    	// MUST access muss beruecksichtigt werden
    	return _data.remove(key);
    }

    /*public Object set(PageContext pc, String name, Object value) throws PageException {
    	return set(pc, KeyImpl.init(name), value);
    }*/

    @Override
    public Object set(PageContext pc, Collection.Key key, Object value) throws PageException {
    	if(pc==null)pc=ThreadLocalPageContext.get();
    	if(triggerDataMember(pc) && isInit) {
    		if(!isPrivate(pc)) {
        		return callSetter(pc, key, value);
        	}
        }
    	return _set(pc,key,value);
    }

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
        return set(null,key,value);
	}

    /*public Object setEL(PageContext pc, String name, Object value) {
    	try {return set(pc, name, value);} 
    	catch (PageException e) {return null;}
    }*/
    
    @Override
    public Object setEL(PageContext pc, Collection.Key name, Object value) {
    	try {return set(pc, name, value);} 
    	catch (PageException e) {return null;}
    }

	@Override
	public Object setEL(Key key, Object value) {
    	return setEL(null, key, value);
	}
	
	@Override
	public final Object put(Object key, Object value) {
		// TODO find a better solution
		// when a orm entity the data given by put or also written to the variables scope
		if(entity) {
			getComponentScope().put(key, value);
		}
		return super.put(key, value);
	}
    
    /*public Object get(PageContext pc, String name) throws PageException {
        return get(pc, KeyImpl.init(name));
    }*/
    
	@Override
    public Object get(PageContext pc, Collection.Key key) throws PageException {
        Member member=getMember(pc,key,true,false);
        if(member!=null) return member.getValue();
        
        // trigger
        if(triggerDataMember(pc) && !isPrivate(pc)) {
        	return callGetter(pc,key);
        }
        throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name ["+key+"]","enable [trigger data member] in admininistrator to also invoke getters and setters");
        //throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name ["+name+"]");
    }

    private Object callGetter(PageContext pc,Collection.Key key) throws PageException {
    	Key getterName = KeyImpl.getInstance("get"+key.getLowerString());
    	Member member=getMember(pc,getterName,false,false);
        if(member instanceof UDFPlus) {
            UDFPlus udf = (UDFPlus)member;
            if(udf.getFunctionArguments().length==0 && udf.getReturnType()!=CFTypes.TYPE_VOID) {
                return _call(pc,getterName,udf,null,ArrayUtil.OBJECT_EMPTY);
            }
        } 
        throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name ["+key+"]");
	}
    
    private Object callGetter(PageContext pc,Collection.Key key, Object defaultValue) {
    	Key getterName = KeyImpl.getInstance("get"+key.getLowerString());
    	Member member=getMember(pc,getterName,false,false);
        if(member instanceof UDFPlus) {
            UDFPlus udf = (UDFPlus)member;
            if(udf.getFunctionArguments().length==0 && udf.getReturnType()!=CFTypes.TYPE_VOID) {
                try {
					return _call(pc,getterName,udf,null,ArrayUtil.OBJECT_EMPTY);
				} catch (PageException e) {
					return defaultValue;
				}
            }
        } 
        return defaultValue;
	}
    
    private Object callSetter(PageContext pc,Collection.Key key, Object value) throws PageException {
    	Collection.Key setterName = KeyImpl.getInstance("set"+key.getLowerString());
    	Member member=getMember(pc,setterName,false,false);
    	if(member instanceof UDFPlus) {
        	UDFPlus udf = (UDFPlus)member;
        	if(udf.getFunctionArguments().length==1 && (udf.getReturnType()==CFTypes.TYPE_VOID) || udf.getReturnType()==CFTypes.TYPE_ANY   ) {// TDOO support int return type
                return _call(pc,setterName,udf,null,new Object[]{value});
            }    
        }
        return _set(pc,key,value);
	}
    

	/**
     * return element that has at least given access or null
     * @param access
     * @param name
     * @return matching value
     * @throws PageException
     */
    public Object get(int access, String name) throws PageException {
        return get(access, KeyImpl.init(name));
    }
    
    public Object get(int access, Collection.Key key) throws PageException {
        Member member=getMember(access,key,true,false);
        if(member!=null) return member.getValue();
        
        // Trigger
        PageContext pc = ThreadLocalPageContext.get();
        if(triggerDataMember(pc) && !isPrivate(pc)) {
        	return callGetter(pc,key);
        }
        throw new ExpressionException("Component ["+getCallName()+"] has no accessible Member with name ["+key+"]");
    }

    /*public Object get(PageContext pc, String name, Object defaultValue) {
        return get(pc, KeyImpl.init(name), defaultValue);
    }*/

    @Override
    public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
        Member member=getMember(pc,key,true,false);
        if(member!=null) return member.getValue();
        
        // trigger
        if(triggerDataMember(pc) && !isPrivate(pc)) {
        	return callGetter(pc,key,defaultValue);
        }
        return defaultValue;
    }
    
    /**
     * return element that has at least given access or null
     * @param access
     * @param name
     * @return matching value
     */
    protected Object get(int access, String name, Object defaultValue) {
        return get(access, KeyImpl.init(name), defaultValue);
    }

    /**
     * @param access
     * @param key
     * @param defaultValue
     * @return
     */
    public Object get(int access, Collection.Key key, Object defaultValue) { 
        Member member=getMember(access,key,true,false);
        if(member!=null) return member.getValue();
        
        // trigger
        PageContext pc = ThreadLocalPageContext.get();
        if(triggerDataMember(pc) && !isPrivate(pc)) {
        	return callGetter(pc,key,defaultValue);
        }
        return defaultValue;
    }
    
	@Override
	public Object get(Collection.Key key) throws PageException {
    	return get(ThreadLocalPageContext.get(),key);
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
    	return get(ThreadLocalPageContext.get(),key,defaultValue);
	}

    @Override
    public Object call(PageContext pc, String name, Object[] args) throws PageException {
        return _call(pc,KeyImpl.init(name),null,args,false);
    }

	public Object call(PageContext pc, Collection.Key name, Object[] args) throws PageException {
		return _call(pc,name,null,args,false);
	}
    
    protected Object call(PageContext pc, int access, String name, Object[] args) throws PageException {
        return _call(pc,access,KeyImpl.init(name),null,args,false);
    }
    
    public Object call(PageContext pc, int access, Collection.Key name, Object[] args) throws PageException {
        return _call(pc,access,name,null,args,false);
    }

    @Override
    public Object callWithNamedValues(PageContext pc, String name, Struct args) throws PageException {
        return _call(pc,KeyImpl.init(name),args,null,false);
    }

    public Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException {
		return _call(pc,methodName,args,null,false);
	}
    
    protected Object callWithNamedValues(PageContext pc, int access, String name, Struct args) throws PageException {
        return _call(pc,access,KeyImpl.init(name),args,null,false);
    }
    
    public Object callWithNamedValues(PageContext pc, int access, Collection.Key name, Struct args) throws PageException {
        return _call(pc,access,name,args,null,false);
    }

    public boolean contains(PageContext pc,String name) {
       	return get(pc,KeyImpl.init(name),NullSupportHelper.NULL(pc))!=NullSupportHelper.NULL(pc);
    }

	/**
	 * @param pc
	 * @param key
	 * @return
	 */
	public boolean contains(PageContext pc,Key key) {
	   	return get(pc,key,NullSupportHelper.NULL(pc))!=NullSupportHelper.NULL(pc);
	}
	
	@Override
	public boolean containsKey(Key key) {
	   	return contains(ThreadLocalPageContext.get(),key);
	}
    
    public boolean contains(int access,String name) {
    	return get(access,name,NullSupportHelper.NULL())!=NullSupportHelper.NULL();
   }
    
    public boolean contains(int access,Key name) {
    	return get(access,name,NullSupportHelper.NULL())!=NullSupportHelper.NULL();
    }

    @Override
	public Iterator<Collection.Key> keyIterator() {
    	return keyIterator(getAccess(ThreadLocalPageContext.get()));
    }
    
	@Override
	public Iterator<String> keysAsStringIterator() {
    	return keysAsStringIterator(getAccess(ThreadLocalPageContext.get()));
    }
	
	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return entryIterator(getAccess(ThreadLocalPageContext.get()));
	}

	@Override
    public Collection.Key[] keys() {
    	return keys(getAccess(ThreadLocalPageContext.get()));
    }

    @Override
    public int size() {
    	return size(getAccess(ThreadLocalPageContext.get()));
    }

    
    @Override
    public Class<?> getJavaAccessClass(RefBoolean isNew) throws PageException {
    	return getJavaAccessClass(ThreadLocalPageContext.get(),isNew);
    }
    
    public Class<?> getJavaAccessClass(PageContext pc,RefBoolean isNew) throws PageException {
    	return getJavaAccessClass(pc,isNew, false,true,true,true,false,false);
    }

    @Override
    public Class<?> getJavaAccessClass(PageContext pc,RefBoolean isNew,boolean writeLog, boolean takeTop, boolean create, boolean suppressWSbeforeArg) throws PageException {
    	isNew.setValue(false);
    	ComponentProperties props =(takeTop)?top.properties:properties;
    	if(props.javaAccessClass==null) {
    		props.javaAccessClass=ComponentUtil.getComponentJavaAccess(pc,this,isNew,create,writeLog,suppressWSbeforeArg,true,false);
		}
    	return props.javaAccessClass;
    }

    @Override
    public Class<?> getJavaAccessClass(PageContext pc,RefBoolean isNew,boolean writeLog, boolean takeTop, boolean create, boolean suppressWSbeforeArg, boolean output,boolean returnValue) throws PageException {
    	isNew.setValue(false);
    	ComponentProperties props =(takeTop)?top.properties:properties;
    	if(props.javaAccessClass==null) {
    		props.javaAccessClass=ComponentUtil.getComponentJavaAccess(pc,this,isNew,create,writeLog,suppressWSbeforeArg,output,returnValue);
		}
    	return props.javaAccessClass;
    }
    
    public boolean isPersistent() {
    	return top.properties.persistent;
    }
    
    public boolean isAccessors() {
    	return top.properties.accessors;
    }

	public void setProperty(Property property) throws PageException {
		top.properties.properties.put(StringUtil.toLowerCase(property.getName()),property);
		if(top.properties.persistent || top.properties.accessors){
			if(property.getDefault()!=null)scope.setEL(KeyImpl.init(property.getName()), property.getDefault());
			PropertyFactory.createPropertyUDFs(this,property);
		}
	}

	

	private void initProperties() throws PageException {
		top.properties.properties=new LinkedHashMap<String,Property>();
		
		// MappedSuperClass  
		if(isPersistent() && !isBasePeristent() && top.base!=null && top.base.properties.properties!=null && top.base.properties.meta!=null) {
			boolean msc = Caster.toBooleanValue(top.base.properties.meta.get(KeyConstants._mappedSuperClass,Boolean.FALSE),false);
			if(msc){
				Property p;
				Iterator<Entry<String, Property>> it = top.base.properties.properties.entrySet().iterator();
				while(it.hasNext())	{
					p = it.next().getValue();
					if(p.isPeristent()) {
						
						setProperty(p);
					}
				}
			}
		}
	}

	public Property[] getProperties(boolean onlyPeristent) {
		return getProperties(onlyPeristent, false,false,false);
	}

	public Property[] getProperties(boolean onlyPeristent, boolean includeBaseProperties, boolean preferBaseProperties, boolean inheritedMappedSuperClassOnly) {
		Map<String,Property> props=new LinkedHashMap<String,Property>();
		_getProperties(top,props,onlyPeristent, includeBaseProperties, preferBaseProperties, inheritedMappedSuperClassOnly);
		return props.values().toArray(new Property[props.size()]);
	}

	private static void _getProperties(ComponentImpl c,Map<String,Property> props,boolean onlyPeristent, boolean includeBaseProperties, boolean preferBaseProperties, boolean inheritedMappedSuperClassOnly) {
		//if(c.properties.properties==null) return new Property[0];
		
		// collect with filter
		if(c.properties.properties!=null){
			Property p;
			Iterator<Entry<String, Property>> it = c.properties.properties.entrySet().iterator();
			while(it.hasNext())	{
				p = it.next().getValue();
				if(!onlyPeristent || p.isPeristent()) {
					if (!preferBaseProperties || !props.containsKey(p.getName().toLowerCase())) {
						props.put(p.getName().toLowerCase(),p);
					}
				}
			}
		}

		// MZ: Moved to the bottom to allow base properties to override inherited versions
		if(includeBaseProperties && c.base!=null) {
			if (!inheritedMappedSuperClassOnly || (c.base.properties.meta != null && Caster.toBooleanValue(c.base.properties.meta.get(KeyConstants._mappedSuperClass, Boolean.FALSE), false))) {
				_getProperties(c.base, props, onlyPeristent, includeBaseProperties, preferBaseProperties, inheritedMappedSuperClassOnly);
			}
		}

	}

	public ComponentScope getComponentScope() {
		return scope;
	}


	@Override
	public int compareTo(boolean b) throws PageException {
		return Operator.compare(castToBooleanValue(), b);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return Operator.compare((Date)castToDateTime(), (Date)dt);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return Operator.compare(castToDoubleValue(), d);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return Operator.compare(castToString(), str);
	}

	public void addConstructorUDF(Key key, UDF udf) throws ApplicationException {
		registerUDF(key, (UDFPlus)udf, false, true);
		/*if(constructorUDFs==null)
			constructorUDFs=new HashMap<Key,UDF>();
		constructorUDFs.put(key, value);*/
	}

// MUST more native impl
	public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
		boolean pcCreated=false;
		PageContext pc = ThreadLocalPageContext.get();
		try{
			if(pc==null){
				pcCreated=true;
				ConfigWeb config = (ConfigWeb) ThreadLocalPageContext.getConfig();
				Pair[] parr = new Pair[0];
				pc=ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", "/","", new Cookie[0], parr,null, parr, new StructImpl(),true,-1);
			}
			
			// reading fails for serialized data from Lucee version 4.1.2.002
			String name = in.readUTF();
			
			if(name.startsWith("evaluateComponent('") && name.endsWith("})")) {
				readExternalOldStyle(pc, name);
				return;
			}
			
			String md5 = in.readUTF();
			Struct _this = Caster.toStruct(in.readObject(),null);
			Struct _var = Caster.toStruct(in.readObject(),null);
		    
			try {
				ComponentImpl other=(ComponentImpl)EvaluateComponent.invoke(pc, name, md5, _this,_var);
				_readExternal(other);
			}
			catch (PageException e) {
				throw ExceptionUtil.toIOException(e);
			}
		}
		finally {
			if(pcCreated)ThreadLocalPageContext.release();
		}
	}

	private void readExternalOldStyle(PageContext pc, String str) throws IOException {
		try {
			ComponentImpl other=(ComponentImpl) new CFMLExpressionInterpreter(false).interpret(pc,str);
			_readExternal(other);
		}
		catch (PageException e) {
			throw ExceptionUtil.toIOException(e);
		}
	}

	private void _readExternal(ComponentImpl other) {
		this._data=other._data;
		this._udfs=other._udfs;
		setOwner(_udfs);
		setOwner(_data);
		this.afterConstructor=other.afterConstructor;
		this.base=other.base;
		//this.componentPage=other.componentPage;
		this.pageSource=other.pageSource;
		//this.constructorUDFs=other.constructorUDFs;
		this.dataMemberDefaultAccess=other.dataMemberDefaultAccess;
		this.absFin=other.absFin;
		this.isInit=other.isInit;
		this.properties=other.properties;
		this.scope=other.scope;
		this.top=this;
		//this._triggerDataMember=other._triggerDataMember;
		this.hasInjectedFunctions=other.hasInjectedFunctions;
		this.isExtended=other.isExtended;
		this.useShadow=other.useShadow;
		this.entity=other.entity;
		this._static=other._static;
	}

	private void  setOwner(Map<Key,? extends Member> data) {
		Member m;
		Iterator<? extends Member> it = data.values().iterator();
		while(it.hasNext()){
			m=it.next();
			if(m instanceof UDFPlus) {
				((UDFPlus)m).setOwnerComponent(this);
			}
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		ComponentSpecificAccess cw = new ComponentSpecificAccess(Component.ACCESS_PRIVATE,this);  
        Struct _this=new StructImpl();
		Struct _var=new StructImpl();
		
		
		// this scope (removing all UDFs)
		Object member;
	    {
	    	Iterator<Entry<Key, Object>> it = cw.entryIterator();
	        Entry<Key, Object> e;
	        while(it.hasNext()) {
	            e = it.next();
	            member = e.getValue();
	            if(member instanceof UDF)continue;
	            _this.setEL(e.getKey(), member);
	        }
		}
		
	    
	    // variables scope (removing all UDFs and key "this")
	    {
        	ComponentScope scope = getComponentScope();
        	Iterator<Entry<Key, Object>> it = scope.entryIterator();
            Entry<Key, Object> e;
        	Key k;
        	while(it.hasNext()) {
        		e = it.next();
        		k = e.getKey();
                if(KeyConstants._THIS.equalsIgnoreCase(k))continue;
                member = e.getValue();
                if(member instanceof UDF)continue;
	            _var.setEL(e.getKey(), member);
            }
        }
	    
	    out.writeUTF(getAbsName());
		out.writeUTF(ComponentUtil.md5(cw));
		out.writeObject(_this);
		out.writeObject(_var);
		
	}

	@Override
	public Component getBaseComponent() {
		return base;
	}	
	

	private boolean triggerDataMember(PageContext pc) {
		// dialect Lucee always triggers data members
		if(pageSource.getDialect()==CFMLEngine.DIALECT_LUCEE) return true;
		
		//if(_triggerDataMember!=null) return _triggerDataMember.booleanValue();
		
		
		if(pc!=null && pc.getApplicationContext()!=null)
			return pc.getApplicationContext().getTriggerComponentDataMember();
		
		Config config = ThreadLocalPageContext.getConfig();
		if(config!=null)
			return config.getTriggerComponentDataMember();
		
		return false;
	}

	public void setLoaded(boolean loaded) {
		this.loaded=loaded;
	}

	public boolean hasInjectedFunctions() {
		return hasInjectedFunctions;
	}

	@Override
	public void setEntity(boolean entity) {
		this.entity=entity;
	}

	@Override
	public boolean isEntity() {
		return entity;
	}

	@Override
	public Scope staticScope() {
		return _static;
	}

	@Override
	public Interface[] getInterfaces() {
		if(top.absFin==null) return EMPTY;
		return top.absFin.getInterfaces();
	}

	@Override
	public String id() {
		try {
			return Hash.md5(getPageSource().getDisplayPath());
		}
		catch (NoSuchAlgorithmException e) {
			return getPageSource().getDisplayPath();
		}
	}

	@Override
	public int getType() {
		return StructUtil.getType(_data);
	}
}