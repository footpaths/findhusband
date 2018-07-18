package nguyen.findhusband

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.telephony.TelephonyManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException



class PhoneStateReceiver : BroadcastReceiver() {
    private val TAG = PhoneStateReceiver::class.java!!.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        try {

             MainActivity.instance.myAudioRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            MainActivity.instance.myAudioRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            MainActivity.instance.myAudioRecorder?.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
            MainActivity.instance.myAudioRecorder?.setOutputFile(MainActivity.instance.outputFile)



            println("Receiver start")
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING) {

                Toast.makeText(context, "Incoming Call State", Toast.LENGTH_SHORT).show()
       //         Toast.makeText(context, "Ringing State Number is -$incomingNumber", Toast.LENGTH_SHORT).show()
//

            }
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                //nhận cuộc gọi
               // Toast.makeText(context, "Call Received State", Toast.LENGTH_SHORT).show()
                try {
                    MainActivity.instance.myAudioRecorder = MediaRecorder()
                    MainActivity.instance.myAudioRecorder?.prepare()
                    MainActivity.instance.myAudioRecorder?.start()
                } catch (ise: IllegalStateException) {
                    // make something ...
                } catch (ioe: IOException) {
                    // make something
                }

            }
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                //kết thúc
                val fdelete = File(MainActivity.instance.outputFile)
                println(MainActivity.instance.outputFile)
                fdelete.delete()
              //  MainActivity.instance.myAudioRecorder?.stop()
               // MainActivity.instance.myAudioRecorder?.release()
               // MainActivity.instance.myAudioRecorder = null
                if (null != MainActivity.instance.myAudioRecorder) {

                    try {
                        MainActivity.instance.myAudioRecorder!!.prepare()
                    } catch (e: IllegalStateException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    } catch (e: IOException) {
                        // TODO Auto-generated catch block
                        e.printStackTrace()
                    }

                    MainActivity.instance.myAudioRecorder!!.stop()
                    MainActivity.instance.myAudioRecorder!!.reset()
                    MainActivity.instance.myAudioRecorder!!.release()

                    MainActivity.instance.myAudioRecorder = null

                }


             //   Toast.makeText(context, "Call Idle State", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }






    }
}
