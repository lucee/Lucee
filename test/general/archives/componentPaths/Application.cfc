component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.componentPaths = [{
		archive: variables.testDir & "components.lar",
		primary: "archive"
	}];

}
