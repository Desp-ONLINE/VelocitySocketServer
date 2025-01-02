package com.binggre.velocitysocketserver.utils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

import static com.binggre.velocitysocketserver.utils.VelocitySocketServer.CLOSE;
import static com.binggre.velocitysocketserver.utils.VelocitySocketServer.REQUEST;

public class SocketServerClient {

    private static final AtomicInteger generateId = new AtomicInteger(0);
    private final int id;
    private final Socket socket;

    private final BufferedReader reader;
    private final BufferedWriter writer;

    public SocketServerClient(Socket socket) throws IOException {
        this.socket = socket;
        this.id = generateId.incrementAndGet();
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
        new Thread(this::handleThread).start();
    }

    private void handleThread() {
        while (true) {
            try {
                String read = reader.readLine();
                if (read == null || read.isEmpty()) {
                    continue;
                }
                if (read.startsWith(CLOSE)) {
                    SocketServer.getInstance().close(this);

                } else if (read.startsWith(REQUEST)) {
                    read = read.replace(REQUEST, "");
                    String socketId = read.replaceAll("^(\\d+).*", "$1");
                    read = read.substring(socketId.length());

                    System.out.println("socketId = " + socketId);
                    System.out.println("read = " + read);
                    System.out.println("this.id = " + this.id);
                    SocketServer.getInstance().request(read, this.id, Integer.parseInt(socketId), socket.getPort());

                } else {
                    SocketServer.getInstance().send(read, this.id);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void send(String value) {
        try {
            writer.write(value + "\n");
            writer.flush();
        } catch (IOException ignored) {
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