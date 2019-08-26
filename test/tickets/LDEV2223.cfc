component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	variables.curr=getDirectoryFromPath(getCurrentTemplatePath());
	variables.dir=curr&"LDEV2223/";
	variables.trg=dir&"res/";
	variables.file=dir&"susi.txt";
	variables.trgFile=trg&"susi.txt";
	variables.zipfile=dir&"test.zip";
	variables.password="nbkhgugu";
	variables.text="Susi Sorglos foehnte Ihr Haar!";
	
	public function setUp(){
		if(directoryExists(dir)) directoryDelete(dir,true);
		directoryCreate(dir);
		fileWrite(file,text);
	}

	public function afterTests(){
		if(directoryExists(dir)) directoryDelete(dir,true);
	}

	public function test(){
		// create a password protected zip
		zip action="zip" file=zipFile source=file password=password overwrite="true";
		
		// try to unzip with an invalid password
		var counter=0;
		loop times=5 {
			try {
				zip 
					action="unzip" 
					destination=trg 
					file=zipfile 
					password="invalid" 
					overwrite="true";
			}
			catch(e) {
				counter++;
			}
		}

		// unzip with the correct password
		assertEquals(5,counter);
		zip 
			action="unzip" 
			destination=trg 
			file=zipfile 
			password=password 
			overwrite="true";
		assertTrue(fileExists(trgFile));
		assertEquals(len(text),len(fileRead(trgFile)));
	}

} 