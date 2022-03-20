package com.sandeepprabhakula.leafdiseasedetection.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sandeepprabhakula.leafdiseasedetection.R
import com.sandeepprabhakula.leafdiseasedetection.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserProfile : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        _binding = FragmentUserProfileBinding.inflate(layoutInflater,container,false)
        Glide.with(requireContext()).load(currentUser?.photoUrl).circleCrop().into(binding.dp)
        binding.userEmail.text = currentUser?.email
        binding.userName.text = currentUser?.displayName
        binding.logout.setOnClickListener {
            mAuth.signOut()
            findNavController().navigate(R.id.action_userProfile_to_loginFragment)
        }
        binding.deleteAccount.setOnClickListener {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Delete Account?")
            alert.setMessage("All the information will be erased.Do you want to delete your account?")
            alert.setPositiveButton("Confirm"){_,_->
                CoroutineScope(Dispatchers.IO).launch {
                    currentUser?.delete()?.await()
                    withContext(Dispatchers.Main){
                        findNavController().navigate(R.id.action_userProfile_to_loginFragment)
                    }
                }
            }
            alert.setNegativeButton("No"){_,_->

            }
            alert.show()
        }
        return binding.root
    }
}