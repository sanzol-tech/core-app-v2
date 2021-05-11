package sanzol.se.services;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.primefaces.context.PrimeApplicationContext;

import com.sun.management.OperatingSystemMXBean;

import sanzol.app.config.AppProperties;
import sanzol.app.config.Application;
import sanzol.app.config.HibernateUtil;
import sanzol.se.commons.FacesUtils;
import sanzol.se.model.SeServerInfo;
import sanzol.se.model.SeServerInfo.ServerInfoStatus;
import sanzol.util.DateTimeUtils;
import sanzol.util.ExceptionUtils;
import sanzol.util.NTPClient;
import sanzol.util.NetUtils;
import sanzol.util.Utils;
import sanzol.util.i18n.DateFormatter;
import sanzol.util.persistence.Dialects;

public class SeServerInfoService
{
	private static SeServerInfoService seServerInfoService;

	private List<SeServerInfo> lstInfoEnvironment;
	private List<SeServerInfo> lstInfoDB;
	private List<SeServerInfo> lstInfoTime;
	private List<SeServerInfo> lstInfoCPU;
	private List<SeServerInfo> lstInfoMem;
	private List<SeServerInfo> lstInfoDisk;
	private List<SeServerInfo> lstInfoNet;
	private List<SeServerInfo> lstInfoVersions;

	public List<SeServerInfo> getLstInfoEnvironment()
	{
		return lstInfoEnvironment;
	}

	public List<SeServerInfo> getLstInfoDB()
	{
		return lstInfoDB;
	}

	public List<SeServerInfo> getLstInfoTime()
	{
		return lstInfoTime;
	}

	public List<SeServerInfo> getLstInfoCPU()
	{
		return lstInfoCPU;
	}

	public List<SeServerInfo> getLstInfoMem()
	{
		return lstInfoMem;
	}

	public List<SeServerInfo> getLstInfoDisk()
	{
		return lstInfoDisk;
	}

	public List<SeServerInfo> getLstInfoNet()
	{
		return lstInfoNet;
	}

	public List<SeServerInfo> getLstInfoVersions()
	{
		return lstInfoVersions;
	}

	private SeServerInfoService()
	{
		//
	}

	public static synchronized SeServerInfoService getInstance()
	{
		if (seServerInfoService == null)
		{
			seServerInfoService = new SeServerInfoService();
			seServerInfoService.load();
		}

		return seServerInfoService;
	}

	private long lastProcessMillis;
	private long lastExecTimeMillis;

	private void load()
	{
		long t1 = System.currentTimeMillis();

		lstInfoEnvironment = getInfoEnvironment();
		lstInfoDB = getInfoDB();
		lstInfoTime = getInfoTime();
		lstInfoCPU = getInfoCPU();
		lstInfoMem = getInfoMem();
		lstInfoDisk = getInfoDisk();
		lstInfoNet = getInfoNet();
		lstInfoVersions = getInfoVersions();

		long t2 = System.currentTimeMillis();

		lastProcessMillis = t2;
		lastExecTimeMillis = t2 - t1;
	}

	public synchronized void refresh()
	{
		long t1 = System.currentTimeMillis();

		if (t1 < lastProcessMillis + (lastExecTimeMillis * 3))
		{
			return;
		}

		lstInfoEnvironment = getInfoEnvironment();
		lstInfoDB = getInfoDB();
		lstInfoTime = getInfoTime();
		lstInfoCPU = getInfoCPU();
		lstInfoMem = getInfoMem();
		lstInfoDisk = getInfoDisk();
		lstInfoNet = getInfoNet();

		long t2 = System.currentTimeMillis();

		lastProcessMillis = t2;
		lastExecTimeMillis = t2 - t1;
	}

	// ----------------------------------------------------------------------------------------------------
	// ENVIRONMENT
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoEnvironment()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		lstInfo.add(new SeServerInfo("PID", String.valueOf(ProcessHandle.current().pid())));
		lstInfo.add(getServerStartUp());

		lstInfo.add(new SeServerInfo("", ""));
		lstInfo.add(new SeServerInfo("java.vm.name", System.getProperty("java.vm.name")));
		lstInfo.add(new SeServerInfo("java.vm.version", System.getProperty("java.vm.version")));
		lstInfo.add(new SeServerInfo("java.vm.info", System.getProperty("java.vm.info")));
		lstInfo.add(new SeServerInfo("java.version", System.getProperty("java.version")));
		lstInfo.add(new SeServerInfo("java.vendor", System.getProperty("java.vendor")));
		lstInfo.add(new SeServerInfo("java.home", System.getProperty("java.home")));

		lstInfo.add(new SeServerInfo("", ""));
		lstInfo.add(new SeServerInfo("os.name", System.getProperty("os.name")));
		lstInfo.add(new SeServerInfo("os.arch", System.getProperty("os.arch")));
		lstInfo.add(new SeServerInfo("os.version", System.getProperty("os.version")));

		lstInfo.add(new SeServerInfo("", ""));
		lstInfo.add(new SeServerInfo("user.name", System.getProperty("user.name")));
		lstInfo.add(new SeServerInfo("user.home", System.getProperty("user.home")));
		lstInfo.add(new SeServerInfo("user.languare", System.getProperty("user.language")));
		lstInfo.add(new SeServerInfo("user.country", System.getProperty("user.country")));
		lstInfo.add(new SeServerInfo("user.timezone", System.getProperty("user.timezone")));

		lstInfo.add(new SeServerInfo("", ""));

		ServletContext servletContext = FacesUtils.getServletContext();
		lstInfo.add(new SeServerInfo("Web Server", servletContext.getServerInfo()));
		lstInfo.add(new SeServerInfo("Servlet version", servletContext.getMajorVersion() + "." + servletContext.getMinorVersion()));

		return lstInfo;
	}

	private SeServerInfo getServerStartUp()
	{
		String startUpTime = DateFormatter.getFormatterZonedDateTime().format(Application.getStartUpTime());
		String readableDateDiff = DateTimeUtils.readableDateDiff(Application.getStartUpTime(), ZonedDateTime.now());
		return new SeServerInfo("Start Server", readableDateDiff + " ( " + startUpTime + " )");
	}

	// ----------------------------------------------------------------------------------------------------
	// DB
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoDB()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		getDBMetaData(lstInfo);

		return lstInfo;
	}

	private void getDBMetaData(List<SeServerInfo> lstInfo)
	{
		String dbengine = AppProperties.PROP_BASE_ENGINE.getValue();
		if (dbengine != null)
		{
			if (Dialects.DBENGINE_MYSQL.equalsIgnoreCase(dbengine))
			{
				getMySqlMetaData(lstInfo);
			}
			else if (Dialects.DBENGINE_ORACLE.equalsIgnoreCase(dbengine))
			{
				getOracleMetaData(lstInfo);
			}
			else if (Dialects.DBENGINE_POSTGRESQL.equalsIgnoreCase(dbengine))
			{
				getPostgreeSqlMetaData(lstInfo);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void getMySqlMetaData(List<SeServerInfo> lstInfo)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			NativeQuery<Object[]> query = hbSession.createSQLQuery("select @@version_comment, @@version, @@hostname, @@basedir, @@datadir");
			Object[] result = (Object[]) query.uniqueResult();

			String versionComment = (String) result[0];
			String version = (String) result[1];
			String dbVersion = versionComment + " " + version;

			String dbHostName = (String) result[2];
			String dbBasedir = (String) result[3];
			String dbDatadir = (String) result[4];

			String sql_startup
				= "SELECT NOW() - INTERVAL variable_value SECOND MySQL_Started"
				+ " FROM performance_schema.global_status"
				+ " WHERE variable_name='Uptime'";

			NativeQuery<Timestamp> query2 = hbSession.createSQLQuery(sql_startup);
			Timestamp dbStartupTime = (Timestamp) query2.uniqueResult();

			lstInfo.add(new SeServerInfo("DB Version", dbVersion));
			lstInfo.add(new SeServerInfo("DB Startup", DateFormatter.getFormatterDateTime().format(dbStartupTime.toLocalDateTime())));
			lstInfo.add(new SeServerInfo("db HostName", dbHostName));
			lstInfo.add(new SeServerInfo("Base directory", dbBasedir));
			lstInfo.add(new SeServerInfo("Data directory", dbDatadir));

			tx.commit();

			if (dbDatadir != null && !dbDatadir.isBlank())
			{
				lstInfo.add(getDBFreeDiskSpace(dbDatadir));
			}
		}
		catch (Exception e)
		{
			lstInfo.add(new SeServerInfo("MySql MetaData", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR));

			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void getPostgreeSqlMetaData(List<SeServerInfo> lstInfo)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			NativeQuery<String> query1 = hbSession.createSQLQuery("SELECT version()");
			String dbVersion = (String) query1.uniqueResult();
			lstInfo.add(new SeServerInfo("db Version", dbVersion));

			NativeQuery<String> query2 = hbSession.createSQLQuery("select setting from pg_settings where name = 'data_directory'");
			String dbDatadir = (String) query2.uniqueResult();
			lstInfo.add(new SeServerInfo("db Data directory", dbDatadir));

			NativeQuery<Timestamp> query3 = hbSession.createSQLQuery("SELECT pg_postmaster_start_time()");
			Timestamp dbStartupTime = (Timestamp) query3.uniqueResult();
			lstInfo.add(new SeServerInfo("db Startup", DateFormatter.getFormatterDateTime().format(dbStartupTime.toLocalDateTime())));

			NativeQuery<String> query4 = hbSession.createSQLQuery("SELECT current_user");
			String dbUser = (String) query4.uniqueResult();
			lstInfo.add(new SeServerInfo("db User", dbUser));

			tx.commit();
		}
		catch (Exception e)
		{
			lstInfo.add(new SeServerInfo("PostgreeSql MetaData", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR));

			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}
	}

	@SuppressWarnings("unchecked")
	private void getOracleMetaData(List<SeServerInfo> lstInfo)
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			NativeQuery<Object[]> query = hbSession.createSQLQuery("SELECT INSTANCE_NAME, HOST_NAME, VERSION, STARTUP_TIME FROM V$INSTANCE");
			Object[] result = (Object[]) query.uniqueResult();

			String dbInstanceName = (String) result[0];
			String dbHostName = (String) result[1];
			String dbVersion = (String) result[2];
			Timestamp dbStartupTime = (Timestamp) result[3];

			lstInfo.add(new SeServerInfo("db InstanceName", dbInstanceName));
			lstInfo.add(new SeServerInfo("db HostName", dbHostName));
			lstInfo.add(new SeServerInfo("db Version", dbVersion));
			lstInfo.add(new SeServerInfo("db Startup", DateFormatter.getFormatterDateTime().format(dbStartupTime.toLocalDateTime())));

			tx.commit();
		}
		catch (Exception e)
		{
			lstInfo.add(new SeServerInfo("Oracle MetaData", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR));

			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
		}
		finally
		{
			hbSession.close();
		}
	}

	private SeServerInfo getDBFreeDiskSpace(String path)
	{
		final long MEGABYTE = 1048576;

		File file = new File(path);
		long usableSpace = file.getUsableSpace();
		return new SeServerInfo("Disk space in data dir", Utils.readableMemorySize(usableSpace), usableSpace < 10 * MEGABYTE ? ServerInfoStatus.WARN : ServerInfoStatus.OK);
	}

	// ----------------------------------------------------------------------------------------------------
	// TIME
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoTime()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		lstInfo.add(new SeServerInfo("Server Time", DateFormatter.getFormatterZonedDateTime().format(ZonedDateTime.now())));
		lstInfo.add(getDBCurrentTime());
		lstInfo.add(testNTPServer());

		return lstInfo;
	}

	private SeServerInfo testNTPServer()
	{
		String NTPServer = AppProperties.PROP_NTP_SERVER.getValue();
		try
		{
			Instant instant = NTPClient.getWebTime(NTPServer);
			String strTime = DateFormatter.getFormatterZonedDateTime().format(instant.atZone(ZoneId.of("UTC")));
			return new SeServerInfo(NTPServer, strTime);
		}
		catch (Exception e)
		{
			return new SeServerInfo("NTP Server", NTPServer + " : " + ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	private SeServerInfo getDBCurrentTime()
	{
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		Session hbSession = sessionFactory.getCurrentSession();
		Transaction tx = null;
		try
		{
			tx = hbSession.beginTransaction();

			String sql;

			switch (AppProperties.PROP_BASE_ENGINE.getValue())
			{
			case Dialects.DBENGINE_MYSQL:
				sql = "select now()";
				break;
			case Dialects.DBENGINE_ORACLE:
				sql = "select sysdate from dual";
				break;
			case Dialects.DBENGINE_POSTGRESQL:
				sql = "select now()";
				break;
			case Dialects.DBENGINE_SQLSERVER:
				sql = "select getdate()";
				break;
			default:
				sql = "select current_timestamp()";
				break;
			}

			NativeQuery<Timestamp> query = hbSession.createSQLQuery(sql);
			Timestamp dbCurrentTime = (Timestamp) query.uniqueResult();

			tx.commit();

			return new SeServerInfo("DB Time", DateFormatter.getFormatterDateTime().format(dbCurrentTime.toLocalDateTime()));
		}
		catch (Exception e)
		{
			if (tx != null && tx.getStatus().canRollback())
			{
				tx.rollback();
			}
			return new SeServerInfo("DB Time", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR);
		}
		finally
		{
			hbSession.close();
		}
	}

	// ----------------------------------------------------------------------------------------------------
	// CPU
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoCPU()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		lstInfo.add(new SeServerInfo("AvailableProcessors", String.valueOf(osBean.getAvailableProcessors())));

		long systemCpuLoad = Math.round(osBean.getSystemCpuLoad() * 100);
		lstInfo.add(new SeServerInfo("SystemCpuLoad", systemCpuLoad + " %"));

		long processCpuLoad = Math.round(osBean.getProcessCpuLoad() * 100);
		lstInfo.add(new SeServerInfo("ProcessCpuLoad", processCpuLoad + " %"));

		lstInfo.add(new SeServerInfo("Live Threads", String.valueOf(ManagementFactory.getThreadMXBean().getThreadCount())));
		lstInfo.add(new SeServerInfo("Daemon Threads", String.valueOf(ManagementFactory.getThreadMXBean().getDaemonThreadCount())));

		return lstInfo;
	}

	// ----------------------------------------------------------------------------------------------------
	// MEM
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoMem()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		long jvmFreeMemory = Runtime.getRuntime().freeMemory();
		long jvmMaxMemory = Runtime.getRuntime().maxMemory();
		long jvmTotalMemory = Runtime.getRuntime().totalMemory();

		lstInfo.add(new SeServerInfo("JVM Free memory", Utils.readableMemorySize(jvmFreeMemory)));
		lstInfo.add(new SeServerInfo("JVM Max memory", Utils.readableMemorySize(jvmMaxMemory)));
		lstInfo.add(new SeServerInfo("JVM Total memory", Utils.readableMemorySize(jvmTotalMemory)));

		lstInfo.add(new SeServerInfo("", ""));

		OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

		lstInfo.add(new SeServerInfo("CommittedVirtualMemorySize", Utils.readableMemorySize(osBean.getCommittedVirtualMemorySize())));
		lstInfo.add(new SeServerInfo("FreePhysicalMemorySize", Utils.readableMemorySize(osBean.getFreePhysicalMemorySize())));
		lstInfo.add(new SeServerInfo("TotalPhysicalMemorySize", Utils.readableMemorySize(osBean.getTotalPhysicalMemorySize())));
		lstInfo.add(new SeServerInfo("TotalSwapSpaceSize", Utils.readableMemorySize(osBean.getTotalSwapSpaceSize())));

		return lstInfo;
	}

	// ----------------------------------------------------------------------------------------------------
	// Disk
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoDisk()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		ServletContext servletContext = FacesUtils.getServletContext();
		String rootPath = servletContext.getRealPath("/");
		lstInfo.add(new SeServerInfo("Deploy path", rootPath));

		File file = new File(rootPath);
		long usableSpace = file.getUsableSpace();
		long totalSpace = file.getTotalSpace();
		long freeRatio = (usableSpace * 100) / totalSpace;
		lstInfo.add(new SeServerInfo("Free disk space", Utils.readableMemorySize(usableSpace), freeRatio < 10 ? ServerInfoStatus.WARN : ServerInfoStatus.OK));
		lstInfo.add(new SeServerInfo("Total disk space", Utils.readableMemorySize(totalSpace)));

		return lstInfo;
	}

	// ----------------------------------------------------------------------------------------------------
	// NET
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoNet()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		getHostName(lstInfo);
		getNetInfo(lstInfo);

		return lstInfo;
	}

	private void getHostName(List<SeServerInfo> lstInfo)
	{
		try
		{
			InetAddress inetAddress = InetAddress.getLocalHost();
			lstInfo.add(new SeServerInfo("Hostname", inetAddress.getHostName()));
		}
		catch (UnknownHostException e)
		{
			lstInfo.add(new SeServerInfo("Hostname", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR));
		}
	}

	private void getNetInfo(List<SeServerInfo> lstInfo)
	{
		try
		{
			lstInfo.add(new SeServerInfo("Interfaces", NetUtils.getInfo()));
		}
		catch (Exception e)
		{
			lstInfo.add(new SeServerInfo("Interfaces", ExceptionUtils.getMessage(e), ServerInfoStatus.ERROR));
		}
	}

	// ----------------------------------------------------------------------------------------------------
	// Versions
	// ----------------------------------------------------------------------------------------------------

	private List<SeServerInfo> getInfoVersions()
	{
		List<SeServerInfo> lstInfo = new ArrayList<SeServerInfo>();

		lstInfo.add(new SeServerInfo("JavaServer Faces", FacesContext.class.getPackage().getImplementationTitle() + " " + FacesContext.class.getPackage().getImplementationVersion()));
		lstInfo.add(new SeServerInfo("PrimeFaces", PrimeApplicationContext.getCurrentInstance(FacesContext.getCurrentInstance()).getEnvironment().getBuildVersion()));
		lstInfo.add(new SeServerInfo(org.omnifaces.util.Faces.class.getPackage().getSpecificationTitle(), org.omnifaces.util.Faces.class.getPackage().getSpecificationVersion()));
		lstInfo.add(new SeServerInfo("Hibernate", org.hibernate.Version.getVersionString()));

		return lstInfo;
	}

}
