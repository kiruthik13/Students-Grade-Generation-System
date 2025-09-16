package com.wipro.studentgrade.service;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.wipro.studentgrade.bean.StudentBean;
import com.wipro.studentgrade.dao.StudentDAO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpApiServer {
	private final int port;
	private final GradeProcessor gradeProcessor;
	private final StudentDAO studentDAO;

	public HttpApiServer(int port) {
		this.port = port;
		this.gradeProcessor = new GradeProcessor();
		this.studentDAO = new StudentDAO();
	}

	public void start() throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		
		// Serve static files from web directory
		server.createContext("/", new StaticFileHandler(Paths.get("..", "web")));
		
		// API endpoints
		server.createContext("/api/students", new StudentsHandler());
		server.createContext("/api/students/delete", new DeleteStudentHandler());
		server.createContext("/api/students/clear", new ClearStudentsHandler());
		server.createContext("/api/students/bulk-delete", new BulkDeleteHandler());
		server.createContext("/api/students/update", new UpdateStudentHandler());
		server.createContext("/api/statistics", new StatisticsHandler());
		
		server.setExecutor(null);
		System.out.println("Professional Student Grade System started at http://localhost:" + port);
		System.out.println("Open your browser and go to: http://localhost:" + port);
		server.start();
	}

	// Serves static files from web directory
	private static class StaticFileHandler implements HttpHandler {
		private final Path webDir;
		StaticFileHandler(Path webDir) { this.webDir = webDir.normalize().toAbsolutePath(); }
		
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
				respond(exchange, 405, "Method Not Allowed");
				return;
			}
			
			String path = exchange.getRequestURI().getPath();
			if ("/".equals(path)) path = "/index.html";
			
			Path file = webDir.resolve(path.substring(1)).normalize();
			if (!file.startsWith(webDir) || !Files.exists(file) || Files.isDirectory(file)) {
				respond(exchange, 404, "File Not Found");
				return;
			}
			
			String contentType = getContentType(file.toString());
			exchange.getResponseHeaders().set("Content-Type", contentType);
			byte[] data = Files.readAllBytes(file);
			exchange.sendResponseHeaders(200, data.length);
			try (OutputStream os = exchange.getResponseBody()) { os.write(data); }
		}
	}

	private static String getContentType(String fileName) {
		String name = fileName.toLowerCase();
		if (name.endsWith(".html")) return "text/html; charset=utf-8";
		if (name.endsWith(".css")) return "text/css; charset=utf-8";
		if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
		if (name.endsWith(".json")) return "application/json; charset=utf-8";
		return "application/octet-stream";
	}

	private class StudentsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) { handleGet(exchange); return; }
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) { handlePost(exchange); return; }
				respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
		
		private void handleGet(HttpExchange exchange) throws IOException {
			List<StudentBean> students = studentDAO.getAllStudents();
			StringBuilder json = new StringBuilder("[");
			for (int i = 0; i < students.size(); i++) {
				json.append(studentToJson(students.get(i)));
				if (i < students.size() - 1) json.append(",");
			}
			json.append("]");
			respondJson(exchange, 200, json.toString());
		}
		
		private void handlePost(HttpExchange exchange) throws IOException {
			Map<String, String> data = parseFormData(exchange);
			String name = data.getOrDefault("name", "");
			int m1 = parseInt(data.get("m1")), m2 = parseInt(data.get("m2")), 
				m3 = parseInt(data.get("m3")), m4 = parseInt(data.get("m4")), m5 = parseInt(data.get("m5"));
			
			StudentBean bean = new StudentBean(name, m1, m2, m3, m4, m5);
			String result = gradeProcessor.generateGrade(bean);
			respondJson(exchange, 201, studentToJson(bean));
		}
	}

	private class DeleteStudentHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					Map<String, String> data = parseFormData(exchange);
					String studentId = data.get("studentId");
					if (studentId == null || studentId.trim().isEmpty()) {
						respondJson(exchange, 400, jsonMsg("error", "Student ID is required"));
						return;
					}
					String result = studentDAO.deleteStudent(studentId.trim());
					respondJson(exchange, 200, jsonMsg("message", result));
				} else {
					respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
				}
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
	}

	private class ClearStudentsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					String result = studentDAO.clearAllStudents();
					respondJson(exchange, 200, jsonMsg("message", result));
				} else {
					respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
				}
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
	}

	private class BulkDeleteHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					Map<String, String> data = parseFormData(exchange);
					String studentIds = data.get("studentIds");
					if (studentIds == null || studentIds.trim().isEmpty()) {
						respondJson(exchange, 400, jsonMsg("error", "Student IDs are required"));
						return;
					}
					
					String[] ids = studentIds.split(",");
					int deletedCount = 0;
					for (String id : ids) {
						try {
							studentDAO.deleteStudent(id.trim());
							deletedCount++;
						} catch (Exception e) {
							// Continue with other deletions
						}
					}
					
					respondJson(exchange, 200, jsonMsg("message", "Successfully deleted " + deletedCount + " students"));
				} else {
					respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
				}
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
	}

	private class UpdateStudentHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
					Map<String, String> data = parseFormData(exchange);
					String studentId = data.get("studentId");
					String name = data.getOrDefault("name", "");
					int m1 = parseInt(data.get("m1")), m2 = parseInt(data.get("m2")), 
						m3 = parseInt(data.get("m3")), m4 = parseInt(data.get("m4")), m5 = parseInt(data.get("m5"));
					
					if (studentId == null || studentId.trim().isEmpty()) {
						respondJson(exchange, 400, jsonMsg("error", "Student ID is required"));
						return;
					}
					
					// Delete old record and create new one
					studentDAO.deleteStudent(studentId.trim());
					StudentBean bean = new StudentBean(name, m1, m2, m3, m4, m5);
					bean.setStudentId(studentId.trim()); // Keep the same ID
					String result = gradeProcessor.generateGrade(bean);
					
					respondJson(exchange, 200, studentToJson(bean));
				} else {
					respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
				}
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
	}

	private class StatisticsHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			addCors(exchange.getResponseHeaders());
			if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { respond(exchange, 204, ""); return; }
			
			try {
				if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
					List<StudentBean> students = studentDAO.getAllStudents();
					
					int totalStudents = students.size();
					double avgGrade = students.isEmpty() ? 0 : students.stream().mapToInt(StudentBean::getAverage).average().orElse(0);
					String topGrade = students.isEmpty() ? "F" : students.stream().map(StudentBean::getGrade).max(String::compareTo).orElse("F");
					
					// Grade distribution
					Map<String, Integer> gradeCounts = new HashMap<>();
					for (StudentBean s : students) {
						gradeCounts.put(s.getGrade(), gradeCounts.getOrDefault(s.getGrade(), 0) + 1);
					}
					
					StringBuilder stats = new StringBuilder();
					stats.append("{");
					stats.append("\"totalStudents\":").append(totalStudents).append(",");
					stats.append("\"averageGrade\":").append(Math.round(avgGrade)).append(",");
					stats.append("\"topGrade\":\"").append(escapeJson(topGrade)).append("\",");
					stats.append("\"gradeDistribution\":{");
					gradeCounts.forEach((grade, count) -> {
						stats.append("\"").append(escapeJson(grade)).append("\":").append(count).append(",");
					});
					if (stats.charAt(stats.length() - 1) == ',') {
						stats.setLength(stats.length() - 1);
					}
					stats.append("}");
					stats.append("}");
					
					respondJson(exchange, 200, stats.toString());
				} else {
					respond(exchange, 405, jsonMsg("error", "Method Not Allowed"));
				}
			} catch (Exception ex) {
				respond(exchange, 500, jsonMsg("error", ex.getMessage()));
			}
		}
	}

	private Map<String, String> parseFormData(HttpExchange exchange) throws IOException {
		String contentType = getHeader(exchange, "Content-Type");
		if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
			String body = readBody(exchange.getRequestBody());
			return parseUrlEncoded(body);
		}
		return new HashMap<>();
	}

	private static void addCors(Headers headers) {
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
	}
	private static String getHeader(HttpExchange ex, String key) { List<String> values = ex.getRequestHeaders().get(key); return values == null || values.isEmpty() ? null : values.get(0); }
	private static String readBody(InputStream is) throws IOException { byte[] buf = is.readAllBytes(); return new String(buf, StandardCharsets.UTF_8); }
	private static Map<String, String> parseUrlEncoded(String body) throws IOException {
		Map<String, String> map = new HashMap<>();
		if (body == null || body.isEmpty()) return map;
		for (String pair : body.split("&")) {
			String[] kv = pair.split("=", 2);
			String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
			String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
			map.put(k, v);
		}
		return map;
	}

	private static int parseInt(String v) {
		try { return Integer.parseInt(v); } catch (Exception e) { return -1; }
	}

	private static String escapeJson(String s) {
		if (s == null) return "";
		StringBuilder out = new StringBuilder(s.length() + 16);
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"': out.append("\\\""); break;
				case '\\': out.append("\\\\"); break;
				case '\n': out.append("\\n"); break;
				case '\r': out.append("\\r"); break;
				case '\t': out.append("\\t"); break;
				default:
					if (c < 0x20) {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
			}
		}
		return out.toString();
	}

	private static String studentToJson(StudentBean s) {
		return String.format(
			"{\"studentId\":\"%s\",\"name\":\"%s\",\"mark1\":%d,\"mark2\":%d,\"mark3\":%d,\"mark4\":%d,\"mark5\":%d,\"total\":%d,\"average\":%d,\"grade\":\"%s\"}",
			escapeJson(s.getStudentId() != null ? s.getStudentId() : ""),
			escapeJson(s.getName() != null ? s.getName() : ""),
			s.getMark1(), s.getMark2(), s.getMark3(), s.getMark4(), s.getMark5(),
			s.getTotal(), s.getAverage(),
			escapeJson(s.getGrade() != null ? s.getGrade() : "")
		);
	}

	private static String jsonMsg(String key, String msg) {
		return "{\"" + key + "\":\"" + escapeJson(msg) + "\"}";
	}

	private static void respondJson(HttpExchange ex, int status, String body) throws IOException {
		ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		respond(ex, status, body);
	}

	private static void respond(HttpExchange ex, int status, String body) throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		ex.sendResponseHeaders(status, bytes.length);
		try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
	}

	public static void main(String[] args) throws Exception {
		new HttpApiServer(8080).start();
	}
}
