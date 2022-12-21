import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends Thread {
    public List<Thread> threads = new ArrayList<>();

    int period = 1000;
    int port = 3232;

    String message;

    public TCPServer(int period, int port, String message) {
        this.message = message;
        this.period = period;
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
                    var thread = new TCPConnection(socket, period, message);
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
