/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: August 2020
 *
 */
package sanzol.util.properties;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Property implements Serializable
{
	private static final long serialVersionUID = 1L;

	public enum SourceType
	{
		CONST, ARGUMENT, FILE, DB, LICENSE
	}

	public enum PropertyType
	{
		STRING, TEXT, INTEGER, LONG, DOUBLE, BOOLEAN, DATETIME, SET
	}

	private SourceType sourceType;
	private boolean hidden;
	private String name;
	private String detail;
	private String value;
	private PropertyType propertyType;
	private String setValues;

	public Property(SourceType sourceType, boolean hidden, String name, String value, PropertyType propertyType, String setValues)
	{
		this.sourceType = sourceType;
		this.hidden = hidden;
		this.name = name;
		this.value = value;
		this.propertyType = propertyType;
		this.setValues = setValues;
	}

	public SourceType getSourceType()
	{
		return sourceType;
	}

	public void setSourceType(SourceType sourceType)
	{
		this.sourceType = sourceType;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public void setHidden(boolean hidden)
	{
		this.hidden = hidden;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDetail()
	{
		return detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public PropertyType getPropertyType()
	{
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType)
	{
		this.propertyType = propertyType;
	}

	public String getSetValues()
	{
		return setValues;
	}

	public void setSetValues(String setValues)
	{
		this.setValues = setValues;
	}

	// ----------------------------------------------------------------

	public Property withSourceType(SourceType sourceType)
	{
		this.sourceType = sourceType;
		return this;
	}

	public Property withHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}

	public Property withName(String name)
	{
		this.name = name;
		return this;
	}

	public Property withDetail(String detail)
	{
		this.detail = detail;
		return this;
	}

	public Property withValue(String value)
	{
		this.value = value;
		return this;
	}

	public Property withPropertyType(PropertyType propertyType)
	{
		this.propertyType = propertyType;
		return this;
	}

	public Property withSetValues(String setValues)
	{
		this.setValues = setValues;
		return this;
	}

	// ----------------------------------------------------------------

	public Integer getIntegerValue()
	{
		return Integer.valueOf(value);
	}

	public Long getLongValue()
	{
		return Long.valueOf(value);
	}

	public Double getDouleValue()
	{
		return Double.valueOf(value);
	}

	public Boolean getBooleanValue()
	{
		return Boolean.valueOf(value);
	}

	public LocalDateTime getLocalDateTimeValue()
	{
		return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
	}

	// ----------------------------------------------------------------

	public void setValue(Integer value)
	{
		this.value = value.toString();
	}

	public void setValue(Long value)
	{
		this.value = value.toString();
	}

	public void setValue(Double value)
	{
		this.value = value.toString();
	}

	public void setValue(Boolean value)
	{
		this.value = value.toString();
	}

	public void setValue(LocalDateTime value)
	{
		this.value = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value);
	}

	// ---------------------------------------------------------------------------------------

	@Override
	public String toString()
	{
		return name;
	}

}
