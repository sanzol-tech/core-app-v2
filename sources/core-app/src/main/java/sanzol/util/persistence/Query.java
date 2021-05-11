/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.persistence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.util.properties.PropertiesDB;

public class Query
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesDB.class);

	private String strSelect = "";
	private String strFrom = "";
	private String strJoin = "";
	private String strWhere = "";
	private String strOrderBy = "";
	private List<Param> lstParams = new ArrayList<Param>();

	public Query()
	{
		// Default constructor
	}

	@SuppressWarnings("rawtypes")
	public Query(Class clazz, String alias)
	{
		strFrom = clazz.getSimpleName() + " " + alias;
	}

	public Query from(String... alias)
	{
		for (String item : alias)
		{
			strFrom += (strFrom.isEmpty() ? "" : ", ") + item;
		}

		return this;
	}

	@SuppressWarnings("rawtypes")
	public String alias(Class clazz, String alias)
	{
		return clazz.getSimpleName() + " " + alias;
	}

	// ----------------------------------------------------------------------------------

	public Query rowCount()
	{
		strSelect = "count(*)";

		return this;
	}

	public Query select(String fields)
	{
		strSelect = fields;

		return this;
	}

	// ----------------------------------------------------------------------------------

	public Query leftJoin(String field, String alias)
	{
		strJoin = String.format("left join %s as %s", field, alias);

		return this;
	}

	// ----------------------------------------------------------------------------------

	public Query where(String... parameters)
	{
		strWhere = "";

		for (String item : parameters)
		{
			if (item != null)
			{
				strWhere += (strWhere.isEmpty() ? "" : " and ") + item;
			}
		}

		return this;
	}

	public String AND(String... parameters)
	{
		String c = "";

		int count = 0;
		for (String item : parameters)
		{
			if (item != null)
			{
				c += (c.isEmpty() ? "" : " and ") + item;
				count++;
			}
		}

		if (!c.isEmpty() && count > 1)
		{
			c = "( " + c + " )";
		}

		return c;
	}

	public String OR(String... parameters)
	{
		String c = "";

		int count = 0;
		for (String item : parameters)
		{
			if (item != null)
			{
				c += (c.isEmpty() ? "" : " or ") + item;
				count++;
			}
		}

		if (!c.isEmpty() && count > 1)
		{
			c = "( " + c + " )";
		}

		return c;
	}

	public String eq(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " = :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String ne(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " <> :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String ge(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " >= :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String gt(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " > :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String le(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " <= :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String lt(String field, Object value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " < :" + paramName;
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String nowBetween(String fieldFrom, String fieldTo)
	{
		String c = "current_timestamp() between " + fieldFrom + " and coalesce(" + fieldTo + ", current_timestamp())";
		return c;
	}

	public String in(String field, Collection<?> value)
	{
		if (value == null)
		{
			return null;
		}
		String paramName = "p" + lstParams.size();
		String c = field + " in (:" + paramName + ")";
		lstParams.add(new Param(paramName, value));
		return c;
	}

	public String isNull(String field)
	{
		String c = field + " is null";
		return c;
	}

	public String isNotNull(String field)
	{
		String c = field + " is not null";
		return c;
	}

	// ----------------------------------------------------------------------------------

	public Query orderBy(String... fields)
	{
		for (String item : fields)
		{
			strOrderBy += (strOrderBy.isEmpty() ? "" : ", ") + item;
		}

		return this;
	}

	public String asc(String field)
	{
		return field;
	}

	public String desc(String field)
	{
		return field + " desc";
	}

	// ----------------------------------------------------------------------------------

	public String getHql()
	{
		StringBuilder sb = new StringBuilder();

		if (strSelect != null && !strSelect.isEmpty())
		{
			sb.append("select " + strSelect);
			sb.append(" ");
		}

		sb.append("from " + strFrom);

		if (strJoin != null && !strJoin.isEmpty())
		{
			sb.append(" " + strJoin);
		}

		if (strWhere != null && !strWhere.isEmpty())
		{
			sb.append(" where " + strWhere);
		}

		if (strOrderBy != null && !strOrderBy.isEmpty())
		{
			sb.append(" order by " + strOrderBy);
		}

		LOG.debug(sb.toString());

		return sb.toString();
	}

	public org.hibernate.query.Query<?> createQuery(org.hibernate.Session hbSession)
	{
		org.hibernate.query.Query<?> hbQuery = hbSession.createQuery(getHql());

		if (lstParams != null)
		{
			for (Param param : lstParams)
			{
				if (param.getValue() instanceof Collection)
				{
					hbQuery.setParameterList(param.getName(), (Collection<?>) param.getValue());
				}
				else
				{
					hbQuery.setParameter(param.getName(), param.getValue());
				}

				LOG.debug(String.format("%s: %s", param.getName(), param.getValue()));
			}
		}

		return hbQuery;
	}

	@SuppressWarnings("rawtypes")
	public List list(org.hibernate.Session hbSession)
	{
		return createQuery(hbSession).list();
	}

	public Object firstResult(org.hibernate.Session hbSession)
	{
		return createQuery(hbSession).setMaxResults(1).uniqueResult();
	}

	public Object uniqueResult(org.hibernate.Session hbSession)
	{
		return createQuery(hbSession).uniqueResult();
	}

}
