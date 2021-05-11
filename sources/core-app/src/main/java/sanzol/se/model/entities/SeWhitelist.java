package sanzol.se.model.entities;

import static sanzol.app.config.I18nPreference.getI18nString;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import sanzol.util.DateTimeUtils;

@Entity
@Table(name = "se_whitelist")
public class SeWhitelist implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer whitelistId;
	private String lastname;
	private String firstname;
	private LocalDate birthDate;
	private String gender;
	private String documentId;
	private String email;
	private String cellphone;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_whitelist_seq")
	//@GenericGenerator(name = "se_whitelist_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_whitelist_seq") })
	@Column(name = "WHITELIST_ID", nullable = false)
	public Integer getWhitelistId()
	{
		return whitelistId;
	}

	public void setWhitelistId(Integer whitelistId)
	{
		this.whitelistId = whitelistId;
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

	// ------------------------------------------------------------------------------------------------

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
	public String getAge()
	{
		return DateTimeUtils.getAge(birthDate);
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

}