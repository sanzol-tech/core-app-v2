package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import sanzol.se.audit.AuditService;
import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeUser;
import sanzol.se.services.SePasswordService;
import sanzol.util.validator.PasswordValidator;
import sanzol.util.validator.ValidationDisplay;
import sanzol.util.validator.ValidationItem;

@Named
@RequestScoped
public class SePasswordChangeController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String THIS_PAGE = "sePasswordChange";

	private static final String MESSAGE_NOTEQUAL = getI18nString("sePasswordChange.message.notEqual");
	private static final String MESSAGE_EQUALS_OLD_NEW = getI18nString("sePasswordChange.message.equalsOldNew");
	private static final String MESSAGE_CHANGED_SUCCESSFUL = getI18nString("sePasswordChange.message.changedSuccesful");

	private String oldPassword;
	private String newPassword;
	private String rePassword;

	public String getOldPassword()
	{
		return oldPassword;
	}

	public void setOldPassword(String oldPassword)
	{
		this.oldPassword = oldPassword;
	}

	public String getNewPassword()
	{
		return newPassword;
	}

	public void setNewPassword(String newPassword)
	{
		this.newPassword = newPassword;
	}

	public String getRePassword()
	{
		return rePassword;
	}

	public void setRePassword(String rePassword)
	{
		this.rePassword = rePassword;
	}

	public void pageLoad()
	{
		HttpServletRequest req = FacesUtils.getRequest();

		RequestContext context = RequestContext.createContext(req);
		try
		{
			AuditService.auditPageLoad(context, THIS_PAGE, null);
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}
	}

	private boolean validate()
	{
		return ValidationDisplay.isValid(
				new ValidationItem(!oldPassword.equals(newPassword), MESSAGE_EQUALS_OLD_NEW, "newPassword"),
				new ValidationItem(newPassword.equals(rePassword), MESSAGE_NOTEQUAL, "rePassword"),
				PasswordValidator.validatePassword("newPassword", newPassword)
			);
	}

	public String updatePassword()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			if (!validate())
			{
				return null;
			}

			SeUser seUser = context.getSeUser();

			SePasswordService service = new SePasswordService(context);
			service.changePassword(seUser, oldPassword, newPassword);

			if (!context.hasErrorOrFatal())
			{
				FacesUtils.addMessageInfo(MESSAGE_CHANGED_SUCCESSFUL);
				FacesUtils.getExternalContext().getFlash().setKeepMessages(true);

				return "/site/loggedIndex.xhtml?faces-redirect=true";
			}
		}
		catch (Exception e)
		{
			context.getMsgLogger().addMessage(e);
		}
		finally
		{
			context.close();
		}

		return null;
	}

	public String getPolicy()
	{
		return PasswordValidator.getPolicy();
	}

	public void generatePassword()
	{
		newPassword = PasswordValidator.generatePassword();
		rePassword = newPassword;
	}

}
