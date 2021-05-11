/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.app.config;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HibernateUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(HibernateUtil.class);

	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory;

	public static SessionFactory getSessionFactory()
	{
		if (sessionFactory == null)
		{
			try
			{
				String hibernateConfig = AppProperties.PROP_BASE_HIBERNATE_CONFIG.getValue();
				String url = AppProperties.PROP_BASE_URL.getValue();
				String schema = AppProperties.PROP_BASE_SCHEMA.getValue();
				String catalog = AppProperties.PROP_BASE_CATALOG.getValue();
				String username = AppProperties.PROP_BASE_USERNAME.getValue();
				String password = AppProperties.PROP_BASE_PASSWORD.getValue();

				StandardServiceRegistryBuilder o = new StandardServiceRegistryBuilder().configure(hibernateConfig);
				o.applySetting("hibernate.connection.url", url);
				o.applySetting("hibernate.connection.username", username);
				o.applySetting("hibernate.connection.password", password);

				if (schema != null && !schema.isBlank())
				{
					o.applySetting("hibernate.default_schema", schema);
				}
				if (catalog != null && !catalog.isBlank())
				{
					o.applySetting("hibernate.default_catalog", catalog);
				}

				registry = o.build();

				// Create MetadataSources
				MetadataSources sources = new MetadataSources(registry);

				// Create Metadata
				Metadata metadata = sources.getMetadataBuilder().build();

				// Create SessionFactory
				sessionFactory = metadata.getSessionFactoryBuilder().build();
			}
			catch (Exception ex)
			{
				LOG.error("SessionFactory creation failed", ex);

				if (registry != null)
				{
					StandardServiceRegistryBuilder.destroy(registry);
				}
			}
		}

		return sessionFactory;
	}

	public static void shutdown()
	{
		if (registry != null)
		{
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}
}