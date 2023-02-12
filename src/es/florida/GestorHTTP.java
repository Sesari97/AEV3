package es.florida;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

import java.util.ArrayList;

import java.util.Base64;


import org.json.JSONException;
import org.json.JSONObject;
import com.google.protobuf.TextFormat.ParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class GestorHTTP implements HttpHandler {

	public ArrayList<String> id = new ArrayList<String>();
	public ArrayList<String> alias = new ArrayList<String>();
	public ArrayList<String> name = new ArrayList<String>();
	public ArrayList<String> birth_date = new ArrayList<String>();
	public ArrayList<String> nationality = new ArrayList<String>();
	public ArrayList<String> image = new ArrayList<String>();
	public boolean fields = true;

	private String handleGetRequest(HttpExchange exchange) {
		String parameter = (exchange.getRequestURI().toString().split("\\?")[1].split("/")[1]);
		if (parameter.equals("showAll")) {
			System.out.println("Parameter entered correctly");
			return exchange.getRequestURI().toString().split("\\?")[1].split("/")[1];
		} else {
			System.out.println("Error");
		}
		return parameter;
	}

	private void handleGETRespose(HttpExchange exchange) throws IOException {
		String line = "";
		OutputStream outputStream = exchange.getResponseBody();
		for (String string : alias) {
			String text = ("<li>" + string + "</li>");
			line += text;
		}
		String htmlResponse = "<html><body><h1>Alias delinquents: </h1><ul>" + line + "</ul></body></html>";
		exchange.sendResponseHeaders(200, htmlResponse.length());

		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		infoLog.write("hadleGETResponse > Returns response HTML: " + htmlResponse);
		outputStream.close();
	}

	private void otherHandleResponse(HttpExchange exchange, String aliasName) throws IOException {
		System.out.println(aliasName);
		OutputStream outputStream = exchange.getResponseBody();
		String queryName = null;
		String birth_dateQuery = null;
		String queryNacionality = null;
		String queryImage = null;

		for (String string : alias) {
			if (string.equals(aliasName)) {
				int position = alias.indexOf(aliasName);
				queryName = name.get(position);
				birth_dateQuery = birth_date.get(position);
				queryNacionality = nationality.get(position);
				queryImage = image.get(position);
			}
		}
		String htmlResponse = "<html><body><h1>Information about delinquent: </h1><h2>Name: " + queryName
				+ "</h2><h2>Birth Date: " + birth_dateQuery + "</h2><h2>Nationality: " + queryNacionality
				+ "</h2><h2>Image: <br><img src=" + queryImage + " height='200' width='200'></h2></body></html>";
		exchange.sendResponseHeaders(200, htmlResponse.length());
		outputStream.write(htmlResponse.getBytes());
		outputStream.flush();
		infoLog.write("handleGETResponse > returns response HTML: " + htmlResponse);
	}

	private String handlePostRequest(HttpExchange exchange) throws InterruptedException, IOException {
		System.out.println("Received URI type POST " + exchange.getRequestBody().toString());
		InputStream inputStream = exchange.getRequestBody();
		InputStreamReader isr = new InputStreamReader(inputStream);
		BufferedReader br = new BufferedReader(isr);
		StringBuilder sb = new StringBuilder();
		String line;

		try {
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String postRequest = sb.toString();
		return postRequest;
	}

	private void handlePOSTResponse(HttpExchange exchange, String requestParamValue)
			throws IOException, ClassNotFoundException, JSONException, InstantiationException, IllegalAccessException {

		BufferedReader br = new BufferedReader(new StringReader(requestParamValue));
		String line, lineJson = "";
		while ((line = br.readLine()) != null)
			lineJson += line;
		br.close();

		JSONObject jsO = new JSONObject(lineJson);
		String aliasJSON = jsO.getString("alias");
		String nameJSON = jsO.getString("name");
		String birth_dateJSON = jsO.getString("birth_date");
		String nacionalityJSON = jsO.getString("nationality");
		String imageJSON = jsO.getString("image");
		String image64 = imageDeco(imageJSON);

		Connection c = null;
		java.sql.Statement stmt = null;
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			c = DriverManager.getConnection("jdbc:mysql://localhost:3306/carcel", "root", "");
			c.setAutoCommit(false);
			System.out.println("Opened Database successfully");

			stmt = c.createStatement();
			String sql = "INSERT INTO delinquents (alias,name,birth_date,nationality,image) " + "VALUES (" + "'"
					+ aliasJSON + "'" + "," + "'" + nameJSON + "'" + "," + "'" + birth_dateJSON + "'" + "," + "'"
					+ nacionalityJSON + "'" + "," + "'" + image64 + "'" + ")/*;";
			stmt.executeUpdate(sql);
			stmt.close();
			c.commit();
			c.close();

		} catch (Exception e) {
			System.out.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		System.out.println("Records created successfully");
	}

	public static String imageDeco(String img) throws IOException {
		String encodedUri = Base64.getUrlEncoder().encodeToString(img.getBytes());
		return encodedUri;
	}

	public static String imageDecoText(String cadena) throws IOException {
		byte[] decodedBytes = Base64.getUrlDecoder().decode(cadena);
		String decodeUri = new String(decodedBytes);
		return decodeUri;
	}

	public void handle(HttpExchange exchange) throws IOException {
		try {
			leerBD();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String requestParamValue = null;

		if ("GET".equals(exchange.getRequestMethod())) {
			requestParamValue = handleGetRequest(exchange);
			if (requestParamValue.equals("showAll")) {
				handleGETRespose(exchange);
			} else {
				String comprobador = requestParamValue.split("=")[0];
				for (String string : alias) {
					if (comprobador.equals(string)) {
						otherHandleResponse(exchange, comprobador);
					}
				}
			}
		} else if ("POST".equals(exchange.getRequestMethod())) {
			try {
				requestParamValue = handlePostRequest(exchange);
				System.out.println(requestParamValue);
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
			try {
				try {
					handlePOSTResponse(exchange, requestParamValue);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void leerBD() throws IOException, ParseException {
		alias.clear();
		name.clear();
		birth_date.clear();
		nationality.clear();
		image.clear();
		id.clear();

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/carcel", "root", "");
			java.sql.Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM delinquents");
			try {
				while (rs.next()) {
					System.out.println(rs.getNString(1) + " " + rs.getString(2) + " " + rs.getString(3) + " "
							+ rs.getString(4) + " " + rs.getString(5) + " " + rs.getString(6));
					alias.add(rs.getString(1));
					name.add(rs.getString(2));
					birth_date.add(rs.getString(3));
					nationality.add(rs.getString(4));
					String imagen64String = imageDeco(rs.getString(5));
					image.add(imagen64String);
					id.add(rs.getString(6));

				}
			} catch (Exception e) {
				while (rs.next()) {
					System.out.println(rs.getString(1));
				}
			}
			rs.close();
			stmt.close();
			con.close();
		} catch (Exception e) {
			System.out.println("You have not entered a correct parameter");
		}

	}

}
