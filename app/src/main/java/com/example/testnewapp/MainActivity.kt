package com.example.testnewapp


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.testnewapp.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {
    private var phoneDate: SharedPreferences? = null
    var remoteConfig = Firebase.remoteConfig
    lateinit var urlString: String

    var questionNumber by Delegates.notNull<Int>()
    var currentQuestion by Delegates.notNull<Int>()
    var ansverTriger by Delegates.notNull<Int>()
    var trueAnsver by Delegates.notNull<Int>()


    private lateinit var bindingClass: ActivityMainBinding

    var victorinaList = arrayListOf<VictorinaItem>()

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bindingClass = ActivityMainBinding.inflate(layoutInflater)

        setContentView(bindingClass.root)

        phoneDate = getSharedPreferences("USER", Context.MODE_PRIVATE)

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }

        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_default)

        getRemoteConfigData()

        vicrorinaCreation()

        urlString = phoneDate?.getString(Constanse.URL,"empty")!!
        if (urlString == ""){
            startVictorina()
        } else{
            var k = isOnline(this)
            if (k){
                bindingClass.webView.visibility=View.VISIBLE
                bindingClass.victorina.visibility = View.GONE
            }else{
                startVictorina()
            }
        }




        bindingClass.webView.settings.javaScriptEnabled = true
        bindingClass.webView.settings.setSupportZoom(true)
        bindingClass.webView.webViewClient = WebViewClient()
        bindingClass.webView.loadUrl(urlString)

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
            debug("следующий вопрос")
        }




    }

    override fun onBackPressed() {
        if (bindingClass.webView.canGoBack()){
            bindingClass.webView.goBack()
        }
    }
    fun startVictorina(){
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
        debug("создание вопроса")
        bindingClass.textViewQestion.isEnabled= false




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

    fun getRemoteConfigData(){
        remoteConfig.fetch(0).addOnCompleteListener {
            if (it.isComplete){
                Toast.makeText(this,"успешно",Toast.LENGTH_SHORT).show()
                debug("успешно")

                remoteConfig.fetchAndActivate()
            }else {
                debug("неудача")
                Toast.makeText(this, "неудача", Toast.LENGTH_SHORT).show()
            }
        }
        urlString = remoteConfig.getString("url")
        saveString(Constanse.URL, urlString)

        debug("urlString $urlString")
    }
    fun vicrorinaCreation(){
        victorinaList.add(VictorinaItem("How long is a marathon?",
            "42.19 kilometres (26.2 miles)",
            "43.77 kilometres (27.2 miles)",
            "40.56 kilometres (25.2 miles)"))

        victorinaList.add(VictorinaItem("How many players are there on a baseball team?",
            "9 players",
            "10 players",
            "11 players"))

        victorinaList.add(VictorinaItem("Which country won the World Cup 2018?",
            "France",
            "Germany",
            "Brazil"))

        victorinaList.add(VictorinaItem("What sport is considered the “king of sports?",
            "Football",
            "Volleyball",
            "Hockey"))

        victorinaList.add(VictorinaItem("What are the two national sports of Canada?",
            "Lacrosse and ice hockey",
            "Lacrosse and volleyball",
            "Football and ice hockey"))
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

}