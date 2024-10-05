package dev.ilyasse.translator;

enum Error {
    NETWORK_ERROR(0),
    SERVER_ERROR(1),
    PARSING_ERROR(2),
    TOO_MANY_REQUESTS(3);

    private final int code;

    Error(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}