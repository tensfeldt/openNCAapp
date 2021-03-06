package com.pfizer.equip.searchservice.dto;

import com.pfizer.elasticsearch.dto.Predicate;

/**
 * Base class for search conditions. Search conditions
 * consist of a name, a condition, and a property-value pair.
 * 
 * @author HeinemanWP
 *
 */
public class BaseSearchCondition {
	private String name;
	protected String condition;
	protected String property;
	protected String value;
	
	public BaseSearchCondition() {}
	
	public BaseSearchCondition(String name) {
		this.name = name;
	}
	
	public BaseSearchCondition(String name, String condition, String property, String value) {
		this.name = name;
		this.condition = condition;
		this.property = property;
		this.value= value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public Predicate toElasticSearch() {
		switch (condition.toLowerCase()) {
		case "<":
		case "<=":
		case ">":
		case ">=":
		case "=":
		case "!=":
		case "like":
			return new Predicate(condition, getProperty(), getValue());
		default:
			break;
		}
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append("(");
		sb.append(condition);
		sb.append("|");
		sb.append(property);
		sb.append("|");
		sb.append(value);
		sb.append(")");
		return sb.toString();
	}
	
	
}
