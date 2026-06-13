<cfscript>
	function adminAIStoreAppKey() {
		return "adminAIStore_" & hash((session.urlToken ?: "") & ":" & request.adminType, "SHA-256");
	}

	function adminAIStoreGetBucket() {
		var key = adminAIStoreAppKey();
		if (!structKeyExists(application, key) || !isStruct(application[key])) {
			application[key] = { "updated": now(), "pages": {} };
		}
		application[key].updated = now();
		return application[key];
	}

	function adminAIStoreCleanup() {
		for (var key in application) {
			if (left(key, 13) == "adminAIStore_" && isStruct(application[key])) {
				if (dateDiff("h", application[key].updated ?: now(), now()) > 24) {
					structDelete(application, key);
				}
			}
		}
	}

	function adminAIStoreGet(required string pageHash) {
		var bucket = adminAIStoreGetBucket();
		if (structKeyExists(bucket.pages, arguments.pageHash)) {
			return bucket.pages[arguments.pageHash];
		}
		return {};
	}

	function adminAIStoreSet(required string pageHash, required struct data) {
		var bucket = adminAIStoreGetBucket();
		bucket.pages[arguments.pageHash] = arguments.data;
		adminAIStoreCleanup();
	}

	function adminAIStoreDelete(required string pageHash) {
		var bucket = adminAIStoreGetBucket();
		structDelete(bucket.pages, arguments.pageHash);
	}
</cfscript>
