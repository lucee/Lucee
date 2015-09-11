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


	
	function getAllFunctions() {

		var result = getFunctionList().keyArray().sort( 'textnocase' ).filter( function( el ) { return left( el, 1 ) != '_'; } );

		return result;
	}


	function getAllTags() {

		var result = [];

		var itemList = getTagList();

		for ( local.ns in itemList.keyArray() ) {

			for ( local.key in itemList[ ns ].keyArray() ) {
			
				result.append( ns & key );
			}
		}
		
		result.sort( 'textnocase' );

		return result;
	}


	/**
	* returns an array of namespaces sorted by Len DESC, Text
	*/
	function getTagNamespaces() {

		var result = getTagList().keyArray();

		result.sort( function( lhs, rhs ) { 

				var ll = len( lhs );
				var lr = len( rhs );

				if ( ll != lr )
					return lr - ll;

				return compareNoCase( lhs, rhs );
			} 
		);

		return result;
	}


	function getMemberFunctions() {

		var result = {};
		var data = getMemberFunctionList();

		for ( local.obj in data.keyArray().sort( 'textnocase' ) ) {

			for ( local.method in data[ obj ].keyArray() ) {

				result[ ucFirst( obj ) & '.' & method ] = data[ obj ][ method ];
			}
		}

		return result;
	}


	/**
	* returns a struct of structs where the keys at the top level represent Object names,
	* the keys at 2nd level represent member method name, and their value shows the corresponding
	* BIF name.
	* 
	* result is cached in the this scope for faster execution in subsequent calls
	*/
	function getMemberFunctionList() {

		var result = {};
		var funcList = getFunctionList();

		if ( !isDefined( "this.data.MemberFunctionList" ) ) {

			loop collection="#funcList#" index="local.key" {
				
				local.data = getFunctionData( key );
				
				if ( isDefined( "data.member.name" ) ) 
					result[ data.member.type ][ data.member.name ] = key;
			}

			this.data.MemberFunctionList = result;
		}

		return this.data.MemberFunctionList;
	}


	function getBIFName( objectDotMethod, method="" ) {

		var memberFunctionList = getMemberFunctionList();

		var object = objectDotMethod;

		if ( !len( method ) ) {

			object = listFirst( objectDotMethod, '.' );
			method = listLast ( objectDotMethod, '.' );
		}

		if ( memberFunctionList.keyExists( object ) && memberFunctionList[ object ].keyExists( method ) )
			return memberFunctionList[ object ][ method ];

		return "";
	}


	function formatAttrDesc( desc ) {

		var NL = chr(10);

		desc = replace( trim( desc ), NL & "-", "<br><li>", "all" );
		desc = replace( desc, NL, "<br>", "all" );
	
		return desc;
	}

}