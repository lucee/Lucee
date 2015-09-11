component {


	this.name = "__LUCEE_STATIC_RESOURCE" & ( left( CGI.CF_TEMPLATE_PATH, 6 ) == "zip://" ? "_ARC" : "" );


	variables.isDebug = false;		// ATTN: set to false for production!


	function onApplicationStart() {

		Application.objects.missingTemplateHandler = new StaticResourceProvider();
	}


	function onMissingTemplate( target ) {

		if ( variables.isDebug )	onApplicationStart();		// disable cache for debug/develop

		Application.objects.missingTemplateHandler.onMissingTemplate( target );
	}

}