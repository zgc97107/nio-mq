import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class IOTest {

    private static class Server {

        private ServerSocket socket;

        public static void main(String[] args) {
            new Server().start();
        }

        public Server() {
            try {
                socket = new ServerSocket(8888);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void start() {
            while (true) {
                try {
                    System.out.println("waiting for connection");
                    Socket clientSocket = socket.accept();
                    System.out.println("new connection: " + clientSocket.getRemoteSocketAddress());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String message = reader.readLine();
                    System.out.println("receive message: " + message);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                    bw.write("success");
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Receiver {

        public static void main(String[] args) {
            try {
                Socket socket = new Socket("127.0.0.1", 8888);
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String message = br.readLine();
                System.out.println("Receiver completed: " + message);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Sender {

        public static void main(String[] args) {
            try {
                Socket socket = new Socket("127.0.0.1", 8888);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF("hello");
                socket.close();
                System.out.println("Sender completed");
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
