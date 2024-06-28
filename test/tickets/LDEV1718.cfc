component extends="org.lucee.cfml.test.LuceeTestCase" {

	private function slashify(str) {
		return replace(str, "\", "/","all");
	}

	function beforeAll(){
		variables.oldMappings = GetApplicationSettings().mappings;

		// first we create a base folder
		var curr=slashify(getDirectoryFromPath(getCurrentTemplatePath()));
		var base=curr&"testLDEV1718/";
		if(directoryExists(base)) directoryDelete(base, true);
		directoryCreate(base);

		// folder for first mapping
		variables.sue=base&"sue/";
		directoryCreate(sue);
		variables.sue_sub=sue&"/sub/";
		directoryCreate(sue_sub);
		variables.sue_sub2=sue&"ellen/sub/";
		directoryCreate(sue_sub2);
		variables.sue_sub2=sue&"ellen/sub/";
		directoryCreate(sue_sub2&"subsub/");
		
		// folder for second mapping
		variables.sue_ellen=base&"sue-ellen/";
		directoryCreate(sue_ellen);
		variables.sue_ellen_sub=sue_ellen&"sub/";
		directoryCreate(sue_ellen_sub);
	}

	function afterAll(){
		application action="update" mappings=variables.oldMappings;

		// delete test folder
		var curr=getDirectoryFromPath(getCurrentTemplatePath());
		var base=curr&"testLDEV1718";
		if(directoryExists(base)) directoryDelete(base, true);
	}

	function run(){
		describe( title="Test suite for LDEV-1718", body=function(){
			it(title="checking with a single mapping, just the base line", skip=false, body=function(){ // this needs work, disabled
				
				// creating just 1 mapping
				var mappings["/sue"] = sue;
				application action="update" mappings=mappings;
				
				// this should be resolved via the /sue mapping, because it is a match with that mapping and there is no other mapping, dahh!
				var path=slashify(expandPath("/sue/ellen/sub/"));
				expect( path ).toBe( sue&"ellen/sub/" );
			});

			it(title="checking with 2 mappings with existing folders in both mappings", skip=false, body=function(){ // this needs work, disabled
				
				// creating 2 mappings, one is a subset of the other (the mapping that match with the longer virtual path always get prefered)
				var mappings["/sue"] = sue;
				var mappings["/sue/ellen"] = sue_ellen;
				application action="update" mappings=mappings;
				 
				// now the sue/ellen mapping should be used
				var path=slashify(expandPath("/sue/ellen/sub/"));
				expect( path ).toBe( sue_ellen_sub );
			});



			it(title="checking with 2 mappings with existing folder in just one mapping", skip=false, body=function(){ // this needs work, disabled
				
				// creating 2 mappings, one is a subset of the other (the mapping that match with the longer virtual path always get prefered)
				var mappings["/sue"] = sue;
				var mappings["/sue/ellen"] = sue_ellen;
				application action="update" mappings=mappings;
				 
				// now we test sub sub that only exists in the /sue mapping
				var path=slashify(expandPath("/sue/ellen/sub/subsub/"));
				
				// even that does not exist in the sue/ellen mapping, that mapping is used because of the "lucee.mapping.first" env var is true, so mapping over match
				expect( path ).toBe( sue_ellen_sub&"subsub/" );
				
				// it should not exist, becaue that only exists in the sue mapping that was not prefered
				expect( directoryExists(path) ).toBe( false );
			});
		});
	}
}