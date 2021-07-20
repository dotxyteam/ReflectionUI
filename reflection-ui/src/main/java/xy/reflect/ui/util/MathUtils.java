


package xy.reflect.ui.util;

import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Utilities for dealing with numbers.
 * 
 * @author olitank
 *
 */
public class MathUtils {

	public static int round(Number x) {
		return Math.round(x.floatValue());
	}

	public static Point2D.Double scaleToHeight(Point2D.Double dimension, double targetHeight) {
		double targetWidth = targetHeight * dimension.x / dimension.y;
		return new Point2D.Double(targetWidth, targetHeight);
	}

	public static Point2D.Double scaleToWidth(Point2D.Double dimension, double targetWidth) {
		double targetHeight = targetWidth * dimension.y / dimension.x;
		return new Point2D.Double(targetWidth, targetHeight);
	}

	public static Point2D.Double multiply(Point2D.Double base, Point2D.Double factor) {
		return new Point2D.Double(base.x * factor.x, base.y * factor.y);
	}

	public static Point2D.Double multiply(Point2D.Double base, double factor) {
		return multiply(base, new Point2D.Double(factor, factor));
	}

	public static Point2D.Double toDoublePoint(Dimension dim) {
		return new Point2D.Double(dim.width, dim.height);
	}

	public static Rectangle toInteger(Rectangle2D.Double dRect) {
		return new Rectangle(MathUtils.round(dRect.x), MathUtils.round(dRect.y), MathUtils.round(dRect.width),
				MathUtils.round(dRect.height));
	}

	public static double scalePercentage(double percentage, double start, double end) {
		return scaleNumber(percentage, 0, 100, start, end);
	}

	public static double scaleNumber(double input, double inputMin, double inputMax, double outputMin,
			double outputMax) {
		input = Math.max(inputMin, Math.min(inputMax, input));
		return (((input - inputMin) / (inputMax - inputMin)) * (outputMax - outputMin)) + outputMin;
	}

	public static Rectangle2D.Double scaleToFitInside(Point2D.Double size, Point2D.Double boxSize) {
		Point2D.Double candidateScaledSize1 = scaleToWidth(size, boxSize.x);
		Point2D.Double candidateScaledSize2 = scaleToHeight(size, boxSize.y);
		final Point2D.Double scaledSize;
		if ((candidateScaledSize1.x * candidateScaledSize1.y) < (candidateScaledSize2.x * candidateScaledSize2.y)) {
			scaledSize = candidateScaledSize1;
		} else {
			scaledSize = candidateScaledSize2;
		}
		double x = (boxSize.x - scaledSize.x) / 2.0;
		double y = (boxSize.y - scaledSize.y) / 2.0;
		return new Rectangle2D.Double(x, y, scaledSize.x, scaledSize.y);
	}

	public static Rectangle2D.Double scaleToBound(Point2D.Double size, Point2D.Double boxSize) {
		Point2D.Double candidateScaledSize1 = scaleToWidth(size, boxSize.x);
		Point2D.Double candidateScaledSize2 = scaleToHeight(size, boxSize.y);
		final Point2D.Double scaledSize;
		if ((candidateScaledSize1.x * candidateScaledSize1.y) > (candidateScaledSize2.x * candidateScaledSize2.y)) {
			scaledSize = candidateScaledSize1;
		} else {
			scaledSize = candidateScaledSize2;
		}
		double x = (boxSize.x - scaledSize.x) / 2.0;
		double y = (boxSize.y - scaledSize.y) / 2.0;
		return new Rectangle2D.Double(x, y, scaledSize.x, scaledSize.y);
	}

	public static Rectangle scaletoFitInside(Dimension size, Dimension boxSize) {
		return MathUtils
				.toInteger(MathUtils.scaleToFitInside(MathUtils.toDoublePoint(size), MathUtils.toDoublePoint(boxSize)));
	}

	public static Rectangle scaleToBound(Dimension size, Dimension boxSize) {
		return MathUtils
				.toInteger(MathUtils.scaleToBound(MathUtils.toDoublePoint(size), MathUtils.toDoublePoint(boxSize)));
	}

	public static Dimension toDimension(Point2D.Double point) {
		return new Dimension(round(point.x), round(point.y));
	}

	public static int unsignedByte(byte b) {
		return 0xFF & (int) b;
	}

	public static Rectangle2D.Double getBoundsAroundCenter(double centerX, double centerY, double width,
			double height) {
		double left = centerX - width / 2.0;
		double top = centerY - height / 2.0;
		return new Rectangle2D.Double(left, top, width, height);
	}

	public static double dotsToMillimeters(int dots, int dpi) {
		double MILIMITER_TO_CENTIMETER_FACTOR = 0.1;
		double CENTIMETER_TO_INCH_FACTOR = 1.0 / 2.54;
		double MILIMITER_TO_DOT_FACTOR = MILIMITER_TO_CENTIMETER_FACTOR * CENTIMETER_TO_INCH_FACTOR * dpi;
		return dots / MILIMITER_TO_DOT_FACTOR;
	}

	public static int millimetersToDots(double mm, int dpi) {
		double MILIMITER_TO_CENTIMETER_FACTOR = 0.1;
		double CENTIMETER_TO_INCH_FACTOR = 1.0 / 2.54;
		double MILIMITER_TO_DOT_FACTOR = MILIMITER_TO_CENTIMETER_FACTOR * CENTIMETER_TO_INCH_FACTOR * dpi;
		return round(mm * MILIMITER_TO_DOT_FACTOR);
	}

	public static Point2D.Double getSize(Rectangle2D.Double rect) {
		return new Point2D.Double(rect.width, rect.height);
	}

	public static double getEllipseRadius(double horizontalRadius, double verticalRadius, double angleRadians) {
		return (horizontalRadius * verticalRadius) / sqrt(pow(horizontalRadius, 2) * pow(sin(angleRadians), 2)
				+ pow(verticalRadius, 2) * pow(cos(angleRadians), 2));
	}

	public static double convexRatio(final double initialRatio, final int gravityPercent) {
		if (gravityPercent == 100) {
			return 0.0;
		}
		if (gravityPercent == 0) {
			return 1.0;
		}
		double curveHeight = -(Math.sqrt(2) / 2.0) * ((gravityPercent - 50.0) / 50.0);
		double toMaxCurveHeight = Math.sqrt(2) / 2.0 - curveHeight;
		double curvePeakX = toMaxCurveHeight / Math.sqrt(2);
		double curvePeakY = 1.0 - curvePeakX;
		if (initialRatio < curvePeakX) {
			return initialRatio * curvePeakY / curvePeakX;
		} else {
			return (initialRatio - curvePeakX) * curvePeakX / curvePeakY + curvePeakY;
		}
	}

}
