package com.cv.faceapi;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Environment;
import com.cv.faceapi.CvFaceApiBridge.ResultCode;
import com.cv.faceapi.CvFaceApiBridge.cv_face_t;
import com.cv.faceapi.CvFaceApiBridge.cv_feature_t;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * @author 作者 : qinll
 * @version 创建时间：2015-11-23 上午11:40:39 类说明
 */
public class CvFaceGroup {
    private final Pointer detectorHandle,verifyHandle;
	private static final String MODEL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/verify.tar";

	public CvFaceGroup()
	{
        detectorHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_create_detector(null,
        		CvFaceApiBridge.CV_DETECT_SKIP_BELOW_THRESHOLD | CvFaceApiBridge.CV_DETECT_ENABLE_ALIGN);
		verifyHandle = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_create_handle(MODEL_PATH);
	}
	
	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
		
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_destroy_detector(detectorHandle);
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_destroy_handle(verifyHandle);
	}
	public CvFace21[] detect(Bitmap image) {
        int[] colorData = CvUtils.getBGRAImageByte(image);
        return detect(colorData, CVImageFormat.CV_PIX_FMT_BGRA8888, image.getWidth(), image.getHeight(), image.getWidth());
    }
    public CvFace21[] detect(int[] colorImage, int cvImageFormat, int imageWidth, int imageHeight, int imageStride) {
        PointerByReference ptrToArray = new PointerByReference();
        IntByReference ptrToSize = new IntByReference();
        int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_face_detect(detectorHandle, colorImage, cvImageFormat, imageWidth,
                imageHeight, imageStride, 0, ptrToArray, ptrToSize);

        if (rst != ResultCode.CV_OK.getResultCode()) {
            throw new RuntimeException("Calling cv_face_detect() method failed! ResultCode=" + rst);
        }

        if (ptrToSize.getValue() == 0) {
            return new CvFace21[0];
        }

        cv_face_t arrayRef = new cv_face_t(ptrToArray.getValue());
        arrayRef.read();
        cv_face_t[] array = cv_face_t.arrayCopy((cv_face_t[]) arrayRef.toArray(ptrToSize.getValue()));
        CvFaceApiBridge.FACESDK_INSTANCE.cv_face_release_detector_result(ptrToArray.getValue(), ptrToSize.getValue());
        
        CvFace21[] ret = new CvFace21[array.length]; 
        for (int i = 0; i < array.length; i++) {
        	ret[i] = new CvFace21(array[i]);
        }

        return ret;
    }
    
	public byte [] getFeature(Bitmap image, CvFace21 face) {
		int[] colorImage = CvUtils.getBGRAImageByte(image);
		return getFeature(colorImage, CVImageFormat.CV_PIX_FMT_BGRA8888,
				image.getWidth(), image.getHeight(), image.getWidth() * 4, face);
	}
	public byte [] getFeature(int[] colorImage, int cvImageFormat,
			int imageWidth, int imageHeight, int imageStride, CvFace21 face) {
		PointerByReference ptrToArray = new PointerByReference();
		IntByReference ptrToSize = new IntByReference();
		int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_get_feature(verifyHandle, colorImage, cvImageFormat, imageWidth,imageHeight, imageStride, face, ptrToArray, ptrToSize);
		if (rst != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_verify_get_feature() method failed! ResultCode=" + rst);
		}
		if (ptrToSize.getValue() == 0) {
			return null;
		}
		byte [] str = new byte[(ptrToSize.getValue()*4+2)/3 + 1];
		int resultCode = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_serialize_feature(ptrToArray.getValue(), str);
		CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrToArray.getValue());
		if (resultCode != ResultCode.CV_OK.getResultCode()) {
			throw new RuntimeException(
					"Calling cv_verify_serialize_feature() method failed! ResultCode=" + resultCode);
		}
		return str;
	}

	public int group(ArrayList<byte[]> list,int feature_count) {
		IntByReference groups_count = null;
		int groupsCount = 0;
		if (verifyHandle != null) {
			// 反序列化
			Pointer ptrFeature1 = null;
			Pointer ptrFeature2 = null;
			Pointer ptrFeature3 = null;
			Pointer ptrFeature4 = null;
			ptrFeature1 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(list.get(0));
			ptrFeature2 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(list.get(1));
			ptrFeature3 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(list.get(2));
			ptrFeature4 = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_deserialize_feature(list.get(3));
			
			// group			
			Pointer[]  featureArray = new Pointer[4];
			featureArray[0] = ptrFeature1;
			featureArray[1] = ptrFeature2;
			featureArray[2] = ptrFeature3;
			featureArray[3] = ptrFeature4;
					
			
			PointerByReference p_groups_array = new PointerByReference();
		    groups_count = new IntByReference();
		    int rst = CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_grouping(verifyHandle, featureArray, feature_count, p_groups_array, groups_count);
			if (rst != ResultCode.CV_OK.getResultCode()) {
				throw new RuntimeException(
						"Calling cv_verify_grouping() method failed! ResultCode=" + rst);
			}
			groupsCount = groups_count.getValue();
			
			CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrFeature1);
			CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrFeature2);
			CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_feature(ptrFeature3);
			CvFaceApiBridge.FACESDK_INSTANCE.cv_verify_release_grouping_result(p_groups_array.getValue(),groups_count.getValue());
		}
		return groupsCount;
	}

}
