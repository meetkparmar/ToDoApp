package com.prudhvir3ddy.todo_app_gettingthingsdone.view.splash

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.prudhvir3ddy.todo_app_gettingthingsdone.R.layout
import com.prudhvir3ddy.todo_app_gettingthingsdone.storage.SharedPrefs
import com.prudhvir3ddy.todo_app_gettingthingsdone.view.login.LoginActivity
import com.prudhvir3ddy.todo_app_gettingthingsdone.view.main.TasksActivity
import com.prudhvir3ddy.todo_app_gettingthingsdone.view.onboarding.OnBoardingActivity
import org.koin.android.ext.android.inject

class SplashActivity : AppCompatActivity() {

  companion object {
    const val MY_REQUEST_CODE = 1
    val TAG = SplashActivity::class.simpleName
  }

  private lateinit var appUpdateManager: AppUpdateManager

  private val sharedPrefs: SharedPrefs by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(layout.activity_splash)

    getFcmToken()

    handleAppFlow()

    handleAppUpdateManager()

  }

  private fun handleAppUpdateManager() {
    appUpdateManager = AppUpdateManagerFactory.create(this)
    val appUpdateInfoTask = appUpdateManager.appUpdateInfo
    appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
      if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
        // For a flexible update, use AppUpdateType.FLEXIBLE
        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
      ) {
        requestUpdate(appUpdateInfo)
      }
    }
  }

  private fun requestUpdate(appUpdateInfo: AppUpdateInfo) {
    appUpdateManager.startUpdateFlowForResult(
      appUpdateInfo,
      AppUpdateType.FLEXIBLE, //  HERE specify the type of update flow you want
      this,   //  the instance of an activity
      MY_REQUEST_CODE
    )
  }

  private fun handleAppFlow() {
    when {
      sharedPrefs.getLogin() -> {
        startActivity(Intent(this, TasksActivity::class.java))
      }
      sharedPrefs.getFirstTime() -> {
        startActivity(Intent(this, LoginActivity::class.java))
      }
      else -> {
        startActivity(Intent(this, OnBoardingActivity::class.java))
      }
    }
    finish()
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == MY_REQUEST_CODE) {
      when (resultCode) {
        Activity.RESULT_OK -> {
        }
        Activity.RESULT_CANCELED -> {
        }
        ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
        }
      }
    }
  }


}
