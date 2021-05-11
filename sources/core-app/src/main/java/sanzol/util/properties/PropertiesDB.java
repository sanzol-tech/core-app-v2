/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.util.properties;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sanzol.se.context.ContextLogger;
import sanzol.se.context.RequestContext;
import sanzol.se.model.entities.SeProperty;
import sanzol.se.services.SePropertiesService;
import sanzol.util.properties.Property.SourceType;

public final class PropertiesDB
{
	private static final Logger LOG = LoggerFactory.getLogger(PropertiesDB.class);

	private PropertiesDB()
	{
		// Hide constructor
	}

	public static void load(Map<String, Property> lstProperties)
	{
		try (RequestContext context = RequestContext.createContext(null))
		{
			SePropertiesService service = new SePropertiesService(context);
			List<SeProperty> lstSeProperties = service.getSeProperties();

			ContextLogger contextLogger = context.getMsgLogger();
			if (contextLogger.hasErrorOrFatal())
			{
				LOG.error(contextLogger.getLastErrorOrFatal().getMessage());

				Runtime.getRuntime().halt(0);
			}

			for (SeProperty seProperty : lstSeProperties)
			{
				Property property = lstProperties.get(seProperty.getName());
				if (property != null && property.getSourceType() == SourceType.DB)
				{
					property.setValue(seProperty.getDecryptValue());
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Error loading PropertiesDB", e);
		}
	}

}
