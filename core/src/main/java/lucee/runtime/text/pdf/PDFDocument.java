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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.file.FileResource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.util.StructSupport;

import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.exceptions.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.PDDocument;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;

public class PDFDocument extends StructSupport implements Struct {

	private byte[] barr;
	private String password;
	private Resource resource;
	private Set<Integer> pages;

	public PDFDocument(byte[] barr, String password) {
		this.barr=barr;
		this.password=password;
	}

	public PDFDocument(Resource resource, String password) {
		this.resource=resource;
		this.password=password;
	}

	public PDFDocument(byte[] barr, Resource resource, String password) {
		this.resource=resource;
		this.barr=barr;
		this.password=password;
	}
	

	@Override
	public void clear() {
		getInfo().clear();
	}


	@Override
	public boolean containsKey(Key key) {
		return getInfo().containsKey(key);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		PDFDocument duplicate=new PDFDocument(barr,resource,password);
		return duplicate;
	}
	

	@Override
	public Object get(Key key) throws PageException {
		return getInfo().get(key);
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		return getInfo().get(key, defaultValue);
	}

	@Override
	public Key[] keys() {
		return getInfo().keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		return getInfo().remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		return getInfo().removeEL(key);
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return getInfo().set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
		return getInfo().setEL(key, value);
	}

	@Override
	public int size() {
		return getInfo().size();
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel,DumpProperties properties) {
		
		DumpData dd = getInfo().toDumpData(pageContext, maxlevel,properties);
		if(dd instanceof DumpTable)((DumpTable)dd).setTitle("Struct (PDFDocument)");
		return dd;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return getInfo().keyIterator();
	}
    
    @Override
	public Iterator<String> keysAsStringIterator() {
    	return getInfo().keysAsStringIterator();
    }
	
	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return getInfo().entryIterator();
	}
	
	@Override
	public Iterator<Object> valueIterator() {
		return getInfo().valueIterator();
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return getInfo().castToBooleanValue();
	}
    
    @Override
    public Boolean castToBoolean(Boolean defaultValue) {
        return getInfo().castToBoolean(defaultValue);
    }

	@Override
	public DateTime castToDateTime() throws PageException {
		return getInfo().castToDateTime();
	}
    
    @Override
    public DateTime castToDateTime(DateTime defaultValue) {
        return getInfo().castToDateTime(defaultValue);
    }

	@Override
	public double castToDoubleValue() throws PageException {
		return getInfo().castToDoubleValue();
	}
    
    @Override
    public double castToDoubleValue(double defaultValue) {
        return getInfo().castToDoubleValue(defaultValue);
    }

	@Override
	public String castToString() throws PageException {
		return getInfo().castToString();
	}
	@Override
	public String castToString(String defaultValue) {
		return getInfo().castToString(defaultValue);
	}

	@Override
	public int compareTo(String str) throws PageException {
		return getInfo().compareTo(str);
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		return getInfo().compareTo(b);
	}

	@Override
	public int compareTo(double d) throws PageException {
		return getInfo().compareTo(d);
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		return getInfo().compareTo(dt);
	}
///////////////////////////////////////////////
	
	public PdfReader getPdfReader() throws ApplicationException {
		try {
			if(barr!=null) {
				if(password!=null)return new PdfReader(barr,password.getBytes());
				return new PdfReader(barr);
			}
			if(password!=null)return new PdfReader(IOUtil.toBytes(resource),password.getBytes());
			return new PdfReader(IOUtil.toBytes(resource));
		}
		catch(IOException ioe) {
			throw new ApplicationException("can not load file ["+resource+"]",ioe.getMessage());
		}
	}
	
	private String getFilePath() {
		if(resource==null) return "";
		return resource.getAbsolutePath();
	}

	public Struct getInfo()  {

		PdfReader pr=null;
		try {
			pr=getPdfReader();
			//PdfDictionary catalog = pr.getCatalog();
			int permissions = pr.getPermissions();
			boolean encrypted=pr.isEncrypted();
			
			Struct info=new StructImpl();
			info.setEL("FilePath", getFilePath());
			
			// access
			info.setEL("ChangingDocument", allowed(encrypted,permissions,PdfWriter.ALLOW_MODIFY_CONTENTS));
			info.setEL("Commenting", allowed(encrypted,permissions,PdfWriter.ALLOW_MODIFY_ANNOTATIONS));
			info.setEL("ContentExtraction", allowed(encrypted,permissions,PdfWriter.ALLOW_SCREENREADERS));
			info.setEL("CopyContent", allowed(encrypted,permissions,PdfWriter.ALLOW_COPY));
			info.setEL("DocumentAssembly", allowed(encrypted,permissions,PdfWriter.ALLOW_ASSEMBLY+PdfWriter.ALLOW_MODIFY_CONTENTS));
			info.setEL("FillingForm", allowed(encrypted,permissions,PdfWriter.ALLOW_FILL_IN+PdfWriter.ALLOW_MODIFY_ANNOTATIONS));
			info.setEL("Printing", allowed(encrypted,permissions,PdfWriter.ALLOW_PRINTING));
			info.setEL("Secure", "");
			info.setEL("Signing", allowed(encrypted,permissions,PdfWriter.ALLOW_MODIFY_ANNOTATIONS+PdfWriter.ALLOW_MODIFY_CONTENTS+PdfWriter.ALLOW_FILL_IN));
			
			info.setEL("Encryption", encrypted?"Password Security":"No Security");// MUST
			info.setEL("TotalPages", Caster.toDouble(pr.getNumberOfPages()));
			info.setEL("Version", "1."+pr.getPdfVersion());
			info.setEL("permissions", ""+permissions);
			info.setEL("permiss", ""+PdfWriter.ALLOW_FILL_IN);
			
			info.setEL("Application", "");
			info.setEL("Author", "");
			info.setEL("CenterWindowOnScreen", "");
			info.setEL("Created", "");
			info.setEL("FitToWindow", "");
			info.setEL("HideMenubar", "");
			info.setEL("HideToolbar", "");
			info.setEL("HideWindowUI", "");
			info.setEL("Keywords", "");
			info.setEL("Language", "");
			info.setEL("Modified", "");
			info.setEL("PageLayout", "");
			info.setEL("Producer", "");
			info.setEL("Properties", "");
			info.setEL("ShowDocumentsOption", "");
			info.setEL("ShowWindowsOption", "");
			info.setEL("Subject", "");
			info.setEL("Title", "");
			info.setEL("Trapped", "");
	
			// info
			HashMap imap = pr.getInfo();
			Iterator it = imap.entrySet().iterator();
			Map.Entry entry;
			while(it.hasNext()) {
				entry=(Entry) it.next();
				info.setEL(Caster.toString(entry.getKey(),null), entry.getValue());
			}
			return info;
		}
		catch(PageException pe) {
			throw new PageRuntimeException(pe);
		}
		finally {
			if(pr!=null)pr.close();
		}
	}
	

	

	private static Object allowed(boolean encrypted, int permissions, int permission) {
		return (!encrypted || (permissions&permission)>0)?"Allowed":"Not Allowed";
	}



	public void setPages(String strPages) throws PageException {
		if(StringUtil.isEmpty(strPages))return;
		if(pages==null)
			pages=new HashSet<Integer>();
		PDFUtil.parsePageDefinition(pages,strPages);
	}

	public Set<Integer> getPages() {
		//if(pages==null)pages=new HashSet();
		return pages;
	}

	public Resource getResource() {
		return resource;
	}
	public byte[] getRaw() throws IOException {
		if(barr!=null)return barr;
		return IOUtil.toBytes(resource);
	}

	@Override
	public boolean containsValue(Object value) {
		return getInfo().containsValue(value);
	}

	@Override
	public java.util.Collection values() {
		return getInfo().values();
	}
	
	public PDDocument toPDDocument() throws CryptographyException, InvalidPasswordException, IOException {
		PDDocument doc;
		if(barr!=null) 
			doc= PDDocument.load(new ByteArrayInputStream(barr,0,barr.length));
		else if(resource instanceof FileResource)
			doc= PDDocument.load((File)resource);
		else 
			doc= PDDocument.load(new ByteArrayInputStream(IOUtil.toBytes(resource),0,barr.length));
		
		if(password!=null)doc.decrypt(password);
		
		
		return doc;
		
	}

}