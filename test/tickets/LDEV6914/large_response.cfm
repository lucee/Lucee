<cfscript>
// Outputs a large string, closes the root writer so CFMLWriterImpl.close() runs inside this request,
// then records the state of pool.baos before PageContextImpl.release() can reset it.
// Called by LDEV6914.cfc via _internalRequest.
param name="url.size" default="200000";
param name="url.key" default="";
writeOutput( repeatString( "x", val( url.size ) ) );

pc = getPageContext();
pc.getRootOut().close();

pool = pc.getWriterPool();
server[ url.key ] = {
	isChild: pc.isChild(),
	baosIsNull: isNull( pool.baos ),
	baosSize: isNull( pool.baos ) ? 0 : pool.baos.size()
};
</cfscript>