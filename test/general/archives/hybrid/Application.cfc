component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/hybridLib" ] = {
		physical: variables.testDir & "hybrid-physical/",
		archive: variables.testDir & "hybrid.lar",
		primary: "archive"
	};

}
