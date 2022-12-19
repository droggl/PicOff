package com.example.picoff.ui.challenges

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picoff.R
import com.example.picoff.adapters.ChallengesAdapter
import com.example.picoff.databinding.FragmentChallengesBinding
import com.example.picoff.models.ChallengeModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*


class ChallengesFragment : Fragment() {

    private var _binding: FragmentChallengesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var btnNewChallenge: FloatingActionButton
    private lateinit var rvChallenges: RecyclerView
    private lateinit var tvLoadingData: TextView
    private lateinit var challengeList: ArrayList<ChallengeModel>

    private lateinit var dbRef: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentChallengesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Set onClickListener to new challenge create button
        btnNewChallenge = root.findViewById(R.id.buttonCreateNew)
        btnNewChallenge.setOnClickListener {
            val dialog = CreateNewChallengeDialogFragment()
            dialog.show(parentFragmentManager, "createNewChallengeDialog")
        }

        // Set layout manager for recycler view
        rvChallenges = root.findViewById(R.id.rvChallenges)
        rvChallenges.layoutManager = LinearLayoutManager(context)
        rvChallenges.setHasFixedSize(true)

        tvLoadingData = root.findViewById(R.id.tvLoadingData)

        challengeList = arrayListOf()
        getChallengesData()

        return root
    }

    private fun getChallengesData() {
        rvChallenges.visibility = View.GONE
        tvLoadingData.visibility = View.VISIBLE

        dbRef = FirebaseDatabase.getInstance().getReference("Challenges")

        // TODO if online cache data into local database (or just viewmodel) with creator name -> if offline show those data
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                challengeList.clear()
                if (snapshot.exists()) {
                    for (challengeSnap in snapshot.children) {
                        val challengeData = challengeSnap.getValue(ChallengeModel::class.java)
                        challengeList.add(challengeData!!)
                    }
                    val mAdapter = ChallengesAdapter(challengeList)
                    rvChallenges.adapter = mAdapter

                    // Override onItemClickListener to open ChallengeDialogFragment
                    mAdapter.setOnItemClickListener(object : ChallengesAdapter.OnItemClickListener {
                        override fun onItemClick(position: Int) {
                            val dialog = ChallengeDialogFragment(challengeList[position])
                            dialog.show(parentFragmentManager, "challengeDialog")
                        }
                    })

                    // Hide loading screen and show
                    rvChallenges.visibility = View.VISIBLE
                    tvLoadingData.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
