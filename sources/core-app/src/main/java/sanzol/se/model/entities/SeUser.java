package sanzol.se.model.entities;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

@Entity
@Table(name = "se_users")
public class SeUser implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer userId;
	private SeAccount seAccount;
	private String username;
	private String lastname;
	private String firstname;
	private String email;
	private String cellphone;
	private int maxSessions;
	private boolean isKillPrevSession;
	private int maxInactiveInterval;
	private int userLevel;
	private LocalDateTime lastLogin;
	private int incorrectAttempts;
	private boolean isLocked;
	private boolean isActive;
	private Set<SeSectorUser> lstSeSectorsUser = new HashSet<SeSectorUser>(0);
	private Set<SeRoleUser> lstSeRolesUser = new HashSet<SeRoleUser>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_users_seq")
	//@GenericGenerator(name = "se_users_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_users_seq") })
	@Column(name = "USER_ID", unique = true, nullable = false)
	public Integer getUserId()
	{
		return this.userId;
	}

	public void setUserId(Integer userId)
	{
		this.userId = userId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ACCOUNT_ID")
	public SeAccount getSeAccount()
	{
		return this.seAccount;
	}

	public void setSeAccount(SeAccount seAccount)
	{
		this.seAccount = seAccount;
	}

	@Column(name = "USERNAME", nullable = false, length = 20)
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

	@Column(name = "EMAIL", length = 200)
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

	@Column(name = "MAX_SESSIONS", nullable = false, precision = 9, scale = 0)
	public int getMaxSessions()
	{
		return this.maxSessions;
	}

	public void setMaxSessions(int maxSessions)
	{
		this.maxSessions = maxSessions;
	}

	@Column(name = "IS_KILL_PREV_SESSION", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsKillPrevSession()
	{
		return this.isKillPrevSession;
	}

	public void setIsKillPrevSession(boolean isKillPrevSession)
	{
		this.isKillPrevSession = isKillPrevSession;
	}

	@Column(name = "MAX_INACTIVE_INTERVAL", nullable = false, precision = 9, scale = 0)
	public int getMaxInactiveInterval()
	{
		return this.maxInactiveInterval;
	}

	public void setMaxInactiveInterval(int maxInactiveInterval)
	{
		this.maxInactiveInterval = maxInactiveInterval;
	}

	@Column(name = "USER_LEVEL", nullable = false, precision = 9, scale = 0)
	public int getUserLevel()
	{
		return this.userLevel;
	}

	public void setUserLevel(int userLevel)
	{
		this.userLevel = userLevel;
	}

	@Column(name = "LAST_LOGIN")
	public LocalDateTime getLastLogin()
	{
		return this.lastLogin;
	}

	public void setLastLogin(LocalDateTime lastLogin)
	{
		this.lastLogin = lastLogin;
	}

	@Column(name = "INCORRECT_ATTEMPTS", nullable = false, precision = 9, scale = 0)
	public int getIncorrectAttempts()
	{
		return this.incorrectAttempts;
	}

	public void setIncorrectAttempts(int incorrectAttempts)
	{
		this.incorrectAttempts = incorrectAttempts;
	}

	@Column(name = "IS_LOCKED", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsLocked()
	{
		return this.isLocked;
	}

	public void setIsLocked(boolean isLocked)
	{
		this.isLocked = isLocked;
	}

	@Column(name = "IS_ACTIVE", nullable = false, precision = 1, scale = 0)
	@Type(type = "org.hibernate.type.NumericBooleanType")
	public boolean isIsActive()
	{
		return this.isActive;
	}

	public void setIsActive(boolean isActive)
	{
		this.isActive = isActive;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seUser")
	public Set<SeSectorUser> getLstSeSectorsUser()
	{
		return lstSeSectorsUser;
	}

	public void setLstSeSectorsUser(Set<SeSectorUser> lstSeSectorsUser)
	{
		this.lstSeSectorsUser = lstSeSectorsUser;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "seUser")
	public Set<SeRoleUser> getLstSeRolesUser()
	{
		return lstSeRolesUser;
	}

	public void setLstSeRolesUser(Set<SeRoleUser> lstSeRolesUser)
	{
		this.lstSeRolesUser = lstSeRolesUser;
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
	public String getNameAlt()
	{
		if ((lastname != null && !lastname.isBlank()) && (firstname != null && !firstname.isBlank()))
			return firstname + " " + lastname;
		else if (lastname != null && !lastname.isEmpty())
			return lastname;
		else
			return firstname;
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
			person = MimeUtility.decodeText(getNameAlt());
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

}