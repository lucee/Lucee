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
package lucee.runtime.net.amf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.SystemOut;
import lucee.runtime.CFMLFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.CFMLEngineImpl;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.http.HttpServletRequestDummy;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.HttpUtil;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import flex.messaging.config.ConfigMap;

public class CFMLProxy {

	private static final Collection.Key FLASH = KeyImpl.intern("flash");
	private static final Collection.Key PARAMS = KeyImpl.intern("params");
	private static final Collection.Key RESULT = KeyImpl.intern("result");
	private static final Collection.Key AMF_FORWARD = KeyImpl.intern("AMF-Forward");
	private static final String TEMPLATE_EXTENSION = "cfm";
	
	

	public Object invokeBody(AMFCaster caster,ConfigMap configMap,ServletConfig config,HttpServletRequest req, 
			HttpServletResponse rsp,String serviceName,String serviceMethodName,List rawParams) throws ServletException, PageException,IOException { 
    
		//try { 
    	
    	
        // Forward
        CFMLFactory factory = CFMLEngineImpl.getInstance().getCFMLFactory(config, req);
        PageContext pc=null;

        // CFC Files
        String cfc;
        Object parameters=null;
        try {
            cfc="/"+serviceName.replace('.','/')+"."+Constants.getCFMLComponentExtension();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            pc=createPageContext(factory,cfc,"method="+serviceMethodName,baos,req,true,-1);
            PageSource source = ((PageContextImpl)pc).getPageSourceExisting(cfc);

            if(caster==null){
            	ConfigImpl ci = (ConfigImpl)pc.getConfig();
            	caster=ci.getAMFCaster();
            	if(caster==null) ci.setAMFCaster(caster=createCaster(ci,configMap));
            }
            parameters=caster.toCFMLObject(rawParams);
        	if(source!=null) {
        		print(pc,cfc+"?method="+serviceMethodName);
            	// Map
        			//print.err(parameters);
        		if(parameters instanceof Map){
        			//print.err("map");
            		pc.getHttpServletRequest().setAttribute("argumentCollection", parameters);
            	}
        		// List
            	else if(parameters instanceof List){
            		//print.err("list");
            		pc.getHttpServletRequest().setAttribute("argumentCollection", parameters);
            	}
            	else {
            		ArrayList list = new ArrayList();
            		list.add(parameters);
            		pc.getHttpServletRequest().setAttribute("argumentCollection", list);
            		
            	}
            	
                // Execute
                pc.executeCFML(cfc,true,true);

                // write back response
                writeBackResponse(pc,rsp);
                
                // After
	            return caster.toAMFObject(pc.variablesScope().get(AMF_FORWARD,null));
	            
            }
        }
        finally {
            if(pc!=null)factory.releaseLuceePageContext(pc,true);
        }    
        
     // CFML Files
        String cfml;
        try {
            cfml="/"+(serviceName.replace('.','/')+'/'+serviceMethodName.replace('.','/'))+"."+TEMPLATE_EXTENSION;
            pc=createPageContext(factory,cfml,"",null,req,true,-1);
            PageSource source = ((PageContextImpl)pc).getPageSourceExisting(cfml);
            
            if(source!=null) {
            	print(pc,cfml);
            	// Before
                Struct params=new StructImpl();
                pc.variablesScope().setEL(FLASH,params);
                params.setEL(PARAMS,parameters);
                
                // Execute
                pc.executeCFML(cfml,true,true);
                
                // write back response
                writeBackResponse(pc,rsp);
                
                // After
                Object flash=pc.variablesScope().get(FLASH,null);
                if(flash instanceof Struct) {
                	return caster.toAMFObject(((Struct)flash).get(RESULT,null));
                }
                return null;
            }
        }
        finally {
            if(pc!=null)factory.releaseLuceePageContext(pc,true);
        }
        
        throw new AMFException("can't find cfml ("+cfml+") or cfc ("+cfc+") matching the request");
    }
	
	private AMFCaster createCaster(ConfigImpl ci, ConfigMap properties) throws ClassException {
			
			Map amfCasterArguments=ci.getAMFCasterArguments();
			if(properties!=null){
				ConfigMap cases = properties.getPropertyAsMap("property-case", null);
		        if(cases!=null){
		        	if(!amfCasterArguments.containsKey("force-cfc-lowercase"))
		        		amfCasterArguments.put("force-cfc-lowercase",Caster.toBoolean(cases.getPropertyAsBoolean("force-cfc-lowercase", false)));
		        	if(!amfCasterArguments.containsKey("force-query-lowercase"))
		        		amfCasterArguments.put("force-query-lowercase",Caster.toBoolean(cases.getPropertyAsBoolean("force-query-lowercase", false)));
		        	if(!amfCasterArguments.containsKey("force-struct-lowercase"))
		        		amfCasterArguments.put("force-struct-lowercase",Caster.toBoolean(cases.getPropertyAsBoolean("force-struct-lowercase", false)));
		        	
		        }
		        ConfigMap access = properties.getPropertyAsMap("access", null);
		        if(access!=null){
		        	if(!amfCasterArguments.containsKey("use-mappings"))
		        		amfCasterArguments.put("use-mappings",Caster.toBoolean(access.getPropertyAsBoolean("use-mappings", false)));
		        	if(!amfCasterArguments.containsKey("method-access-level"))
		        		amfCasterArguments.put("method-access-level",access.getPropertyAsString("method-access-level","remote"));
		        }
			}
			
			AMFCaster amfCaster = (AMFCaster)ClassUtil.loadInstance(ci.getAMFCasterClass());
			amfCaster.init(amfCasterArguments);
			
			return amfCaster;
		}

	private PageContext createPageContext(CFMLFactory factory,String scriptName,String queryString, OutputStream os, 
			HttpServletRequest formerReq, boolean register,long timeout) {
		Resource root = factory.getConfig().getRootDirectory();
		if(os==null)os=DevNullOutputStream.DEV_NULL_OUTPUT_STREAM;
		
		// Request
		HttpServletRequestDummy req = new HttpServletRequestDummy(
				root,"localhost",scriptName,queryString,
				ReqRspUtil.getCookies(formerReq,((ConfigImpl)factory.getConfig()).getWebCharset()),
				HttpUtil.cloneHeaders(formerReq),
				HttpUtil.cloneParameters(formerReq),
				HttpUtil.getAttributesAsStruct(formerReq),null);
				req.addHeader("AMF-Forward", "true");
		HttpServletResponseDummy rsp = new HttpServletResponseDummy(os);

		return factory.getLuceePageContext(factory.getServlet(), req, rsp, null, false, -1, false,register,timeout,true,false);
	}
	
	private void writeBackResponse(PageContext pc, HttpServletResponse rsp) {
		HttpServletResponseDummy hsrd=(HttpServletResponseDummy) pc.getHttpServletResponse();
        
		// Cookie
		Cookie[] cookies = hsrd.getCookies();
        if(cookies!=null) {
        	for(int i=0;i<cookies.length;i++) {
        		rsp.addCookie(cookies[i]);
        	}
        }
        
        // header
        Pair<String,Object>[] headers = hsrd.getHeaders();
        Pair<String,Object> header ;
        Object value;
        if(headers!=null) {
        	for(int i=0;i<headers.length;i++) {
        		header=headers[i];
        		value=header.getValue();
        		if(value instanceof Long)rsp.addDateHeader(header.getName(), ((Long)value).longValue());
        		else if(value instanceof Integer)rsp.addDateHeader(header.getName(), ((Integer)value).intValue());
        		else rsp.addHeader(header.getName(), Caster.toString(header.getValue(),""));
        	}
        }
	}
	
	private void print(PageContext pc, String str) {
		if(pc.getConfig() instanceof ConfigWebImpl)
			SystemOut.printDate(((ConfigWebImpl)pc.getConfig()).getOutWriter(), str);
        
	}
	
	public static Class<? extends AMFCaster> toAMFCasterClass(Config config,String strCaster) {
		Log log=config.getLog("application");
        try{
			if(StringUtil.isEmpty(strCaster) || "classic".equalsIgnoreCase(strCaster)) 
	        	return ClassicAMFCaster.class;
	        else if("modern".equalsIgnoreCase(strCaster))
	        	return ModernAMFCaster.class;
	        else {
	        	Class caster = ClassUtil.loadClass(strCaster);
	        	if((caster.newInstance() instanceof AMFCaster)) {
	        		return caster;
	        	}
	        	log.error("Flex","object ["+Caster.toClassName(caster)+"] must implement the interface "+ResourceProvider.class.getName());
	        	
	        }
        }
        catch(Exception e){
        	log.error("Flex", e);
        }
        return ClassicAMFCaster.class;
	}
}