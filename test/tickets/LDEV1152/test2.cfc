<cfcomponent Displayname = "test" Output="true">
    <cfscript>
    public any function writeTest(){
        var sWrite =  "
            <cfif True>
                <cfset sDateFormatStringDefault = 'dd.mm.##yyyy##'>
            </cfif>";
        myFile = fileOpen(expandPath("./result.txt"), "write");
        fileWriteLine(myFile, sWrite); 
        fileClose(myFile);
    } 


    // if uncomment these lines the following error will occur:

    // Lucee 5.1.0.34 Error (template)
    // Message No matching end tag found for tag [cfif]
    // Pattern <cfif [condition="boolean"]></cfif>
    // Documentation   Used with cfelse and cfelseif, cfif lets you create simple and compound conditional statements
    // in CFML. The value in the cfif tag can be any expression.
    // Optional:
    // * condition (boolean): condition o the expression

    // end comment

    public any function writeTest2(){
        var sWrite =  "
            <cfif True>
                <cfset sDateFormatStringDefault = 'dd.mm.##yyyy##'>
            ";
        myFile = fileOpen(expandPath("./result.txt"), "write");
        fileWriteLine(myFile, sWrite); 
        fileClose(myFile);
    } 

    </cfscript>
</cfcomponent>