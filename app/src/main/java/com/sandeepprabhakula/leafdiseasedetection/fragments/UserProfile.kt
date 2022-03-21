package com.sandeepprabhakula.leafdiseasedetection.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.sandeepprabhakula.leafdiseasedetection.MainActivity
import com.sandeepprabhakula.leafdiseasedetection.R
import com.sandeepprabhakula.leafdiseasedetection.databinding.FragmentUserProfileBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class UserProfile : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val mAuth = FirebaseAuth.getInstance()
    private val currentUser = mAuth.currentUser
    lateinit var locale: Locale
    private var currentLanguage = "en"
    private var currentLang: String? = null
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
        binding.chooseLanguage.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle(getString(R.string.choose_language))
            val options = arrayOf("English","Hindi","Telugu")
            alertDialog.setSingleChoiceItems(options,0){dialogInterface,i->
                when(options[i]){
                    "English"->{
                        setLocale("en")
                    }
                    "Telugu"->{
                        setLocale("te")
                    }
                    "Hindi" ->{
                        setLocale("hi")
                    }
                }
            }
            alertDialog.show()
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

    private fun setLocale(localeName: String) {
        if (localeName != currentLanguage) {
            locale = Locale(localeName)
            val res = resources
            val dm = res.displayMetrics
            val conf = res.configuration
            conf.locale = locale
            res.updateConfiguration(conf, dm)
            val refresh = Intent(requireContext(),MainActivity::class.java)
            refresh.putExtra(currentLang, localeName)
            startActivity(refresh)
        } else {
            Toast.makeText(
                requireContext(), "Language, , already, , selected)!", Toast.LENGTH_SHORT).show();
        }
    }
}