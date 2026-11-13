component {
	this.name = "LDEV5937";
	this.functionPaths = [
		getDirectoryFromPath( getCurrentTemplatePath() ) & "funcsA/",
		getDirectoryFromPath( getCurrentTemplatePath() ) & "funcsB/",
		getDirectoryFromPath( getCurrentTemplatePath() ) & "doesNotExist/"
	];
}
