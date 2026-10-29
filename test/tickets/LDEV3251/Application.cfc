component {

	this.name = "LDEV3251_" & ( url.mode ?: "none" );
	this.timezone = "Europe/Zurich";

	switch ( url.mode ?: "none" ) {
		case "iso":
			this.serialization.dateFormat = "iso";
			break;
		case "isoms":
			this.serialization.dateFormat = "isoms";
			break;
		case "mask":
			this.serialization.dateFormat = "yyyy/MM/dd HH:mm";
			break;
		case "cf":
			this.serialization.dateFormat = "cf";
			break;
		case "lowercase":
			this.serialization.dateformat = "iso";
			break;
		case "invalid":
			this.serialization.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS_nope[";
			break;
	}

}
