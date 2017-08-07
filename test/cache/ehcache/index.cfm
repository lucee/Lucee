<cfsetting showdebugoutput="false"><cfscript>
// client
if(isNull(client.startTime))client.startTime=now();
if(!isNull(client.time)) client.lastTime=client.time;
client.time=now();
// session
if(isNull(session.startTime))session.startTime=now();
if(!isNull(session.time)) session.lastTime=session.time;
session.time=now();



echo(serialize({client:client,session:session}));



</cfscript>