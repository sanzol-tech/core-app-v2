package sanzol.se.model.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "se_soft_versions")
public class SeSoftVersion implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer softVersionId;
	private int major;
	private int minor;
	private int patch;
	private String fase;
	private LocalDateTime firstStartup;
	private String detail;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_soft_versions_seq")
	//@GenericGenerator(name = "se_soft_versions_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_soft_versions_seq") })
	@Column(name = "SOFT_VERSION_ID", unique = true, nullable = false)
	public Integer getSoftVersionId()
	{
		return this.softVersionId;
	}

	public void setSoftVersionId(Integer softVersionId)
	{
		this.softVersionId = softVersionId;
	}

	@Column(name = "MAJOR", nullable = false, precision = 9, scale = 0)
	public int getMajor()
	{
		return this.major;
	}

	public void setMajor(int major)
	{
		this.major = major;
	}

	@Column(name = "MINOR", nullable = false, precision = 9, scale = 0)
	public int getMinor()
	{
		return this.minor;
	}

	public void setMinor(int minor)
	{
		this.minor = minor;
	}

	@Column(name = "PATCH", nullable = false, precision = 9, scale = 0)
	public int getPatch()
	{
		return this.patch;
	}

	public void setPatch(int patch)
	{
		this.patch = patch;
	}

	@Column(name = "FASE", nullable = false, length = 20)
	public String getFase()
	{
		return this.fase;
	}

	public void setFase(String fase)
	{
		this.fase = fase;
	}

	@Column(name = "FIRST_STARTUP", nullable = false)
	public LocalDateTime getFirstStartup()
	{
		return firstStartup;
	}

	public void setFirstStartup(LocalDateTime firstStartup)
	{
		this.firstStartup = firstStartup;
	}

	@Column(name = "DETAIL", length = 2000)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	// ------------------------------------------------------------------------------------------------

	@Transient
	public String getVersion()
	{
		return major + "." + minor + "." + patch + "_" + fase;
	}

}