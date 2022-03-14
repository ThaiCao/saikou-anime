package ani.saikou.settings

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ani.saikou.BottomSheetDialogFragment
import ani.saikou.databinding.BottomSheetSettingshBinding
import ani.saikou.openLinkInBrowser
import ani.saikou.setSafeOnClickListener


class SettingsDialogFragment : BottomSheetDialogFragment() {
    private var _binding: BottomSheetSettingshBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetSettingshBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.settingsSettings.setSafeOnClickListener {
            startActivity(Intent(activity, SettingsActivity::class.java))
        }
        binding.settingsAnilistSettings.setOnClickListener{
            openLinkInBrowser("https://anilist.co/settings/lists")
        }

        binding.settingsDownloads.setSafeOnClickListener {
            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}