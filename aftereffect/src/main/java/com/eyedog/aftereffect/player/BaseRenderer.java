
package com.eyedog.aftereffect.player;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.eyedog.aftereffect.filters.GLImageFilter;
import com.eyedog.aftereffect.utils.OpenGLUtils;
import com.eyedog.aftereffect.utils.TextureRotationUtils;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * created by jw200 at 2019/3/9 14:34
 **/
public abstract class BaseRenderer implements GLSurfaceView.Renderer {
    private final String TAG = "BaseRenderer";

    protected GLSurfaceView mSurfaceView;

    protected GLImageFilter mInputFilter;

    protected GLImageFilter mOutputFilter;

    protected FloatBuffer mVertexBuffer;

    protected FloatBuffer mTextureBuffer;

    protected int mInputTexture = OpenGLUtils.GL_NOT_TEXTURE;

    protected int mIncommingWidth, mIncommingHeight;

    protected int mSurfaceWidth, mSurfaceHeight;

    protected boolean mHasInputSizeChanged = false, mHasSurfaceChanged = false;

    protected Object mLock = new Object();

    private boolean mHasFrameBufferInited = false;

    public BaseRenderer(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
        mHasSurfaceChanged = false;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mInputFilter = initInputFilter();
        mOutputFilter = initOutputFilter();
        mVertexBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.CubeVertices);
        mTextureBuffer = OpenGLUtils.createFloatBuffer(TextureRotationUtils.TextureVertices);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        synchronized (mLock) {
            mHasSurfaceChanged = true;
        }
        mSurfaceWidth = width;
        mSurfaceHeight = height;
        onDisplaySizeChanged(width, height);
        onFilterSizeChanged();
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        onFilterSizeChanged();
        beforeDrawFrame();
        if (mInputTexture != OpenGLUtils.GL_NOT_TEXTURE && mHasFrameBufferInited) {
            int currentTexture = mInputTexture;
            if (mInputFilter != null) {
                currentTexture = mInputFilter.drawFrameBuffer(currentTexture, mVertexBuffer,
                        mTextureBuffer);
            }
            int nextTextureId = onDrawFrameBuffer(currentTexture, mVertexBuffer, mTextureBuffer);
            if (nextTextureId != OpenGLUtils.GL_NOT_TEXTURE) {
                currentTexture = nextTextureId;
            }
            mOutputFilter.drawFrame(currentTexture, mVertexBuffer, mTextureBuffer);
        } else {
            Log.e(TAG, "onDrawFrame GL_NOT_TEXTURE");
        }
    }

    protected int onDrawFrameBuffer(int textureId, FloatBuffer vertexBuffer,
            FloatBuffer textureBuffer) {
        return OpenGLUtils.GL_NOT_TEXTURE;
    }

    protected void beforeDrawFrame() {

    }

    public abstract int createInputTexture();

    protected GLImageFilter initInputFilter() {
        if (mInputFilter == null) {
            mInputFilter = new GLImageFilter(mSurfaceView.getContext());
        } else {
            mInputFilter.initProgramHandle();
        }
        return mInputFilter;
    }

    protected GLImageFilter initOutputFilter() {
        if (mOutputFilter == null) {
            mOutputFilter = new GLImageFilter(mSurfaceView.getContext());
        } else {
            mOutputFilter.initProgramHandle();
        }
        return mOutputFilter;
    }

    public void onInputSizeChanged(int width, int height) {
        synchronized (mLock) {
            mHasInputSizeChanged = true;
            mIncommingWidth = width;
            mIncommingHeight = height;
        }
    }

    private void onFilterSizeChanged() {
        if (mHasInputSizeChanged) {
            synchronized (mLock) {
                if (mHasInputSizeChanged) {
                    mHasInputSizeChanged = false;
                    mHasFrameBufferInited = true;
                    onChildFilterSizeChanged();
                    if (mInputFilter != null) {
                        mInputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                        mInputFilter.initFrameBuffer(mIncommingWidth, mIncommingHeight);
                    }
                    if (mOutputFilter != null) {
                        mOutputFilter.onInputSizeChanged(mIncommingWidth, mIncommingHeight);
                    }
                }
            }
        }
    }

    protected void onChildFilterSizeChanged() {
    }

    protected void onDisplaySizeChanged(int width, int height) {
        if (mOutputFilter != null) {
            mOutputFilter.onDisplaySizeChanged(width, height);
        }
        if (mInputFilter != null) {
            mInputFilter.onDisplaySizeChanged(width, height);
        }
    }

    protected void release() {
        synchronized (mLock) {
            mHasInputSizeChanged = false;
            mHasSurfaceChanged = false;
            mHasFrameBufferInited = false;
        }
        if (mInputFilter != null) {
            mInputFilter.release();
            mInputFilter = null;
        }
        if (mOutputFilter != null) {
            mOutputFilter.release();
            mOutputFilter = null;
        }
        if (mVertexBuffer != null) {
            mVertexBuffer.clear();
            mVertexBuffer = null;
        }
        if (mTextureBuffer != null) {
            mTextureBuffer.clear();
            mTextureBuffer = null;
        }
    }
}
