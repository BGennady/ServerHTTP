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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    // фиксированное количество потоков для обработки подключений
    private static final int THREAD_POOL_SIZE = 64;
    // список допустимых путей, которые сервер может обслуживать
    private final List<String> validPaths;
    // пул потоков, используемый для многопоточной обработки запросов
    private final ExecutorService threadPool;

    // Конструктор, инициализирует список допустимых путей и пул потоков
    public Server(List<String> validPaths) {
        this.validPaths = validPaths;
        // создаем пул потоков фиксированного размера для обработки запросов
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
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
            // читаем первую строку запроса (формат должен быть GET /path HTTP/1.1)
            String requestLine = in.readLine();
            // сплитуем строку по пробелам для извлечения метода, пути и версии HTTP
            String[] parts = requestLine.split(" ");
            // проверка, что запрос содержит три части (метод, путь и версию)
            if (parts.length != 3) {
                return;  // если нет, завершаю обработку
            }
            String path = parts[1]; // получаю путь из запроса
            // проверяю, существует ли запрашиваемый путь в списке допустимых
            if (!validPaths.contains(path)) {
                // ошибку 404 Not Found, если путь не найден
                out.write(("HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                out.flush();
                return;
            }

            // находим путь к файлу на диске
            Path filePath = Path.of(".", "public", path);
            // Определяем MIME-тип файла
            String mimeType = Files.probeContentType(filePath);

            // если запрашивается /classic.html, добавляем динамическое содержимое
            if (path.equals("/classic.html")) {
                // Читаем шаблон и заменяем {time} на текущее время
                String template = Files.readString(filePath);
                byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

                // отправка ответа с динамическим содержимым
                out.write(("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n").getBytes());
                out.write(content);
                out.flush();
                return;
            }

            // если запрос не к /classic.html, отправляем статический файл
            long length = Files.size(filePath);
            out.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: " + mimeType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());
            Files.copy(filePath, out); // копирует содержимое файла в выходной поток
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
