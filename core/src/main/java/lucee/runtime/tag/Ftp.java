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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.runtime.PageContextImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.tag.TagImpl;
import lucee.runtime.net.ftp.FTPConnection;
import lucee.runtime.net.ftp.FTPConnectionImpl;
import lucee.runtime.net.ftp.FTPConstant;
import lucee.runtime.net.ftp.FTPPath;
import lucee.runtime.net.ftp.FTPPool;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTimeImpl;
import lucee.runtime.type.util.ListUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

/**
* 
* Lets users implement File Transfer Protocol (FTP) operations.
*
*
*
**/
public final class Ftp extends TagImpl {
    
    private static final String ASCCI_EXT_LIST="txt;htm;html;cfm;cfml;shtm;shtml;css;asp;asa";

	private static final Key SUCCEEDED = KeyImpl.intern("succeeded");
	private static final Key ERROR_CODE = KeyImpl.intern("errorCode");
	private static final Key ERROR_TEXT = KeyImpl.intern("errorText");
	private static final Key RETURN_VALUE = KeyImpl.intern("returnValue");
	private static final Key CFFTP = KeyImpl.intern("cfftp");
	/*private static final Key  = KeyImpl.getInstance();
	private static final Key  = KeyImpl.getInstance();
	private static final Key  = KeyImpl.getInstance();
	private static final Key  = KeyImpl.getInstance();
	private static final Key  = KeyImpl.getInstance();
	private static final Key  = KeyImpl.getInstance();*/
	
    private FTPPool pool;

	private String action;
	private String username;
	private String password;
	private String server;
	private int timeout=30;
	private int port=21;
	private String connectionName;
	private int retrycount=1;
	private int count=0;
	private boolean stoponerror=true;
	private boolean passive;
	private String name;
	private String directory;
	private String ASCIIExtensionList=ASCCI_EXT_LIST;
	private short transferMode=FTPConstant.TRANSFER_MODE_AUTO;
	private String remotefile;
	private String localfile;
	private boolean failifexists=true;
	private String existing;
	private String _new;
    private String item;
    private String result;

    private String proxyserver;
	private int proxyport=80;
	private String proxyuser;
	private String proxypassword="";
	
	//private Struct cfftp=new StructImpl();

    @Override
	public void release()	{
		super.release();
		this.pool=null;
		
		this.action=null;
		this.username=null;
		this.password=null;
		this.server=null;
		this.timeout=30;
		this.port=21;
		this.connectionName=null;
		this.proxyserver=null;
		this.proxyport=80;
		this.proxyuser=null;
		this.proxypassword="";
		this.retrycount=1;
		this.count=0;
		this.stoponerror=true;
		this.passive=false;
		this.name=null;
		this.directory=null;
		this.ASCIIExtensionList=ASCCI_EXT_LIST;
		this.transferMode=FTPConstant.TRANSFER_MODE_AUTO;
		this.remotefile=null;
		this.localfile=null;
		this.failifexists=true;
		this.existing=null;
		this._new=null;
		this.item=null;
        this.result=null;
	}

	/**
	 * sets the attribute action
	 * @param action
	 */
	public void setAction(String action) {
		this.action=action.trim().toLowerCase();
	}

	@Override
	public int doStartTag()	{
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException	{
	    pool=((PageContextImpl)pageContext).getFTPPool();
	    FTPClient client = null; 
	    
	    
	    // retries
	    do {
		    try {
			    if(action.equals("open")) client=actionOpen();
			    else if(action.equals("close")) client=actionClose();
			    else if(action.equals("changedir")) client=actionChangeDir();
			    else if(action.equals("createdir")) client=actionCreateDir();
			    else if(action.equals("listdir")) client=actionListDir();
			    else if(action.equals("removedir")) client=actionRemoveDir();
			    else if(action.equals("getfile")) client=actionGetFile();
			    else if(action.equals("putfile")) client=actionPutFile();
			    else if(action.equals("rename")) client=actionRename();
			    else if(action.equals("remove")) client=actionRemove();
			    else if(action.equals("getcurrentdir")) client=actionGetCurrentDir();
			    else if(action.equals("getcurrenturl")) client=actionGetCurrentURL();
			    else if(action.equals("existsdir")) client=actionExistsDir();
			    else if(action.equals("existsfile")) client=actionExistsFile();
			    else if(action.equals("exists")) client=actionExists();
			    //else if(action.equals("copy")) client=actionCopy();
			    
			     
			    else throw new ApplicationException(
			            "attribute action has an invalid value ["+action+"]",
			            "valid values are [open,close,listDir,createDir,removeDir,changeDir,getCurrentDir," +
			            "getCurrentURL,existsFile,existsDir,exists,getFile,putFile,rename,remove]");

		    }
		    catch(IOException ioe) {
		        if(count++<retrycount)continue;
		        throw Caster.toPageException(ioe);
		    }
		
		    if(client==null || !checkCompletion(client))break;
	    }while(true);
	    
		return EVAL_PAGE;
	}

    /**
     * check if a file or directory exists
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    private FTPClient actionExists() throws PageException, IOException {
        required("item",item); 

        FTPClient client = getClient();
        FTPFile file=existsFile(client,item,false);
        Struct cfftp = writeCfftp(client);

        cfftp.setEL(RETURN_VALUE,Caster.toBoolean(file!=null));
        cfftp.setEL(SUCCEEDED,Boolean.TRUE);
        
        return client;
    }

    /**
     * check if a directory exists or not
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    private FTPClient actionExistsDir() throws PageException, IOException {
    	required("directory",directory); 

    	FTPClient client = getClient();
    	boolean res = existsDir(client,directory);
        Struct cfftp = writeCfftp(client);

        cfftp.setEL(RETURN_VALUE,Caster.toBoolean(res));
        cfftp.setEL(SUCCEEDED,Boolean.TRUE);
        
        stoponerror=false;
        return client;
    	
    	/*FTPClient client = pool.get(createConnection());
        FTPFile file=existsFile(client,directory);
        Struct cfftp = writeCfftp(client);

        cfftp.setEL(RETURN_VALUE,Caster.toBoolean(file!=null && file.isDirectory()));
        cfftp.setEL(SUCCEEDED,Boolean.TRUE);
        
        stoponerror=false;
        return client;*/
    }

    /**
     * check if a file exists or not
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    private FTPClient actionExistsFile() throws PageException, IOException {
        required("remotefile",remotefile); 

        FTPClient client = getClient();
        FTPFile file=existsFile(client,remotefile,true);
        
        Struct cfftp = writeCfftp(client);

        cfftp.setEL(RETURN_VALUE,Caster.toBoolean(file!=null && file.isFile()));
        cfftp.setEL(SUCCEEDED,Boolean.TRUE);
        
        stoponerror=false;
        return client;
    
    
    
    }

    
    
	
    /* *
     * check if file or directory exists if it exists return FTPFile otherwise null
     * @param client
     * @param strPath
     * @return FTPFile or null
     * @throws IOException
     * @throws PageException
     * /
    private FTPFile exists(FTPClient client, String strPath) throws PageException, IOException {
        strPath=strPath.trim();
        
        // get parent path
        FTPPath path=new FTPPath(client.printWorkingDirectory(),strPath);
        String name=path.getName();
        print.out("path:"+name);
        
        // when directory
        FTPFile[] files=null;
        try {
            files = client.listFiles(path.getPath());
        } catch (IOException e) {}
        
        if(files!=null) {
            for(int i=0;i<files.length;i++) {
                if(files[i].getName().equalsIgnoreCase(name)) {
                    return files[i];
                }
            }
            
        }
        return null;
    }*/
    
    private FTPFile existsFile(FTPClient client, String strPath,boolean isFile) throws PageException, IOException {
        strPath=strPath.trim();
        if(strPath.equals("/")) {
            FTPFile file= new FTPFile();
            file.setName("/");
            file.setType(FTPFile.DIRECTORY_TYPE);
            return file;
        }
        
        // get parent path
        FTPPath path=new FTPPath(client.printWorkingDirectory(),strPath);
        String p=path.getPath();
        String n=path.getName();

        strPath=p;
        if("//".equals(p))strPath="/";
        if(isFile)strPath+=n;
        
        // when directory
        FTPFile[] files=null;
        try {
			files = client.listFiles(strPath);
		} catch (IOException e) {}
        
        if(files!=null) {
            for(int i=0;i<files.length;i++) {
            	if(files[i].getName().equalsIgnoreCase(n)) {
                    return files[i];
                }
            }
            
        }
        return null;
    }
    
    private boolean existsDir(FTPClient client, String strPath) throws PageException, IOException {
        strPath=strPath.trim();
        
        // get parent path
        FTPPath path=new FTPPath(client.printWorkingDirectory(),strPath);
        String p=path.getPath();
        String n=path.getName();

        strPath=p+""+n;
        if("//".equals(p))strPath="/"+n;
        if(!strPath.endsWith("/"))strPath+="/";
        
        String pwd = client.printWorkingDirectory();
        boolean rc = client.changeWorkingDirectory(directory);
        client.changeWorkingDirectory(pwd);
        return rc;
    }

    /**
     * removes a file on the server
     * @return FTPCLient
     * @throws IOException
     * @throws PageException 
     */
    private FTPClient actionRemove() throws IOException, PageException {
        required("item",item);
        FTPClient client = getClient();
        client.deleteFile(item);
        writeCfftp(client);
        
        return client;
    }

    /**
     * rename a file on the server
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    private FTPClient actionRename() throws PageException, IOException {
        required("existing",existing); 
        required("new",_new);
        
        FTPClient client = getClient();
		client.rename(existing,_new);
        writeCfftp(client);
        
        return client;
    }

    /**
     * copy a local file to server
     * @return FTPClient
     * @throws IOException
     * @throws PageException
     */
    private FTPClient actionPutFile() throws IOException, PageException  {
        required("remotefile",remotefile); 
        required("localfile",localfile); 
        
		FTPClient client = getClient();
		Resource local=ResourceUtil.toResourceExisting(pageContext ,localfile);//new File(localfile);
		//	if(failifexists && local.exists()) throw new ApplicationException("File ["+local+"] already exist, if you want to overwrite, set attribute failIfExists to false");
		InputStream is=null;
		
        try {
        	is=IOUtil.toBufferedInputStream(local.getInputStream());
        	client.setFileType(getType(local));
            client.storeFile(remotefile,is);
        }
        finally {
        	IOUtil.closeEL(is);
        }
        writeCfftp(client);
        
        return client;
    }

    /**
     * gets a file from server and copy it local
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    private FTPClient actionGetFile() throws PageException, IOException {
        required("remotefile",remotefile); 
        required("localfile",localfile); 
		
        
		FTPClient client = getClient();
		Resource local=ResourceUtil.toResourceExistingParent(pageContext ,localfile);//new File(localfile);
        pageContext.getConfig().getSecurityManager().checkFileLocation(local);
		if(failifexists && local.exists()) throw new ApplicationException("File ["+local+"] already exist, if you want to overwrite, set attribute failIfExists to false");
		OutputStream fos=null;
        client.setFileType(getType(local));
        try {
        	fos=IOUtil.toBufferedOutputStream(local.getOutputStream());
            client.retrieveFile(remotefile,fos);
        }
        finally {
        	IOUtil.closeEL(fos);
        }
        writeCfftp(client);
        
        return client;
    }

    /**
     * get url of the working directory
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    private FTPClient actionGetCurrentURL() throws PageException, IOException {
        FTPClient client = getClient();
        String pwd=client.printWorkingDirectory();
        Struct cfftp = writeCfftp(client); 
        cfftp.setEL("returnValue","ftp://"+client.getRemoteAddress().getHostName()+pwd);
        return client;
    }

    /**
     * get path from the working directory
     * @return FTPCLient
     * @throws IOException
     * @throws PageException
     */
    private FTPClient actionGetCurrentDir() throws PageException, IOException {
        FTPClient client = getClient();
        String pwd=client.printWorkingDirectory();
        Struct cfftp = writeCfftp(client);
        cfftp.setEL("returnValue",pwd);
        return client;
    }

    /**
     * change working directory 
     * @return FTPCLient
     * @throws IOException
     * @throws PageException 
     */
    private FTPClient actionChangeDir() throws IOException, PageException {
        required("directory",directory); 

        FTPClient client = getClient();
        client.changeWorkingDirectory(directory);
        writeCfftp(client);
        return client;
    }

    private FTPClient getClient() throws PageException, IOException {
    	return pool.get(_createConnection());
	}

	/**
     * removes a remote directory on server
     * @return FTPCLient
     * @throws IOException
     * @throws PageException 
     */
    private FTPClient actionRemoveDir() throws IOException, PageException {
        required("directory",directory); 

        FTPClient client = getClient();
        client.removeDirectory(directory);
        writeCfftp(client);
        return client;
    }

    /**
     * create a remote directory
     * @return FTPCLient
     * @throws IOException
     * @throws PageException 
     */
    private FTPClient actionCreateDir() throws IOException, PageException {
        required("directory",directory); 

        FTPClient client = getClient();
        client.makeDirectory(directory);
        writeCfftp(client);
        return client;
    }

    /**
     * List data of a ftp connection
     * @return FTPCLient
     * @throws PageException
     * @throws IOException
     */
    private FTPClient actionListDir() throws PageException, IOException {
        required("name",name);
        required("directory",directory);
        
        FTPClient client = getClient();
        FTPFile[] files = client.listFiles(directory);
        if(files==null)files=new FTPFile[0];
        
        String[] cols = new String[]{"attributes","isdirectory","lastmodified","length","mode","name",
                "path","url","type","raw"};
        String[] types = new String[]{"VARCHAR","BOOLEAN","DATE","DOUBLE","VARCHAR","VARCHAR",
                "VARCHAR","VARCHAR","VARCHAR","VARCHAR"};
        
        lucee.runtime.type.Query query=new QueryImpl(cols,types,0,"query");
        
        // translate directory path for display
        if(directory.length()==0)directory="/";
        else if(directory.startsWith("./"))directory=directory.substring(1);
        else if(directory.charAt(0)!='/')directory='/'+directory;
        if(directory.charAt(directory.length()-1)!='/')directory=directory+'/';
                
        pageContext.setVariable(name,query);
        int row=0;
        for(int i=0;i<files.length;i++) {
            FTPFile file = files[i];
            if(file.getName().equals(".") || file.getName().equals("..")) continue;
            query.addRow();
            row++;
            query.setAt("attributes",row,"");
            query.setAt("isdirectory",row,Caster.toBoolean(file.isDirectory()));
            query.setAt("lastmodified",row,new DateTimeImpl(file.getTimestamp()));
            query.setAt("length",row,Caster.toDouble(file.getSize()));
            query.setAt("mode",row,FTPConstant.getPermissionASInteger(file));
            query.setAt("type",row,FTPConstant.getTypeAsString(file.getType()));
            //query.setAt("permission",row,FTPConstant.getPermissionASInteger(file));
            query.setAt("raw",row,file.getRawListing());
            query.setAt("name",row,file.getName());
            query.setAt("path",row,directory+file.getName());
            query.setAt("url",row,"ftp://"+client.getRemoteAddress().getHostName()+""+directory+file.getName());
        }
        writeCfftp(client);
        return client;
    }

    /**	
     * Opens a FTP Connection
     * @return FTPCLinet
     * @throws IOException
     * @throws PageException 
     */
    private FTPClient actionOpen() throws IOException, PageException {
        required("server",server);
        required("username",username);
        required("password",password);
        
        
        FTPClient client = getClient();
        writeCfftp(client);
        return client;
    }

    /**
     * close a existing ftp connection
     * @return FTPCLient
     * @throws PageException 
     */
    private FTPClient actionClose() throws PageException {
        FTPConnection conn = _createConnection();
        FTPClient client = pool.remove(conn);
        
        Struct cfftp = writeCfftp(client);
        cfftp.setEL("succeeded",Caster.toBoolean(client!=null));
        return client;
    }

	/**
	 * throw a error if the value is empty (null)
     * @param attributeName
     * @param atttributValue
	 * @throws ApplicationException
     */
    private void required(String attributeName, String atttributValue) throws ApplicationException {
        if(atttributValue==null)
            throw new ApplicationException(
                    "invalid attribute constelation for the tag ftp", 
                    "attribute ["+attributeName+"] is required, if action is ["+action+"]");
    }

    /**
     * writes cfftp variable
     * @param client
     * @return FTPCLient
     * @throws PageException 
     */
    private Struct writeCfftp(FTPClient client) throws PageException  {
        Struct cfftp=new StructImpl();
        if(result==null)pageContext.variablesScope().setEL(CFFTP,cfftp);
        else pageContext.setVariable(result,cfftp);
        if(client==null) {
            cfftp.setEL(SUCCEEDED,Boolean.FALSE);
            cfftp.setEL(ERROR_CODE,new Double(-1));
            cfftp.setEL(ERROR_TEXT,"");
            cfftp.setEL(RETURN_VALUE,"");
            return cfftp;
        }
        int repCode = client.getReplyCode();
        String repStr=client.getReplyString();
        cfftp.setEL(ERROR_CODE,new Double(repCode));
        cfftp.setEL(ERROR_TEXT,repStr);
        
        cfftp.setEL(SUCCEEDED,Caster.toBoolean(FTPReply.isPositiveCompletion(repCode)));
        cfftp.setEL(RETURN_VALUE,repStr);
        return cfftp;
    }

    /**
     * check completion status of the client
     * @param client
     * @return FTPCLient
     * @throws ApplicationException
     */
    private boolean checkCompletion(FTPClient client) throws ApplicationException {
        boolean  isPositiveCompletion=FTPReply.isPositiveCompletion(client.getReplyCode());
        if(isPositiveCompletion) return false;
        if(count++<retrycount) return true;
        if(stoponerror){
        	throw new lucee.runtime.exp.FTPException(action,client);
        }
        
        return false;
    }
    
    /**
     * get FTP. ... _FILE_TYPE 
     * @param file
     * @return type
     */
    private int getType(Resource file) {
        if(transferMode==FTPConstant.TRANSFER_MODE_BINARY) return FTP.BINARY_FILE_TYPE;
        else if(transferMode==FTPConstant.TRANSFER_MODE_ASCCI) return FTP.ASCII_FILE_TYPE;
        else {
            String ext=ResourceUtil.getExtension(file,null);
            if(ext==null || ListUtil.listContainsNoCase(ASCIIExtensionList,ext,";",true,false)==-1)
                return FTP.BINARY_FILE_TYPE;
            	return FTP.ASCII_FILE_TYPE;
        }
    }
    
    /**
     * @return return a new FTP Connection Object
     */
    private FTPConnection _createConnection() {
    	
        return new FTPConnectionImpl(connectionName,server,username,password,port,timeout,transferMode,passive,proxyserver,proxyport,proxyuser,proxypassword);
    }
    
    /**
     * @param password The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
    /**
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }
    /**
     * @param server The server to set.
     */
    public void setServer(String server) {
        this.server = server;
    }
    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(double timeout) {
        this.timeout = (int)timeout;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(double port) {
        this.port = (int)port;
    }
    /**
     * @param connection The connection to set.
     */
    public void setConnection(String connection) {
        this.connectionName = connection;
    }
    /**
     * @param proxyserver The proxyserver to set.
     */
    public void setProxyserver(String proxyserver) {
        this.proxyserver = proxyserver;
    }
	
	/** set the value proxyport
	*  The port number on the proxy server from which the object is requested. Default is 80. When 
	* 	used with resolveURL, the URLs of retrieved documents that specify a port number are automatically 
	* 	resolved to preserve links in the retrieved document.
	* @param proxyport value to set
	**/
	public void setProxyport(double proxyport)	{
		this.proxyport=(int)proxyport;
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
     * @param retrycount The retrycount to set.
     */
    public void setRetrycount(double retrycount) {
        this.retrycount = (int)retrycount;
    }
    /**
     * @param stoponerror The stoponerror to set.
     */
    public void setStoponerror(boolean stoponerror) {
        this.stoponerror = stoponerror;
    }
    /**
     * @param passive The passive to set.
     */
    public void setPassive(boolean passive) {
        this.passive = passive;
    }
    /**
     * @param directory The directory to set.
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @param extensionList The aSCIIExtensionList to set.
     */
    public void setAsciiextensionlist(String extensionList) {
        ASCIIExtensionList = extensionList.toLowerCase().trim();
    }
    /**
     * @param transferMode The transferMode to set.
     */
    public void setTransfermode(String transferMode) {
        transferMode=transferMode.toLowerCase().trim();
        if(transferMode.equals("binary"))this.transferMode=FTPConstant.TRANSFER_MODE_BINARY;
        else if(transferMode.equals("ascci"))this.transferMode=FTPConstant.TRANSFER_MODE_ASCCI;
        else this.transferMode=FTPConstant.TRANSFER_MODE_AUTO;
    }
    
    /**
     * @param localfile The localfile to set.
     */
    public void setLocalfile(String localfile) {
        this.localfile = localfile;
    }
    /**
     * @param remotefile The remotefile to set.
     */
    public void setRemotefile(String remotefile) {
        this.remotefile = remotefile;
    }
    /**
     * @param failifexists The failifexists to set.
     */
    public void setFailifexists(boolean failifexists) {
        this.failifexists = failifexists;
    }
    /**
     * @param _new The _new to set.
     */
    public void setNew(String _new) {
        this._new = _new;
    }
    /**
     * @param existing The existing to set.
     */
    public void setExisting(String existing) {
        this.existing = existing;
    }
    /**
     * @param item The item to set.
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * @param result The result to set.
     */
    public void setResult(String result) {
        this.result = result;
    }
}