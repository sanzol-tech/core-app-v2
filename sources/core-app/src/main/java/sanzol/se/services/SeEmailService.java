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
	private SeEmailTemplate seEmailTemplate;
	private Replacer replacer;


	public static SeEmailService create()
	{
		SeEmailService seMailService = new SeEmailService();

		seMailService.mailer = MailerBuilder
			.withSMTPServer(seMailService.server, seMailService.port, seMailService.username, seMailService.password)
			.withTransportStrategy(seMailService.enaled_ssl ? TransportStrategy.SMTPS : TransportStrategy.SMTP)
			.buildMailer();

		return seMailService;
	}

	public SeEmailService withTemplate(SeEmailTemplate seEmailTemplate, Replacer replacer)
	{
		this.seEmailTemplate = seEmailTemplate;
		this.replacer = replacer;

		return this;
	}

	public void send(String to) throws IOException, DocumentException
	{
		 send(to, null);
	}

	public void send(String to, Path pathAttach) throws IOException, DocumentException
	{
		String subject = replacer.replace(seEmailTemplate.getSubject());
		String plainText = (seEmailTemplate.getPlainBody() != null && !seEmailTemplate.getPlainBody().isBlank()) ? replacer.replace(seEmailTemplate.getPlainBody()) : null;
		String htmlText = (seEmailTemplate.getHtmlBody() != null && !seEmailTemplate.getHtmlBody().isBlank()) ? replacer.replace(seEmailTemplate.getHtmlBody()) : null;

		if (!DEBUG)
		{
			EmailPopulatingBuilder epb = EmailBuilder.startingBlank()
					.from(fromPerson, fromAddress)
					.to(to)
					.withSubject(subject)
					.withPlainText(plainText)
					.withHTMLText(htmlText);

			if (pathAttach != null)
			{
				withAttachment(epb, pathAttach);
			}

			Email email = epb.buildEmail();

			mailer.sendMail(email);
		}
		else
		{
			debug(to, subject, plainText, htmlText);
		}
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

	public void sendAsync(String to)
	{
		sendAsync(to, null);
	}

	public void sendAsync(String to, Path attach)
	{
		Thread thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					send(to, attach);
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

	private void debug(String to, String subject, String plainText, String htmlText)
	{
		try	{
			Thread.sleep(3000);
		} catch (InterruptedException e) { }

		System.out.println("+-------- MAIL --------+");
		System.out.println("to: " + to);
		System.out.println("subject: " + subject);
		System.out.println("+----------------------+");
		System.out.println("plain body:");
		System.out.println(plainText);
		System.out.println("+----------------------+");
		System.out.println("html body:");
		System.out.println(htmlText);
		System.out.println("+----------------------+");
		System.out.println("");
	}

}
