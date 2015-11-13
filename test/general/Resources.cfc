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
	    	directory directory="#sd#" action="delete" recurse="yes";
	    	file action="delete" file="#sf#";
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
		    directory directory="#sdNew#" action="delete" recurse="yes";
		    file action="delete" file="#sf#";
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
	    	directory directory="#sd#" action="delete" recurse="yes";
		    file action="delete" file="#s#";
		    file action="delete" file="#d#";
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
	    	directory directory="#sd#" action="delete" recurse="yes";
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
	    	file action="delete" file="#s#";
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
	    	file action="delete" file="#s#";
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
    assertEqual("s,ss,sss.txt",listSort(arrayToList(children),"textnoCase"));

    // filter
    filter=createObject("java","lucee.commons.io.res.filter.ExtensionResourceFilter").init("txt",false);
    children=res.list(filter);
    assertEqual("sss.txt",listSort(arrayToList(children),"textnoCase"));
}

private function testResourceIS(res) localMode=true {
    
    // must be a existing dir
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
    assertEqual("original.txt",f.getName());
    assertEqual("dir",d.getName());
    assertEqual("dir2",d2.getName());

    // parent
    assertEqual("dir",dd.getParentResource().getName());

    // getRealPath
    assertEqual(res.toString()&"/dir/test.txt",dd.toString());

}

private function testResourceReadWrite(res) localMode=true {
    f=res.getRealResource("original.txt");
    
    IOUtil=createObject("java","lucee.commons.io.IOUtil");
    
    IOUtil.write(f, "Susi Sorglos", nullValue(), false);
    res=IOUtil.toString(f,nullValue());
    assertEqual("Susi Sorglos",res);

    IOUtil.write(f, "Susi Sorglos", nullValue(), false);
    res=IOUtil.toString(f,nullValue());
    assertEqual("Susi Sorglos",res);

    IOUtil.write(f, " foehnte Ihr Haar", nullValue(), true);
    res=IOUtil.toString(f,nullValue());
    assertEqual("Susi Sorglos foehnte Ihr Haar",res);
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





	private void function test(string label,string root){
		var start=getTickCount();
		var dir=arguments.root&"testResource/";
		
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
		    testResourceProvider(dir&"testcaseRes");
		}
		finally {
			directory directory="#dir#" action="delete" recurse="yes";
		}   
		assertFalse(DirectoryExists(dir));

	}

	public void function testRam(){
		test("ram","ram://");
	}

	public void function testLocalFilesystem(){
		test("file",getDirectoryFromPath(getCurrentTemplatePath()));
	}

	public void function testZip(){
		
		var file=getDirectoryFromPath(getCurrentTemplatePath())&"zip-"&getTickCount()&".zip";
		try {
			//first we create a zip we can use then as a filesystem
			zip action="zip" file=file  {
				zipparam source=getCurrentTemplatePath();
			}

			// now we use that zip
			test("zip",file);
		}
		// now we delete that zip again
		finally {
			fileDelete(file);
		}
	}

	private void function testS3() localmode=true{

		// getting the credetials from the enviroment variables
		if(!isNull(server.system.environment.S3_ACCESS_ID) && !isNull(server.system.environment.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.environment.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.environment.S3_SECRET_KEY;
		}
		// getting the credetials from the system variables
		else if(!isNull(server.system.properties.S3_ACCESS_ID) && !isNull(server.system.properties.S3_SECRET_KEY)) {
			s3.accessKeyId=server.system.properties.S3_ACCESS_ID;
			s3.awsSecretKey=server.system.properties.S3_SECRET_KEY;
		}


		if(!isNull(s3.accessKeyId)) {
			application action="update" s3=s3; 

			test("s3","s3:///");
		}
		// TODO report somwhow that this was not executed else fail("cannot execute the testcases for s3, because there are no credetials set in the environment variables, you need to set ""S3_ACCESS_ID"" and ""S3_SECRET_KEY"" in the enviroment variables to execute this testcases ");

	}


} 



