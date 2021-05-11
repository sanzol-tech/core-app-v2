package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import sanzol.util.security.Cipher;

@Entity
@Table(name = "se_properties")
public class SeProperty implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer propertyId;
	private String name;
	private String detail;
	private String value;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	//@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "se_properties_seq")
	//@GenericGenerator(name = "se_properties_seq", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @Parameter(name = "sequence_name", value = "se_properties_seq") })
	@Column(name = "PROPERTY_ID", unique = true, nullable = false)
	public Integer getPropertyId()
	{
		return this.propertyId;
	}

	public void setPropertyId(Integer propertyId)
	{
		this.propertyId = propertyId;
	}

	@Column(name = "NAME", unique = true, nullable = false, length = 40)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "DETAIL", length = 500)
	public String getDetail()
	{
		return this.detail;
	}

	public void setDetail(String detail)
	{
		this.detail = detail;
	}

	@Column(name = "VALUE", length = 4000)
	public String getValue()
	{
		return this.value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	// ------------------------------------------------------------------------------------------------

	@Transient
	public String getDecryptValue()
	{
		return Cipher.decrypt(this.value, Cipher.Codec.Hex);
	}

	@Transient
	public void setEncryptValue(String value)
	{
		this.value = Cipher.encrypt(value, Cipher.Codec.Hex);
	}

}