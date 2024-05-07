package screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.fragments.Images

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.adapters.ImagesAdapter
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.databinding.FragmentAllImagesBinding
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.model.MediaStoreImage
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.AppCommons
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.AppConstants
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.DataLoadingStats
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.GridSpacingItemDecoration
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.utils.LoadingDialog
import screen.mirroring.tvcasting.screencast.miracast.apps.casttotv.screenmirror.smarttv.miracast.chromecast.aircast.remote.castingapp.viewmodel.ImagesViewModel
import javax.inject.Inject


@AndroidEntryPoint
class ImagesFragment : Fragment(), ImagesAdapter.ImageClickListener {


    private var rvImages: RecyclerView? = null
    private var imagesAdapter: ImagesAdapter? = null
    private val imagesViewModel: ImagesViewModel by viewModels()
    private lateinit var backPressedCallback: OnBackPressedCallback

    @Inject
    lateinit var allImagesBinding: FragmentAllImagesBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View {

        return allImagesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LoadingDialog.hideLoadingDialog()
        initRecyclerView()
        AppCommons.myPostAnalytic("all_image_screen_loaded")
        CoroutineScope(Dispatchers.Main).launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermissionsAndroid13AboveIMAGES()
            } else {
                checkPermissions()
            }
        }

        onBackPressed()
    }

    /**
     * function for init recyclerview android
     */
    private fun initRecyclerView() {
        rvImages = allImagesBinding.rvImages
        rvImages?.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(GridSpacingItemDecoration(3, 0, true))
            adapter = imagesAdapter
        }
    }


    @Suppress("UNUSED_EXPRESSION")
    private fun loadImages() {
        if (AppConstants.allImagesList.size == 0) {
            imagesViewModel.data.observe(viewLifecycleOwner) { images ->
                if (images.isNotEmpty()) {
                    AppConstants.allImagesList = images as MutableList<MediaStoreImage>
                    imagesAdapter = ImagesAdapter(requireContext(), this, this)
                    imagesAdapter?.updateData(images)
                    showImages()
                    allImagesBinding.notFoundMedia.visibility = View.GONE
                    rvImages?.visibility = View.VISIBLE
                } else {
                    allImagesBinding.notFoundMedia.visibility = View.VISIBLE
                    rvImages?.visibility = View.GONE
                    // Toast.makeText(requireContext(), "No Images Found!", Toast.LENGTH_SHORT).show()
                }
            }
            imagesViewModel.dataLoadingStats.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is DataLoadingStats.Loading -> {
                        allImagesBinding.progressImages.visibility = View.VISIBLE
                    }
                    is DataLoadingStats.Completed -> {
                        allImagesBinding.progressImages.visibility = View.GONE
                    }
                    is DataLoadingStats.Failure -> {
                        allImagesBinding.progressImages.visibility = View.GONE
                    }
                }
            }
            imagesViewModel.loadAllImagesData()
        } else {

            imagesAdapter = ImagesAdapter(requireContext(), this, this)
            imagesAdapter?.updateData(AppConstants.allImagesList)
            showImages()
        }
    }

    private fun showImages() {
        rvImages?.adapter = imagesAdapter
    }

    /**
     * function for get READ EXTERNAL STORAGE PERMISSION
     */
    private fun checkPermissions() {

        Permissions.check(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE,
            null,
            object : PermissionHandler() {
                override fun onGranted() {
                    loadImages()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissionsAndroid13AboveIMAGES() {
        val permissions = arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        Permissions.check(requireContext(), permissions, null, null, object : PermissionHandler() {
            override fun onGranted() {
                println("${AppConstants.tag_all_images_fragment_error} PERMISSION IMAGES ALL")
                loadImages()
            }
        })
    }


    override fun onImageItemClick(item: MediaStoreImage) {

    }


    override fun onDestroy() {
        super.onDestroy()

        if (::backPressedCallback.isInitialized) {
            backPressedCallback.remove()
        }
        println("${AppConstants.tag_all_images_fragment_error}_FRAGMENT_DESTROYED")
    }

    private fun onBackPressed() {
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
                ImagesHomeFragment.isViewPagerInit = false
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, backPressedCallback
        )
    }

}