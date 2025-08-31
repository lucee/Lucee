package lucee.runtime.net.rpc;

import java.util.Iterator;
import java.util.Map.Entry;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.converter.ConverterException;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.PageContext;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionArgument;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.UDF;

public class OpenAPIGenerator {

	public static String generateOpenAPI(Component comp, String baseURL, PageContext pc) throws PageException, ConverterException {
		Struct openapi = new StructImpl();

		// OpenAPI version and info
		openapi.setEL("openapi", "3.0.3");

		Struct info = new StructImpl();
		String displayName = comp.getDisplayName();
		if (StringUtil.isEmpty(displayName)) {
			displayName = "Lucee Remote Component API";
		}
		info.setEL("title", displayName);

		String hint = comp.getHint();
		if (StringUtil.isEmpty(hint)) {
			hint = "Auto-generated OpenAPI specification for Lucee remote component";
		}
		info.setEL("description", hint);
		info.setEL("version", "1.0.0");
		openapi.setEL("info", info);

		// Servers
		Array servers = new ArrayImpl();
		Struct server = new StructImpl();
		server.setEL("url", baseURL);
		servers.appendEL(server);
		openapi.setEL("servers", servers);

		// Paths - generate from remote methods
		Struct paths = new StructImpl();
		generatePaths(comp, paths);
		openapi.setEL("paths", paths);

		// Components/Schemas
		Struct components = new StructImpl();
		Struct schemas = new StructImpl();
		generateCommonSchemas(schemas);
		components.setEL("schemas", schemas);
		openapi.setEL("components", components);

		JSONConverter converter = new JSONConverter(true, null);
		return converter.serialize(pc, openapi, SerializationSettings.SERIALIZE_AS_ROW);
	}

	private static void generatePaths(Component comp, Struct paths) throws PageException {
		ComponentScope scope = comp.getComponentScope();
		Iterator<Entry<Key, Object>> it = scope.entryIterator();

		while (it.hasNext()) {
			Entry<Key, Object> entry = it.next();
			Object obj = entry.getValue();

			if (obj instanceof UDF) {
				UDF udf = (UDF) obj;

				// Only include remote methods
				if (udf.getAccess() == Component.ACCESS_REMOTE) {
					String methodName = entry.getKey().getString();
					generateMethodPath(udf, methodName, paths);
				}
			}
		}
	}

	private static void generateMethodPath(UDF udf, String methodName, Struct paths) {
		String pathKey = "?method=" + methodName;

		Struct pathItem = new StructImpl();

		// Get HTTP methods from UDF attribute, default to GET
		String[] httpMethods = {"get"}; // Default to GET
		try {
			Object httpMethodAttr = udf.getMetaData(null).get("httpMethod", null);
			if (httpMethodAttr != null) {
				String methodStr = httpMethodAttr.toString().toLowerCase();
				// Split by comma and trim whitespace
				httpMethods = methodStr.split(",");
				for (int i = 0; i < httpMethods.length; i++) {
					httpMethods[i] = httpMethods[i].trim();
				}
			}
		} catch (Exception e) {
			// Keep default GET if attribute access fails
		}

		// Create operation for each HTTP method
		for (String httpMethod : httpMethods) {
			Struct operation = new StructImpl();

			// Method info
			String hint = udf.getHint();
			if (StringUtil.isEmpty(hint)) {
				hint = "Remote method: " + methodName;
			}
			operation.setEL("summary", hint);
			operation.setEL("operationId", methodName + "_" + httpMethod.toUpperCase());
			operation.setEL("tags", new String[]{"Remote Methods"});

			// Handle parameters based on HTTP method
			FunctionArgument[] args = udf.getFunctionArguments();
			if (args != null && args.length > 0) {
				if ("get".equals(httpMethod) || "head".equals(httpMethod)) {
					// For GET/HEAD, use query parameters ONLY
					Array parameters = new ArrayImpl();

					for (FunctionArgument arg : args) {
						Struct param = new StructImpl();
						param.setEL("name", arg.getName().getString());
						param.setEL("in", "query");
						param.setEL("required", arg.isRequired());

						Struct schema = new StructImpl();
						schema.setEL("type", cfmlToToOpenApiType(arg.getTypeAsString()));
						param.setEL("schema", schema);

						String argHint = arg.getHint();
						if (!StringUtil.isEmpty(argHint)) {
							param.setEL("description", argHint);
						}

						parameters.appendEL(param);
					}

					operation.setEL("parameters", parameters);
					// DO NOT set requestBody for GET/HEAD methods
				} else {
					// For POST/PUT/PATCH, use request body ONLY
					Struct requestBody = new StructImpl();
					requestBody.setEL("required", true);

					Struct content = new StructImpl();
					Struct formContent = new StructImpl();
					Struct schema = new StructImpl();
					schema.setEL("type", "object");

					Struct properties = new StructImpl();
					Array required = new ArrayImpl();

					for (FunctionArgument arg : args) {
						Struct propSchema = new StructImpl();
						propSchema.setEL("type", cfmlToToOpenApiType(arg.getTypeAsString()));

						String argHint = arg.getHint();
						if (!StringUtil.isEmpty(argHint)) {
							propSchema.setEL("description", argHint);
						}

						properties.setEL(arg.getName().getString(), propSchema);

						if (arg.isRequired()) {
							required.appendEL(arg.getName().getString());
						}
					}

					schema.setEL("properties", properties);
					if (required.size() > 0) {
						schema.setEL("required", required);
					}

					formContent.setEL("schema", schema);
					content.setEL("application/x-www-form-urlencoded", formContent);
					content.setEL("multipart/form-data", formContent);

					requestBody.setEL("content", content);
					operation.setEL("requestBody", requestBody);
					// DO NOT set parameters for POST/PUT/PATCH methods
				}
			}

			// Response (same for all HTTP methods)
			Struct responses = new StructImpl();

			// 200 Success response
			Struct response200 = new StructImpl();
			response200.setEL("description", "Successful response");

			Struct content = new StructImpl();
			Struct jsonContent = new StructImpl();
			Struct schema = new StructImpl();
			schema.setEL("type", cfmlToToOpenApiType(udf.getReturnTypeAsString()));
			jsonContent.setEL("schema", schema);
			content.setEL("application/json", jsonContent);
			response200.setEL("content", content);
			responses.setEL("200", response200);

			// Error responses
			Struct response400 = new StructImpl();
			response400.setEL("description", "Bad Request");
			Struct errorContent = new StructImpl();
			Struct errorSchema = new StructImpl();
			errorSchema.setEL("$ref", "#/components/schemas/Error");
			errorContent.setEL("schema", errorSchema);
			Struct errorContentWrapper = new StructImpl();
			errorContentWrapper.setEL("application/json", errorContent);
			response400.setEL("content", errorContentWrapper);
			responses.setEL("400", response400);

			Struct response500 = new StructImpl();
			response500.setEL("description", "Internal Server Error");
			response500.setEL("content", errorContentWrapper);
			responses.setEL("500", response500);

			operation.setEL("responses", responses);

			// Set the operation under the correct HTTP method
			pathItem.setEL(httpMethod, operation);
		}

		paths.setEL(pathKey, pathItem);
	}

	private static String cfmlToToOpenApiType(String luceeType) {
		if (luceeType == null) return "string";

		switch (luceeType.toLowerCase()) {
			case "numeric":
			case "number":
				return "number";
			case "boolean":
				return "boolean";
			case "array":
				return "array";
			case "struct":
			case "component":
				return "object";
			case "date":
				return "string"; // OpenAPI format: date-time can be added
			case "binary":
				return "string"; // OpenAPI format: binary can be added
			default:
				return "string";
		}
	}

	private static void generateCommonSchemas(Struct schemas) {
		// Error schema
		Struct errorSchema = new StructImpl();
		errorSchema.setEL("type", "object");

		Struct errorProps = new StructImpl();

		Struct messageSchema = new StructImpl();
		messageSchema.setEL("type", "string");
		messageSchema.setEL("description", "Error message");
		errorProps.setEL("message", messageSchema);

		Struct typeSchema = new StructImpl();
		typeSchema.setEL("type", "string");
		typeSchema.setEL("description", "Error type");
		errorProps.setEL("type", typeSchema);

		Struct detailSchema = new StructImpl();
		detailSchema.setEL("type", "string");
		detailSchema.setEL("description", "Error detail");
		errorProps.setEL("detail", detailSchema);

		errorSchema.setEL("properties", errorProps);

		Array errorRequired = new ArrayImpl();
		errorRequired.appendEL("message");
		errorSchema.setEL("required", errorRequired);

		schemas.setEL("Error", errorSchema);
	}
}