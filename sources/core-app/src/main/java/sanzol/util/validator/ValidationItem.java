/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: April 2021
 *
 */
package sanzol.util.validator;

public class ValidationItem
{
	boolean valid;
	String message;
	String clientId;

	public ValidationItem(boolean valid, String message, String clientId)
	{
		this.valid = valid;
		this.message = message;
		this.clientId = clientId;
	}

	public ValidationItem(String message, String clientId)
	{
		this.valid = message == null || message.isBlank();
		this.message = message;
		this.clientId = clientId;
	}

	public ValidationItem(String message)
	{
		this.valid = message != null && !message.isBlank();
		this.message = message;
		this.clientId = null;
	}

	public static ValidationItem VALID()
	{
		return new ValidationItem(true, null, null);
	}

	public boolean isValid()
	{
		return valid;
	}

	public void setValid(boolean valid)
	{
		this.valid = valid;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getClientId()
	{
		return clientId;
	}

	public void setClientId(String clientId)
	{
		this.clientId = clientId;
	}
}
