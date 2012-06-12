package com.redhat.topicindex.session;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import com.redhat.topicindex.utils.SkynetExceptionUtilities;
import com.redhat.topicindex.utils.Constants;

/** This class is used to provide information on the state of the server. */
@Name("systemInfo")
public class SystemInfo
{
	@In
	private EntityManager entityManager;
	private String url;

	/**
	 * @return The current database connection url
	 */
	private String getConnectionUrl()
	{
		try
		{
			url = "";
			final Session sess = (Session) entityManager.getDelegate();
			sess.doWork(new Work()
			{
				@Override
				public void execute(Connection connection) throws SQLException
				{
					final DatabaseMetaData dbmd = connection.getMetaData();
					url = dbmd.getURL();
				}
			});
		}
		catch (final Exception ex)
		{
			SkynetExceptionUtilities.handleException(ex, false, "Probably an error getting the details of the database connection");
		}

		return url;
	}

	/**
	 * @return true if the system is connected to the live database, and false
	 *         otherwise
	 */
	public boolean isLiveDatabase()
	{
		return this.getConnectionUrl().indexOf(Constants.LIVE_SQL_SERVER) != -1;

	}
}
