/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: April 2021
 *
 */
package sanzol.util.validator;

import javax.faces.application.FacesMessage;

import sanzol.se.commons.FacesUtils;

public class ValidationDisplay
{

	public static boolean isValid(ValidationItem... values)
	{
		boolean result = true;

		for (ValidationItem validation : values)
		{
			if (validation != null && !validation.isValid())
			{
				addMessageError(validation.getClientId(), validation.getMessage());
				result = false;
			}
		}

		return result;
	}

	private static void addMessageError(String clientId, String msg)
	{
		FacesUtils.getContext().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
	}

}
