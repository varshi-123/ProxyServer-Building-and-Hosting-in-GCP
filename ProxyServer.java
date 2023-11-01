import java.io.*;
import java.net.*;
import java.util.UUID;

public class HW1Server {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java HW1Server portNumber");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Proxy server is running on port " + portNumber);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InetAddress clientAddress = clientSocket.getInetAddress();
            System.out.println("Client connected from " + clientAddress.getHostAddress());

            // Get input and output streams for client communication
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read the client's command
            String command = clientReader.readLine();
            System.out.println("Received command: " + command);

            // Ensure the target URL contains a protocol (e.g., "http://")
            if (!command.startsWith("GET ")) {
                clientWriter.println("HTTP/1.1 400 Bad Request");
                clientSocket.close();
                return;
            }

            String targetURL = extractTargetURL(command);

            // Create a unique identifier for the saved proxy file
            String fileId = UUID.randomUUID().toString();
            String fileName = extractHTMLFileName(targetURL);
            String localFileName = "proxy-" + fileName + "-" + fileId + ".html";

            // Create a new socket to connect to the remote server following HTTP protocol
            URL url = new URL(targetURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the connection
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Host", url.getHost());
            connection.connect();

            // Get the response from the remote server
            BufferedReader webReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            PrintWriter webWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            // Save a copy of the HTML file received from the web server
            FileWriter fileWriter = new FileWriter(localFileName);
            String responseLine;
            while ((responseLine = webReader.readLine()) != null) {
                fileWriter.write(responseLine + "\n");
                webWriter.println(responseLine);
            }
            fileWriter.close();
            System.out.println("Saved a copy as " + localFileName);

            // Close connections
            webReader.close();
            connection.disconnect();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String extractTargetURL(String command) {
        String[] parts = command.split(" ");
        if (parts.length >= 2) {
            return "http://" + parts[1]; // Ensure the URL has the "http://" protocol
        }
        return "";
    }

    private String extractHTMLFileName(String targetURL) {
        // Extract the file name from the target URL
        String[] parts = targetURL.split("/");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return "index.html";
    }
}
