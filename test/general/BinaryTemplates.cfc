component extends="org.lucee.cfml.test.LuceeTestCase"{
	function test() {
		try {
			var pc=getPageContext();
			var config=pc.getConfig();
			var cfclasses=config.getClassDirectory()&"";
			var content='#chr(60)#cfset ver="4.5.5">#chr(60)#cffunction name="functionA" returntype="string">#chr(60)#cfreturn "ok">#chr(60)#/cffunction>';
			
			var runName="datakjbbhvjvh";
			var dir=getDirectoryFromPath(getCurrentTemplatePath())&"BinaryTemplates"&server.separator.file;
			if(!directoryExists(dir)) directoryCreate(dir);

			var run=dir&runName&".cfm";
			var run2=dir&runName&"2.cfm";


			// create source file and call
			if(fileExists(run)) fileDelete(run);
			fileWrite(run,content);
			include "BinaryTemplates/"&runName&".cfm";
			assertEquals("ok",functionA());
			functionA="";

			var clazz=directoryList(path:cfclasses,recurse:true,filter:function(name){
				return find(runName,arguments.name);
				})[1];
			

			// create a binary file and call
			if(fileExists(run)) fileDelete(run);
			fileCopy(clazz,run);
			include "BinaryTemplates/"&runName&".cfm";
			assertEquals("ok",functionA());
			functionA="";

			// and an other name
			if(fileExists(run2)) fileDelete(run2);
			fileCopy(clazz,run2);
			include "BinaryTemplates/"&runName&"2.cfm";
			assertEquals("ok",functionA());
		}
		finally {
			if(directoryExists(dir)) directoryDelete(dir,true);
		}

	}


}
