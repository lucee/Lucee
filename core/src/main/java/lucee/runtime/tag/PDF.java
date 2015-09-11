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
package lucee.runtime.tag;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.io.res.filter.ResourceFilter;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.io.res.util.WildCardFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;
import lucee.runtime.text.pdf.PDFDocument;
import lucee.runtime.text.pdf.PDFUtil;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

public class PDF extends BodyTagImpl  {

	private static final int ACTION_ADD_WATERMARK = 0;
	private static final int ACTION_DELETE_PAGES = 1;
	private static final int ACTION_GET_INFO = 2;
	private static final int ACTION_MERGE = 3;
	private static final int ACTION_PROCESSDDX = 5;
	private static final int ACTION_PROTECT = 5;
	private static final int ACTION_READ = 6;
	private static final int ACTION_REMOVE_WATERMARK = 7;
	private static final int ACTION_SET_INFO = 8;
	private static final int ACTION_THUMBNAIL = 9;
	private static final int ACTION_WRITE = 10;
	private static final int ACTION_EXTRACT_TEXT = 11;

	
	private static final String FORMAT_JPG ="jpg";
	private static final String FORMAT_TIFF = "tiff";
	private static final String FORMAT_PNG = "png";
	
	private static final int ORDER_TIME = 0;
	private static final int ORDER_NAME = 1;
	
	private static final int RESOLUTION_HIGH = 0;
	private static final int RESOLUTION_LOW = 1;

	private static final int SAVE_OPTION_FULL = 0;
	private static final int SAVE_OPTION_INCREMENTAL = 1;
	private static final int SAVE_OPTION_LINEAR = 2;
	
	private static final int TYPE_STRING = 1;
	private static final int TYPE_XML = 2;
    
	
	private static final ExtensionResourceFilter PDF_FILTER = new ExtensionResourceFilter("pdf");
	private static final int UNDEFINED = Integer.MIN_VALUE;
	
	
	
	private int action=ACTION_PROCESSDDX;
	private boolean ascending =false;
	private Object copyFrom=null;
	private String ddxFile=null;
	private Resource destination=null;
	private Resource directory=null;
	private int encrypt=PDFUtil.ENCRYPT_RC4_128;
	private boolean flatten=false;
	private boolean foreground=false;
	private String format=FORMAT_JPG;
	private Object image=null;
	private Struct info=null;
	private Struct inputFiles=null;
	private Struct outputFiles=null;
	private boolean isBase64=false;
	private boolean keepBookmark=false;
	private String name=null;
	private String newOwnerPassword=null;
	private String newUserPassword=null;
	private float opacity=0.3F;
	private int order=ORDER_TIME;
	private boolean overwrite=false;
	private String pages=null;
	private String password=null;
	private int permissions=0;
	private String position=null;
	private int resolution=RESOLUTION_HIGH;
	private float rotation=0;
	private int saveOption=SAVE_OPTION_FULL;
	private int scale=25;
	private boolean showOnPrint=false;
	private Object source =null;
	private boolean stopOnError=false;
	private boolean transparent=false;
	private char version=0;
	private java.util.List<PDFParamBean> params;
	private ResourceFilter filter=null;
	private String imagePrefix=null;
	private int type=TYPE_XML;
	
	@Override
	public void release() {
		super.release();
		action=ACTION_PROCESSDDX;
		ascending =false;
		copyFrom=null;
		ddxFile=null;
		destination=null;
		directory=null;
		encrypt=PDFUtil.ENCRYPT_RC4_128;
		flatten=false;
		foreground=false;
		format=FORMAT_JPG;
		image=null;
		info=null;
		inputFiles=null;
		outputFiles=null;
		isBase64=false;
		keepBookmark=false;
		name=null;
		newOwnerPassword=null;
		newUserPassword=null;
		opacity=0.3F;
		order=ORDER_TIME;
		overwrite=false;
		pages=null;
		password=null;
		permissions=0;
		position=null;
		resolution=RESOLUTION_HIGH;
		rotation=0;
		saveOption=SAVE_OPTION_FULL;
		scale=25;
		showOnPrint=false;
		source =null;
		stopOnError=false;
		transparent=false;
		version=0;
		params=null;
		filter=null;
		imagePrefix=null;
		type=TYPE_XML;
	}
	
	
	
	/**
	 * @param imagePrefix the imagePrefix to set
	 */
	public void setImageprefix(String imagePrefix) {
		this.imagePrefix = imagePrefix;
	}



	/**
	 * @param action the action to set
	 * @throws ApplicationException 
	 */
	public void setAction(String strAction) throws ApplicationException {
		
		strAction=StringUtil.toLowerCase(strAction.trim());
		if("addwatermark".equals(strAction))				action=ACTION_ADD_WATERMARK;
		else if("add-watermark".equals(strAction))			action=ACTION_ADD_WATERMARK;
		else if("add_watermark".equals(strAction))			action=ACTION_ADD_WATERMARK;
		else if("deletepages".equals(strAction))			action=ACTION_DELETE_PAGES;
		else if("delete-pages".equals(strAction))			action=ACTION_DELETE_PAGES;
		else if("delete_pages".equals(strAction))			action=ACTION_DELETE_PAGES;
		else if("deletepage".equals(strAction))				action=ACTION_DELETE_PAGES;
		else if("delete-page".equals(strAction))			action=ACTION_DELETE_PAGES;
		else if("delete_page".equals(strAction))			action=ACTION_DELETE_PAGES;
		else if("getinfo".equals(strAction))				action=ACTION_GET_INFO;
		else if("get-info".equals(strAction))				action=ACTION_GET_INFO;
		else if("get_info".equals(strAction))				action=ACTION_GET_INFO;
		else if("merge".equals(strAction))					action=ACTION_MERGE;
		//else if("processddx".equals(strAction))				action=ACTION_PROCESSDDX;
		//else if("process-ddx".equals(strAction))			action=ACTION_PROCESSDDX;
		//else if("process_ddx".equals(strAction))			action=ACTION_PROCESSDDX;
		else if("protect".equals(strAction))				action=ACTION_PROTECT;
		else if("read".equals(strAction))					action=ACTION_READ;
		else if("removewatermark".equals(strAction))		action=ACTION_REMOVE_WATERMARK;
		else if("removewater-mark".equals(strAction))		action=ACTION_REMOVE_WATERMARK;
		else if("removewater_mark".equals(strAction))		action=ACTION_REMOVE_WATERMARK;
		else if("setinfo".equals(strAction))				action=ACTION_SET_INFO;
		else if("set-info".equals(strAction))				action=ACTION_SET_INFO;
		else if("set_info".equals(strAction))				action=ACTION_SET_INFO;
		else if("thumbnail".equals(strAction))				action=ACTION_THUMBNAIL;
		else if("write".equals(strAction))					action=ACTION_WRITE;
		else if("extracttext".equals(strAction))					action=ACTION_EXTRACT_TEXT;
		else if("extract-text".equals(strAction))					action=ACTION_EXTRACT_TEXT;
		else if("extract_text".equals(strAction))					action=ACTION_EXTRACT_TEXT;
		
		else throw new ApplicationException("invalid action definition ["+strAction+"], valid actions definitions are " +
				"[addWatermark,deletePages,getInfo,merge,protect,read,removeWatermark,setInfo,thumbnail,write]");
		
	}
	

	public void setType(String strType) throws ApplicationException {
		
		strType=StringUtil.toLowerCase(strType.trim());
		if("string".equals(strType))				type=TYPE_STRING;
		else if("text".equals(strType))				type=TYPE_STRING;
		else if("plain".equals(strType))				type=TYPE_STRING;
		else if("xml".equals(strType))				type=TYPE_XML;
		
		else throw new ApplicationException("invalid type definition ["+strType+"], valid type definitions are " +
				"[string,xml]");
		
	}
	
	/** 
	*  sets a filter pattern
	* @param pattern
	 * @throws PageException 
	**/
	public void setFilter(String pattern) throws PageException	{
	    if(pattern.trim().length()>0) {
            try {
            	this.filter=new WildCardFilter(pattern);
            } 
            catch (MalformedPatternException e) {
                throw Caster.toPageException(e);
            }
        }
	}
	
	/**
	 * @param ascending the ascending to set
	 */
	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}
	/**
	 * @param copyFrom the copyFrom to set
	 * @throws ExpressionException 
	 */
	public void setCopyfrom(Object copyFrom) throws ExpressionException {
		this.copyFrom = copyFrom;//ResourceUtil.toResourceExisting(pageContext, copyFrom);
	}
	/**
	 * @param ddxFile the ddxFile to set
	 */
	public void setDdxfile(String ddxFile) {
		this.ddxFile = ddxFile;// MUST
	}
	/**
	 * @param destination the destination to set
	 */
	public void setDestination(String destination) {
		this.destination = ResourceUtil.toResourceNotExisting(pageContext, destination);
	}
	/**
	 * @param directory the directory to set
	 * @throws ExpressionException 
	 */
	public void setDirectory(String directory) throws ExpressionException {
		this.directory = ResourceUtil.toResourceExisting(pageContext, directory);
	}
	/**
	 * @param encrypt the encrypt to set
	 * @throws ApplicationException 
	 */
	public void setEncrypt(String strEncrypt) throws ApplicationException {

		strEncrypt=StringUtil.toLowerCase(strEncrypt.trim());
		if("aes128".equals(strEncrypt))				encrypt=PDFUtil.ENCRYPT_AES_128;	
		else if("aes-128".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_AES_128;	
		else if("aes_128".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_AES_128;	
		else if("none".equals(strEncrypt))			encrypt=PDFUtil.ENCRYPT_NONE;		
		else if("".equals(strEncrypt))				encrypt=PDFUtil.ENCRYPT_NONE;		
		else if("rc4128".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128;		
		else if("rc4-128".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128;		
		else if("rc4_128".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128;		
		else if("rc4128m".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128M;		
		else if("rc4-128m".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128M;		
		else if("rc4_128m".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_128M;		
		else if("rc440".equals(strEncrypt))			encrypt=PDFUtil.ENCRYPT_RC4_40;		
		else if("rc4-40".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_40;		
		else if("rc4_40".equals(strEncrypt))		encrypt=PDFUtil.ENCRYPT_RC4_40;		
		
		
		else throw new ApplicationException("invalid encrypt definition ["+strEncrypt+"], valid encrypt definitions are " +
				"[aes_128,none,rc4_128,rc4_128m,rc4_40]");
	}
	/**
	 * @param flatten the flatten to set
	 */
	public void setFlatten(boolean flatten) {
		this.flatten = flatten;
	}
	/**
	 * @param foreground the foreground to set
	 */
	public void setForeground(boolean foreground) {
		this.foreground = foreground;
	}
	/**
	 * @param format the format to set
	 * @throws ApplicationException 
	 */
	public void setFormat(String strFormat) throws ApplicationException {
		strFormat=StringUtil.toLowerCase(strFormat.trim());
		if("jpg".equals(strFormat))					format=FORMAT_JPG;	
		else if("jpeg".equals(strFormat))			format=FORMAT_JPG;	
		else if("jpe".equals(strFormat))			format=FORMAT_JPG;	
		else if("tiff".equals(strFormat))			format=FORMAT_TIFF;
		else if("tif".equals(strFormat))			format=FORMAT_TIFF;	
		else if("png".equals(strFormat))			format=FORMAT_PNG;	
		
		else throw new ApplicationException("invalid format definition ["+strFormat+"], valid format definitions are " +
				"[jpg,tiff,png]");
	}
	/**
	 * @param image the image to set
	 */
	public void setImage(Object image) {
		this.image = image;// MUST
	}
	/**
	 * @param prefix the prefix to set
	 */
	public void setPrefix(String prefix) {
		this.imagePrefix = prefix;
	}
	/**
	 * @param info the info to set
	 */
	public void setInfo(Struct info) {
		this.info = info;
	}
	/**
	 * @param inputFiles the inputFiles to set
	 */
	public void setInputfiles(Struct inputFiles) {
		this.inputFiles = inputFiles;
	}
	/**
	 * @param outputFiles the outputFiles to set
	 */
	public void setOutputfiles(Struct outputFiles) {
		this.outputFiles = outputFiles;
	}
	/**
	 * @param isBase64 the isBase64 to set
	 */
	public void setIsbase64(boolean isBase64) {
		this.isBase64 = isBase64;
	}
	/**
	 * @param keepBookmark the keepBookmark to set
	 */
	public void setKeepbookmark(boolean keepBookmark) {
		this.keepBookmark = keepBookmark;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @param newOwnerPassword the newOwnerPassword to set
	 */
	public void setNewownerpassword(String newOwnerPassword) {
		this.newOwnerPassword = newOwnerPassword;
	}
	/**
	 * @param newUserPassword the newUserPassword to set
	 */
	public void setNewuserpassword(String newUserPassword) {
		this.newUserPassword = newUserPassword;
	}
	/**
	 * @param opacity the opacity to set
	 * @throws ApplicationException 
	 */
	public void setOpacity(double opacity) throws ApplicationException {
		if(opacity<0 || opacity>10) 
			throw new ApplicationException("invalid opacity definition ["+Caster.toString(opacity)+"], value should be in range from 0 to 10");
		this.opacity = (float) (opacity/10);
	}
	/**
	 * @param order the order to set
	 * @throws ApplicationException 
	 */
	public void setOrder(String strOrder) throws ApplicationException {
		strOrder=StringUtil.toLowerCase(strOrder.trim());
		if("name".equals(strOrder))					order=ORDER_NAME;	
		else if("time".equals(strOrder))			order=ORDER_TIME;	
		
		else throw new ApplicationException("invalid order definition ["+strOrder+"], valid order definitions are " +
				"[name,time]");
	}
	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	/**
	 * @param pages the pages to set
	 */
	public void setPages(String pages) {
		this.pages = pages;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @param permissions the permissions to set
	 * @throws PageException 
	 */
	public void setPermissions(String strPermissions) throws PageException {
		permissions=PDFUtil.toPermissions(strPermissions);
	}	
	/**
	 * @param position the position to set
	 */
	public void setPosition(String position) {
		this.position = position;//MUST
	}
	/**
	 * @param resolution the resolution to set
	 * @throws ApplicationException 
	 */
	public void setResolution(String strResolution) throws ApplicationException {
		strResolution=StringUtil.toLowerCase(strResolution.trim());
		if("low".equals(strResolution))			resolution=RESOLUTION_LOW;	
		else if("high".equals(strResolution))	resolution=RESOLUTION_HIGH;	
		
		else throw new ApplicationException("invalid resolution definition ["+strResolution+"], valid resolution definitions are " +
				"[low,high]");
	}
	
	/**
	 * @param rotation the rotation to set
	 */
	public void setRotation(double rotation) {
		rotation=rotation%360D;
		//rotation=rotation/360*6.28318525;
		
		this.rotation = (float) rotation;
	}
	
	/**
	 * @param saveOption the saveOption to set
	 * @throws ApplicationException 
	 */
	public void setSaveoption(String strSaveOption) throws ApplicationException {
		strSaveOption=StringUtil.toLowerCase(strSaveOption.trim());
		if("full".equals(strSaveOption))			saveOption=SAVE_OPTION_FULL;	
		else if("incremental".equals(strSaveOption))saveOption=SAVE_OPTION_INCREMENTAL;	
		else if("linear".equals(strSaveOption))		saveOption=SAVE_OPTION_LINEAR;	
		
		else throw new ApplicationException("invalid saveOption definition ["+strSaveOption+"], valid saveOption definitions are " +
				"[full,linear,incremental]");
	}
	
	/**
	 * @param scale the scale to set
	 * @throws ApplicationException 
	 */
	public void setScale(double scale) throws ApplicationException {
		//if(scale<1 || scale>1000)  this check is now done inside PDF2IMage implementation
		//	throw new ApplicationException("invalid scale definition ["+Caster.toString(scale)+"], value should be in range from 1 to 100");
		this.scale = (int) scale;
	}
	
	/**
	 * @param showOnPrint the showOnPrint to set
	 */
	public void setShowonprint(boolean showOnPrint) {
		this.showOnPrint = showOnPrint;
	}
	
	/**
	 * @param source the source to set
	 */
	public void setSource(Object source) {
		this.source = source;
	}
	/**
	 * @param stopOnError the stopOnError to set
	 */
	public void setStoponerror(boolean stopOnError) {
		this.stopOnError = stopOnError;
	}
	/**
	 * @param transparent the transparent to set
	 */
	public void setTransparent(boolean transparent) {
		this.transparent = transparent;
	}
	/**
	 * @param version the version to set
	 * @throws ApplicationException 
	 */
	public void setVersion(double version) throws ApplicationException {
		if(1.1 == version)			this.version='1';	
		else if(1.2 == version)		this.version=PdfWriter.VERSION_1_2;	
		else if(1.3 == version)		this.version=PdfWriter.VERSION_1_3;	
		else if(1.4 == version)		this.version=PdfWriter.VERSION_1_4;	
		else if(1.5 == version)		this.version=PdfWriter.VERSION_1_5;	
		else if(1.6 == version)		this.version=PdfWriter.VERSION_1_6;	
		
		else throw new ApplicationException("invalid version definition ["+Caster.toString(version)+"], valid version definitions are " +
				"[1.1, 1.2, 1.3, 1.4, 1.5, 1.6]");
	}
	
	@Override
	public int doStartTag() throws PageException	{
		// RR SerialNumber sn = pageContext.getConfig().getSerialNumber();
	    //if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
	    //    throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+" version of Lucee");
	    
	    return EVAL_BODY_BUFFERED;
	}

	@Override
	public void doInitBody()	{
		
	}
	
	@Override
	public int doAfterBody()	{
		return SKIP_BODY;
	}
	
	@Override
	public int doEndTag() throws PageException {
		try {

			if(ACTION_ADD_WATERMARK==action)			doActionAddWatermark();
			else if(ACTION_REMOVE_WATERMARK==action)	doActionRemoveWatermark();
			else if(ACTION_READ==action)				doActionRead();
			else if(ACTION_WRITE==action)				doActionWrite();
			else if(ACTION_GET_INFO==action)			doActionGetInfo();
			else if(ACTION_SET_INFO==action)			doActionSetInfo();
			else if(ACTION_MERGE==action)				doActionMerge();
			else if(ACTION_DELETE_PAGES==action)		doActionDeletePages();
			else if(ACTION_PROTECT==action)				doActionProtect();
			else if(ACTION_THUMBNAIL==action)			doActionThumbnail();
			else if(ACTION_EXTRACT_TEXT==action)		{
				if(true)throw new ApplicationException("not supported yet, see https://issues.jboss.org/browse/LUCEE-1559");
				doActionExtractText();
			}
			
			//else if(ACTION_PROCESSDDX==action)	throw new ApplicationException("action [processddx] not supported");
			
			
			
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}	
		return EVAL_PAGE;
	}
	
	
	
	
	private void doActionWrite() throws PageException, IOException, DocumentException {
		required("pdf", "write", "source", source);
		required("pdf", "write", "destination", destination);
		
		if(destination.exists() && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		PDFDocument doc = toPDFDocument(source, password, null);
		//PdfReader pr = doc.getPdfReader();
		// output
		boolean destIsSource = doc.getResource()!=null && destination.equals(doc.getResource());
		
		OutputStream os=null;
		if(destIsSource){
			os=new ByteArrayOutputStream();
		}
		else if(destination!=null) {
			os=destination.getOutputStream();
		}
		
		try {	
			PDFUtil.concat(new PDFDocument[]{doc}, os, true, true, true,version);
		}
		finally {
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				if(destination!=null)IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
			}
		}
		
/*	
		// flatten = "yes|no"
	    // must saveOption = "linear|incremental|full"
*/
	}



	private void doActionThumbnail() throws PageException, IOException, DocumentException {
		required("pdf", "thumbnail", "source", source);

		PDFDocument doc = toPDFDocument(source, password, null);
		PdfReader pr = doc.getPdfReader();
		boolean isEnc=pr.isEncrypted();
		pr.close();
		if(isEnc) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			//PDFUtil.concat(new PDFDocument[]{doc}, baos, true, true, true, (char)0);
			PDFUtil.encrypt(doc,baos,null,null,0,PDFUtil.ENCRYPT_NONE);
			baos.close();
			doc = new PDFDocument(baos.toByteArray(),doc.getResource(),null);
		}
		
		doc.setPages(pages);
		
		// scale
		if(scale<1)
			throw new ApplicationException("value of attribute scale ["+scale+"] should be at least 1");
		
		// destination
		if(destination==null)
			destination=ResourceUtil.toResourceNotExisting(pageContext, "thumbnails");
		
		// imagePrefix
		if(imagePrefix==null){
			Resource res = doc.getResource();
			if(res!=null){
				String n = res.getName();
				int index=n.lastIndexOf('.');
				if(index!=-1)imagePrefix=n.substring(0,index);
				else imagePrefix=n;
			}
			else imagePrefix="memory";
		}
		
		// MUST password
		PDFUtil.writeImages(doc.getRaw(), doc.getPages(), destination, imagePrefix, format, scale,overwrite,
				resolution==RESOLUTION_HIGH,transparent);
		
		
		
	}
	
	
	
	
	private void doActionAddWatermark() throws PageException, IOException, DocumentException {
		required("pdf", "addWatermark", "source", source);
		if(copyFrom==null && image==null)
			throw new ApplicationException("at least one of the following attributes must be defined " +
					"[copyFrom,image]");
		
		if(destination!=null && destination.exists() && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		
		// image
		Image img=null;
		if(image!=null) {
			lucee.runtime.img.Image ri = lucee.runtime.img.Image.createImage(pageContext,image,false,false,true,null);
			img=Image.getInstance(ri.getBufferedImage(),null,false);
		}
		// copy From
		else {
			byte[] barr;
			try{
				Resource res = Caster.toResource(pageContext, copyFrom, true);
				barr=IOUtil.toBytes(res);
			}
			catch(ExpressionException ee) {
				barr=Caster.toBinary(copyFrom);
			}
			img=Image.getInstance(PDFUtil.toImage(barr, 1).getBufferedImage(),null,false);
			
		}
		
		// position
		float x=UNDEFINED,y=UNDEFINED;
		if(!StringUtil.isEmpty(position)) {
			int index=position.indexOf(',');
			if(index==-1)
				throw new ApplicationException("attribute [position] has an invalid value ["+position+"]," +
						"value should follow one of the following pattern [40,50], [40,] or [,50]");
			String strX = position.substring(0,index).trim();
			String strY = position.substring(index+1).trim();
			if(!StringUtil.isEmpty(strX))x = Caster.toIntValue(strX);
			if(!StringUtil.isEmpty(strY))y = Caster.toIntValue(strY);
			
		}
		
		
		PDFDocument doc = toPDFDocument(source, password, null);
		doc.setPages(pages);
		PdfReader reader = doc.getPdfReader();
		reader.consolidateNamedDestinations();
		boolean destIsSource = destination!=null && doc.getResource()!=null && destination.equals(doc.getResource());
		java.util.List bookmarks = SimpleBookmark.getBookmark(reader);
	    ArrayList master = new ArrayList();	
		if(bookmarks!=null)master.addAll(bookmarks);
	    
		// output
		OutputStream os=null;
		if(!StringUtil.isEmpty(name) || destIsSource){
			os=new ByteArrayOutputStream();
		}
		else if(destination!=null) {
			os=destination.getOutputStream();
		}
		
		try {
			
			int len = reader.getNumberOfPages();
		    PdfStamper stamp = new PdfStamper(reader, os);
		    
		    if(len >0){
		    	if(x==UNDEFINED || y==UNDEFINED) {
			    	PdfImportedPage first = stamp.getImportedPage(reader, 1);
			    	if(y==UNDEFINED)y=(first.getHeight()-img.getHeight())/2;
			    	if(x==UNDEFINED)x=(first.getWidth()-img.getWidth())/2;	    	
		    	}
		    	img.setAbsolutePosition(x, y);
		    	//img.setAlignment(Image.ALIGN_JUSTIFIED); ration geht nicht anhand mitte
		    	
		    }
		    
			// rotation
			if(rotation!=0) {
				img.setRotationDegrees(rotation);
			}
			
			Set _pages = doc.getPages();
		    for (int i=1;i<=len;i++) {
		    	if(_pages!=null && !_pages.contains(Integer.valueOf(i))) continue;
		    	PdfContentByte cb =foreground? stamp.getOverContent(i):stamp.getUnderContent(i);
		    	PdfGState gs1 = new PdfGState();
		    	//print.out("op:"+opacity);
		    	gs1.setFillOpacity(opacity);
		    	//gs1.setStrokeOpacity(opacity);
		        cb.setGState(gs1);
		    	cb.addImage(img);
		    }
		    if(bookmarks!=null)stamp.setOutlines(master);
		    stamp.close();
		}
		finally {
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				if(destination!=null)IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
				if(!StringUtil.isEmpty(name)){
					pageContext.setVariable(name,new PDFDocument(((ByteArrayOutputStream)os).toByteArray(),password));
				}
			}
		}
	}
	
	private void doActionRemoveWatermark() throws PageException, IOException, DocumentException {
		required("pdf", "removeWatermark", "source", source);
		
		if(destination!=null && destination.exists() && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		lucee.runtime.img.Image ri = new lucee.runtime.img.Image(1,1,BufferedImage.TYPE_INT_RGB,Color.BLACK);
		Image img = Image.getInstance(ri.getBufferedImage(),null,false);
		img.setAbsolutePosition(1,1);
		
		
		PDFDocument doc = toPDFDocument(source, password, null);
		doc.setPages(pages);
		PdfReader reader = doc.getPdfReader();

		boolean destIsSource = destination!=null && doc.getResource()!=null && destination.equals(doc.getResource());
		java.util.List bookmarks = SimpleBookmark.getBookmark(reader);
	    ArrayList master = new ArrayList();	
		if(bookmarks!=null)master.addAll(bookmarks);
	    
		// output
		OutputStream os=null;
		if(!StringUtil.isEmpty(name) || destIsSource){
			os=new ByteArrayOutputStream();
		}
		else if(destination!=null) {
			os=destination.getOutputStream();
		}
		
		try {
			int len = reader.getNumberOfPages();
		    PdfStamper stamp = new PdfStamper(reader, os);
			
			Set _pages = doc.getPages();
		    for (int i=1;i<=len;i++) {
		    	if(_pages!=null && !_pages.contains(Integer.valueOf(i))) continue;
		    	PdfContentByte cb =foreground? stamp.getOverContent(i):stamp.getUnderContent(i);
		    	PdfGState gs1 = new PdfGState();
		        gs1.setFillOpacity(0);
		        cb.setGState(gs1);
		    	cb.addImage(img);
		    }
		    if(bookmarks!=null)stamp.setOutlines(master);
		    stamp.close();
		}
		finally {
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				if(destination!=null)IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
				if(!StringUtil.isEmpty(name)){
					pageContext.setVariable(name,new PDFDocument(((ByteArrayOutputStream)os).toByteArray(),password));
				}
			}
		}
	}
	
	private void doActionDeletePages() throws PageException, IOException, DocumentException {
		required("pdf", "deletePage", "pages", pages,true);
		required("pdf", "deletePage", "source", source);
		
		PDFDocument doc = toPDFDocument(source, password, null);
		doc.setPages(pages);
		
		
		if(destination==null && StringUtil.isEmpty(name)){
			if(doc.getResource()==null)
				throw new ApplicationException("source is not based on a resource, destination attribute is required");
			destination=doc.getResource();
		}
		else if(destination!=null && destination.exists() && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		boolean destIsSource = destination!=null && doc.getResource()!=null && destination.equals(doc.getResource());
		
		// output
		OutputStream os=null;
		if(!StringUtil.isEmpty(name) || destIsSource){
			os=new ByteArrayOutputStream();
		}
		else if(destination!=null) {
			os=destination.getOutputStream();
		}
		
		try {
			PDFUtil.concat(new PDFDocument[]{doc}, os, true, true,true,version);
		}
		finally {
			//if(document!=null)document.close();
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				if(destination!=null)IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
				if(!StringUtil.isEmpty(name)){
					pageContext.setVariable(name,new PDFDocument(((ByteArrayOutputStream)os).toByteArray(),password));
				}
			}
		}
	}
	
	
	private void doActionMerge() throws ApplicationException, PageException, IOException, DocumentException {
		
		if(source==null && params==null && directory==null)
			throw new ApplicationException("at least one of the following constellation must be defined" +
					" attribute source, attribute directory or cfpdfparam child tags");
		if(destination==null && StringUtil.isEmpty(name,true))
			throw new ApplicationException("at least one of the following attributes must be defined " +
					"[destination,name]");
		if(destination!=null && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		ArrayList docs = new ArrayList();
		PDFDocument doc;
		boolean isListing=false;
		
		// source
		if(source!=null) {
			if(Decision.isArray(source)) {
				Array arr = Caster.toArray(source);
				int len = arr.size();
				for(int i=1;i<=len;i++) {
					docs.add(doc=toPDFDocument(arr.getE(i),password,null));
					doc.setPages(pages);
				}
			}
			else if(source instanceof String) {
				String[] sources = ListUtil.toStringArrayTrim(ListUtil.listToArrayRemoveEmpty((String)source, ','));
				for(int i=0;i<sources.length;i++) {
					docs.add(doc=toPDFDocument(sources[i],password,null));
					doc.setPages(pages);
				}
			}
			else docs.add(toPDFDocument(source,password,null));
			
		}
		boolean destIsSource = false;
		
		// params
		if(directory!=null && !directory.isDirectory()) {
			if(!directory.exists())
				throw new ApplicationException("defined attribute directory does not exist");
			throw new ApplicationException("defined attribute directory is not a directory");
		}
		if(params!=null) {
			Iterator it = params.iterator();
			PDFParamBean param;
			while(it.hasNext()) {
				param=(PDFParamBean) it.next();
				docs.add(doc=toPDFDocument(param.getSource(), param.getPassword(),directory));
				doc.setPages(param.getPages());
			}
		}
		else if(directory!=null) {
			isListing=true;
			Resource[] children = ResourceUtil.listResources(directory, filter);
			
			if(ascending) {
				for(int i=children.length-1;i>=0;i--) {
					if(destination!=null && children[i].equals(destination))	destIsSource=true;
					docs.add(doc=toPDFDocument(children[i],password,null));
					doc.setPages(pages);
				}
			}
			else {
				for(int i=0;i<children.length;i++) {
					if(destination!=null && children[i].equals(destination))	destIsSource=true;
					docs.add(doc=toPDFDocument(children[i],password,null));
					doc.setPages(pages);
				}
			}
			
		}
		
		int doclen=docs.size();
		if(doclen==0)
			throw new ApplicationException("you have to define at leat 1 pdf file");
		
		// output
		OutputStream os=null;
		if(!StringUtil.isEmpty(name) || destIsSource){
			os=new ByteArrayOutputStream();
		}
		else if(destination!=null) {
			os=destination.getOutputStream();
		}
		
		
		/*com.lowagie.text.Document document=null;
		PdfCopy copy=null;
		PdfReader pr;
		Set pages;
		int size;*/
		
		
		try {
			if(!isListing)stopOnError=true;
			PDFUtil.concat((PDFDocument[]) docs.toArray(new PDFDocument[docs.size()]), os, keepBookmark, false,stopOnError,version);
			/*
			boolean init=false;
			for(int d=0;d<doclen;d++) {
				doc=(PDFDocument) docs.get(d);
				pages=doc.getPages();
				try {
					pr=doc.getPdfReader();
					print.out(pr.getCatalog().getKeys());
					
				}
				catch(Throwable t) {
					if(isListing && !stopOnError)continue;
					throw Caster.toPageException(t);
				}
				print.out("d+"+d);
				if(!init) {
					init=true;
					print.out("set");
					document = new com.lowagie.text.Document(pr.getPageSizeWithRotation(1));
					copy = new PdfCopy(document,os);
					document.open();
				}
				size=pr.getNumberOfPages();
				print.out("pages:"+size);
				for(int page=1;page<=size;page++) {
					if(pages==null || pages.contains(Constants.Integer(page))) {
						copy.addPage(copy.getImportedPage(pr, page));
					}
				}
			}*/
		}
		finally {
			//if(document!=null)document.close();
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				if(destination!=null)IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
				if(!StringUtil.isEmpty(name))pageContext.setVariable(name, ((ByteArrayOutputStream)os).toByteArray());
			}
			
		}
		
		
	}
	private void doActionRead() throws PageException {
		required("pdf", "read", "name", name,true);
		required("pdf", "read", "source", source);
		
		pageContext.setVariable(name, toPDFDocument(source,password,null));
	}
	
	private void doActionProtect() throws PageException, IOException, DocumentException {
		required("pdf", "protect", "source", source);
		
		if(StringUtil.isEmpty(newUserPassword) && StringUtil.isEmpty(newOwnerPassword))
			throw new ApplicationException("at least one of the following attributes must be defined [newUserPassword,newOwnerPassword]");
        
		
		PDFDocument doc = toPDFDocument(source,password,null);
		
		if(destination==null){
			destination=doc.getResource();
			if(destination==null)
				throw new ApplicationException("source is not based on a resource, destination file is required");
		}
		else if(destination.exists() && !overwrite)
			throw new ApplicationException("destination file ["+destination+"] already exists");
		
		boolean destIsSource = doc.getResource()!=null && destination.equals(doc.getResource());
		
		// output
		OutputStream os=null;
		if(destIsSource){
			os=new ByteArrayOutputStream();
		}
		else {
			os=destination.getOutputStream();
		}
		
		
		try{
			PDFUtil.encrypt(doc,os,newUserPassword,newOwnerPassword,permissions,encrypt);
		}
		finally {
			IOUtil.closeEL(os);
			if(os instanceof ByteArrayOutputStream) {
				IOUtil.copy(new ByteArrayInputStream(((ByteArrayOutputStream)os).toByteArray()), destination,true);// MUST overwrite
			}
			
		}
		
	}
	

	

	private void doActionSetInfo() throws PageException, IOException, DocumentException {
		required("pdf", "setInfo", "info", info);
		required("pdf", "getInfo", "source", source);
		
		PDFDocument doc = toPDFDocument(source,password,null);
		PdfReader pr = doc.getPdfReader();
		OutputStream os=null;
		try {
			if(destination==null){
				if(doc.getResource()==null)
					throw new ApplicationException("source is not based on a resource, destination file is required");
				destination=doc.getResource();
			}
			else if(destination.exists() && !overwrite)
				throw new ApplicationException("destination file ["+destination+"] already exists");
			
			PdfStamper stamp = new PdfStamper(pr, os=destination.getOutputStream());
			HashMap moreInfo = new HashMap();
            
			//Key[] keys = info.keys();
			Iterator<Entry<Key, Object>> it = info.entryIterator();
			Entry<Key, Object> e;
			while(it.hasNext()) {
				e = it.next();
				moreInfo.put(StringUtil.ucFirst(e.getKey().getLowerString()), Caster.toString(e.getValue()));
			}
			// author
			Object value = info.get(KeyConstants._author, null);	
			if(value!=null)moreInfo.put("Author", Caster.toString(value));
			// keywords
			value = info.get("keywords", null);	
			if(value!=null)moreInfo.put("Keywords", Caster.toString(value));
			// title
			value = info.get(KeyConstants._title, null);	
			if(value!=null)moreInfo.put("Title", Caster.toString(value));
			// subject
			value = info.get(KeyConstants._subject, null);	
			if(value!=null)moreInfo.put("Subject", Caster.toString(value));
			// creator
			value = info.get("creator", null);	
			if(value!=null)moreInfo.put("Creator", Caster.toString(value));
			// trapped
			value = info.get("Trapped", null);	
			if(value!=null)moreInfo.put("Trapped", Caster.toString(value));
			// Created
			value = info.get("Created", null);	
			if(value!=null)moreInfo.put("Created", Caster.toString(value));
			// Language
			value = info.get("Language", null);	
			if(value!=null)moreInfo.put("Language", Caster.toString(value));
			
			
			stamp.setMoreInfo(moreInfo);
			stamp.close();
			
		}
		finally {
			IOUtil.closeEL(os);
			pr.close();
		}
	}
	
	private void doActionGetInfo() throws PageException {
		required("pdf", "getInfo", "name", name,true);
		required("pdf", "getInfo", "source", source);
		
		PDFDocument doc = toPDFDocument(source,password,null);
		pageContext.setVariable(name, doc.getInfo());
		
	}
	private void doActionExtractText() throws PageException, IOException, CryptographyException, InvalidPasswordException {
		required("pdf", "extractText", "name", name,true);
		
		PDFDocument doc = toPDFDocument(source,password,null);
		doc.setPages(pages);
		
		pageContext.setVariable(name, PDFUtil.extractText(doc,doc.getPages()));
		/*
		 <cfpdf 
 required 
    action="extracttext" <!---extract all the words in the PDF.---> 
    ***source= "absolute or relative path of the PDF file|PDF document variable| 
            cfdocument variable" 
    pages = "*" <!----page numbers from where the text needs to be extracted from the 
                PDF document---> 

optional 
    addquads = "add the position or quadrants for the text in the PDF" 
    honourspaces = "true|false" 
    overwrite = "true" <!---Overwrite the specified object in the PDF document---> 
    ***password = "" <!--- PDF document password---> 
    type = "string|xml" <!---format in which the text needs to be extracted---> 
    one of the following: 
    destination = "PDF output file pathname" 
    name = "PDF document variable" 
    usestructure = "true|false" 
		 * */
	}
	
	private Object allowed(boolean encrypted, int permissions, int permission) {
		return (!encrypted || (permissions&permission)>0)?"Allowed":"Not Allowed";
	}
	

	private PDFDocument toPDFDocument(Object source,String password, Resource directory) throws PageException {
		
		if(source instanceof PDFDocument) 
			return (PDFDocument)source;
		if(Decision.isBinary(source)){
			return new PDFDocument(Caster.toBinary(source),password);
		}
		if(source instanceof Resource){ 
			return new PDFDocument((Resource) source,password);
		}
		if(source instanceof String){
			if(directory!=null) {
				Resource res = directory.getRealResource((String)source);
				if(!res.isFile()){
					Resource res2 = ResourceUtil.toResourceNotExisting(pageContext, (String)source);
					if(res2.isFile())
						res=res2;
					else 
						throw new ExpressionException("file or directory "+res+" not exist");
				}
				return new PDFDocument(res,password);	
			}
			return new PDFDocument(ResourceUtil.toResourceExisting(pageContext, (String)source),password);	
		}
		
		throw new CasterException(source,PdfReader.class);
	}
	
	/*private byte[] toBinary(Object source) throws ExpressionException, IOException {
		
		if(source instanceof PDFDocument) 
			return toBinary(((PDFDocument)source).getResource());
		if(Decision.isBinary(source)){
			return Caster.toBinary(source);
		}
		if(source instanceof Resource){ 
			return IOUtil.toBytes((Resource)source);
		}
		if(source instanceof String){
			if(directory!=null) {
				Resource res = directory.getRealResource((String)source);
				if(!res.isFile()){
					Resource res2 = ResourceUtil.toResourceNotExisting(pageContext, (String)source);
					if(res2.isFile())
						res=res2;
					else 
						throw new ExpressionException("file or directory "+res+" not exist");
				}
				return IOUtil.toBytes(res);	
			}
			return IOUtil.toBytes(ResourceUtil.toResourceExisting(pageContext, (String)source));	
		}
		
		throw new CasterException(source,PdfReader.class);
	}*/
	
	protected void setParam(PDFParamBean param) {
		if(params==null)
			params=new ArrayList<PDFParamBean>();
		params.add(param);
	}
	
	
	

}