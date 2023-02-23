public class Main {
    public static void main(String[] args) {
        var tcpServer = new TCPServer(1616);
        tcpServer.start();
    }
}