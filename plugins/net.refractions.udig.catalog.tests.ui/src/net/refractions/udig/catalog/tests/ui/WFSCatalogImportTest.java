/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package net.refractions.udig.catalog.tests.ui;

import static org.junit.Assert.assertTrue;

import java.net.URL;

import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.internal.wfs.WFSServiceImpl;

public class WFSCatalogImportTest extends CatalogImportTest {

	@Override
	Object getContext() throws Exception {
		return new URL("http://www2.dmsolutions.ca/cgi-bin/mswfs_gmap?version=1.0.0&request=getcapabilities&service=wfs"); //$NON-NLS-1$
	}
	
	@Override
	void assertServiceType(IService service) {
		assertTrue(service instanceof WFSServiceImpl);
	}
	
}
