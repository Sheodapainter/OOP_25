package org.myshop.music;

import org.example.database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public record Song(String artist, String title, int duration){ //rekord
    public static class Persistence { //klasa pomocnicza do odczytania bazy danych
        public static Optional<Song> read(int index) throws SQLException { //odczyt piosenki na bazie indeksu
            String sql = "SELECT * FROM song WHERE id = ?;"; //komenda SQL
            PreparedStatement stmt = DatabaseConnection.getConnection().prepareStatement(sql); 
            stmt.setInt(1, index); //polaczenie z baza danych i ustawienie ? jako indeks
            ResultSet rs = stmt.executeQuery(); //wynik zapytania do zmiennej rs
            if (rs.next()) { //zwrocone zostalo cokolwiek
                String artist = rs.getString("artist");
                String title = rs.getString("title");
                int duration = rs.getInt("length"); //pobiera do zmiennych dane z okreslonych kolumn
                return Optional.of(new Song(artist, title, duration)); //nowy obiekt song w optional
            } else {
                return Optional.empty();
            }
        }
    }
}
