<cffunction name="threadJoin" output="no" returntype="void" 
	hint="Makes the current thread wait until the thread or threads specified in the name attribute complete processing, 
or until the period specified in the timeout argument passes, before continuing processing. If you don't specify a timeout and thread you are joining
            to doesn't finish, the current thread also cannot finish processing."><cfargument 
            name="name" type="string" required="no" hint="The name of the thread or threads to join to the current thread. To specify multiple threads, use a comma-delimited list."><cfargument 
            name="timeout" type="numeric" required="no" default="#0#" hint="The number of milliseconds that the current thread waits for the thread or threads being joined to finish. 
If any thread does not finish by the specified time, the current thread proceeds. If the attribute value is 0, the default, the current thread continues waiting until all joining threads finish. 
If the current thread is thepage thread, the page continues waiting until the threads are joined, even if you specify a page timeout. (optional, default=0)"><!---
    
    ---><cfthread action="join" attributeCollection="#arguments#"/><!---
---></cffunction>