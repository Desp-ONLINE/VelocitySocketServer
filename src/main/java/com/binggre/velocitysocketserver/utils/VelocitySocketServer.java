package com.binggre.velocitysocketserver.utils;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

import static com.binggre.velocitysocketserver.BuildConstants.VERSION;

@Plugin(id = "velocitysocketserver", name = "VelocitySocketServer", version = VERSION)
public class VelocitySocketServer {

    private static VelocitySocketServer instance;
    public static final String REQUEST = "Request:";
    public static final String RESPONSE = "Response:";
    public static final String CLOSE = "Close:";
    public static final String REFRESH_CONNECT_LIST = "RefreshConnectList:";

    public static VelocitySocketServer getInstance() {
        return instance;
    }

    @Inject
    private Logger logger;

    public Logger getLogger() {
        return logger;
    }

    public static final int SOCKET_PORT = 1079;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        instance = this;
        SocketServer.getInstance();
    }
}
