<cfscript>
	struct function getCredentials() {
		return server.getTestService("s3");
	}


	function test() {
		var props  = getCredentials();	
		var id="b"&lcase(left(replace(createUUID(),"-","","all"),10));
		var  dir="s3://#props.ACCESS_KEY_ID#:#props.SECRET_KEY#@/"&id&"/";
		var  file=dir&"test.txt";
		var hasAllRead=false;
		try {    
			directoryCreate(dir);
			fileWrite(file, "Susi Sorglos!");
			var res=storeGetACL(file);
			if(!isNull(res)) {
				loop array=res item="local.data" {
					dump(data);
					if((data.group?:"")=="all" && (data.permission?:"")=="READ") {
						hasAllRead=true;
						break;
					}
				}
			}
		}
		finally {
			if(directoryExists(dir)) directoryDelete(dir, true);
		}
		return hasAllRead;
	}
	echo(test());
	</cfscript>