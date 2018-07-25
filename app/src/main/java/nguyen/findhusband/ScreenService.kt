package nguyen.findhusband

import android.annotation.TargetApi
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.AudioFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.CountDownTimer
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import java.io.File
import java.io.IOException
import java.security.AccessController.getContext
import java.text.SimpleDateFormat
import java.util.*

class ScreenService : Service() {


    private var sReceiver: BroadcastReceiver? = null


    override fun onBind(arg: Intent): IBinder? {

        return null

    }



    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartCommand(intent: Intent, flag: Int, startIs: Int): Int {

        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)

        filter.addAction(Intent.ACTION_SCREEN_OFF)

        sReceiver = PhoneStateReceiver()


        registerReceiver(sReceiver, filter)

        return Service.START_STICKY

    }

    override fun onDestroy() {

        if (sReceiver != null)

            unregisterReceiver(sReceiver)

        super.onDestroy()

    }

}