import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TCPServer extends Thread {
    public List<TCPConnection> threads = new ArrayList<>();
    public Map<Integer, RRect> shapes = new HashMap<>();
    int port = 3232;

    public static Integer generator = 0;

    public TCPServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {

            while (true) {
                try {
                    var socket = serverSocket.accept();
                    var thread = new TCPConnection(socket, shapes, threads);
                    thread.start();
                    threads.add(thread);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
