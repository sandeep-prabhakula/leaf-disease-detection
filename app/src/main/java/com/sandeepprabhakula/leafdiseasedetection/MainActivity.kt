package com.sandeepprabhakula.leafdiseasedetection

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sandeepprabhakula.leafdiseasedetection.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navController = findNavController(R.id.navHostFragment)
        val bottomNav:BottomNavigationView = findViewById(R.id.bottomNav)
        bottomNav.setupWithNavController(navController)
        navController.addOnDestinationChangedListener{_,destination,_->
            when(destination.id){
                R.id.loginFragment->{
                    bottomNav.visibility = View.GONE
                }
                else->{
                    bottomNav.visibility = View.VISIBLE
                }
            }

        }
        requestPermissions()
    }

    private fun hasCameraPermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    private fun hasReadExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        val permissionToRequest = mutableListOf<String>()
        if (!hasCameraPermission()) {
            permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if(!hasReadExternalStoragePermission()){
            permissionToRequest.add(Manifest.permission.CAMERA)
        }
        if (permissionToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionToRequest.toTypedArray(), 200)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.isNotEmpty()) {
            for (i in grantResults.indices) {
                if (i == PackageManager.PERMISSION_GRANTED) {
                    Log.d("requestedPermission","${permissions[i]} granted")
                }
            }
        }
    }
}