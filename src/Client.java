import java.io.*;
import java.net.*;
public class Client {
    private ClientSideConnection csc;
    private DataInputStream dataIn = null;
    private DataOutputStream dataOut = null;

    private class ClientSideConnection{
        private Socket socket;
        private File file;

        public ClientSideConnection(int portNumber){
            System.out.println("[Client]");
            try {
                socket = new Socket("localhost", portNumber);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
                System.out.println("Connected to server.");
            } catch (IOException e) {
                System.out.println("IO Exception from CSC Constructor");
            }
        } // ClientSideConnection Constructor
    } // ClientSideConnection Class

    // Methods

    public void connectToServer(int portNumber) {
        csc = new ClientSideConnection(portNumber);
    }

    public void sendFile(String path) throws IOException {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);
        dataOut.writeLong(file.length());
        byte[] buffer = new byte[4 * 1024];
        while((bytes = fis.read(buffer)) != -1){
            dataOut.write(buffer, 0, bytes);
            dataOut.flush();
        }
        fis.close();
    }

    public static void main(String[] args) throws IOException{
        // If a host name and/or port number are not entered
        if (args.length != 2) {
            System.err.println("Needs a 'host name' and 'port number'.");
            System.exit(1);
        }

        //String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Client c = new Client();
        c.connectToServer(portNumber);
        int fileNum = c.dataIn.readInt();
        int fileIndex = c.dataIn.readInt();
        String directory = System.getProperty("user.dir") + "\\Test Files";
        String fileName = new File(directory).list()[fileIndex];
        System.out.println("[Client] File Count: " + fileNum + ", File Index: " + fileIndex);
        System.out.println("[Client] Chosen File: " + fileName + "\n[Client] Sending " + fileName +
                "to the server...");
        c.sendFile(directory + "\\" + fileName);
    }
} // Client Class
