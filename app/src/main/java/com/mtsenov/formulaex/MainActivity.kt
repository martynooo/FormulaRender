package com.mtsenov.formulaex

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doAfterTextChanged
import com.bumptech.glide.Glide
import com.mtsenov.formulaex.databinding.ActivityMainBinding
import com.mtsenov.formulaex.model.CheckFormulaResponse
import com.mtsenov.formulaex.utility.UtilityChecks
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.inputEditText.doAfterTextChanged {
            binding.textView.visibility = View.INVISIBLE
        }

        binding.btnConvert.setOnClickListener{
            if (UtilityChecks.isNetworkAvailable(this))
                    if (inputValid()) {
                        convertFormula(binding.inputEditText.text.toString())
                        binding.textView.visibility = View.INVISIBLE
                    }
                    else {
                        binding.inputEditText.setText("")
                        binding.textView.visibility = View.VISIBLE
                    }
            else
                Toast.makeText(applicationContext, "No internet connection :(", Toast.LENGTH_SHORT)
                    .show()
        }

        binding.formulaImage.setOnLongClickListener {
            shareImage(binding.formulaImage)
        }
    }

    private fun inputValid(): Boolean {
        //I was trying to come up with a more sophisticated regex, however just checking for empty input
        val pattern: Pattern = Pattern.compile("^\$")
        val matcher: Matcher = pattern.matcher(binding.inputEditText.text.toString())
        return !matcher.matches()
    }

    private fun convertFormula(query: String) {
        val service = APIClient.client?.create<APIInterface>()
        val checkQuery = CheckFormulaResponse(query)
        val call: Call<CheckFormulaResponse>? = service?.checkFormula(checkQuery)
        Log.e("formulaex", "check formula with query --> $query")

        call?.enqueue(object : Callback<CheckFormulaResponse?> {
            override fun onResponse(
                call: Call<CheckFormulaResponse?>?,
                response: Response<CheckFormulaResponse?>
            ) {
                val checkFormulaResponse: CheckFormulaResponse? = response.body()
                if (checkFormulaResponse != null) {
                    Log.e("formulaex", "checked --> " + checkFormulaResponse.checked.toString())
                    Log.e("formulaex", "Resource location --> " + response.headers()["x-resource-location"])
                }
                response.headers()["x-resource-location"]?.let { renderImage(it) }
            }

            override fun onFailure(
                call: Call<CheckFormulaResponse?>,
                t: Throwable?
            ) {
                Toast.makeText(applicationContext, "onFailure called ", Toast.LENGTH_SHORT)
                    .show()
                call.cancel()
            }
        })
    }

    private fun renderImage(hash: String) {
        Log.e("formulaex", "Rendering formula from --> https://en.wikipedia.org/api/rest_v1/media/math/render/png/$hash ")

        //Could be SVG(which is always better) but it needs more libraries and is time consuming,
        // so using PNG for this exercise
        Glide.with(this)
            .load("https://en.wikipedia.org/api/rest_v1/media/math/render/png/$hash")
            .error(Glide.with(binding.formulaImage).load(R.drawable.ic_launcher_foreground))
            .into(binding.formulaImage)
    }

    private fun shareImage(image: ImageView): Boolean {

        val bitmapDrawable: Drawable = image.drawable
        val bitmap: Bitmap = bitmapDrawable.toBitmap()

        val imageFile = File(cacheDir, "${UUID.randomUUID()}.png")
        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("formulaex", "Error writing bitmap", e)
        }
        val imageUri = FileProvider.getUriForFile(
            this,
            BuildConfig.APPLICATION_ID,
            imageFile
        )

        //Image may be black if you are using dark mode (transparent background and black letters...) :/
        val share = Intent(Intent.ACTION_SEND)
        share.type = "image/png"
        share.putExtra(Intent.EXTRA_STREAM, imageUri)
        startActivity(Intent.createChooser(share, getText(R.string.send_to)))

        return true
    }


}