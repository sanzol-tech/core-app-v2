/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: May 2021
 *
 */
package sanzol.se.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.EmailPopulatingBuilder;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;

import sanzol.app.config.AppProperties;
import sanzol.se.model.entities.SeEmailTemplate;
import sanzol.util.DateTimeUtils;
import sanzol.util.Replacer;

public class SeEmailService
{
	private static final Logger LOG = LoggerFactory.getLogger(SeEmailService.class);

	private static final boolean DEBUG = true;

	private final String server = AppProperties.PROP_MAIL_OUT_SERVER.getValue();
	private final Integer port = AppProperties.PROP_MAIL_OUT_PORT.getIntegerValue();
	private final String username = AppProperties.PROP_MAIL_OUT_USERNAME.getValue();
	private final String password = AppProperties.PROP_MAIL_OUT_PASSWORD.getValue();
	private final Boolean enaled_ssl = AppProperties.PROP_MAIL_OUT_ENALED_SSL.getBooleanValue();
	private final String fromAddress = AppProperties.PROP_MAIL_OUT_ADDRESS.getValue();
	private final String fromPerson = AppProperties.PROP_MAIL_OUT_PERSON.getValue();

	private Mailer mailer;
	private String to = null;
	private String subject = null;
	private String plainBody = null;
	private String htmlBody = null;
	private Path pathAttach = null;
	private LocalDateTime sentDate = null;

	public LocalDateTime getSentDate()
	{
		return sentDate;
	}

	public static SeEmailService create()
	{
		SeEmailService seMailService = new SeEmailService();

		seMailService.mailer = MailerBuilder
			.withSMTPServer(seMailService.server, seMailService.port, seMailService.username, seMailService.password)
			.withTransportStrategy(seMailService.enaled_ssl ? TransportStrategy.SMTPS : TransportStrategy.SMTP)
			.buildMailer();

		return seMailService;
	}

	public SeEmailService withTemplate(String to, SeEmailTemplate seEmailTemplate, Replacer replacer)
	{
		return with(to, seEmailTemplate.getSubject(), seEmailTemplate.getPlainBody(), seEmailTemplate.getHtmlBody(), replacer);
	}

	public SeEmailService with(String to, String subject, String plainBody)
	{
		this.to = to;
		this.subject = subject;
		this.plainBody = plainBody;

		return this;
	}

	public SeEmailService with(String to, String subject, String plainBody, String htmlBody, Replacer replacer)
	{
		this.to = to;
		this.subject = replacer.replace(subject);
		this.plainBody = (plainBody != null && !plainBody.isBlank()) ? replacer.replace(plainBody) : null;
		this.htmlBody = (htmlBody != null && !htmlBody.isBlank()) ? replacer.replace(htmlBody) : null;

		return this;
	}

	public SeEmailService withAttach(Path pathAttach)
	{
		this.pathAttach = pathAttach;
		return this;
	}

	public SeEmailService send() throws IOException, DocumentException
	{
		if (!DEBUG)
		{
			EmailPopulatingBuilder epb = EmailBuilder.startingBlank()
					.from(fromPerson, fromAddress)
					.to(to)
					.withSubject(subject)
					.withPlainText(plainBody)
					.withHTMLText(htmlBody);

			if (pathAttach != null)
			{
				withAttachment(epb, pathAttach);
			}

			Email email = epb.buildEmail();

			mailer.sendMail(email);
		}
		else
		{
			debug(to, subject, plainBody, htmlBody);
		}

		sentDate = DateTimeUtils.now();

		return this;
	}

	private void withAttachment(EmailPopulatingBuilder epb, Path pathAttach) throws IOException, DocumentException
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		PdfReader reader = new PdfReader(pathAttach.toString());
		reader.selectPages("1");
		PdfStamper stamper = new PdfStamper(reader, outputStream);
		stamper.close();

		String name = pathAttach.getFileName().toString();
		byte[] data = outputStream.toByteArray();
		String mimetype = Files.probeContentType(pathAttach);

		epb.withAttachment (name, data, mimetype);
	}

	public void sendAsync()
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					send();
				}
				catch (Exception e)
				{
					LOG.error("Error sending mail", e);
				}
			}
		});
		thread.setName("mailSender");
		thread.start();
	}

	private void debug(String to, String subject, String plainBody, String htmlBody)
	{
		try	{
			Thread.sleep(3000);
		} catch (InterruptedException e) { }

		System.out.println("+-------- MAIL --------+");
		System.out.println("to: " + to);
		System.out.println("subject: " + subject);
		System.out.println("+----------------------+");
		System.out.println("plain body:");
		System.out.println(plainBody);
		System.out.println("+----------------------+");
		System.out.println("html body:");
		System.out.println(htmlBody);
		System.out.println("+----------------------+");
		System.out.println("");
	}

}
