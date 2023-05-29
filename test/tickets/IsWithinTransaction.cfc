component extends = "org.lucee.cfml.test.LuceeTestCase" {
        
    public void function testIsWithinTransaction() {
        assertFalse(isWithinTransaction());

        transaction {
            assertTrue(isWithinTransaction());
        }
    }
}