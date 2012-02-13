/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004-2012, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.project.ui.internal;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.ID;
import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IRepository;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.IMap;
import net.refractions.udig.project.Interaction;
import net.refractions.udig.project.internal.Layer;

import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * The utility class of the Legend View. This contains static helper methods for Legend View
 * functions.
 * 
 * @author nchan
 * @since 1.3.1
 */
public final class LegendViewUtils {

    private static final String MAP_GRAPHIC_PROTOCOL = "mapgraphic"; //$NON-NLS-1$
    private static final String GRID_ID_STR = "grid"; //$NON-NLS-1$
    private static final String GRID_URL = "mapgraphic:/localhost/mapgraphic#grid"; //$NON-NLS-1$
    private static final ID GRID_ID = new ID(GRID_URL, null);

    /**
     * Checks if layer is a background layer
     * 
     * @param layer
     * @return true - if layer is a background layer, otherwise false
     */
    public static boolean isBackgroundLayer( ILayer layer ) {
        if (layer.getInteraction(Interaction.BACKGROUND)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if layer is a map graphics layer
     * 
     * @param layer
     * @return true - if layer is a map graphics layer, otherwise false
     */
    public static boolean isMapGraphicLayer( ILayer layer ) {
        if (MAP_GRAPHIC_PROTOCOL.equals(layer.getID().getProtocol())) {
            return true;
        }
        return false;
    }

    /**
     * Checks if layer is a grid layer
     * 
     * @param layer
     * @return true - if layer is a grid layer, otherwise false
     */
    public static boolean isGridLayer( ILayer layer ) {
        if (isMapGraphicLayer(layer) && GRID_ID_STR.equals(layer.getID().getRef())) {
            return true;
        }
        return false;
    }

    public static Layer findGridLayer( IMap map ) {

        if (map != null) {
            for( ILayer layer : map.getMapLayers() ) {
                if (isGridLayer(layer)) {
                    return (Layer) layer;
                }
            }
        }

        return null;

    }

    /**
     * Retrieves the an IGeoResource reference of the grid map graphic from the catalog.
     * 
     * @return grid map graphic
     */
    public static IGeoResource getGridMapGraphic() {
        final IRepository local = CatalogPlugin.getDefault().getLocal();
        return local.getById(IGeoResource.class, GRID_ID, new NullProgressMonitor());
    }

}
