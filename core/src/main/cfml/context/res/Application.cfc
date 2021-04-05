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

	setting showdebugOutput=false;

	this.name = "__LUCEE_STATIC_RESOURCE" & (left(CGI.CF_TEMPLATE_PATH, 6) == "zip://" ? "_ARC" : "");

	this.clientManagement  = false;
	this.sessionManagement = false;
	this.setClientCookies  = false;
	this.setDomainCookies  = false;
	this.applicationTimeout= createTimeSpan(1,0,0,0);
	this.localMode         = true;
	this.scriptProtect     = "all";
	this.web.charset       = "utf-8";

	variables.isDebug = true;		// ATTN: set to false for production!


	function onApplicationStart() {

		Application.objects.missingTemplateHandler = new StaticResourceProvider();
	}


	function onMissingTemplate(target) {
		if (variables.isDebug)
			onApplicationStart();		// disable cache for debug/develop

		Application.objects.missingTemplateHandler.onMissingTemplate(arguments.target);
	}

}