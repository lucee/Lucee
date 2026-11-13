<cfscript>
	request.ldev2489 = {};
	myarray = [ ()=>1, ( x )=>x * 2 ];
	request.ldev2489.first = myarray[ 1 ]();
	request.ldev2489.second = myarray[ 2 ]( 21 );
	request.ldev2489.named = myarray[ 2 ]( x=5 );
	idx = 2;
	request.ldev2489.dynamic = myarray[ idx ]( 4 );
	request.ldev2489.member = myarray.len();
</cfscript>
