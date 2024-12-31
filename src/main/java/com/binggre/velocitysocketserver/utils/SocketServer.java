package com.binggre.velocitysocketserver.utils;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.binggre.velocitysocketserver.utils.VelocitySocketServer.RESPONSE;

public class SocketServer {

    private static SocketServer instance;
    private static Logger logger;

    public static synchronized SocketServer getInstance() {
        if (instance == null) {
            try {
                instance = new SocketServer(VelocitySocketServer.SOCKET_PORT);
                logger = VelocitySocketServer.getInstance().getLogger();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private final ServerSocket server;
    private final ConcurrentMap<Integer, SocketClient> connectedClients = new ConcurrentHashMap<>();
    private final ExecutorService clientPool = Executors.newCachedThreadPool();

    private SocketServer(int port) throws IOException {
        server = new ServerSocket(port);
        ExecutorService serverExecutor = Executors.newFixedThreadPool(2);
        serverExecutor.execute(() -> {
            while (!server.isClosed()) {
                try {
                    logger.info("Client Connecting...");
                    Socket clientSocket = server.accept();
                    SocketClient client = new SocketClient(clientSocket, clientPool);
                    connectedClients.put(client.getId(), client);
                    client.run();
                    logger.info("Connected client id : {}", client.getId());
                    System.out.println(connectedClients.size() + " clients connected.");
                } catch (IOException e) {
                    System.err.println("Error while accepting client: " + e.getMessage());
                }
            }
        });
    }

    public void send(String value, int exception) {
        for (SocketClient socket : connectedClients.values()) {
            if (socket.getId() != exception) {
                socket.send(value);
            }
        }
    }

    public void request(String value, int port, int exception) {
        String request = RESPONSE + port + value;
        for (SocketClient socket : connectedClients.values()) {
            if (socket.getId() != exception) {
                socket.send(request);
            }
        }
    }

    public void remove(int id) {
        connectedClients.remove(id);
    }

    public void close(SocketClient client) {
        client.close();
        remove(client.getId());
    }
}