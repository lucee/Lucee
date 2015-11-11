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


} 



