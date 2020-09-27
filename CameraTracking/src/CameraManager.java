import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class CameraManager extends JPanel {
	
	static JFrame frame0 = null;
	static BufferedImage image;

		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			g.drawImage(image, 0, 0, this);
		}

		public CameraManager() {

		}

		public CameraManager(BufferedImage img) {
			image = img;
		}

		public static CameraManager getCM() {
			return new CameraManager();
		}

		public void createWindow(BufferedImage img, String text, int x, int y) {
			System.out.println("Creating viewing window");
			frame0 = new JFrame();
			frame0.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame0.setTitle(text);
			frame0.setSize(img.getWidth(), img.getHeight() + 30);
			frame0.setLocation(x, y);
			frame0.getContentPane().add(new CameraManager(img));
		}

		// Show image on window
		public void showFrame(BufferedImage img, String windowName) {

			if (frame0 == null) {
				createWindow(img, windowName, 0, 0);
			} else {
				frame0.getContentPane().removeAll();
				frame0.getContentPane().add(new CameraManager(img));
			}

			frame0.setVisible(true);
		}
		
		public void close() {
			frame0.dispose();
		}
	}
