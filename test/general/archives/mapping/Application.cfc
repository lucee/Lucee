component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/archiveLib" ] = {
		archive: variables.testDir & "mapping.lar",
		primary: "archive"
	};

}
