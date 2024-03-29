package com.example.picoff.ui.dialogs

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.SavedStateViewModelFactory
import com.bumptech.glide.Glide
import com.example.picoff.R
import com.example.picoff.models.PendingChallengeModel
import com.example.picoff.ui.MainActivity
import com.example.picoff.viewmodels.MainViewModel
import java.io.File


class PendingChallengeDialogFragment : DialogFragment() {

    companion object {
        private const val PENDING_CHALLENGE = "pendingChallenge"
        private const val SHOW_ONLY_INFO = "showOnlyInfo"

        fun newInstance(pendingChallenge: PendingChallengeModel, showOnlyInfo: Boolean = false) = PendingChallengeDialogFragment().apply {
            arguments = bundleOf(
                PENDING_CHALLENGE to pendingChallenge,
                SHOW_ONLY_INFO to showOnlyInfo)
        }
    }

    private var showOnlyInfo: Boolean = false
    private lateinit var pendingChallenge: PendingChallengeModel

    private lateinit var tvChallengerName: TextView
    private lateinit var tvChallengeTitle: TextView
    private lateinit var tvChallengeDesc: TextView
    private lateinit var tvStatus: TextView
    private lateinit var ivUserAvatar: ImageView
    private lateinit var btnCancel: Button
    private lateinit var btnAccept: Button

    private val viewModel: MainViewModel by activityViewModels {
        SavedStateViewModelFactory(requireActivity().application, requireActivity())
    }

    private var mediaPath: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView: View = inflater.inflate(R.layout.dialog_pending_challenge, container, false)

        // Get arguments
        showOnlyInfo = requireArguments().getBoolean(SHOW_ONLY_INFO)
        pendingChallenge = if (Build.VERSION.SDK_INT >= 33) {
            requireArguments().getParcelable(PENDING_CHALLENGE, PendingChallengeModel::class.java)!!
        } else {
            requireArguments().getParcelable(PENDING_CHALLENGE)!!
        }


        tvChallengerName = rootView.findViewById(R.id.tvDlgPendingChallengerName)
        ivUserAvatar = rootView.findViewById(R.id.ivDlgPendingChallengeAvatar)
        tvChallengeTitle = rootView.findViewById(R.id.tvDlgPendingChallengeTitle)
        tvChallengeDesc = rootView.findViewById(R.id.tvDlgPendingChallengeDesc)
        btnCancel = rootView.findViewById(R.id.btnDlgPendingChallengeCancel)
        tvStatus = rootView.findViewById(R.id.tvDlgPendingChallengeStatus)
        btnAccept = rootView.findViewById(R.id.btnDlgPendingChallengeAccept)

        // Get display name + avatar of challenge creator from firebase
        val isUserChallenger = viewModel.auth.currentUser!!.uid == pendingChallenge.uidChallenger
        var opponentName = pendingChallenge.nameChallenger
        var opponentPhotoUrl = pendingChallenge.photoUrlChallenger
        if (isUserChallenger) {
            opponentName = pendingChallenge.nameRecipient
            opponentPhotoUrl = pendingChallenge.photoUrlRecipient
        }

        tvChallengerName.text = opponentName
        Glide.with(requireContext()).load(opponentPhotoUrl).into(ivUserAvatar)
        rootView.findViewById<LinearLayout>(R.id.lytAvatar).visibility = View.VISIBLE

        tvChallengeTitle.text = pendingChallenge.challengeTitle
        tvChallengeDesc.text = pendingChallenge.challengeDesc

        btnCancel.setOnClickListener {
            dismiss()
        }

        val statusText = "Status: ${pendingChallenge.status}"
        tvStatus.text = statusText

        if (showOnlyInfo) {
            btnAccept.visibility = View.INVISIBLE
        } else {
            btnAccept.setOnClickListener {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                try {
                    if ((activity as MainActivity).checkPermissions().isEmpty()) {
                        mediaPath = viewModel.createNewImageFile(requireContext())
                        val uri = FileProvider.getUriForFile(
                            requireContext(),
                            "com.example.picoff.provider",
                            mediaPath!!
                        )
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
                        launcher.launch(takePictureIntent)
                    } else {
                        Toast.makeText(context, "Permission not granted! Please grant permissions in settings!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
                } catch (e: ActivityNotFoundException) {
                    // display error state to the user
                }
            }
        }

        return rootView
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                if (mediaPath != null) {
                    val dialog = DisplayCameraImageDialogFragment.newInstance(
                        pendingChallenge = pendingChallenge,
                        filePath = mediaPath!!.absolutePath,
                        responseMode = true
                    )
                    dialog.show(parentFragmentManager, "displayCameraImageDialog")
                } else {
                    Toast.makeText(context, "Error: image could not be loaded", Toast.LENGTH_SHORT).show()
                }

                dismiss()
            }
        }
}
