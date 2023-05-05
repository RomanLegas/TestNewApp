package sportssisal.sportssisal

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.debug(text:String){
    Log.d("MyLog", text)
}
fun AppCompatActivity.saveString(name: String, res: String){
    val phoneDate = getSharedPreferences("USER", Context.MODE_PRIVATE)
    val editor = phoneDate?.edit()
    editor?.putString(name, res)
    editor?.apply()
}