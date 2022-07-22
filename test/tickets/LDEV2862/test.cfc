component persistent="true" table="test" output="false"{
    property name="id" fieldtype = "id" generator="native";
    property name="A" ormType="string";

    property name="test2" cfc="test2" linktable="okok" fieldtype="many-to-many" fkcolumn="testid" inversejoinColumn="id";
}