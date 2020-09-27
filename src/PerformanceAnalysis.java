import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.time.StopWatch;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;

public class PerformanceAnalysis {

	//Name of the file
	final static String who = "subject9";
	
	final DecimalFormat df = new DecimalFormat("#0.00");
	final static int PIXEL_THRESHOLD = 500;

	final static Point XTextPosition = new Point(0, 10);
	final static Point YTextPosition = new Point(0, 22);

	final static Scalar Green = new Scalar(0, 255, 0);
	final static Scalar Yellow = new Scalar(0, 255, 255);
	final static Scalar Red = new Scalar(0, 0, 255);

	private int numberOfFrame;
	private double totalX;
	private double totalY;

	Integer delay;
	Integer iteration;
	List<String> dataToWrite;

	public PerformanceAnalysis() {

		dataToWrite = new ArrayList();

		numberOfFrame = 0;
		totalX = 0;
		totalY = 0;
		delay = 0;
		iteration = 0;
	}

	public Mat FrameAnalyze(Mat outputImg) {

		Moments moments;
		Scalar color;

		Mat img = new Mat();

		Imgproc.cvtColor(outputImg, img, Imgproc.COLOR_GRAY2BGR);

		numberOfFrame++;

		double diffX = 0;
		double diffY = 0;
		String oob;

		if (Core.countNonZero(outputImg) >= PIXEL_THRESHOLD) {

			oob = "1";

			moments = Imgproc.moments(outputImg);

			Point centroid = new Point();
			Point centerImg = new Point();

			centroid.x = moments.get_m10() / moments.get_m00();
			centroid.y = moments.get_m01() / moments.get_m00();

			centerImg.x = img.width() / 2;
			centerImg.y = img.height() / 2;

			diffX = centroid.x - centerImg.x;
			diffY = centroid.y - centerImg.y;

			if (Math.abs(diffX) < 100 && Math.abs(diffY) < 80) {
				color = Green;
			} else if (Math.abs(diffX) < 220 && Math.abs(diffY) < 160) {
				color = Yellow;
			} else {
				color = Red;
			}

			Imgproc.line(img, new Point(centroid.x, centerImg.y), centerImg, color, 2);
			Imgproc.line(img, centroid, new Point(centroid.x, centerImg.y), color, 2);

			String toWrite = "x = " + df.format(Math.abs(diffX));
			Imgproc.putText(img, toWrite, XTextPosition, Core.FONT_HERSHEY_SIMPLEX, 0.4, color);
			totalX += Math.abs(diffX);

			toWrite = "y = " + df.format(Math.abs(diffY));
			Imgproc.putText(img, toWrite, YTextPosition, Core.FONT_HERSHEY_SIMPLEX, 0.4, color);
			totalY += Math.abs(diffY);

			dataToWrite.add(
					((Integer) numberOfFrame).toString() + "," + Math.abs(diffX) + "," + Math.abs(diffY) + "," + oob);
			return img;
		} else {

			oob = "0";
			totalX += 999;
			totalY += 999;

			dataToWrite.add(
					((Integer) numberOfFrame).toString() + "," + Math.abs(diffX) + "," + Math.abs(diffY) + "," + oob);
			return outputImg;
		}

	}

	public void result() {

		try {
			Files.write(Paths.get("data/" + iteration.toString() + "_" + who + "_5min_" + delay.toString() + "ms" + ".csv"), dataToWrite,
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.out.println("Could not write File");
		}

		double avgX = totalX / numberOfFrame;
		double avgY = totalY / numberOfFrame;

		System.out.println("-----------------------------------------------");
		System.out.println("Average X : " + avgX);
		System.out.println("Average Y : " + avgY);
		System.out.println("Your score : " + Math.sqrt(avgX * avgX + avgY * avgY));
		System.out.println("-----------------------------------------------");
	}

	public void changeDelay(Integer newDelay) {
		
		try {
			Files.write(
					Paths.get("data/" + iteration.toString() + "_" + who + "_5min_" + delay.toString() + "ms" + ".csv"),
					dataToWrite, Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.out.println("Could not write File");
		}
		
		numberOfFrame = 0;
		iteration++;
		delay = newDelay;
		dataToWrite.clear();
	}

}
