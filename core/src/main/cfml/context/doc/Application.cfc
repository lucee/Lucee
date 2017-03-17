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


	this.name="luceedoc_#server.lucee.version#";
	this.clientmanagement="no";
	this.clientstorage="file"; 
	this.scriptprotect="all";
	this.sessionmanagement="yes";
	this.sessionStorage="memory";
	this.sessiontimeout="#createTimeSpan(0,0,30,0)#";
	this.setclientcookies="yes";
	this.setdomaincookies="no"; 
	this.applicationtimeout="#createTimeSpan(1,0,0,0)#";
	this.localmode="update";
	this.web.charset="utf-8";

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

		// getting available component packages
		request.componentDetails={};
		try{
			request.componentDetails.pack=getPackages();
		}
		catch(e) {
			request.componentDetails.pack="org.lucee.cfml";
		}

		request.componentDetails.cfcs=[];
		loop array="#request.componentDetails.pack#" item="currPack"{
			var tmpComponents=ComponentListPackage(currPack);
			for(i=1;i<=tmpComponents.len();i++){
				tmpComponents[i]=currPack&"."&tmpComponents[i];
			}
			arrayAppend(request.componentDetails.cfcs, tmpComponents, true);
		}
		arraySort(request.componentDetails.cfcs, "textnocase");
	}

	function onMissingTemplate( target ) {

		if ( variables.isDebug )	onApplicationStart();		// disable cache for debug/develop

		Application.objects.missingTemplateHandler.onMissingTemplate( target );
	}

	// Private functions
	function toPackage(required string root,required string path) localmode=true {
		path=replace(path,root,'');// TODO make this better
		path=listTrim(path,'\/');
		path=replace(path,'\','.','all');
		path=replace(path,'/','.','all');
		return path;
	}
	function getPackages() localmode=true {
		pc=getPageContext();
		config=pc.getConfig();
		mappings=config.getComponentMappings();
		sct={};
		for(m in mappings) {
			path=m.getPhysical().getAbsolutePath();
			directory
				recurse=true
				action="list"
				directory=path
				name="list";
			loop query=list {
				if(list.type=='file')
					sct[toPackage(path,list.directory)]='';
			}
		}
		arr=[];
		loop struct=sct index='k' {
			arrayAppend(arr,k);
		}
		return arr;
	}

}