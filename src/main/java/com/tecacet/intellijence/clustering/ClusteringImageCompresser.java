package com.tecacet.intellijence.clustering;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ClusteringImageCompresser {

	private int colors = 16;
	private int bits = 4;

	public BufferedImage compress(BufferedImage image) {

		/*
		 * Reshape the image into a list of pixels. Each pixel has 3 dimensions
		 * corresponding to the RGB colors
		 */
		List<double[]> data = reshape(image);

		/* Train the clustering algorithm on the data */
		KMeansClusterer<double[]> clusterer = new KMeansClusterer<double[]>(
				colors, new EuclideanMetric(), new MeanCenterExtractor());
		List<double[]> centers = clusterer.cluster(data);

		/* Cleate a color model with the reduced set of colors */
		IndexColorModel cm = generateColorModel(centers, colors, bits);

		/* Create an array where each pixel value points to a color in the index */
		int width = image.getWidth();
		int height = image.getHeight();
		byte[] pixels = new byte[width * height];
		int i = 0;
		for (double[] datum : data) {
			int c = clusterer.findClosestCenter(datum);
			pixels[i++] = (byte) c;
		}

		// Create a data buffer using the byte buffer of pixel data.
		// The pixel data is not copied; the data buffer uses the byte buffer
		// array.
		DataBuffer dbuf = new DataBufferByte(pixels, width * height, 0);

		// Prepare a sample model that specifies a storage 4-bits of
		// pixel data in an 8-bit data element
		int bitMasks[] = new int[] { 0xf };
		SampleModel sampleModel = new SinglePixelPackedSampleModel(
				DataBuffer.TYPE_BYTE, width, height, bitMasks);

		// Create a raster using the sample model and data buffer
		WritableRaster raster = Raster.createWritableRaster(sampleModel, dbuf,
				null);

		// Combine the color model and raster into a buffered image
		BufferedImage compressed = new BufferedImage(cm, raster, false, null);
		return compressed;

	}

	private static IndexColorModel generateColorModel(List<double[]> centers,
			int colors, int bits) {

		byte[] r = new byte[colors];
		byte[] g = new byte[colors];
		byte[] b = new byte[colors];

		for (int i = 0; i < centers.size(); i++) {
			r[i] = (byte) centers.get(i)[0];
			g[i] = (byte) centers.get(i)[1];
			b[i] = (byte) centers.get(i)[2];
		}
		return new IndexColorModel(bits, colors, r, g, b);
	}

	/*
	 * Convert an RGB image into a list of or RGB values represented as a 3D
	 * vector
	 */
	private static List<double[]> reshape(BufferedImage image) {
		final int width = image.getWidth();
		final int height = image.getHeight();

		List<double[]> result = new ArrayList<double[]>(height * width);

		for (int col = 0; col < height; col++) {
			for (int row = 0; row < width; row++) {
				double[] rgb = getRGB(image, row, col);
				result.add(rgb);
			}
		}
		return result;
	}

	private static double[] getRGB(BufferedImage image, int row, int col) {
		double[] rgb = new double[3];
		for (int c = 0; c < 3; c++) {
			// System.out.println(row+" "+col+" "+c);
			rgb[c] = image.getRaster().getSample(row, col, c);
		}
		return rgb;
	}

	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(new File("JungleBird.png"));
		ClusteringImageCompresser compression = new ClusteringImageCompresser();
		BufferedImage compressed = compression.compress(image);
		ImageIO.write(compressed, "png", new File("compressed.png"));
	}
}