import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ChatServer {
    private static final int PORT = 5000;
    static Vector<WatekKlienta> klienci = new Vector<>();

    public static void main(String[] args) {
        System.out.println("Serwer czatu uruchomiony...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Nowy klient: " + socket.getInetAddress());
                WatekKlienta watek = new WatekKlienta(socket);
                klienci.add(watek);
            }
        } catch (IOException e) {
            System.err.println("Błąd serwera: " + e.getMessage());
        }
    }

    public static synchronized void broadcast(String message, WatekKlienta nadawca) {
        for (WatekKlienta klient : klienci) {
            if (klient != nadawca) {
                klient.wyslijWiadomosc(message);
            }
        }
    }

    public static synchronized void usunKlienta(WatekKlienta klient) {
        klienci.remove(klient);
    }
}
