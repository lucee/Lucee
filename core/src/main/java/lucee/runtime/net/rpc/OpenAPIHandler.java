package lucee.runtime.net.rpc;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.rpc.OpenAPIGenerator;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.ComponentScope;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.type.StructImpl;

import java.util.Iterator;
import java.util.Map.Entry;

public class OpenAPIHandler {
	private static final String CFC_EXTENSION = ".cfc";
	private static final String SWAGGER_UI_VERSION = "4.15.5";

	public static void handleOpenAPI(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		Component comp;
		boolean openApiEnabled = false;
		try {
			// Get the CFC path from the request
			comp = loadAndValidateComponent(pc, req);
			openApiEnabled = Caster.toBooleanValue(comp.getMetaData(pc).get("openApi", null), false);
		} catch (PageException e) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		}

		if (!openApiEnabled) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_FORBIDDEN,"Component is not enabled for OpenAPI");
			return;
		}
		if (!hasRemoteMethods(comp)) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,"No remote methods found in component");
			return;
		}

		String cfcPath = req.getServletPath();
		if (cfcPath.endsWith(CFC_EXTENSION)) {
			cfcPath = cfcPath.substring(0, cfcPath.length() - CFC_EXTENSION.length());
		}
		String baseURL = req.getScheme() + "://" + req.getServerName() +
					":" + req.getServerPort() + req.getContextPath() + cfcPath + CFC_EXTENSION;

		String openApiJson;
		try {
			openApiJson = OpenAPIGenerator.generateOpenAPI(comp, baseURL, pc);
		} catch (PageException e) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		} catch (Exception e) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
			return;
		}
		rsp.setContentType("application/json");
		rsp.setCharacterEncoding("UTF-8");
		rsp.setHeader("Access-Control-Allow-Origin", "*"); // For CORS if needed
		rsp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		pc.forceWrite(openApiJson);
	}

	private static Component loadAndValidateComponent(PageContext pc, HttpServletRequest req) throws PageException {
		// Get the CFC path from the request
		String cfcPath = req.getServletPath();
		if (cfcPath.endsWith(CFC_EXTENSION)) {
			cfcPath = cfcPath.substring(0, cfcPath.length() - CFC_EXTENSION.length());
		}
		Component comp = pc.loadComponent(cfcPath);
		return comp;
	}

	public static void handleSwaggerUI(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) throws IOException, PageException {

		Component comp = loadAndValidateComponent(pc, req);
		Struct meta = comp.getMetaData(pc);

		// check that the component has swagger="true" (which requires openApi="true")
		boolean swaggerEnabled = Caster.toBooleanValue(meta.get("swagger", null), false);
		boolean openApiEnabled = Caster.toBooleanValue(meta.get("openApi", null), false);

		if (!swaggerEnabled) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_FORBIDDEN,"Component is not enabled for Swagger (swagger=\"true\" required)");
			return;
		}

		if (!openApiEnabled) {
			writeErrorResponse(pc, rsp, HttpServletResponse.SC_FORBIDDEN,"Component requires OpenAPI to be enabled (openApi=\"true\" required for Swagger)");
			return;
		}

		String cfcPath = req.getServletPath();
		String openApiUrl = req.getRequestURL() + "?openapi";
		String swaggerUiVersion = Caster.toString(meta.get("swaggerUiVersion", SWAGGER_UI_VERSION), SWAGGER_UI_VERSION);
		String swaggerHtml = generateSwaggerHTML(openApiUrl, swaggerUiVersion, cfcPath);

		rsp.setContentType("text/html");
		rsp.setCharacterEncoding("UTF-8");
		rsp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		pc.forceWrite(swaggerHtml);
	}

	private static void writeErrorResponse(PageContext pc, HttpServletResponse rsp, int statusCode, String errorMessage) throws IOException {

		rsp.setStatus(statusCode);
		rsp.setContentType("application/json");
		try {
			Struct errorStruct = new StructImpl();
			errorStruct.set("error", errorMessage);
			String json = new JSONConverter(true, null).serialize(null, errorStruct,  SerializationSettings.SERIALIZE_AS_ROW);
			pc.forceWrite(json);
		} catch (Exception e) {
			throw new RuntimeException("Failed to serialize error response to JSON", e);
		}
	}

	private static boolean hasRemoteMethods(Component comp) {
		try {
			if (comp == null || comp.getComponentScope() == null) {
				return false;
			}
			ComponentScope scope = comp.getComponentScope();
			Iterator<Entry<Key, Object>> it = scope.entryIterator();
			while (it.hasNext()) {
				Entry<Key, Object> entry = it.next();
				Object value = entry.getValue();
				if (value instanceof UDF) {
					UDF udf = (UDF) value;
					if (udf.getAccess() == Component.ACCESS_REMOTE) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private static String generateSwaggerHTML(String openApiUrl, String swaggerUiVersion, String cfcPath) {
		return "<!DOCTYPE html>\n" +
			   "<html lang=\"en\">\n" +
			   "<head>\n" +
			   "  <meta charset=\"UTF-8\">\n" +
			   "  <meta name=\"robots\" content=\"noindex, nofollow\">\n" +
			   "  <title>API Documentation - " + cfcPath + "</title>\n" +
			   "  <link rel=\"stylesheet\" type=\"text/css\" href=\"https://unpkg.com/swagger-ui-dist@" + swaggerUiVersion + "/swagger-ui.css\" />\n" +
			   "  <style>\n" +
			   "    html { box-sizing: border-box; overflow: -moz-scrollbars-vertical; overflow-y: scroll; }\n" +
			   "    *, *:before, *:after { box-sizing: inherit; }\n" +
			   "    body { margin:0; background: #fafafa; }\n" +
			   "  </style>\n" +
			   "</head>\n" +
			   "<body>\n" +
			   "  <div id=\"swagger-ui\"></div>\n" +
			   "  <script src=\"https://unpkg.com/swagger-ui-dist@" + swaggerUiVersion + "/swagger-ui-bundle.js\"></script>\n" +
			   "  <script src=\"https://unpkg.com/swagger-ui-dist@" + swaggerUiVersion + "/swagger-ui-standalone-preset.js\"></script>\n" +
			   "  <script>\n" +
			   "    window.onload = function() {\n" +
			   "      const ui = SwaggerUIBundle({\n" +
			   "        url: '" + openApiUrl + "',\n" +
			   "        dom_id: '#swagger-ui',\n" +
			   "        deepLinking: true,\n" +
			   "        presets: [\n" +
			   "          SwaggerUIBundle.presets.apis,\n" +
			   "          SwaggerUIStandalonePreset\n" +
			   "        ],\n" +
			   "        plugins: [\n" +
			   "          SwaggerUIBundle.plugins.DownloadUrl\n" +
			   "        ],\n" +
			   "        layout: \"StandaloneLayout\"\n" +
			   "      });\n" +
			   "    };\n" +
			   "  </script>\n" +
			   "</body>\n" +
			   "</html>";
	}
}