package com.dmdev.service;

public enum TableType
{
    BASE_TABLE("BASE TABLE"),
    VIEW("VIEW"),
    FOREIGN_TABLE("FOREIGN TABLE"),
    LOCAL_TEMPORARY("LOCAL TEMPORARY");

    private final String tableTypeName;

    TableType(String tableTypeName)
    {
        this.tableTypeName = tableTypeName;
    }

    public String getTableTypeName()
    {
        return tableTypeName;
    }
}
