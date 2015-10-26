package lucee.runtime.functions.system;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.io.CharsetUtil;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.mimetype.ContentType;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.URLEncoder;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.functions.other.CreatePageContext;
import lucee.runtime.net.http.HttpServletResponseDummy;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Form;
import lucee.runtime.type.util.KeyConstants;

public class InternalRequest implements Function {

	private static final long serialVersionUID = -8163856691035353577L;

	public static final Key FILECONTENT_BYNARY = KeyImpl._const("filecontent_binary");

	public static Struct call(PageContext pc, String template, String method, Struct urls,Struct forms, Struct cookies, Struct headers) throws PageException {
		// charset
		Charset reqCharset=pc.getWebCharset();
		
		String ext=ResourceUtil.getExtension(template, null);
		// welcome files
		if(StringUtil.isEmpty(ext)) {
			throw new FunctionException(pc,"Invoke",1,"url","welcome file listing not supported, please define the template name.");
		}
		
		// dialect
		int dialect=((CFMLFactoryImpl)pc.getConfig().getFactory()).toDialect(ext,-1);
		if(dialect==-1) dialect=pc.getCurrentTemplateDialect();
		// CFMLEngine.DIALECT_LUCEE
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		PageContextImpl _pc=createPageContext(pc, template, urls, cookies, headers, reqCharset, baos);
		fillForm(_pc,forms);
		Collection cookie,request,session;
		int status;
		long exeTime;
		boolean isText=false;
		Charset charset=null;
		try{
			
			if(CFMLEngine.DIALECT_LUCEE==dialect)
				_pc.execute(template, true, false);
			else 
				_pc.executeCFML(template, true, false);
			
		}
		finally{
			_pc.flush();
			cookie=_pc.cookieScope().duplicate(false);
			request=_pc.requestScope().duplicate(false);
			session=_pc.sessionScope().duplicate(false);
			exeTime=System.currentTimeMillis()-pc.getStartTime();
			//debugging=_pc.getDebugger().getDebuggingData(_pc).duplicate(false);
			
			HttpServletResponseDummy rsp = (HttpServletResponseDummy) _pc.getHttpServletResponse();
			
			// headers
			Collection.Key name;
			headers=new StructImpl();
			Iterator<String> it = rsp.getHeaderNames().iterator();
			Object value;
			Array arr;
			while(it.hasNext()){
				name=KeyImpl.init(it.next());
				value=rsp.getHeaders(name.getString());
				if(Decision.isSimpleValue(value))
					headers.set(name, value);
				else {
					arr = Caster.toArray(value,null);
					if(arr!=null){
						if(arr.size()>1)
							headers.set(name, arr);
						else
							headers.set(name, arr.getE(1));
					}
					else headers.set(name, value);
				}	
			}
			
			
			// status
			status = rsp.getStatus();
			ContentType ct = HTTPUtil.toContentType(rsp.getContentType(),null);
			if(ct!=null){
				isText = HTTPUtil.isTextMimeType(ct.getMimeType());
				if(ct.getCharset()!=null) charset=CharsetUtil.toCharset(ct.getCharset(),null);
			}
			releasePageContext(pc,_pc);
			
		}
		Struct rst=new StructImpl();
		
		byte[] barr=baos.toByteArray();
		if(isText) rst.set(KeyConstants._filecontent, new String(barr,charset==null?reqCharset:charset));
		else rst.set(FILECONTENT_BYNARY,barr);
		rst.set(KeyConstants._cookies, cookie);
		rst.set(KeyConstants._request, request);
		rst.set(KeyConstants._session, session);
		rst.set(KeyConstants._headers, headers);
		//rst.put(KeyConstants._debugging, debugging);
		rst.set(KeyConstants._executionTime, new Double(exeTime));
		rst.set(KeyConstants._status, new Double(status));
		return rst;
    }

	private static void fillForm(PageContextImpl _pc, Struct src) throws PageException {
		if(src==null) return;
		Iterator<Entry<Key, Object>> it = src.entryIterator();
		Form trg = _pc.formScope();
		Entry<Key, Object> e;
		while(it.hasNext()) {
			e = it.next();
			trg.set(e.getKey(), e.getValue());
		}
	}

	private static PageContextImpl createPageContext(PageContext pc, String template, Struct urls, Struct cookies, Struct headers, Charset charset, OutputStream os) throws PageException {
		
		// query string | URL
				Entry<Key, Object> e;
				StringBuilder sbQS=new StringBuilder();
				if(urls!=null) {
					Iterator<Entry<Key, Object>> it = urls.entryIterator();
					while(it.hasNext()) {
						e = it.next();
						sbQS.append(urlenc(e.getKey().getString(),charset));
						sbQS.append('=');
						sbQS.append(urlenc(Caster.toString(e.getValue()), charset));
					}
				}
		
		return ThreadUtil.createPageContext(
				pc.getConfig(), 
				os, 
				pc.getHttpServletRequest().getServerName(), 
				 template, 
				sbQS.toString(), 
				CreatePageContext.toCookies(cookies), 
				CreatePageContext.toPair(headers,true), 
				CreatePageContext.toPair(new StructImpl(),true), 
				CreatePageContext.castValuesToString(new StructImpl()),true,-1);
	}
	
	private static void releasePageContext(PageContext pc, PageContext oldPC) {
		pc.flush();
		ThreadLocalPageContext.release();
		if(oldPC!=null)ThreadLocalPageContext.register(oldPC);
	}
	
	private static String urlenc(String str, Charset charset) throws PageException {
		try{
			if(!ReqRspUtil.needEncoding(str,false)) return str;
			return URLEncoder.encode(str,charset);
		}
		catch(UnsupportedEncodingException uee){
			throw Caster.toPageException(uee);
		}
    }
}
