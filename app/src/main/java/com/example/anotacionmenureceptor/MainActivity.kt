package com.example.anotacionmenureceptor


import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var txt_id_usuario: TextView
    private lateinit var txt_registrados: TextView
    private lateinit var txt_confirmados: TextView
    private lateinit var txt_id_menu: TextView

    private var horaDesdeConsulta = 0;
    private var horaHastaConsulta = 0;

    private val ruta = "http://api.yemita.com.py/"
    var token =
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRvcyI6W3siY29kX3VzdWFyaW8iOjEwMTYsInVzdWFyaW8iOiJodmVsYXpxdWV6IiwicGFzc3dvcmQiOiIxMjM0NSIsImNsYXNpZmljYWRvcmEiOiJCIiwicm9sIjoiVSIsIm5vbWJyZSI6IkhFUk5BTiBWRUxBWlFVRVoiLCJpZF9yb2wiOjE2LCJpZF9lc3RhZG8iOjEsImNsYXZlIjoiODI3Y2NiMGVlYThhNzA2YzRjMzRhMTY4OTFmODRlN2IifV0sImlhdCI6MTY3ODkwNjE1NX0.ww2JnJvUlyPdpMbzuOr71KKAYEPuCDAMO_UTeBkd5L8"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
        getConfirmados()
        ejecutarCorrutina()

        txt_id_usuario.setOnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> postRegistroConfirmacion()
                }
            }
            false
        }
    }

    private fun ejecutarCorrutina() = CoroutineScope(Dispatchers.IO).launch {
        while (true) {
            delay(60000) // delay for 60000= 1 MINUTOS
            println("La función se ejecuta cada  1 MINUTOS")
            consultaTiempoEjecucion()
        }
    }

    private fun consultaTiempoEjecucion() {
        getHorasValidacion()
        val dateFormat: DateFormat = SimpleDateFormat("HH")
        val date = Date()
        if (dateFormat.format(date).toInt() in (horaDesdeConsulta..horaHastaConsulta)) {
            getConfirmados()
        }
    }

    private fun getHorasValidacion() {
        // on below line we are creating a variable for url
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request = object : JsonObjectRequest(
            Request.Method.GET,
            ruta + "validacionConsultaBdMenu",
            null,
            { response ->
                // this method is called when we get successful response from API.
                try {
                    horaDesdeConsulta = response.getInt("desde")
                    horaHastaConsulta = response.getInt("hasta")
                    Log.i("Valores", "Desde $horaDesdeConsulta , Hasta  $horaHastaConsulta")
                } catch (e: Exception) {
                }
            },
            { error ->

            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // set the Authorization header with the bearer token
                headers["Authorization"] = token
                return headers
            }
        }
        queue.add(request)
    }

    private fun getConfirmados() {
        // on below line we are creating a variable for url
        val url = ruta + "menudiarioPersonasConfirmadas"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request = object : JsonArrayRequest(Request.Method.GET, url, null, { response ->
            // this method is called when we get successful response from API.
            try {
                txt_registrados.text = ""
                txt_confirmados.text = ""
                for (i in 0 until response.length()) {
                    val respObj = response.getJSONObject(i)
                    txt_registrados.text = respObj.getString("registrados")
                    txt_confirmados.text = respObj.getString("confirmados")
                    Log.i("mensaje:" ,respObj.getString("registrados"))
                }
             } catch (e: Exception) {

            }
        }, { error ->

        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // set the Authorization header with the bearer token
                headers["Authorization"] = token
                return headers
            }
        }

        // at last we are adding our
        // request to our queue.
        queue.add(request)
    }

    private fun initComponents() {
        txt_id_usuario = findViewById(R.id.txt_id_usuario)
        txt_registrados = findViewById(R.id.txt_registrados)
        txt_id_menu = findViewById(R.id.txt_id_menu)
        txt_confirmados = findViewById(R.id.txt_confirmados)
    }

    private fun postRegistroConfirmacion() {
        val alertLoading = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        alertLoading.progressHelper.barColor = Color.parseColor("#A5DC86")
        alertLoading.titleText = "Confirmando, espere..."
        alertLoading.setCancelable(false)
        alertLoading.show()


        val queue = Volley.newRequestQueue(this)
        val url = ruta + "menuRegistroConfirmado"
        val jsonObject = JSONObject()
        jsonObject.put(
            "id_usuario", txt_id_usuario.text.toString()
        ) // Agrega los parámetros que deseas enviar

        val request = object : JsonObjectRequest(Request.Method.POST, url, jsonObject, { response ->
            val mensaje =
                response.getString("mensaje") // Obtener el campo "mensaje" del JSON de respuesta
            var tipoAlerta = 1
            var reproductor = R.raw.intente

            when (response.getInt("tipo_mensaje")) {
                10, 20,30 -> tipoAlerta = SweetAlertDialog.ERROR_TYPE;
                40,50 -> tipoAlerta = SweetAlertDialog.SUCCESS_TYPE
                else -> tipoAlerta = SweetAlertDialog.ERROR_TYPE
            }

            reproductor = when (response.getInt("tipo_mensaje")) {
                20 -> R.raw.alert_sound
                30 -> R.raw.intente
                40,50 -> R.raw.confirmado
                else -> R.raw.intente
            }
            alertLoading.dismissWithAnimation()

            val dialog = SweetAlertDialog(this@MainActivity, tipoAlerta)
            dialog.titleText = mensaje
            dialog.setCancelable(true)
            dialog.show()

            if(response.getInt("tipo_mensaje")==20){
                val mp2 = MediaPlayer.create(this, R.raw.voz)
                mp2.start()
                Thread.sleep((2 * 1000).toLong())
            }
            val mp = MediaPlayer.create(this, reproductor)
            mp.start()


            txt_id_usuario.text = ""
            txt_id_usuario.requestFocus()
            getConfirmados()

            Handler().postDelayed({
                dialog.dismissWithAnimation()
            }, 2000)
        }, { error ->
            alertLoading.dismissWithAnimation()

            Log.i("mensaje", error.toString())
            txt_id_usuario.text = ""
            txt_id_usuario.requestFocus()
            val mp = MediaPlayer.create(this,  R.raw.intente)
            mp.start()


        }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // set the Authorization header with the bearer token
                headers["Authorization"] = token
                return headers
            }
        }
        queue.add(request)

    }
}