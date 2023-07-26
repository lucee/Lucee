component {
	this.name="LDEV4651";
	param name="FORM.Scene";
	param name="FORM.docType" default="true";
	param name="FORM.entity" default="true";
	param name="FORM.cfapplicationOverride" default="false";
	param name="FORM.xmlParseThenSearch" default="false";
	
	switch (FORM.Scene){
		case "disallowDoctypeDecl-True":
			this.xmlFeatures = {
				"externalGeneralEntities": false,
				"secure": true,
				"disallowDoctypeDecl": true
			};
			break;
		case "disallowDoctypeDecl-False":
			this.xmlFeatures = {
				"externalGeneralEntities": false,
				"secure": false,
				"disallowDoctypeDecl": false
			};
			break;	
		case "invalidConfig-Secure":
			this.xmlFeatures = {
				"secure": "lucee"
			};
			break;
		case "invalidConfig-Doctype":
			this.xmlFeatures = {
				"disallowDoctypeDecl": "lucee"
			};
			break;
		case "invalidConfig-Entities":
			this.xmlFeatures = {
				"disallowDoctypeDecl": "lucee"
			};
			break;
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