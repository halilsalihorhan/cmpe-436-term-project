import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TCPClient {
    private String name;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    public TCPClient(String name) {
        this.name = name;
        try {
            socket = new Socket("localhost", 3232);
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void request() {
            try {
                if (socket.isClosed()) {
                    System.err.println("Socket is closed");
                    return;
                }
                sendRequest("");
                String message = readResponse();
                System.out.println(name + ": " + message);

            } catch (Exception e) {
                e.printStackTrace();
                closeConnection();
            }
    }

    private void closeConnection() {
        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
        } catch (Exception e) {

        }
    }
    private void sendRequest(String message) throws Exception {
        dataOutputStream.writeUTF(message);
        dataOutputStream.flush();
    }

    private String readResponse() throws IOException {
        var raw = dataInputStream.readUTF();
        var message = raw.split("\\|")[0];
        var isLast = Boolean.parseBoolean(raw.split("\\|")[1]);
        if (isLast) {
            closeConnection();
        }
        return message;
    }
}
