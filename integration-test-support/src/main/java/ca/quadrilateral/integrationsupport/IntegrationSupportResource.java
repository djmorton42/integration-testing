package ca.quadrilateral.integrationsupport;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/")
public class IntegrationSupportResource {
    private static final Logger logger = LoggerFactory.getLogger(IntegrationSupportResource.class);

    private static Configuration configuration = null;

    @Path("ping")
    @GET
    public Response ping() {
        logger.info("Received Integration Test Support Ping");

        return Response
          .ok("pong")
          .build();
    }

    @Path("configure")
    @POST
    public Response configure(final Configuration configuration) {
        IntegrationSupportResource.configuration = configuration;
        return Response.ok().build();
    }

    @Path("data")
    @DELETE
    public Response clearDatabase() throws Exception {
        logger.info("Clearing database");
        final DataSource dataSource = getDataSource();

        try (
                final Connection connection = dataSource.getConnection();
                final Statement statement = connection.createStatement()) {

            connection.setAutoCommit(false);

            try {
                for (final String tableName : configuration.getTables()) {
                    statement.executeUpdate("DELETE FROM " + tableName);
                }
                connection.commit();
            } catch (final Exception e) {
                logger.error("Error clearing database", e);
                connection.rollback();
            }
        }
        logger.info("Database clearing completed");

        return Response
                .noContent()
                .build();
    }

    @Path("data")
    @Consumes({MediaType.TEXT_PLAIN})
    @POST
    public void executeDatabaseCommands(final String commandText) throws Exception {
        final DataSource dataSource = getDataSource();

        final String[] commands = StringUtils.split(commandText, ";");

        try (
                final Connection connection = dataSource.getConnection();
                final Statement statement = connection.createStatement()
                ) {

            for (final String command : commands) {
                statement.execute(command);
            }
        }
    }

    @Path("data")
    @Consumes({MediaType.TEXT_PLAIN})
    @Produces({MediaType.APPLICATION_JSON})
    @GET
    public Response executeDatabaseQuery(final String commandText) throws Exception {
        final DataSource dataSource = getDataSource();

        final String[] commands = StringUtils.split(commandText, ";");
        if (commands.length > 1) {
            return Response
                    .status(Status.BAD_REQUEST)
                    .entity("\"Only one command can be submitted\"")
                    .build();
        }

        try (
                final Connection connection = dataSource.getConnection();
                final Statement statement = connection.createStatement()
                ) {

            final ResultSet resultSet = statement.executeQuery(commandText);

            final JsonArrayBuilder jsonArrayBuilder = resultSetToJson(resultSet);

            final JsonArray jsonArray = jsonArrayBuilder.build();

            return Response
                        .ok(jsonArray.toString())
                        .build();
        }
    }

    private DataSource getDataSource() {
        try {
            final InitialContext context = new InitialContext();
            return (DataSource) context.lookup(configuration.getDatasourceJndi());
        } catch (final NamingException e) {
            logger.error("Error retrieving data source from JNDI", e);
            throw new RuntimeException(e);
        }
    }

    private JsonArrayBuilder resultSetToJson(final ResultSet resultSet) {
        try {
            final ResultSetMetaData metadata = resultSet.getMetaData();

            final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

            while (resultSet.next()) {
                final JsonObjectBuilder rowBuilder = Json.createObjectBuilder();
                for(int i = 1; i <= metadata.getColumnCount(); i++) {
                    putColumnValue(resultSet, metadata, rowBuilder, i);
                }
                jsonArrayBuilder.add(rowBuilder.build());
            }

            return jsonArrayBuilder;
        } catch (final SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void putColumnValue(
            final ResultSet resultSet,
            final ResultSetMetaData metadata,
            final JsonObjectBuilder rowBuilder,
            final int index) throws SQLException {

        final int columnType = metadata.getColumnType(index);
        final String columnName = metadata.getColumnName(index);

        switch (columnType) {
            case Types.BIGINT:
                final long longValue = resultSet.getLong(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, longValue);
                }
                break;
            case Types.BOOLEAN:
                final boolean booleanValue = resultSet.getBoolean(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, booleanValue);
                }
                break;
            case Types.CHAR:
            case Types.VARCHAR:
                final String stringValue = resultSet.getString(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, stringValue);
                }
                break;
            case Types.DECIMAL:
            case Types.NUMERIC:
                final BigDecimal decimalValue = resultSet.getBigDecimal(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, decimalValue);
                }
                break;
            case Types.DOUBLE:
                final double doubleValue = resultSet.getDouble(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, doubleValue);
                }
                break;
            case Types.FLOAT:
            case Types.REAL:
                final float floatValue = resultSet.getFloat(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, floatValue);
                }
                break;
            case Types.INTEGER:
                final int intValue = resultSet.getInt(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, intValue);
                }
                break;
            case Types.NULL:
                rowBuilder.addNull(metadata.getColumnName(index));
                break;
            case Types.SMALLINT:
                final short shortValue = resultSet.getShort(index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, shortValue);
                }
                break;
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
                final Date dateValue = getDateValue(columnType, resultSet, index);
                if (resultSet.wasNull()) {
                    rowBuilder.addNull(columnName);
                } else {
                    rowBuilder.add(columnName, getTimestamp(dateValue));
                }
        }
    }

    private Date getDateValue(final int columnType, final ResultSet resultSet, final int index) throws SQLException {
        switch(columnType) {
            case Types.DATE:
                return resultSet.getDate(index);
            case Types.TIME:
                return resultSet.getTime(index);
            case Types.TIMESTAMP:
                return resultSet.getTimestamp(index);
        }
        throw new IllegalArgumentException("Only Date, Time and Timestamp column types are supported");
    }

    private Long getTimestamp(final Date datetime) {
        if (datetime == null) {
            return null;
        } else {
            return datetime.getTime();
        }
    }
}
