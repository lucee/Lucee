<!--
{
  "title": "Checksum",
  "id": "checksum",
  "description": "This document explains how to use a checksum in Lucee.",
  "keywords": [
    "Checksum",
    "File validation",
    "cfhttp",
    "hash",
    "fileReadBinary"
  ]
}
-->
This document explains how to use a checksum in Lucee.

Many servers provide a checksum for the files they provide for download. We use the checksum to validate a download file in order to avoid a corrupted file.

If you download a file in your application, you can automatically check if the download is valid or not if the necessary info was provided in the response header.

### Example 1 : ###

```luceescript
<cfscript>
_url="http://central.maven.org/maven2/org/lucee/esapi/2.1.0.1/esapi-2.1.0.1.jar";
http url=_url result="res";
if(res.status_code!=200) throw "wtf";
dump(res.responseheader);

// store the file
localFile="esapi-2.1.0.1.jar";
fileWrite(localFile,res.fileContent);

// get a hash
dump(fileInfo(localFile));
dump(hash(fileReadBinary(localFile),"md5"));
dump(hash(fileReadBinary(localFile),"SHA1"));


// validate file
if(!isEmpty(res.responseheader["X-Checksum-MD5"]?:"")) {
fi=fileInfo("esapi-2.1.0.1.jar");
if(res.responseheader["X-Checksum-MD5"]==fi.checksum) {
dump("we have a match!");
}
else {
fileDelete("esapi-2.1.0.1.jar");
dump("something went wrong! give it another try?");
}
}
</cfscript>
```

* Download the jar file by using cfhttp.
* Dump the file response header. You can see the "X-Checksum-MD5" "X-Checksum-SHA1" keys from the file itself.
* Save the file, and dump(fileInfo(localFile.checksum)). Check to see if the dump matches the value of the downloaded file response["X-Checksum-MD5"] header.

Checksum values are hashed from the binaryfile itself.

```luceescript
dump(hash(fileReadBinary(localFile),"md5"));
dump(hash(fileReadBinary(localFile),"SHA1"));
```

You can validate the checksum as shown below:

```luceescript
// validate file
if(!isEmpty(res.responseheader["X-Checksum-MD5"]?:"")) {
fi=fileInfo("esapi-2.1.0.1.jar");
if(res.responseheader["X-Checksum-MD5"]==fi.checksum) {
dump("we have a match!");
}
else {
fileDelete("esapi-2.1.0.1.jar");
dump("something went wrong! give it another try?");
}
}
```

If the checksum is provided, we can check it. However, the checksum may not always be provided. The following example shows how to provide a checksum for all downloads.

### Example 2 ###

//download.cfm

```luceescript
<cfscript>
fi=fileInfo("esapi-2.1.0.1.jar");
header name="Content-MD5" value=fi.checksum;
content file="esapi-2.1.0.1.jar" type="application/x-zip-compressed";
</cfscript>
```

This code allows a downloading application to check if the download was successful or not. Adding the file with header content "Content-MD5" is not required.

Download the file using the below example code:

```luceescript
<cfscript>
// possible MD5 headers
HEADER_NAMES.SHA1=["Content-SHA1","X-Checksum-SHA1"];
HEADER_NAMES.MD5=["Content-MD5","X-Checksum-MD5"]; // ETag
_url=getDirectoryFromPath(cgi.request_url)&"/_download.cfm";

http url=_url result="res";
if(res.status_code!=200) throw "wtf";

// store the file
fileWrite("clone.jar",res.fileContent);

// see if we have one of the MD5 headers
checksum={type:"",name:""};
loop label="outer" struct=HEADER_NAMES index="type" item="names" {
loop array=names item="name" {
if(structKeyExists(res.responseheader,name)) {
checksum.type=type;
checksum.name=name;
checksum.value=res.responseheader[name];
break outer;
}
}
}
dump(checksum);

// validate file
if(!isEmpty(checksum.name)) {
cs=hash(fileReadBinary("clone.jar"),checksum.type);
//dump(checksum);
if(checksum.value==cs) {
dump("we have a match!");
}
else {
fileDelete("clone.jar");
dump("something went wrong! give it another try?");
}
}
</cfscript>
```

The above code checks and validates the downloaded file.

### Footnotes ###

You can see the details in this video:
[Checksum](https://www.youtube.com/watch?v=Kb_zSsRDEOg)