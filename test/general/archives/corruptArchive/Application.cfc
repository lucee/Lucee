component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/corruptArchiveLib" ] = {
		physical: variables.testDir & "corruptArchive-physical/",
		archive: variables.testDir & "corrupt.lar",
		primary: "archive"
	};

}
