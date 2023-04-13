package com.sandeepprabhakula.leafdiseasedetection.fragments

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.sandeepprabhakula.leafdiseasedetection.databinding.ActivityMainBinding
import com.sandeepprabhakula.leafdiseasedetection.databinding.FragmentDetectionOfDiseaseBinding
import com.sandeepprabhakula.leafdiseasedetection.ml.model
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DetectionOfDisease : Fragment() {
    private val REQUEST_IMAGE_CAPTURE = 1
    private var _binding: FragmentDetectionOfDiseaseBinding? = null
    private val binding get() = _binding!!
    private val imageSize = 224
    var curFile: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetectionOfDiseaseBinding.inflate(layoutInflater, container, false)
        binding.cameraIntent.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
        binding.filesIntent.setOnClickListener {
            getContent.launch("image/*")
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as Bitmap
            binding.testImage.setImageBitmap(imageBitmap)
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, false)
            classifyImage(imageBitmap)
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            curFile = uri
            binding.testImage.setImageURI(curFile)
            var imageBitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, curFile)
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, imageSize, imageSize, false)
            classifyImage(imageBitmap)
        }

    private fun classifyImage(image: Bitmap?) {
        val model = model.newInstance(requireContext())
        val inputFeature0 =
            TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
        val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(imageSize * imageSize)
        image?.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
        var pixel = 0

        for (i in 0 until imageSize) {
            for (j in 0 until imageSize) {
                val tem = intValues[pixel++]
                byteBuffer.putFloat((tem shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((tem shr 8 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((tem and 0xFF) * (1f / 255))
            }
        }
        inputFeature0.loadBuffer(byteBuffer)
        val outputs: model.Outputs = model.process(inputFeature0)
        val outputFeature0: TensorBuffer = outputs.outputFeature0AsTensorBuffer

        val confidences = outputFeature0.floatArray
        var maxPos = 0
        var maxConfidence = 0f
        for (i in confidences.indices) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i]
                maxPos = i
            }
        }
        binding.accuracyResult.text = maxConfidence.toString()
        val classes = arrayOf(
            "Apple - Scab",
            "Apple - Black Rot",
            "Apple -Cedar Apple Rust",
            "Apple -Healthy",
            "Cherry -Healthy",
            "Cherry -Powdery Mildew",
            "Corn -Cercospora Leaf Spot Gray",
            "Corn -Common Rust",
            "Corn -Healthy",
            "Corn -North Leaf Blight",
            "Grape -Black Rot",
            "Grape -Black Measles",
            "Grape -Leaf Blight",
            "Grape -Healthy",
            "Orange -Citrus Greening",
            "Peach -Bacterial Spot",
            "Peach -Healthy",
            "Pepper -Bacterial Spot",
            "Pepper -Healthy",
            "Potato -Early Blight",
            "Potato -Healthy",
            "Potato -Late Blight",
            "Squash -Powdery Mildew",
            "Strawberry - Healthy",
            "Strawberry - Leaf Scorch",
            "Tomato - Bacterial Spot",
            "Tomato - Early Blight",
            "Tomato - Healthy",
            "Tomato - Late Blight",
            "Tomato - Leaf Mold",
            "Tomato - Septoria leaf spot",
            "Tomato - Spider mites",
            "Tomato - Target Spot",
            "Tomato - Mosaic virus",
            "Tomato - Yellow Leaf curl virus"

        )
        binding.diseaseResult.text = classes[maxPos]
        model.close()
    }
}