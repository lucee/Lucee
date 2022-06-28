<!--- 
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
 ---><cfscript>

component extends="org.lucee.cfml.test.LuceeTestCase" {
    // taken from expandPath()
    public void function testMapping() localmode=true {
        mappings = getApplicationSettings().mappings;
        curr = getDirectoryFromPath( getCurrentTemplatePath() );
        mappings[ "/susi"]=curr ;
        application action="update" mappings=mappings;
        try {
            expect( contractPath( expandPath( "\susi/" ) ) ).toBe( "/susi" );
            expect( contractPath( expandPath( "/susi/" ) ) ).toBe( "/susi" );
        }
        finally {
            // remove mapping /susi
            mappings = GetApplicationSettings().mappings
            structDelete( mappings, "/susi", false );
            application action="update" mappings=mappings;
        }
    }
}
</cfscript>
