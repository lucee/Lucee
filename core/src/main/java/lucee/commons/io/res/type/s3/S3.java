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
package lucee.commons.io.res.type.s3;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lucee.commons.digest.Hash;
import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.URLEncoder;
import lucee.commons.net.http.Entity;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient3.HTTPEngine3Impl;
import lucee.loader.util.Util;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.dt.DateTime;

import org.apache.commons.collections4.map.ReferenceMap;
import org.xml.sax.SAXException;

public final class S3 implements S3Constants {

	private static final String DEFAULT_URL="s3.amazonaws.com";
	
	private String secretAccessKey;
	private String accessKeyId;
	private TimeZone timezone;
	private String host;


	private static final Map<String,S3Info> infos=new ReferenceMap<String,S3Info>();
	private static final Map<String,AccessControlPolicy> acps=new ReferenceMap<String,AccessControlPolicy>();

	public static final int MAX_REDIRECT = 15;

	@Override
	public String toString(){
		return "secretAccessKey:"+secretAccessKey+";accessKeyId:"+accessKeyId+";host:"+host+";timezone:"+
		(timezone==null?"":timezone.getID());
	}
	
	public String hash() {
		try {
			return Hash.md5(toString());
		}
		catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public S3(String secretAccessKey, String accessKeyId,TimeZone tz) {
		host=DEFAULT_URL;
		this.secretAccessKey = secretAccessKey;
		this.accessKeyId = accessKeyId;
		this.timezone = tz;
		//testFinal();
	}
	
	

	public S3() {
		
		//testFinal();
	}
	
	/**
	 * @return the secretAccessKey
	 * @throws S3Exception 
	 */
	String getSecretAccessKeyValidate() throws S3Exception {
		if(StringUtil.isEmpty(secretAccessKey))
			throw new S3Exception("secretAccessKey is not defined, define in the application event handler "
		+" (s3.awsSecretKey) or as part of the path.");
		return secretAccessKey;
	}
	
	/**
	 * @return the accessKeyId
	 * @throws S3Exception 
	 */
	String getAccessKeyIdValidate() throws S3Exception {
		if(StringUtil.isEmpty(accessKeyId))
			throw new S3Exception("accessKeyId is not defined, define in "
		+"application event handler (this.s3.accessKeyId) or as part of the path.");
		return accessKeyId;
	}
	
	String getSecretAccessKey() {
		return secretAccessKey;
	}
	
	/**
	 * @return the accessKeyId
	 * @throws S3Exception 
	 */
	String getAccessKeyId() {
		return accessKeyId;
	}

	/**
	 * @return the tz
	 */
	TimeZone getTimeZone() {
		if(timezone==null)timezone=ThreadLocalPageContext.getTimeZone();
		return timezone;
	}
	
	private static byte[] HMAC_SHA1(String key, String message,String charset) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
		
			SecretKeySpec sks = new SecretKeySpec(key.getBytes(charset),"HmacSHA1");
			Mac mac = Mac.getInstance(sks.getAlgorithm());
			mac.init(sks);
			mac.update(message.getBytes(charset));
			return mac.doFinal();
		
	}

	private static String createSignature(String str, String secretAccessKey,String charset) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		//str=StringUtil.replace(str, "\\n", String.valueOf((char)10), false);
		byte[] digest = HMAC_SHA1(secretAccessKey,str,charset);
		try {
			return Caster.toB64(digest);
		} catch (Throwable t) {
			throw new IOException(t.getMessage());
		}
	}
	
	public InputStream listBucketsRaw() throws MalformedURLException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		String dateTimeString = Util.toHTTPTimeString();
		String signature = createSignature("GET\n\n\n"+dateTimeString+"\n/", getSecretAccessKeyValidate(), "iso-8859-1");
		
		HTTPResponse rsp = HTTPEngine3Impl.get(new URL("http://"+host), null, null, -1,MAX_REDIRECT, null, Constants.NAME, null,
				new Header[]{
					header("Date",dateTimeString),
					header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature)
				}
		);
		return rsp.getContentAsStream();
		
	}
	

	public HTTPResponse head(String bucketName, String objectName) throws MalformedURLException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		bucketName=checkBucket(bucketName);
		boolean hasObj=!StringUtil.isEmpty(objectName);
		if(hasObj)objectName=checkObjectName(objectName);
		
		String dateTimeString = Util.toHTTPTimeString();
		String signature = createSignature("HEAD\n\n\n"+dateTimeString+"\n/"+bucketName+"/"+(hasObj?objectName:""), getSecretAccessKeyValidate(), "iso-8859-1");
		
		
		List<Header> headers=new ArrayList<Header>();
		headers.add(header("Date",dateTimeString));
		headers.add(header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature));
		headers.add(header("Host",bucketName+"."+host));
		
		String strUrl="http://"+bucketName+"."+host+"/";
		//if(Util.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/";
		if(hasObj) {
			strUrl+=objectName;
		}
		HTTPResponse method = HTTPEngine3Impl.head(new URL(strUrl), null, null, -1,MAX_REDIRECT, null, Constants.NAME, null,headers.toArray(new Header[headers.size()]));
		return method;
		
	}
	
	
	
	
	public InputStream aclRaw(String bucketName, String objectName) throws MalformedURLException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		bucketName=checkBucket(bucketName);
		boolean hasObj=!StringUtil.isEmpty(objectName);
		if(hasObj)objectName=checkObjectName(objectName);
		
		String dateTimeString = Util.toHTTPTimeString();
		String signature = createSignature("GET\n\n\n"+dateTimeString+"\n/"+bucketName+"/"+(hasObj?objectName:"")+"?acl", getSecretAccessKeyValidate(), "iso-8859-1");
		
		
		List<Header> headers=new ArrayList<Header>();
		headers.add(header("Date",dateTimeString));
		headers.add(header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature));
		headers.add(header("Host",bucketName+"."+host));
		
		String strUrl="http://"+bucketName+"."+host+"/";
		//if(Util.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/";
		if(hasObj) {
			strUrl+=objectName;
		}
		strUrl+="?acl";
		
		HTTPResponse method = HTTPEngine3Impl.get(new URL(strUrl), null, null, -1,MAX_REDIRECT, null, Constants.NAME, null,headers.toArray(new Header[headers.size()]));
		return method.getContentAsStream();
		
	}
	
	public AccessControlPolicy getAccessControlPolicy(String bucketName, String objectName) throws InvalidKeyException, MalformedURLException, NoSuchAlgorithmException, IOException, SAXException {
		InputStream raw = aclRaw(bucketName,objectName);
		//print.o(IOUtil.toString(raw, null));
		ACLFactory factory=new ACLFactory(raw, this);
		return factory.getAccessControlPolicy();
	}
	
	


	public void setAccessControlPolicy(String bucketName, String objectName,AccessControlPolicy acp) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SAXException {
		bucketName=checkBucket(bucketName);
		boolean hasObj=!StringUtil.isEmpty(objectName);
		if(hasObj)objectName=checkObjectName(objectName);
		

		Entity re = HTTPEngine3Impl.getByteArrayEntity(acp.toXMLString().getBytes(CharsetUtil.ISO88591),"text/html");
		
		
		String dateTimeString = Util.toHTTPTimeString();
		
		
		String cs = "PUT\n\n"+re.contentType()+"\n"+dateTimeString+"\n/"+bucketName+"/"+(hasObj?objectName:"")+"?acl";
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		Header[] headers = new Header[]{
				header("Content-Type",re.contentType()),
				header("Content-Length",Long.toString(re.contentLength())),
				header("Date",dateTimeString),
				header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature),
		};
		
		String strUrl="http://"+bucketName+"."+host+"/";
		if(hasObj) {
			strUrl+=objectName;
		}
		strUrl+="?acl";
		
		
		
		HTTPResponse method = HTTPEngine3Impl.put(new URL(strUrl), null, null, -1,MAX_REDIRECT, null,null, 
				Constants.NAME, null,headers,re);
		if(method.getStatusCode()!=200){
			new ErrorFactory(method.getContentAsStream());
		}
		
		
	}
	
	public InputStream listContentsRaw(String bucketName,String prefix,String marker,int maxKeys) throws MalformedURLException, IOException, InvalidKeyException, NoSuchAlgorithmException {
		bucketName=checkBucket(bucketName);
		String dateTimeString = Util.toHTTPTimeString();
		String signature = createSignature("GET\n\n\n"+dateTimeString+"\n/"+bucketName+"/", getSecretAccessKeyValidate(), "iso-8859-1");
		
		
		List<Header> headers=new ArrayList<Header>();
		headers.add(header("Date",dateTimeString));
		headers.add(header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature));
		headers.add(header("Host",bucketName+"."+host));
		
		String strUrl="http://"+bucketName+"."+host+"/";
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/";
		
		
		char amp='?';
		if(!Util.isEmpty(prefix)){
			strUrl+=amp+"prefix="+encodeEL(prefix);
			amp='&';
		}
		if(!Util.isEmpty(marker)) {
			strUrl+=amp+"marker="+encodeEL(marker);
			amp='&';
		}
		if(maxKeys!=-1) {
			strUrl+=amp+"max-keys="+maxKeys;
			amp='&';
		}
		
		HTTPResponse method = HTTPEngine3Impl.get(new URL(strUrl), null, null, -1,MAX_REDIRECT, null, Constants.NAME, null,headers.toArray(new Header[headers.size()]));
		return method.getContentAsStream();
	}
	

	public Content[] listContents(String bucketName,String prefix) throws InvalidKeyException, MalformedURLException, NoSuchAlgorithmException, IOException, SAXException {
		String marker=null,last=null;
		ContentFactory factory;
		Content[] contents;
		List<Content[]> list = new ArrayList<Content[]>();
		int size=0;
		while(true) {
			factory = new ContentFactory(listContentsRaw(bucketName, prefix, marker, -1),this);
			contents = factory.getContents();
			list.add(contents);
			size+=contents.length;
			if(factory.isTruncated() && contents.length>0) {
				last=marker;
				marker=contents[contents.length-1].getKey();
				if(marker.equals(last))break;
			}
			else break;
		}
		
		if(list.size()==1) return list.get(0);
		if(list.size()==0) return new Content[0];
		
		Content[] rtn=new Content[size];
		Iterator<Content[]> it = list.iterator();
		int index=0;
		while(it.hasNext()) {
			contents=it.next();
			for(int i=0;i<contents.length;i++) {
				rtn[index++]=contents[i];
			}
		}
		
		return rtn;
	}

	public Content[] listContents(String bucketName,String prefix,String marker,int maxKeys) throws InvalidKeyException, MalformedURLException, NoSuchAlgorithmException, IOException, SAXException {
		InputStream raw = listContentsRaw(bucketName, prefix, marker, maxKeys);
		//print.o(IOUtil.toString(raw, null));
		ContentFactory factory = new ContentFactory(raw,this);
		return factory.getContents();
	}
	
	public Bucket[] listBuckets() throws InvalidKeyException, MalformedURLException, NoSuchAlgorithmException, IOException, SAXException {
		InputStream raw = listBucketsRaw();
		//print.o(IOUtil.toString(raw, null));
		BucketFactory factory = new BucketFactory(raw,this);
		return factory.getBuckets();
	}
	
	public void putBuckets(String bucketName,int acl, int storage) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SAXException {
		String strXML = "";
		if(storage==STORAGE_EU) {
			strXML="<CreateBucketConfiguration><LocationConstraint>EU</LocationConstraint></CreateBucketConfiguration>";
		}
		
		byte[] barr = strXML.getBytes(CharsetUtil.ISO88591);
		put(bucketName, null, acl,HTTPEngine3Impl.getByteArrayEntity(barr,"text/html"));	
	}
	
	/*public void putObject(String bucketName,String objectName,int acl,Resource res) throws IOException, InvalidKeyException, NoSuchAlgorithmException, PageException, SAXException, EncoderException {
		String contentType = IOUtil.getMimeType(res, "application");
		InputStream is = null;
		try {
			is = res.getInputStream();
			put(bucketName, objectName, acl, is, contentType);
		}
		finally {
			IOUtil.closeEL(is);
		}
	}*/
	/*
	public void put(String bucketName,String objectName,int acl, InputStream is,long length, String contentType) throws IOException, InvalidKeyException, NoSuchAlgorithmException, PageException, SAXException, EncoderException {
		put(bucketName, objectName, acl, HTTPUtil.toRequestEntity(is),length, contentType);
	}*/
		
	public void put(String bucketName,String objectName,int acl, Entity re) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SAXException {
		bucketName=checkBucket(bucketName);
		objectName=checkObjectName(objectName);
		
		String dateTimeString = Util.toHTTPTimeString();
		// Create a canonical string to send based on operation requested 
		String cs = "PUT\n\n"+re.contentType()+"\n"+dateTimeString+"\nx-amz-acl:"+toStringACL(acl)+"\n/"+bucketName+"/"+objectName;
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		Header[] headers = new Header[]{
				header("Content-Type",re.contentType()),
				header("Content-Length",Long.toString(re.contentLength())),
				header("Date",dateTimeString),
				header("x-amz-acl",toStringACL(acl)),
				header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature),
		};
		
		String strUrl="http://"+bucketName+"."+host+"/"+objectName;
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/"+objectName;
		
		
		
		HTTPResponse method = HTTPEngine3Impl.put(new URL(strUrl), null, null, -1,MAX_REDIRECT, null,null, 
				Constants.NAME, null,headers,re);
		if(method.getStatusCode()!=200){
			new ErrorFactory(method.getContentAsStream());
		}
		
		
	}
		
	public HttpURLConnection preput(String bucketName,String objectName,int acl, String contentType) throws IOException, InvalidKeyException, NoSuchAlgorithmException {
		bucketName=checkBucket(bucketName);
		objectName=checkObjectName(objectName);
		
		String dateTimeString = Util.toHTTPTimeString();
		// Create a canonical string to send based on operation requested 
		String cs = "PUT\n\n"+contentType+"\n"+dateTimeString+"\nx-amz-acl:"+toStringACL(acl)+"\n/"+bucketName+"/"+objectName;
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		
		String strUrl="http://"+bucketName+"."+host+"/"+objectName;
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/"+objectName;
		
		URL url = new URL(strUrl);
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("PUT");
		
		conn.setFixedLengthStreamingMode(227422142);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		conn.setRequestProperty("CONTENT-TYPE", contentType);
		conn.setRequestProperty("USER-AGENT", "S3 Resource");        
		//conn.setRequestProperty("Transfer-Encoding", "chunked" );
		conn.setRequestProperty("Date", dateTimeString);
		conn.setRequestProperty("x-amz-acl", toStringACL(acl));
		conn.setRequestProperty("Authorization", "AWS "+getAccessKeyIdValidate()+":"+signature);
		return conn;
	}

	public String getObjectLink(String bucketName,String objectName,int secondsValid) throws InvalidKeyException, NoSuchAlgorithmException, IOException {
		bucketName=checkBucket(bucketName);
		objectName=checkObjectName(objectName);
		
		long epoch = (System.currentTimeMillis()/1000)+(secondsValid);
		String cs = "GET\n\n\n"+epoch+"\n/"+bucketName+"/"+objectName;
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		
		String strUrl="http://"+bucketName+"."+host+"/"+objectName;
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/"+objectName;
		
		
		return strUrl+"?AWSAccessKeyId="+getAccessKeyIdValidate()+"&Expires="+epoch+"&Signature="+signature;
	}

	public InputStream getInputStream(String bucketName,String objectName) throws InvalidKeyException, NoSuchAlgorithmException, IOException, SAXException  {
		return getData(bucketName, objectName).getContentAsStream();
	}
	
	public Map<String, String> getMetadata(String bucketName,String objectName) throws InvalidKeyException, NoSuchAlgorithmException, IOException, SAXException  {
		HTTPResponse method = getData(bucketName, objectName);
		Header[] headers = method.getAllHeaders();
		Map<String,String> rtn=new HashMap<String, String>();
		String name;
		if(headers!=null)for(int i=0;i<headers.length;i++){
			name=headers[i].getName();
			if(name.startsWith("x-amz-meta-"))
				rtn.put(name.substring(11), headers[i].getValue());
		}
		return rtn;
	}
	
	private HTTPResponse getData(String bucketName,String objectName) throws InvalidKeyException, NoSuchAlgorithmException, IOException, SAXException  {
		bucketName=checkBucket(bucketName);
		objectName=checkObjectName(objectName);
		
		String dateTimeString = Util.toHTTPTimeString();
		//long epoch = (System.currentTimeMillis()/1000)+6000;
		String cs = "GET\n\n\n"+dateTimeString+"\n/"+bucketName+"/"+objectName;
		    
		
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		
		String strUrl="http://"+bucketName+"."+host+"/"+objectName;
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/"+objectName;
		URL url = new URL(strUrl);
		
		
		HTTPResponse method = HTTPEngine3Impl.get(url, null, null, -1,MAX_REDIRECT, null, Constants.NAME, null,
				new Header[]{
				header("Date",dateTimeString),
				header("Host",bucketName+"."+host),
				header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature)
				}
		);
		if(method.getStatusCode()!=200)
			new ErrorFactory(method.getContentAsStream());
		return method;
	}

	public void delete(String bucketName, String objectName) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SAXException {
		bucketName=checkBucket(bucketName);
		objectName = checkObjectName(objectName);

		String dateTimeString = Util.toHTTPTimeString();
		// Create a canonical string to send based on operation requested 
		String cs ="DELETE\n\n\n"+dateTimeString+"\n/"+bucketName+"/"+objectName;
		//print.out(cs);
		String signature = createSignature(cs, getSecretAccessKeyValidate(), "iso-8859-1");
		
		Header[] headers =new Header[]{
				header("Date",dateTimeString),
				header("Authorization","AWS "+getAccessKeyIdValidate()+":"+signature),
		};
		
		String strUrl="http://"+bucketName+"."+host+"/"+objectName;
		if(StringUtil.hasUpperCase(bucketName))strUrl="http://"+host+"/"+bucketName+"/"+objectName;
		
		
		
		HTTPResponse rsp = HTTPEngine3Impl.delete(new URL(strUrl), null, null, -1,MAX_REDIRECT, null, Constants.NAME,  null,headers);
		
		if(rsp.getStatusCode()!=200)
			new ErrorFactory(rsp.getContentAsStream());
	}

	
	
	
	
	
	// --------------------------------
	public static String toStringACL(int acl) throws S3Exception {
		switch(acl) {
			case ACL_AUTH_READ:return "authenticated-read";
			case ACL_PUBLIC_READ:return "public-read";
			case ACL_PRIVATE:return "private";
			case ACL_PUBLIC_READ_WRITE:return "public-read-write";
		}
		throw new S3Exception("invalid acl definition");
	}

	public static String toStringStorage(int storage) throws S3Exception {
		String s = toStringStorage(storage, null);
		if(s==null)
			throw new S3Exception("invalid storage definition");
		return s;
	}
	public static String toStringStorage(int storage, String defaultValue) {
		switch(storage) {
			case STORAGE_EU:return "eu";
			case STORAGE_US:return "us";
			case STORAGE_US_WEST:return "us-west";
		}
		return defaultValue;
	}
	
	public static int toIntACL(String acl) throws S3Exception {
		acl=acl.toLowerCase().trim();
		if("public-read".equals(acl)) return ACL_PUBLIC_READ;
		if("private".equals(acl)) return ACL_PRIVATE;
		if("public-read-write".equals(acl)) return ACL_PUBLIC_READ_WRITE;
		if("authenticated-read".equals(acl)) return ACL_AUTH_READ;
		
		if("public_read".equals(acl)) return ACL_PUBLIC_READ;
		if("public_read_write".equals(acl)) return ACL_PUBLIC_READ_WRITE;
		if("authenticated_read".equals(acl)) return ACL_AUTH_READ;
		
		if("publicread".equals(acl)) return ACL_PUBLIC_READ;
		if("publicreadwrite".equals(acl)) return ACL_PUBLIC_READ_WRITE;
		if("authenticatedread".equals(acl)) return ACL_AUTH_READ;
		
		throw new S3Exception("invalid acl value, valid values are [public-read, private, public-read-write, authenticated-read]");
	}

	public static int toIntStorage(String storage) throws S3Exception {
		int s=toIntStorage(storage,-1);
		if(s==-1)
			throw new S3Exception("invalid storage value, valid values are [eu,us,us-west]");
		return s;
	}
	public static int toIntStorage(String storage, int defaultValue) {
		storage=storage.toLowerCase().trim();
		if("us".equals(storage)) return STORAGE_US;
		if("usa".equals(storage)) return STORAGE_US;
		if("eu".equals(storage)) return STORAGE_EU;
		
		if("u.s.".equals(storage)) return STORAGE_US;
		if("u.s.a.".equals(storage)) return STORAGE_US;
		if("europe.".equals(storage)) return STORAGE_EU;
		if("euro.".equals(storage)) return STORAGE_EU;
		if("e.u.".equals(storage)) return STORAGE_EU;
		if("united states of america".equals(storage)) return STORAGE_US;
		
		if("us-west".equals(storage)) return STORAGE_US_WEST;
		if("usa-west".equals(storage)) return STORAGE_US_WEST;
		
		
		return defaultValue;
	}
	

	private String checkObjectName(String objectName) throws UnsupportedEncodingException {
		if(Util.isEmpty(objectName)) return "";
		if(objectName.startsWith("/"))objectName=objectName.substring(1);
		return encode(objectName);
	}

	private String checkBucket(String name) {
		/*if(!Decision.isVariableName(name)) 
			throw new S3Exception("invalid bucket name definition ["+name+"], name should only contain letters, digits, dashes and underscores");
		
		if(name.length()<3 || name.length()>255) 
			throw new S3Exception("invalid bucket name definition ["+name+"], the length of a bucket name must be between 3 and 255");
		*/
		
		return encodeEL(name);
	}

	private String encodeEL(String name) {
		try {
			return encode(name);
		
		} catch (UnsupportedEncodingException e) {
			return name;
		}
	}
	private String encode(String name) throws UnsupportedEncodingException {
		return URLEncoder.encode(name,"UTF-8");
	}

	/**
	 * @param secretAccessKey the secretAccessKey to set
	 */
	public void setSecretAccessKey(String secretAccessKey) {
		this.secretAccessKey = secretAccessKey;
	}

	/**
	 * @param accessKeyId the accessKeyId to set
	 */
	public void setAccessKeyId(String accessKeyId) {
		this.accessKeyId = accessKeyId;
	}

	/**
	 * @param url the url to set
	 */
	public void setHost(String host) {
		this.host=host;
	}

	public String getHost() {
		return host;
	}

	public S3Info getInfo(String path) {
		return infos.get(toKey(path));
	}

	public void setInfo(String path,S3Info info) {
		infos.put(toKey(path),info);
	}

	public AccessControlPolicy getACP(String path) {
		return acps.get(toKey(path));
	}

	public void setACP(String path,AccessControlPolicy acp) {
		acps.put(toKey(path),acp);
	}

	public void releaseCache(String path) {
		Object k = toKey(path);
		infos.remove(k);
		acps.remove(k);
	}

	private String toKey(String path) {
		return toString()+":"+path.toLowerCase();
	}
	
	private Header header(String name, String value) {
		return HTTPEngine3Impl.header(name,value);
	}

	public static DateTime toDate(String strDate, TimeZone tz) throws PageException {
		if(strDate.endsWith("Z"))
			strDate=strDate.substring(0,strDate.length()-1);
		return Caster.toDate(strDate, tz);
	}
}