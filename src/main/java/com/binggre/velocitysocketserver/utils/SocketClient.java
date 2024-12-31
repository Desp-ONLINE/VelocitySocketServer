package com.binggre.velocitysocketserver.utils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static com.binggre.velocitysocketserver.utils.VelocitySocketServer.REQUEST;

public class SocketClient {

    private static final AtomicInteger generateId = new AtomicInteger(0);
    private final int id;
    private final Socket socket;

    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final ExecutorService executor;

    public SocketClient(Socket socket, ExecutorService clientPool) throws IOException {
        this.socket = socket;
        this.id = generateId.incrementAndGet();
        this.executor = clientPool;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }

    public Socket getSocket() {
        return socket;
    }

    public int getId() {
        return id;
    }

    public void run() {
        executor.execute(() -> {
            while (true) {
                try {
                    String read = reader.readLine();
                        // 씨발 왜 안 되냐고
                    if (read.startsWith(REQUEST)) {
                        read = read.replace(REQUEST, "");
                        SocketServer.getInstance().request(read, socket.getPort(), this.id);

                    } else {
                        SocketServer.getInstance().send(read, this.id);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

    public void send(String value) {
        try {
            writer.write(value + "\n");
            writer.flush();
        } catch (IOException e) {
            SocketServer.getInstance().close(this);
        }
    }

    public void close() {
        try {
            writer.close();
        } catch (IOException ignored) {

        }
        try {
            reader.close();
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}