/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: February 2021
 *
 */
package sanzol.util;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public final class NetUtils
{

	private NetUtils()
	{
		// Hide constructor
	}

	public static String getInfo() throws SocketException, UnknownHostException
	{
		StringBuilder sb = new StringBuilder();

		ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

		for (NetworkInterface iface : interfaces)
		{
			if (iface.isUp() && !iface.isLoopback())
			{
				sb.append("Interface Name: " + iface.getName()).append("\n");
				sb.append("Interface display name: " + iface.getDisplayName()).append("\n");
				sb.append("Hardware Address: " + macFormatter(iface.getHardwareAddress())).append("\n");

				sb.append("\t").append("Interface addresses: ").append("\n");

				for (InterfaceAddress addr : iface.getInterfaceAddresses())
				{
					InetAddress inetAddress = addr.getAddress();
					if (inetAddress instanceof Inet4Address)
						sb.append("\t\t").append("ipv4 " + inetAddress.getHostAddress()).append("\n");
					else if (inetAddress instanceof Inet6Address)
						sb.append("\t\t").append("ipv6 " + inetAddress.getHostAddress()).append("\n");
				}

				sb.append("\t").append("MTU: " + iface.getMTU()).append("\n");
				sb.append("\t").append("is virtual: " + iface.isVirtual()).append("\n");
				sb.append("\t").append("is point to point: " + iface.isPointToPoint()).append("\n");

				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String getInfoForHash()
	{
		StringBuilder sb = new StringBuilder();

		try
		{
			ArrayList<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());

			for (NetworkInterface iface : interfaces)
			{
				if (iface.isUp() && !iface.isLoopback())
				{
					sb.append(iface.getName()).append("{;}");
					sb.append(iface.getDisplayName()).append("{;}");
					sb.append(macFormatter(iface.getHardwareAddress())).append("{;}");
					sb.append("MTU:" + iface.getMTU()).append("{;}");
					sb.append("virtual:" + iface.isVirtual()).append("{;}");
					sb.append("p2p:" + iface.isPointToPoint()).append("{.}");
				}
			}
		}
		catch (Exception e)
		{
			sb.append(e.getMessage());
		}

		return sb.toString();
	}

	public static String macFormatter(byte[] mac)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mac.length; i++)
		{
			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
		}
		return sb.toString();
	}

}
