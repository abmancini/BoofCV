/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.detect.intensity;

import gecv.abst.detect.intensity.*;
import gecv.abst.filter.blur.FactoryBlurFilter;
import gecv.alg.misc.PixelMath;
import gecv.alg.transform.gss.FactoryGaussianScaleSpace;
import gecv.core.image.ConvertBufferedImage;
import gecv.gui.ListDisplayPanel;
import gecv.gui.SelectAlgorithmPanel;
import gecv.gui.image.ShowImages;
import gecv.gui.image.VisualizeImageData;
import gecv.io.image.UtilImageIO;
import gecv.struct.gss.GaussianScaleSpace;
import gecv.struct.image.ImageBase;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt16;
import gecv.struct.image.ImageUInt8;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Displays the intensity of detected features across scalespace
 *
 * @author Peter Abeles
 */
public class IntensityFeatureScaleSpaceApp<T extends ImageBase, D extends ImageBase>
		extends SelectAlgorithmPanel {

//	static String fileName = "evaluation/data/outdoors01.jpg";
	static String fileName = "evaluation/data/sunflowers.png";
//	static String fileName = "evaluation/data/particles01.jpg";
//	static String fileName = "evaluation/data/scale/beach02.jpg";
//	static String fileName = "evaluation/data/indoors01.jpg";
//	static String fileName = "evaluation/data/shapes01.png";

	ListDisplayPanel gui = new ListDisplayPanel();

	GaussianScaleSpace<T,D> ss;

	BufferedImage input;
	T workImage;
	Class<T> imageType;

	public IntensityFeatureScaleSpaceApp( Class<T> imageType , Class<D> derivType ) {
		this.imageType = imageType;

		addAlgorithm("Hessian Det", new WrapperLaplacianBlobIntensity<T,D>(HessianBlobIntensity.Type.DETERMINANT,derivType));
		addAlgorithm("Laplacian", new WrapperLaplacianBlobIntensity<T,D>(HessianBlobIntensity.Type.TRACE,derivType));
		addAlgorithm("Harris",new WrapperGradientCornerIntensity<T,D>(FactoryCornerIntensity.createHarris(derivType,2,0.4f)));
		addAlgorithm("KLT",new WrapperGradientCornerIntensity<T,D>( FactoryCornerIntensity.createKlt(derivType,2)));
		addAlgorithm("FAST 12",new WrapperFastCornerIntensity<T,D>(FactoryCornerIntensity.createFast12(imageType,5,11)));
		addAlgorithm("KitRos",new WrapperKitRosCornerIntensity<T,D>(derivType));
		addAlgorithm("Median",new WrapperMedianCornerIntensity<T,D>(FactoryBlurFilter.median(imageType,2),imageType));

		add(gui, BorderLayout.CENTER);

		ss = FactoryGaussianScaleSpace.nocache(imageType);

		double scales[] = new double[31];
		for( int i = 0; i < scales.length ; i++ ) {
			scales[i] =  Math.exp(i*0.15);
		}
		ss.setScales(scales);
	}

	@Override
	public synchronized void setActiveAlgorithm(String name, Object cookie ) {
		if( input == null ) {
			return;
		}
		GeneralFeatureIntensity<T,D> intensity =
				(GeneralFeatureIntensity<T,D>)cookie;

		gui.reset();
		gui.addImage(input,"Original Image");

		final ProgressMonitor progressMonitor = new ProgressMonitor(this,
				"Computing Scale Space Response",
				"", 0, ss.getTotalScales());


		for( int i = 0; i < ss.getTotalScales() && !progressMonitor.isCanceled(); i++ ) {
			ss.setActiveScale(i);
			double scale = ss.getCurrentScale();
			T scaledImage = ss.getScaledImage();

			D derivX = ss.getDerivative(true);
			D derivY = ss.getDerivative(false);
			D derivXX = ss.getDerivative(true,true);
			D derivYY = ss.getDerivative(false,false);
			D derivXY = ss.getDerivative(true,false);

			intensity.process(scaledImage,derivX,derivY,derivXX,derivYY,derivXY);

			ImageFloat32 featureImg = intensity.getIntensity();
			BufferedImage b = VisualizeImageData.colorizeSign(featureImg,null, PixelMath.maxAbs(featureImg));
			gui.addImage(b,String.format("Scale %6.2f",scale));

			final int progressStatus = i+1;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					progressMonitor.setProgress(progressStatus);
				}
			});

		}
	}

	public synchronized void setImage( BufferedImage input ) {
		setPreferredSize(new Dimension(input.getWidth(),input.getHeight()));
		this.input = input;
		workImage = ConvertBufferedImage.convertFrom(input,null,imageType);
		ss.setImage(workImage);
		setPreferredSize(new Dimension(input.getWidth(),input.getHeight()));
		refreshAlgorithm();

		ShowImages.showWindow(this,"Feature Scale Space Intensity: "+imageType.getSimpleName());
	}

	public static void main( String args[] ) {
		BufferedImage input = UtilImageIO.loadImage(fileName);

		IntensityFeatureScaleSpaceApp<ImageFloat32,ImageFloat32> app =
				new IntensityFeatureScaleSpaceApp<ImageFloat32,ImageFloat32>(ImageFloat32.class,ImageFloat32.class);
		app.setImage(input);

		IntensityFeatureScaleSpaceApp<ImageUInt8, ImageSInt16> app2 =
				new IntensityFeatureScaleSpaceApp<ImageUInt8,ImageSInt16>(ImageUInt8.class,ImageSInt16.class);
		app2.setImage(input);
	}
}
