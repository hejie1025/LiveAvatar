package com.metek.liveavatar.ulsee.gles;

import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.metek.liveavatar.ulsee.FragmentTracker;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GLRendering {
    //region GL constants
    private static final int kPositionAttribute = 1;
    private static final int kTex0CoordAttribute = 2;
    private static final int kVertexColorAttribute = 3;
    private static final int kRadiusAttribute = 4;

    private static final int kConnectionCount = 61;
    private static final short kConnections[] = {
            0, 1,
            1, 2,
            2, 3,
            3, 4,
            4, 5,
            5, 6,
            6, 7,
            7, 8,
            8, 9,
            9, 10,
            10, 11,
            11, 12,
            12, 13,
            13, 14,
            14, 15,
            15, 16,
            17, 18,
            18, 19,
            19, 20,
            20, 21,
            22, 23,
            23, 24,
            24, 25,
            25, 26,
            27, 28,
            28, 29,
            29, 30,
            31, 32,
            32, 33,
            33, 34,
            34, 35,
            36, 37,
            37, 38,
            38, 39,
            39, 40,
            40, 41,
            41, 36,
            42, 43,
            43, 44,
            44, 45,
            45, 46,
            46, 47,
            47, 42,
            48, 49,
            49, 50,
            50, 51,
            51, 52,
            52, 53,
            53, 54,
            54, 55,
            55, 56,
            56, 57,
            57, 58,
            58, 59,
            59, 48,
            60, 65,
            60, 61,
            61, 62,
            62, 63,
            63, 64,
            64, 65
    };
//endregion

    private int _warper, _mesher, _pointDrawer;
    private int[] _warpUniforms;
    private int _warpVBO, _warpIBO;

    private int _meshVBO, _meshIBO, _gazeIBO;
    private int[] _meshUniforms = new int[1];

    private int _pointVBO;
    private int[] _pointUniforms = new int[1];

    private int _poseVBO, _poseIBO;

    private FloatBuffer _poseCubeCoordinates;

    public GLRendering() {
        initialise();
    }

    public void drawScene(int rotation, int width, int height,
                          float[][] shape, float[][] shapeQuality,
                          float[][] pupils, float[][] gaze,
                          float[][] pose, float[] poseQuality) {

//        drawBackground(backgroundTexture, rotation);

        for (int k = 0; k < shape.length; k++) {
            if (shape[k] != null) {
                drawMesh(shape[k], rotation, width, height);
                GLES20.glEnable(GLES20.GL_BLEND);
                drawShape(shape[k], shapeQuality[k], 5.0f, rotation, width, height);
                if (pupils[k] != null) {
                    float[] qualityRed = {0.0f, 0.0f};
                    float dx = (pupils[k][0] - pupils[k][2]);
                    float dy = (pupils[k][1] - pupils[k][3]);
                    float dist = (float) Math.sqrt(dx * dx + dy * dy);
                    float radius = Math.max(1.0f, dist / 14.0f) - 1.0f;
                    drawShape(pupils[k], qualityRed, radius, rotation, width, height);
                    GLES20.glDisable(GLES20.GL_BLEND);
                    if (gaze[k] != null) {
                        float[] gazept = new float[8];
                        final float gazeLen = 50.0f;
                        gazept[0] = pupils[k][0];
                        gazept[1] = pupils[k][1];
                        gazept[2] = pupils[k][0] + gazeLen * gaze[k][0];
                        gazept[3] = pupils[k][1] + gazeLen * gaze[k][1];
                        gazept[4] = pupils[k][2];
                        gazept[5] = pupils[k][3];
                        gazept[6] = pupils[k][2] + gazeLen * gaze[k][3];
                        gazept[7] = pupils[k][3] + gazeLen * gaze[k][4];

                        drawMesh(gazept, rotation, width, height);
                    }
                } else {
                    GLES20.glDisable(GLES20.GL_BLEND);
                }
            }

            if (pose[k] != null && poseQuality[k] > 0.0f) {
                drawPose(pose[k], poseQuality[k], rotation, width, height);
            }
        }
    }

    public void drawBackground(int textureName, int rotation) {
        GLES20.glUseProgram(_warper);
        float[] rotmat = new float[16];
        Matrix.setIdentityM(rotmat, 0);

        if (FragmentTracker.CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Matrix.orthoM(rotmat, 0, -1.0f, 1.0f, 1.0f, -1.0f, -50.0f, 100.0f);
            Matrix.rotateM(rotmat, 0, rotation+180, 0, 0, 1);
        } else
            Matrix.rotateM(rotmat, 0, rotation, 0, 0, 1);

        GLES20.glUniformMatrix4fv(_warpUniforms[0], 1, false, rotmat, 0);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureName);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        glUtils.checkGLError("texture parameters");

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _warpVBO);
        GLES20.glEnableVertexAttribArray(kPositionAttribute);
        GLES20.glVertexAttribPointer(kPositionAttribute, 3, GLES20.GL_FLOAT, false,
                5 * glUtils.BYTES_PER_FLOAT, 0);
        GLES20.glEnableVertexAttribArray(kTex0CoordAttribute);
        GLES20.glVertexAttribPointer(kTex0CoordAttribute, 2, GLES20.GL_FLOAT, false,
                5 * glUtils.BYTES_PER_FLOAT, 3* glUtils.BYTES_PER_FLOAT);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _warpIBO);
        glUtils.checkGLError("VBO setup");

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, 6, GLES20.GL_UNSIGNED_SHORT, 0);
        glUtils.checkGLError("Drawing background");

        GLES20.glDisableVertexAttribArray(kTex0CoordAttribute);
        GLES20.glDisableVertexAttribArray(kPositionAttribute);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glUseProgram(0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
    }

    private void drawMesh(float[] shape, int rotation, int width, int height) {
        final int pointCount = shape.length / 2;
        if (pointCount != 66 && pointCount != 4)
            throw new RuntimeException("Only 4 or 66 points!");

        GLES20.glUseProgram(_mesher);

        float[] mat = new float[16];
        Matrix.setIdentityM(mat, 0);

        if (FragmentTracker.CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Matrix.orthoM(mat, 0, -1.0f, 1.0f, 1.0f, -1.0f, -50.0f, 100.0f);
            Matrix.rotateM(mat, 0, rotation + 180, 0, 0, 1);
        } else
            Matrix.rotateM(mat, 0, rotation, 0, 0, 1);

        GLES20.glUniformMatrix4fv(_meshUniforms[0], 1, false, mat, 0);

        float[] rg = new float[2];
        if (pointCount == 66) {
            rg[0] = 0.0f;
            rg[1] = 1.0f;
        } else if (pointCount == 4) {
            rg[0] = 1.0f;
            rg[1] = 0.0f;
        }
        FloatBuffer points = glUtils.createFloatBuffer(pointCount * 7);
        for (int i = 0; i < pointCount; i++) {
            final float x = 2.0f * shape[2*i] / width - 1.0f;
            final float y = 2.0f * shape[2*i +1] / height - 1.0f;
            points.put(x);
            points.put(y);
            points.put(1.0f);
            points.put(rg[0]);
            points.put(rg[1]);
            points.put(0.0f);
            points.put(1.0f);
        }
        points.position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _meshVBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, points.capacity() * glUtils.BYTES_PER_FLOAT, points,
                GLES20.GL_STREAM_DRAW);
        GLES20.glVertexAttribPointer(kPositionAttribute, 3, GLES20.GL_FLOAT, false,
                7* glUtils.BYTES_PER_FLOAT, 0);
        GLES20.glEnableVertexAttribArray(kPositionAttribute);
        GLES20.glVertexAttribPointer(kVertexColorAttribute, 4, GLES20.GL_FLOAT, false,
                7* glUtils.BYTES_PER_FLOAT, 3* glUtils.BYTES_PER_FLOAT);
        GLES20.glEnableVertexAttribArray(kVertexColorAttribute);

        if (pointCount == 66) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _meshIBO);
            GLES20.glDrawElements(GLES20.GL_LINES, 2*kConnectionCount, GLES20.GL_UNSIGNED_SHORT, 0);
        } else if (pointCount == 4) {
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _gazeIBO);
            GLES20.glDrawElements(GLES20.GL_LINES, 4, GLES20.GL_UNSIGNED_SHORT, 0);
        }

        GLES20.glDisableVertexAttribArray(kVertexColorAttribute);
        GLES20.glDisableVertexAttribArray(kPositionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        glUtils.checkGLError("Drawing mesh");
    }

    private void drawShape(float[] shape, float[] quality, float radiusScale, int rotation, int width, int height) {
        final int pointCount = shape.length / 2;

        GLES20.glUseProgram(_pointDrawer);
        float[] mat = new float[16];
        Matrix.setIdentityM(mat, 0);

        if (FragmentTracker.CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Matrix.orthoM(mat, 0, -1.0f, 1.0f, 1.0f, -1.0f, -50.0f, 100.0f);
            Matrix.rotateM(mat, 0, rotation + 180, 0, 0, 1);
        } else
            Matrix.rotateM(mat, 0, rotation, 0, 0, 1);

        GLES20.glUniformMatrix4fv(_pointUniforms[0], 1, false, mat, 0);

        FloatBuffer points = glUtils.createFloatBuffer(pointCount * 8);
        for (int i = 0; i < pointCount; i++) {
            points.put(2.0f * shape[2*i] / width -1.0f);
            points.put(2.0f * shape[2*i +1] / height - 1.0f);
            points.put(1.0f);
            points.put(1.0f - quality[i]);
            points.put(quality[i]);
            points.put(0.0f);
            points.put(1.0f);
            points.put(1.0f + radiusScale * (1.0f - quality[i]));
        }
        points.position(0);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _pointVBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, points.capacity() * glUtils.BYTES_PER_FLOAT,
                points, GLES20.GL_STREAM_DRAW);
        GLES20.glEnableVertexAttribArray(kPositionAttribute);
        GLES20.glVertexAttribPointer(kPositionAttribute, 3, GLES20.GL_FLOAT, false,
                8* glUtils.BYTES_PER_FLOAT, 0);
        GLES20.glEnableVertexAttribArray(kVertexColorAttribute);
        GLES20.glVertexAttribPointer(kVertexColorAttribute, 4, GLES20.GL_FLOAT, false,
                8* glUtils.BYTES_PER_FLOAT, (3* glUtils.BYTES_PER_FLOAT));
        GLES20.glEnableVertexAttribArray(kRadiusAttribute);
        GLES20.glVertexAttribPointer(kRadiusAttribute, 1, GLES20.GL_FLOAT, false,
                8* glUtils.BYTES_PER_FLOAT, 7* glUtils.BYTES_PER_FLOAT);
        glUtils.checkGLError("Drawing shape");

        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, pointCount);

        GLES20.glDisableVertexAttribArray(kRadiusAttribute);
        GLES20.glDisableVertexAttribArray(kVertexColorAttribute);
        GLES20.glDisableVertexAttribArray(kPositionAttribute);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        glUtils.checkGLError("Unbinding after drawing shape");

    }

    private void drawPose(float[] pose, float quality, final int rotation, int width, int height) {
        GLES20.glUseProgram(_mesher);
        float[] rotmat = new float[16];
        Matrix.setIdentityM(rotmat, 0);

        if (FragmentTracker.CAMERA_FACING == Camera.CameraInfo.CAMERA_FACING_BACK) {
            Matrix.orthoM(rotmat, 0, -1.0f, 1.0f, 1.0f, -1.0f, -50.0f, 100.0f);
            Matrix.rotateM(rotmat, 0, rotation + 180, 0, 0, 1);
        } else
            Matrix.rotateM(rotmat, 0, rotation, 0, 0, 1);

        GLES20.glUniformMatrix4fv(_meshUniforms[0], 1, false, rotmat, 0);

        Matrix.setIdentityM(rotmat, 0);
        float[] tmp = new float[16];
        float[] mat = new float[16];
        Matrix.setIdentityM(tmp, 0);
        Matrix.rotateM(tmp, 0, (float) (pose[0] * 180.0 / Math.PI), 1, 0, 0);
        Matrix.rotateM(tmp, 0, (float) (pose[1] * 180.0 / Math.PI), 0, 1, 0);
        Matrix.rotateM(tmp, 0, (float) (pose[2] * 180.0 / Math.PI), 0, 0, 1);

        Matrix.multiplyMM(mat, 0, rotmat, 0, tmp, 0);

        final float focal = 700.0f;
        float[] matScale = new float[16];
        Matrix.setIdentityM(matScale, 0);
        matScale[0] = focal / (width/ 2.0f);
        matScale[5] = focal / (height/ 2.0f);
        Matrix.multiplyMM(rotmat, 0, matScale, 0, mat, 0);
        final float scale = focal / pose[5];
        float[] trans = new float[3];
        trans[0] = scale * (pose[3] - width / 2.0f) / (width / 2.0f);
        trans[1] = scale * (pose[4] - height / 2.0f) / (height / 2.0f);
        trans[2] = scale;
        rotmat[12] = trans[0]; rotmat[13] = trans[1]; rotmat[14] = trans[2];
        //this should be done in the GPU instead
        FloatBuffer cubeCoords = glUtils.createFloatBuffer(7 * 8);
        _poseCubeCoordinates.rewind();

        for (int i = 0; i < 8; i++) {
            float[] vec = new float[4];
            _poseCubeCoordinates.get(vec,0,3);
            vec[3] = 1.0f;
            float[] xyzw = new float[4];
            Matrix.multiplyMV(xyzw, 0, rotmat, 0, vec, 0);

            cubeCoords.put(xyzw[0] / xyzw[2]);
            cubeCoords.put(xyzw[1] / xyzw[2]);
            cubeCoords.put(1.0f);
            cubeCoords.put(1.0f - quality);
            cubeCoords.put(quality);
            cubeCoords.put(0.0f);
            cubeCoords.put(1.0f);
        }
        cubeCoords.position(0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _poseVBO);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, cubeCoords.capacity() * glUtils.BYTES_PER_FLOAT,
                cubeCoords, GLES20.GL_STREAM_DRAW);
        GLES20.glVertexAttribPointer(kPositionAttribute, 3, GLES20.GL_FLOAT, false,
                7 * glUtils.BYTES_PER_FLOAT, 0);
        GLES20.glEnableVertexAttribArray(kPositionAttribute);
        GLES20.glVertexAttribPointer(kVertexColorAttribute, 4, GLES20.GL_FLOAT, false,
                7 * glUtils.BYTES_PER_FLOAT, 3 * glUtils.BYTES_PER_FLOAT);
        GLES20.glEnableVertexAttribArray(kVertexColorAttribute);

        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _poseIBO);

        GLES20.glDrawElements(GLES20.GL_LINES, 24, GLES20.GL_UNSIGNED_SHORT, 0);

        GLES20.glUniformMatrix4fv(_meshUniforms[0], 1, false, glUtils.IDENTITY_MATRIX, 0);
        GLES20.glDisableVertexAttribArray(kVertexColorAttribute);
        GLES20.glDisableVertexAttribArray(kPositionAttribute);
        GLES20.glUseProgram(0);
    }

    private void initialise() {
        //initialise the programs
        {
            final String basicWarpVertex =
                    "precision highp float;\n" +
                            "attribute vec4 position;\n" +
                            "attribute vec2 textureCoord;\n"+
                            "uniform mat4 matrix;\n"+
                            "uniform mat4 textureMatrix;\n" +
                            "varying highp vec2 texCoord;\n" +
                            "void main() {\n" +
                            "  texCoord = (textureMatrix * vec4(textureCoord.x, textureCoord.y, 0.0, 1.0)).xy;\n" +
                            "  gl_Position = matrix * position; \n" +
                            "}";

            final String basicWarpFragment =
                    "#extension GL_OES_EGL_image_external : require\n" +
                            "uniform samplerExternalOES texture;\n" +
                            "varying highp vec2 texCoord;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = texture2D(texture, texCoord);\n" +
                            "}";

            String[] attributes = {"position", "textureCoord"};
            int[] attribLoc = {kPositionAttribute, kTex0CoordAttribute};
            String[] uniforms = {"matrix", "texture", "textureMatrix"};

            _warpUniforms = new int[uniforms.length];

            _warper = glUtils.createProgram(basicWarpVertex, basicWarpFragment,
                    attributes, attribLoc,  uniforms, _warpUniforms);
            if (_warper <= 0) throw new RuntimeException("Error creating warp program");
            GLES20.glUseProgram(_warper);
            GLES20.glUniformMatrix4fv(_warpUniforms[0], 1, false, glUtils.IDENTITY_MATRIX, 0);
            GLES20.glUniform1i(_warpUniforms[1], 2); //set the texture unit
            GLES20.glUniformMatrix4fv(_warpUniforms[2], 1, false, glUtils.IDENTITY_MATRIX, 0);
            glUtils.checkGLError("Creating program - setting uniform");

            int[] tmp = new int[2];
            GLES20.glGenBuffers(2, tmp, 0);
            _warpVBO = tmp[0];
            _warpIBO = tmp[1];
            float[] vertex = {
                    -1.0f, -1.0f, 1.0f, 0.0f, 0.0f,
                    1.0f, -1.0f, 1.0f, 1.0f, 0.0f,
                    1.0f,  1.0f, 1.0f, 1.0f, 1.0f,
                    -1.0f,  1.0f, 1.0f, 0.0f, 1.0f
            };
            FloatBuffer ver = glUtils.createFloatBuffer(vertex);
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _warpVBO);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, ver.capacity() * glUtils.BYTES_PER_FLOAT,
                    ver, GLES20.GL_DYNAMIC_DRAW);
            glUtils.checkGLError("Creating array buffer");
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

            short triangles[] = {2,1,0, 0, 3, 2};
            ShortBuffer tri = glUtils.createShortBuffer(triangles);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _warpIBO);
            glUtils.checkGLError("Creating element buffer");
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, tri.capacity() * glUtils.BYTES_PER_SHORT,
                    tri, GLES20.GL_STATIC_DRAW);
            glUtils.checkGLError("Creating element buffer");
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        {
            String pointDrawerVertex =
                    "precision highp float;\n " +
                            "uniform mat4 matrix;\n" +
                            "attribute vec3 position;\n" +
                            "attribute vec4 vertex_color;\n" +
                            "attribute float radius;\n" +

                            "varying mediump vec4 ptColor;\n" +
                            "void main() {\n" +
                            "  ptColor = vertex_color;\n" +
                            "  gl_PointSize = 2.*radius;\n" +
                            "  gl_Position = matrix * vec4(position,1.);\n" +
                            "}";
            String pointDrawerFragment =
                    "precision highp float;\n" +
                            "varying mediump vec4 ptColor;\n" +
                            "void main() {\n" +
                            "  float alpha = float((length(gl_PointCoord - vec2(0.5, 0.5)) < 0.5));\n" +
                            "  gl_FragColor = vec4(ptColor.rgb, alpha * ptColor.a);\n" +
                            "}";

            String attributes[] = {"position", "vertex_color", "radius"};
            String unifNames[] = {"matrix"};
            int attribLoc[] = {kPositionAttribute, kVertexColorAttribute, kRadiusAttribute};
            _pointDrawer = glUtils.createProgram(pointDrawerVertex, pointDrawerFragment,
                    attributes, attribLoc, unifNames, _pointUniforms);
            if (_pointDrawer <= 0) throw new RuntimeException("Error creating point drawing program");
            glUtils.checkGLError("Creating point drawer");
            GLES20.glUseProgram(_pointDrawer);
            float[] mat = new float[16];
            Matrix.setIdentityM(mat, 0);
            Matrix.rotateM(mat, 0, 90, 0, 0, 1);
            GLES20.glUniformMatrix4fv(_pointUniforms[0], 1, false, mat, 0);

            int[] tmp = new int[1];
            GLES20.glGenBuffers(1, tmp, 0);
            _pointVBO = tmp[0];
            glUtils.checkGLError("Creating point VBO buffer");

        }
        {
            String meshDrawerVertex =
                    "precision highp float;\n" +
                            "attribute vec4 position;\n" +
                            "attribute mediump vec4 vertex_color;\n" +
                            "uniform mat4 matrix;\n" +
                            "varying mediump vec4 color;\n" +

                            "void main() {\n" +
                            "  color = vertex_color;\n" +
                            "  gl_Position = matrix * position;\n" +
                            "}";
            String meshDrawerFragment =
                    "varying mediump vec4 color;\n" +
                            "void main() {\n" +
                            "  gl_FragColor = color;\n" +
                            "}";

            String attributes[] = {"position", "vertex_color"};
            int attribLoc[] = {kPositionAttribute, kVertexColorAttribute};
            String unifNames[] = {"matrix"};

            _mesher = glUtils.createProgram(meshDrawerVertex, meshDrawerFragment,
                    attributes, attribLoc, unifNames, _meshUniforms);
            if (_mesher <= 0) throw new RuntimeException("Error creating mesh drawing program");

            GLES20.glUseProgram(_mesher);
            GLES20.glUniformMatrix4fv(_meshUniforms[0], 1, false, glUtils.IDENTITY_MATRIX, 0);
            glUtils.checkGLError("Setting uniform matrix");

            int[] tmp = new int[2];
            GLES20.glGenBuffers(2, tmp, 0);
            _meshVBO = tmp[0];
            _meshIBO = tmp[1];
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _meshIBO);
            ShortBuffer elements = glUtils.createShortBuffer(kConnections);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, elements.capacity() * glUtils.BYTES_PER_SHORT,
                    elements, GLES20.GL_STATIC_DRAW);
            glUtils.checkGLError("Connections buffer");

            GLES20.glGenBuffers(1, tmp, 0); _gazeIBO = tmp[0];
            short gazeLines[] = {0, 1, 2, 3};
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _gazeIBO);
            ShortBuffer gazeElements = glUtils.createShortBuffer(gazeLines);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, gazeElements.capacity() * glUtils.BYTES_PER_SHORT,
                    gazeElements, GLES20.GL_STATIC_DRAW);
            glUtils.checkGLError("Gaze element buffer");

            // pose cube. These are always the same
            GLES20.glGenBuffers(2, tmp, 0);
            _poseVBO = tmp[0]; _poseIBO = tmp[1];
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, _poseVBO);
            final float sideLen = 20.0f;
            float cubeCoords[] = {
                    -sideLen, -sideLen, 1.5f*sideLen,
                    sideLen, -sideLen, 1.5f*sideLen,
                    sideLen,  sideLen, 1.5f*sideLen,
                    -sideLen,  sideLen, 1.5f*sideLen,

                    -sideLen, -sideLen, -.5f*sideLen,
                    sideLen, -sideLen, -.5f*sideLen,
                    sideLen,  sideLen, -.5f*sideLen,
                    -sideLen,  sideLen, -.5f*sideLen
            };
            _poseCubeCoordinates = glUtils.createFloatBuffer(cubeCoords);

            short cubeLines[] = {
                    0,1, 1,2, 2,3, 3,0,
                    0,4, 4,5, 5,1, 5,6,
                    6,2, 6,7, 7,3, 7,4};
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, _poseIBO);
            ShortBuffer cubeElements = glUtils.createShortBuffer(cubeLines);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, cubeElements.capacity() * glUtils.BYTES_PER_SHORT,
                    cubeElements, GLES20.GL_STATIC_DRAW);
            glUtils.checkGLError("Cube element buffer");
        }
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES20.glLineWidth(3);
        GLES20.glBlendEquation(GLES20.GL_FUNC_ADD);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

    }
}
