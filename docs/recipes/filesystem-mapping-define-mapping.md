<!--
{
  "title": "Define a mapping",
  "id": "cookbook-filesystem-mapping-define-mapping",
  "related": [
    "tag-application",
    "function-expandpath",
    "cookbook-application-context-set-mapping"
  ],
  "categories": [
    "application",
    "files"
  ],
  "description": "All about the different mappings in Lucee and how to use them.",
  "keywords": [
    "Mapping",
    "Filesystem",
    "Define mapping",
    "Application.cfc",
    "Lucee archive"
  ]
}
-->
## How to define a regular Mapping

Lucee allows you to define a mapping to a specific location in a filesystem, so you don't always have to use the full path. In most cases, the full path is not working anyway, for example with [tag-include] which does not work with a full path.

This is supported with all the various [lucee-resources] supported (local, ftp, http, ram, zip, s3, etc.).

## Create a regular Mapping in the Administrator

The most common way to define a regular mapping is in the Lucee Server or Web Administrator.

The only difference between the Web and Server Administrator is that a mapping defined in the Server Administrator is visible to all web contexts, and a mapping defined in the Web Administrator is only visible to the current web context.

In your Administrator, go to the Page "Archives & Resources/Mappings" in the section "create new Mapping" that looks like this.

![create-mapping.png](https://bitbucket.org/repo/rX87Rq/images/4035761629-create-mapping.png)

With "Virtual" choose the virtual path for the mapping, this is the path you will later use to address this mapping.

"Resource" is the physical location where the mapping is pointing to, for example `C:\Projects\whatever\test`.

With "Archive" you can map a Lucee archive (more below) to the mapping.

"Primary" defines where a template is searched first. Let's say you have set primary to "archive" and you execute the following code:

```coldfusion
<cfinclude template="/myMapping/test.cfm">
```

In that case, Lucee first checks the archive associated with the "/myMapping" mapping for "test.cfm". If the template is not found there, Lucee also checks the physical location.

"Inspect templates" defines the re-check interval for the physical paths.

### Using the Mapping

Now you can use that mapping in your code:

```coldfusion
<cfinclude template="/shop/whatever.cfm"> <!--- load a template from the "shop" mapping --->
<cfset cfc = new shop.Whatever()> <!--- load a CFC from the "shop" mapping (see also "this.componentpaths" for handling components) --->
```

## Advanced

In the previous example, we simply set a path. As you can see in the Administrator, a mapping can contain more data than only a physical path. Of course, you can use these settings also with a mapping done in the [tag-application].

```cfs
// Application.cfc
component {
   this.mappings['/shop'] = {
      physical: getDirectoryFromPath(getCurrentTemplatePath()) & 'shop',
      archive: getDirectoryFromPath(getCurrentTemplatePath()) & 'shop.lar',
      primary: 'archive'
   };
}
```

In that case, we not only define a physical path but also a Lucee archive (.lar).

"primary" defines where Lucee checks first for a resource. Let's say you have the following code:

```coldfusion
<cfinclude template="/shop/whatever.cfm">
```

In that case, Lucee first checks in the archive for "whatever.cfm". If not found there, it looks inside the physical path.

### Side Note

Of course, this can be done for all mapping types:

```cfs
// Application.cfc
component {
   this.componentpaths = [{archive: getDirectoryFromPath(getCurrentTemplatePath()) & 'testbox.lar'}]; // loading testbox from an archive
   this.customtagpaths = [{archive: getDirectoryFromPath(getCurrentTemplatePath()) & 'helper.lar'}]; // a collection of helper custom tags
}
```

### See Also

- [Forcing Lucee to re-check the physical paths of application defined mappings without a restart](https://blog.simplicityweb.co.uk/123/forcing-lucee-to-re-check-the-physical-paths-of-application-defined-mappings-without-a-restart)
- [Confusion Over this.mappings And expandPath() Not Working In Lucee](https://www.bennadel.com/blog/3718-confusion-over-this-mappings-and-expandpath-not-working-in-lucee-cfml-5-3-3-62.htm)