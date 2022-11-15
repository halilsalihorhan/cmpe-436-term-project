public class Main {
    public static void main(String[] args) {

        var tcpServer = new TCPServer();
        tcpServer.start();

        var tcpClient = new TCPClient("ALI");
        var tcpClient2 = new TCPClient("VELI");

        var thread = new Thread(() -> {
            for(int i = 0; i < 10; i++){
                tcpClient.request();
            }
        });


        thread.start();
        var thread2 = new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tcpClient2.request();
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tcpClient2.request();
        });
        thread2.start();
    }
}