package echo_server;

import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.Inet6Address;
import java.io.*;
import java.util.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;
import java.sql.*;

public class echoServer {
    public static void main(String[] args) {
        try {
            // Bind to port 8080
        	int port = 8080;
        	
        	String ip = findIP();
        	        	
            //Initialises HTTP Server
        	HttpServer httpServer = HttpServer.create(new InetSocketAddress(ip, port), 0);
 
            // Adding '/' context
            httpServer.createContext("/", new RequestHandler());
            // Start the server
            httpServer.start();
            System.out.println("Server started on port " + ip + ":" + port);
        } catch (IOException ex) {
            System.out.println("Error could not start server");
        }
 
    }
 
    private static class RequestHandler implements HttpHandler {
 
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
                    
                    String res = dbInsert(new String(data));
                    
                    String response = "RESPONSE: " + HttpURLConnection.HTTP_OK + " POST" + "\n" + 
                    		"RESPONSE_BODY: " + res + '\n';
                    
                    os.write(response.getBytes());
 
                    he.close();
                    
 
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Serve GET requests
            else if (he.getRequestMethod().equalsIgnoreCase("GET")) {
            	
            	try {
            		System.out.println("GET request received");
            		
    				he.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
    				
    				OutputStream os = he.getResponseBody();
                    
    				//Get from SQL database here...
    				
    				String res = dbGet();
    				
                    String response = "RESPONSE: " + HttpURLConnection.HTTP_OK + " GET" + "\n" + 
                    		"RESPONSE_BODY: " + res + "\n";
                    
                    os.write(response.getBytes());

                    he.close();

    				
    			} catch (Exception e) {
    				e.printStackTrace();
    			}	
            }
        }
    }
    
    private static String dbInsert(String str) {
    	String[] strArr = str.split("&");

        Statement stmt = null;
        
        Connection conn = null;
        
        String res = "";
        
        try {
           try {
              Class.forName("com.mysql.jdbc.Driver");
           } catch (Exception e) {
              System.out.println(e);
        }
           
        conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/db1?useSSL=false", "root", "password");
        
        System.out.println("MYSQL Connection is created successfully:");
        
        stmt = (Statement) conn.createStatement();
        
        for (String data: strArr) {
        	String[] arr = data.split("=");
        	
        	String key = arr[0];
        	
        	String value = arr[1];
        	
        	String query;
        	
        	List<String> keyID = new ArrayList<String>();
        	
        	ResultSet rs = stmt.executeQuery("SELECT key_id FROM hashmap");
        	
        	while (rs.next()) {
        		keyID.add(rs.getString("key_id"));
        	}
        	
        	if (!keyID.contains(key)) {
        		query = String.format("INSERT INTO hashmap " + "VALUES ('%s', '%s')", key, value);
                
        		stmt.executeUpdate(query);
        	} else {
        		query = String.format("UPDATE hashmap " + "SET value = '%s'" + "WHERE key_id = '%s' ", value, key);

        		stmt.executeUpdate(query);
        	}
        }
                
        ResultSet responseBody = stmt.executeQuery("SELECT * FROM hashmap");
        
        while (responseBody.next()) {
        	res += responseBody.getString("key_id") + "=" + responseBody.getString("value") + "&";
        }
        
        System.out.println("Record is inserted in the table successfully..................");
        
        stmt.close();
        
        } catch (SQLException excep) {
        	
           excep.printStackTrace();
           
        } catch (Exception excep) {
        	
           excep.printStackTrace();
           
        } finally {
           try {
              if (stmt != null)
                 conn.close();
           } catch (SQLException se) {}
           try {
              if (conn != null)
                 conn.close();
           } catch (SQLException se) {
              se.printStackTrace();
           }  
        }
        
        return res;
    }
    
    private static String dbGet() {
    	String result = "";
    	
        try
        {
          // create our mysql database connection
          String myDriver = "org.gjt.mm.mysql.Driver";
          Class.forName(myDriver);
          Connection conn = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:3306/db1?useSSL=false", "root", "password");

          
          // our SQL SELECT query. 
          // if you only need a few columns, specify them by name instead of using "*"
          String query = "SELECT * FROM hashmap";

          // create the java statement
          Statement st = conn.createStatement();
          
          // execute the query, and get a java resultset
          ResultSet rs = st.executeQuery(query);
          
          // iterate through the java resultset
          while (rs.next()) {
          	result += rs.getString("key_id") + "=" + rs.getString("value") + "&";
          }
          
          st.close();
          
        }
        catch (Exception e)
        {
          System.err.println("Got an exception! ");
          System.err.println(e.getMessage());
        }
        
        return result;
    }
    
    //Picks out IPV4 Address
	
    static String findIP() throws SocketException {

    	String ip = null;
    	
    	//Checks all network interfaces
    	Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    	
 
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            // filters out 127.0.0.1 and inactive interfaces
            if (iface.isLoopback() || !iface.isUp())
                continue;

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while(addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                //continues loop if IPV6 else assigns VAR ip to IPV4"
                if (addr instanceof Inet6Address) continue;

                ip = addr.getHostAddress();
            }
        }
        
        return ip;
    }
    
    
}

