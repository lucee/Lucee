/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
/**
* Create a new function: isInThread() to allow for checking if you are in a thread or not
*/
component extends="testbox.system.BaseSpec"{
	
/*********************************** LIFE CYCLE Methods ***********************************/

	// executes before all suites+specs in the run() method
	function beforeAll(){
	}

	// executes after all suites+specs in the run() method
	function afterAll(){
	}

/*********************************** BDD SUITES ***********************************/

	function run( testResults, testBox ){
		// all your suites go here.
		story( "Provide a way to verify if am in a cfthread or not.", function(){
			given( "I am NOT in a thread", function(){
				then( "the result should be false", function(){
					expect(	isInThread() ).toBeFalse();
				});
			});
			given( "I am in a thread", function(){
				then( "the result should be true", function(){
					callThread();
					expect(	request.data ).toBeTrue();
				});
			
			});
		});
	}

	// Workaround until compiler issue is solved
	function callThread(){
		thread name="threadTest"{
			request.data = isInThread();
		}
		thread action="join" name="threadTest";
	}
	
}