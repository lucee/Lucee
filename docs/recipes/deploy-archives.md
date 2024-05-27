<!--
{
  "title": "Creating and deploying Lucee Archives (.lar files)",
  "id": "deploy-archives",
  "description": "This document explains how to deploy an Application on a live server without using a single CFML file.",
  "keywords": [
    "Lucee",
    "Archives",
    ".lar files",
    "Deploy",
    "Mapping",
    "Component",
    "CFC",
    "CFM"
  ]
}
-->
## Deploy Archive

This document explains how to deploy an Application on a live server without using a single CFML file.

### Using CFC file

```lucee
//placed under outside root/component/org/lucee/examples/deploy/Test.cfc
<cfscript>
component test {
    function salve() {
        return "Hi There"
    }
}
</cfscript>
```

You will need to add a mapping for the above CFC, because it's not inside the Root folder.

Create component mapping in **Archives & Resources -> Component**.

Create a mapping for test.cfc as shown below:

```
name: mycfc
resource: **Full folder path**/component/
```

After creating the mapping, you need to create an archive file for the CFC.

* Go to the detail view of mycfc mapping page,
* Click the button **assign archive to mapping**.

An archive (lar file) is created automatically and saved in `WEB-INF/lucee/context/archives`.

Now you can see the archive path on the mycfc mapping.

### Using CFM file

Create a mapping for the below CFM file:

```lucee
//placed under /ROOT/test/deploy/index.cfm
<cfscript>
test = new org.lucee.examples.deploy.Test();
dump(test.salve());
</cfscript>
```

```
name: /deploy
resource: ROOT/test/deploy/index.cfm
```

After creating the mapping in the Administrator, you can create an archive file by clicking **assign archive to mapping**.

Now you can see both lar files in the `WEB-INF/lucee/context/archives` folder:

* One is `lucee/context/archives/xxx-deploy.lar` file,
* Another one is `lucee/context/archives/xxx-mycfc.lar`

Now you can place the archive files on your target server.

Copy the archive files (deploy.lar, mycfc.lar) and place them in the target server's `/WEB-INF/lucee/deploy` folder. Wait for a minute, and it will successfully deploy your archives into the server.

You can now view mappings in the admin.

### Footnotes

Here you can see the above details in a video:

[Lucee Deploy Archive file](https://www.youtube.com/watch?time_continue=473&v=E9Z0KvspBAY)