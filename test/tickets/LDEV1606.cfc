component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1606", body=function() {
			it( title='Checking cfzipparam tag with attribute filter',body=function( currentSpec ) {
				local.dir=GetDirectoryFromPath(getCurrentTemplatePath())&"LDEV1606/";
				local.src=local.dir&"test.zip";
				local.trg=local.dir&"tmp/";
				
				if(!directoryExists(trg)) directoryCreate(trg);
				try {
					cfzip( action="unzip", file=src, destination=trg ){
						cfzipparam (filter="*.xml");
					}
					cfdirectory(action="list", directory=trg,  name="result" recurse=true);
					
					result=queryFilter(result,function(row) {
						return row.type=="file";
					});
				}
				finally {
					if(directoryExists(trg)) directoryDelete(trg,true);
				}

				expect(result.recordcount).toBe(2);
			});

			it( title='Checking cfzip tag with attribute filter',body=function( currentSpec ) {
				local.dir=GetDirectoryFromPath(getCurrentTemplatePath())&"LDEV1606/";
				local.src=local.dir&"test.zip";
				local.trg=local.dir&"tmp/";
				
				if(!directoryExists(trg)) directoryCreate(trg);
				try {
					cfzip( action="unzip", file=src, destination=trg, filter="*.xml");
					cfdirectory(action="list", directory=trg,  name="result" recurse=true);
					
					
					result=queryFilter(result,function(row) {
						return row.type=="file";
					});
				}
				finally {
					if(directoryExists(trg)) directoryDelete(trg,true);
				}

				expect(result.recordcount).toBe(2);
			});
		


			it( title='Checking cfzip tag with attribute entrypath',body=function( currentSpec ) {
				local.dir=GetDirectoryFromPath(getCurrentTemplatePath())&"LDEV1606/";
				local.src=local.dir&"test.zip";
				local.trg=local.dir&"tmp/";
				
				if(!directoryExists(trg)) directoryCreate(trg);
				try {
					cfzip( action="unzip", file=src, destination=trg,entrypath="/test/test.xml" ){
						//cfzipparam (entrypath="/test/test.xml");
					}
					cfdirectory(action="list", directory=trg,  name="result" recurse=true);
					
					result=queryFilter(result,function(row) {
						return row.type=="file";
					});

				}
				finally {
					if(directoryExists(trg)) directoryDelete(trg,true);
				}

				expect(result.recordcount).toBe(1);
				//expect(resultXML.recordcount).toBe(1);
			});

			it( title='Checking cfzipparam tag with attribute entrypath',body=function( currentSpec ) {
				local.dir=GetDirectoryFromPath(getCurrentTemplatePath())&"LDEV1606/";
				local.src=local.dir&"test.zip";
				local.trg=local.dir&"tmp/";
				
				if(!directoryExists(trg)) directoryCreate(trg);
				try {
					cfzip( action="unzip", file=src, destination=trg){
						cfzipparam (entrypath="/test/test.xml");
						cfzipparam (entrypath="/test/Modern.cfc");
					}
					cfdirectory(action="list", directory=trg,  name="result" recurse=true);
					
					result=queryFilter(result,function(row) {
						return row.type=="file";
					});

				}
				finally {
					if(directoryExists(trg)) directoryDelete(trg,true);
				}

				expect(result.recordcount).toBe(2);
			});



		});

	}
}