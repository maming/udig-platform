/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2011, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.ui.aoi;

import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.aoi.AOIListener;
import net.refractions.udig.aoi.AOIProxy;
import net.refractions.udig.aoi.IAOIService;
import net.refractions.udig.aoi.IAOIStrategy;
import net.refractions.udig.internal.ui.UiPlugin;
import net.refractions.udig.ui.PlatformGIS;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * Allows a user to select the AOIStrategy to define the AOI (Area of Interest)
 * <p>
 * This view processes the "AOI" extension point in order to obtain the list of options to
 * display to the user. Each AOIStrategy may optionally provide a "page" used to further refine the
 * limit used for the AOI.
 * 
 * @author pfeiffp
 * @sinve 1.3.0
 */
public class AOIView extends ViewPart {

    /**
     * Listens to the global IAOIService and updates our view if anything changes!
     */
    private AOIListener serviceWatcher = new AOIListener(){
        // private IAOIStrategy selectedStrategy = null;
        public void handleEvent( AOIListener.Event event ) {
            final AOIListener.Event aoiEvent = event;
            // must be run in the UI thread to be able to call setSelected
            PlatformGIS.asyncInDisplayThread(new Runnable(){
                
                @Override
                public void run() {
                    AOIProxy currentStrategy;
                    if (aoiEvent.source instanceof AOIProxy) {
                        currentStrategy = (AOIProxy) aoiEvent.source;
                    } else {
                        currentStrategy = PlatformGIS.getAOIService().getProxy();
                    }
                    setSelected(currentStrategy);            
                    
                }
            }, true);
        }
    };

    // private Combo combo;
    private ComboViewer comboViewer;

    /**
     * Listens to the user and changes the global IAOIService to the indicated strategy.
     */
    private ISelectionChangedListener comboListener = new ISelectionChangedListener(){
        @Override
        public void selectionChanged( SelectionChangedEvent event ) {
            IStructuredSelection selectedStrategy = (IStructuredSelection) event.getSelection();
            AOIProxy selected = (AOIProxy) selectedStrategy.getFirstElement();
            
            publishAOIStrategy(selected);
        }
    };

    private PageBook pagebook;
    private Map<AOIProxy,PageRecord> pages = new HashMap<AOIProxy, PageRecord>();

    private Composite placeholder;
    
    //private List<IPageBookViewPage> pages = new ArrayList<IPageBookViewPage>();
    //private Map<AOIProxy,Control> controls = new HashMap<AOIProxy, Control>();

    /**
     * AOI View constructor adds the known strategies
     */
    public AOIView() {
    }
    

    private void publishAOIStrategy( AOIProxy selected ) {
        IAOIService aOIService = PlatformGIS.getAOIService();
        aOIService.setProxy(selected);
    }
    
    /**
     * This will update the combo viewer and pagebook (carefully unhooking events while the viewer is updated).
     * 
     * @param selected
     */
    public void setSelected( AOIProxy selected ) {
        if (selected == null) {
            selected = PlatformGIS.getAOIService().getDefault();
        }
        
        boolean disposed = comboViewer.getControl().isDisposed();
        if (comboViewer == null || disposed) {
            listenService(false);
            return; // the view has shutdown!
        }
        
        AOIProxy current = getSelected();
        // check combo
        if (current != selected) {
            try {
                listenCombo(false);
                comboViewer.setSelection(new StructuredSelection(selected), true);
            } finally {
                listenCombo(true);
            }
        }
        
        // this is the control displayed right now
        Control currentControl = null;
        for( Control page : pagebook.getChildren() ){
            if( page.isVisible() ){
                currentControl = page;
                break;
            }
        }
        
        // Check if we already created the control for selected
        PageRecord record = pages.get(selected);
        if( record == null ){
            // record has not been created yet
            IPageBookViewPage page = selected.createPage();
            if( page == null ){
                MessagePage messagePage = new MessagePage();
                
                record = new PageRecord( this, messagePage);
                
                messagePage.init( record.getSite() );
                
                messagePage.createControl( pagebook );
                messagePage.setMessage( selected.getName() );
            }
            else {
                record = new PageRecord( this, page );    
                try {
                    page.init( record.getSite() );
                } catch (PartInitException e) {
                    UiPlugin.log(getClass(), "initPage", e); //$NON-NLS-1$
                }
                page.createControl( pagebook );
            }
            pages.put(selected, record );
        }
        Control selectedControl = record.getControl();
        
        if( selectedControl == null ){
            // this is not expected to be null!
            if( placeholder == null ){
                // placeholder just so we see something!
                Composite content  = new Composite(pagebook, SWT.NULL);
                content.setLayout(new FillLayout());
    
                Label label = new Label( content, SWT.LEFT | SWT.TOP | SWT.WRAP );
                label.setText("Current Area of Interest used for filtering content.");
                
                placeholder = content;
            }
            selectedControl = placeholder;
        }
        
        if( currentControl != selectedControl ){
            if( selectedControl != null ){
                pagebook.showPage(selectedControl); // done!
            }
        }
    }
    /**
     * Access the IAOIStrategy selected by the user
     * 
     * @return IAOIStrategy selected by the user
     */
    public AOIProxy getSelected() {
        if (comboViewer.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) comboViewer.getSelection();
            return (AOIProxy) selection.getFirstElement();
        }
        return null;
    }

    protected void listenCombo( boolean listen ) {
        if (comboViewer == null || comboViewer.getControl().isDisposed()) {
            return; // run away!
        }
        if (listen) {
            comboViewer.addSelectionChangedListener(comboListener);
        } else {
            comboViewer.removeSelectionChangedListener(comboListener);
        }
    }

    protected void listenService( boolean listen ) {
        IAOIService aOIService = PlatformGIS.getAOIService();
        if (listen) {
            aOIService.addListener(serviceWatcher);
        } else {
            aOIService.removeListener(serviceWatcher);
        }
    }

    @Override
    public void init( IViewSite site, IMemento memento ) throws PartInitException {
        super.init(site, memento);
        
        // this is where you read your memento to remember
        // anything the user told you from last time
        // this.addAOIStrategy();
        if( memento != null ){
//            String id = memento.getString("AOI");
//            IAOIService service = PlatformGIS.getAOIService();
//            this.initialStrategy = service.findProxy(id);
        }
    }
    
    @Override
    public void saveState( IMemento memento ) {
        super.saveState(memento);
        
        IAOIService service = PlatformGIS.getAOIService();
        String id = service.getProxy().getId();
        
        memento.putString("AOI", id );
    }

    @Override
    public void createPartControl( Composite parent ) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        parent.setLayout(layout);
        Label label = new Label(parent, SWT.LEFT);
        label.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
        label.setText("Area of Interest: ");

        // get the current strategy
        IAOIService aOIService = PlatformGIS.getAOIService();
        listenService(true);

        // eclipse combo viewer
        comboViewer = new ComboViewer(parent, SWT.READ_ONLY);
        comboViewer.setContentProvider(new ArrayContentProvider());
        comboViewer.setLabelProvider(new LabelProvider(){
            @Override
            public String getText( Object element ) {
                if (element instanceof IAOIStrategy) {
                    IAOIStrategy comboStrategy = (IAOIStrategy) element;
                    return comboStrategy.getName();
                }
                return super.getText(element);
            }
        });
        comboViewer.setInput(aOIService.getProxyList());
        // set the current strategy
        AOIProxy proxy = aOIService.getProxy();
        if (proxy == null) {
            proxy = aOIService.getDefault();
        }
        comboViewer.setSelection(new StructuredSelection(proxy));

        // now that we are configured we can start to listen!
        listenCombo(true);
        
        pagebook = new PageBook(parent, SWT.NONE );
        GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, true, true);
        layoutData.widthHint=400;
        layoutData.horizontalSpan=2;
        layoutData.heightHint=400;
        pagebook.setLayoutData(layoutData);        
    }

    @Override
    public void setFocus() {
        comboViewer.getControl().setFocus();
    }

    @Override
    public void dispose() {
        super.dispose();
        // clean up any page stuffs
        if( pages != null && !pages.isEmpty() ){
            for( PageRecord record : pages.values() ){
                record.dispose();
            }
            pages.clear();
            pages = null;
        }
        if (comboViewer != null) {
            comboViewer.removeSelectionChangedListener(comboListener);
        }
        if (serviceWatcher != null) {
            IAOIService aOIService = PlatformGIS.getAOIService();
            if (aOIService != null) {
                aOIService.removeListener(serviceWatcher);
            }
            serviceWatcher = null;
        }
    }
}
