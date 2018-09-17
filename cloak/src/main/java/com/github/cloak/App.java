package com.github.cloak;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.image.UtilImageIO;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import com.github.sarxos.webcam.Webcam;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class App {

	public static volatile boolean saveImage = false;

	public static void main(String[] args) {

		// Open a webcam at a resolution close to 640x480
		Webcam webcam = Webcam.getWebcams().get(0);
		UtilWebcamCapture.adjustResolution(webcam,640,480);
		webcam.open();

		// Create the panel used to display the image
		ImagePanel gui = new ImagePanel();
		gui.setPreferredSize(webcam.getViewSize());
		gui.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				saveImage = true;
			}
		});

		ShowImages.showWindow(gui,"Webcam",true);

		int total = 0;
		while( true ) {
			BufferedImage image = webcam.getImage();

			if( saveImage ) {
				System.out.println("Saving image "+total);
				saveImage = false;
				UtilImageIO.saveImage(image,String.format("image%04d.png",(total++)));
			}

			gui.setImageUI(image);
		}
	}
}
