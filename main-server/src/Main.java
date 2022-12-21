public class Main {
    public static void main(String[] args) {

        var tcpServer = new TCPServer(200, 1616, "Lorem Ipsum Dolar Sit, Amet LOTHAR Frank");
        tcpServer.start();
        var tcpServer1 = new TCPServer(10, 4242, "Lorem Ipsu");
        tcpServer1.start();
        var tcpServer2 = new TCPServer(200, 8181, "m Dolar Sit, Amet LOTHAR Frank");
        tcpServer2.start();
    }
}