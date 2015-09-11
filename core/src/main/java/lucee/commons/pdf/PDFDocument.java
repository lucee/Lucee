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
package lucee.commons.pdf;

import java.awt.Dimension;
import java.awt.Insets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;

import lucee.Info;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.HTMLEntities;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPEngine;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.system.ContractPath;
import lucee.runtime.functions.system.GetDirectoryFromPath;
import lucee.runtime.net.http.ReqRspUtil;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.runtime.util.URLResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class PDFDocument {

	// PageType
    public static final Dimension PAGETYPE_ISOB5 = new Dimension(501, 709);
    public static final Dimension PAGETYPE_ISOB4 = new Dimension(709, 1002);
    public static final Dimension PAGETYPE_ISOB3 = new Dimension(1002, 1418);
    public static final Dimension PAGETYPE_ISOB2 = new Dimension(1418, 2004);
    public static final Dimension PAGETYPE_ISOB1 = new Dimension(2004, 2836);
    public static final Dimension PAGETYPE_ISOB0 = new Dimension(2836, 4008);
    public static final Dimension PAGETYPE_HALFLETTER = new Dimension(396, 612);
    public static final Dimension PAGETYPE_LETTER = new Dimension(612, 792);
    public static final Dimension PAGETYPE_TABLOID = new Dimension(792, 1224);
    public static final Dimension PAGETYPE_LEDGER = new Dimension(1224, 792);
    public static final Dimension PAGETYPE_NOTE = new Dimension(540, 720);
    public static final Dimension PAGETYPE_LEGAL = new Dimension(612, 1008);
	
    public static final Dimension PAGETYPE_A10 = new Dimension(74, 105);
    public static final Dimension PAGETYPE_A9 = new Dimension(105, 148);
    public static final Dimension PAGETYPE_A8 = new Dimension(148, 210);
    public static final Dimension PAGETYPE_A7 = new Dimension(210, 297);
    public static final Dimension PAGETYPE_A6 = new Dimension(297, 421);
    public static final Dimension PAGETYPE_A5 = new Dimension(421, 595);
    public static final Dimension PAGETYPE_A4 = new Dimension(595, 842);
    public static final Dimension PAGETYPE_A3 = new Dimension(842, 1190);
    public static final Dimension PAGETYPE_A2 = new Dimension(1190, 1684);
    public static final Dimension PAGETYPE_A1 = new Dimension(1684, 2384);
    public static final Dimension PAGETYPE_A0 = new Dimension(2384, 3370);
	
	
	public static final Dimension PAGETYPE_B4=new Dimension(708,1000);
	public static final Dimension PAGETYPE_B5=new Dimension(499,708);
	public static final Dimension PAGETYPE_B4_JIS=new Dimension(728,1031);
	public static final Dimension PAGETYPE_B5_JIS=new Dimension(516,728);
	public static final Dimension PAGETYPE_CUSTOM=new Dimension(1,1);
			
	// encryption
	public static final int ENC_NONE=0;
	public static final int ENC_40BIT=1;
	public static final int ENC_128BIT=2;
	
	//	fontembed 
	public static final int FONT_EMBED_NO=0;
	public static final int FONT_EMBED_YES=1;
	public static final int FONT_EMBED_SELECCTIVE=FONT_EMBED_YES;

	// unit
	public static final double UNIT_FACTOR_CM=85d/3d;// =28.333333333333333333333333333333333333333333;
	public static final double UNIT_FACTOR_IN=UNIT_FACTOR_CM*2.54;
	public static final double UNIT_FACTOR_POINT=1;
		
	// margin init
	private static final int MARGIN_INIT=36;

	// mimetype
	private static final int MIMETYPE_TEXT_HTML = 0;
	private static final int MIMETYPE_TEXT = 1;
	private static final int MIMETYPE_IMAGE = 2;  
	private static final int MIMETYPE_APPLICATION = 3;
	private static final int MIMETYPE_OTHER = -1;
		
	private double margintop=-1;
	private double marginbottom=-1;
	private double marginleft=-1;
	private double marginright=-1;

	private int mimetype=MIMETYPE_TEXT_HTML;
	private String strMimetype=null;
	private String strCharset=null;

	private boolean backgroundvisible;
	private boolean fontembed=true;
	private PDFPageMark header;
	private PDFPageMark footer;
	
	private String proxyserver;
	private int proxyport=80;
	private String proxyuser=null;
	private String proxypassword="";

	private String src=null;
	private Resource srcfile=null;
	private String body;
	//private boolean isEvaluation;
	private String name;
	private String authUser;
	private String authPassword;
	private String userAgent;
	private boolean localUrl;
	private boolean bookmark; 
	private boolean htmlBookmark;
	
	
	
	public PDFDocument(){
		Info info = CFMLEngineFactory.getInstance().getInfo();
		userAgent= Constants.NAME+" "+info.getVersion()+" "+info.getStateAsString();
		
	}
	
	public void setHeader(PDFPageMark header) {
		this.header=header;
	}

	public void setFooter(PDFPageMark footer) {
		this.footer=footer;
	}
	

	/**
	 * @param marginbottom the marginbottom to set
	 */
	public void setMarginbottom(double marginbottom) {
		this.marginbottom = marginbottom;
	}

	/**
	 * @param marginleft the marginleft to set
	 */
	public void setMarginleft(double marginleft) {
		this.marginleft = marginleft;
	}

	/**
	 * @param marginright the marginright to set
	 */
	public void setMarginright(double marginright) {
		this.marginright = marginright;
	}

	/**
	 * @param margintop the margintop to set
	 */
	public void setMargintop(double margintop) {
		this.margintop = margintop;
	}
	
	/**
	 * @param strMimetype the mimetype to set
	 */
	public void setMimetype(String strMimetype) {
		strMimetype = strMimetype.toLowerCase().trim();
		this.strMimetype=strMimetype;
		// mimetype
		if(strMimetype.startsWith("text/html"))			mimetype=MIMETYPE_TEXT_HTML;
		else if(strMimetype.startsWith("text/"))		mimetype=MIMETYPE_TEXT;
		else if(strMimetype.startsWith("image/"))		mimetype=MIMETYPE_IMAGE;
		else if(strMimetype.startsWith("application/"))		mimetype=MIMETYPE_APPLICATION;
		else mimetype=MIMETYPE_OTHER;
		
		// charset
		String[] arr = ListUtil.listToStringArray(strMimetype, ';');
		if(arr.length>=2) {
			this.strMimetype=arr[0].trim();
			for(int i=1;i<arr.length;i++) {
				String[] item = ListUtil.listToStringArray(arr[i], '=');
				if(item.length==1) {
					strCharset=item[0].trim();
					break;
				}
				else if(item.length==2 && item[0].trim().equals("charset")) {
					strCharset=item[1].trim();
					break;
				}
			}
		}
	}
	
	/** set the value proxyserver
	*  Host name or IP address of a proxy server.
	* @param proxyserver value to set
	**/
	public void setProxyserver(String proxyserver)	{
		this.proxyserver=proxyserver;
	}
	
	/** set the value proxyport
	*  The port number on the proxy server from which the object is requested. Default is 80. When 
	* 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically 
	* 	resolved to preserve links in the retrieved document.
	* @param proxyport value to set
	**/
	public void setProxyport(int proxyport)	{
		this.proxyport=proxyport;
	}

	/** set the value username
	*  When required by a proxy server, a valid username.
	* @param proxyuser value to set
	**/
	public void setProxyuser(String proxyuser)	{
		this.proxyuser=proxyuser;
	}

	/** set the value password
	*  When required by a proxy server, a valid password.
	* @param proxypassword value to set
	**/
	public void setProxypassword(String proxypassword)	{
		this.proxypassword=proxypassword;
	}

	/**
	 * @param src
	 * @throws PDFException
	 */
	public void setSrc(String src) throws PDFException {
		if(srcfile!=null) throw new PDFException("You cannot specify both the src and srcfile attributes");
		this.src = src;
	}
	

	/**
	 * @param srcfile the srcfile to set
	 * @throws PDFException 
	 */
	public void setSrcfile(Resource srcfile) throws PDFException {
		if(src!=null) throw new PDFException("You cannot specify both the src and srcfile attributes");
		this.srcfile=srcfile;
	}

	public void setBody(String body) {
		this.body=body;
	}

	public byte[] render(Dimension dimension,double unitFactor, PageContext pc,boolean generateOutlines) throws PageException, IOException {
		ConfigWeb config = pc.getConfig();
		PDF pd4ml = new PDF(config);
		pd4ml.generateOutlines(generateOutlines);
		pd4ml.enableTableBreaks(true);
		pd4ml.interpolateImages(true);
		// MUSTMUST DO NOT ENABLE, why this was disabled
		pd4ml.adjustHtmlWidth();
		
		//check size
		int mTop = 	toPoint(margintop,unitFactor);
		int mLeft = toPoint(marginleft,unitFactor);
		int mBottom=toPoint(marginbottom,unitFactor);
		int mRight=toPoint(marginright,unitFactor);
		if((mLeft+mRight)>dimension.getWidth())
			throw new ExpressionException("current document width ("+Caster.toString(dimension.getWidth())+" point) is smaller that specified horizontal margin  ("+Caster.toString(mLeft+mRight)+" point).",
					"1 in = "+Math.round(1*UNIT_FACTOR_IN)+" point and 1 cm = "+Math.round(1*UNIT_FACTOR_CM)+" point");
		if((mTop+mBottom)>dimension.getHeight())
			throw new ExpressionException("current document height ("+Caster.toString(dimension.getHeight())+" point) is smaller that specified vertical margin  ("+Caster.toString(mTop+mBottom)+" point).",
					"1 in = "+Math.round(1*UNIT_FACTOR_IN)+" point and 1 cm = "+Math.round(1*UNIT_FACTOR_CM)+" point");
		
		// Size
		pd4ml.setPageInsets(new Insets(mTop,mLeft,mBottom,mRight));
		pd4ml.setPageSize(dimension);
		
		// header
		if(header!=null) pd4ml.setPageHeader(header);
		// footer
		if(footer!=null) pd4ml.setPageFooter(footer);
		
		// content
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		try {
			content(pd4ml,pc,baos);
			
		}
		finally {
			IOUtil.closeEL(baos);
		}
		return baos.toByteArray();
	}

	private void content(PDF pd4ml, PageContext pc, OutputStream os) throws PageException, IOException {
		ConfigWeb config = pc.getConfig();
		pd4ml.useTTF("java:fonts", fontembed);
		
		// body
    	if(!StringUtil.isEmpty(body,true)) {
    		// optimize html
    		URL base = getBase(pc);
    		try {
    			body=beautifyHTML(new InputSource(new StringReader(body)),base);
			}catch (Throwable t) {}
			
    		pd4ml.render(body, os,base);
			
    	}
    	// srcfile
    	else if(srcfile!=null) {
    		if(StringUtil.isEmpty(strCharset))strCharset=((PageContextImpl)pc).getResourceCharset().name();
    		
			// mimetype
			if(StringUtil.isEmpty(strMimetype)) {
				String mt = ResourceUtil.getMimeType(srcfile,null);
				if(mt!=null) setMimetype(mt);
			}
			InputStream is = srcfile.getInputStream();
    		try {
    			
    			URL base = new URL("file://"+srcfile);
    			if(!localUrl){
    				//PageContext pc = Thread LocalPageContext.get();
    				
	    				String abs = srcfile.getAbsolutePath();
	    				String contract = ContractPath.call(pc, abs);
	    				if(!abs.equals(contract)) {
	    					base=HTTPUtil.toURL(ReqRspUtil.getDomain(pc.getHttpServletRequest())+contract,true);
	    				}

    			}
    			
    			//URL base = localUrl?new URL("file://"+srcfile):getBase();
    			render(pd4ml, is,os,base);
			} 
    		catch (Throwable t) {}
    		finally {
    			IOUtil.closeEL(is);
    		}
    	}
    	// src
    	else if(src!=null) {
    		if(StringUtil.isEmpty(strCharset))strCharset="iso-8859-1";
    		URL url = HTTPUtil.toURL(src,true);
			
			// set Proxy
			if(StringUtil.isEmpty(proxyserver) && config.isProxyEnableFor(url.getHost())) {
				ProxyData pd = config.getProxyData();
				proxyserver=pd==null?null:pd.getServer();
				proxyport=pd==null?0:pd.getPort();
				proxyuser=pd==null?null:pd.getUsername();
				proxypassword=pd==null?null:pd.getPassword();
			}
			
			HTTPResponse method = HTTPEngine.get(url, authUser, authPassword, -1,true, null, userAgent,
					ProxyDataImpl.getInstance(proxyserver, proxyport, proxyuser, proxypassword),null);
			
			// mimetype
			if(StringUtil.isEmpty(strMimetype)) {
				ContentType ct = method.getContentType();
				if(ct!=null)
					setMimetype(ct.toString());
				
			}
			InputStream is = new ByteArrayInputStream(method.getContentAsByteArray());
			try {
				
				render(pd4ml, is, os,url);
			}
			finally {
				IOUtil.closeEL(is);
			}
    	}
    	else {
    		pd4ml.render("<html><body> </body></html>", os,null);
    	}
	}

	private static String beautifyHTML(InputSource is,URL base) throws ExpressionException, SAXException, IOException {
		Document xml = XMLUtil.parse(is,null,true);
		patchPD4MLProblems(xml);
		
		if(base!=null)URLResolver.getInstance().transform(xml, base);
		String html = XMLCaster.toHTML(xml);
		return html;
	}

	private static void patchPD4MLProblems(Document xml) {
		Element b = XMLUtil.getChildWithName("body", xml.getDocumentElement());
		if(!b.hasChildNodes()){
			b.appendChild(xml.createTextNode(" "));
		}
	}


	private static URL getBase(PageContext pc) throws MalformedURLException {
		//PageContext pc = Thread LocalPageContext.get();
		if(pc==null)return null;
		
		String userAgent = pc.getHttpServletRequest().getHeader("User-Agent");
		// bug in pd4ml-> html badse definition create a call
		if(!StringUtil.isEmpty(userAgent) && userAgent.startsWith("Java"))return null;
		
		return HTTPUtil.toURL(GetDirectoryFromPath.call(pc, ReqRspUtil.getRequestURL(pc.getHttpServletRequest(), false)),true);
	}


	private void render(PDF pd4ml, InputStream is,OutputStream os, URL base) throws IOException, PageException {
		try {
			
			// text/html
			if(mimetype==MIMETYPE_TEXT_HTML) {
				body="";
				
				try {
					InputSource input = new InputSource(IOUtil.getReader(is,CharsetUtil.toCharset(strCharset)));
					body=beautifyHTML(input,base);
				} 
				catch (Throwable t) {}
				//else if(body==null)body =IOUtil.toString(is,strCharset); 
				pd4ml.render(body, os,base);
			}
			// text
			else if(mimetype==MIMETYPE_TEXT) {
				body =IOUtil.toString(is,strCharset); 
				body="<html><body><pre>"+HTMLEntities.escapeHTML(body)+"</pre></body></html>";
				pd4ml.render(body, os,null);
			}
			// image
			else if(mimetype==MIMETYPE_IMAGE) {
				Resource tmpDir= SystemUtil.getTempDirectory();
				Resource tmp = tmpDir.getRealResource(this+"-"+Math.random());
				IOUtil.copy(is, tmp,true);
				body="<html><body><img src=\"file://"+tmp+"\"></body></html>";
				try {
					pd4ml.render(body, os,null);
				}
				finally {
					tmp.delete();
				}	
			}
			// Application
			else if(mimetype==MIMETYPE_APPLICATION && "application/pdf".equals(strMimetype)) {
				IOUtil.copy(is, os,true,true);
			}
			else pd4ml.render(new InputStreamReader(is), os);
		}
		finally {
			IOUtil.closeEL(is,os);
		}
	}

	public static int toPoint(double value,double unitFactor) {
		if(value<0) return MARGIN_INIT;
		return (int)Math.round(value*unitFactor);
		//return r;
	}

	public PDFPageMark getHeader() {
		return header;
	}
	public PDFPageMark getFooter() {
		return footer;
	}

	public void setFontembed(int fontembed) {
		this.fontembed=fontembed!=FONT_EMBED_NO;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * @return the authUser
	 */
	public String getAuthUser() {
		return authUser;
	}


	/**
	 * @param authUser the authUser to set
	 */
	public void setAuthUser(String authUser) {
		this.authUser = authUser;
	}


	/**
	 * @return the authPassword
	 */
	public String getAuthPassword() {
		return authPassword;
	}


	/**
	 * @param authPassword the authPassword to set
	 */
	public void setAuthPassword(String authPassword) {
		this.authPassword = authPassword;
	}


	/**
	 * @return the userAgent
	 */
	public String getUserAgent() {
		return userAgent;
	}


	/**
	 * @param userAgent the userAgent to set
	 */
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}


	/**
	 * @return the proxyserver
	 */
	public String getProxyserver() {
		return proxyserver;
	}


	/**
	 * @return the proxyport
	 */
	public int getProxyport() {
		return proxyport;
	}


	/**
	 * @return the proxyuser
	 */
	public String getProxyuser() {
		return proxyuser;
	}


	/**
	 * @return the proxypassword
	 */
	public String getProxypassword() {
		return proxypassword;
	}


	public boolean hasProxy() {
		return !StringUtil.isEmpty(proxyserver);
	}


	/**
	 * @return the localUrl
	 */
	public boolean getLocalUrl() {
		return localUrl;
	}


	/**
	 * @param localUrl the localUrl to set
	 */
	public void setLocalUrl(boolean localUrl) {
		this.localUrl = localUrl;
	}


	/**
	 * @return the bookmark
	 */
	public boolean getBookmark() {
		return bookmark;
	}


	/**
	 * @param bookmark the bookmark to set
	 */
	public void setBookmark(boolean bookmark) {
		this.bookmark = bookmark;
	}


	/**
	 * @return the htmlBookmark
	 */
	public boolean getHtmlBookmark() {
		return htmlBookmark;
	}


	/**
	 * @param htmlBookmark the htmlBookmark to set
	 */
	public void setHtmlBookmark(boolean htmlBookmark) {
		this.htmlBookmark = htmlBookmark;
	}

}