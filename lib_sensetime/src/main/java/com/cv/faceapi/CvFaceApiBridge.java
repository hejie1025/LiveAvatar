package com.cv.faceapi;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

import java.util.Arrays;
import java.util.List;

/**
 * CvFaceSDK: Native API of DLL
 * Created by Guangli W on 9/7/15.
 */
public interface CvFaceApiBridge extends Library {

    class cv_rect_t extends Structure {
    	
        public static class ByValue extends cv_rect_t implements Structure.ByValue {
        }

        public int left;
        public int top;
        public int right;
        public int bottom;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"left", "top", "right", "bottom"});
        }

        @Override
        public cv_rect_t clone() {
        	cv_rect_t copy = new cv_rect_t();
            copy.left = this.left;
            copy.top = this.top;
            copy.right = this.right;
            copy.bottom = this.bottom;
            return copy;
        }

        /**
         * The jna.Structure class is passed on by reference by default,
         * however, in some cases, we need it by value.
         */
        public cv_rect_t.ByValue copyToValue() {
        	cv_rect_t.ByValue retObj = new cv_rect_t.ByValue();
            retObj.left = this.left;
            retObj.top = this.top;
            retObj.right = this.right;
            retObj.bottom = this.bottom;
            return retObj;
        }
    }

    class cv_pointf_t extends Structure {
    	public static class ByReference extends cv_pointf_t implements Structure.ByReference {};
        public static class ByValue extends cv_pointf_t implements Structure.ByValue{};
        public float x;
        public float y;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"x", "y"});
        }
    }

    class cv_pointi_t extends Structure {
    	public static class ByReference extends cv_pointi_t implements Structure.ByReference {};
        public static class ByValue extends cv_pointi_t implements Structure.ByValue{};
        public int x;
        public int y;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"x", "y"});
        }
    }

    class cv_face_t extends Structure {
    	public cv_rect_t rect;
    	public float score;
    	public float[] points_array = new float[42];
    	public int points_count;
    	public int yaw;
    	public int pitch;
    	public int roll;
    	public int eye_dist;
    	public int ID;

        public cv_face_t() {
            super();
        }

        public cv_face_t(Pointer p) {
            super(p);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"rect", "score", "points_array", "points_count", 
            		"yaw", "pitch", "roll", "eye_dist", "ID"});
        }

        @Override
        public cv_face_t clone() {
        	cv_face_t copy = new cv_face_t();
            copy.rect = this.rect.clone();
            copy.score = this.score;
            copy.points_array = this.points_array;
            copy.points_count = this.points_count;
            copy.yaw = this.yaw;
            copy.pitch = this.pitch;
            copy.roll = this.roll;
            copy.eye_dist = this.eye_dist;
            copy.ID = this.ID;
            return copy;
        }

        public static cv_face_t[] arrayCopy(cv_face_t[] origin) {
        	cv_face_t[] copy = new cv_face_t[origin.length];
            for (int i = 0; i < origin.length; ++i) {
                copy[i] = origin[i].clone();
            }
            return copy;
        }
    }
    class cv_face_106_t extends Structure {
    	public cv_rect_t rect;
    	public float score;
    	public float[] points_array = new float[212];
    	public int points_count;
    	public int yaw;
    	public int pitch;
    	public int roll;
    	public int eye_dist;
    	public int ID;

        public cv_face_106_t() {
            super();
        }

        public cv_face_106_t(Pointer p) {
            super(p);
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"rect", "score", "points_array", "points_count", 
            		"yaw", "pitch", "roll", "eye_dist", "ID"});
        }

        @Override
        public cv_face_106_t clone() {
        	cv_face_106_t copy = new cv_face_106_t();
            copy.rect = this.rect.clone();
            copy.score = this.score;
            copy.points_array = this.points_array;
            copy.points_count = this.points_count;
            copy.yaw = this.yaw;
            copy.pitch = this.pitch;
            copy.roll = this.roll;
            copy.eye_dist = this.eye_dist;
            copy.ID = this.ID;
            return copy;
        }

        public static cv_face_106_t[] arrayCopy(cv_face_106_t[] origin) {
        	cv_face_106_t[] copy = new cv_face_106_t[origin.length];
            for (int i = 0; i < origin.length; ++i) {
                copy[i] = origin[i].clone();
            }
            return copy;
        }
    }
    
    class cv_quality_result_t extends Structure {
    	public static class ByReference extends cv_quality_result_t implements Structure.ByReference {};
        public static class ByValue extends cv_quality_result_t implements Structure.ByValue{};
        public float score;

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"score"});
        }
    }
    
    class cv_feature_t extends Structure {
    	public int ver;
    	public int idx;
    	public int len;
	public float[] feat = new float[180];;
    	
    	public cv_feature_t() {
    		super();
		}

    	public cv_feature_t(Pointer p) {
    		super(p);
    	}

		@Override
		protected List getFieldOrder() {
			// TODO Auto-generated method stub
			return Arrays.asList(new String[]{"ver", "idx", "len", "feat"});
		}

		@Override
        public cv_feature_t clone() {
			cv_feature_t copy = new cv_feature_t();
            copy.ver = this.ver;
            copy.idx = this.idx;
            copy.len = this.len;
            copy.feat = this.feat;
            return copy;
        }

		public static cv_feature_t [] arrayCopy(cv_feature_t[] origin) {
			 cv_feature_t[] copy = new cv_feature_t[origin.length];
	            for (int i = 0; i < origin.length; ++i) {
	                copy[3] = origin[3].clone();
	            }
	            return copy;
	        }
    }

    enum ResultCode {
        CV_OK(0),
        CV_E_INVALIDARG(-1),
        CV_E_HANDLE(-2),
        CV_E_OUTOFMEMORY(-3),
        CV_E_FAIL(-4),
        CV_E_DELNOTFOUND(-5);

        private final int resultCode;

        ResultCode(int resultCode) {
            this.resultCode = resultCode;
        }

        public int getResultCode() {
            return resultCode;
        }
    }

    /**
     * The instance of facesdk DLL
     */
    CvFaceApiBridge FACESDK_INSTANCE = (CvFaceApiBridge) Native.loadLibrary("cvface_api", CvFaceApiBridge.class);

    /**
     * For FaceDetector class
     */
    final int CV_FACE_DEFAULT_CONFIG = 0x00000000;
    final int CV_DETECT_SKIP_BELOW_THRESHOLD = 0x00000001;
    final int CV_FACE_RESIZE_IMG_320W = 0x00000002;
    final int CV_FACE_RESIZE_IMG_640W = 0x00000004;
    final int CV_FACE_RESIZE_IMG_1280W = 0x00000008;
    final int CV_DETECT_ENABLE_ALIGN = 0x00000100;

    //detect
    Pointer cv_face_create_detector(String model_path, int config);
    void cv_face_destroy_detector(Pointer detector_handle);
    int cv_face_detect(Pointer detector_handle, byte[] color_image,int image_fomat, int image_width,
            int image_height, int image_stride, int orientation, PointerByReference p_faces_array,
            IntByReference p_faces_count);
    int cv_face_detect(Pointer detector_handle, int[] color_image, int format, int image_width,
                                     int image_height, int image_stride, int orientation, PointerByReference p_faces_array,
                                     IntByReference p_faces_count);
    int cv_face_release_detector_result(Pointer faces_array, int faces_count);
    
    //align
    Pointer cv_face_create_alignment(String model_path, int facial_points_count);
    void cv_face_destroy_alignment(Pointer lrAlignmentor_instance);
    int cv_face_align(Pointer alignment_handle, int[] color_image, int format, int image_width,
                            int image_height, int image_stride, cv_face_t[] face, cv_pointf_t[] facial_points_array);

    //track
    Pointer cv_face_create_tracker(String model_path, int config);
    Pointer cv_face_create_tracker_106(String model_path, int config);
    int cv_face_track_106_set_detect_face_cnt_limit(Pointer tracker_handle,int max);
    int cv_face_track_set_detect_face_cnt_limit(Pointer tracker_handle,int max);
    
    int cv_face_track(Pointer tracker_handle, byte[] color_image, int format,int image_width, int image_height,
    		int stride, int orientation, PointerByReference p_faces_array, IntByReference p_faces_count);
    int cv_face_track_106(Pointer tracker_handle, byte[] color_image, int format,int image_width, int image_height,
    		int stride, int orientation, PointerByReference p_faces_array, IntByReference p_faces_count);
    
    int cv_face_track(Pointer tracker_handle, int[] color_image, int format,int image_width, int image_height,
    		int stride, int orientation, PointerByReference p_faces_array, IntByReference p_faces_count);
    int cv_face_track_106(Pointer tracker_handle, int[] color_image, int format,int image_width, int image_height,
    		int stride, int orientation, PointerByReference p_faces_array, IntByReference p_faces_count);
    
    void cv_face_release_tracker_result(Pointer faces_array, int faces_count);
    void cv_face_release_tracker_106_result(Pointer faces_array, int faces_count);
    
    void cv_face_destroy_tracker(Pointer tracker_handle);
    void cv_face_destroy_tracker_106(Pointer tracker_handle);
    
    //liveness detector
    
    Pointer cv_face_create_liveness_detector(String model, int config);//2
    
    void cv_face_liveness_detector_reset(Pointer liveness_handle);//3
    
    int cv_face_liveness_detect(Pointer liveness_handle,int[] color_image, int format,int image_width, int image_height,
    		int stride, cv_face_t face,FloatByReference liveness_score,IntByReference liveness_state);//4
    
    int cv_face_liveness_detect(Pointer liveness_handle,byte[] color_image, int format,int image_width, int image_height,
    		int stride, cv_face_t face,FloatByReference liveness_score,IntByReference liveness_state);//4
    
    void cv_face_destroy_liveness_detector(Pointer liveness_handle);//5
  
    //attribute 
    Pointer cv_face_create_attribute_detector(String model, int config);
    
    int cv_face_attribute_detect(Pointer attribute_handle,int[] color_image, int format,int image_width, int image_height,
    		int stride, cv_face_t face, int[] results_array);//4
    
    void cv_face_destroy_attribute_detector(Pointer attribute_handle);
    
    //evaluator
    Pointer cv_face_create_evaluator(String model_path);
    
    int cv_face_evaluate(Pointer detector_handle, int[] color_image,int image_fomat, int image_width,
            int image_height, int image_stride, cv_face_t face,cv_quality_result_t quality_result_t);
    
    void cv_face_destroy_evaluator(Pointer attribute_handle);
    
    //Verify
    Pointer cv_verify_create_handle(String model);
    
    int cv_verify_get_feature(Pointer verify_handle, int[] color_image, int format, int image_width, int image_height,
	int stride, cv_face_t face, PointerByReference p_feature, IntByReference p_feature_size);

    int cv_verify_serialize_feature(Pointer p_feature, byte [] featureString);

    Pointer cv_verify_deserialize_feature(byte [] feature_str);

    void cv_verify_release_feature(Pointer feature);

    int cv_verify_compare_feature(Pointer verify_handle, Pointer feature1, Pointer feature2, FloatByReference score);
    
    void cv_verify_destroy_handle(Pointer verify_handle);
    
    
   //group
    int cv_verify_grouping(Pointer verify_handle,Pointer[] featureArray,int feature_count,PointerByReference p_faces_array,IntByReference p_faces_count);
    void cv_verify_release_grouping_result(Pointer p_faces_array, int p_faces_count);
    
    
}
