import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

public class Receive {

	// OpenVC variable

	static CameraManager cm;

	static // Networking variable
	UDPNetworkInterface udp;
	static final byte[] byteAddr = new byte[] { (byte) 127, (byte) 0, (byte) 0, (byte) 1 };
	private final static int bufferSize = 45000;
	static final int localPort = 6050;
	static final int remotePort = 6051;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		// Initiate OpenVC
		cm = CameraManager.getCM();

		// Initiate Networking

		udp = new UDPNetworkInterface(byteAddr, remotePort, localPort, bufferSize);

		warmup();
		
		udp.sendMessage(udp.MESSAGE_START);
		while (true) {
			receiveFrame();
		}

	}

	/**
	 * Receive a frame using the udp socket and show it on screen.
	 */
	public static void receiveFrame() {

		// Receiving packet containing the image.
		InputStream IS = new ByteArrayInputStream(udp.ReceivePacket());

		// Using the InputStream we show the frame received.
		try {
			cm.showFrame(ImageIO.read(IS), "Remote Capture");
		} catch (IOException e) {
			System.out.println("Coud not rebuild BufferedImage");
		}

		// Closing InputStream.
		try {
			IS.close();
		} catch (IOException e) {
			System.out.println("Coud not close InputStream");
		}
	}

	/**
	 * Take a BufferedImage, transform it into a Mat end returning it.
	 * 
	 * @param bi
	 *            BufferedImage to transform in Mat
	 * @return Transformed BufferedImage
	 */
	public static Mat bufferedImageToMat(BufferedImage bi) {
		Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		mat.put(0, 0, data);
		return mat;
	}

	/**
	 * Take a Mat, transform it into a BufferedImage end returning it.
	 * 
	 * @param matrix
	 *            Mat to transform in BufferedImage
	 * @return Transformed Mat
	 * @throws IOException
	 *             If ImageIO is not able to read the MatOfByte
	 */
	public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}
	
	private static void warmup() {
		long startTime = System.currentTimeMillis();
		boolean show1 = false;
		boolean show2 = false;
		boolean show3 = false;

		while (System.currentTimeMillis() <= startTime + 3000) {

			if (System.currentTimeMillis() <= startTime + 999 && !show3) {
				try {
					cm.showFrame(ImageIO.read(new File("3.png")), "Remote Capture");
					show3 = true;
				} catch (IOException e) {
					System.out.println("Coud not show image file");
				}
			} else if (System.currentTimeMillis() >= startTime + 1000 && System.currentTimeMillis() <= startTime + 1999 && !show2) {
				try {
					cm.showFrame(ImageIO.read(new File("2.png")), "Remote Capture");
					show2 = true;
				} catch (IOException e) {
					System.out.println("Coud not show image file");
				}
			} else if (System.currentTimeMillis() >= startTime + 2000 && !show1) {
				try {
					cm.showFrame(ImageIO.read(new File("1.png")), "Remote Capture");
					show1 = true;
				} catch (IOException e) {
					System.out.println("Coud not show image file");
				}
			}

		}
	}
}
