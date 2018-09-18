package com.github.cloak;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.gui.tracker.TrackerObjectQuadPanel;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import com.github.sarxos.webcam.Webcam;
import georegression.struct.shapes.Quadrilateral_F64;

import java.awt.*;
import java.awt.image.BufferedImage;

public class App {

	public static volatile boolean saveImage = false;

	public static void main(String[] args) {

		// Open a webcam at a resolution close to 640x480
		Webcam webcam = Webcam.getWebcams().get(0);
		UtilWebcamCapture.adjustResolution(webcam,640,480);
		webcam.open();

		TrackerObjectQuad tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);

		// Create the panel used to display the image
		Quadrilateral_F64 location = new Quadrilateral_F64(211.0,162.0,326.0,153.0,335.0,258.0,215.0,249.0);
		GrayU8 frame = ConvertBufferedImage.convertFrom(webcam.getImage(), (GrayU8)null);
		tracker.initialize(frame,location);

		// For displaying the results
		TrackerObjectQuadPanel gui = new TrackerObjectQuadPanel(null);
		gui.setPreferredSize(webcam.getViewSize());
		gui.setTarget(location, true);
		ShowImages.showWindow(gui,"Tracking Results", true);

		int total = 0;
		while( true ) {
			BufferedImage image = webcam.getImage();
			GrayU8 gray = ConvertBufferedImage.convertFrom(image, (GrayU8)null);

			boolean visible = tracker.process(gray,location);

			gui.setImageUI(image);
			gui.setTarget(location, visible);
			gui.repaint();
			//System.out.println(image);
		}
	}
}
