package com.prudhvir3ddy.todo_app_gettingthingsdone.view.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.prudhvir3ddy.todo_app_gettingthingsdone.BuildConfig
import com.prudhvir3ddy.todo_app_gettingthingsdone.R
import com.prudhvir3ddy.todo_app_gettingthingsdone.storage.db.ToDo
import com.prudhvir3ddy.todo_app_gettingthingsdone.storage.db.ToDoDatabase
import com.prudhvir3ddy.todo_app_gettingthingsdone.utils.IntentConstants
import kotlinx.android.synthetic.main.activity_detail.description_tv
import kotlinx.android.synthetic.main.activity_detail.image_path_iv
import kotlinx.android.synthetic.main.activity_detail.title_tv
import kotlinx.android.synthetic.main.dialog_image_source_selector.view.camera_tv
import kotlinx.android.synthetic.main.dialog_image_source_selector.view.gallery_tv
import org.koin.android.ext.android.inject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {

  val GALLERY_PICK_RC = 2
  val CAMERA_CAPTURE_RC = 1

  lateinit var currentPhotoPath: String

  private val toDoDatabase: ToDoDatabase by inject()

  var id: Int? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_detail)

    val intent = intent
    title_tv.text = intent.getStringExtra(IntentConstants.TITLE)
    description_tv.text = intent.getStringExtra(IntentConstants.DESCRIPTION)
    id = intent.getIntExtra(IntentConstants.ID, 0)
    val image = intent.getStringExtra(IntentConstants.IMAGE_PATH)

    if (image != null && !TextUtils.isEmpty(image)) {
      Glide.with(this).load(image).into(image_path_iv)
    }
    image_path_iv.setOnClickListener {
      setupDialog()
    }
  }

  private fun setupDialog() {
    val view = LayoutInflater.from(this).inflate(R.layout.dialog_image_source_selector, null)
    val cameraTv = view.camera_tv
    val galleryTv = view.gallery_tv

    val dialog = AlertDialog.Builder(this)
      .setCancelable(true)
      .setTitle("Choose an action")
      .setView(view)
      .create()

    cameraTv.setOnClickListener {
      createImageFile()
      takePicture()
      dialog.hide()

    }


    galleryTv.setOnClickListener {
      val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
      startActivityForResult(intent, GALLERY_PICK_RC)
      dialog.hide()
    }

    dialog.show()
  }

  private fun takePicture() {
    Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
      // Ensure that there's a camera activity to handle the intent
      takePictureIntent.resolveActivity(packageManager)?.also {
        // Create the File where the photo should go
        val photoFile: File? = try {
          createImageFile()
        } catch (ex: IOException) {
          // Error occurred while creating the File
          null
        }
        // Continue only if the File was successfully created
        photoFile?.also {
          val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            it
          )
          takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
          startActivityForResult(takePictureIntent, CAMERA_CAPTURE_RC)
        }
      }
    }
  }

  @Throws(IOException::class)
  private fun createImageFile(): File? {
    // Create an image file name
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
      "JPEG_${timeStamp}_", /* prefix */
      ".jpg", /* suffix */
      storageDir /* directory */
    ).apply {
      // Save a file: path for use with ACTION_VIEW intents
      currentPhotoPath = absolutePath
    }
  }

  private fun askPermissions() {

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == Activity.RESULT_OK) {
      when (requestCode) {
        GALLERY_PICK_RC -> {
          val selectedImage = data?.data
          currentPhotoPath = selectedImage.toString()
          Glide.with(this).load(selectedImage).into(image_path_iv)
        }
        CAMERA_CAPTURE_RC -> {
          Glide.with(this).load(currentPhotoPath).into(image_path_iv)
        }
      }
      toDoDatabase.databaseWriteExecutor.execute {
        toDoDatabase.todoDao().updateToDo(
          ToDo(
            id = id,
            title = title_tv.text.toString(),
            description = description_tv.text.toString(),
            imagePath = currentPhotoPath
          )
        )
      }
    }
  }
}
