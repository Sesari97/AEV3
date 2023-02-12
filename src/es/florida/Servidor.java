package es.florida;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class Servidor {

	public static void main(String[] args) throws IOException {
		
		String host = "localhost";
		int port = 7777;
		InetSocketAddress directionTCPIP = new InetSocketAddress(host,port);
		int backlog = 0;
		HttpServer server = HttpServer.create(directionTCPIP, backlog);
		
		GestorHTTP gestorHTTP = new GestorHTTP();
		String rutaRespuesta = "/servidor";
		server.createContext(rutaRespuesta, (HttpHandler) gestorHTTP);
		
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
		server.setExecutor(threadPoolExecutor);
		
		infoLog.newFile();
		String timeStamp = new SimpleDateFormat("'Date: 'dd/MM/yyyy 'Hour: 'HH.mm.ss").format(new java.util.Date());
		infoLog.write("Date: " + timeStamp);
		infoLog.write("Host: " + host);
		infoLog.write("Port: " + port);
		infoLog.write("HttpServer: " + server);
		
		server.start();
		System.out.println("HTTP is in port " + port);

	}

}
