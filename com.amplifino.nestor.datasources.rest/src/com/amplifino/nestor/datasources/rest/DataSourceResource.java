package com.amplifino.nestor.datasources.rest;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
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
	
	@POST
	@Path("/{dsn}")
	public Response run(@PathParam("dsn") String dataSourceName, String sql){
		return dataSourceApplication.dataSource(dataSourceName)
			.map(dataSource -> this.execute(dataSource, sql))
			.orElseThrow(NotFoundException::new);
	}
	
	public Response execute(DataSource dataSource, String sql) {
		try {
			return Response.ok().entity(doExecute(dataSource, sql)).build();
		} catch (SQLException e) {
			e.printStackTrace();
			return Response.status(Status.BAD_REQUEST)
				.entity(e.toString())
				.type("text/plain")
				.build();
		}
	}
	
	private SqlResult doExecute(DataSource dataSource, String sql) throws SQLException {
		String[] parts = sql.split(" ");
		if (parts.length == 2 && (parts[0].equalsIgnoreCase("desc") || parts[0].equalsIgnoreCase("describe"))) {
			return describe(dataSource, parts[1]);
		} else if (parts.length == 2 && (parts[0].equalsIgnoreCase("key"))) { 
			return key(dataSource, parts[1]);
		} else if (parts.length == 2 && (parts[0].equalsIgnoreCase("rel"))) { 
			return relations(dataSource, parts[1]);
		} else if (parts.length == 2 && (parts[0].equalsIgnoreCase("ref"))) { 
			return references(dataSource, parts[1]);
		} else {
			return doSql(dataSource, sql);
		}
	}
	
	/*
	private SqlResult describe(DataSource dataSource, String table) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				try (ResultSet resultSet = statement.executeQuery("select * from " + table + " where 1 = 0")) {
					return new SqlResult(parse(resultSet.getMetaData()));
				}
			}
		}
	}
	*/
	
	private SqlResult describe(DataSource dataSource, String table) throws SQLException {
		return toSqlResult(dataSource, c -> c.getMetaData().getColumns(null,  null,  table.toUpperCase(), "%"));		
	}
	
	private SqlResult key(DataSource dataSource, String table) throws SQLException {
		return toSqlResult(dataSource, c -> c.getMetaData().getPrimaryKeys(null,  null,  table.toUpperCase()));			
	}
	
	private SqlResult relations(DataSource dataSource, String table) throws SQLException {
		return toSqlResult(dataSource, c -> c.getMetaData().getImportedKeys(null,  null,  table.toUpperCase()));
	}
	
	private SqlResult references(DataSource dataSource, String table) throws SQLException {
		return toSqlResult(dataSource, c -> c.getMetaData().getExportedKeys(null,  null,  table.toUpperCase()));		
	}
	
	private SqlResult doSql(DataSource dataSource, String sql) throws SQLException {
		try (Connection connection = dataSource.getConnection()) {
			try (Statement statement = connection.createStatement()) {
				boolean hasResultSet = statement.execute(sql);
				if (!hasResultSet) {
					return new SqlResult(statement.getUpdateCount());
				}
				try (ResultSet resultSet = statement.getResultSet()) {
					return new SqlResult(parse(resultSet));
				}
			}
		}
	}
	
	private List<Map<String, Object>> parse(ResultSet resultSet) throws SQLException {
		ResultSetMetaData metaData = resultSet.getMetaData();
		List<Map<String, Object>> result = new ArrayList<>();
		while (resultSet.next()) {
			Map<String, Object> row = new LinkedHashMap<>(metaData.getColumnCount());
			for (int i = 1 ; i <= metaData.getColumnCount(); i++) {
				row.put(metaData.getColumnName(i).toLowerCase(), resultSet.getObject(i));
			}
			result.add(row);
		}
		return result;
	}
	
	private List<Map<String, Object>> parse(ResultSetMetaData metaData) throws SQLException {
		List<Map<String, Object>> result = new ArrayList<>();
		for (int i = 1 ; i <= metaData.getColumnCount(); i++) {
			Map<String, Object> row = new LinkedHashMap<>(5);
			row.put("name", metaData.getColumnName(i));
			row.put("type", metaData.getColumnType(i));
			row.put("scale", metaData.getScale(i));
			row.put("java", metaData.getColumnClassName(i));
			row.put("isNullable", metaData.isNullable(i));
			result.add(row);
		}
		return result;
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
				try (ResultSet rs = metaData.getTables(null,  null, "%", new String[] {"TABLE"})) {
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
				try (ResultSet rs = metaData.getColumns(null,  null, tableName, "%")) {
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
