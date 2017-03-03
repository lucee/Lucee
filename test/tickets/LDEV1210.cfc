component extends="org.lucee.cfml.test.LuceeTestCase"{
	function isNotSupported() {
		try{
			var extensionNames = "";
			if( structKeyExists(server, "lucee") && listFirst(server.lucee.version, ".") == "5" ){
				admin action="getRHExtensions" type="server" password="password" returnVariable="returnVariable"; //To get extension details in above 5.0 versions
				extensionNames = valueList(returnVariable.name);
			}else{
				admin action="getExtensions" type="server" password="password" returnVariable="returnVariable"; //To get extension details in below 5.0 versions
				extensionNames = valueList(returnVariable.label);
			}
			if(listFindNoCase(extensionNames,"Memcached driver (BETA)")){
				return false;
			}
			return true;
		}catch( any e ){
			return true;
		}
	}

	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1210", function() {
			it( title='Checking to cache string using Memcached extension', skip=isNotSupported(), body=function( currentSpec ) {
				var testString = 'This is a test string';
				cachePut('cacheTestString', testString);
				var cachedString = cacheGet('cacheTestString');
				var result = "";

				try{
					result=cachedString;
				}catch(any e){
					result=e.message;
				}

				expect(result).toBe("This is a test string");
			});

			it( title='Checking to cache query using Memcached extension', skip=isNotSupported(), body=function( currentSpec ) {
				var testQuery = queryNew("name,age","varchar,numeric",{name:["user1","user2"],age:[15,20]});
				cachePut('cacheTestQuery', testQuery);
				var cachedQuery = cacheGet('cacheTestQuery');
				var result = "";

				try{
					result=cachedQuery.name[1];
				}catch(any e){
					result=e.message;
				}

				expect(result).toBe("user1");
			});
		});
	}
}