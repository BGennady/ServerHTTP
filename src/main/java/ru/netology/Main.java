package ru.netology;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();

        // добавляем: метод, путь и (request и поток отправки клиенту)
        server.addHandler("GET", "/message", (request, outputStream) ->{
                    String response = "Ответ на запрос GET /messages";
            try {
                outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                outputStream.write(("Content-Length: " + response.length() + "\r\n").getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(response.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        server.addHandler("POST", "/messages", (request, outputStream) -> {
            // Код обработки POST запроса на /messages
            String response = "Ответ на запрос POST /messages";
            try {
                outputStream.write("HTTP/1.1 200 OK\r\n".getBytes());
                outputStream.write(("Content-Length: " + response.length() + "\r\n").getBytes());
                outputStream.write("\r\n".getBytes());
                outputStream.write(response.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
       server.start(9999);
    }
}
