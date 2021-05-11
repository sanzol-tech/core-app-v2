package sanzol.se.web.controllers;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.IOException;
import java.io.Serializable;

import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import sanzol.se.commons.FacesUtils;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeRegistration;
import sanzol.se.services.SeRegistrationsService;
import sanzol.se.services.cache.SeRegistrationsStatesCache;
import sanzol.util.Captcha;
import sanzol.util.CaseUtils;
import sanzol.util.validator.PasswordValidator;
import sanzol.util.validator.PatternValidator;
import sanzol.util.validator.RangeValidator;
import sanzol.util.validator.StringValidator;
import sanzol.util.validator.ValidationDisplay;
import sanzol.util.validator.ValidationItem;

@Named
@ViewScoped
public class LnUserRegistrationController implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE_PASSWORD_NOTEQUAL = getI18nString("sePasswordChange.message.notEqual");
	private static final String MESSAGE_VALIDATION_CODE_INVALID = getI18nString("userRegistration.message.validationCode.invalid");
	private static final String MESSAGE_IVALID_CAPTCHA = getI18nString("captcha.message.invalid");

	private static final int CAPTCHA_LENGHT = 6;

	private String vpCode;

	private Integer step = 1;

	private SeRegistration seRegistration = new SeRegistration();

	private Captcha captcha;
	private String captchaText;

	private String validationCode;
	private String password;
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

	public void setStep(Integer step)
	{
		this.step = step;
	}

	public SeRegistration getSeRegistration()
	{
		return seRegistration;
	}

	public void setSeRegistration(SeRegistration seRegistration)
	{
		this.seRegistration = seRegistration;
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

	public String getValidationCode()
	{
		return validationCode;
	}

	public void setValidationCode(String validationCode)
	{
		this.validationCode = validationCode;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
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
		if (vpCode != null && vpCode.length() == SeRegistrationsService.VALIDATION_CODE_SIZE)
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

	// ------------------------------------------------------------------------------------------------------------------------

	public void normalize()
	{
		seRegistration.setUsername(CaseUtils.lowerTrim(seRegistration.getUsername()));
		seRegistration.setLastname(CaseUtils.trim(seRegistration.getLastname()));
		seRegistration.setFirstname(CaseUtils.trim(seRegistration.getFirstname()));
		seRegistration.setGender("M".equals(seRegistration.getGender()) || "F".equals(seRegistration.getGender()) ? seRegistration.getGender() : null);
		seRegistration.setDocumentId(CaseUtils.upperTrim(seRegistration.getDocumentId()));
		seRegistration.setEmail(CaseUtils.lowerTrim(seRegistration.getEmail()));
		seRegistration.setCellphone(CaseUtils.trim(seRegistration.getCellphone()));
	}

	// ----- STEP 1 -----------------------------------------------------------------------------------------------------------

	public boolean validateStep1()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("lastname", "{userRegistration.field.lastname}", seRegistration.getLastname(), null, null, true),
				StringValidator.validate("firstname", "{userRegistration.field.firstname}", seRegistration.getFirstname(), null, null, true),
				PatternValidator.validateEmail("email", "{userRegistration.field.email}", seRegistration.getEmail(), false),
				PatternValidator.validatePhone("cellphone", "{userRegistration.field.cellphone}", seRegistration.getCellphone(), false),
				new ValidationItem(captcha.isValidIgnoreCase(captchaText), MESSAGE_IVALID_CAPTCHA, "captcha")
			);
	}

	public void startRegistration()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			normalize();
			if (!validateStep1())
			{
				return;
			}

			SeRegistrationsService service = new SeRegistrationsService(context);
			service.addUserRequest(seRegistration);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			seRegistration = null;
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
			SeRegistrationsService service = new SeRegistrationsService(context);
			seRegistration = service.checkValidationCode(validationCode);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			if (seRegistration == null)
			{
				FacesUtils.addMessageError(MESSAGE_VALIDATION_CODE_INVALID);
				validationCode = null;
				return;
			}

			if (seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.INVITED) || seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.USER_REQUESTED))
			{
				step = 3;
			}
			else if (seRegistration.getRegistrationStateId().equals(SeRegistrationsStatesCache.AUTHORIZATION_SUCCESSFUL))
			{
				step = 4;
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

	public boolean validateStep3()
	{
		return ValidationDisplay.isValid(
				StringValidator.validate("username", "{userRegistration.field.username}", seRegistration.getUsername(), null, null, true),
				StringValidator.validate("lastname", "{userRegistration.field.lastname}", seRegistration.getLastname(), null, null, true),
				StringValidator.validate("firstname", "{userRegistration.field.firstname}", seRegistration.getFirstname(), null, null, true),
				RangeValidator.validateBirthDate("birthDate", "{userRegistration.field.birthDate}", seRegistration.getBirthDate(), 0, 120, false),
				StringValidator.validateAlphanumeric("documentId", "{userRegistration.field.documentId}", seRegistration.getDocumentId(), 5, 10, false),
				PatternValidator.validateEmail("email", "{userRegistration.field.email}", seRegistration.getEmail(), true),
				PatternValidator.validatePhone("cellphone", "{userRegistration.field.cellphone}", seRegistration.getCellphone(), false)
			);
	}

	public void saveForm()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			normalize();
			if (!validateStep3())
			{
				return;
			}

			SeRegistrationsService service = new SeRegistrationsService(context);
			Boolean canContinue = service.checkWhitelist(seRegistration);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			if (canContinue)
				step = 4;
			else
				step = 6;
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

	// ----- STEP 4 -----------------------------------------------------------------------------------------------------------

	public String getPolicy()
	{
		return PasswordValidator.getPolicy();
	}

	public void generatePassword()
	{
		password = PasswordValidator.generatePassword();
		rePassword = password;
	}

	public boolean validateStep4()
	{
		return ValidationDisplay.isValid(
				new ValidationItem(password.equals(rePassword), MESSAGE_PASSWORD_NOTEQUAL, "rePassword"),
				PasswordValidator.validatePassword("password", password)
			);
	}

	public void completeSignUp()
	{
		RequestContext context = RequestContext.createContext();
		try
		{
			if (!validateStep4())
			{
				return;
			}

			SeRegistrationsService service = new SeRegistrationsService(context);
			service.createUser(seRegistration, password);
			if (context.hasErrorOrFatal())
			{
				return;
			}

			step = 5;
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

}
