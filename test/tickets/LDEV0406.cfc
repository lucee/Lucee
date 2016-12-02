component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		if( !structKeyExists(request, "WebAdminPassword") )
			request.WebAdminPassword = "password";

		if( !directoryExists( expandPath( "./LDEV0406" ) ) )
			directoryCreate(expandPath("./LDEV0406"));
		if( !directoryExists( expandPath( "./LDEV0406/api1" ) ) )
			directoryCreate(expandPath("./LDEV0406/api1"));
		if( !directoryExists( expandPath( "./LDEV0406/api2" ) ) )
			directoryCreate(expandPath("./LDEV0406/api2"));
		if( !directoryExists( expandPath( "./LDEV0406/api3" ) ) )
			directoryCreate(expandPath("./LDEV0406/api3"));
	}

	function run(){
		describe( title="Test suite for restInitApplication()", body=function(){
			it(title="Without password argument", body=function(){
				restInitApplication( dirPath=expandPath("./LDEV0406/api1/"), serviceMapping="api1" );
			});

			it(title="With password argument", body=function(){
				restInitApplication( dirPath=expandPath("./LDEV0406/api2/"), serviceMapping="api2", password=request.WebAdminPassword );
				restInitApplication( dirPath=expandPath("./LDEV0406/api3/"), serviceMapping="api3", password=request.WebAdminPassword );
			});
		});

		describe( title="Test suite for restDeleteApplication()", body=function(){
			it(title="Without password argument", body=function(){
				restDeleteApplication( dirPath=expandPath("./LDEV0406/api2/") );
			});

			it(title="With password argument", body=function(){
				restDeleteApplication( dirPath=expandPath("./LDEV0406/api3/"), password=request.WebAdminPassword );
			});
		});
	}
}