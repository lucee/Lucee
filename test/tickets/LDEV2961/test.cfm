<cfoutput>
    <cfparam name="form.scene" default="">
    <cfif form.scene eq 1>
        <cfset setArr = ["John","Jason","James"] >
        <cfset funOne( data = {"el1" = "asd","el2" = setArr} )>
        <cffunction name="funOne">
            <cfargument name="data" passby="value">
            <cfset arrayAppend(ARGUMENTS.data.el2, "TEST") />
        </cffunction>
        <cfoutput>#arraylen(setArr)#</cfoutput>
    </cfif>

    <cfif form.scene eq 2>
        <cfset testArr = ["lucee","testcase"] />
        <cfset funTwo(getArr = testArr)>
        <cffunction name="funTwo">
            <cfargument name="getArr" passby="value">
            <cfset arrayAppend(ARGUMENTS.getArr, "review") />
        </cffunction>
        <cfoutput>#arraylen(testArr)#</cfoutput>
    </cfif>
</cfoutput>