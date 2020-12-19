component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	//public function beforeTests(){}
	
	//public function afterTests(){}

	//public function setUp(){}

	private void function directoryCreateDelete(string label,string dir){
		var sub=arguments.dir&"test1/";
		var subsub=sub&"test2/";
	    
		// before doing anything it should not exist
		assertFalse(directoryExists(sub));
	    assertFalse(directoryExists(subsub));
	    
	    // create the dirs
	    directory directory="#sub#" action="create";
	    directory directory="#subsub#" action="create";

	    // now it should exist
	    assertTrue(directoryExists(sub));
	    assertTrue(directoryExists(subsub));
	    
	    // delete them again
	    directory directory="#subsub#" action="delete" recurse="no";
	    directory directory="#sub#" action="delete" recurse="no";

		// should be gone again
	    assertFalse(directoryExists(sub));
	    assertFalse(directoryExists(subsub));


	    // create the dirs again
	    directory directory="#sub#" action="create";
	    directory directory="#subsub#" action="create";

	    // now it should exist
	    assertTrue(directoryExists(sub));
	    assertTrue(directoryExists(subsub));
	    
	    // delete them again
	    directory directory="#sub#" action="delete" recurse="yes";

		// should be gone again
	    assertFalse(directoryExists(sub));
	    assertFalse(directoryExists(subsub));

		// create the dirs again
	    directory directory="#sub#" action="create";
	    directory directory="#subsub#" action="create";

	    // now it should exist
	    assertTrue(directoryExists(sub));
	    assertTrue(directoryExists(subsub));

	    // this must throw an exception
	    var hasException=false;
	    try {
	    	// can not remove directory directory is not empty
	    	directory directory="#sub#" action="delete" recurse="no";
	    }
	    catch(local.e){
	    	hasException=true;
	    }
	    assertTrue(hasException);

	    directory directory="#sub#" action="delete" recurse="yes";
	    
		// should be gone again
	    assertFalse(directoryExists(sub));
	    assertFalse(directoryExists(subsub));
	}

	private void function dirList(string label,string dir){
	    var children="";
	    var sd=arguments.dir&"test1/";
	    var sf=arguments.dir&"test2.txt";
		var sdsd=sd&"test3/";
	    var sdsf=sd&"test4.txt";
	    var sdsdsf=sdsd&"test5.txt";
	    try{
		    directory directory="#dir#" action="list" name="children" recurse="no";
			assertEquals(0,children.recordcount);

			// create the data
			directory directory="#sd#" action="create";
		    directory directory="#sdsd#" action="create";
		    file action="write" file="#sf#" output="" addnewline="no" fixnewline="no";
		    file action="write" file="#sdsf#" output="" addnewline="no" fixnewline="no";
		    file action="write" file="#sdsdsf#" output="" addnewline="no" fixnewline="no";
		    
		    directory directory="#dir#" action="list" name="children" recurse="no";
		    assertEquals(2,children.recordcount);
		    assertEquals("test1,test2.txt",	listSort(valueList(children.name),'textnocase'));
		    assertEquals("0,0",				listSort(valueList(children.size),'textnocase'));
		    assertEquals("Dir,File",		listSort(valueList(children.type),'textnocase'));
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes";
		    assertEquals(5,children.recordcount);
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes" filter="*5.txt";
		    assertEquals(1,children.recordcount);
	    }
	    finally {
	    	if(directoryExists(sd))directory directory="#sd#" action="delete" recurse="yes";
	    	if(fileExists(sf))file action="delete" file="#sf#";

	    }
	}

	private void function dirRename(string label,string dir){
		var children="";
	    var sd=arguments.dir&"test1/";
	    var sdNew=arguments.dir&"test1New/";
	    var sf=arguments.dir&"test2.txt";
		var sdsd=sd&"test3/";
		var sdsdNew=sd&"test3New/";
	    var sdsf=sd&"test4.txt";
	    var sdsdsf=sdsd&"test5.txt";
	    try{
		    directory directory="#sd#" action="create";
		    directory directory="#sdsd#" action="create";
		    file action="write" file="#sf#" output="" addnewline="no" fixnewline="no";
		    file action="write" file="#sdsf#" output="" addnewline="no" fixnewline="no";
		    file action="write" file="#sdsdsf#" output="" addnewline="no" fixnewline="no";
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes";
		    assertEquals("test1,test2.txt,test3,test4.txt,test5.txt",
		  		listSort(valueList(children.name),'textnocase'));

		    directory directory="#sdsd#" action="rename" newdirectory="#sdsdNew#";
		    directory directory="#sd#" action="rename" newdirectory="#sdNew#";
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes";
		    assertEquals("test1New,test2.txt,test3New,test4.txt,test5.txt",
		  		ListSort( valueList(children.name),'text'));
	    }
	    finally {



		    if(directoryExists(sdNew))directory directory="#sdNew#" action="delete" recurse="yes";
		    if(fileExists(sf)){
		    	file action="delete" file="#sf#";
		    }
		}
	}

	private void function fileACopy(string label,string dir){
		var children="";
	    var s=arguments.dir&"copy1.txt";
	    var d=arguments.dir&"copy2.txt";
	    var sd=arguments.dir&"test1/";
	    var sdsf=sd&"test4.txt";
	    try{
		    directory directory="#sd#" action="create";
		    file action="write" file="#s#" output="aaa" addnewline="no" fixnewline="no";
		    file action="copy" source="#s#" destination="#d#";
		    file action="copy" source="#s#" destination="#sdsf#";
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes";
		    assertEquals("copy1.txt,copy2.txt,test1,test4.txt",
			  		ListSort(valueList(children.name),'text'));
	    }
	    finally {
	    	if(directoryExists(sd))directory directory="#sd#" action="delete" recurse="yes";
		    if(fileExists(s))file action="delete" file="#s#";
		    if(fileExists(d))file action="delete" file="#d#";
	    }
	}

	private void function fileAMove(string label,string dir){
	
	    var children="";
	    var s=arguments.dir&"move1.txt";
	    var d=arguments.dir&"move2.txt";
	    var sd=arguments.dir&"test1/";
	    var sdsf=sd&"test4.txt";
	    try{
		    directory directory="#sd#" action="create";
		    file action="write" file="#s#" output="" addnewline="no" fixnewline="no";
		    file action="move" source="#s#" destination="#d#";
		    file action="move" source="#d#" destination="#sdsf#";
		    
		    directory directory="#dir#" action="list" name="children" recurse="yes";
		    assertEquals("test1,test4.txt",
			  		valueList(children.name));
	    }
	    finally {
	    	if(directoryExists(sd))directory directory="#sd#" action="delete" recurse="yes";
	    }
	}

	private void function fileAReadAppend(string label,string dir){
	    var content="";
	    var s=arguments.dir&"read.txt";
	    try {
		    file action="write" file="#s#" output="Write" addnewline="no" fixnewline="no";
		    file action="append" addnewline="no" file="#s#" output="Append" fixnewline="no";
		    file action="read" file="#s#" variable="content";
		    assertEquals("WriteAppend",content);
	    }
	    finally {
	    	if(fileExists(s))file action="delete" file="#s#";
	    }
	}

	private void function fileAReadBinary(string label,string dir){
	    var content="";
	    var s=arguments.dir&"read.gif";
	    
	    try {
		    file action="write" file="#s#" output="Susi" addnewline="no" fixnewline="no";
	    	file action="readbinary" file="#s#" variable="content";
		    assertEquals("U3VzaQ==",ToBase64(content));
	    }
	    finally {
	    	if(fileExists(s))file action="delete" file="#s#";
	    }
	}



private function testResourceDirectoryCreateDelete(res) localMode=true {
    sss=res.getRealResource("s/ss");

    // must fail 
    try{
        sss.createDirectory(false); 
        fail=false;
    }
    catch(e){fail=true;}
    assertTrue(fail);

    // must work
    sss.createDirectory(true);

    assertTrue(sss.exists());
    assertTrue(sss.getParentResource().exists());

    s=sss.getParentresource();

    // must fail dir with kids
    try{
        s.remove(false); 
        fail=false;
    }
    catch(e){fail=true;}
    assertTrue(s.exists());
    assertTrue(fail);

    s.remove(true);
    assertFalse(s.exists());

    // must fail 
    try{ 
        s.remove(true);// delete
       fail=false;
    }
    catch(e){fail=true;}
    assertTrue(fail);
    
    assertFalse(sss.exists());


    d=res.getRealResource("notExist");
    try{
        d.remove(false); 
        fail=false;
    }
    catch(e){fail=true;}
    assertTrue(fail);
}

private function testResourceFileCreateDelete(res) localMode=true {
    
    sss=res.getRealResource("s/ss.txt");

    // must fail 
    try{
        sss.createFile(false); 
        fail=false;
    }
    catch(e){fail=true;}
    assertTrue(fail);

    // must work
    sss.createFile(true);
    assertTrue(sss.exists());

    s=sss.getParentresource();

    // must fail 
    try{
        s.remove(false); 
        fail=false;
    }
    catch(e){fail=true;}
    assertTrue(fail);

    s.remove(true);
    assertFalse(sss.exists());

}

private function testResourceListening(res) localMode=true {
    s=res.getRealResource("s/ss.txt");
    s.createFile(true); 
    ss=res.getRealResource("ss/");
    ss.createDirectory(true);
    sss=res.getRealResource("sss.txt");
    sss.createFile(true); 

    // all
    children=res.list();
    assertEquals("s,ss,sss.txt",listSort(arrayToList(children),"textnoCase"));

    // filter
    filter=createObject("java","lucee.commons.io.res.filter.ExtensionResourceFilter").init("txt",false);
    children=res.list(filter);
    assertEquals("sss.txt",listSort(arrayToList(children),"textnoCase"));
}

private function toResource(string path) localMode=true {
    var res=createObject('java','lucee.commons.io.res.util.ResourceUtil').toResourceNotExisting(getPageContext(), path);
    return res;
}

private function testResourceIS(res) localMode=true {
    
    // must be an existing dir
    assertTrue(res.exists());
    assertTrue(res.isDirectory());
    assertFalse(res.isFile());

    s=res.getRealResource("s/ss.txt");
    assertFalse(s.exists());
    assertFalse(s.isDirectory());
    assertFalse(s.isFile());

    s=res.getRealResource("ss/");
    assertFalse(s.exists());
    assertFalse(s.isDirectory());
    assertFalse(s.isFile());
}


private function testResourceMoveCopy(res) localMode=true {
    o=res.getRealResource("original.txt");
    o.createFile(true); 
    assertTrue(o.exists());

    // copy
    c=res.getRealResource("copy.txt");
    assertFalse(c.exists());
    o.copyTo(c,false);
    assertTrue(o.exists());
    assertTrue(c.exists());

    c=res.getRealResource("copy2.txt");
    assertFalse(c.exists());
    c.copyFrom(o,false);
    assertTrue(o.exists());
    assertTrue(c.exists());

    // move
    m=res.getRealResource("move.txt");
    assertFalse(m.exists());
    o.moveTo(m);
    assertFalse(o.exists());
    assertTrue(m.exists());

}

private function testResourceGetter(res) localMode=true {
	
    f=res.getRealResource("original.txt");
    d=res.getRealResource("dir/");
    d2=res.getRealResource("dir2")
    dd=res.getRealResource("dir/test.txt");
    
    // Name
    assertEqualPaths("original.txt",f.getName());
    assertEqualPaths("dir",d.getName());
    assertEqualPaths("dir2",d2.getName());

    // parent
    assertEqualPaths("dir",dd.getParentResource().getName());

    // getRealPath
    assertEqualPaths(res.toString()&"/dir/test.txt",dd.toString());
}

private function testResourceReadWrite(res) localMode=true {
    f=res.getRealResource("original.txt");
    
    IOUtil=createObject("java","lucee.commons.io.IOUtil");
    
    IOUtil.write(f, "Susi Sorglos", nullValue(), false);
    res=IOUtil.toString(f,nullValue());
    assertEquals("Susi Sorglos",res);

    IOUtil.write(f, "Susi Sorglos", nullValue(), false);
    res=IOUtil.toString(f,nullValue());
    assertEquals("Susi Sorglos",res);

    IOUtil.write(f, " foehnte Ihr Haar", nullValue(), true);
    res=IOUtil.toString(f,nullValue());
    assertEquals("Susi Sorglos foehnte Ihr Haar",res);
}

private function testResourceProvider(string path) localmode=true {
    // first we ceate a resource object
    res=createObject('java','lucee.commons.io.res.util.ResourceUtil').toResourceNotExisting(getPageContext(), path);

    // delete when exists
    if(res.exists()) res.remove(true);
    
    // test create/delete directory
    try {
        res.createDirectory(true); 
        testResourceDirectoryCreateDelete(res);
    }
    finally {if(res.exists()) res.remove(true);} 
    
    // test create/delete file
    try {
        res.createDirectory(true); 
        testResourceFileCreateDelete(res);
    }
    finally {if(res.exists()) res.remove(true);} 
    
    // test listening
    try {
        res.createDirectory(true); 
        testResourceListening(res);
    }
    finally {if(res.exists()) res.remove(true);} 

    // test "is"
    try {
        res.createDirectory(true); 
        testResourceIS(res);
    }
    finally {if(res.exists()) res.remove(true);} 

    // test move and copy
    try {
        res.createDirectory(true); 
        testResourceMoveCopy(res);
    }
    finally {if(res.exists()) res.remove(true);} 

    // test Getter
    try {
        res.createDirectory(true); 
        testResourceGetter(res);
    }
    finally {if(res.exists()) res.remove(true);} 

    // test read/write
    try {
        res.createDirectory(true); 
        testResourceReadWrite(res);
    }
    finally {if(res.exists()) res.remove(true);} 
   
}


/**
* compare paths ignoring the difference between forward-slash and back-slash
* for a platform-independent comparison
*/
private function assertEqualPaths(string path1, string path2) {

	assertEquals(replace(path1, "\", "/", "all"), replace(path2, "\", "/", "all"));
}



	private void function test(string label,string root){
		var start=getTickCount();
		var dir=arguments.root&"test-#createUniqueId()#-res/";
		
		// make sure there are no data from a previous run 
		if(directoryExists(dir)) {
			directory directory="#dir#" action="delete" recurse="yes";
		}
   		directory directory="#dir#" action="create";
   		try{
	   		assertTrue(DirectoryExists(dir));
			directoryCreateDelete(arguments.label,dir);
			dirList(arguments.label,dir);
			dirRename(arguments.label,dir);
		    fileACopy(arguments.label,dir);
		    fileAMove(arguments.label,dir);
		    fileAReadAppend(arguments.label,dir);
		    fileAReadBinary(arguments.label,dir);
		    testResourceProvider(dir&"testcaseres1");
		    
		}
		finally {
			if(directoryExists(dir)) directory directory="#dir#" action="delete" recurse="yes";
		}   
		assertFalse(DirectoryExists(dir));

	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var s3={};
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.environment.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.environment.S3_SECRET_KEY;
		}
		// getting the credetials from the system variables
		else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.properties.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.properties.S3_SECRET_KEY;
		}
		return s3;
	}

	

	private void function addMapping(required string virtual, required string path){
		var mappings=getApplicationSettings().mappings;
		mappings[virtual]=path;
		application 
			action="update" 
			mappings = mappings;
	}

	public void function testRam(){
		test("ram","ram://");
	}
	public void function testRamAsMapping(){
		addMapping("/testResRam","ram://");
		test("ram","/testResRam/");
	}

	public void function testLocalFilesystem(){
		test("file",getDirectoryFromPath(getCurrentTemplatePath()));
	}
	public void function testLocalFilesystemAsMapping(){

		addMapping("/testResLF",getDirectoryFromPath(getCurrentTemplatePath()));
		test("file","/testResLF/");
	}

	public void function testZip(){
		var file=getDirectoryFromPath(getCurrentTemplatePath())&"zip-"&getTickCount()&".zip";
		var zipPath="zip://"&file&"!/";
		try {
			//first we create a zip we can use then as a filesystem
			zip action="zip" file=file  {
				zipparam source=getCurrentTemplatePath();
			}
			// now we use that zip
			test("zip",zipPath);
		}
		// now we delete that zip again
		finally {
			fileDelete(file);
		}
	}

	public void function testZipAsMapping(){
		var file=getDirectoryFromPath(getCurrentTemplatePath())&"zip-"&getTickCount()&".zip";
		var zipPath="zip://"&file&"!/";
		try {
			//first we create a zip we can use then as a filesystem
			zip action="zip" file=file  {
				zipparam source=getCurrentTemplatePath();
			}
			
			addMapping("/testreszip",zipPath);
			// now we use that zip
			//throw expandPath("/testResZip/")&":"&file;
			test("zip","/testreszip/");
		}
		// now we delete that zip again
		finally {
			fileDelete(file);
		}
	}



	public void function testS3() localmode=true{
		var s3=getCredencials();
		if(!isNull(s3.accessKeyId)) {
			application action="update" s3=s3; 
			test("s3","s3:///");
		}
	}

	public void function testS3AsMapping() localmode=true{
		var s3=getCredencials();
		if(!isNull(s3.accessKeyId)) {
			application action="update" s3=s3; 
			addMapping("/testress3","s3:///");
			test("s3","/testress3/");
		}
	}
} 



