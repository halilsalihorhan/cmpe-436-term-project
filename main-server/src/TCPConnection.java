import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCPConnection extends Thread{
    private int pointer;
    private List<String> messages = new ArrayList<>();
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    int period = 1000;


    public TCPConnection(Socket socket, int period, String message) throws IOException {
        this.period = period;
        this.socket = socket;
        dataInputStream =  new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        var x = message.split("");
        messages.addAll(Arrays.asList(x));
    }

    @Override
    public void run() {
        super.run();
        try{
            while (true){
//                String message = readRequest();
                //
                pointer++;
                Thread.sleep(period);
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
