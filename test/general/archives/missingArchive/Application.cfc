component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/missingArchiveLib" ] = {
		physical: variables.testDir & "missingArchive-physical/",
		archive: variables.testDir & "does-not-exist.lar",
		primary: "archive"
	};

}
