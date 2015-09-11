component {


	this.Name = "__LUCEE_DOC";


	function onApplicationStart() {

		Application.objects.utils = new DocUtils();
	}


	function onRequestStart( target ) {

		param name="cookie.lucee_admin_lang" default="en";
		Session.lucee_admin_lang = cookie.lucee_admin_lang;

		param name="URL.item"   default="";
		param name="URL.format" default="html";
	}

}