package nguyen.findhusband

class LockService : Service() {
    fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun onCreate() {
        super.onCreate()
    }

    fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_USER_PRESENT)
        Log.e("Action", "Screen")
        val mReceiver = ScreenReceiver()
        registerReceiver(mReceiver, filter)
        Log.e("Sample", "Screen")
        return super.onStartCommand(intent, flags, startId)

    }

    inner class LocalBinder : Binder() {
        internal val service: LockService
            get() = this@LockService
    }
}