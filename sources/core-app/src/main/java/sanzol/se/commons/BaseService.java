/**
 * 
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 * 
 */
package sanzol.se.commons;

import sanzol.se.context.RequestContext;

public abstract class BaseService
{
	protected RequestContext context;

	public RequestContext getContext()
	{
		return context;
	}

	public BaseService(RequestContext context)
	{
		this.context = context;
	}

}
