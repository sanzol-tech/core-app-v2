package sanzol.se.model.entities;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javax.mail.internet.MimeUtility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import sanzol.se.services.cache.SeRegistrationsStatesCache;
import sanzol.util.DateTimeUtils;

@Entity
@Table(name = "se_registrations")
public class SeRegistration implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer registrationId;
	private String username;
	private String lastname;
	private String firstname;
	private LocalDate birthDate;
	private String gender;
	private String documentId;
	private String email;
	private String cellphone;
	private String validationCode;
	private SeRegistrationEvent firstEvent;
	private SeRegistrationEvent lastEvent;
	private SeUser seUser;
	private Set<SeRegistrationEvent> lstSeRegistrationsEvents = new HashSet<SeRegistrationEvent>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_registrations_seq")
	//@GenericGenerator(name = "se_registrations_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_registrations_seq") })
	@Column(name = "REGISTRATION_ID", nullable = false)
	public Integer getRegistrationId()
	{
		return this.registrationId;
	}

	public void setRegistrationId(Integer registrationId)
	{
		this.registrationId = registrationId;
	}

	@Column(name = "USERNAME", length = 20)
	public String getUsername()
	{
		return this.username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	@Column(name = "LASTNAME", nullable = false, length = 40)
	public String getLastname()
	{
		return this.lastname;
	}

	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}

	@Column(name = "FIRSTNAME", nullable = false, length = 40)
	public String getFirstname()
	{
		return this.firstname;
	}

	public void setFirstname(String firstname)
	{
		this.firstname = firstname;
	}

	@Column(name = "BIRTH_DATE")
	public LocalDate getBirthDate()
	{
		return this.birthDate;
	}

	public void setBirthDate(LocalDate birthDate)
	{
		this.birthDate = birthDate;
	}

	@Column(name = "GENDER", length = 1)
	public String getGender()
	{
		return this.gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	@Column(name = "DOCUMENT_ID", length = 20)
	public String getDocumentId()
	{
		return this.documentId;
	}

	public void setDocumentId(String documentId)
	{
		this.documentId = documentId;
	}

	@Column(name = "EMAIL", nullable = false, length = 200)
	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Column(name = "CELLPHONE", length = 40)
	public String getCellphone()
	{
		return this.cellphone;
	}

	public void setCellphone(String cellphone)
	{
		this.cellphone = cellphone;
	}

	@Column(name = "VALIDATION_CODE", nullable = false, length = 32)
	public String getValidationCode()
	{
		return this.validationCode;
	}

	public void setValidationCode(String validationCode)
	{
		this.validationCode = validationCode;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "FIRST_REGISTRATION_EVENT_ID", nullable = false)
	public SeRegistrationEvent getFirstEvent()
	{
		return firstEvent;
	}

	public void setFirstEvent(SeRegistrationEvent firstEvent)
	{
		this.firstEvent = firstEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "LAST_REGISTRATION_EVENT_ID", nullable = false)
	public SeRegistrationEvent getLastEvent()
	{
		return lastEvent;
	}

	public void setLastEvent(SeRegistrationEvent lastEvent)
	{
		this.lastEvent = lastEvent;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "USER_ID")
	public SeUser getSeUser()
	{
		return this.seUser;
	}

	public void setSeUser(SeUser seUser)
	{
		this.seUser = seUser;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seRegistration")
	@OrderBy("registrationEventId DESC")
	public Set<SeRegistrationEvent> getLstSeRegistrationsEvents()
	{
		return lstSeRegistrationsEvents;
	}

	public void setLstSeRegistrationsEvents(Set<SeRegistrationEvent> lstSeRegistrationsEvents)
	{
		this.lstSeRegistrationsEvents = lstSeRegistrationsEvents;
	}

	// ----- TRANSIENT --------------------------------------------------------------------------------------------------------

	@Transient
	public String getFullName()
	{
		if ((lastname != null && !lastname.isBlank()) && (firstname != null && !firstname.isBlank()))
			return lastname + ", " + firstname;
		else if (lastname != null && !lastname.isEmpty())
			return lastname;
		else
			return firstname;
	}

	@Transient
	public String getFullNameAlt()
	{
		if ((lastname != null && !lastname.isBlank()) && (firstname != null && !firstname.isBlank()))
			return firstname + " " + lastname;
		else if (lastname != null && !lastname.isEmpty())
			return lastname;
		else
			return firstname;
	}

	@Transient
	public String getGenderName()
	{
		if ("M".equals(gender))
			return getI18nString("label.gender.male");

		if ("F".equals(gender))
			return getI18nString("label.gender.female");

		return null;
	}

	@Transient
	public String getFormattedEmail()
	{
		if (email == null || email.isBlank())
		{
			return null;
		}

		String person;
		try	{
			person = MimeUtility.decodeText(getFullNameAlt());
		} catch (UnsupportedEncodingException e) { 
			person = null; 
		}		

		String result = "";
		for (String item : email.split("[;,]"))
		{
			if (!item.isBlank())
			{
				if (person != null)
					result += (result.isEmpty() ? "" : ";") + person + " <" + item.trim() + ">";
				else
					result += (result.isEmpty() ? "" : ";") + item.trim();
			}
		}

		return result;
	}

	// ----- LAST STATE -------------------------------------------------------------------------------------------------------

	@Transient
	public Integer getRegistrationStateId()
	{
		if (lastEvent.getSeRegistrationState() == null)
		{
			return null;
		}

		return lastEvent.getSeRegistrationState().getRegistrationStateId();
	}

	@Transient
	public boolean isExpired()
	{
		return lastEvent.getExpirationDate() != null && lastEvent.getExpirationDate().isBefore(DateTimeUtils.now());
	}

	@Transient
	public String getStateName()
	{
		return lastEvent.getSeRegistrationState().getName() + (isExpired() ? " - EXPIRED" : "");
	}

	@Transient
	public boolean isInvited()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.INVITED;
	}

	@Transient
	public boolean isUser_requested()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.USER_REQUESTED;
	}

	@Transient
	public boolean isAuthorization_pending()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.AUTHORIZATION_PENDING;
	}

	@Transient
	public boolean isAuthorization_successful()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.AUTHORIZATION_SUCCESSFUL;
	}

	@Transient
	public boolean isAuthorization_revoked()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.AUTHORIZATION_REVOKED;
	}

	@Transient
	public boolean isUser_created()
	{
		return lastEvent.getSeRegistrationState().getRegistrationStateId() == SeRegistrationsStatesCache.USER_CREATED;
	}

}