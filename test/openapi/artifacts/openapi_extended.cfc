component 
    displayname="OpenAPI Test Service" 
    hint="A comprehensive test service demonstrating all OpenAPI data types and patterns" {

    // ========================================
    // BASIC PRIMITIVE TYPES
    // ========================================

    /**
     * String type example
     * @param name Required string parameter
     * @param description Optional string with pattern validation
     * @param category String with enum values
     */
    remote string function getString(
        required string name hint="User's full name" example="John Doe",
        string description hint="Optional description" default="",
        string category hint="Category selection" enum="personal,business,other" default="personal"
    ) 
    access="remote" 
    returntype="string" 
    hint="Returns a formatted string message"
    httpmethod="POST"
    restpath="/string" {
        
        return "Hello #arguments.name#! Category: #arguments.category#. Description: #arguments.description#";
    }

    /**
     * Numeric types example
     * @param price Decimal number
     * @param quantity Integer number
     * @param percentage Float between 0-100
     */
    remote numeric function calculateTotal(
        required numeric price hint="Item price" minimum="0" example="29.99",
        required numeric quantity hint="Quantity to purchase" type="integer" minimum="1" example="3",
        numeric percentage hint="Discount percentage" minimum="0" maximum="100" default="0" example="15.5"
    )
    access="remote"
    returntype="numeric"
    hint="Calculates total price with discount"
    httpmethod="POST"
    restpath="/calculate" {
        
        var discount = arguments.percentage / 100;
        var subtotal = arguments.price * arguments.quantity;
        return subtotal * (1 - discount);
    }

    /**
     * Boolean type example
     */
    remote boolean function validateUser(
        required string username hint="Username to validate" minlength="3" maxlength="50",
        required boolean active hint="Whether user should be active" default="true",
        boolean sendEmail hint="Send notification email" default="false"
    )
    access="remote"
    returntype="boolean"
    hint="Validates user credentials and status"
    httpmethod="GET"
    restpath="/validate" {
        
        // Simple validation logic
        return len(arguments.username) >= 3 && arguments.active;
    }

    // ========================================
    // DATE/TIME TYPES
    // ========================================

    /**
     * Date and DateTime examples
     */
    remote struct function processDate(
        required date birthDate hint="User's birth date" format="date" example="1990-05-15",
        date appointmentDateTime hint="Appointment date and time" format="date-time" example="2024-12-25T14:30:00Z",
        string timezone hint="Timezone identifier" default="UTC" example="America/New_York"
    )
    access="remote"
    returntype="struct"
    hint="Processes date information and returns formatted results"
    httpmethod="GET"
    restpath="/dates" {
        
        var result = {
            "birthDate": dateFormat(arguments.birthDate, "yyyy-mm-dd"),
            "age": dateDiff("yyyy", arguments.birthDate, now()),
            "appointmentDateTime": isDefined("arguments.appointmentDateTime") ? 
                dateTimeFormat(arguments.appointmentDateTime, "iso8601") : "",
            "timezone": arguments.timezone,
            "currentDateTime": dateTimeFormat(now(), "iso8601")
        };
        
        return result;
    }

    // ========================================
    // ARRAY TYPES
    // ========================================

    /**
     * Array parameter and return examples
     */
    remote array function processArray(
        required array stringArray hint="Array of strings" items="string" example='["apple","banana","cherry"]',
        array numberArray hint="Array of numbers" items="number" default="[]" example="[1,2,3,4,5]",
        array mixedArray hint="Array of mixed types" default="[]"
    )
    access="remote"
    returntype="array"
    hint="Processes arrays and returns combined results"
    httpmethod="POST"
    restpath="/arrays" {
        
        var result = [];
        
        // Process string array
        for (var item in arguments.stringArray) {
            arrayAppend(result, { "type": "string", "value": item, "length": len(item) });
        }
        
        // Process number array
        if (isDefined("arguments.numberArray")) {
            for (var num in arguments.numberArray) {
                arrayAppend(result, { "type": "number", "value": num, "doubled": num * 2 });
            }
        }
        
        return result;
    }

    /**
     * Array of objects example
     */
    remote array function getUserList(
        numeric limit hint="Maximum number of users to return" minimum="1" maximum="100" default="10",
        numeric offset hint="Number of users to skip" minimum="0" default="0",
        string sortBy hint="Field to sort by" enum="name,email,createdDate" default="name"
    )
    access="remote"
    returntype="array"
    hint="Returns array of user objects"
    httpmethod="GET"
    restpath="/users" {
        
        var users = [];
        
        for (var i = 1; i <= arguments.limit; i++) {
            var user = {
                "id": arguments.offset + i,
                "name": "User " & (arguments.offset + i),
                "email": "user" & (arguments.offset + i) & "@example.com",
                "active": (i % 2 == 1),
                "createdDate": dateAdd("d", -i, now()),
                "profile": {
                    "firstName": "First" & i,
                    "lastName": "Last" & i,
                    "age": 20 + (i % 50)
                }
            };
            arrayAppend(users, user);
        }
        
        return users;
    }

    // ========================================
    // OBJECT/STRUCT TYPES
    // ========================================

    /**
     * Complex object parameter and return
     */
    remote struct function createUser(
        required struct userProfile hint="User profile information",
        struct preferences hint="User preferences" default="#{}#",
        struct metadata hint="Additional metadata" default="#{}#"
    )
    access="remote"
    returntype="struct"
    hint="Creates a new user with profile information"
    httpmethod="POST"
    restpath="/users" {
        
        var newUser = {
            "id": createUUID(),
            "profile": arguments.userProfile,
            "preferences": arguments.preferences,
            "metadata": arguments.metadata,
            "createdDate": now(),
            "lastModified": now(),
            "status": "active"
        };
        
        // Validate required fields in userProfile
        if (!structKeyExists(arguments.userProfile, "email")) {
            throw(message="Email is required in userProfile", type="ValidationError");
        }
        
        return newUser;
    }

    /**
     * Nested object example
     */
    remote struct function getCompanyInfo(
        required string companyId hint="Company identifier" example="COMP123"
    )
    access="remote"
    returntype="struct"
    hint="Returns detailed company information with nested objects"
    httpmethod="GET"
    restpath="/companies/{companyId}" {
        
        return {
            "id": arguments.companyId,
            "name": "Sample Company Inc.",
            "address": {
                "street": "123 Main Street",
                "city": "Anytown",
                "state": "CA",
                "zipCode": "12345",
                "country": "US",
                "coordinates": {
                    "latitude": 37.7749,
                    "longitude": -122.4194
                }
            },
            "contact": {
                "phone": "+1-555-123-4567",
                "email": "info@samplecompany.com",
                "website": "https://samplecompany.com"
            },
            "employees": [
                {
                    "id": "EMP001",
                    "name": "John Doe",
                    "title": "CEO",
                    "department": "Executive"
                },
                {
                    "id": "EMP002",
                    "name": "Jane Smith",
                    "title": "CTO",
                    "department": "Technology"
                }
            ],
            "settings": {
                "timezone": "America/Los_Angeles",
                "currency": "USD",
                "features": {
                    "apiAccess": true,
                    "reporting": true,
                    "analytics": false
                }
            }
        };
    }

    // ========================================
    // QUERY TYPE (CFML specific)
    // ========================================

    /**
     * Query type example - converts to array of objects in JSON
     */
    remote query function getDataQuery(
        string filter hint="Filter criteria" default="",
        numeric maxRows hint="Maximum rows to return" default="50"
    )
    access="remote"
    returntype="query"
    hint="Returns query data (converts to array of objects in JSON)"
    httpmethod="GET"
    restpath="/data" {
        
        var qry = queryNew("id,name,email,status,createdDate", "integer,varchar,varchar,varchar,timestamp");
        
        for (var i = 1; i <= arguments.maxRows; i++) {
            queryAddRow(qry);
            querySetCell(qry, "id", i);
            querySetCell(qry, "name", "Record " & i);
            querySetCell(qry, "email", "record" & i & "@example.com");
            querySetCell(qry, "status", i % 2 == 1 ? "active" : "inactive");
            querySetCell(qry, "createdDate", dateAdd("d", -i, now()));
        }
        
        return qry;
    }

    // ========================================
    // BINARY/FILE TYPES
    // ========================================

    /**
     * Binary data example
     */
    remote any function uploadFile(
        required any fileData hint="Binary file data" format="binary",
        string fileName hint="Name of the file" example="document.pdf",
        string contentType hint="MIME type of the file" example="application/pdf"
    )
    access="remote"
    returntype="struct"
    hint="Handles file upload and returns file information"
    httpmethod="POST"
    restpath="/upload" {
        
        return {
            "fileName": arguments.fileName ?: "unknown",
            "contentType": arguments.contentType ?: "application/octet-stream",
            "size": isDefined("arguments.fileData") ? len(arguments.fileData) : 0,
            "uploadDate": now(),
            "fileId": createUUID(),
            "status": "uploaded"
        };
    }

    // ========================================
    // ERROR HANDLING EXAMPLES
    // ========================================

    /**
     * Function that demonstrates error responses
     */
    remote struct function testErrorHandling(
        required string action hint="Action to perform" enum="success,notfound,validation,server",
        string message hint="Custom error message" default=""
    )
    access="remote"
    returntype="struct"
    hint="Tests different error response scenarios"
    httpmethod="POST"
    restpath="/test-errors" {
        
        switch (arguments.action) {
            case "success":
                return { "status": "success", "message": "Operation completed successfully" };
            
            case "notfound":
                throw(message="Resource not found", type="NotFoundError", errorcode="404");
            
            case "validation":
                throw(message="Validation failed: " & arguments.message, type="ValidationError", errorcode="400");
            
            case "server":
                throw(message="Internal server error occurred", type="ServerError", errorcode="500");
            
            default:
                throw(message="Invalid action specified", type="BadRequestError", errorcode="400");
        }
    }

    // ========================================
    // MIXED COMPLEX EXAMPLE
    // ========================================

    /**
     * Complex function with multiple parameter types
     */
    remote struct function complexOperation(
        required string operationId hint="Unique operation identifier",
        required struct config hint="Operation configuration",
        array items hint="Items to process" default="[]",
        boolean async hint="Process asynchronously" default="false",
        numeric timeout hint="Timeout in seconds" minimum="1" maximum="300" default="30",
        date scheduledFor hint="When to execute (if async)" format="date-time",
        struct metadata hint="Additional metadata" default="#{}#"
    )
    access="remote"
    returntype="struct"
    hint="Performs a complex operation with multiple parameter types"
    httpmethod="POST"
    restpath="/complex" {
        
        var result = {
            "operationId": arguments.operationId,
            "status": arguments.async ? "queued" : "completed",
            "config": arguments.config,
            "itemsProcessed": arrayLen(arguments.items),
            "timeout": arguments.timeout,
            "executionTime": now(),
            "metadata": arguments.metadata
        };
        
        if (arguments.async && isDefined("arguments.scheduledFor")) {
            result["scheduledFor"] = arguments.scheduledFor;
        }
        
        if (arrayLen(arguments.items) > 0) {
            result["items"] = [];
            for (var item in arguments.items) {
                arrayAppend(result.items, {
                    "original": item,
                    "processed": true,
                    "processedAt": now()
                });
            }
        }
        
        return result;
    }

    // ========================================
    // HELPER/UTILITY METHODS
    // ========================================

    /**
     * Get OpenAPI specification for this component
     */
    remote struct function getApiSpec()
    access="remote"
    returntype="struct"
    hint="Returns the OpenAPI specification for this service"
    httpmethod="GET"
    restpath="/spec" {
        
        return {
            "openapi": "3.0.3",
            "info": {
                "title": "OpenAPI Test Service",
                "description": "A comprehensive test service demonstrating all OpenAPI data types",
                "version": "1.0.0"
            },
            "message": "This would be dynamically generated from component introspection"
        };
    }

}