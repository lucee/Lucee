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

component extends="org.lucee.cfml.test.LuceeTestCase"	{


    function run(){
        describe( 'component' , function() {

            it( 'can be initiated' , function() {

                actual = new LDEV0244.good();

                    expect( actual ).toBe( 'hi' );

            });

            it( 'can be initiated even with a single line comment at the end' , function() {

                actual = new LDEV0244.bad();

                    expect( actual ).toBe( 'hi' );

            });

            it( 'can be initiated even with a single line comment at the end within a cfscript' , function() {

                actual = new LDEV0244.SingleLine();

                    expect( actual ).toBe( 'hi' );

            });

            it( 'can be initiated even with a multi line comment at the end' , function() {

                actual = new LDEV0244.MultiLine();

                    expect( actual ).toBe( 'hi' );

            });


            it( 'can be initiated even with a multi line comment at the end within a script' , function() {

                actual = new LDEV0244.MultiLineScript();

                    expect( actual ).toBe( 'hi' );

            });

        });

    }

    /*public void function testSingleLineComment(){
        new LDEV0244.SingleLine();
    }
    public void function testMultiLineComment(){
        new LDEV0244.MultiLine();
    }*/



}
</cfscript>