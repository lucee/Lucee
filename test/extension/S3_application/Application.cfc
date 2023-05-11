/**
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.*
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
 * 
 **/
component {

	this.name = "s3-vfs-tests";
	this.vfs.s3 = getS3vfs();

	private struct function getS3vfs(){
		var vfs = {};
		loop list="s3,s3_custom,s3_google" item="local.s3" {
			var s3Details = server.getTestService(s3);
			if ( len( s3Details ) gt 0 ){
				var st = {
					accessKeyId: s3Details.ACCESS_KEY_ID,
					secretKey: s3Details.SECRET_KEY
				};
				if ( structKeyExists( s3Details, "HOST" ) )
					st.host = s3Details.HOST
				vfs[ local.s3 ] = st;
				
			}
		}
		//systemOutput( vfs, true );
		return vfs;
	}
	
	public function onRequestStart() {
		setting requesttimeout=10;
	}

}