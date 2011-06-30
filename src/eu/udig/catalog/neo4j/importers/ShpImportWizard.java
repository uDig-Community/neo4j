package eu.udig.catalog.neo4j.importers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.neo4j.gis.spatial.ShapefileImporter;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;

import eu.udig.catalog.neo4j.ProgressMonitorWrapper;

/**
 * Wizard to import an ESRI Shapefile in a Neo4j DataStore.
 * 
 * @author Davide Savazzi
 * @author Craig Taverner
 */
public class ShpImportWizard extends Neo4jImportWizard {

    protected void importFile(IProgressMonitor monitor, Neo4jSpatialDataStore dataStore, String filePath, String layerName) throws Exception {
		ShapefileImporter importer = new ShapefileImporter(
				dataStore.getSpatialDatabaseService().getDatabase(), new ProgressMonitorWrapper("Importing...", monitor), 1000);
    	importer.importFile(filePath, layerName);
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
    	initialize(workbench, selection, "SHP", "Shapefile", new String[] { "*.shp", "*.SHP" });
	}

}
