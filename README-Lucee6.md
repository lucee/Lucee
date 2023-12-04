![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-white.png#gh-dark-mode-only)
![Lucee](https://raw.githubusercontent.com/lucee/Lucee/6.0/images/lucee-black.png#gh-light-mode-only)

Lucee 6 comes with a lot of new features and functionality that improve your interaction with Lucee both directly, through new features, and indirectly, by enhancing the existing ones. The focus, as always, is not simply to add functionality that you can achieve yourself with CFML code, but to enhance the language itself.

Stay tuned as we explore the exciting world of Lucee 6. Get ready to elevate your CFML game with the latest and greatest.


# Java

Lucee now offers an array of enhanced functionalities for a more seamless integration between Lucee and Java applications and code.

## Java Code inside CFML!

In Lucee 6 you have the flexibility to incorporate Java code directly within your CFML code opening up new possibilities for seamless integration.

#### Within a User Defined Function (UDF):
```java
int function echoInt(int i) type="java" {
	if(i==1) throw new Exception("Oopsie-daisy!!!");
	return i*2;
}
```

Simply add the attribute `[type="java"]` and you can effortlessly embed Java code within your CFML template.

#### Or in a Closure:
```java
to_string = function (String str1, String str2) type="java" {
    return new java.lang.StringBuilder(str1).append(str2).toString();
}
```

Please note that this feature isn't supported for Lambda Functions as the Lambda Syntax conflicts with the attribute "type=java".


#### Of course, these functions can also be part of a component:
```java
component { 
	int function echoInt(int i) type="java" {
		if(i==1)throw new Exception("Oopsie-daisy!!!");
		return i*2;
	}
	to_string = function (String str1, String str2) type="java" {
		return new java.lang.StringBuilder(str1).append(str2).toString();
	}
}
```
With Lucee 6, you can seamlessly blend Java and CFML to unlock new dimensions of versatility.

### Java Functions Implement Java Functional Interfaces

In Lucee 6, if the interface of a function matches one of the Java Functional Interfaces found in the [Java Standard Library](https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html), Lucee automatically implements that interface. Consequently, the function can be seamlessly passed to Java as a Java Function.

For instance, let's revisit our previous example, which implements the ["IntUnaryOperator"](https://docs.oracle.com/javase/8/docs/api/java/util/function/IntUnaryOperator.html) interface. You can verify this implementation in the function's metadata or its dump.

But why does Lucee implement these interfaces? Lucee goes the extra mile by matching the function's signature, including its arguments and return type, to any of the existing Java Functional Interfaces. And guess what? It all happens automatically!

This feature empowers you to use these functions seamlessly in your Java code, bridging the gap between CFML and Java effortlessly.

## CFML Code in Java! 
Lucee 6 takes CFML to a whole new level by allowing you to seamlessly integrate CFML components and functions with specific Java classes that implement certain interfaces or Java Functions.

### Components to Classes

When you pass a CFML component to a Java method, and that method expects a specific class, Lucee performs automatic conversion or wrapping of the component into that class. You don't need to follow any specific steps; Lucee handles it all behind the scenes.

All you have to do is create a component that implements all the methods of a Java interface as functions. For instance, consider a component that implements the [CharSequence](https://docs.oracle.com/javase/8/docs/api/java/lang/CharSequence.html) interface:
```java
// component that implements all methods from interface CharSequence
component {

   function init(String str) {
       variables.str=reverse(arguments.str);
   }
  
   function length() {
       SystemOutput("MyString.length:"&str.length(),1,1);
       return str.length();
   }


   function  charAt( index) {
       SystemOutput("MyString.charAt("&index&"):"&str.charAt( index),1,1);
       return str.charAt( index);
   }


   function subSequence(start, end) {
       SystemOutput("MyString.subSequence("&start&", "&end&"):"&str.subSequence(start, end),1,1);
       return str.subSequence(start, end);
   }


   function toString() {
       SystemOutput("MyString.toString():"&str.toString(),1,1);
       return str.toString();
   }
}
```

#### With this component in place, you can effortlessly use it as a CharSequence like this:
```java
// this class has a method that takes as an argument a CharSequence, that way we can force Lucee to convert/wrap our component to that interface.
HashUtil =  createObject("java","lucee.commons.digest.HashUtil");


// this component implements all necessary functions for the CharSequence interface
cfc=new MyString("Susi Sorglos");


// calling the method HashUtil.create64BitHashAsString(CharSequence cs) with our component as argument
hash=HashUtil.create64BitHashAsString(cfc);
dump(hash);
```
#### You can also explicitly define the interface you're implementing, as shown here:
```java
component implementsJava="java.util.List" {
	function onMissingMethod(name,args) {
		if(name=="size") return 10;
		throw "method #name# is not supported!";
	}
}
```
In this case, we explicitly implement the ["java.util.List"](https://docs.oracle.com/javase/8/docs/api/java/util/List.html) interface, allowing Lucee to handle it as an array. With this approach, you don't need to define all the methods, as Lucee doesn't enforce a strict function match due to the "implementsJava" attribute.

## UDF to Java Lambda Function

When you pass a CFML Function (UDF/Closure/Lambda) to Java like this
and the Java interface expects a Java Function (Lambda), Lucee automatically converts or wraps that function into a Java function of the corresponding type.

This seamless integration allows you to harness the full power of your CFML functions in your Java code.
In the following example, we demonstrate how Lucee implicitly implements the ["IntUnaryOperator"](https://docs.oracle.com/javase/8/docs/api/java/util/function/IntUnaryOperator.html) interface. You can then pass it to Java and use it as such, making the integration between CFML and Java even smoother.
```java
int function echoInt(int i) type="java" {
	if(i==1)throw new Exception("Oopsie-daisy!!!");
	return i*2;
}
```

# Components

In Lucee 6, we've expanded your options by introducing sub-components, a powerful addition to your CFML toolkit. Unlike traditional components defined in separate .cfc files, sub-components reside within the same template as the main component, offering you increased flexibility and improved code organization.

## Why Use Sub Components?

Sub-components bring several benefits to the table:

- **Modularization**: Sub-components enable you to break down complex components into smaller, more manageable pieces. This modular approach simplifies development, testing, and maintenance.
- **Reusability**: Components that are used across multiple templates can be encapsulated as sub-components. This reusability reduces code duplication and promotes a DRY (Don't Repeat Yourself) coding practice.
- **Scoped Variables**: Sub-components can access variables within the parent component's scope, simplifying data sharing between related components.
- **Improved Collaboration**: In team projects, sub-components can make it easier for developers to work on specific parts of a component without affecting the entire structure. This promotes collaboration and concurrent development.
- **Code Organization**: Sub-components help maintain a clean and organized codebase by keeping related functionality together within a single template.
- **Testing Isolation**: When unit testing components, sub-components can be tested in isolation, allowing for more targeted and granular testing.
- **Easier Debugging**: Smaller, focused sub-components can make debugging more straightforward since you can pinpoint issues within specific sections of your code.
- **Version Control**: Sub-components can be managed and version-controlled independently, providing more control over changes and updates.
- **Performance Optimization**: In certain cases, splitting functionality into sub-components can lead to more efficient code execution, especially when components are conditionally loaded based on specific use cases.
- **Simplified Component Management**: With sub-components, you can manage related code within a single template, making it easier to locate and edit all associated functionality.

These additional benefits highlight the versatility and advantages of using sub-components in your CFML applications.

#### Example of Sub Component Definition:
```java
component {
	function mainTest() {
		return "main";
	}
}
component name="Sub" {  
	function subTest() {
		return "sub";
	}
}
```

#### In the example above, the sub component is given a name attribute for easy referencing. You can invoke sub components as follows:
```java
// Usage example
cfc = new MyCFC();
echo("main -> " & cfc.mainTest());
echo("<br>");

cfc = new MyCFC$Sub();
echo("sub -> " & cfc.subTest());
echo("<br>");
```
Sub components expand your horizons and provide new avenues for structuring your CFML code effectively.

## Inline (Anonymous) Components
In Lucee 6, we not only introduce sub components but also unleash the power of inline components. These components are defined directly within your code, eliminating the need for extra files or templates.

### Why Use Inline Components?
Inline components bring several advantages to your CFML code:

- **Simplicity**: Inline components simplify your code by eliminating the need for separate component files. This can make your codebase more straightforward and easier to manage.
- **Local Scoping**: Inline components are ideal for situations where you need a component with a limited scope. They exist only within the context where they are defined, reducing global namespace clutter.
- **Custom Configuration**: You can configure inline components with specific properties or behaviors tailored to a particular context, providing flexibility in your code.
- **One-time Use**: Use inline components when you require a component for a single, specific task or operation, avoiding the overhead of creating separate files.
- **Encapsulation**: Inline components encapsulate related functionality, making it more self-contained and easier to understand.
- **Code Isolation**: With inline components, you can isolate specific logic or features, making it easier to maintain, update, or remove them as needed.
- **Dynamic Generation**: Inline components allow for dynamic component generation based on runtime conditions, providing flexibility in component creation.
- **Reduced Maintenance**: Since inline components are closely tied to the code where they are used, updates and changes are localized, reducing the potential impact on other parts of the application.
- **Event Handling**: Inline components are excellent for defining listeners and event handlers, enhancing the modularity of your application's event-driven architecture.
- **Improved Readability**: By embedding components directly within your code, you make your code more self-documenting, as the component's purpose and usage are evident within the context.

These additional benefits highlight how inline components can streamline your CFML code, making it more concise, efficient, and maintainable while enhancing code organization and flexibility.


#### Example of Inline Component:
```java
inline=new component {  
	function subTest() {
		return "inline<br>";
	} 
};  
dump("inline->"&inline.subTest());
dump(inline);
```

In the example above, we create an inline component that defines a subTest function. This inline component is perfect for situations where you need a component temporarily, enhancing code efficiency and maintainability.

Inline components offer a powerful way to enhance the structure and readability of your CFML code while efficiently managing local components.


# Query super power 

The humble query tag was among the pioneers in CFML, and it remains a cornerstone of the language's functionality. In Lucee 6, we've taken a deep dive into enhancing this critical component, unlocking its full potential to empower your data manipulation and retrieval tasks. Let's explore the remarkable advancements that make querying in CFML an even more formidable superpower.

## Query listeners

In Lucee 6, we introduce the concept of query listeners, a powerful tool that enables you to monitor and manipulate every query execution within your CFML application. By defining query listeners in your Application.cfc, you gain fine-grained control over the query lifecycle, from before execution to after completion.

### How to Define Query Listeners:
You can define query listeners using the following syntax in your Application.cfc:
```java
this.query.listener= {
	before: function (caller,args) {
		dump(label:"before",var:arguments);
	},
	after: function (caller,args,result,meta) {
		dump(label:"after",var:arguments);
	}
}
```

With this configuration, you have access to two listener functions:

- **Before Function**: This function is executed before the query is executed. It allows you to inspect and potentially modify query parameters or settings.
- **After Function**: After the query execution is complete, the "after" function provides access to the query result and metadata. You can use this function to perform post-processing tasks or log query results.

### Using a Query Listener Component:

Alternatively, you can encapsulate query listener functionality within a dedicated component. Here's how you can do it:
```java
this.query.listener = new QueryListener();
```

By using a dedicated component, you can organize and encapsulate your query listener logic, making it easier to manage and maintain, especially in larger applications like this:
```java
component {
	function before(caller,args) {
		args.sql="SELECT TABLE_NAME as abc FROM INFORMATION_SCHEMA.TABLES"; args.maxrows=2;
		return arguments;
	}

	function after(caller,args,result,meta) {
		var row=queryAddRow(result);
		result.setCell("abc","123",row);
		return arguments;
	}

	function error(args,caller,meta,exception) {
		//handle exception
	}
}
```

### Why Use Query Listeners?
Query listeners offer several benefits:

- **Real-time Monitoring**: You can gain insights into query execution as it happens, allowing you to detect and respond to issues promptly.
- **Dynamic Query Modification**: With the "before" function, you can dynamically modify queries based on specific conditions or requirements.
- **Logging and Debugging**: The "after" function is invaluable for logging query results or conducting further analysis.
- **Customization**: You can tailor query listeners to suit your application's unique needs, enhancing its flexibility and adaptability.

Query listeners provide a valuable toolset for optimizing query execution, troubleshooting issues, and customizing query behavior to meet your application's demands.

## Query Async: Enhancing Query Responsiveness

In Lucee 6, we introduce the powerful "async" attribute for queries, allowing you to boost the responsiveness of your applications. With "async," you can instruct Lucee not to wait for the query's execution to complete. This asynchronous approach is particularly useful when you want to monitor exceptions or retrieve the query result without causing delays in your application's flow.

### How to Use the "async" Attribute:

To leverage the "async" attribute, simply add it to your query tag, like this:
```java
query 
	datasource="mysql" 
	async=true 
	listener={
		error:function (args,caller,meta,exception){
			systemOutput(exception,true,true);
		}
     } {
     ```update user set lastAccess=now()```
}
```
In this example, we've set the "async" attribute to "true" for the query, indicating that Lucee should execute the query asynchronously.
Using a Local Listener:
If you want to capture exceptions or retrieve the query result when using "async," you can define a local listener within the query tag. The local listener is responsible for handling any exceptions that may occur during the asynchronous query execution.
Why Use "async" Queries?
Async queries offer several advantages:

- **Enhanced Responsiveness**: Async queries prevent your application from waiting for potentially time-consuming database operations to complete. This responsiveness can significantly improve user experience.
- **Exception Handling**: By utilizing a local listener, you can promptly detect and handle exceptions that occur during the query execution.
- **Non-blocking**: Asynchronous queries do not block the execution flow of your application, allowing other tasks to proceed simultaneously.
- **Parallel Processing**: You can leverage async queries to perform multiple database operations in parallel, optimizing performance.

Async queries provide a valuable tool for enhancing the responsiveness of your applications while efficiently handling database operations and exceptions. By incorporating "async" into your query strategy, you can strike a balance between speed and robust error handling.

## Query Lazy: Efficient Handling of Large Result Sets

In Lucee 6, we introduce the powerful "queryLazy" function, a game-changer when dealing with queries that return enormous result sets. Traditional queries might consume excessive memory when handling millions of records, but with "queryLazy," you can efficiently read queries in manageable blocks, ensuring that memory constraints are never a concern.

### How "queryLazy" Works:

The "queryLazy" function allows you to process queries in blocks, making it ideal for scenarios where you expect a large result set. Consider this example:
```java
queryLazy(
	sql:"select * from user",
		listener: function (rows){
			dump(rows);
		},
		options:{
			datasource:datasource,
			blockfactor:1000
		}
);
```

In this example, we retrieve records from the "user" table in blocks of a maximum of 1000 records at a time. The "listener" function is invoked for each block of records, enabling you to process them efficiently. By doing so, you prevent memory exhaustion, even when dealing with massive data sets.

### Why Use "queryLazy" Queries?

"queryLazy" offers several advantages:
- **Memory Efficiency**: By reading queries in smaller blocks, "queryLazy" ensures that memory usage remains controlled, even for queries with millions of records.
- **Improved Performance**: Processing smaller chunks of data at a time can lead to improved query performance, as the database doesn't need to fetch the entire result set at once.
- **Real-time Processing**: With the "listener" function, you can process and analyze data as it's retrieved, enabling real-time insights and actions.
- **Scalability**: "queryLazy" is scalable and well-suited for applications that need to handle substantial data volumes without sacrificing performance or memory.
- **Reduced Latency**: Smaller data blocks reduce the time it takes to retrieve and process results, minimizing latency in your application.
- **Robustness**: Handling large data sets with "queryLazy" enhances the robustness and reliability of your applications, preventing potential memory-related issues.

"queryLazy" is your go-to solution for efficiently handling large result sets, ensuring optimal memory management, and maintaining the responsiveness of your CFML applications.

## Query Indexes: Enhancing Query Data Retrieval

In Lucee 6, we introduce the capability to set an index for a query result, providing you with a powerful tool for efficient data retrieval. By defining an index for your query, you can access specific rows, row data, or individual cells with ease, streamlining your data manipulation tasks.

### How to Set an Index for a Query:

You can set an index for a query result using the "indexName" attribute in your <cfquery> tag. Consider this example:
```cfml
<cfquery name="qry" datasource="mysql" indexName="id">
  select 1 as id, 'Susi' as name
  union all
  select 2 as id, 'Peter' as name
</cfquery>
```

In this code, we've defined an index named "id" for the query result.

### Using Query Indexes:

Once you've set an index for a query, you can leverage it for various purposes:

- **Accessing Rows by Index**: Use the "QueryRowByIndex" function to retrieve a specific row by its index, like this: 
```<cfdump var="#QueryRowByIndex(qry,2)#">```
This code retrieves the second row of the query result.
- **Accessing Row Data by Index**: The "QueryRowDataByIndex" function allows you to access the data of a specific row by its index:
```<cfdump var="#QueryRowDataByIndex(qry,2)#">```
In this case, it retrieves the data for the second row.
- **Accessing Cells by Index**: To retrieve the value of a specific cell by its index, you can use the "QueryGetCellByIndex" function:
```<cfdump var="#QueryGetCellByIndex(qry,"name",2)#">```
Here, it fetches the "name" column value for the second row.

### Why Use Query Indexes?

Query indexes offer several benefits:

- **Efficient Data Retrieval**: Indexes provide a direct and efficient way to access specific rows, row data, or cell values within a query result, eliminating the need for manual iteration.
Simplified Code: By using indexes, you can write more concise and readable code for retrieving and working with query data.
- **Enhanced Performance**: Index-based retrieval minimizes the processing overhead associated with traditional query traversal, improving query performance.
- **Flexibility**: You can set and utilize multiple indexes for a single query result, tailoring your data retrieval to different requirements.

Query indexes empower you to streamline your data manipulation tasks, making it easier to access and work with specific data points within your query results. Whether you need to retrieve entire rows, row data, or individual cell values, indexes provide an efficient and convenient solution.

## Returntype for Query: Tailoring Result Formats

In Lucee 6, the cfquery tag and the functions QueryExecute and QueryLazy introduce the new attribute/option, "returntype," which allows you to define the format of the result returned by the query. This flexibility enables you to tailor the query result to suit your specific needs.
Using the "returntype" Attribute:
```cfml
<cfquery name="res" datasource="h2" returntype="array">
  select id, label from tasks
</cfquery>
```

In this example, the "returntype" is set to "array." As a result, the variable "res" does not hold a query object but an array of structs, as shown below:
```json
 [
     {"id": 10, "label": "First task"},
     {"id": 20, "label": "Second task"}
 ]
```

If you set the "returntype" to "struct," you can also define which column serves as the key for the struct using the "columnKey" attribute, like so:

```cfml
<cfquery name="res" datasource="h2" returntype="struct" columnKey="id">
  select id, label from tasks
</cfquery>
```

In this case, the ordered struct returned will look like this:
```json
  {
     "10": {"id": 10, "label": "First task"},
     "20": {"id": 20, "label": "Second task"}
  }
```

By utilizing the "returntype" attribute, you gain control over the format of your query results, allowing you to structure data in a way that best suits your application's requirements. Whether you prefer arrays of structs or ordered structs with custom keys, Lucee 6 provides the flexibility to customize query result formats as needed.

# Mail: Supercharging Your Email Handling

In Lucee 6, we've gone the extra mile to supercharge your email handling capabilities, just as we did with queries. Our enhancements in this area empower you to streamline your email communication, ensuring that sending and receiving messages is a breeze. 

## Mail Listener: Monitoring and Enhancing Email Handling

Similar to our support for query listeners, Lucee 6 introduces the capability to use listeners for handling email. These listeners allow you to closely monitor and manipulate email-related tasks, whether you're sending or receiving messages. You can define mail listeners within your Application.cfc or directly within the mail tag itself, offering flexibility and control over your email interactions.

### How to Define Mail Listeners:

Mail listeners are defined using the "this.mail.listener" structure within your Application.cfc or directly within the mail tag. Here's an example:
```java
this.mail.listener = {
	before = function (caller,nextExecution,created,detail,closed,advanced,tries,id,type,remainingtries) {
		detail.from&=".de";
		return arguments.detail;
	},
	after = function (caller,created,detail,closed,lastExecution,advanced,tries,id,type,passed,remainingtries) {
		systemOutput(arguments.keyList(),1,1);
	}
};
```

In this example, we've defined both "before" and "after" listener functions. The "before" function allows you to modify email details before execution, while the "after" function provides insights into the email's processing with access to various data points.

### Why Use Mail Listeners?
Mail listeners offer several advantages:

- **Customized Email Processing**: You can tailor email handling to your specific requirements by implementing custom logic within listeners.
- **Real-time Monitoring**: Email listeners enable real-time monitoring of email tasks, making it easy to detect and respond to issues as they arise.
- **Data Manipulation**: With listeners, you can manipulate email content, sender information, and other details before or after email execution.
- **Logging and Debugging**: Use listeners for logging email-related data, facilitating debugging and troubleshooting.
- **Enhanced Control**: Mail listeners provide fine-grained control over the email execution process, allowing you to enforce business rules or apply transformations.

Mail listeners empower you to take charge of your email processing, ensuring that it aligns perfectly with your application's needs. Whether you need to customize email content, monitor email interactions, or perform data manipulations, mail listeners offer a versatile solution for optimizing your email handling tasks.

## Mail Async

Like with query the mail tag also supports the “async” attribute, this is not new before this was simply handled by the attribute “spoolenable”.
But like with query you can now combine this as well with a local listener.

# CFTimeout: Managing Code Execution Time

In Lucee 6, the cftimeout tag empowers you to take control over the execution time of specific code blocks. It allows you to set a timeout for a code segment and define how to handle both timeout and error scenarios.

#### You can use the cftimeout tag to set a timeout for a code block like this:
```cfml
<cftimeout timespan="#createTimespan(0, 0, 0, 0,100)#" forcestop=true ontimeout="#function(timespan) {
   dump(timespan);
   }#">
   <cfset sleep(1000)>
</cftimeout>
```
In this basic example, a timeout of 0.1 seconds (100 milliseconds) is defined using the timespan attribute. The code inside the "ontimeout" function is executed when the timeout is reached. In this case, it will display the timespan.

### Additional Possibilities:

The cftimeout tag provides further possibilities for handling code execution. You can enhance your control over execution time by utilizing additional attributes like forcestop and defining both "ontimeout" and "onerror" functions.
```cfml
<cftimeout timespan="#createTimespan(0, 0, 0, 0, 100)#" forcestop="true"
  ontimeout="#function(timespan) {
     dump(timespan);
  }#">
  <cfset sleep(1000)>
</cftimeout>
```
In this enhanced example, the forcestop attribute is set to "true," which means that Lucee will forcefully stop the execution of the code block when the timeout is reached. The "ontimeout" function will be called, displaying the timespan. This additional level of control allows you to decide whether to stop or continue code execution upon reaching the timeout.

### Customized Control Over Execution Time:

The cftimeout tag provides flexibility in managing code execution, allowing you to tailor your application's behavior to meet your specific requirements. Whether it's handling timeouts or errors, "CFTimeout" gives you the tools to control how your application responds to different scenarios.

# Simplified Configuration

In Lucee 6, we've introduced a more streamlined approach to configuring and managing your Lucee instances. 

## Lucee Administrator: Simplified Control

The Lucee Administrator is a crucial part of managing your Lucee installations. It provides a user-friendly web-based interface to configure and monitor various settings, ensuring your Lucee server runs smoothly. With Lucee 6, we've enhanced the Administrator experience to offer more control and flexibility.

Lucee 6 introduces two distinct modes of operation for the Administrator, allowing you to tailor your configuration to your specific needs:

- **Single Mode**: In this mode, you'll find a simplified Lucee experience with just one Administrator to manage. This mode is ideal when you have a single web context and don't need the added complexity of multiple administrators. For new installations, Lucee 6 starts in "Single Mode" by default.
- **Multi Mode**: For more intricate scenarios, such as multi-web context environments, "Multi Mode" is still available. This mode retains the traditional "Server" and "Web" Administrators for comprehensive control.

### Switching Modes:

Additionally, with Lucee 6, you have the flexibility to switch between "Single Mode" and "Multi Mode" directly from within the Lucee Administrator. This means you can adapt your configuration as your needs evolve, ensuring that Lucee always aligns with your changing requirements.

With this seamless transition option, Lucee 6 empowers you to maintain full control over your server's configuration, regardless of whether you start in "Single Mode" or "Multi Mode." We're committed to making your Lucee experience as adaptable and efficient as possible.

In Lucee 6, we've introduced a more streamlined approach to managing your Lucee instances. While Lucee has traditionally supported both the Lucee Web Administrator and the Lucee Server Administrator, we understand that not all situations require this level of complexity.
In many cases, especially when running a single web context in Lucee, managing both administrators can become an unnecessary overhead. With Lucee 6, we've listened to your feedback and introduced a new way to operate: "Single Mode."

## Single Mode vs. Multi Mode:

In "Single Mode," you'll find a simplified Lucee experience with just one Administrator to manage. This mode is ideal when you have a single web context and don't need the added complexity of multiple administrators.
However, we understand that some scenarios demand more fine-grained control. If you're running a multi-web context environment and want to limit access from one web context to another, "Multi Mode" is still available. This mode retains the traditional "Server" and "Web" Administrators for comprehensive control.
With Lucee 6, we've put the power in your hands, allowing you to choose the administration mode that best suits your needs, whether it's the simplicity of "Single Mode" or the versatility of "Multi Mode." We believe in providing you with the flexibility to streamline your workflow and make your Lucee experience even more efficient.

# Lucee Admininstrator

So far Lucee did support 2 kinds of Administrators, the Lucee Web Administrator and the Lucee Server Administrator. This is a big benefit when you have a multi web context environment and you wanna limit the access from one web context to the other.
But like with every feature, when you don’t need it, for example when you run just one web context in Lucee, this is an unnecessary overhead and a hassle.
Lucee 6 allows you to run Lucee in “Single Mode”, what means you only have one Administrator or in “Multi Mode” to have the old behavior with “Server” and “Web” Administrator.

When you install a new Lucee 6 version, it will be in single mode by default, when you update an existing Lucee 5 version to Lucee 6, it will be in multi mode. So we are backward compatible for updates only.
But you can easily switch between multi and single in the Lucee Administrator on the overview page.

## Lucee Configuration

So far Lucee did use XML to store it’s configuration, with Lucee 6 we move away from that and use cfconfig (json) instead, a configuration standard introduces by [Ortus Solutions](https://www.ortussolutions.com/). 
With the move to that new format, we did not only add support for all possible settings you could do in the XML, we added new functionality.

### Extensions: Enhanced Functionality

One significant improvement in Lucee 6 is the handling of extensions. Previously, extensions in the XML configuration merely reflected what was already installed on the server. However, with the new version, you can now define extensions, and Lucee will take care of the installation process if they are not already present. Extensions can be configured in three distinct ways, providing you with greater flexibility:

#### Local File Source:
```json
{
	"source":"/Users/susi/Downloads/com.teradata.jdbc-16.20.0.12.lex",
	"name":"Teradata"
}
```

In this example, the extension is defined with a source attribute pointing to a local .lex file, this can be any supported virtual file system like http, S3, ftp, ... .

#### URL Endpoint Source:
```json
{
	"source":"https://ext.lucee.org/org.postgresql.jdbc-42.2.20.lex",
	"name":"PostgreSQL"
}
```
Alternatively, you can specify the source as an URL endpoint, making it more accessible for remote installations. Lucee supports various virtual filesystems (such as S3, FTP, HTTPS, HTTP, GitHub, and more) as endpoints for these extensions. However, please note that you can only use an endpoint if the corresponding virtual filesystem is installed. For instance, you can't point to an S3 endpoint if the S3 extension is not installed.

#### Extension ID:
```json
{
	"id":"0F5DEC68-DB34-42BB-A1C1B609175D7C57"
	,"version":"7.1.3"
	,"name":"Exasol"
}
```

The third method allows you to define extensions by specifying their unique identifier (id). If the extension is not available locally, Lucee will automatically download it from the Lucee extension provider. This feature simplifies the process and ensures that your extensions are always up to date and readily accessible.

### Seamless Installation:

Extensions are installed before the rest of the configuration is loaded. This enables you to, for example, install the S3 extension and immediately utilize it for a mapping that points to your S3 storage, simplifying the setup process and ensuring smooth integration of essential features.

With these enhancements in Lucee 6, we aim to make your configuration experience more versatile, efficient, and user-friendly, ultimately providing you with a modern and powerful toolset to tailor Lucee to your exact requirements.

## ConfigImport

### Introducing configImport in Lucee 6

With Lucee 6, we've added the configImport function. This practical tool simplifies the process of updating your current configuration. Here's a quick example to show you how it works:
```java
configImport({
    "fileSystem": {
			"functionAdditionalDirectory": "{web-root-directory}/addfunction/"
    	}
  	}, 
  	"web", 
  	"mypassword"
);
```
### How Does It Work?

- **Dual Input Options**: The first argument of configImport can be a structure containing the data you wish to update, or it can be a path pointing to a cfconfig file. This flexibility allows you to choose the most convenient method for your needs.
- **Security Measures**: For security, updating configurations requires the Lucee Administrator password. This ensures that your configurations remain secure and are only modified with proper authorization.
- **Password Initialization**: If you haven't set an administrator password yet, the password provided in this function will be established as your new administrator password. This feature is particularly useful during initial setups or when configuring Server.cfc or Web.cfc on startup.

Use configImport to streamline your configuration updates in Lucee 6.

# There is More

Next to the bigger changes, Lucee comes with a lot of "smaller" changes, but these can have big benefits for your daily life. Let's delve into some of these enhancements:

## CFS Templates

### Simplifying Script Code with .cfs Files in Lucee 6

Lucee 6 introduces the convenience of using .cfs files. These files are designed to streamline your scripting experience by allowing script code to be written directly, without the need for enclosing it in ```<cfscript>``` tags.

#### Key Features:

- **Direct Scripting**: Write your ColdFusion script code directly in .cfs files. This eliminates the extra step of wrapping your code in ```<cfscript>``` tags, making your code cleaner and more straightforward.
- **Seamless Integration**: .cfs files work seamlessly within the Lucee environment, allowing for a more efficient coding process.

This new feature is all about making your development process more efficient and less cluttered. Give .cfs files a try and experience a smoother scripting workflow in Lucee 6.

## Enhanced Number Handling

In Lucee 6, we've made a significant change in how numbers are handled internally. Moving away from the previous standard of using “double”, Lucee 6 now employs “BigDecimal” for number processing.

#### Key Improvements:

- **Increased Precision**: BigDecimal offers greater precision in mathematical operations compared to double. This enhancement ensures that your calculations are more accurate and reliable.
- **Simplified Coding**: With this transition to BigDecimal, the need for using the PrecisionEvaluate function is eliminated. You can now perform precise mathematical operations directly, without additional function calls.

This upgrade to BigDecimal in Lucee 6 marks a leap forward in offering more precise and efficient number handling for your applications.

## Connection Pooling

In Lucee 6, there's a significant change in how datasource connections are managed. Moving away from the custom-made connection pool that was previously used, Lucee now integrates Apache Commons Pool2 for handling datasource connections.

### Advantages of This Shift:

- **Enhanced Control**: Apache Commons Pool2 provides a more robust framework for managing connection pools, offering greater control over how datasource connections are handled.
- **Future-Ready**: This transition opens up new possibilities for future enhancements in managing datasource connections. With Apache Commons Pool2, Lucee is better positioned to incorporate advanced features and improvements in connection management.

By adopting Apache Commons Pool2, Lucee 6 sets the stage for more efficient and effective connection pooling, aligning with modern standards and practices.

## New and Extended Functions/Tags 

Lucee 6 brings a host of new and enhanced functions and tags, broadening the capabilities and efficiency of your coding experience. Here’s a snapshot of what’s new:

- **General Enhancements**: ```<cfsetting>``` tag gets additional attributes for more control.
Array Functions: Introducing ArrayRemoveDuplicates, ArraySplice to manipulate arrays more effectively.
- **File and Query Handling**: Functions like IsFileObject, DirectoryInfo, QueryAppend, QueryClear, QueryInsertAt, QueryIsEmpty, QueryPrepend, QueryRenameColumn, QueryReverse, QueryRowSwap offer more nuanced file and query operations.
- **String Manipulation**: New functions such as StringEach, StringEvery, StringFilter, StringMap, StringReduce, StringSome, StringSort for advanced string processing.
- **Markdown to HTML**: Convert Markdown to HTML easily with MarkdownToHTML.
- **System Insights**: ExtensionInfo for detailed information about Lucee extensions.
- **Internal Requests and Searches**: Use InternalRequest, findLast, findLastNoCase, and more for internal process management and string searching.

This is just a glimpse of the extensive list of new functionalities in Lucee 6, designed to make your development process more efficient and versatile.

## Shrink Down Core Size

In Lucee 6, we've taken significant steps to optimize and streamline the core by moving more core functionalities to extensions. Key features like Axis, ESAPI, and Image processing have been transitioned into extension modules.

### Benefits of This Approach:

- **On-Demand Loading**: Libraries from these extensions are now loaded only when needed. This on-demand approach reduces the initial load on the system, enhancing performance and efficiency.
- **Lighter Core**: By moving these functionalities out of the core, the overall size of the Lucee core is reduced. This makes for a leaner, more agile application core, which is easier to maintain and quicker to start.
- **Customizable Setup**: With core functionalities as extensions, you can tailor your Lucee setup more precisely, adding only the extensions you need for your specific use case.

This strategic reorganization of core functionalities into extensions marks a significant step towards a more modular, efficient, and performance-oriented Lucee 6.

## Removed XML Transformers/Parser

### Streamlining XML Handling in Lucee 6

In Lucee 6, we've made a strategic decision regarding the handling of XML data. We've removed the external XML transformers and parsers that were previously part of Lucee and have shifted to utilizing the capabilities inherent in the Java Virtual Machine (JVM).

### Key Changes and Benefits:

- **Leveraging JVM Capabilities**: By relying on the XML processing functionalities available within the JVM, Lucee 6 simplifies its architecture. This change ensures more native and efficient XML handling, tapping into the JVM's robust and optimized XML processing features.
- **Reduced Overhead**: This shift away from external XML tools reduces the overhead associated with maintaining separate XML processors. It streamlines Lucee's functionality and potentially enhances performance and stability.
- **Simplified Maintenance**: With XML processing now directly managed by the JVM, developers can expect a more straightforward approach to XML handling in Lucee 6, aligning with standard Java practices.

This update in Lucee 6 reflects our ongoing commitment to optimizing and simplifying the platform, ensuring that it remains as efficient and developer-friendly as possible.

## Global Proxy

Lucee 6 introduces the capability to define a global proxy. This feature allows you to specify proxy settings either through the Lucee Administrator or directly within the Application.cfc. The global proxy settings ensure that all HTTP requests from Lucee go through the specified proxy server.
```java
this.proxy = {
	server: "myproxy.com",
	port: 1234,
	username: "susi",
	password: "sorglos",
		excludes: ["lucee.org"],
		includes: ["whatever.com"]
};
```

### Key Features:

- **Flexibility**: You can easily define the proxy server, port, and authentication details.
- **Selective Routing**: The excludes and includes arrays allow you to specify which domains should bypass or use the proxy, giving you fine-grained control over your network traffic.
- **Centralized Management**: Setting the proxy at the application level (or via the Lucee Administrator) offers a centralized approach to manage how external resources are accessed by your applications.

This feature is particularly useful for scenarios where internet access is restricted or monitored through a proxy, ensuring seamless integration of Lucee applications in such environments.

# Extensions

Next to the Lucee core we also have a lot of changes made to Lucee extensions.

## Tasks

Lucee 6 brings a significant enhancement with its new event-driven Task engine extension. This extension is designed to execute tasks in an innovative yet familiar way, offering flexibility and efficiency in task management.

### Key Aspects of the New Task Engine:
- **Versatility**: The new Task engine in Lucee 6 allows for task execution that is similar to the conventional scheduled tasks, providing a familiar ground for those accustomed to the previous system.
- **Ease of Transition**: If you're already using scheduled tasks, transitioning to the new event-driven Task engine is straightforward. This feature ensures a seamless shift, minimizing the learning curve and adaptation time.
- **Event-Driven Approach**: With this update, tasks in Lucee can be triggered by specific events, offering more dynamic and responsive task management. This approach allows for more precise control over when and how tasks are executed.

The introduction of the event-driven Task engine in Lucee 6 represents a step forward in simplifying and enhancing the task execution process, aligning with modern development practices.

## Image Extension

Lucee 6 expands the functionality of its Image extension to support a wider range of image formats and includes built-in support for JDeli, a commercial library known for its advanced imaging capabilities.

### What's New?

- **Broader Format Compatibility**: The Image extension now supports additional formats such as WEBP, HEIC, HEIF, PSD, and more. This expansion allows for more flexibility in handling various image types within your applications.
- **JDeli Integration**: Built-in support for JDeli enhances the Image extension's capabilities. JDeli is a high-performance, commercial library that offers advanced image processing features. This integration provides superior image handling, especially for formats that are not natively supported by the JVM.

The enhanced Image extension in Lucee 6, with its wider format support and JDeli integration, significantly improves image processing capabilities, making it easier to work with a variety of image formats in your applications.

## Redis Cache Extension

The Redis Cache Extension in Lucee 6 has undergone significant improvements. These enhancements not only involve a shift to a new library but also the introduction of a feature for direct, native access.

### Key Updates:

- **New Library Integration**: The extension now utilizes a different library for Redis Cache. This transition aims to bolster the extension's efficiency and capability, ensuring more robust and feature-rich Redis integration for Lucee users.
- **Direct Access with RedisCommand**: A major feature is the addition of the RedisCommand function. This function allows Lucee developers to execute Redis commands natively, offering an unprecedented level of control and flexibility in managing cache operations.

These improvements in the Redis Cache Extension for Lucee 6 are designed to enhance the overall performance and functionality of Redis caching, making it more adaptable to the diverse needs of web applications.

## S3 Extension

Enhanced Direct Access and Expanded Support in Lucee 6

The S3 Extension in Lucee 6 has received substantial updates, introducing native access functions and broadening support for various S3 providers.

### What's New?

- **Native Access Functions**: The addition of functions for native access to the S3 extension marks a significant enhancement. This allows for more direct and precise interaction with S3, not limited to its use as a virtual file system. The benefit? Improved performance due to more efficient, targeted code.
- **Support for Additional S3 Providers**: Lucee 6's S3 Extension now extends support beyond AWS to include other S3-compatible providers like Google Cloud Storage and Wasabi. This broadened compatibility offers more flexibility in choosing storage solutions that fit your needs.
- **Library Transition**: We've transitioned from the Jedis library to the library provided by AWS. The reason? Jedis had certain limitations and bugs that restricted functionality. The AWS library, on the other hand, provides a more reliable and comprehensive toolkit for interacting with S3 services.

These enhancements in the S3 Extension for Lucee 6 are designed to improve both the versatility and performance of S3 interactions, offering a more robust and flexible approach to managing S3 resources.

















