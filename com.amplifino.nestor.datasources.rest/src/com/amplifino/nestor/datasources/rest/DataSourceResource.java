package com.amplifino.nestor.datasources.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
public class DataSourceResource {

    @Inject
    private DataSourceApplication dataSourceApplication;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<DataSourceInfo> dataSourceNames() {
        return dataSourceApplication.dataSourceNames().stream()
            .map(DataSourceInfo::new)
            .collect(Collectors.toList());
    }

    @GET
    @Path("{dataSourceName}/tables/{tableName}/describe")
    @Produces(MediaType.APPLICATION_JSON)
    public Response describeTable(@PathParam("dataSourceName") String dataSourceName, @PathParam("tableName") String tableName) {
        return
            dataSourceApplication
                .dataSource(dataSourceName)
                .map(dataSource -> this.describe(dataSource, tableName))
                .orElseThrow(NotFoundException::new);
    }

    private Response describe(DataSource dataSource, String tableName) {
        try {
            SqlResult result =
                toSqlResult(dataSource, c -> c.getMetaData().getColumns(null, null, tableName.toUpperCase(), "%"));
            return Response.ok().entity(result).build();
        } catch (SQLException e) {
            return badRequestResponse(e);
        }
    }

    private Response badRequestResponse(Exception e) {
        e.printStackTrace();
        return Response.status(Status.BAD_REQUEST)
            .entity(e.toString())
            .type("text/plain")
            .build();
    }

    @GET
    @Path("{dataSourceName}/tables/{tableName}/keys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tableKeys(@PathParam("dataSourceName") String dataSourceName, @PathParam("tableName") String tableName) {
        return
            dataSourceApplication
                .dataSource(dataSourceName)
                .map(dataSource -> this.keys(dataSource, tableName))
                .orElseThrow(NotFoundException::new);
    }

    private Response keys(DataSource dataSource, String tableName) {
        try {
            SqlResult result =
                toSqlResult(dataSource, c -> c.getMetaData().getPrimaryKeys(null, null, tableName.toUpperCase()));
            return Response.ok().entity(result).build();
        } catch (SQLException e) {
            return badRequestResponse(e);
        }
    }

    @GET
    @Path("{dataSourceName}/tables/{tableName}/relations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tableRelations(@PathParam("dataSourceName") String dataSourceName, @PathParam("tableName") String tableName) {
        return
            dataSourceApplication
                .dataSource(dataSourceName)
                .map(dataSource -> this.relations(dataSource, tableName))
                .orElseThrow(NotFoundException::new);
    }

    private Response relations(DataSource dataSource, String tableName) {
        try {
            SqlResult result =
                toSqlResult(dataSource, c -> c.getMetaData().getImportedKeys(null, null, tableName.toUpperCase()));
            return Response.ok().entity(result).build();
        } catch (SQLException e) {
            return badRequestResponse(e);
        }
    }

    @GET
    @Path("{dataSourceName}/tables/{tableName}/references")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tableReferences(@PathParam("dataSourceName") String dataSourceName, @PathParam("tableName") String tableName) {
        return
            dataSourceApplication
                .dataSource(dataSourceName)
                .map(dataSource -> this.references(dataSource, tableName))
                .orElseThrow(NotFoundException::new);
    }

    private Response references(DataSource dataSource, String tableName) {
        try {
            SqlResult result =
                toSqlResult(dataSource, c -> c.getMetaData().getExportedKeys(null, null, tableName.toUpperCase()));
            return Response.ok().entity(result).build();
        } catch (SQLException e) {
            return badRequestResponse(e);
        }
    }

    private List<Map<String, Object>> parse(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Object>> result = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> row = new LinkedHashMap<>(metaData.getColumnCount());
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                row.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
            }
            result.add(row);
        }
        return result;
    }

    @POST
    @Path("/{dataSourceName}")
    public Response run(@PathParam("dataSourceName") String dataSourceName, String sql) {
        return dataSourceApplication.dataSource(dataSourceName)
            .map(dataSource -> this.execute(dataSource, sql))
            .orElseThrow(NotFoundException::new);
    }

    private Response execute(DataSource dataSource, String sql) {
        try {
            SqlJsonResult result = doSql(dataSource, sql);
            return Response.ok().entity(result).build();
        } catch (SQLException | JSONException e) {
            return badRequestResponse(e);
        }
    }

    private SqlJsonResult doSql(DataSource dataSource, String sql) throws SQLException, JSONException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                boolean hasResultSet = statement.execute(sql);
                if (!hasResultSet) {
                    return new SqlJsonResult(statement.getUpdateCount());
                }
                try (ResultSet resultSet = statement.getResultSet()) {
                    return new SqlJsonResult(parseColumns(resultSet.getMetaData()), parseTuples(resultSet));
                }
            }
        }
    }

    private JSONObject parseColumns(ResultSetMetaData metaData) throws SQLException, JSONException {
        JSONObject columns = new JSONObject();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String table = metaData.getTableName(i);
            String column = metaData.getColumnName(i);
            JSONObject tableJson = this.getOrAddJSONObject(columns, table);
            tableJson.put(column, column.toLowerCase());
        }
        return columns;
    }

    private JSONObject getOrAddJSONObject(JSONObject json, String prop) throws JSONException {
        if (!json.has(prop)) {
            json.put(prop, new JSONObject());
        }
        return json.getJSONObject(prop);
    }

    private JSONArray parseTuples(ResultSet resultSet) throws SQLException, JSONException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        JSONArray rows = new JSONArray();
        while (resultSet.next()) {
            JSONObject row = new JSONObject();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String table = metaData.getTableName(i);
                String column = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                JSONObject tableJson = this.getOrAddJSONObject(row, table);
                tableJson.put(column, value);
            }
            rows.put(row);
        }
        return rows;
    }

    @GET
    @Path("{name}/tables")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TableInfo> tables(@PathParam("name") String name) {
        return dataSourceApplication.dataSource(name)
            .map(dataSource -> this.getTables(name, dataSource))
            .orElseThrow(NotFoundException::new);
    }

    private List<TableInfo> getTables(String name, DataSource dataSource) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<TableInfo> result = new ArrayList<>();
                try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        result.add(new TableInfo(name, rs.getString(3)));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            throw new WebApplicationException(e);
        }
    }

    @GET
    @Path("{dataSourceName}/tables/{tableName}/columns")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ColumnInfo> columns(@PathParam("dataSourceName") String dataSourceName, @PathParam("tableName") String tableName) {
        return dataSourceApplication.dataSource(dataSourceName)
            .map(dataSource -> this.getColumns(dataSource, tableName))
            .orElseThrow(NotFoundException::new);
    }

    private List<ColumnInfo> getColumns(DataSource dataSource, String tableName) {
        try {
            try (Connection connection = dataSource.getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();
                List<ColumnInfo> result = new ArrayList<>();
                try (ResultSet rs = metaData.getColumns(null, null, tableName, "%")) {
                    while (rs.next()) {
                        result.add(new ColumnInfo(rs.getString(4)));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            throw new WebApplicationException(e);
        }
    }

    private SqlResult toSqlResult(DataSource dataSource, MetaDataInfo metaDataInfo) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (ResultSet resultSet = metaDataInfo.getMetaData(connection)) {
                return new SqlResult(parse(resultSet));
            }
        }
    }

    @FunctionalInterface
    private interface MetaDataInfo {
        ResultSet getMetaData(Connection connection) throws SQLException;
    }

}
