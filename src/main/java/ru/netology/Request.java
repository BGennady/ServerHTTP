package ru.netology;

import java.util.Map;

// класс будет хранить информацию о запросе: метод, путь, заголовки и тело.
public class Request {
    private final String method;                 // HTTP метод (GET, POST и т.д.)
    private final String path;                   // путь запроса (например, /messages)
    private final Map<String, String> headers;   // заголовки запроса
    private final String body;                   // тело запроса (для POST и PUT запросов)

    public Request(String method, String path, Map<String, String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }
}
