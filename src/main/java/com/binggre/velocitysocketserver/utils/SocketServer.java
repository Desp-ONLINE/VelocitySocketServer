package com.binggre.velocitysocketserver.utils;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
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
    private final ConcurrentMap<Integer, SocketServerClient> connectedClients = new ConcurrentHashMap<>();

    private SocketServer(int port) throws IOException {
        server = new ServerSocket(port);
        ExecutorService serverExecutor = Executors.newFixedThreadPool(2);
        serverExecutor.execute(() -> {
            while (!server.isClosed()) {
                try {
                    logger.info("Client Connecting...");
                    Socket clientSocket = server.accept();
                    SocketServerClient client = new SocketServerClient(clientSocket);
                    connectedClients.put(client.getId(), client);
                    client.run();
                    logger.info("Connected client id : {}", client.getId());
                    logger.info("Current connected count {}", connectedClients.size());
                    refreshConnectedList();
                } catch (IOException e) {
                    System.err.println("Error while accepting client: " + e.getMessage());
                }
            }
        });
    }

    private void refreshConnectedList() {
        Collection<SocketServerClient> values = connectedClients.values();
        List<Integer> refreshList = values.stream().map(SocketServerClient::getId).toList();

        StringBuilder refreshListString = new StringBuilder();
        for (Integer id : refreshList) {
            refreshListString.append(id);
        }

        for (SocketServerClient socket : values) {
            socket.send(VelocitySocketServer.REFRESH_CONNECT_LIST + refreshListString);
        }
        logger.info("Refresh connected list {}", refreshList);
    }

    public void send(String value, int exception) {
        for (SocketServerClient socket : connectedClients.values()) {
            if (socket.getId() != exception) {
                socket.send(value);
            }
        }
    }

    public void request(String value, int exception, int socketId, int port) {
        String request = RESPONSE + port + value;
        for (SocketServerClient socket : connectedClients.values()) {
            if (socket.getId() == exception) {
                continue;
            }
            if (socket.getId() == socketId) {
                socket.send(request);
            }
        }
    }

    public void remove(int id) {
        connectedClients.remove(id);
    }

    public void close(SocketServerClient client) {
        client.close();
        remove(client.getId());
        refreshConnectedList();
    }
}