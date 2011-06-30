package eu.udig.catalog.neo4j.importers;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.neo4j.gis.spatial.geotools.data.Neo4jSpatialDataStore;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;


/**
 * @author Davide Savazzi
 * @author Craig Taverner
 */
public class ImportWizardPage extends WizardPage {
	private String fileType;
	private String typeName;
	private String[] extensions;
    private File neo4jDir = null;
    private String neo4jDirPath = null;
    
    private File selectedFile = null;
    private String fileName = null;
    
    private Text layerNameField = null;
    private String layerName = null;
    
//    public static final String ID = "Neo4jImportWizardPage";

	public ImportWizardPage(String fileType, String typeName, String[] extensions) {
		super(fileType + "Neo4jImportWizardPage");
		this.fileType = fileType;
		this.typeName = typeName;
		this.extensions = extensions;
		setTitle("Import " + this.fileType + " to Neo4j");
		setDescription("Import a " + this.typeName + " to a Neo4j Database");
	}

	public String getFileType() {
		return fileType;
	}

	public String getTypeName() {
		return typeName;
	}

	public void createControl(Composite parent) {
        Composite area = new Composite(parent, SWT.NONE);
        area.setLayout(new GridLayout());
        
        Group dirInputGroup = createGroup(area, "Neo4j Database Directory");
        final Text dirText = createTextField(dirInputGroup, neo4jDirPath);
        dirText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setNeo4jDir(dirText.getText());
			}});
        final Button dirButton = new Button(dirInputGroup, SWT.PUSH);
        dirButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        dirButton.setText("...");
        dirButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
            	DirectoryDialog dirDialog = new DirectoryDialog(dirButton.getShell(), SWT.OPEN);
                String path = dirDialog.open();
                dirText.setText(path);
            }
        });
        
        Group fileInputGroup = createGroup(area, typeName);
	    final Text fileText = createTextField(fileInputGroup, "");
	    fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setSelectedFile(fileText.getText());
			}});
	    final Button fileButton = new Button(fileInputGroup, SWT.PUSH);
	    fileButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	    fileButton.setText("...");
	    fileButton.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
	    	public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
	    		FileDialog fileDialog = new FileDialog(fileButton.getShell(), SWT.OPEN);
	            fileDialog.setFilterExtensions(extensions);
	            String path = fileDialog.open();
	            fileText.setText(path);
	        }
	    });
	    
        Group layerInputGroup = createGroup(area, "Layer name");
	    layerNameField = createTextField(layerInputGroup, "");
	    
        setControl(area);
    }

    public void dispose() {
    	if (layerNameField != null) {
    		layerName = layerNameField.getText();
    	}
    	
    	super.dispose();
    }
    
    public String getLayerName() {
    	return layerName;
    }
    
    public String getFilename() {
        return fileName;
    }
    
    private void setSelectedFile(String path) {
        selectedFile = null;
        fileName = null;
    	
        if (path != null) {
        	File f = new File(path);
            if (f.exists()) {
                selectedFile = f;
                fileName = path;
                
                String layerName = fileName;
                layerName = layerName.substring(0, layerName.lastIndexOf("."));
    	        layerName = layerName.substring(layerName.lastIndexOf(File.separator) + 1);
    	        layerNameField.setText(layerName);
            } 
        }    	
        
    	checkFinish();
    }
    
    public String getNeo4jDir() {
    	return neo4jDirPath;
    }
    
    private void setNeo4jDir(String path) {
    	neo4jDir = null;
    	neo4jDirPath = null;
    	
        if (path != null) {
            File f = new File(path);
            if (f.exists()) {
            	neo4jDir = f;
            	neo4jDirPath = path;
            }
        }    	
        
    	checkFinish();
    }
    
    
    // Private methods

    private Group createGroup(Composite area, String name) {
    	Group group = new Group(area, SWT.None);
        group.setText(name);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
    	return group;
    }
    
    private Text createTextField(Group group, String value) {
	    Text text = new Text(group, SWT.SINGLE | SWT.LEAD | SWT.BORDER);
	    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	    text.setText(value == null ? "" : value);
	    return text;
    }
    
    private void checkFinish() {
    	this.setPageComplete(false); // disables finish button
    	//getShpImportWizard().disableFinishButton();
        if (isReadable(selectedFile) && selectedFile.isFile() && 
        		isReadable(neo4jDir) && neo4jDir.isDirectory() && neo4jDir.canWrite()) {
        	this.setPageComplete(true); // enables finish button
        	//getShpImportWizard().enableFinishButton();
        }
		if (getWizard() != null)
			getWizard().getContainer().updateButtons();
    }

//    private ShpImportWizard getShpImportWizard() {
//    	return (ShpImportWizard) getWizard();
//    }
    
    private boolean isReadable(File file) {
		return file != null && file.exists() && file.canRead();
    }

	public void setDatabase(Neo4jSpatialDataStore dataStore) {
		setNeo4jDir(((EmbeddedGraphDatabase)dataStore.getSpatialDatabaseService().getDatabase()).getStoreDir());
	}
}
