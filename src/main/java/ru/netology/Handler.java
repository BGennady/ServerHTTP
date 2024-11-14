package ru.netology;

import java.io.BufferedOutputStream;

// функциональный интерфейс для обработки запроса
@FunctionalInterface
public interface Handler {
    // метод для обработки запроса
    void handle (Request request, BufferedOutputStream outputStream);
    }
