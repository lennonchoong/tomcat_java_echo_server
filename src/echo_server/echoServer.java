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
 
    static class TestHandler implements HttpHandler {
 
        @Override
        public void handle(HttpExchange he) throws IOException {
            System.out.println("Serving request");
            
            // Serve for POST requests only
            if (he.getRequestMethod().equalsIgnoreCase("POST")) {
 
                try {
 
                    // REQUEST Headers
                    Headers requestHeaders = he.getRequestHeaders();
                    Set<Map.Entry<String, List<String>>> entries = requestHeaders.entrySet();
                    int contentLength = Integer.parseInt(requestHeaders.getFirst("Content-length"));
                    
                    // REQUEST Body
                    InputStream is = he.getRequestBody();
                    byte[] data = new byte[contentLength];
                    int length = is.read(data);
                    
                    // RESPONSE Headers
                    Headers responseHeaders = he.getResponseHeaders();
                    
                    // Send RESPONSE Headers
                    he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    
                    // RESPONSE Body
                    OutputStream os = he.getResponseBody();
                    
                    String response = "Return response: " + new String(data) + "\n" + 
                    		"Response Code: " + HttpURLConnection.HTTP_OK + '\n';
                    
                    os.write(response.getBytes());
 
                    he.close();
 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
 
        }
    }
}

