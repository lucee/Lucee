<!--
{
  "title": "Datasources",
  "id": "cookbook-datasource-define-datasource",
  "description": "How to define a Datasource in Lucee.",
  "keywords": [
    "Datasource",
    "Define datasource",
    "Administrator",
    "Application.cfc",
    "Default datasource",
    "MySQL"
  ]
}
-->
# How to define a Datasource

To execute queries, you need a datasource definition, which points to a specific local or remote datasource. There are different ways to do so.

## Create a Datasource in the Administrator

The most common way to define a datasource is in the Lucee Server or Web Administrator. The only difference between the Web and Server Administrator is that datasources defined in the Server Administrator are visible to all web contexts, while datasources defined in the Web Administrator are only visible to the current web context.

In your Administrator, go to the Page "Services/Datasource", in the section "create new Datasource" choose a name for your datasource and the type of your Datasource, for example "MySQL".

![create datasource](https://bitbucket.org/repo/rX87Rq/images/3802808059-createds.png)

On the following page, you can define settings to connect to your datasource. The look and feel of this page depend on the datasource type used. After saving this page, you get back to the overview page and you will get feedback if Lucee was able to connect to your datasource or not.

## Create a Datasource in the Application.cfc

You cannot only define a datasource in the Lucee Administrator, you can also do this in the [cookbook-application-context-basic]. The easiest way to do so is to create a datasource in the Administrator (see above) and then go to the detail view of this datasource by clicking the "edit button".

![select datasource](https://bitbucket.org/repo/rX87Rq/images/4142224660-select-datasource.png)

At the bottom of the detail page, you find a box that will look like this:

![datasource application definition](https://bitbucket.org/repo/rX87Rq/images/1656402808-datasource-app-def.png)

You can simply copy the code inside the box to your [cookbook-application-context-basic] body, and Lucee will pick up this definition. After that, you can delete the datasource from the Administrator.

```cfs
this.datasources["myds"] = {
    class: 'org.gjt.mm.mysql.Driver',
    connectionString: 'jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true',
    username: 'root',
    password: 'encrypted:5120611ea34c6123fd85120a0c27ab23fd81ea34cb854'
};
```

Alternatively, you can also use this pattern:

```cfs
this.datasources["myds"] = {
    type: 'mysql',
    host: 'localhost',
    database: 'test',
    port: 3306,
    username: 'root',
    password: 'encrypted:5120611ea34c6123fd85120a0c27ab23fd81ea34cb854',
    connectionLimit: -1,
    connectionTimeout: 1,
    blob: false,
    clob: false,
    storage: false,
    timezone: 'CET',
    custom: {useUnicode: true, characterEncoding: 'UTF-8'}
};
```

### Default Datasource

With the [cookbook-application-context-basic], you can also define a default datasource that is used if no "datasource" attribute is defined with the tag cfquery, cfstoredproc, cfinsert, cfupdate, etc. Simply do the following:

```cfs
this.defaultdatasource = "myds";
```

In that case, the datasource "myds" is used if there is no datasource defined. Instead of defining a datasource name, you can also define the datasource directly as follows:

```cfs
this.datasource = {
    class: 'org.gjt.mm.mysql.Driver',
    connectionString: 'jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=UTF-8&useLegacyDatetimeCode=true',
    username: 'root',
    password: 'encrypted:5120611ea34c6123fd85120a0c27ab23fd81ea34cb854'
};
```