/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: August 2020
 *
 */
package sanzol.util.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Version;
import sanzol.util.properties.Property.SourceType;
import sanzol.util.security.Cipher;

public final class PropertiesFile
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFile.class);

	private static final String PROPERTIES_FILE = "config.properties";

	private PropertiesFile()
	{
		// Hide constructor
	}

	public static void load(Map<String, Property> lstProperties)
	{
		if (!loadFromFile(lstProperties))
		{
			loadFromResource(lstProperties);
		}
	}

	public static boolean loadFromFile(Map<String, Property> lstProperties)
	{
		try
		{
			File file = getPropertiesFile();
			if (file != null)
			{
				String text = FileUtils.readFileToString(file, StandardCharsets.ISO_8859_1);
				loadString(lstProperties, text);
				return true;
			}
		}
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesFile (file)", e);
		}
		return false;
	}

	public static boolean loadFromFile(Map<String, Property> lstProperties, String filename)
	{
		try
		{
			File file = new File(filename);
			String text = FileUtils.readFileToString(file, StandardCharsets.ISO_8859_1);
			loadString(lstProperties, text);
			return true;
		}
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesFile (file)", e);
		}
		return false;
	}

	private static File getPropertiesFile()
	{
		File f;

		String arg_location = AppProperties.PROP_CONFIG_LOCATION.getValue();
		if (arg_location != null && !arg_location.isEmpty())
		{
			f = new File(arg_location, PROPERTIES_FILE);
			if (f.exists() && !f.isDirectory())
			{
				return f;
			}
		}

		String userDir = System.getProperty("user.dir");
		if (userDir != null && !userDir.isEmpty())
		{
			f = new File(userDir, PROPERTIES_FILE);
			if (f.exists() && !f.isDirectory())
			{
				return f;
			}
		}

		String userHome = System.getProperty("user.home");
		if (userHome != null && !userHome.isEmpty())
		{
			String clientVendor = Version.getClient() != null ? Version.getClient() : Version.getVendor();
			Path p = Paths.get(userHome, clientVendor, Version.getProduct(), PROPERTIES_FILE);
			f = p.toFile();
			if (f.exists() && !f.isDirectory())
			{
				return f;
			}
		}

		return null;
	}

	public static boolean loadFromResource(Map<String, Property> lstProperties)
	{
		try
		{
			InputStream in = PropertiesFile.class.getResourceAsStream("/" + PROPERTIES_FILE);
			StringWriter writer = new StringWriter();

			IOUtils.copy(in, writer, StandardCharsets.ISO_8859_1);
			String text = writer.toString();

			writer.close();
			in.close();

			loadString(lstProperties, text);

			return true;
		}
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesFile (resource)", e);
			return false;
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
			if (property != null && property.getSourceType() == SourceType.FILE)
			{
				if (property.isHidden())
				{
					if (value.startsWith("***/"))
					{
						property.setValue(Cipher.decrypt(value.substring(4), Cipher.Codec.Hex));
					}
					else
					{
						property.setValue(value);
					}
				}
				else
				{
					property.setValue(value);
				}
			}
		}
	}

	public static String toText(Map<String, Property> lstProperties) throws GeneralSecurityException
	{
		StringBuffer sb = new StringBuffer();

		for (Map.Entry<String, Property> entry : lstProperties.entrySet())
		{
			Property property = entry.getValue();

			if (property.getSourceType() == SourceType.FILE)
			{
				if (property.isHidden())
				{
					sb.append(property.getName() + "=***/" + Cipher.encrypt(property.getValue(), Cipher.Codec.Hex) + "\r\n");
				}
				else
				{
					sb.append(property.getName() + "=" + property.getValue() + "\r\n");
				}
			}

		}

		return sb.toString();
	}

	public static void save(Map<String, Property> lstProperties, File file) throws IOException, GeneralSecurityException
	{
		if (lstProperties != null && !lstProperties.isEmpty())
		{
			String text = toText(lstProperties);
			FileUtils.writeStringToFile(file, text, StandardCharsets.ISO_8859_1);
		}
	}

}
