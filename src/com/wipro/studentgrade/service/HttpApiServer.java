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
		server.createContext("/api/students", new StudentsHandler());
		server.setExecutor(null);
		System.out.println("HTTP API Server started on http://localhost:" + port);
		server.start();
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
			List<StudentBean> list = studentDAO.getAllStudents();
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (int i = 0; i < list.size(); i++) { StudentBean s = list.get(i); sb.append(studentToJson(s)); if (i < list.size() - 1) sb.append(','); }
			sb.append("]");
			respondJson(exchange, 200, sb.toString());
		}
		private void handlePost(HttpExchange exchange) throws IOException {
			String contentType = getHeader(exchange, "Content-Type");
			Map<String, String> data;
			if (contentType != null && contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
				String body = readBody(exchange.getRequestBody()); data = parseUrlEncoded(body);
			} else { String body = readBody(exchange.getRequestBody()); data = parseSimpleJson(body); }
			String name = data.getOrDefault("name", "");
			int m1 = parseInt(data.get("m1")), m2 = parseInt(data.get("m2")), m3 = parseInt(data.get("m3")), m4 = parseInt(data.get("m4")), m5 = parseInt(data.get("m5"));
			StudentBean bean = new StudentBean(name, m1, m2, m3, m4, m5);
			String result = gradeProcessor.generateGrade(bean);
			respondJson(exchange, 201, studentToJson(bean));
		}
	}

	private static void addCors(Headers headers) {
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
	}
	private static String getHeader(HttpExchange ex, String key) { List<String> values = ex.getRequestHeaders().get(key); return values == null || values.isEmpty() ? null : values.get(0); }
	private static String readBody(InputStream is) throws IOException { byte[] buf = is.readAllBytes(); return new String(buf, StandardCharsets.UTF_8); }
	private static Map<String, String> parseUrlEncoded(String body) throws IOException { Map<String, String> map = new HashMap<>(); if (body == null || body.isEmpty()) return map; for (String pair : body.split("&")) { String[] kv = pair.split("=", 2); String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8); String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : ""; map.put(k, v); } return map; }
	private static Map<String, String> parseSimpleJson(String json) { Map<String, String> map = new HashMap<>(); if (json == null) return map; String s = json.trim(); if (s.startsWith("{") && s.endsWith("}")) { s = s.substring(1, s.length() - 1).trim(); if (!s.isEmpty()) { String[] parts = s.split(","); for (String part : parts) { String[] kv = part.split(":", 2); if (kv.length == 2) { String k = strip(kv[0]); String v = strip(kv[1]); map.put(k, v); } } } } return map; }
	private static String strip(String s) { s = s.trim(); if (s.startsWith("\"") && s.endsWith("\"")) { return s.substring(1, s.length() - 1); } return s; }
	private static int parseInt(String v) { try { return Integer.parseInt(v); } catch (Exception e) { return -1; } }
	private static String escapeJson(String s) { if (s == null) return ""; StringBuilder out = new StringBuilder(s.length() + 16); for (int i = 0; i < s.length(); i++) { char c = s.charAt(i); switch (c) { case '"': out.append("\\\""); break; case '\\': out.append("\\\\"); break; case '\n': out.append("\\n"); break; case '\r': out.append("\\r"); break; case '\t': out.append("\\t"); break; default: if (c < 0x20) { out.append(String.format("\\u%04x", (int) c)); } else { out.append(c); } } } return out.toString(); }
	private static String studentToJson(StudentBean s) { String name = escapeJson(s.getName()); String grade = s.getGrade() == null ? "" : s.getGrade(); String id = s.getStudentId() == null ? "" : s.getStudentId(); return new StringBuilder().append('{').append("\"studentId\":\"").append(escapeJson(id)).append("\",").append("\"name\":\"").append(name).append("\",").append("\"mark1\":").append(s.getMark1()).append(',').append("\"mark2\":").append(s.getMark2()).append(',').append("\"mark3\":").append(s.getMark3()).append(',').append("\"mark4\":").append(s.getMark4()).append(',').append("\"mark5\":").append(s.getMark5()).append(',').append("\"total\":").append(s.getTotal()).append(',').append("\"average\":").append(s.getAverage()).append(',').append("\"grade\":\"").append(escapeJson(grade)).append("\"").append('}').toString(); }
	private static String jsonMsg(String key, String msg) { return "{\"" + key + "\":\"" + escapeJson(msg) + "\"}"; }
	private static void respondJson(HttpExchange ex, int status, String body) throws IOException { ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8"); respond(ex, status, body); }
	private static void respond(HttpExchange ex, int status, String body) throws IOException { byte[] bytes = body.getBytes(StandardCharsets.UTF_8); ex.sendResponseHeaders(status, bytes.length); try (OutputStream os = ex.getResponseBody()) { os.write(bytes); } }

	public static void main(String[] args) throws Exception { new HttpApiServer(8081).start(); }
}
