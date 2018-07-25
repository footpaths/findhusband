package nguyen.findhusband

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import nguyen.ScreenPreference
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecordingActivity : Activity() {
    private val PERMISSION_CODE = 1
    private var mScreenDensity: Int = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private val DISPLAY_WIDTH = 480
    private val DISPLAY_HEIGHT = 640
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    private var mMediaRecorder: MediaRecorder? = null
    internal var filePath = ""
    private val TAG = "ScreenRecordingActivity"
    var statusOpen: String? = ""
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
      //  setContentView(R.layout.activity_screen_recording)
        Log.d("ScreenRecordingActivity", "qua")

        statusOpen = ScreenPreference.getInstance(this@ScreenRecordingActivity).orderID
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi

        initRecorder()
        prepareRecorder()
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mMediaProjectionCallback = MediaProjectionCallback()

        if (statusOpen!!.contains("true")) {
            shareScreen()
        } else {
            try {
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
                Log.v("ScreenRecordingActivity", "Recording Stopped")
                stopScreenSharing()
            }catch (e:IllegalStateException){
                println(e.toString())
            }

        }
        object : CountDownTimer(2000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
               // Toast.makeText(this@ScreenRecordingActivity, "còn : "+ (millisUntilFinished / 1000).toString(), Toast.LENGTH_SHORT).show()
               // count.setText("Time remaining " + (millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                moveTaskToBack(true)
            }
        }.start()


       // finish()

    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager!!.createScreenCaptureIntent(), PERMISSION_CODE)
            return
        }
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder?.start()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder!!.surface, null /*Handler*/, null)/*Callbacks*/
    }

    private fun stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay?.release()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: $requestCode")
            return
        }
        if (resultCode != RESULT_OK) {
            Toast.makeText(this,
                    "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show()
            // mToggleButton.setChecked(false)

            //   ScreenPreference.getInstance(this@ScreenRecordingActivity).orderID = "true"

            return
        }
        mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data)
        mMediaProjection?.registerCallback(mMediaProjectionCallback, null)
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder?.start()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)


    private fun prepareRecorder() {
        try {
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            finish()
        } catch (e: IOException) {
            e.printStackTrace()
            finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
//            if (statusOpen!!.contains("false")) {
//                mMediaRecorder!!.stop()
//                mMediaRecorder!!.reset()
//                finish()
//                Log.v(TAG, "Recording Stopped")
//            }
//            mMediaProjection = null
//            stopScreenSharing()
//            Log.i(TAG, "MediaProjection Stopped")
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initRecorder() {
        if (mMediaRecorder == null) {
            mMediaRecorder = MediaRecorder()
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder!!.setVideoEncodingBitRate(512 * 1000)
            mMediaRecorder!!.setVideoFrameRate(30)
            mMediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mMediaRecorder!!.setOutputFile(getFilePath())
        }
    }

    fun getFilePath(): String? {
        val directory = Environment.getExternalStorageDirectory().toString() + File.separator + "Recordings"
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show()
            return null
        }
        val folder = File(directory)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdir()
        }

        if (success) {
            val videoName = "capture_" + getCurSysDate() + ".mp4"
            filePath = directory + File.separator + videoName
        } else {
            Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show()
            return null
        }
        return filePath
    }

    fun getCurSysDate(): String {
        return SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        super.onDestroy()
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }

}
