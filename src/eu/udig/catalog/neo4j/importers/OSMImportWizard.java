package eu.udig.catalog.neo4j.importers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.neo4j.gis.spatial.ShapefileImporter;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.osm.OSMImporter;
import org.neo4j.graphdb.GraphDatabaseService;

import eu.udig.catalog.neo4j.ProgressMonitorWrapper;

/**
 * Wizard to import an ESRI Shapefile in a Neo4j DataStore.
 * 
 * @author Davide Savazzi
 * @author Craig Taverner
 */
public class OSMImportWizard extends Neo4jImportWizard {

    protected void importFile(IProgressMonitor monitor, Neo4jSpatialDataStore dataStore, String filePath, String layerName) throws Exception {
    	OSMImporter importer = new OSMImporter(layerName);
    	GraphDatabaseService database = dataStore.getSpatialDatabaseService().getDatabase();
    	importer.importFile(database, filePath);
    	importer.reIndex(database);
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
    	initialize(workbench, selection, "OSM", "OpenStreetMap", new String[] { "*.osm", "*.OSM" });
	}

}
