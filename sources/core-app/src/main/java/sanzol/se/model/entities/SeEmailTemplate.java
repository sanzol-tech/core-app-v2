package sanzol.se.model.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "se_email_templates")
public class SeEmailTemplate implements java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private Integer emailTemplateId;
	private String name;
	private String tags;
	private String subject;
	private String plainBody;
	private String htmlBody;

	@Id
	@Column(name = "EMAIL_TEMPLATE_ID", nullable = false)
	public Integer getEmailTemplateId()
	{
		return this.emailTemplateId;
	}

	public void setEmailTemplateId(Integer emailTemplateId)
	{
		this.emailTemplateId = emailTemplateId;
	}

	@Column(name = "NAME", nullable = false, length = 40)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Column(name = "TAGS", length = 200)
	public String getTags()
	{
		return tags;
	}

	public void setTags(String tags)
	{
		this.tags = tags;
	}

	@Column(name = "SUBJECT", nullable = false, length = 78)
	public String getSubject()
	{
		return subject;
	}

	public void setSubject(String subject)
	{
		this.subject = subject;
	}

	@Column(name = "PLAIN_BODY", length = 65535)
	public String getPlainBody()
	{
		return this.plainBody;
	}

	public void setPlainBody(String plainBody)
	{
		this.plainBody = plainBody;
	}

	@Column(name = "HTML_BODY", length = 65535)
	public String getHtmlBody()
	{
		return this.htmlBody;
	}

	public void setHtmlBody(String htmlBody)
	{
		this.htmlBody = htmlBody;
	}

}