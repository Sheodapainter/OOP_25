package pl.umcs.oop.powtorzeniegui.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientThread extends Thread {
    private final Socket socket; //polaczenie z klientem
    private final PrintWriter out; //wysylanie danych do klienta
    private final BufferedReader in; //odbior danych od klienta

    public ClientThread(Socket socket) throws IOException {
        this.socket = socket; //przyjmuje ostatnie gniazdo
        out = new PrintWriter(socket.getOutputStream(), true); //wysyla wiadomosci z autoflush
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //odbiera tekst od klienta
    }

    public void send(String message) {
        out.println(message);
    } //wysyla wiadomosc do klienta

    public void run() {
        System.out.println(socket+": wątek wystartował! Czytanie...");
        try {
            String message;
            while ((message = in.readLine()) != null){ //wczytuje dane linia po linii
                System.out.println("Otrzymano: "+message); //wypisuje na serwerze wiadomosc
                Server.broadcast(message); //rozsyla wiadomosc do innych
            }

        } catch (IOException e) {
            System.err.println(socket + ": " + e.getMessage()); //wypisuje blad
        } finally {
            try {
                socket.close(); //zamyka gniazdo
            } catch (IOException ignored) {}
            Server.removeClient(this); //usuwa klienta
        }
    }

    public Socket getSocket() {
        return socket;
    } //zwraca polaczenie
}
