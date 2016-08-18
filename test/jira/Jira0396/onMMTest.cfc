<cfcomponent>

<cfscript>
function onMissingMethod(target,args){
	ReturnStruct = {arguments=arguments,target=target,args=args};
	return ReturnStruct;
}
</cfscript>

</cfcomponent>
	

