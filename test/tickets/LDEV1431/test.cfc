component {
    public static function testStatic() {
        return getcurrentTemplatePath();
    }

    public function testInstance() {
        return  static.testStatic();
    }
}