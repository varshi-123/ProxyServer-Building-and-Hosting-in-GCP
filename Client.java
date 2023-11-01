import java.io.*;
import java.net.*;

public class HW1Client {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java HW1Client host portNumber");
            System.exit(1);
        }

        String serverHost = args[0];
        int serverPort = Integer.parseInt(args[1]); // Fix: Added closing parenthesis

        try (Socket socket = new Socket(serverHost, serverPort)) {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Get user input for the target file's URL
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Enter a command with the target file's URL (e.g., GET example.com/index.html): ");
            String command = userInput.readLine();

            // Send the user's command to the proxy server
            out.println(command);

            // Create a file to save the response
            String localFileName = getLocalFileName(command);
            File localFile = new File(localFileName);
            FileWriter localFileWriter = new FileWriter(localFile);
            BufferedWriter bufferedFileWriter = new BufferedWriter(localFileWriter);

            long startTime = System.currentTimeMillis(); // Record the start time

            String responseLine;
            while ((responseLine = in.readLine()) != null) {
                bufferedFileWriter.write(responseLine);
            }

            long endTime = System.currentTimeMillis(); // Record the end time
            long downloadTime = endTime - startTime; // Calculate the download time

            bufferedFileWriter.close();

            System.out.println("Downloaded data saved as " + localFileName);
            System.out.println("Download time: " + downloadTime + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getLocalFileName(String command) {
        String[] parts = command.split(" ");
        if (parts.length >= 2) {
            String target = parts[1];
            String[] targetParts = target.split("/");
            if (targetParts.length > 0) {
                String fileName = targetParts[targetParts.length - 1];
                return fileName;
            }
        }
        return "downloaded.html"; // Default name if parsing fails
    }
}
