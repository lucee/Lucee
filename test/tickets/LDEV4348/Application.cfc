component {
	this.name="LDEV4348";
	param name="FORM.Scene";
	
	switch (FORM.Scene){
		case "all-secure":
			this.xmlFeatures = {
				"externalGeneralEntities": false,
				"secure": true,
				"disallowDoctypeDecl": true
			};
			break;
		case "all-insecure":
			this.xmlFeatures = {
				"externalGeneralEntities": true,
				"secure": false,
				"disallowDoctypeDecl": false
			};
			break;
		case "default":
			break;
		default:
			throw "unknown scene: #form.scene#";
			break;
	}
}