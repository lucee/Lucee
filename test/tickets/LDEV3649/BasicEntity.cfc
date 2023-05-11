/**
 * Basic ORM Entity
*/
import sub.BasicComponent;
component persistent="true" {
    property name="id" fieldtype="id" generator="native";

    public function ViaImportPath(){
        var BasicComponent = new BasicComponent();
        return BasicComponent.testFunc();
    }

}