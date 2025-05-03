component extends="org.lucee.cfml.test.LuceeTestCase" labels="mappings" {

	function afterAll(){
		application action="update" name="LDEV4334-reset-#CreateUniqueID()#";
	}

	function run( testResults, testBox ) {
		describe("Testcase for LDEV4334 mappings, contractPath / ExpandPath", function() {

			it( title="check mappings", body=function( currentSpec ) {
				application action="update" name="LDEV4334-#CreateUniqueID()#";
				var res = mappingsTest( "LDEV4334/index.cfm", "with no mapping" );
				//debug(res);
			});

			it( title="check mappings", body=function( currentSpec ) localmode=true {
				application action="update" name="LDEV4334-#CreateUniqueID()#";
				curr = getDirectoryFromPath( getCurrentTemplatePath() );
				mappings[ "/susi"]=curr;
				application action="update" mappings=mappings;
				res = mappingsTest( "LDEV4334/index.cfm", "with single mapping" );
				//debug(res);
			});

		});
	};
	
	private function mappingsTest ( required string base, required string desc  ){
		var paths = structNew("ordered");
		paths['desc'] = arguments.desc;
		paths['base'] = arguments.base;
		paths['applicationMappings'] =			serializeJson(GetApplicationSettings().mappings);
		paths['getCurrentTemplatePath()']=					getCurrentTemplatePath();
		paths['currentFolder*getDirectoryFromPath(getCurrentTemplatePath())']=					getDirectoryFromPath(getCurrentTemplatePath() );
		paths['getContextRoot()'] =					getContextRoot();
		paths['expandPath(#paths.base#)'] =					expandPath(paths.base);
		paths['expandPath( "." )'] =				expandPath( "." );
		paths['expandPath( ".." )'] =				expandPath( ".." );
		paths['expandPath( "./" )'] =			expandPath( "./" );
		paths['expandPath( "../" )'] =		expandPath( "../" );
		paths['expandPath( "./" & #paths.base# )'] =			expandPath( "./" & paths.base );
		paths['expandPath( "/" & #paths.base# )'] =				expandPath( "/" & paths.base );
		paths['contractPath( #paths.base# )'] =					contractPath( paths.base );
		paths['contractPath( "." )'] =					contractPath( "." );
		paths['contractPath( "./" )'] =					contractPath( "./" );
		paths['contractPath( ".." )'] =					contractPath( ".." );
		paths['contractPath( "../" )'] =					contractPath( "../" );
		paths['contractPath( expandPath(#paths.base#) )'] =		contractPath( expandPath(paths.base) ) ;
		paths['contractPath( expandPath( "./" & #paths.base #) )'] =	contractPath( expandPath( "./" & paths.base ) );
		paths['contractPath( expandPath( "/" & #paths.base# ) )'] = 	contractPath( expandPath( "/" & paths.base ) );
		/*
		systemOutput("", true);
		loop collection=paths key="local.key" value="local.value"{
			systemOutput(key & chr(9) & value, true);
		}
		*/
		return paths;
	}
}