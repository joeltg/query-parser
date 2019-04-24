package edu.mit.kfg.priorart.parser;

public class RequestClass {
	String query;
	String operator;
	String filters;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getFilters() {
		return filters;
	}

	public void setFilters(String filters) {
		this.filters = filters;
	}

	public RequestClass(String query, String operator, String filters) {
		this.query = query;
		this.operator = operator;
		this.filters = filters;
	}

	public RequestClass() {
	}
}