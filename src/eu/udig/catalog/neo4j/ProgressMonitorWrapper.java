package eu.udig.catalog.neo4j;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.neo4j.gis.spatial.Listener;


/**
 * Implementation of a Neo4j-Spatial Listener that 
 * manage an Eclipse IProgressMonitor implementation.
 * 
 * @author Davide Savazzi
 * @author Craig Taverner
 */
public class ProgressMonitorWrapper implements Listener {
	private String taskName;
	private IProgressMonitor monitor;

	public ProgressMonitorWrapper(String taskName, IProgressMonitor monitor) {
		if (monitor == null) monitor = new NullProgressMonitor();
		
		this.taskName = taskName;
		this.monitor = monitor;
	}
	
	public void setTaskName(String name) {
		this.taskName = name;
	}
	
	public void begin(int unitsOfWork) {
		monitor.beginTask(taskName, unitsOfWork);
	}

	public void worked(int workedSinceLastNotification) {
		monitor.worked(workedSinceLastNotification);
	}

	public void done() {
		monitor.done();
	}
	
}
