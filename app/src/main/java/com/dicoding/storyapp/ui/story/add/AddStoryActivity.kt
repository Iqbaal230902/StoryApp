package com.dicoding.storyapp.ui.story.add

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.paging.ExperimentalPagingApi
import com.dicoding.storyapp.BuildConfig
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.ui.ViewModelFactory
import com.dicoding.storyapp.ui.main.MainActivity
import com.dicoding.storyapp.ui.story.viewmodel.AddStoryViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class AddStoryActivity : AppCompatActivity() {
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var currentImagePath: String
    private lateinit var token: String
    private var currentLocation: Location? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    @OptIn(ExperimentalPagingApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.getSession().observe(this) { user ->
            if (user != null) {
                token = user.token.toString()
            }
        }
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.galleryButton.setOnClickListener {
            openGallery()
        }

        binding.cameraButton.setOnClickListener {
            openCamera()
        }

        binding.buttonAdd.setOnClickListener {
            binding.buttonAdd.isEnabled = false
            val description = binding.edAddDescription.text.toString().trim()

            if (description.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty!", Toast.LENGTH_SHORT).show()
                binding.buttonAdd.isEnabled = true
                return@setOnClickListener
            }

            currentImageUri?.let { uri ->
                val contentResolver: ContentResolver = applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                val tempFile = File(cacheDir, "temp_image.jpg")
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val compressedPhoto = compressImage(tempFile)
                compressedPhoto?.let { compressed ->
                    val requestFile = compressed.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("photo", compressed.name, requestFile)

                    val descriptionBody = description.toRequestBody("text/plain".toMediaTypeOrNull())
                    val insertStory = viewModel.addNewStory(token, body, descriptionBody, currentLocation)
                    if(insertStory != null && !insertStory.error){
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }else{
                        Toast.makeText(this, "Error, Fill all field", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            binding.buttonAdd.isEnabled = true
        }

        binding.checkboxShowLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Log.d("apanih", "here")
                getCurrentLocation()
            }
        }
    }

    private fun compressImage(file: File): File? {
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, 1024, 1024)

            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)

            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val fos = FileOutputStream(file)
            fos.write(byteArrayOutputStream.toByteArray())
            fos.flush()
            fos.close()

            return file
        } catch (e: Exception) {
            return null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.ivItemPhoto.setImageURI(it)
        }
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        currentImageUri = uri
        showImage()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchGallery()
        } else {
            requestGalleryPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestGalleryPermission() {
        val permission = Manifest.permission.READ_MEDIA_IMAGES
        if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(this, "Gallery access is required to select an image.", Toast.LENGTH_SHORT).show()
        }
        requestPermissions(arrayOf(permission), REQUEST_CODE_PERMISSIONS)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            requestCameraPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestCameraPermission() {
        val permission = Manifest.permission.CAMERA
        if (shouldShowRequestPermissionRationale(permission)) {
            Toast.makeText(this, "Camera access is required to take a photo.", Toast.LENGTH_SHORT).show()
        }
        requestPermissions(arrayOf(permission), REQUEST_CODE_PERMISSIONS)
    }

    @Suppress("DEPRECATION")
    private fun launchCamera() {
        val pictureFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }

        pictureFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.fileprovider",
                it
            )
            currentImageUri = photoURI

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            }

            startActivityForResult(cameraIntent, PIC_ID)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentImagePath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIC_ID && resultCode == RESULT_OK) {
            showImage()
        }
    }

    private fun launchGallery() {
        getContent.launch("image/*")
    }

    private fun getCurrentLocation() {
        getMyLocation()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = location
                    Toast.makeText(this, getString(R.string.my_location), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, getString(R.string.accept_location), Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    companion object {
        private var currentImageUri: Uri? = null
        private const val PIC_ID = 123
        private const val REQUEST_CODE_PERMISSIONS: Int = 101
    }
}
