/*
 * Copyright (c) 2011-2012, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.factory.feature.disparity;

import boofcv.abst.feature.disparity.StereoDisparity;
import boofcv.abst.feature.disparity.WrapDisparitySadRect;
import boofcv.alg.feature.disparity.DisparityScoreSadRect;
import boofcv.alg.feature.disparity.DisparitySelect;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;

/**
 * @author Peter Abeles
 */
@SuppressWarnings("unchecked")
public class FactoryStereoDisparity {


	public static <T extends ImageSingleBand> StereoDisparity<T,ImageUInt8>
	regionWta( int maxDisparity,
			   int regionRadiusX, int regionRadiusY ,
			   double maxPerPixelError ,
			   int validateRtoL ,
			   double texture ,
			   Class<T> imageType ) {

		double maxError = (regionRadiusX*2+1)*(regionRadiusY*2+1)*maxPerPixelError;

		DisparityScoreSadRect<T,ImageUInt8> alg;

		if( imageType == ImageUInt8.class ) {
			DisparitySelect<int[],T> select = (DisparitySelect)FactoryStereoDisparityAlgs.
					selectDisparity_S32((int) maxError, validateRtoL, texture);
			alg = (DisparityScoreSadRect)FactoryStereoDisparityAlgs.scoreDisparitySadRect_U8(
					maxDisparity,regionRadiusX,regionRadiusY,select);
		} else if( imageType == ImageFloat32.class ) {
			DisparitySelect<float[],T> select = (DisparitySelect)FactoryStereoDisparityAlgs.
					selectDisparity_F32((int) maxError, validateRtoL, texture);
			alg = (DisparityScoreSadRect)FactoryStereoDisparityAlgs.scoreDisparitySadRect_F32(
					maxDisparity, regionRadiusX, regionRadiusY, select);
		} else
			throw new RuntimeException("Image type not supported: "+imageType.getSimpleName() );



		return new WrapDisparitySadRect<T,ImageUInt8>(alg);
	}

	public static <T extends ImageSingleBand> StereoDisparity<T,ImageFloat32>
	regionSubpixelWta( int maxDisparity,
					   int regionRadiusX, int regionRadiusY ,
					   double maxPerPixelError ,
					   int validateRtoL ,
					   double texture ,
					   Class<T> imageType ) {

		double maxError = (regionRadiusX*2+1)*(regionRadiusY*2+1)*maxPerPixelError;

		DisparityScoreSadRect<T,ImageFloat32> alg;

		if( imageType == ImageUInt8.class ) {
			DisparitySelect<int[],T> select = (DisparitySelect)FactoryStereoDisparityAlgs.
					selectDisparitySubpixel_F32((int) maxError, validateRtoL, texture);
			alg = (DisparityScoreSadRect)FactoryStereoDisparityAlgs.scoreDisparitySadRect_U8(
					maxDisparity,regionRadiusX,regionRadiusY,select);
		} else if( imageType == ImageFloat32.class ) {
			DisparitySelect<float[],T> select = (DisparitySelect)FactoryStereoDisparityAlgs.
					selectDisparity_F32((int) maxError, validateRtoL, texture);
			alg = (DisparityScoreSadRect)FactoryStereoDisparityAlgs.scoreDisparitySadRect_F32(
					maxDisparity, regionRadiusX, regionRadiusY, select);
		} else
			throw new RuntimeException("Image type not supported: "+imageType.getSimpleName() );

		return new WrapDisparitySadRect<T,ImageFloat32>(alg);
	}
}