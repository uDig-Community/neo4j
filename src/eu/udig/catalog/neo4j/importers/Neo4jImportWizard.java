package eu.udig.catalog.neo4j.importers;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.URLUtils;
import net.refractions.udig.ui.ExceptionDetailsDialog;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStoreFactory;

import eu.udig.catalog.neo4j.Activator;
import eu.udig.catalog.neo4j.Neo4jSpatialGeoResource;
import eu.udig.catalog.neo4j.Neo4jSpatialService;

/**
 * Wizard to import a file in a Neo4j DataStore. This should be extended by
 * classes specific to the import format, and they should implement the abstract
 * method importFile(IProgressMonitor monitor, Neo4jSpatialDataStore dataStore,
 * String filePath, String layerName) in order to process the format specific
 * import.
 * 
 * @author Davide Savazzi
 * @author Craig Taverner
 */
public abstract class Neo4jImportWizard extends Wizard implements INewWizard {
	private ImportWizardPage mainPage;
	private static final String WIZ_GIF = "icons/shpwizard/worldimage_wiz.gif";

	protected void initialize(IWorkbench workbench, IStructuredSelection selection, String fileType, String name,
			String[] extensions) {
		mainPage = new ImportWizardPage(fileType, name, extensions);
		if (selection != null && !selection.isEmpty()) {
			for (Object selected : selection.toList()) {
				if (selected instanceof Neo4jSpatialService) {
					mainPage.setDatabase(((Neo4jSpatialService) selected).getDataStore(null));
					break;
				} else if (selected instanceof Neo4jSpatialGeoResource) {
					mainPage.setDatabase(((Neo4jSpatialGeoResource) selected).service().getDataStore(null));
				}
			}
		}
		setWindowTitle(mainPage.getTitle());
		ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();

		ImageDescriptor banner = imageRegistry.getDescriptor(WIZ_GIF);
		if (banner == null) {
			// URL bannerURL =
			// Activator.getDefault().getBundle().getEntry(WIZ_GIF);
			// banner = ImageDescriptor.createFromURL(bannerURL);
			// imageRegistry.put(WIZ_GIF, banner);
		}
		setDefaultPageImageDescriptor(banner);

		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		super.addPages();
		addPage(mainPage);
	}

	/**
	 * This is the main method format specific importing wizards need to
	 * implement
	 * 
	 * @param monitor
	 * @param dataStore
	 * @param filePath
	 * @param layerName
	 * @throws Exception
	 */
	protected abstract void importFile(IProgressMonitor monitor, Neo4jSpatialDataStore dataStore, String filePath, String layerName)
			throws Exception;

	public boolean performFinish() {
		// run with backgroundable progress monitoring
		IRunnableWithProgress operation = new IRunnableWithProgress() {
			public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				String filePath = mainPage.getFilename();

				// remove extension
				// filePath = filePath.substring(0, filePath.lastIndexOf("."));

				String layerName = mainPage.getLayerName();
				if (layerName == null || layerName.trim().equals("")) {
					layerName = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
				}

				String neo4jPath = mainPage.getNeo4jDir();
				if (!neo4jPath.endsWith(File.separator)) {
					neo4jPath += File.separator;
				}
				neo4jPath += "neostore.id";

				try {
					Map<String, Serializable> params = new HashMap<String, Serializable>();
					params.put(Neo4jSpatialDataStoreFactory.URLP.key, URLUtils.fileToURL(new File(neo4jPath)));
					importFile(monitor, Activator.getDefault().getDataStore(params), filePath, layerName);
				} catch (Throwable e) {
					e.printStackTrace();
					String message = "An error occurred while reading the " + mainPage.getTypeName();
					ExceptionDetailsDialog.openError(null, message, IStatus.ERROR, Activator.ID, e);
				}
			}
		};
		PlatformGIS.runInProgressDialog("Importing a " + mainPage.getTypeName() + " to a Neo4j Database", true, operation, true);
		return true;
	}

}
