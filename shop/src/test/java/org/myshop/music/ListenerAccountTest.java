package org.myshop.music;

import org.example.database.DatabaseConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class ListenerAccountTest {
    @BeforeAll //wykonany przed kazdym testem
    public static void connectToDatabase() throws SQLException {
        DatabaseConnection.connect("songs.db"); //laczy sie do bazy danych
        ListenerAccount.Persistence.init(); //klasa pomocnicza, tworzy tabele jezeli potrzeba
    }

    @Test
    public void testRegister() throws SQLException {
        int register_id = ListenerAccount
                .Persistence.register("user1", "pass123"); //rejestruje uzytkownika, zwraca id
        assertNotEquals(0, register_id); //test udany jezeli nie jest rowne 0
    }
    @AfterAll //wykonywany po kazdym tescie
    public static void disconnectFromDatabase() {
        DatabaseConnection.disconnect(); //rozlacza sie z baza danych
    }
}

