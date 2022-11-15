import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPConnection extends Thread{
    private int pointer;
    private List<String> messages = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    public TCPConnection(Socket socket) throws IOException {
        this.socket = socket;
        dataInputStream =  new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        messages.add("Hello from server");
        messages.add("How are you?");
        messages.add("I'm fine, thank you");
        messages.add("Goodbye");
        messages.add("See you later");
        messages.add("Bye");
    }

    @Override
    public void run() {
        super.run();
        try{
            while (true){
                String message = readRequest();
                //
                pointer++;
                sendResponse(messages.get(pointer - 1));
                if(pointer == messages.size()){
                    closeConnection();
                }
            }
        } catch (Exception e) {

        } finally {
           closeConnection();
        }
    }

    private void closeConnection() {
        try {
            socket.close();
            dataInputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(String message) throws IOException {
        var x = pointer == messages.size();
        dataOutputStream.writeUTF(message+"|"+ x);
        dataOutputStream.flush();
    }

    private String readRequest() throws IOException {
        return dataInputStream.readUTF();
    }
}
