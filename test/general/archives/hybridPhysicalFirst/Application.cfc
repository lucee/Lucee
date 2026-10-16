component {

	variables.testDir = getTempDirectory() & "archivesTest/";

	this.mappings[ "/hybridPFLib" ] = {
		physical: variables.testDir & "hybridPF-physical/",
		archive: variables.testDir & "hybridPF.lar",
		primary: "physical"
	};

}
