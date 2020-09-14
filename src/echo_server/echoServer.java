package echo_server;

import java.net.InetSocketAddress;
import java.net.HttpURLConnection;
import java.io.*;
import java.util.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

public class echoServer {
    public static void main(String[] args) {
        try {
            // Bind to port 8080
        	int port = 8080;
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
 
            // Adding '/' context
            httpServer.createContext("/", new TestHandler());
 
            // Start the server
            httpServer.start();
            System.out.println("Server started on port " + port);
        } catch (IOException ex) {
            System.out.println("error");
        }
 
    }
 
    // Handler for '/test' context
    static class TestHandler implements HttpHandler {
 
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Serving the request");
            
            // Serve for POST requests only
            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
 
                try {
 
                    // REQUEST Headers
                    Headers requestHeaders = he.getRequestHeaders();
                    Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();
                    System.out.println(entries);
                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
 
                    // REQUEST Body
                    InputStream is = he.getRequestBody();
                    System.out.println(is.toString());
                    byte[] data = new byte[contentLength];
                    int length = is.read(data);
 
                    // RESPONSE Headers
                    Headers responseHeaders = he.getResponseHeaders();
 
                    // Send RESPONSE Headers
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, contentLength);
 
                    // RESPONSE Body
                    OutputStream os = he.getResponseBody();
 
                    os.write(data);
 
                    he.close();
 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
 
        }
    }
}

