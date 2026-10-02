component {
	this.name = "LDEV6469";
	this.mappings[ "/parent" ] = getDirectoryFromPath( getCurrentTemplatePath() ) & "parent";
	this.mappings[ "/child" ]  = getDirectoryFromPath( getCurrentTemplatePath() ) & "child";
}
