/**
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	function testNumber() {
		assertTrue(isJson( var='{a:1}', lenient=true ));
	}
	function testBoolean() {
		assertTrue(isJson( var='{a:true}', lenient=true ));
	}
	function testNull() {
		assertTrue(isJson( var='{a:null}', lenient=true ));
	}

	function testString() {
		assertTrue(isJson( var='{a:"Susi"}', lenient=true ));
	}
	
	function testVariableGet1() {
		assertFalse(isJson( var='{a:susi}', lenient=true ));
	}
	function testVariableGet2() {
		assertTrue(isJson( var='{a:"##susi##"}', lenient=true ));
	}
	function testVariableGet3() {
		assertTrue(isJson( var='{a:"##susi##jjj"}', lenient=true ));
	}
	function testVariableGet4() {
		assertFalse(isJson( var='{a:[a,b,c]}', lenient=true ));
	}

	function testVariableSet() {
		assertFalse(isJson( var='{a:susi=1}', lenient=true ));
	}
	
	function testValueString() {
		assertTrue(isJson('"This is a \"string\"."'));
		assertEquals(deserializeJson('"This is a \"string\"."'), "This is a ""string"".");
	}

	function testValueNumber() {
		assertTrue(isJson('1234567890'));
		assertEquals(deserializeJson('1234567890'), 1234567890);
	}

	function testValueDecimal() {
		assertTrue(isJson('-123.45'));
		assertEquals(deserializeJson('-123.45'), -123.45);
	}

	function testValueScientificNotation() {
		assertTrue(isJson('10e1'));
		assertEquals(deserializeJson('10e1'), 100);
	}

	function testValueTrue() {
		assertTrue(isJson('true'));
		assertEquals(deserializeJson('true'), true);
	}

	function testValueFalse() {
		assertTrue(isJson('false'));
		assertEquals(deserializeJson('false'), false);
	}

	function testValueNull() {
		assertTrue(isJson('null'));
		assertTrue(isNull(deserializeJson('null')));
	}

	function testValueEmoji() {
		assertTrue(isJson('"#chr(128515)#"'));  //unicode smiley
		assertEquals(deserializeJson('"#chr(128515)#"'), chr(128515));
	}

	function testArray() {
		assertTrue(isJson('[1, "2", 10e1]'));
		assertEquals(deserializeJson('[1, "2", 10e1]'), [1, "2", 100]);
	}

	function testEmptyArray() {
		assertTrue(isJson('[]'));
		assertEquals(deserializeJson('[]'), []);
	}

	function testEmptyObject() {
		assertTrue(isJson('{}'));
		assertEquals(deserializeJson('{}'), {});
	}
	function testWhiteSpace() {
		// we allow white space, even it is not really correct
		assertTrue(isJson( var=' {a:1} ', lenient=true ));
		assertTrue(isJson( var='	{a:1}	', lenient=true ));
		assertTrue(isJson( var='
{a:1}
', lenient=true ));
	}

	// uses files from https://github.com/json5/json5-tests
	private function testSuite (){
		var json5TestDir = expandPath( getDirectoryFromPath( getCurrentTemplatePath() ) & "../artifacts/json5-tests" );
		var json5Tests = directoryList( path=json5TestDir, recurse=true, listinfo="path", type="file" );
		var canJson5 = ( len( getFunctionData( "isJson" ).arguments ) == 2 );

		expect( ArrayLen( json5Tests ) ).toBeGT( 0 );
		// systemOutput( "", true);
		// systemOutput( "running json5 testsuite with #ArrayLen( json5Tests )# tests", true);
		
		loop array=json5Tests item="local.test"{
			var fileType = listLast( test, "." );
			var expectedResult="";
			switch( fileType ) {
				case "json":
					expectedResult = true;
					break;
				case "json5":
					expectedResult = canJson5;
					break;
				case "txt":
					expectedResult = false;
					break;
				case "js":
					expectedResult = false;
					break;
				case "errorspec":
					expectedResult = true;
					break;
				default:
					expectedResult = false;
			}
			// due to false positives, this function is private / disabled LDEV-5017
			if ( isJson( fileRead( test ) ) neq expectedResult ) {
				// systemOutput( "expected: " & expectedResult & " isJson:" & isJson( fileRead( test ) ) & " " & test, true);
			}
			//TBD add second argument, version for json5 support
			//expect( isJson( fileRead( test ) ) ).toBe( expectedResult, test );
		}
	}
} 
