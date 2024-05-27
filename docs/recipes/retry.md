<!--
{
  "title": "Retry",
  "id": "retry",
  "related": [
    "tag-catch",
    "tag-retry",
    "tag-try"
  ],
  "description": "This document explains how to use retry functionality with some simple examples.",
  "keywords": [
    "Retry",
    "Exception handling",
    "Try catch",
    "Lucee"
  ]
}
-->
## Retry ##

This document explains how to use retry functionality with some simple examples.

### Example 1: ###

```luceescript
// example1.cfm

path="test.txt";
function fr(){
	dump(fileRead(path));
}
try {
	fr();
}
catch(e) {
	if(!fileExists(path)) {
		fileWrite(path,"content of the file");
		fr();
	}
	else echo(e);
}
if(fileExists(path)) fileDelete(path);
```

In this example, we have the "try" and "catch" blocks for reading a file and outputting the file content. Maybe that file does not exist in every case. So we have to check in advance if the file exists or not. In this example, the file read is a function `fr()`, and we will call this function in the "try" block. If the file does not exist, the "catch" block is executed and the `fr()` function is called again.

This is not the best way. 'Retry' is a better option. The retry code looks like example2.cfm

### Example 2: ###

```luceescript
// example2.cfm

path="test.txt";
try {
	dump(fileRead(path));
}
catch(e) {
	if(!fileExists(path)) {
		fileWrite(path,"content of the file");
		retry;
	}
	echo(e);
}
if(fileExists(path)) fileDelete(path);
```

In this example, we use the retry functionality. Here we also still check if the file exists or not. If the file does not exist, we create a new file by using `fileWrite`. Then call retry to avoid duplicate code `fr()`. Retry points to the beginning of the try block and then it will read again the file and output the file content.

We do not get an exception because if the file does not exist, we call retry (read the file again and output the file content). For this case, we simply use retry, and if it fails, we correct what is wrong.

### Footnotes ###

Here you can see these details in the video also:

[Retry](https://youtu.be/zA9aAAimkk8)