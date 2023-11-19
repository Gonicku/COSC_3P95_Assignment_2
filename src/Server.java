import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.io.*;
import java.net.*;

public class Server {
    ForkJoinPool fjp;
    private ServerSocket ss;
    private ServerSideConnection[] files;
    private DataInputStream dataIn = null;
    private DataOutputStream dataOut = null;

    public Server(int portNumber){
        System.out.println("[Server]\n");

        try{
            ss = new ServerSocket(portNumber);
        } catch (IOException e){
            System.out.println("IOException from Server Constructor");
        }
    } // Server Constructor

    // Methods

    public void acceptConnections(int numOfFiles){
        try {
            int fileCount = 0;
            files = new ServerSideConnection[numOfFiles];
            System.out.println("Waiting for connections...");
            while(fileCount < numOfFiles){
                Socket s = ss.accept();
                System.out.println("[Server]\tFile #" + (fileCount + 1) + " has is being sent.");
                ServerSideConnection ssc = new ServerSideConnection(s, fileCount);
                files[fileCount] = ssc;
                Thread t = new Thread(ssc);
                t.start();
                fileCount++;
            }
        } catch (IOException e){
            System.out.println("IOException from acceptConnections()");
        }
    }

    private Integer countFiles(File directory){
        int numOfFiles = directory.list().length;
        //System.out.println("Number of files in folder: " + numOfFiles);

        return numOfFiles;
    }

    private void receiveFile(String fileName) throws IOException{
        int bytes = 0;
        FileOutputStream fos = new FileOutputStream(fileName);
        long size = dataIn.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1){
            fos.write(buffer, 0, bytes);
            size -= bytes;
        }
        System.out.println("[Server]\tFile received.");
        fos.close();
    }

    // Inner Class
    private class ServerSideConnection implements Runnable {
        private Socket socket;
        private int fileIndex = 0;

        public ServerSideConnection(Socket s, int index) {
            socket = s;
            fileIndex = index;
            try {
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                System.out.println("IOException from run() SSC");
            }
        } // ServerSideConnection Constructor

        @Override
        public void run() {
            try {
                String path = System.getProperty("user.dir") + "\\Test Files";
                //System.out.println(path);
                File selectedFile = new File(path);
                dataOut.writeInt(countFiles(selectedFile));
                //System.out.println(Arrays.toString(selectedFile.list()));
                dataOut.writeInt(fileIndex);
                receiveFile(selectedFile.list()[fileIndex]);
                dataOut.flush();

                /*while (true){

                }*/
            } catch (IOException e){
                System.out.println("IOException from run() SSC");
            }
        }
    } // ServerSideConnection Class

    public static void main(String[] args) throws IOException{
        System.out.println(args[0]);
        if (args.length != 1) {
            System.err.println("Needs a 'port number'.");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        int numOfFiles = 2;
        Server server = new Server(portNumber);
        server.acceptConnections(numOfFiles);
        System.out.println("[Server]\t" + numOfFiles + " files have been transferred. No longer accepting transfers");
    }
} // Server Class
