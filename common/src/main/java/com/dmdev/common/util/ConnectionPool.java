package com.dmdev.common.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

@UtilityClass
public class ConnectionPool
{
    private static final String DB_KEY = "db.driver";
    private static final String URL_KEY = "db.url";
    private static final String USER_KEY = "db.user";
    private static final String PASSWORD_KEY = "db.password";
    private static final Integer DEFAULT_POOL_SIZE = 10;
    private static final String POOL_SIZE_KEY = "db.pool.size";

    private static BlockingQueue<Connection> pool;
    private static List<Connection> sourceConnections;

    static {
        loadDriver();
        initConnectionPool();
    }

    @SneakyThrows
    private static void loadDriver()
    {
        Class.forName(PropertiesUtil.get(DB_KEY));
    }

    private static void initConnectionPool()
    {
        var poolSize = PropertiesUtil.get(POOL_SIZE_KEY);
        var size = poolSize == null ? DEFAULT_POOL_SIZE : Integer.parseInt(poolSize);
        pool = new ArrayBlockingQueue<>(size);
        sourceConnections = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            var connection = open();

            var proxyConnection = (Connection) Proxy.newProxyInstance(
                ConnectionPool.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> method.getName().equals("close")
                    ? pool.add((Connection)proxy)
                    : method.invoke(connection, args)
            );
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    @SneakyThrows
    private static Connection open()
    {
        return DriverManager.getConnection(
            PropertiesUtil.get(URL_KEY),
            PropertiesUtil.get(USER_KEY),
            PropertiesUtil.get(PASSWORD_KEY)
        );
    }

    @SneakyThrows
    public static Connection get()
    {
        return pool.take();
    }

    @SneakyThrows
    public static void close()
    {
        for (Connection sourceConnection : sourceConnections) {
            sourceConnection.close();
        }
    }
}
