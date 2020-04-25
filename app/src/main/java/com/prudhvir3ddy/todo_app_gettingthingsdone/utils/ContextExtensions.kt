package com.prudhvir3ddy.todo_app_gettingthingsdone.utils

import android.content.Context
import android.widget.Toast

fun Context.showToast(text: String, duration: Int = Toast.LENGTH_LONG) {
  Toast.makeText(this, text, duration).show()
}