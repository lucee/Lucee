/**
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */ 
component {


	this.Name = "__LUCEE_DOCS";

	variables.isDebug = true;		// ATTN: set to false for production!


	function onApplicationStart() {

		Application.objects.utils = new DocUtils();
		Application.objects.missingTemplateHandler = new StaticResourceProvider();
	}


	function onRequestStart( target ) {

		param name="cookie.lucee_admin_lang" default="en";
		Session.lucee_admin_lang = cookie.lucee_admin_lang;

		param name="URL.item"   default="";
		param name="URL.format" default="html";
	}

	function onMissingTemplate( target ) {

		if ( variables.isDebug )	onApplicationStart();		// disable cache for debug/develop

		Application.objects.missingTemplateHandler.onMissingTemplate( target );
	}

}