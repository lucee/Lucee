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


	function init() {

		this.resources = {};

		this.mimeTypes = {

			  CSS : "text/css"
			, JS  : "text/javascript"
			, GIF : "image/gif"
			, JPG : "image/jpeg"
			, PNG : "image/png"

			, SVG : "image/svg+xml"
			, EOT : "application/vnd.ms-fontobject"
			, OTF : "application/x-font-opentype"
			, TTF : "application/x-font-ttf"
			, WOFF: "application/font-woff"
		};

		this.basePath = getDirectoryFromPath( getCurrentTemplatePath() );

		return this;
	}


	function onMissingTemplate( target ) {

		var filename = right( arguments.target, 4 ) == ".cfm" ? left( arguments.target, len( arguments.target ) - 4 ) : arguments.target;

		var resInfo = getResInfo( filename );
		
		if(!resInfo.exists) {
			// maybe the name has the version appendix
			nameAppendix=hash(server.lucee.version&server.lucee['release-date'],'quick');
			if(find("-"&nameAppendix,filename)) {
				var resInfo = getResInfo( replace(filename,"-"&nameAppendix,"") );
			}
		}

		if ( resInfo.exists ) {

			header name='Expires'       value='#getHttpTimeString( now() + 10 )#';
			header name='Cache-Control' value='max-age=#86400 * 10#';
			header name='ETag'          value=resInfo.etag;

			if ( CGI.HTTP_IF_NONE_MATCH == resInfo.etag ) {

				header statuscode='304' statustext='Not Modified';
				content reset=true type=resInfo.mimeType;
			} else {

				content reset=true type=resInfo.mimeType file=resInfo.path;
			}
		} else {
			header statuscode='404' statustext='Not Found';
		//	header statuscode='404' statustext='Not Found @ #resInfo.path#';

			systemOutput( "static resource #arguments.target# was not found @ #resInfo.path#", true, true );
		}

		return resInfo.exists;
	}


	private function getResInfo( filename ) {

		if ( structKeyExists( this.resources, arguments.filename ) )
			return this.resources[ arguments.filename ];

		var ext = listLast( arguments.filename, '.' );

		var result = { path: expandPath( arguments.filename ), exists: false, mimeType: "" };

		if ( this.mimeTypes.keyExists( ext ) )
			result.mimeType = this.mimeTypes[ ext ];

		if ( isEmpty(result.mimeType) || (find(this.basePath, result.path) != 1) )
			return result;

		if ( fileExists(result.path) )
			result.exists = true;

		if ( !result.exists )
			return result;

		result.isText = left( result.mimeType, 4 ) == "text";

		result.contents = result.isText ? fileRead( result.path ) : fileReadBinary( result.path );

		result.etag = hash( result.contents );

		this.resources[ arguments.filename ] = result;

		return result;
	}

}