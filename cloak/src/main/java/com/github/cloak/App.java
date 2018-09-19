package com.github.cloak;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.webcamcapture.UtilWebcamCapture;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.point.Point2D_I32;
import georegression.struct.shapes.Quadrilateral_F64;
import georegression.struct.shapes.Rectangle2D_F64;
import com.github.sarxos.webcam.Webcam;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

public class App<T extends ImageBase<T>> extends JPanel
		implements MouseListener, MouseMotionListener {

	TrackerObjectQuad<T> tracker;

	Quadrilateral_F64 target = new Quadrilateral_F64();

	Point2D_I32 start_point = new Point2D_I32();
	Point2D_I32 end_point = new Point2D_I32();

	int panel_width, panel_height;
	volatile int mode = 0; // Used to change states when drawing the selected/target rectangle.
	volatile int cloak = 0; // Used to enable/disable cloaking on mouse-click.

	BufferedImage workImage;

	JFrame window;

	public App( TrackerObjectQuad<T> tracker,
										int panel_width , int panel_height)
	{
		/**
		* Adds tracker, mouse event listeners and panel dimensions to the 'App' class.
		* @param tracker Tracker used to track the selected object.
		* @param panel_width Width of the panel.
		* @param panel_height Height of the panel.
		* @return void
		*/
		this.tracker = tracker;
		this.panel_width = panel_width;
		this.panel_height = panel_height;

		addMouseListener(this);
		addMouseMotionListener(this);

		window = new JFrame("Object cloaking");
		window.setContentPane(this);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public void process() {
		/**
		* Conducts the complete process of accessing webcam, tracking and cloaking the selected objects(rectangle).
		* @return void
		*/
		Webcam webcam = UtilWebcamCapture.openDefault(panel_width, panel_height);

		Dimension window_size = webcam.getViewSize();
		setPreferredSize(window_size);
		setMinimumSize(window_size);
		window.setMinimumSize(window_size);
		window.setPreferredSize(window_size);
		window.setVisible(true);

		T input = tracker.getImageType().createImage(window_size.width, window_size.height);

		workImage = new BufferedImage(input.getWidth(),input.getHeight(), BufferedImage.TYPE_INT_RGB);

		while( true ) {
			BufferedImage buffered = webcam.getImage();
			if( buffered == null ) break;
			ConvertBufferedImage.convertFrom(webcam.getImage(), input, true);

			int mode = this.mode;

			boolean success = false;
			if( mode == 2 ) {
				Rectangle2D_F64 rect = new Rectangle2D_F64();
				rect.set(start_point.x, start_point.y, end_point.x, end_point.y);
				UtilPolygons2D_F64.convert(rect, target);
				success = tracker.initialize(input, target);
				this.mode = success ? 3 : 0;
			} else if( mode == 3 ) {
				success = tracker.process(input, target);
			}

			// Enables cloaking on double mouse-click
			if(cloak%2 == 1) {
				pixelblur(buffered, (int)target.a.getX(), (int)target.a.getY(), (int)target.c.getX(), (int)target.c.getY());
			}

			synchronized( workImage ) {
				Graphics2D g2 = workImage.createGraphics();
				g2.drawImage(buffered, 0, 0, null);

				if (mode == 1) {
					drawSelected(g2);
				} else if (mode == 3) {
					if( success ) {
						drawTrack(g2);
					}
				}
			}

			repaint();
		}
	}

	@Override
	public void paint (Graphics g) {
		// Draws the graphic image
		if( workImage != null ) {
			synchronized (workImage) {
				((Graphics2D) g).drawImage(workImage, 0, 0, null);
			}
		}
	}

	private void drawSelected( Graphics2D g2 ) {
		/**
		*	Draws the selected/target rectangle when initialized.
		* @param g2 Graphics tool for 2D drawing.
		* @return void
		*/
		g2.setColor(Color.RED);
		g2.setStroke( new BasicStroke(2));
		g2.drawLine(start_point.getX(),start_point.getY(),end_point.getX(),start_point.getY());
		g2.drawLine(end_point.getX(),start_point.getY(),end_point.getX(),end_point.getY());
		g2.drawLine(end_point.getX(),end_point.getY(),start_point.getX(),end_point.getY());
		g2.drawLine(start_point.getX(),end_point.getY(),start_point.getX(),start_point.getY());
	}

	private void drawTrack( Graphics2D g2 ) {
		/**
		*	Draws the final selected/target rectangle.
		* @param g2 Graphics tool for 2D drawing.
		* @return void
		*/
		g2.setStroke(new BasicStroke(2));
		g2.setColor(Color.WHITE);
		g2.drawLine((int)target.a.getX(),(int)target.a.getY(),(int)target.b.getX(),(int)target.b.getY());
		g2.setColor(Color.WHITE);
		g2.drawLine((int)target.b.getX(),(int)target.b.getY(),(int)target.c.getX(),(int)target.c.getY());
		g2.setColor(Color.WHITE);
		g2.drawLine((int)target.c.getX(),(int)target.c.getY(),(int)target.d.getX(),(int)target.d.getY());
		g2.setColor(Color.WHITE);
		g2.drawLine((int)target.d.getX(),(int)target.d.getY(),(int)target.a.getX(),(int)target.a.getY());
	}

	private void drawTarget( Graphics2D g2 ) {
		/**
		*	Draws the selected/target rectangle during mouse drag.
		* @param g2 Graphics tool for 2D drawing.
		* @return void
		*/
		g2.setColor(Color.RED);
		g2.setStroke( new BasicStroke(2));
		g2.drawLine(start_point.getX(),start_point.getY(),end_point.getX(),start_point.getY());
		g2.drawLine(end_point.getX(),start_point.getY(),end_point.getX(),end_point.getY());
		g2.drawLine(end_point.getX(),end_point.getY(),start_point.getX(),end_point.getY());
		g2.drawLine(start_point.getX(),end_point.getY(),start_point.getX(),start_point.getY());
	}

	@Override
	public void mousePressed(MouseEvent event) {
		// Sets the start point of the selected/target rectangle. Enables changes when mouse is dragged.
		start_point.set(event.getX(),event.getY());
		end_point.set(event.getX(),event.getY());
		mode = 1;
	}

	@Override
	public void mouseReleased(MouseEvent event) {
		// Sets the end point of the selected/target rectangle.
		end_point.set(event.getX(),event.getY());
		mode = 2;
	}

	@Override public void mouseClicked(MouseEvent event) {
		// Increases count to enable or disable cloaking.
		cloak += 1;
	}

	@Override public void mouseEntered(MouseEvent event) {}

	@Override public void mouseExited(MouseEvent event) {}

	@Override public void mouseDragged(MouseEvent event) {
		// Sets the end point of the selected/target rectangle. Changes constantly when mouse is dragged.
		if( mode == 1 ) {
			end_point.set(event.getX(),event.getY());
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {}

	public static void pixelblur(BufferedImage image, int ul_x, int ul_y, int lr_x, int lr_y){
		/**
		* Assignes the average RGB values of the surrounding pixels and does the same ti every pixel in the rectangle.
		* Gives a pixel blur effect.
		* @param image This is a buffered image taken from the camera.
		* @param ul_x Upper-left x-coordinate of the target rectangle.
		* @param ul_y Upper-left y-coordinate of the target rectangle.
		* @param lr_x Lower-right x-coordinate of the target rectangle.
		* @param lr_y Lower-right y-coordinate of the target rectangle.
		* @return void
		*/
		for(int i = ul_x; i<=lr_x;i++){
			for(int j = ul_y; j<=lr_y;j++){
				image.setRGB(i, j, (image.getRGB(i - 1, j - 1) + image.getRGB(i - 1, j) +
				image.getRGB(i - 1, j + 1) + image.getRGB(i, j - 1 ) + image.getRGB(i, j + 1) +
				image.getRGB(i + 1, j - 1) + image.getRGB(i + 1, j) + image.getRGB(i + 1, j - 1)
				)/8);
			}
		}
	}

	public static void vanish(BufferedImage image, int ul_x, int ul_y, int lr_x, int lr_y, int bound){
		/**
		* Takes the RGB value of the bound pixel and adds it to every pixel in the rectangle.
		* @param image This is a buffered image taken from the camera.
		* @param ul_x Upper-left x-coordinate of the target rectangle.
		* @param ul_y Upper-left y-coordinate of the target rectangle.
		* @param lr_x Lower-right x-coordinate of the target rectangle.
		* @param lr_y Lower-right y-coordinate of the target rectangle.
		* @param bound Range of the bound pixel
		* @return void

		* Note: When using this effect, it is assumed that the surface it dominated with a single colorType
		* and object is strictly within the selected box.
		*/
		for(int i = ul_x; i<=lr_x;i++){
			for(int j = ul_y; j<=lr_y;j++){
				image.setRGB(i, j, image.getRGB(ul_x - bound, ul_y - bound));
			}
		}
	}

	public static void main(String[] args) {

		ImageType<Planar<GrayU8>> colorType = ImageType.pl(2, GrayU8.class);

		TrackerObjectQuad tracker = FactoryTrackerObjectQuad.circulant(null, GrayU8.class);

		int window_width = 640;
		int window_height = 480;

		App app = new App(tracker, window_width, window_height);

		app.process();
	}
}
