component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.customTagPaths = [{
		archive: variables.testDir & "customtags.lar",
		primary: "archive"
	}];

}
