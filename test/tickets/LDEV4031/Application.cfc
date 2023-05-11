component {
	this.name = "LDEV4031";
	param name="FORM.Scene" default="";
	
	public void function onRequest( required string targetPage ) {
		if (FORM.Scene == 1) {
		var whatever = expandPath('anotherDirectory'); 
		}
		else if (FORM.Scene == 2) {
		var whatever = expandPath('/anotherDirectory'); 
		}
		this.mappings[ 'module' ] = getDirectoryFromPath( getCurrentTemplatePath() ) & 'moduleDir';
		include arguments.targetPage;
	}
}