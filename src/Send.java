import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class Send {

	// OpenVC variable
	static CameraManager cm;
	static VideoCapture camera;
	static Mat frame;
	static PerformanceAnalysis pa;

	// Networking variable
	static final byte[] byteAddr = new byte[] { (byte) 137, (byte) 94, (byte) 178, (byte) 183 };
	// static final byte[] byteAddr = new byte[] { (byte) 127, (byte) 0, (byte) 0,
	// (byte) 1 };
	static final int remotePort = 6050;
	static final int localPort = 6051;
	private final static int bufferSize = 45000;
	static final int heightWidth = 600;
	static final int pixelThreshold = 3000;
	static int frameHeight;
	static int frameWidth;
	
	//Number of minutes for each delay
	static final int numberOfMinute = 3;

	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		UDPNetworkInterface udp;

		// Initiate openVC
		cm = CameraManager.getCM();
		System.out.println("Camera Manager has been created");
		camera = new VideoCapture(0);
		System.out.println("Video Capture device as been acquiered");
		frame = new Mat();
		if (!camera.isOpened()) {
			System.out.println("Error");
			System.exit(1);
		} else {
			camera.read(frame);
			frameHeight = frame.height();
			frameWidth = frame.width();
		}

		pa = new PerformanceAnalysis();

		udp = new UDPNetworkInterface(byteAddr, remotePort, localPort, bufferSize);

		while (udp.receiveMessage() != udp.MESSAGE_START) {
			System.out.println("Waiting to start");
		}

		Integer delay = -200;

		while (delay <= 1200) {

			long stopTime = System.currentTimeMillis() + 60000 * numberOfMinute;

			while (stopTime >= System.currentTimeMillis()) {

				udp.SendPacket(getFrame());

			}

			delay += 200;
			if (delay <= 1200) {
				pa.changeDelay(delay);
			}
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(ImageIO.read(new File("EndOfTest.png")), "jpg", baos);
			udp.SendPacket(baos.toByteArray());
		} catch (IOException e) {
			System.out.println("Could not get byte[] from BufferedImage");
		}

		pa.result();

		udp.close();
		cm.close();
		System.exit(0);

	}

	/**
	 * Read a frame from the camera previously initialized and return a byte[] ready
	 * to send using any socket.
	 * 
	 * @return A byte[] of the captured frame.
	 */
	public static byte[] getFrame() {
		Mat frame = new Mat();
		BufferedImage image = null;
		byte[] bytes = null;
		ByteArrayOutputStream baos;

		if (camera.read(frame)) {

			frame = frame.submat(new Rect(frameWidth / 3, frameHeight / 3, frameWidth / 3, frameHeight / 3));
			Imgproc.resize(frame, frame, new Size(frameWidth, frameHeight));

			ImageAnalyze(frame);

			try {
				image = MatToBufferedImage(frame);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			try {
				baos = new ByteArrayOutputStream();
				ImageIO.write(image, "jpg", baos);
				bytes = baos.toByteArray();
			} catch (IOException e) {
				System.out.println("Could not get byte[] from BufferedImage");
			}

			return bytes;

		} else {
			return null;
		}
	}

	/**
	 * Transform a Mat into a BufferedImage.
	 * 
	 * @param frame
	 *            The Mat to transform
	 * @return A BufferedImage corresponding to the input Mat.
	 */
	public static BufferedImage MatToBufferedImage(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}

	/**
	 * Take a BufferedImage, transform it into a Mat end returning it.
	 * 
	 * @param bi
	 *            BufferedImage to transform in Mat
	 * @return Transformed BufferedImage
	 */
	public static BufferedImage Mat2BufferedImage(Mat matrix) throws IOException {
		MatOfByte mob = new MatOfByte();
		Imgcodecs.imencode(".jpg", matrix, mob);
		return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}

	/**
	 * Analyze a frame to see if the green plane is in it.
	 * 
	 * @param frame
	 *            The frame to analyze.
	 */
	public static void ImageAnalyze(Mat frame) {

		// frame = frame.submat(new Rect(frameWidth / 3, frameHeight / 3, frameWidth /
		// 3, frameHeight / 3));
		// Imgproc.resize(frame, frame, new Size(frameWidth, frameHeight));

		Mat outputImg = new Mat();
		Core.inRange(frame, new Scalar(10, 115, 10), new Scalar(180, 255, 115), outputImg);

		outputImg = pa.FrameAnalyze(outputImg);

		try {
			cm.showFrame(Mat2BufferedImage(outputImg), "Green Detection");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
