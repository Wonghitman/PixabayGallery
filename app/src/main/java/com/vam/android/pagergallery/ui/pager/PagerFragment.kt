package com.vam.android.pagergallery.ui.pager

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.text.DateFormat.getDateTimeInstance
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.permissionx.guolindev.PermissionX
import com.vam.android.pagergallery.*
import com.vam.android.pagergallery.base.BaseFragment
import com.vam.android.pagergallery.databinding.FragmentPagerBinding
import com.vam.android.pagergallery.ui.gallery.GalleryViewModel
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.senab.photoview.PhotoView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Date


class PagerFragment : BaseFragment<FragmentPagerBinding, GalleryViewModel>(
    FragmentPagerBinding::inflate,
    GalleryViewModel::class.java,
    true
) {
    private lateinit var viewPager2: ViewPager2


    override fun initFragment(
        binding: FragmentPagerBinding,
        viewModel: GalleryViewModel,
        savedInstanceState: Bundle?
    ) {


        viewPager2 = binding.viewPager2
        val adapter = PagerAdapter()
        viewPager2.adapter = adapter
        Log.d("faf", arguments?.getInt("PHOTO_POSITION").toString())
        Log.d("faf", publicViewModel!!.PagingList.value.toString())


        publicViewModel!!.PagingList.observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(publicViewModel!!.PagingList.value)
            viewPager2.setCurrentItem(arguments?.getInt("PHOTO_POSITION") ?: 0, false)

        }


        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val photoTag = binding.photoTag
                photoTag.text = getString(
                    R.string.photo_tag,
                    position + 1,
                    publicViewModel!!.PagingList.value?.size
                )

            }

        })

        binding.saveButton.setOnClickListener {
            PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .request { allGranted, grantedList, deniedList ->
                    if (allGranted) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            withContext(Dispatchers.IO) {
                                savephoto()
                            }
                        }

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "这些权限被拒绝: $deniedList",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
        binding.editButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                editPhoto()
            }
        }

        binding.shateButtion.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                sharePhoto()
            }
        }


    }

    private suspend fun editPhoto() {
        withContext(Dispatchers.IO) {
            val holder =
                (viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(viewPager2.currentItem) as PagerPhotoViewHolder
            val bitmap: Bitmap =
                getBitmapFromURL(holder.itemView.tag as String) ?: return@withContext

            // 创建一个临时文件来保存图片
            val file = File(requireContext().cacheDir, "shared_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // 使用FileProvider获取文件的Uri
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )

            // Call uCrop with the Uri of the saved bitmap
            cutImageByuCrop(uri)
        }

    }

    private suspend fun sharePhoto() {
        withContext(Dispatchers.IO) {

            val holder =
                (viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(viewPager2.currentItem) as PagerPhotoViewHolder
            val bitmap: Bitmap =
                getBitmapFromURL(holder.itemView.tag as String) ?: return@withContext
            val file = File(requireContext().cacheDir, "shared_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/jpeg"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, "分享到"))

        }
    }

    private suspend fun savephoto() {
        withContext(Dispatchers.IO) {
            val holder =
                (viewPager2[0] as RecyclerView).findViewHolderForAdapterPosition(viewPager2.currentItem) as PagerPhotoViewHolder
            val bitmap: Bitmap =
                getBitmapFromURL(holder.itemView.tag as String) ?: return@withContext

            // 创建一个临时文件来保存图片
            val file = File(requireContext().cacheDir, "shared_image.jpg")
            val fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()

            // 使用FileProvider获取文件的Uri
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "${BuildConfig.APPLICATION_ID}.provider",
                file
            )

            // 使用ContentResolver将图片保存到媒体库,并且重命名
            val contentValues = ContentValues().apply {
                put(
                    MediaStore.Images.Media.DISPLAY_NAME,
                    "${getDateTimeInstance().format(Date())}.jpg"
                )
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val collection = MediaStore.Images.Media.getContentUri("external")
            val photoUri = requireContext().contentResolver.insert(collection, contentValues)

            requireContext().contentResolver.openOutputStream(photoUri!!).use { outputStream ->
                requireContext().contentResolver.openInputStream(uri).use { inputStream ->
                    if (outputStream != null && inputStream != null)
                        inputStream.copyTo(outputStream)
                }
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            requireContext().contentResolver.update(photoUri, contentValues, null, null)

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "储存成功", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val uCropActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val resultUri = UCrop.getOutput(result.data!!)
                    val contentValues = ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "${getDateTimeInstance().format(Date())}+edited.jpg"
                        )
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val collection = MediaStore.Images.Media.getContentUri("external")
                    val photoUri =
                        requireContext().contentResolver.insert(collection, contentValues)

                    requireContext().contentResolver.openOutputStream(photoUri!!)
                        .use { outputStream ->
                            requireContext().contentResolver.openInputStream(resultUri!!)
                                .use { inputStream ->
                                    if (outputStream != null && inputStream != null)
                                        inputStream.copyTo(outputStream)
                                }
                        }

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    requireContext().contentResolver.update(photoUri, contentValues, null, null)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "储存成功", Toast.LENGTH_SHORT).show()
                    }
                }
            } else if (result.resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(result.data!!)
                Toast.makeText(requireContext(), "裁剪失败: $cropError", Toast.LENGTH_SHORT).show()
            }
        }


    // 裁剪图片
    fun cutImageByuCrop(uri: Uri) {
        val uCrop = UCrop.of(uri, Uri.fromFile(File(requireContext().cacheDir, "temp.jpg")))
        uCrop.withOptions(getUCropOptions())
        uCropActivityResultLauncher.launch(uCrop.getIntent(requireContext()))
    }

    // UCrop配置
    fun getUCropOptions(): UCrop.Options {
        val options = UCrop.Options()
        options.setCompressionQuality(100)
        options.setToolbarColor(
            ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.design_default_color_primary
            )
        )
        options.setStatusBarColor(
            ContextCompat.getColor(
                requireContext(),
                com.google.android.material.R.color.design_default_color_primary_variant
            )
        )
        return options
    }

    suspend fun getBitmapFromURL(src: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }


}

