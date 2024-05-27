<!--
{
  "title": "Supercharge your website",
  "id": "supercharge-your-website",
  "description": "This document explains how you can improve the performance of your website in a very short time with Lucee.",
  "keywords": [
    "Supercharge website",
    "Performance",
    "Caching",
    "Template cache",
    "Lucee"
  ]
}
-->
## Supercharge your website ##

This document explains how you can improve the performance of your website in a very short time with Lucee.

### Example: ###

```luceescript
// index.cfm
writeDump(now());
```

Run the above index.cfm, and you get a timestamp. Whenever we call our file, Lucee checks once at every request if a file has changed or not (for files currently residing in the template cache). If a file has changed, Lucee recompiles it, and executes it. Checking the files at every request takes time. If you have a published server, and you know your server does not produce or change any files, you can simply avoid that check that happens all the time.

### Using Admin ###

* Go to _Admin -> Performance/ Caching -> Inspect Templates (CFM/CFC) -> Never_

* The default is "Once", checking any requested files one time within each request. You should check "Never" to avoid the checking at every request.

* Change the index.cfm and run it again. No changes happen in the output because Lucee does not check if the file changed or not. Now, let's see the faster execution and less performance memory being used.

* You can clear the cache by code using `pagePoolClear()`. This clears all template cache so that Lucee will check again if the template has changed. On the next request, Lucee will check initially for the file.

* Another option to clear the template cache is to use clear cache via the admin by clicking the button in _Admin -> Settings -> Performance/ Caching -> Page Pool Cache_.

### Footnotes ###

Here you can see these details on video also:

[Charge Your Website](https://youtu.be/w-eeigEkmn0)