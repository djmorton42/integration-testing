package ca.quadrilateral.integrationsupport;

import java.util.List;

public class Configuration {
	private String datasourceJndi;
	private List<String> tables;
	
	public Configuration() {}
	
	public void setDatasourceJndi(final String datasourceJndi) {
		this.datasourceJndi = datasourceJndi;
	}
	
	public String getDatasourceJndi() {
		return datasourceJndi;
	}
	
	public void setTables(final List<String> tables) {
		this.tables = tables;
	}
	
	public List<String> getTables() {
		return tables;
	}
}
