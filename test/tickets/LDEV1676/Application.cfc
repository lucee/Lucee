component {
	this.name="LDEV1676";
	param name="FORM.Scene" default="";
	
	if(FORM.Scene == 1) {
		this.xmlFeatures.externalGeneralEntities = true;
	}

	else if(FORM.Scene == 2) {
		this.xmlFeatures = {
			externalGeneralEntities: false,
			secure: true,
			disallowDoctypeDecl: false
		};
	}

	else if(FORM.Scene == 3) {
		this.xmlFeatures = {
			externalGeneralEntities: false,
			secure: true,
			disallowDoctypeDecl: true
		};
	}
}