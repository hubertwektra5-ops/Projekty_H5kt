import java.io.*;
import java.net.Socket;

public class WatekKlienta extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String login;

    public WatekKlienta(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        start();
    }

    public void run() {
        try {
            out.println("Podaj login:");
            login = in.readLine();
            System.out.println("Klient zalogowany jako: " + login);
            out.println("Witaj na czacie, " + login + "!");

            String msg;
            while ((msg = in.readLine()) != null) {
                System.out.println(login + ": " + msg);
                if (msg.equalsIgnoreCase("/quit")) break;
                // tymczasowo tylko echo:
                out.println("Echo: " + msg);
            }

            socket.close();
            System.out.println(login + " rozłączony.");
        } catch (IOException e) {
            System.err.println("Błąd klienta: " + e.getMessage());
        }
    }
}
