<cfscript>
component persistent="true" discriminatorColumn="isUserTag" table="Tag1425" {

	property name="id" column="tagId" fieldtype="id" generator="identity";
	property name="bookmarks" fieldtype="many-to-many" type="array" CFC="Bookmark" linktable="BookmarkTag" persistent='true';
	



}
</cfscript>