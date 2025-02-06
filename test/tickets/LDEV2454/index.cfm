<html>
	<title>LDEV-2454 this.blockedExtForFileUpload</title>
</html>
<body>
<h1>LDEV-2454 this.blockedExtForFileUpload=* should block all uploads </h1>
<cfdump var=#getApplicationSettings().blockedExtForFileUpload#>
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
	dump(form);
	flush;
	if (cgi.REQUEST_METHOD eq "POST"){
		// this should throw an error
		//content reset="true";
		finfo = FileUpload( destination=GetTempFile(GetTempDirectory(), "ldev2454") , fileField="file", nameConflict="overwrite" );
		dump(finfo);
		echo("success");
	}
</cfscript>