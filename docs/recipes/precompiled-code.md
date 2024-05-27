<!--
{
  "title": "Precompiled",
  "id": "precompiled-code",
  "description": "How to pre-compile code for a production server while the source code is deployed to avoid compilation on the production server for security reasons.",
  "keywords": [
    "Precompiled",
    "Pre-compile code",
    "Production server",
    "Security",
    "CFML",
    "Class files",
    "Lucee"
  ]
}
-->
## Precompiled Code

This document explains how to pre-compile code for a production server while the source code is deployed. This method avoids compilation on the production server for security reasons. We explain this method with a simple example below:

Example:

```lucee
//index.cfm page in current instance location like \webapps\ROOT\sample\index.cfm

Time is <cfscript>
writeoutput(now());
</cfscript>
```

Run this index.cfm page in the browser.

* When a cfm or cfc file is executed for the first time (or after the file has been edited), a class file holding the java byte-code representing that CFML file is automatically created within the cfclasses folder, webroot --> WEB-INF --> lucee --> cfclasses folder, in a sub-folder representing your application's context, for example `CFC__lucee_tomcat_webapps_ROOT4900`. (Differently from Adobe ColdFusion, a separate class file is not created for each method/function within a cfc/cfm file.)

* After executing a request to our `/sample/index.cfm` example above, a class file named `index_cfm$cf.class` will be created within a `sample` folder of the cfclasses folder for our application's context.

* To demonstrate how you can deploy that compiled byte-code for a cfm as if it was the cfm itself, you can copy that class file and paste it into your original application folder (\webapps\ROOT\sample). Since you already have the original index.cfm there, rename this class file to `test.cfm`.

* Finally, run the `/sample/test.cfm` file in your browser. It should show the same results as the index.cfm file would.

### Footnotes

Here you can see the above details in a video:

[Lucee Precompiled Code](https://www.youtube.com/watch?v=Yjy3bQJgphA)