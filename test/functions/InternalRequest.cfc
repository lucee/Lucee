<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
    component extends="org.lucee.cfml.test.LuceeTestCase" labels="internalRequest" {
    
        public void function testUrlStruct() localmode=true {
            uri = createURI("internalRequest/echo.cfm");
            result =_InternalRequest(
                template: uri,
                url: {test=1}
            );
            expect( result.filecontent ).toBe( '{"FORM":{},"URL":{"TEST":"1"}}' );
        }

        public void function testUrlQueryString() localmode=true {
            uri = createURI("internalRequest/echo.cfm");
            result =_InternalRequest(
                template: uri,
                url: "test=1"
            );
            expect( result.filecontent ).toBe( '{"FORM":{},"URL":{"TEST":"1"}}' );
        }

        public void function testFormStruct() localmode=true {
            uri = createURI("internalRequest/echo.cfm");
            result = _InternalRequest(
                template: uri,
                form: {test=1}
            );
            var json=deserializeJSON(result.filecontent);
            expect(json.form.test).toBe( "1" );
            expect(json.form.fieldnames).toBe( "TEST" );
            expect( structCount(json.url) ).toBe( 0 );
        }
        
        public void function testFormQueryString() localmode=true {
            uri = createURI("internalRequest/echo.cfm");
            result =_InternalRequest(
                template: uri,
                form: "test=1"
            );
            var json=deserializeJSON(result.filecontent);
            expect(json.form.test).toBe( "1" );
            expect(json.form.fieldnames).toBe( "TEST" );
            expect( structCount(json.url) ).toBe( 0 );
        }

        public void function testContentTypeAndLength() localmode=true {
            uri = createURI("InternalRequest/content.cfm");
            local.result = _InternalRequest (
					template : "#uri#"
				);

            expect(result["headers"]["content-type"]).toBe("application/pdf");
            expect(result["headers"]["content-length"]).toBe(975);
        }

        // internalRequest public function
        public void function testInternalRequestPublic() localmode=true skip=true {
            uri = createURI("internalRequest/echo.cfm");
            result = InternalRequest(
                template: uri,
                form: {test=1}
            );
            expect(result.cookies).toBeTypeOf("query");
        }

        /*
        TODO throws
        lucee.runtime.exp.Abort: Page request is aborted
        [java]    [script]         at lucee.runtime.tag.Abort.doStartTag(Abort.java:69)
        */

        public void function testAbort() localmode=true skip=true {
            uri = createURI("internalRequest/abort.cfm");
            result = _InternalRequest(
                template: uri,
                form: {test=1}
            );
            expect( result.filecontent ).toBe( '{"FORM":{"TEST":1},"URL":{}}' );
        }

        /*
        TODO throws
        lucee.runtime.exp.Abort: Page request is aborted
        [java]    [script]         at lucee.runtime.tag.Abort.doStartTag(Abort.java:69)
        */


        public void function testContent() localmode=true skip=true {
            uri = createURI("internalRequest/cfcontent.cfm");
            result = _InternalRequest(
                template: uri,
                form: {test=1}
            );
            expect( result.filecontent ).toBe( '{"FORM":{"TEST":1},"URL":{}}' );
        }

        /*
        TODO throws
        lucee.runtime.exp.Abort: Page request is aborted
        [java]    [script]         at lucee.runtime.tag.Abort.doStartTag(Abort.java:69)
        */

        public void function testContentFile() localmode=true skip=true {
            uri = createURI("internalRequest/cfcontent-file.cfm");
            result = _InternalRequest(
                template: uri,
                form: {test=1}
            );
            expect( result.filecontent ).toContain( 'getCurrentTemplatePath()' );
        }

    
        private string function createURI(string calledName){
            var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
            return baseURI&""&calledName;
        }
        
    } 
    </cfscript>