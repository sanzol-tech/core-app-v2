/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: March 2021
 *
 */
package sanzol.util;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Instant;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public final class NTPClient
{

	private NTPClient()
	{
		// Hide constructor
	}

	public static Instant getWebTime(String address) throws IOException
	{
		NTPUDPClient client = new NTPUDPClient();
		client.open();
		client.setDefaultTimeout(500);
		client.setSoTimeout(500);
		InetAddress inetAddress = InetAddress.getByName(address);
		TimeInfo timeInfo = client.getTime(inetAddress);
		long t = timeInfo.getMessage().getTransmitTimeStamp().getTime();
		return Instant.ofEpochMilli(t);
	}

}