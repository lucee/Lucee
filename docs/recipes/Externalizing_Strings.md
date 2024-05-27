<!--
{
  "title": "Externalize strings",
  "id": "Externalizing_Strings",
  "description": "Externalize strings from generated class files to separate files. This method is used to reduce the memory of the static contents for templates.",
  "keywords": [
    "Externalize strings",
    "Memory reduction",
    "Class files",
    "Static contents",
    "Lucee"
  ]
}
-->
## Externalize strings ##

Externalize strings from generated class files to separate files. This method is used to reduce the memory of the static contents for templates. We explain this method with a simple example below:

**Example:**

//index.cfm

```lucee
<cfset year = 1960>
<html>
    <body>
        <h1>....</h1>
        .......
        .......
        It was popular in the <cfoutput> #year# </cfoutput>
        .......
        <b>....</b>
    </body>
</html>
```

1. Here the Index.cfm file contains a lot of strings (static contents), but there is no functionality. The file just gives a cfoutput with year. The variable string 'year' is already declared by using in top of the Index.cfm page.

2. Execute the CFM page in a browser. A class file is created in the `webapps/ROOT/WEB-INF/lucee/cfclasses/` directory while the CFM file is executed. The run time compiler compiles that file to load the Java bytecode and execute it.

3. Right click the class file. Then see `Get info`. For example, in my class file there is 8Kb size on the disk. In Lucee, the CFM file with its strings was also loaded. So a lot of memory could be occupied just by string loading the bytecode. To avoid this problem, the Lucee admin has the following solution:

   - Lucee admin --> Language/compiler --> Externalize strings
   - This `Externalize strings` setting has four options. Select any one option to test. We selected the fourth option (externalize strings larger than 10 characters).
   - Again run the CFM page in a browser. The class file is created with lower memory size than the original 8Kb on disk.
   - In addition, it created a text file too. The text file contains the strings from the CFM page. The cfoutput with year is simply not there. The byte code will crop the piece of cfoutput content from the CFM file.

So, the string 'year' is no longer in memory. When the bytecode is called, it loads the string into memory. The memory is not occupied forever and this reduces the footprint of our application.

### Footnotes ###

Here you can see the above details in video

[Externalize strings](https://youtu.be/AUcsHkVFXHE)