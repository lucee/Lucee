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
component extends="org.lucee.cfml.test.LuceeTestCase" {


    public void function beforeTests(){
        variables.arrayOfSamples    = [new Jira2098.Sample(), new Jira2098.Sample()];
        variables.arrayofSubSamples    = [new Jira2098.SubSample(), new Jira2098.SubSample()];
        variables.arrayofNotSamples    = [new Jira2098.NotSample(), new Jira2098.NotSample()];
        variables.arrayOfStrings    = ["array", "of", "strings"];
        variables.arrayOfarrayOfStrings    = [arrayOfStrings];
        variables.arrayOfNumerics    = [-1, 2.2, pi()];
        variables.arrayOfStructs    = [{one="tahi"}, {two="rua"}, {three="toru"}, {four="wha"}];

    }


    public void function testAcceptArrayOfSamples(){
        acceptArrayOfSamples(arrayOfSamples);
    }

    public void function testReturnArrayOfSamples(){
        returnArrayOfSamples(arrayOfSamples);
    }

    /**
    * @mxunit:expectedexception expression
    */ 
    public void function testAcceptArrayOfSamples_withStrings(){
        acceptArrayOfSamples(arrayOfStrings);
    }

    /**
    * @mxunit:expectedexception expression
    */ 
    public void function testReturnArrayOfSamples_withStrings(){
        returnArrayOfSamples(arrayOfStrings);
    }

    public void function testAcceptArrayOfSamples_withSubSamples(){
        acceptArrayOfSamples(arrayOfSubSamples);
    }

    public void function testReturnArrayOfSamples_withSubSamples(){
        returnArrayOfSamples(arrayOfSubSamples);
    }
    
    /**
    * @mxunit:expectedexception expression
    */ 
    public void function acceptArrayOfSamples_withNotSamples(){
        acceptArrayOfSamples(arrayOfNotSamples);
    }

    /**
    * @mxunit:expectedexception expression
    */ 
    public void function testReturnArrayOfSamples_withNotSamples(){
        returnArrayOfSamples(arrayOfNotSamples);
    }

    public void function testAcceptArrayOfArrayOfStrings(){
        acceptArrayOfArrayOfStrings(arrayOfarrayOfStrings);
    }
    
    public void function testAcceptArrayOfStrings(){
        acceptArrayOfStrings(arrayOfStrings);
    }
    

    public void function testReturnArrayOfStrings(){
        returnArrayOfStrings(arrayOfStrings);
    }

    public void function testAcceptArrayOfNumerics(){
        acceptArrayOfNumerics(arrayOfNumerics);
    }

    public void function testReturnArrayOfNumerics(){
        returnArrayOfNumerics(arrayOfNumerics);
    }

    /**
    * @mxunit:expectedexception expression
    */ 
    public void function testAcceptArrayOfNumerics_withStrings(){
        acceptArrayOfNumerics(arrayOfStrings);
    }

    /**
    * @mxunit:expectedexception expression
    */ 
    public void function testReturnArrayOfNumerics_withStrings(){
        returnArrayOfNumerics(arrayOfStrings);
    }

    public void function testAcceptArrayOfStructs(){
        acceptArrayOfStructs(arrayOfStructs);
    }

    public void function testReturnArrayOfStructs(){
        returnArrayOfStructs(arrayOfStructs);
    }
    
    private any function acceptArrayOfSamples(required Sample[] samples){
    return samples;
}

private Sample[] function returnArrayOfSamples(required array samples){
    return samples;
}

private any function acceptArrayOfStrings(required string[] strings){
    return strings;
}
private any function acceptArrayOfArrayOfStrings(required string[][] strings){
    return strings;
}

private string[] function returnArrayOfStrings(required array strings){
    return strings;
}

private any function acceptArrayOfNumerics(required numeric[] numerics){
    return numerics;
}

private numeric[] function returnArrayOfNumerics(required array numerics){
    return numerics;
}

private any function acceptArrayOfStructs(required struct[] structs){
    return structs;
}

private struct[] function returnArrayOfStructs(required array structs){
    return structs;
}

}
</cfscript>