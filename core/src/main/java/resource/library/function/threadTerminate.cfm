<cffunction name="ThreadTerminate" output="no" returntype="void" hint="Stops processing of the thread specified in the name attribute.
If you terminate a thread, the thread scope includes an ERROR metadata structure with information about the termination. (optional, default=run)"><cfargument 
	name="name" type="string" required="yes" hint="The name of the thread to stop."><!--- 
   
    ---><cfthread action="terminate" name="#arguments.name#"/><!---
---></cffunction>