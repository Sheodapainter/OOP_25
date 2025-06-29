package pl.umcs.oop.powtorzeniegui.server;

import javafx.scene.paint.Color;
import pl.umcs.oop.powtorzeniegui.Dot;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server extends Thread {
    private static final List<ClientThread> clients = new CopyOnWriteArrayList<>(); //lista polaczonych uzytkownikow
    private static Connection dbConnection; //polaczenie z baza danych

    public static void main(String[] args) {
        try(ServerSocket serverSocket = new ServerSocket(5000)){ //nasluchuje na porcie
            connectToDatabase();
            while (true) {
                System.out.println("Czekam na połączenie");
                Socket accepted = serverSocket.accept(); // Czekaj na klienta
                System.out.println("Zaakceptowano: " + accepted);
                ClientThread ct = new ClientThread(accepted); //nowy watek obslugi klienta
                clients.add(ct);
                ct.start();
                System.out.println("Wysyłanie punktów...");
                for (Dot point : Server.getSavedDots()) {
                    ct.send(point.toMessage());
                } //wysyla zapisane punkty do nowego klienta
            }
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Dot> getSavedDots() {
        List<Dot> savedDots = new ArrayList<>();
        try {
            String sql = "SELECT * FROM dot;";
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql); //wybiera wszystkie kropki z bazy danych

            while (rs.next()) { //wykonuje dla każdego obiektu bazy
                double x = rs.getDouble("x");
                double y = rs.getDouble("y");
                Color color = Color.valueOf(rs.getString("color"));
                double radius = rs.getDouble("radius");

                savedDots.add(new Dot(x, y, radius, color)); //zrobienie obiektu dot
            }

        } catch (SQLException e) {
            System.err.println("Błąd oczytu z bazy: "+e.getMessage());
        }

        return savedDots; //zwraca liste kropek
    }

    private static void saveDot(Dot point) {
        try {
            String sql = "INSERT INTO dot(x, y, color, radius) VALUES (?, ?, ?, ?);"; //? to zmienne
            PreparedStatement stmt = dbConnection.prepareStatement(sql);
            stmt.setDouble(1, point.x());
            stmt.setDouble(2, point.y());
            stmt.setString(3, point.color().toString());
            stmt.setDouble(4, point.radius()); //zapisuje dane do zmiennych
            stmt.executeUpdate(); //wykonuje
        } catch (SQLException e) {
            System.err.println("Błąd zapisu do bazy: "+e.getMessage());
        }
    }

    private static void connectToDatabase() throws SQLException{
        // Uwaga! Wymaga dodania zależności sqlite-jdbc od org.xerial do pom.xml
        dbConnection = DriverManager.getConnection("jdbc:sqlite:server.db"); //tworzy nowa baze lub uzywa istniejacej
        String drop = "DROP TABLE IF EXISTS dot;";
        String create = """
                CREATE TABLE dot(
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                color TEXT NOT NULL,
                radius INTEGER NOT NULL
                );
                """;
        Statement stmt = dbConnection.createStatement();
        stmt.executeUpdate(drop);       // czyszczę bazę, żeby nie mieć rysunków po poprzedniej sesji serwera
        stmt.executeUpdate(create);        //dodaje do bazy

        System.out.println("Serwer połączył się z bazą");
    }

    public static void broadcast(String message) {
        Dot point = Dot.fromMessage(message);
        saveDot(point); //zapis do bazy
        for (ClientThread client : clients) {
            client.send(message);
        } //wysyla do kazdego wiadomosc o kropce
    }

    public static void removeClient(ClientThread client) {
        System.out.println("Usuwam klienta: "+client.getSocket());
        clients.remove(client); //usuwa klienta z listy po rozlaczeniu
    }
}
