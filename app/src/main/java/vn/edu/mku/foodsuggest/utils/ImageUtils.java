package vn.edu.mku.foodsuggest.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp tiện ích xử lý ảnh với OpenCV for Android (Chương 2)
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static boolean isOpenCvLoaded = false;

    static {
        try {
            isOpenCvLoaded = OpenCVLoader.initLocal();
            Log.d(TAG, "OpenCVLoader initLocal result: " + isOpenCvLoaded);
        } catch (Throwable t) {
            Log.w(TAG, "OpenCV native library not available, fallback to Java Bitmap processing.", t);
            isOpenCvLoaded = false;
        }
    }

    /**
     * Tăng nhẹ tương phản (alpha = 1.15) và độ sáng (beta = 10) để làm nổi bật nguyên liệu
     */
    public static Bitmap enhance(Bitmap input) {
        if (input == null) return null;
        if (!isOpenCvLoaded) {
            return input; // Fallback an toàn
        }
        try {
            Mat src = new Mat();
            Utils.bitmapToMat(input, src);
            Mat enhanced = new Mat();
            // alpha = 1.15 (tương phản), beta = 10 (độ sáng)
            src.convertTo(enhanced, -1, 1.15, 10);
            Bitmap output = Bitmap.createBitmap(enhanced.cols(), enhanced.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(enhanced, output);
            src.release();
            enhanced.release();
            return output;
        } catch (Throwable t) {
            Log.e(TAG, "Error in enhance: ", t);
            return input;
        }
    }

    /**
     * Tìm vùng chữ nhật chứa nguyên liệu chính (Chương 2.2.3)
     */
    private static Rect findMainObjectRect(Mat src) {
        try {
            Mat gray = new Mat();
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_RGBA2GRAY);
            Imgproc.GaussianBlur(gray, gray, new Size(5, 5), 0);

            Mat edges = new Mat();
            Imgproc.Canny(gray, edges, 50, 150);
            Imgproc.dilate(edges, edges, Mat.ones(5, 5, CvType.CV_8U));

            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(edges, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            double minArea = src.rows() * src.cols() * 0.05;
            double centerX = src.cols() / 2.0;
            double centerY = src.rows() / 2.0;

            Rect best = null;
            double bestScore = 0;

            for (MatOfPoint c : contours) {
                Rect r = Imgproc.boundingRect(c);
                double area = r.area();
                if (area < minArea) continue;

                double rectCenterX = r.x + r.width / 2.0;
                double rectCenterY = r.y + r.height / 2.0;
                double distanceToCenter = Math.hypot(rectCenterX - centerX, rectCenterY - centerY);

                double score = area / (1.0 + distanceToCenter);
                if (score > bestScore) {
                    bestScore = score;
                    best = r;
                }
            }

            gray.release();
            edges.release();
            return best;
        } catch (Throwable t) {
            Log.e(TAG, "Error in findMainObjectRect: ", t);
            return null;
        }
    }

    /**
     * Cắt nguyên liệu chính với lề 10% (Chương 2.2.3)
     */
    public static Bitmap cropMainObject(Bitmap input) {
        if (input == null) return null;
        if (!isOpenCvLoaded) {
            return input;
        }
        try {
            Mat src = new Mat();
            Utils.bitmapToMat(input, src);
            Rect biggest = findMainObjectRect(src);
            Bitmap result = input;

            if (biggest != null) {
                int padX = (int) (biggest.width * 0.1);
                int padY = (int) (biggest.height * 0.1);
                int left = Math.max(0, biggest.x - padX);
                int top = Math.max(0, biggest.y - padY);
                int width = Math.min(src.cols() - left, biggest.width + 2 * padX);
                int height = Math.min(src.rows() - top, biggest.height + 2 * padY);

                Rect padded = new Rect(left, top, width, height);
                Mat cropped = new Mat(src, padded);
                result = Bitmap.createBitmap(cropped.cols(), cropped.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(cropped, result);
                cropped.release();
            }
            src.release();
            return result;
        } catch (Throwable t) {
            Log.e(TAG, "Error in cropMainObject: ", t);
            return input;
        }
    }

    /**
     * Vẽ khung chữ nhật nổi bật bao quanh nguyên liệu
     */
    public static Bitmap drawMainObjectBox(Bitmap input) {
        if (input == null) return null;
        if (!isOpenCvLoaded) return input;
        try {
            Mat src = new Mat();
            Utils.bitmapToMat(input, src);
            Rect biggest = findMainObjectRect(src);
            src.release();

            Bitmap output = input.copy(Bitmap.Config.ARGB_8888, true);
            if (biggest != null) {
                Canvas canvas = new Canvas(output);
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#2E7D32")); // Màu xanh thương hiệu SmartCook
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(8f);
                canvas.drawRect(biggest.x, biggest.y, biggest.x + biggest.width, biggest.y + biggest.height, paint);
            }
            return output;
        } catch (Throwable t) {
            Log.e(TAG, "Error in drawMainObjectBox: ", t);
            return input;
        }
    }
}
