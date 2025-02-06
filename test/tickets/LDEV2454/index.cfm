<html>
	<title>LDEV-2454 this.blockedExtForFileUpload</title>
</html>
<body>
<h1>LDEV-2454 this.blockedExtForFileUpload=* should block all uploads </h1>
<form method="post" enctype="multipart/form-data">
	<div>
	  <label for="file">LDEV2454 Choose file to upload</label>
	  <input type="file" id="file" name="file">
	</div>
	<div>
	  <button>Submit</button>
	</div>
</form>

<cfscript>
	if (cgi.REQUEST_METHOD eq "POST"){
		// this should throw an error
		content reset="true";
		FileUpload( destination=GetTempFile(GetTempDirectory(), "ldev2454") , fileField="file", nameConflict="overwrite" );
		echo("success");
	}
</cfscript>