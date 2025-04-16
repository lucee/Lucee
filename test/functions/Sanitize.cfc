component extends="org.lucee.cfml.test.LuceeTestCase" {
    function run() {
        
        describe("Sanitize Function Tests", function() {
            
            it("should mask passwords in URL parameters", function() {
                var input = "User login with password=secretPass123";
                var expected = "User login with password=****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask API keys", function() {
                var input = "API request with apiKey=abc123xyz";
                var expected = "API request with apiKey=****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask access tokens", function() {
                var input = "Using access_key=AKIAIOSFODNN7EXAMPLE";
                var expected = "Using access_key=****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask JWT tokens", function() {
                var input = "JWT: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
                var expected = "JWT: ****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask Bearer tokens", function() {
                var input = "Authorization: Bearer abc123xyz456";
                var expected = "Authorization: Bearer ****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask secrets in JSON", function() {
                var input = "config={'password': 'supersecret', 'api_key': 'abcdef1234567890'}";
                var expected = "config={'password': '****', 'api_key': '****'}";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should mask secrets with double quotes in JSON", function() {
                var input = 'config={"password": "supersecret", "api_key": "abcdef1234567890"}';
                var expected = 'config={"password": "****", "api_key": "****"}';
                expect(sanitize(input)).toBe(expected);
                var config={"password": "****", "api_key": "****"}
                config={"password": "****", "api_key": "abcdef1234567890"}
            });
            
            it("should leave non-sensitive data unchanged", function() {
                var input = "A random string with no secrets: hello world";
                var expected = "A random string with no secrets: hello world";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should handle empty strings", function() {
                var input = "";
                var expected = "";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should handle null values", function() {
                expect(sanitize(nullValue())).toBe("");
            });
            
            it("should mask multiple sensitive data in one string", function() {
                var input = "http://example.com/api?sensitive=true&password=mysecretpassword&token=12345";
                var expected = "http://example.com/api?sensitive=true&password=****&token=****";
                expect(sanitize(input)).toBe(expected);
            });
            
            it("should use custom replacement when provided", function() {
                var input = "User login with password=secretPass123";
                var expected = "User login with password=[REDACTED]";
                expect(sanitize(input, "[REDACTED]")).toBe(expected);
            });
            
            it("should use default mask when empty replacement is provided", function() {
                var input = "User login with password=secretPass123";
                var expected = "User login with password=****";
                expect(sanitize(input, "")).toBe(expected);
            });
            
            it("should handle complex nested structures", function() {
                var input = "Configuration: { 'server': { 'auth': { 'username': 'admin', 'password': 'super_secure_123' } } }";
                var expected = "Configuration: { 'server': { 'auth': { 'username': 'admin', 'password': '****' } } }";
                expect(sanitize(input)).toBe(expected);
            });
        });
        
    }
}