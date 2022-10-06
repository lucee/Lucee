<cffunction name="dump" output="yes" returntype="void" 
	hint="Outputs the elements, variables and values of most kinds of CFML objects. Useful for debugging. You can display the contents of simple and complex variables, objects, components, user-defined functions, and other elements."><!---
	---><cfargument 
    
    name="var" type="object" required="no" hint="Variable to display."><cfargument 
    name="expand" type="boolean" required="no" hint="expands views"><cfargument 
    name="format" type="string" required="no" hint="specify the output format of the dump, the following formats are supported:
- simple: - a simple html output (no javascript or css)
- text (default output=""console""): plain text output (no html)
- html (default output=""browser""): regular output with html/css/javascript
- classic: classic view with html/css/javascript"><cfargument 
	name="hide" type="string" required="no" hint="hide column or keys."><cfargument 
    name="keys" type="numeric" required="no" hint="For a structure, number of keys to display."><cfargument 
    name="label" type="string" required="no" hint="header for the dump output."><cfargument 
    name="metainfo" type="boolean" required="no" hint="Includes information about the query in the cfdump results."><cfargument 
    name="output" type="string" required="no" hint="Where to send the results:
- console: the result is written to the console (System.out).
- debug: the result is written to the debugging logs, when debug is enabled.
- false: output will not be written, effectively disabling the dump.
- browser (default): the result is written the the browser response stream."><cfargument 
	name="show" type="string" required="no" hint="show column or keys."><cfargument 
    name="showUDFs" type="boolean" required="no" hint="show UDFs in cfdump output."><cfargument 
    name="top" type="numeric" required="no" hint="The number of rows to display."><cfargument 
    name="abort" type="boolean" required="no" hint="stops further processing of the request."><cfargument 
    name="eval" type="string" required="no" hint="name of the variable to display, also used as label, when no label defined."><!---

    ---><cfdump attributeCollection="#arguments#" contextlevel="3"><!---
---></cffunction>