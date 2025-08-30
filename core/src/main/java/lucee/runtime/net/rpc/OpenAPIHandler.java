package lucee.runtime.net.rpc;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.OpenAPIGenerator;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.ComponentScope;

import java.util.Iterator;
import java.util.Map.Entry;

public class OpenAPIHandler {
	private static final String CFC_EXTENSION = ".cfc";
	private static final String SWAGGER_UI_VERSION = "4.15.5";

	public static void handleOpenAPI(PageContext pc, HttpServletRequest req, HttpServletResponse rsp) throws IOException {
		try {
			// Get the CFC path from the request
			Component comp = loadAndValidateComponent(pc, req);

			boolean openApiEnabled = Caster.toBooleanValue(comp.getMetaData(pc).get("openApi", null), false);

			// check that the component has the attribute openApi="true"
			if (!openApiEnabled) {
				writeErrorResponse(rsp, HttpServletResponse.SC_FORBIDDEN,
					"Component is not enabled for OpenAPI");
				return;
			}

			// Check if component allows remote access
			if (!hasRemoteMethods(comp)) {
				writeErrorResponse(rsp, HttpServletResponse.SC_METHOD_NOT_ALLOWED,
					"No remote methods found in component");
				return;
			}

			// Generate OpenAPI spec
			String cfcPath = req.getServletPath();
			if (cfcPath.endsWith(CFC_EXTENSION)) {
				cfcPath = cfcPath.substring(0, cfcPath.length() - CFC_EXTENSION.length());
			}
			String baseURL = req.getScheme() + "://" + req.getServerName() +
						":" + req.getServerPort() + req.getContextPath() + "/" + cfcPath + CFC_EXTENSION;

			String openApiJson = OpenAPIGenerator.generateOpenAPI(comp, baseURL, pc);

			// Set response headers
			rsp.setContentType("application/json");
			rsp.setCharacterEncoding("UTF-8");
			rsp.setHeader("Access-Control-Allow-Origin", "*"); // For CORS if needed
			rsp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

			// Write response
			rsp.getWriter().write(openApiJson);

		} catch (PageException e) {
			writeErrorResponse(rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, escapeJson(e.getMessage()));
		} catch (Exception e) {
			// Handle unexpected error
			writeErrorResponse(rsp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, escapeJson(e.getMessage()));
		}
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
			writeErrorResponse(rsp, HttpServletResponse.SC_FORBIDDEN,
					"Component is not enabled for Swagger (swagger=\"true\" required)");
			return;
		}

		if (!openApiEnabled) {
			writeErrorResponse(rsp, HttpServletResponse.SC_FORBIDDEN,
					"Component requires OpenAPI to be enabled (openApi=\"true\" required for Swagger)");
			return;
		}

		String cfcPath = req.getServletPath();
		String openApiUrl = req.getRequestURL() + "?openapi";
		String swaggerUiVersion = Caster.toString(meta.get("swaggerUiVersion", SWAGGER_UI_VERSION), SWAGGER_UI_VERSION);
		String swaggerHtml = generateSwaggerHTML(openApiUrl, swaggerUiVersion, cfcPath);

		rsp.setContentType("text/html");
		rsp.setCharacterEncoding("UTF-8");
		rsp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
		rsp.getWriter().write(swaggerHtml);
	}

	private static void writeErrorResponse(HttpServletResponse rsp, int statusCode, String errorMessage) throws IOException {
		rsp.setStatus(statusCode);
		rsp.setContentType("application/json");
		rsp.getWriter().write("{\"error\":\"" + escapeJson(errorMessage) + "\"}");
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

	private static String escapeJson(String str) {
		if (str == null) return "";
		return str.replace("\\", "\\\\")
				  .replace("\"", "\\\"")
				  .replace("\n", "\\n")
				  .replace("\r", "\\r")
				  .replace("\t", "\\t");
	}

	private static String generateSwaggerHTML(String openApiUrl, String swaggerUiVersion, String cfcPath) {
		return "<!DOCTYPE html>\n" +
			   "<html lang=\"en\">\n" +
			   "<head>\n" +
			   "  <meta charset=\"UTF-8\">\n" +
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