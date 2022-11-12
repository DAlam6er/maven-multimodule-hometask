package com.dmdev.service;

import com.dmdev.common.util.ConnectionPool;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppRunner
{
    private static final String FIND_DATABASE_NAMES_BY_TYPE = """
        SELECT table_name, table_schema
        FROM information_schema.tables
        WHERE table_type = ?
        """;

    public static void main(String[] args)
    {
        try {
            System.out.println(findAllTableNamesByType(TableType.BASE_TABLE));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, List<String>> findAllTableNamesByType(TableType tableType)
        throws SQLException
    {
        Map<String, List<String>> names = new HashMap<>();
        try (var connection = ConnectionPool.get();
             var preparedStatement =
                 connection.prepareStatement(FIND_DATABASE_NAMES_BY_TYPE))
        {
           preparedStatement.setObject(1, tableType.getTableTypeName());

            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var key = resultSet.getObject("table_schema", String.class);
                var value = resultSet.getObject("table_name", String.class);
                if (!names.containsKey(key)) {
                    names.put(key, new ArrayList<>());
                }
                names.get(key).add(value);
            }
            return names;
        }
    }
}
