import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class TCPConnection extends Thread{
    private Socket socket;
    private BufferedReader dataInputStream;
    private DataOutputStream dataOutputStream;

    private Map<Integer, RRect> shapes;
    private List<TCPConnection> threads;

    private Semaphore semaphores;

    private Object lock = new Object();
    private Object lock1 = new Object();

    private static Object acqLock = new Object();


    private void broadcast(String message) {
        for (TCPConnection thread : threads) {
            try {
                thread.sendResponse(message);
                if(message.equals("deleteShape|")) {
                    semaphores = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public TCPConnection(Socket socket, Map<Integer, RRect> shapes, List<TCPConnection> threads ) throws IOException {
        this.shapes = shapes;
        this.socket = socket;
        this.threads = threads;
        dataInputStream =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        initialData();
    }

    private void initialData() {
        var message = "init|";
        for (var shape : shapes.values()) {
            message += shape.id + "/" + shape.left + "/" + shape.top + "/" + shape.right + "/" + shape.bottom + "/" + shape.rotation + "#";
        }
        if (message.endsWith("#")) {
            message = message.substring(0, message.length() - 1);
        }
        try {
            sendResponse(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try{
            String line;
            while ((line = dataInputStream.readLine()) != null){
                readRequest(line);
            }
        } catch (Exception e) {
            System.err.println("Connection closed");
            e.printStackTrace();

        } finally {
           closeConnection();
        }
    }

    private void closeConnection() {
        try {
            if(semaphores != null) {
                semaphores.release();
                semaphores = null;
            }
            socket.close();
            dataInputStream.close();
            dataOutputStream.close();
            threads.remove(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResponse(String message) throws IOException {
        synchronized (lock1) {
            System.out.println("Sending: " + message);
            dataOutputStream.writeUTF(message);
            dataOutputStream.flush();
        }
    }

    private void readRequest(String data) throws IOException {
        synchronized (lock) {
            try {
                System.out.println("Received: " + data);

                data = data.substring(2);
                var payload = data.split("\\|");
                var command = payload[0];
                var args = Arrays.copyOfRange(payload, 1, payload.length);
                switch (command) {
                    case "createShape":
                        createShape(args);
                        break;
                    case "deleteShape":
                        deleteShape(args);
                        break;
                    case "modifyShape":
                        modifyShape(args);
                        break;
                    case "acquireShape":
                        acquireShape(args);
                        break;
                    case "releaseShape":
                        releaseShape(args);
                        break;
                    default:
                        sendResponse("Unknown command");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void releaseShape(String[] args) {
        try {
            if(Integer.parseInt(args[0]) == -1) return;
            var rect = shapes.get(Integer.parseInt(args[0]));
            rect.lock.release();
            semaphores = null;
            sendResponse("releaseShape|OK");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void acquireShape(String[] args) {
        try {
           synchronized (acqLock) {
               var rect = shapes.get(Integer.parseInt(args[0]));
               if (semaphores != null) {
                   semaphores.release();
                   semaphores = null;
               }
               if (rect.lock.tryAcquire()) {
                   semaphores = rect.lock;
                   sendResponse("acquireShape|true|" + rect.id);
               } else {
                   sendResponse("acquireShape|false|" + rect.id);
               }
           }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void modifyShape(String[] args) {

        var id = Integer.parseInt(args[0]);
        var left = Integer.parseInt(args[1]);
        var top = Integer.parseInt(args[2]);
        var right = Integer.parseInt(args[3]);
        var bottom = Integer.parseInt(args[4]);
        var rotation = Float.parseFloat(args[5]);
        if(id == -1) return;
        if(semaphores != shapes.get(id).lock) return;
        shapes.get(id).update(left, top, right, bottom, rotation, "");
        broadcast("modifyShape|" + id + "|" + left + "|" + top + "|" + right + "|" + bottom + "|" + rotation);

    }

    private void deleteShape(String[] args) {
        shapes.clear();
        broadcast("deleteShape|");
    }

    private void createShape(String[] args) {
        System.out.println("Creating shape");
        var id = Integer.parseInt(args[0]);
        var left = Integer.parseInt(args[1]);
        var top = Integer.parseInt(args[2]);
        var right = Integer.parseInt(args[3]);
        var bottom = Integer.parseInt(args[4]);
        var rotation = Float.parseFloat(args[5]);

        if(id == -1) {
            synchronized (TCPServer.generator) {
                id = TCPServer.generator++;
            }
            shapes.put(id, new RRect(id, left, top, right, bottom, rotation, "", new Semaphore(1)));
        }
        broadcast("createShape|"+id+"|"+left+"|"+top+"|"+right+"|"+bottom+"|"+rotation);
    }
}
