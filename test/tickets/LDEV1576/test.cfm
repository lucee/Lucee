<cfscript>
imgRd = ImageRead('test.jpg');
local.passThumbnail=ImageNew(imgRd);
local.ppArgs.passThumbnail = local.passThumbnail.getImageBytes('png'); 

local.qInsert = queryExecute(
" insert into LDEV1576
(ID,passThumbnail)
values
(:requestID,:passThumbnail)",
{
requestID: {value: 8, CFSQLType: 'CF_SQL_INTEGER'},
passThumbnail: {value: local.ppArgs.passThumbnail, CFSQLType: 'CF_SQL_BLOB'}
},
{
result: "qResult"
}
);

local.qresult = queryExecute(
" Select * from LDEV1576",
{}
);

writeOutput(local.qresult.recordCount > 0);
</cfscript>