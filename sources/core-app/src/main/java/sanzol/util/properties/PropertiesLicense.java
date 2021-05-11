/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.se.license.LicenseService;
import sanzol.util.properties.Property.SourceType;
import sanzol.util.security.Cipher;
import sanzol.util.security.Cipher.Codec;

public final class PropertiesLicense
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesLicense.class);

	private static final String LICENSE_FILENAME = "/license.txt";

	private PropertiesLicense()
	{
		// Hide constructor
	}

	public static void load(Map<String, Property> lstProperties)
	{
		try
		{
			InputStream in = LicenseService.class.getResourceAsStream(LICENSE_FILENAME);
			StringWriter writer = new StringWriter();

			IOUtils.copy(in, writer, StandardCharsets.ISO_8859_1);
			String text = writer.toString();

			writer.close();
			in.close();

			loadString(lstProperties, Cipher.decrypt(text, Codec.Hex));
		}
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesLicense", e);
		}
	}

	private static void loadString(Map<String, Property> lstProperties, String text) throws IOException, GeneralSecurityException
	{
		BufferedReader bufReader = new BufferedReader(new StringReader(text));
		String line = null;
		while ((line = bufReader.readLine()) != null)
		{
			if (line == null || line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}

			int index = line.indexOf("=");
			if (index < 0)
			{
				continue;
			}

			String key = line.substring(0, index).trim();
			String value = line.substring(index + 1).trim();

			if (key.isEmpty() || value.isEmpty())
			{
				continue;
			}

			Property property = lstProperties.get(key);
			if (property != null && property.getSourceType() == SourceType.LICENSE)
			{
				property.setValue(value);
			}
		}
	}

}
