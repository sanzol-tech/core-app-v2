package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SePasswordRecovery;
import sanzol.se.services.LnPasswordRecoveryService;
import sanzol.util.Captcha;
import sanzol.util.validator.PasswordValidator;
import sanzol.util.validator.PatternValidator;
import sanzol.util.validator.ValidationDisplay;
import sanzol.util.validator.ValidationItem;

@Named
@ViewScoped
public class LnPasswordRecoveryController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE_VALIDATION_CODE_INVALID = getI18nString("passwordRecovery.message.validationCode.invalid");
	private static final String MESSAGE_PASSWORD_NOTEQUAL = getI18nString("sePasswordChange.message.notEqual");
	private static final String MESSAGE_PASSWORD_CHANGED_SUCCESSFULLY = getI18nString("passwordRecovery.message.changedSuccessfully");
	private static final String MESSAGE_IVALID_CAPTCHA = getI18nString("captcha.message.invalid");

	private static final int CAPTCHA_LENGHT = 6;

	private String vpCode;

	private Integer step = 1;

	private Captcha captcha;
	private String captchaText;

	private String email;
	private String validationCode;
	private String newPassword;
	private String rePassword;

	public String getVpCode()
	{
		return vpCode;
	}

	public void setVpCode(String vpCode)
	{
		this.vpCode = vpCode;
	}

	public Integer getStep()
	{
		return step;
	}

	public Captcha getCaptcha()
	{
		return captcha;
	}

	public String getCaptchaText()
	{
		return captchaText;
	}

	public void setCaptchaText(String captchaText)
	{
		this.captchaText = captchaText;
	}

	public void setStep(Integer step)
	{
		this.step = step;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getRecoveryCode()
	{
		return validationCode;
	}

	public void setRecoveryCode(String validationCode)
	{
		this.validationCode = validationCode;
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

	// ----- PAGE LOAD --------------------------------------------------------------------------------------------------------

	public void pageLoad()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			loadParameters();
			createCaptcha();
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

	private void loadParameters()
	{
		if (vpCode != null && vpCode.length() == LnPasswordRecoveryService.VALIDATION_CODE_SIZE)
		{
			validationCode = vpCode;
			checkValidationCode();
		}
	}

	public void createCaptcha() throws IOException
	{
		captcha = Captcha.withLenght(CAPTCHA_LENGHT).create();
		captchaText = "";
	}

	// ----- STEP 1 -----------------------------------------------------------------------------------------------------------

	public boolean validateStep1()
	{
		return ValidationDisplay.isValid(
				PatternValidator.validateEmail("email", "{passwordRecovery.field.email}", email, true),
				new ValidationItem(captcha.isValidIgnoreCase(captchaText), MESSAGE_IVALID_CAPTCHA, "captcha")
			);
	}

	public void request()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			if (!validateStep1())
			{
				return;
			}

			LnPasswordRecoveryService service = new LnPasswordRecoveryService(context);
			service.addSePasswordRecovery(email);
			if (context.hasErrorOrFatal())
			{
				email = "";
				createCaptcha();
				return;
			}

			step = 2;
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

	// ----- STEP 2 -----------------------------------------------------------------------------------------------------------

	public void checkValidationCode()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			LnPasswordRecoveryService service = new LnPasswordRecoveryService(context);
			SePasswordRecovery sePasswordRecovery = service.checkValidationCode(validationCode);
			if (context.hasErrorOrFatal())
			{
				validationCode = "";
				return;
			}

			if (sePasswordRecovery == null)
			{
				FacesUtils.addMessageError(MESSAGE_VALIDATION_CODE_INVALID);
				validationCode = "";
			}
			else
			{
				step = 3;
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
	}

	// ----- STEP 3 -----------------------------------------------------------------------------------------------------------

	public String getPolicy()
	{
		return PasswordValidator.getPolicy();
	}

	public void generatePassword()
	{
		newPassword = PasswordValidator.generatePassword();
		rePassword = newPassword;
	}

	public boolean validateStep3()
	{
		return ValidationDisplay.isValid(
				new ValidationItem(newPassword.equals(rePassword), MESSAGE_PASSWORD_NOTEQUAL, "rePassword"),
				PasswordValidator.validatePassword("newPassword", newPassword)
			);
	}

	public String update()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			if (!validateStep3())
			{
				return null;
			}

			LnPasswordRecoveryService service = new LnPasswordRecoveryService(context);
			service.updateSePassword(validationCode, newPassword);

			if (!context.hasErrorOrFatal())
			{
				FacesUtils.addMessageInfo(MESSAGE_PASSWORD_CHANGED_SUCCESSFULLY);
				FacesUtils.getExternalContext().getFlash().setKeepMessages(true);

				return "/login/login.xhtml?faces-redirect=true";
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

}
