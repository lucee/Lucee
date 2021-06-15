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

component extends="org.lucee.cfml.test.LuceeTestCase"   {
    /*try{
        dir=getDirectoryFromPath(GetBaseTemplatePath());
        dir=mid(dir,1,len(dir)-1);
    }
    // inside jsr223 getBaseTemplatePath is not supported
    catch(e){
        
    }*/
    dir=server.coldfusion.rootdir;

    parent=getDirectoryFromPath(dir);
    parent=mid(parent,1,len(parent)-1);

    SEP = Server.separator.file;

    public void function testDot(){
        assertEquals(dir,ExpandPath("."));
    }
    public void function testDotDot(){
        assertEquals(parent,ExpandPath(".."));
    }
    public void function testDotDotSlash(){
        assertEquals(parent & SEP, ExpandPath("../"));
        assertEquals("#parent##SEP#tags",ExpandPath("../tags"));
        assertEquals("#parent##SEP#tagx#SEP#",ExpandPath("../tagx/"));
    }

    public void function testSlashJM(){
        //assertEquals("#server.coldfusion.rootdir##SEP#jm",ExpandPath("/jm"));
        assertEquals("#expandPath("/")#jm",ExpandPath("/jm"));
    }
    public void function testBackSlashJM(){
        //assertEquals("#server.coldfusion.rootdir##SEP#jm",ExpandPath("\jm"));
        assertEquals("#expandPath("/")#jm",ExpandPath("\jm"));
    }

    public void function testMapping(){
        local.mappings=GetApplicationSettings().mappings;
        local.curr=getDirectoryFromPath(getCurrentTemplatePath());
        mappings["/susi"]=curr;
        application action="update" mappings=mappings;
        try {
            assertEquals(curr,ExpandPath("\susi/"));
            assertEquals(curr,ExpandPath("/susi/"));
        }
        finally {
            // remove mapping /susi
            local.mappings=GetApplicationSettings().mappings
            structDelete(local.mappings,"/susi",false);
            application action="update" mappings=mappings;
        }
    }

    public void function testSlash(){
        expandPath( '/');
    }

     
        
}
</cfscript>