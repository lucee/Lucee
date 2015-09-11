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
package lucee.runtime.text.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageException;
import lucee.runtime.img.Image;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Constants;
import lucee.runtime.op.Decision;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFText2HTML;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.SimpleBookmark;

public class PDFUtil {


	public static final int ENCRYPT_RC4_40 = PdfWriter.STANDARD_ENCRYPTION_40;
	public static final int ENCRYPT_RC4_128 = PdfWriter.STANDARD_ENCRYPTION_128;
	public static final int ENCRYPT_RC4_128M = PdfWriter.STANDARD_ENCRYPTION_128;
	public static final int ENCRYPT_AES_128 = PdfWriter.ENCRYPTION_AES_128;
	public static final int ENCRYPT_NONE = -1;
	
	private static final int PERMISSION_ALL = 
		PdfWriter.ALLOW_ASSEMBLY+
		PdfWriter.ALLOW_COPY+
		PdfWriter.ALLOW_DEGRADED_PRINTING+
		PdfWriter.ALLOW_FILL_IN+
		PdfWriter.ALLOW_MODIFY_ANNOTATIONS+
		PdfWriter.ALLOW_MODIFY_CONTENTS+
		PdfWriter.ALLOW_PRINTING+
		PdfWriter.ALLOW_SCREENREADERS+PdfWriter.ALLOW_COPY;// muss 2 mal sein, keine ahnung wieso
	
	/**
	 * convert a string list of permission 
	 * @param strPermissions
	 * @return
	 * @throws PageException
	 */
	public static int toPermissions(String strPermissions) throws PageException {
		if(strPermissions==null) return 0;
		int permissions=0;
    	strPermissions=strPermissions.trim();
		
    	String[] arr = lucee.runtime.type.util.ListUtil.toStringArray(lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(strPermissions, ','));
		for(int i=0;i<arr.length;i++) {
			permissions=add(permissions,toPermission(arr[i]));
		}
		return permissions;
	}

	/**
	 * convert a string defintion of a permision in a integer Constant (PdfWriter.ALLOW_XXX)
	 * @param strPermission
	 * @return
	 * @throws ApplicationException
	 */
	public static int toPermission(String strPermission) throws ApplicationException {
		strPermission=strPermission.trim().toLowerCase();
		if("allowassembly".equals(strPermission))				return PdfWriter.ALLOW_ASSEMBLY;
		else if("none".equals(strPermission))					return 0;
		else if("all".equals(strPermission))					return PERMISSION_ALL;
		else if("assembly".equals(strPermission))				return PdfWriter.ALLOW_ASSEMBLY;
		else if("documentassembly".equals(strPermission))		return PdfWriter.ALLOW_ASSEMBLY;
		else if("allowdegradedprinting".equals(strPermission))	return PdfWriter.ALLOW_DEGRADED_PRINTING;
		else if("degradedprinting".equals(strPermission))		return PdfWriter.ALLOW_DEGRADED_PRINTING;
		else if("printing".equals(strPermission))				return PdfWriter.ALLOW_DEGRADED_PRINTING;
		else if("allowfillin".equals(strPermission))			return PdfWriter.ALLOW_FILL_IN;
		else if("fillin".equals(strPermission))					return PdfWriter.ALLOW_FILL_IN;
		else if("fillingform".equals(strPermission))			return PdfWriter.ALLOW_FILL_IN;
		else if("allowmodifyannotations".equals(strPermission)) return PdfWriter.ALLOW_MODIFY_ANNOTATIONS;  
		else if("modifyannotations".equals(strPermission)) 		return PdfWriter.ALLOW_MODIFY_ANNOTATIONS;  
		else if("allowmodifycontents".equals(strPermission))	return PdfWriter.ALLOW_MODIFY_CONTENTS; 
		else if("modifycontents".equals(strPermission))			return PdfWriter.ALLOW_MODIFY_CONTENTS; 
		else if("allowcopy".equals(strPermission))				return PdfWriter.ALLOW_COPY;
		else if("copy".equals(strPermission))					return PdfWriter.ALLOW_COPY;
		else if("copycontent".equals(strPermission))			return PdfWriter.ALLOW_COPY;
		else if("allowprinting".equals(strPermission))			return PdfWriter.ALLOW_PRINTING;
		else if("printing".equals(strPermission))				return PdfWriter.ALLOW_PRINTING;
		else if("allowscreenreaders".equals(strPermission))		return PdfWriter.ALLOW_SCREENREADERS;
		else if("screenreaders".equals(strPermission))			return PdfWriter.ALLOW_SCREENREADERS;
		
		else throw new ApplicationException("invalid permission ["+strPermission+"], valid permission values are [AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations, AllowFillIn, AllowScreenReaders, AllowAssembly, AllowDegradedPrinting]");
	}

	
	private static int add(int permissions, int permission) {
		if(permission==0 || (permissions&permission)>0)return permissions;
		return permissions+permission;
	}
	
	
	/**
	 * @param docs
	 * @param os
	 * @param removePages if true, pages defined in PDFDocument will be removed, otherwise all other pages will be removed
	 * @param version 
	 * @throws PageException 
	 * @throws IOException 
	 * @throws DocumentException 
	 */
	public static void concat(PDFDocument[] docs,OutputStream os, boolean keepBookmark,boolean removePages, boolean stopOnError, char version) throws PageException, IOException, DocumentException {
		Document document = null;
		PdfCopy  writer = null;
		PdfReader reader;
		Set pages;
		boolean isInit=false;
		PdfImportedPage page;
		try {
			int pageOffset = 0;
			ArrayList master = new ArrayList();
			
			for(int i=0;i<docs.length;i++) {
				// we create a reader for a certain document
				pages = docs[i].getPages();
				try {
					reader = docs[i].getPdfReader();
				}
				catch(Throwable t) {
					if(!stopOnError)continue;
					throw Caster.toPageException(t);
				}
				reader.consolidateNamedDestinations();
				
				// we retrieve the total number of pages
				int n = reader.getNumberOfPages();
				List bookmarks = keepBookmark?SimpleBookmark.getBookmark(reader):null;
				if (bookmarks != null) {
					removeBookmarks(bookmarks,pages,removePages);
					if (pageOffset != 0)	SimpleBookmark.shiftPageNumbers(bookmarks, pageOffset, null);	
					master.addAll(bookmarks);
				}
				
				if (!isInit) {
					isInit=true;
					document = new Document(reader.getPageSizeWithRotation(1));
					writer = new PdfCopy(document, os);
					
					if(version!=0)writer.setPdfVersion(version);
					
					
					document.open();
				}
				
				
				for (int y = 1; y <= n; y++) {
					if(pages!=null && removePages==pages.contains(Integer.valueOf(y))){
						continue;
					}
					pageOffset++;
					page = writer.getImportedPage(reader, y);
					writer.addPage(page);
				}
				PRAcroForm form = reader.getAcroForm();
				if (form != null)
					writer.copyAcroForm(reader);
			}
			if (master.size() > 0)
				writer.setOutlines(master);
			
		}
		finally {
			IOUtil.closeEL(document);
		}
	}
	
	
	private static void removeBookmarks(List bookmarks,Set pages, boolean removePages) {
		int size = bookmarks.size();
		for(int i=size-1;i>=0;i--) {
			if(removeBookmarks((Map) bookmarks.get(i),pages, removePages))
				bookmarks.remove(i);
		}
	}
	
	private static boolean removeBookmarks(Map bookmark, Set pages, boolean removePages) {
		List kids=(List) bookmark.get("Kids");
		if(kids!=null)removeBookmarks(kids,pages,removePages);
		Integer page=Caster.toInteger(lucee.runtime.type.util.ListUtil.first((String) bookmark.get("Page")," ",true),Constants.INTEGER_MINUS_ONE);
		return removePages==(pages!=null && pages.contains(page));
	}

	public static Set parsePageDefinition(String strPages) throws PageException {
		if(StringUtil.isEmpty(strPages)) return null;
		HashSet<Integer> set=new HashSet<Integer>();
		parsePageDefinition(set, strPages);
		return set;
	}
	public static void parsePageDefinition(Set<Integer> pages, String strPages) throws PageException {
		if(StringUtil.isEmpty(strPages)) return;
		String[] arr = lucee.runtime.type.util.ListUtil.toStringArrayTrim(lucee.runtime.type.util.ListUtil.listToArrayRemoveEmpty(strPages, ','));
		int index,from,to;
		for(int i=0;i<arr.length;i++){
			index=arr[i].indexOf('-');
			if(index==-1)pages.add(Caster.toInteger(arr[i].trim()));
			else {
				from=Caster.toIntValue(arr[i].substring(0,index).trim());
				to=Caster.toIntValue(arr[i].substring(index+1).trim());
				for(int y=from;y<=to;y++){
					pages.add(Integer.valueOf(y));
				}
			}
		}
	}
	
	
	

	 
	public static void encrypt(PDFDocument doc, OutputStream os, String newUserPassword, String newOwnerPassword, int permissions, int encryption) throws ApplicationException, DocumentException, IOException {
		byte[] user = newUserPassword==null?null:newUserPassword.getBytes();
		byte[] owner = newOwnerPassword==null?null:newOwnerPassword.getBytes();
		
		PdfReader pr = doc.getPdfReader();
		List bookmarks = SimpleBookmark.getBookmark(pr);
		int n = pr.getNumberOfPages();
		
		Document document = new Document(pr.getPageSizeWithRotation(1));
		PdfCopy writer = new PdfCopy(document, os);
		if(encryption!=ENCRYPT_NONE)writer.setEncryption(user, owner, permissions, encryption);
		document.open();
		
		
		PdfImportedPage page;
		for (int i = 1; i <= n; i++) {
			page = writer.getImportedPage(pr, i);
			writer.addPage(page);
		}
		PRAcroForm form = pr.getAcroForm();
		if (form != null)writer.copyAcroForm(pr);
		if (bookmarks!=null)writer.setOutlines(bookmarks);
		document.close();
	}

	public static HashMap generateGoToBookMark(String title,int page) {
		return generateGoToBookMark(title,page, 0, 731);
	}
	
	public static HashMap generateGoToBookMark(String title,int page, int x, int y) {
		HashMap map=new HashMap();
		map.put("Title", title);
		map.put("Action", "GoTo");
		map.put("Page", page+" XYZ "+x+" "+y+" null");
		
		return map;
	}

	public static void setChildBookmarks(Map parent, List children) {
		Object kids = parent.get("Kids");
		if(kids instanceof List){
			((List)kids).addAll(children);
		}
		else parent.put("Kids", children);
	}

	public static PdfReader toPdfReader(PageContext pc,Object value, String password) throws IOException, PageException {
		if(value instanceof PdfReader) return (PdfReader) value;
		if(value instanceof PDFDocument) return ((PDFDocument) value).getPdfReader();
		if(Decision.isBinary(value)){
			if(password!=null)return new PdfReader(Caster.toBinary(value),password.getBytes());
			return new PdfReader(Caster.toBinary(value));
		}
		if(value instanceof Resource) {
			if(password!=null)return new PdfReader(IOUtil.toBytes((Resource)value),password.getBytes());
			return new PdfReader(IOUtil.toBytes((Resource)value));
		}
		if(value instanceof String) {
			if(password!=null)return new PdfReader(IOUtil.toBytes(Caster.toResource(pc,value,true)),password.getBytes());
			return new PdfReader(IOUtil.toBytes((Resource)value));
		}
		throw new CasterException(value,PdfReader.class);
	}
	
	
	
	public static Image toImage(byte[] input,int page) throws PageException, IOException  {
		 return PDF2Image.getInstance().toImage(input, page);
	}

	public static void writeImages(byte[] input,Set pages,Resource outputDirectory, String prefix,
			String format, int scale, boolean overwrite, boolean goodQuality,boolean transparent) throws PageException, IOException {
		PDF2Image.getInstance().writeImages(input, pages, outputDirectory, prefix, format, scale, overwrite, goodQuality, transparent);
	}

	public static Object extractText(PDFDocument doc, Set<Integer> pageNumbers) throws IOException, CryptographyException, InvalidPasswordException {
		PDDocument pdDoc = doc.toPDDocument();
		//PDPageNode pages = pdDoc.getDocumentCatalog().getPages();
		//pages.
		//pdDoc.getDocumentCatalog().
		
		/*Iterator<Integer> it = pageNumbers.iterator();
		int p;
		while(it.hasNext()){
			p=it.next().intValue();
		
			pdDoc.getDocumentCatalog().getPages()
		}
		*/
		
		//print.o(pages);
		
		
		
		//pdDoc.
		
		
		//PDFTextStripperByArea  stripper = new PDFTextStripperByArea();
		//PDFHighlighter  stripper = new PDFHighlighter();
		PDFText2HTML  stripper = new PDFText2HTML("UDF-8");// TODO pass in encoding
		//PDFTextStripper stripper = new PDFTextStripper();
	    StringWriter writer = new StringWriter();
	    stripper.writeText(pdDoc, writer);
	    
		
		return writer.toString();
	}
}