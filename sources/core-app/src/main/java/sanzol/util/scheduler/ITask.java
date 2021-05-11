/**
 *
 * AUTHOR: Fernando Elenberg Sanzol <f@sanzol.com.ar>
 * CREATE: August 2020
 *
 */
package sanzol.util.scheduler;

public interface ITask
{
	public void execute();

	public void stop();
}
