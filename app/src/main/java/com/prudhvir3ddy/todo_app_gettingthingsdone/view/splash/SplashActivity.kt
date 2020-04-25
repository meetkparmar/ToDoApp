package com.prudhvir3ddy.todo_app_gettingthingsdone.view.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.prudhvir3ddy.todo_app_gettingthingsdone.R
import com.prudhvir3ddy.todo_app_gettingthingsdone.R.layout
import com.prudhvir3ddy.todo_app_gettingthingsdone.ToDoApp
import com.prudhvir3ddy.todo_app_gettingthingsdone.storage.SharedPrefs
import com.prudhvir3ddy.todo_app_gettingthingsdone.utils.showToast
import javax.inject.Inject

class SplashActivity : AppCompatActivity(), InstallStateUpdatedListener {

  @Inject
  lateinit var sharedPrefs: SharedPrefs
  private val TAG = SplashActivity::class.simpleName

  companion object {
    const val APP_UPDATE_RC = 1001
  }

  private lateinit var appUpdateManager: AppUpdateManager
  private val updateAvailable = MutableLiveData<Boolean>().apply { value = false }
  private var updateInfo: AppUpdateInfo? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    (application as ToDoApp).appComponent.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_splash)

    appUpdateManager = AppUpdateManagerFactory.create(this)
    appUpdateManager.registerListener(this)

    appUpdateManager.appUpdateInfo.addOnCompleteListener {
      showToast("GET Info complete", Toast.LENGTH_SHORT)
    }

    appUpdateManager.appUpdateInfo.addOnFailureListener {
      showToast("GET Info failed ${it.message}", Toast.LENGTH_LONG)
    }

    checkForUpdate()
    updateAvailable.observe(this, Observer {
      if (it)
        triggerUpdate(updateInfo)
    })


    getFcmToken()
//    when {
//      sharedPrefs.getLogin() -> {
//        startActivity(Intent(this, TasksActivity::class.java))
//      }
//      sharedPrefs.getFirstTime() -> {
//        startActivity(Intent(this, LoginActivity::class.java))
//      }
//      else -> {
//        startActivity(Intent(this, OnBoardingActivity::class.java))
//      }
//    }
//    finish()
  }

  private fun checkForUpdate() {
    appUpdateManager.appUpdateInfo.addOnSuccessListener {
      if (it.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        it.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
      ) {
        updateInfo = it
        updateAvailable.value = true
        showToast("Version code available ${it.availableVersionCode()}", Toast.LENGTH_LONG)
      } else {
        updateAvailable.value = false
        showToast("Update not available", Toast.LENGTH_LONG)
      }
    }
  }

  private fun getFcmToken() {
    FirebaseInstanceId.getInstance().instanceId
      .addOnCompleteListener(OnCompleteListener { task ->
        if (!task.isSuccessful) {
          Log.w(TAG, "getInstanceId failed", task.exception)
          return@OnCompleteListener
        }
        // Get new Instance ID token
        val token = task.result!!.token
        // Log and toast
        Log.d(TAG, token)
      })
  }

  override fun onResume() {
    super.onResume()
    appUpdateManager.appUpdateInfo.addOnSuccessListener {
      if (it.installStatus() == InstallStatus.DOWNLOADED) {
        showToast("in onResume, download complete", Toast.LENGTH_LONG)
        showUpdateSnackbar()
      } else {
        showToast("${it.installStatus()}", Toast.LENGTH_LONG)
      }
    }
  }

  private fun triggerUpdate(updateInfo: AppUpdateInfo?) {
    appUpdateManager.startUpdateFlowForResult(
      updateInfo, AppUpdateType.FLEXIBLE, this, APP_UPDATE_RC
    )
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    super.onActivityResult(requestCode, resultCode, data)
    showToast("request code: $requestCode, result code: $resultCode")
  }

  override fun onStateUpdate(state: InstallState?) {
    if (state?.installStatus() == InstallStatus.DOWNLOADED) {
      showToast("download complete", Toast.LENGTH_LONG)
      showUpdateSnackbar()
    }
  }

  private fun showUpdateSnackbar() {
    Snackbar
      .make(
        findViewById(R.id.splash_layout),
        R.string.restart_to_update,
        Snackbar.LENGTH_INDEFINITE
      )
      .setAction(R.string.action_restart) { appUpdateManager.completeUpdate() }
      .show()
  }

  override fun onDestroy() {
    super.onDestroy()
    appUpdateManager.unregisterListener(this)
  }
}
