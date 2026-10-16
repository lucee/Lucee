component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/missingPhysicalLib" ] = {
		physical: variables.testDir & "does-not-exist/",
		archive: variables.testDir & "mapping.lar",
		primary: "physical"
	};

}
