package com.nymoo.afp.common.render.core;

/**
 * Исключение формата модели.
 * Выбрасывается при ошибках парсинга или некорректной структуре файла модели.
 */
public class ModelFormatException extends RuntimeException {
    private static final long serialVersionUID = 2023547503969671835L;

    public ModelFormatException() {
        super();
    }

    public ModelFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelFormatException(String message) {
        super(message);
    }

    public ModelFormatException(Throwable cause) {
        super(cause);
    }
}