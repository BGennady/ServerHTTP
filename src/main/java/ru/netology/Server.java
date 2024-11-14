package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int THREAD_POOL_SIZE = 64; // количество потоков для обработки подключений
    private final ExecutorService threadPool; // пул потоков, используемый для многопоточной обработки запросов
    // Мапа для хранения Хэндлеров
    private final Map<String, Map<String, Handler>> handlers = new HashMap<>();

    // конструктор, инициализирует пул потоков
    public Server() {
        // пул потоков фиксированного размера для обработки запросов
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    // метод принимает: метод, путь и хандлер
    public void addHandler(String method, String path, Handler handler) {
        handlers
                .computeIfAbsent(method, k -> new HashMap<>())
                .put(path, handler);
    }

    // метод запускает сервер и начинает ожидать подключения по переданому порту
    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.printf("Сервер запущен на порту: %d%n", port);

            // цикл для ожидания новых подключений
            while (true) {
                // Принимаем новое подключение
                Socket clientSocket = serverSocket.accept();
                // Передаем обработку подключения в пул потоков
                threadPool.submit(() -> handleConnection(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод для обработки конкретного подключения
    private void handleConnection(Socket socket) {
        try (
                // поток для чтения данных от клиента
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // поток для отправки данных клиенту
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            // чтение первой строки запроса
            String requestLine = in.readLine();
            // сплитуем строку на части для извлечения метода, пути и версии HTTP
            String[] parts = requestLine.split(" ");
            // проверка, что запрос содержит три части (метод, путь и версию)
            if (parts.length != 3) {
                return;  // если нет, завершаю обработку
            }
            String method = parts[0];
            String path = parts[1];

            //  объекта запроса
            Request request = new Request(method, path, null, null);

            // объект хандлера
            Handler handler = findHandler(method, path);
            // проверяем, что не пустой
            if (handler != null) {
                handler.handle(request, out);
            } else {
                // Если не найден, отправляем ошибку 404
                out.write("HTTP/1.1 404 Not Found\r\n".getBytes());
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод для поиска хендлера из Мапы
    private Handler findHandler(String method, String path) {

        // ппытаемся получить из Мапы, хандлер для этого метода
        Map<String, Handler> methodHandlers = handlers.get(method);
        if (methodHandlers != null) {
            return methodHandlers.get(path); // получаем подмапу по ключу-путь
        }
        return null;
    }
}
