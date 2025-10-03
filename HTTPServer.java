import java.io.*;
import java.net.*;

// Web server
public class HTTPServer {
  public static void main(String[] args) {
    // Define port
    int port = 8090;
    try {
      // Create server socket that listens on port
      ServerSocket serverSocket = new ServerSocket(port);  // Will detect incoming communication
      // While a server socket exists:
      while(true) {
        // Create client instance to facilitate that communication
        Socket clientSocket = serverSocket.accept();
      }
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
  }
}
