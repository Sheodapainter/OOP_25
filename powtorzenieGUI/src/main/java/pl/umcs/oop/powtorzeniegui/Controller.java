package pl.umcs.oop.powtorzeniegui;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import pl.umcs.oop.powtorzeniegui.client.ServerThread;


import java.io.IOException;

public class Controller {
    @FXML
    private TextField addressField; //adres IP
    @FXML
    private TextField portField; //port serwera
    @FXML
    private ColorPicker colorPicker; //wybor koloru
    @FXML
    private Canvas canvas; //kanwa
    @FXML
    private Slider radiusSlider; //slider promienia okregu
    //elementy GUI z pliku view

    private ServerThread serverThread;

    @FXML
    private void onStartServerClicked() {
        System.out.println("Serwer jest niezależnym programem. Uruchom z klasy Server.");
    } //powiadomienie, serwer nie uruchamia sie z aplikacji

    @FXML
    private void onConnectClicked() {
        if (serverThread != null && serverThread.isAlive()) {
            System.err.println("Już połączono!");
            return; //nic nie robi jezeli serwer juz dziala
        }
        String address = addressField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim()); //pobiera dane z pol tekstowych
        try {
            //tworzy i uruchamia nowy watek z tych pol
            ServerThread newThread = new ServerThread(address, port);
            //funkcja rysujaca wykonywana po kliknieciu

            newThread.setDrawFunction(dot -> {
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setFill(dot.color());
                gc.fillOval(dot.x() - dot.radius(), dot.y() - dot.radius(), dot.radius() * 2, dot.radius() * 2);
            });

            newThread.setDaemon(true); //nie blokuje zamkniecia programu
            newThread.start();
            this.serverThread = newThread;
            System.out.println("Połączono z serwerem!");
        } catch (IOException e) {
            System.err.println("Błąd połączenia z serwerem: " + e.getMessage());
        }
    }

    @FXML
    public void onMouseClicked(MouseEvent event) {
        //pobiera wspolzedne klikniecia
        double x = event.getX();
        double y = event.getY();
        //pobiera wartosci z odpowiednich pol
        double radius = radiusSlider.getValue();
        Color color = colorPicker.getValue();

        //tworzy nowy dot z danymi wartosciami
        Dot point = new Dot(x, y, radius, color);
        //wysyla punkt do serwera jesli polaczono
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.send(point);
        } else {
            System.err.println("Połącz się z serwerem!");
        }

    }
}
