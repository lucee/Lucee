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

        function beforeAll() {
            variables.uri = createURI("internalRequest");
        }

        public void function testUrlStruct() localmode=true {
            result =_InternalRequest(
                template: "#variables.uri#/echo.cfm",
                url: {test=1}
            );
            expect( result.filecontent.trim() ).toBe( '{"FORM":{},"URL":{"TEST":"1"}}' );
        }

        public void function testUrlQueryString() localmode=true {
            result =_InternalRequest(
                template: "#variables.uri#/echo.cfm",
                url: "test=1"
            );
            expect( result.filecontent.trim() ).toBe( '{"FORM":{},"URL":{"TEST":"1"}}' );
        }

        public void function testUrlQueryStringAndSameAsList() localmode=true {
            result = _InternalRequest(
                template : "#variables.uri#/index.cfm",
                urls : "test=1&test=2&test=3"
            );
            expect(result.filecontent.trim()).toBe('{"test":"1,2,3"}{}')
        }

        public void function testUrlQueryStringAndSameAsArray() localmode=true {
            result = _InternalRequest (
                template : "#variables.uri#/index.cfm",
                urls : "test=1&test=2&test=3&sameURLFieldsAsArray=true"
            );
            expect(result.filecontent.trim()).toBe('{"test":["1","2","3"],"sameURLFieldsAsArray":"true"}{}')
        }

        public void function testFormStruct() localmode=true {
            result = _InternalRequest(
                template: "#variables.uri#/echo.cfm",
                form: {test=1}
            );
            var json=deserializeJSON(result.filecontent.trim());
            expect(json.form.test).toBe( "1" );
            expect(json.form.fieldnames).toBe( "TEST" );
            expect( structCount(json.url) ).toBe( 0 );
        }
        
        public void function testFormQueryString() localmode=true {
            result =_InternalRequest(
                template: "#variables.uri#/echo.cfm",
                form: "test=1"
            );
            var json=deserializeJSON(result.filecontent.trim());
            expect(json.form.test).toBe( "1" );
            expect(json.form.fieldnames).toBe( "TEST" );
            expect( structCount(json.url) ).toBe( 0 );
        }

        public void function testFormQueryStringAndSameAsList() localmode=true {
            result = _InternalRequest (
                template : "#variables.uri#\index.cfm",
                forms : "test=1&test=2&test=3"
            );
            expect(result.filecontent.trim()).toBe('{}{"test":"1,2,3","fieldnames":"test"}')
        }

        public void function testFormQueryStringAndSameAsArray() localmode=true {
            result = _InternalRequest (
                template : "#variables.uri#\index.cfm",
                forms : "test=1&test=2&test=3",
                urls : "sameFormFieldsAsArray=true"
            );

            expect(result.filecontent.trim()).toBe('{"sameFormFieldsAsArray":"true"}{"test":["1","2","3"],"fieldnames":"test"}')
        }

        // content type and content length
        public void function testContentTypeAndLength() localmode=true {
            result = _InternalRequest (
                template : "#variables.uri#\content.cfm"
            );

            expect(result["headers"]["content-type"]).toBe("application/pdf");
            expect(result["headers"]["content-length"]).toBeBetween(800,1000);
        }

        // internalRequest public function
        public void function testInternalRequestPublic() localmode=true skip=true {
            result = InternalRequest(
                template: "#variables.uri#/echo.cfm",
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
            result = _InternalRequest(
                template: "#variables.uri#/abort.cfm",
                form: {test=1}
            );
            expect( result.filecontent.trim() ).toBe( '{"FORM":{"TEST":1},"URL":{}}' );
        }

        /*
        TODO throws
        lucee.runtime.exp.Abort: Page request is aborted
        [java]    [script]         at lucee.runtime.tag.Abort.doStartTag(Abort.java:69)
        */


        public void function testContent() localmode=true skip=true {
            result = _InternalRequest(
                template: "#variables.uri#/cfcontent.cfm",
                form: {test=1}
            );
            expect( result.filecontent.trim() ).toBe( '{"FORM":{"TEST":1},"URL":{}}' );
        }

        /*
        TODO throws
        lucee.runtime.exp.Abort: Page request is aborted
        [java]    [script]         at lucee.runtime.tag.Abort.doStartTag(Abort.java:69)
        */

        public void function testContentFile() localmode=true skip=true {
            result = _InternalRequest(
                template: "#variables.uri#/cfcontent-file.cfm",
                form: {test=1}
            );
            expect( result.filecontent.trim() ).toContain( 'getCurrentTemplatePath()' );
        }

    
        private string function createURI(string calledName){
            var baseURI="/test/#listLast(getDirectoryFromPath(getCurrentTemplatePath()),"\/")#/";
            return baseURI&""&calledName;
        }
        
    } 
</cfscript>
