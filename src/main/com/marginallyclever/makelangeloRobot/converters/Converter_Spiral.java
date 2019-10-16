package com.marginallyclever.makelangeloRobot.converters;


import java.io.IOException;
import java.io.Writer;

import com.marginallyclever.makelangelo.Log;
import com.marginallyclever.makelangeloRobot.TransformedImage;
import com.marginallyclever.makelangelo.Translator;
import com.marginallyclever.makelangeloRobot.imageFilters.Filter_BlackAndWhite;

/**
 * Generate a Gcode file from the BufferedImage supplied.<br>
 * Use the filename given in the constructor as a basis for the gcode filename, but change the extension to .ngc
 *
 * @author Dan
 */
public class Converter_Spiral extends ImageConverter {
	private static boolean convertToCorners = false;  // draw the spiral right out to the edges of the square bounds.

	@Override
	public String getName() {
		return Translator.get("SpiralName");
	}

	@Override
	public ImageConverterPanel getPanel() {
		return new Converter_Spiral_Panel(this);
	}

	public boolean getToCorners() {
		return convertToCorners;
	}
	
	public void setToCorners(boolean arg0) {
		convertToCorners=arg0;
	}
	
	/**
	 * create a spiral across the image.  raise and lower the pen to darken the appropriate areas
	 *
	 * @param img the image to convert.
	 */
	@Override
	public void finish(Writer out) throws IOException {
		// black and white
		Filter_BlackAndWhite bw = new Filter_BlackAndWhite(255);
		TransformedImage img = bw.filter(sourceImage);

		imageStart(out);

		double toolDiameter = machine.getPenDiameter();

		int i, j;
		final int steps = 4;
		double leveladd = 255.0 / (double)(steps+1);
		double level;
		int z = 0;

		float maxr;
		if (convertToCorners) {
			// go right to the corners
			float h2 = (float)machine.getPaperHeight();
			float w2 = (float)machine.getPaperWidth();
			maxr = (float) (Math.sqrt(h2 * h2 + w2 * w2) + 1.0f);
		} else {
			// do the largest circle that still fits in the image.
			float w = (float)machine.getPaperWidth()/2.0f;
			float h = (float)machine.getPaperHeight()/2.0f;
			maxr = (float)( h < w ? h : w );
			maxr *= machine.getPaperMargin() ;
		}
		
		float r = maxr, f;
		float fx, fy;
		int numRings = 0;
		j = 0;
		while (r > toolDiameter) {
			++j;
			level = leveladd * (1+(j%steps));
			// find circumference of current circle
			float circumference = (float) Math.floor((2.0f * r - toolDiameter) * Math.PI);
			if (circumference > 360.0f) circumference = 360.0f;

			for (i = 0; i <= circumference; ++i) {
				f = (float) Math.PI * 2.0f * (float)i / (float)circumference;
				fx = (float) (Math.cos(f) * r);
				fy = (float) (Math.sin(f) * r);
				
				boolean isInside = isInsidePaperMargins(fx, fy);
				if(isInside) {
					try {
						z = img.sample3x3(fx, fy);
					} catch(Exception e) {
						e.printStackTrace();
					}
	
	
					if(z<level) {
						lowerPen(out);
					} else liftPen(out);
				} else liftPen(out);
				machine.writeMoveTo(out, fx, fy, isPenUp());
			}
			r -= toolDiameter;
			++numRings;
		}

		Log.info("yellow", numRings + " rings.");

		imageEnd(out);
	}
}


/**
 * This file is part of Makelangelo.
 * <p>
 * Makelangelo is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Makelangelo is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Makelangelo.  If not, see <http://www.gnu.org/licenses/>.
 */