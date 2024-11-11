package ru.netology;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // cписок допустимых путей
        List<String> validPaths = Arrays.asList(
                "/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css",
                "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js"
        );

        // объект сервера с допустимыми путями
        Server server = new Server(validPaths);

        // запуск сервера на порту 9999
        server.start(9999);
    }
}
