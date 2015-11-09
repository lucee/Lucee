component persistent="true" table="Page_PageUrl"
{
    property name="Page" fieldtype="id,many-to-one" cfc="Page" fkcolumn="pageID";
    property name="IsDeleted" type="string" ormtype="boolean" default="0";
    property name="Created" type="string" ormtype="timestamp";
    property name="LastModified" type="string" ormtype="timestamp";
}