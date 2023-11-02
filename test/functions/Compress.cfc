component extends="org.lucee.cfml.test.LuceeTestCase"{
function test() {
	loop list="tgz:m,zip:m,tbz:m,tar:m,gzip:s,bzip:s" item="format2type" {
		var format=listFirst(format2type,":");
		var type=listLast(format2type,":");

		try {
			var curr=getDirectoryFromPath(getCurrentTemplatePath());
			
			// source
			var srcDir=curr&"srctmp"&format;
			var src=srcDir&"/susi.txt";
			if(directoryExists(srcDir)) directoryDelete(srcDir,true);
			directoryCreate(srcDir);
			fileWrite(src,"Susi Sorglos foehnte Ihr Haar...");

			// target
			var trgDir=curr&"trgtmp"&format;
			var trg=trgDir&"/susi."&format;
			if(directoryExists(trgDir)) directoryDelete(trgDir,true);
			directoryCreate(trgDir);
			
			compress(format:format, source:type=="m"?srcDir:src, target:trg , includeBaseFolder:true);

			//dump(label:"does the compressed file for #format# exists?",var:yesNoFormat(fileExists(trg)));
			assertTrue(fileExists(trg));

			// target 2
			var trg2Dir=curr&"trg2tmp"&format;
			var trg2=trg2Dir&"/susi."&format&".txt";
			if(directoryExists(trg2Dir)) directoryDelete(trg2Dir,true);
			directoryCreate(trg2Dir);
			
			extract(format,trg,type=="m"?trg2Dir:trg2);
			if(type=="m") assertTrue(yesNoFormat(fileExists(trg2Dir&"/srctmp"&format&"/susi.txt")));
			//dump(label:"do we have the files from extraction of #format#?",var:yesNoFormat(fileExists(trg2Dir&"/srctmp"&format&"/susi.txt")));
			else assertTrue(fileExists(trg2));
			//dump(label:"do we have the file from extraction of #format#?",var:yesNoFormat(fileExists(trg2)));

		}
		finally {
			if(directoryExists(srcDir)) directoryDelete(srcDir,true);
			if(directoryExists(trgDir)) directoryDelete(trgDir,true);
			if(directoryExists(trg2Dir)) directoryDelete(trg2Dir,true);
		}
	}
}
}
