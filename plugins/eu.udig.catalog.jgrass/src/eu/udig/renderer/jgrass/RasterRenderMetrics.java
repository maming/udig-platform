/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 * (C) C.U.D.A.M. Universita' di Trento
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.udig.renderer.jgrass;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.refractions.udig.project.ILayer;
import net.refractions.udig.project.internal.render.Renderer;
import net.refractions.udig.project.render.AbstractRenderMetrics;
import net.refractions.udig.project.render.IRenderContext;

import org.geotools.util.Range;

/**
 * @author Andrea Antonello - www.hydrologis.com
 */
public class RasterRenderMetrics extends AbstractRenderMetrics {

    public RasterRenderMetrics( IRenderContext context, RasterRenderMetricsFactory factory,
            List<String> styleIds ) {
        super(context, factory, styleIds);
    }

    public Renderer createRenderer() {
        RasterRenderer rasterRenderer = new RasterRenderer();
        rasterRenderer.setContext(context);
        return rasterRenderer;
    }

    public boolean canAddLayer( ILayer layer ) {
        return false;
    }

    public boolean canStyle( String styleID, Object value ) {
        return false;
    }

    public Set<Range<Double>> getValidScaleRanges() {
        return new HashSet<Range<Double>>();
    }

}
