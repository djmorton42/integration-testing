package ca.quadrilateral.integration.builder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

public abstract class BaseTestDataSqlBuilder implements ITestDataSqlBuilder {
    private static Set<ITestDataSqlBuilder> executedBuilders = new HashSet<>();
    private static final DateTimeFormatter sqlDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static AtomicLong idGenerator = new AtomicLong(0);
    
    protected boolean finalized = false;
    protected List<String> sqlStrings = new ArrayList<>();
    
    public static void clearBuilderHistory() {
        executedBuilders.clear();
    }
    
    
    protected void addSqlString(final String sqlString) {
        this.sqlStrings.add(sqlString);
    }
    
    protected void addSqlString(final ITestDataSqlBuilder builder) {
        if (!executedBuilders.contains(builder)) {
            this.sqlStrings.add(builder.buildSql());
            executedBuilders.add(builder);
        }
    }
    
    protected String getSqlString() {
        return StringUtils.join(sqlStrings, ";");
    }
    
    protected abstract void generateSqlStrings();
    
    protected void checkFinalization() {
        if (finalized) {
            throw new IllegalStateException("Already finalized.");
        }     
    }
   
    protected void markFinalized() {
        this.finalized = true;
    }
    
    protected long getNextId() {
        return idGenerator.incrementAndGet();
    }
    
    public String formatInteger(final Integer integer) {
        if (integer == null) {
            return null;
        } else {
            return Integer.toString(integer);
        }
    }
    
    public String formatNumber(final Number number) {
    	if (number == null) {
    		return null;
    	} else {
    		return number.toString();
    	}
    }
    
    public String formatDateTime(final LocalDateTime dateTime) {
        if (dateTime == null) {
            return "null";
        }
        
        return quoteString(sqlDateTimeFormatter.format(dateTime));
    }
    
    public String quoteString(final String string) {
        if (string == null) {
            return "null";
        } else {
            return "'" + escapeString(string) + "'";
        }
    }
    
    public String escapeString(final String stringToEscape) {
        return stringToEscape.replace("'", "''");
    }
    
    @Override
    public final String buildSql() {
        generateSqlStrings();
        return getSqlString();
    }
}
