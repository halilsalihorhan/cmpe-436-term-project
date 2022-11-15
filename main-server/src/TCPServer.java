import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer extends Thread {
    public List<Thread> threads = new ArrayList<>();



    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(3131);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {

            while (true) {
                try {
                    var socket = serverSocket.accept();
                    var thread = new TCPConnection(socket);
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
