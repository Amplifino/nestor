package com.amplifino.nestor.datasources.rest;

import com.amplifino.nestor.jdbc.api.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

@Path("/")
public class DataSourceResource {

    @Inject
    private DataSourceApplication dataSourceApplication;

    private static final String QUERY_HISTORY_TABLE_NAME = "QUERY_HISTORY";
    private static final int MAX_HISTORY_PER_USER = 200;
    private final Map<String, Boolean> datasourceHasHistoryTableMap = new HashMap<>();

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
    public Response run(
            @PathParam("dataSourceName") String dataSourceName,
            @Context SecurityContext context,
            String sql) {
        return dataSourceApplication.dataSource(dataSourceName)
            .map(dataSource -> this.execute(context, dataSource, sql))
            .orElseThrow(NotFoundException::new);
    }

    private Response execute(SecurityContext context, DataSource dataSource, String sql) {
        try {
            RunSqlResult result = doSql(dataSource, sql);
            this.appendToHistory(context, dataSource, sql);
            return Response.ok().entity(result).build();
        }
        catch (SQLException e) {
            return badRequestResponse(e);
        }
    }

    private RunSqlResult doSql(DataSource dataSource, String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                boolean hasResultSet = statement.execute(sql);
                if (!hasResultSet) {
                    return new RunSqlResult(statement.getUpdateCount());
                }
                try (ResultSet resultSet = statement.getResultSet()) {
                    return new RunSqlResult(parseColumns(resultSet.getMetaData()), parseTuples(resultSet));
                }
            }
        }
    }

    private List<ColumnInfo> parseColumns(ResultSetMetaData metaData) throws SQLException {
        List<ColumnInfo> columns = new ArrayList<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String table = metaData.getTableName(i);
            String column = metaData.getColumnName(i);
            String type = metaData.getColumnTypeName(i);
            columns.add(new ColumnInfo(column, table, type));
        }
        return columns;
    }

    private <T> Map<String, T> getOrAddTableMap(Map<String, Map<String, T>> map, String key) {
        if (!map.containsKey(key)) {
            map.put(key, new HashMap<>());
        }
        return map.get(key);
    }

    private List<Map<String, Map<String, Object>>> parseTuples(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        List<Map<String, Map<String, Object>>> rows = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Map<String, Object>> row = new HashMap<>();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                String table = metaData.getTableName(i);
                String column = metaData.getColumnName(i);
                Object value = resultSet.getObject(i);
                Map<String, Object> tableMap = this.getOrAddTableMap(row, table);
                tableMap.put(column, value);
            }
            rows.add(row);
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
                        result.add(new ColumnInfo(rs.getString(4), tableName, null));
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

    @GET
    @Path("{dataSourceName}/history")
    @Produces(MediaType.APPLICATION_JSON)
    public Response queryHistory(
            @PathParam("dataSourceName") String dataSourceName,
            @Context SecurityContext context) {
        boolean historyTableExists =
            this.datasourceHasHistoryTableMap
                .computeIfAbsent(dataSourceName, this::queryHistoryTablePresent);
        if (historyTableExists) {
            Principal user = context.getUserPrincipal();
            DataSource dataSource =
                this.dataSourceApplication
                    .dataSource(dataSourceName)
                    .orElseThrow(NotFoundException::new);
            List<HistoryInfo> queries = this.getHistory(dataSource, user.getName());
            return Response.ok(queries).build();
        }
        else {
            throw new BadRequestException(format("Datasource '%s' does not have the '%s' table", dataSourceName, QUERY_HISTORY_TABLE_NAME));
        }

    }

    private boolean queryHistoryTablePresent(String dataSourceName) {
        return
            this.tables(dataSourceName)
                .stream()
                .anyMatch(tableInfo -> tableInfo.name.equals(QUERY_HISTORY_TABLE_NAME));
    }

    private List<HistoryInfo> getHistory(DataSource dataSource, String userName) {
        /*
         * CREATE TABLE QUERY_HISTORY(
         *                      ID    INT auto_increment PRIMARY KEY,
         *                      TIMESTAMP  TIMESTAMP NOT NULL,
         *                      NAME  VARCHAR(255) NOT NULL,
         *                      QUERY VARCHAR(4096));
         */
        String selectStatement = "" +
            "SELECT timestamp, query FROM " + QUERY_HISTORY_TABLE_NAME + " " +
            "WHERE name = ? " +
            "ORDER BY timestamp desc";
        return
            Query
                .on(dataSource)
                .text(selectStatement)
                .parameters(userName)
                .select(resultSet -> new HistoryInfo(resultSet.getTimestamp(1), resultSet.getString(2)))
                .stream()
                .sorted(Comparator.comparing(hi -> ((HistoryInfo) hi).timestamp).reversed())
                .collect(Collectors.toList());
    }

    private void appendToHistory(SecurityContext context, DataSource dataSource, String query) {
        String userName = context.getUserPrincipal().getName();
        String selectStatement = "" +
            "SELECT id FROM " + QUERY_HISTORY_TABLE_NAME + " " +
            "WHERE name = ? " +
            "AND upper(query) = ? " +
            "LIMIT 1";
        Optional<Object> optionalId =
            Query
                .on(dataSource)
                .text(selectStatement)
                .parameters(userName, query.toUpperCase())
                .selectOne(resultSet -> resultSet.getInt(1));
        if (optionalId.isPresent()) {
            this.updateHistoryDate(dataSource, (int) optionalId.get());
        } else {
            try {
                this.removeSurplusHistory(dataSource, userName);
                this.appendToHistory(dataSource, userName, query);
            }
            catch (Exception ex) {
                Logger log = LoggerFactory.getLogger(this.getClass());
                log.error("Unable to persist history", ex);
            }

        }
    }

    private void updateHistoryDate(DataSource dataSource, int id) {
        String insertStatement = "" +
            "UPDATE " + QUERY_HISTORY_TABLE_NAME + " " +
            "SET timestamp = ?" +
            "WHERE id = ?";
        Query.on(dataSource)
            .text(insertStatement)
            .parameters(
                Timestamp.from(Instant.now()),
                id)
            .executeUpdate();
    }

    private void removeSurplusHistory(DataSource dataSource, String userName) {
        String selectStatement = "" +
            "SELECT id FROM " + QUERY_HISTORY_TABLE_NAME + " " +
            "WHERE name = ? " +
            "ORDER BY timestamp desc " +
            "LIMIT ?, 1000 " ;
        List<Integer> oldestIds =
            Query.on(dataSource)
                .text(selectStatement)
                .parameters(userName, MAX_HISTORY_PER_USER - 1)
                .select(resultSet -> resultSet.getInt(1));
        Query query = Query.on(dataSource)
            .text("DELETE FROM " + QUERY_HISTORY_TABLE_NAME + " ")
            .text("WHERE id in ")
            .text(oldestIds.stream().map(id -> "?").collect(Collectors.joining(",", "(", ")")));
        oldestIds.forEach(query::parameters);
        query.executeUpdate();
    }

    private void appendToHistory(DataSource dataSource, String userName, String query) {
        String insertStatement = "" +
            "INSERT INTO " + QUERY_HISTORY_TABLE_NAME + " " +
            "(timestamp, name, query)" +
            "VALUES (?,?,?)";
        Query.on(dataSource)
            .text(insertStatement)
            .parameters(
                Timestamp.from(Instant.now()),
                userName,
                query)
            .executeUpdate();
    }

    @FunctionalInterface

    private interface MetaDataInfo {
        ResultSet getMetaData(Connection connection) throws SQLException;
    }

}
