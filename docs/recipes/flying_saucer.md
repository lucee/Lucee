<!--
{
  "title": "Flying Saucer PDF Engine - CFDOCUMENT",
  "id": "flying_saucer",
  "related": [
    "tag-document"
  ],
  "categories": [
    "pdf"
  ],
  "description": "The new CFDOCUMENT PDF engine, Flying Saucer in Lucee 5.3",
  "menuTitle": "The new PDF engine, Flying Saucer in Lucee 5.3",
  "keywords": [
    "Flying Saucer",
    "PDF Engine",
    "CFDOCUMENT",
    "HTML to PDF"
  ]
}
-->
#PDF Engine (Flying saucer)
This document provides information about the new PDF engine, [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) (FS) in Lucee 5.3

Flying saucer is a new PDF engine in Lucee. PDF engines are mainly used to convert HTML to PDF format.

### Benefits of moving to Flying Saucer from the old engine (PD4ML) ###

* Full support for CSS 2.1
* On average the generated PDFs are smaller
* Consume less Memory and CPU
* Engine in active development,
* Better Results

### Downsides to Flying Saucer compared to the old engine (PD4ML) ###

* The generated PDF does not always look exactly the same when generated with the new FC compared to files generated with the PD4ML.

If it's important that the PDF output remains exactly the same as the old PD4ML-generated file, you will need to check it manually.

If you don't have time to check all PDF outputs, or you really don't care about the fancy new engine, simply add the following code to use the old PDF engine.

via Application.cfc,

```luceescript
this.pdf.type = "classic";
```

or if you are using an Application.cfm,

```lucee
<cfapplication pdf="#\{type:'classic'\}#">
```

and since the PDF Extension 1.0.0.92-SNAPSHOT you can specify the engine using type

```lucee
<cfdocument type="modern">
  or
<cfdocument type="classic">
```

### Features of Flying Saucer ###

You can define a font directory where you have the fonts(.ttf,.otf) you are using in your PDF.

### Define the font directory ####

```lucee
<cfdocument fontDirectory = "path/to/my/font">
```

Define the font directory Application itself:

via Application.cfc

```luceescript
this.pdf.fontDirectory = "path/to/my/font";
```

or via application.cfm

```lucee
<cfapplication pdf="#\{fontDirectory	:'path/to/my/font'\}#">
```

If the font directory isn't specified, Lucee will look for fonts in /WEB_INF/lucee/fonts and uses them if they match.

**Note**: Classic engine works using the font-family-name from pd4fonts.properties file. Modern (Flying saucer) engine works using the font-family-name from the .ttf file with the same case.

#### Simplify Attributes ####

Attributes with cfdocument are a mess. You can make it clearer using the following syntax:

Example:

```lucee
<cfdocument marginTop="5" marginBottom="5" marginLeft="5" marginRight="5" pageWidth="5" pageHeight="5" pageType="A4">
```

In Lucee you can do the following:

```lucee
<cfdocument margin="#\{top:5,bottom:5,left:5,right:5\}#" page="#\{width:5,height:5,type:'A4'\}#">
```

Or even simpler

```lucee
<cfdocument margin="5" page="#\{width:5,height:5,type:'A4'\}#">
```

#### Additional Units ####

In addition to "inch" and "cm", the attribute unit now supports "pixel" and "points".

```lucee
<cfdocument unit="in|cm|px|pt">
```

If you find any issues while using the new PDF engine, please ask a question on the [mailing list](https://dev.lucee.org/)

### Footnotes ###

You can see the details in this video:
[Flying saucer](https://www.youtube.com/watch?v=B3Yfa8SUKKg)