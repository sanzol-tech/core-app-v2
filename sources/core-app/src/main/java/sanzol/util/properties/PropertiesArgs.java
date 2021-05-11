/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: August 2020
 *
 */
package sanzol.util.properties;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.util.properties.Property.PropertyType;
import sanzol.util.properties.Property.SourceType;
import sanzol.util.security.Cipher;

public final class PropertiesArgs
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesArgs.class);

	private PropertiesArgs()
	{
		// Hide constructor
	}

	public static void load(Map<String, Property> lstProperties, String[] args)
	{
		try
		{
			for (String arg : args)
			{
				if (!arg.startsWith("--"))
				{
					continue;
				}

				String key;
				String value;
				int x = arg.indexOf("=");
				if (x >= 0)
				{
					key = arg.substring(2, x);
					value = arg.substring(x + 1);
				}
				else
				{
					key = arg.substring(2);
					value = null;
				}

				Property property = lstProperties.get(key);
				if (property != null && property.getSourceType() == SourceType.ARGUMENT)
				{
					if (property.getPropertyType() == PropertyType.BOOLEAN && value == null)
					{
						value = "true";
					}

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
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesArgs", e);
		}
	}

}
