package mobdoki.client;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
 

  SurfaceHolder mHolder;  
  public Camera camera; 

  public CameraPreview(Context context) {
    super(context);

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder(); 
    mHolder.addCallback(this); 
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
  }

  // Called once the holder is ready
  public void surfaceCreated(SurfaceHolder holder) {  
    // The Surface has been created, acquire the camera and tell it where
    // to draw.
    camera = Camera.open(); 
    try {
      camera.setPreviewDisplay(holder); 
      
      
    	  
      
  //    camera.setPreviewCallback(new PreviewCallback() { 
        // Called for each frame previewed
        //public void onPreviewFrame(byte[] data, Camera camera) {  
          //Preview.this.invalidate(); 
        //}
    //  });
    } catch (IOException e) { 
    	camera.release();
        camera = null;
    }
  }
  
  private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
      final double ASPECT_TOLERANCE = 0.05;
      double targetRatio = (double) w / h;
      if (sizes == null) return null;

      Size optimalSize = null;
      double minDiff = Double.MAX_VALUE;

      int targetHeight = h;

      // Try to find an size match aspect ratio and size
      for (Size size : sizes) {
          double ratio = (double) size.width / size.height;
          if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
          if (Math.abs(size.height - targetHeight) < minDiff) {
              optimalSize = size;
              minDiff = Math.abs(size.height - targetHeight);
          }
      }

      // Cannot find the one match the aspect ratio, ignore the requirement
      if (optimalSize == null) {
          minDiff = Double.MAX_VALUE;
          for (Size size : sizes) {
              if (Math.abs(size.height - targetHeight) < minDiff) {
                  optimalSize = size;
                  minDiff = Math.abs(size.height - targetHeight);
              }
          }
      }
      return optimalSize;
  }

  // Called when the holder is destroyed
  public void surfaceDestroyed(SurfaceHolder holder) { 
	camera.stopPreview();  
    camera.release();
    camera = null;
    
  }

  // Called when holder has changed
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	  Camera.Parameters parameters = camera.getParameters();

      List<Size> sizes = parameters.getSupportedPreviewSizes();
      Size optimalSize = getOptimalPreviewSize(sizes, w, h);
      parameters.setPreviewSize(optimalSize.width, optimalSize.height);

      camera.setParameters(parameters);
      camera.startPreview();
  }

}
