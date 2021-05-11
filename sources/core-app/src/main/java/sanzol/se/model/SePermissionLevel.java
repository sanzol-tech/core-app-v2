/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: June 2020
 *
 */
package sanzol.se.model;

import java.io.Serializable;

import sanzol.se.model.entities.SePermission;

public class SePermissionLevel implements Serializable
{
	private static final long serialVersionUID = 1L;

	private SePermission sePermission;
	private int level;

	public SePermission getSePermission()
	{
		return sePermission;
	}

	public void setSePermission(SePermission sePermission)
	{
		this.sePermission = sePermission;
	}

	public int getLevel()
	{
		return level;
	}

	public void setLevel(int level)
	{
		this.level = level;
	}

}