package io.agora.android_cloud_recording_demo

import android.Manifest
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas
import io.agora.rtc.video.VideoEncoderConfiguration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private var mRtcEngine : RtcEngine? = null

    val baseUrl: String = "" // Enter the link to your token server
    var recording: Boolean = false
    var uid = 0
    var tokenValue: String? = null
    var rid: String? = null
    var sid: String? = null
    var recUid = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermission()
        initAgoraEngineAndJoinChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO), 22)
    }

    fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupVideoProfile()
        setupLocalVideo()
        val job = GlobalScope.launch(Dispatchers.Main){
            getToken()
            delay(2000)
        }

        job.invokeOnCompletion { joinChannel() }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }
    }

    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, appId, mRtcEventHandler)
            println("\nmRtcEngine initialized")
        } catch (e: Exception) {
            println("Exception while initializing AgoraRtcEngine: $e")
        }
    }

    private fun setupVideoProfile() {
        mRtcEngine!!.enableVideo()

        mRtcEngine!!.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
    }

    private fun setupLocalVideo() {
        val container = findViewById<View>(R.id.local_video_view_container) as FrameLayout
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {
        mRtcEngine!!.joinChannel(
                tokenValue,
                channelName,
                null,
                uid
        )
    }

    private fun setupRemoteVideo(uid: Int) {
        val container =
            findViewById<View>(R.id.remote_video_view_container) as FrameLayout
        if (container.childCount >= 1) {
            return
        }
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
        surfaceView.tag = uid
    }

    private fun leaveChannel() {
        mRtcEngine!!.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        val container =
            findViewById<View>(R.id.remote_video_view_container) as FrameLayout
        container.removeAllViews()
    }


    suspend fun getToken() {
            val client = OkHttpClient()
            val url = "$baseUrl/api/get/rtc/$channelName"
            val request = Request.Builder().url(url).build()
            client.newCall(request).enqueue(object : Callback{
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) throw IOException("Unexpected code $response")
                        val body = response.body?.string()
                        val obj = JSONObject(body)
                        tokenValue = obj.getString("rtc_token")
                        uid = obj.getInt("uid")
                        println("\n Token Value = $tokenValue")
                    }
                }
            })
    }

    private fun startRecording(){
        val client = OkHttpClient().newBuilder().build()
        val body = JSONObject()
        try {
            body.put("channel", channelName)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
                .method("POST", body.toString().toRequestBody(mediaType))
                .url("$baseUrl/api/start/call")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                val jobj = JSONObject(responseBody)
                val data = jobj.getJSONObject("data")
                rid = data.getString("rid")
                sid = data.getString("sid")
                recUid = data.getInt("uid")
            }
        })
    }

    private fun stopRecording(){
        val client = OkHttpClient().newBuilder().build()

        val body = JSONObject()
        try {
            body.put("channel", channelName)
            body.put("rid", rid)
            body.put("sid", sid)
            body.put("uid", recUid)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
                .method("POST", body.toString().toRequestBody(mediaType))
                .url("$baseUrl/api/stop/call")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code == 200){
                    println("Call recording stopped")
                }
            }
        })
    }

    private fun startOrStopRecording(isRecording: Boolean){
        if (isRecording){
            startRecording()
        }else{
            stopRecording()
        }
    }

    fun onLocalAudioMuteClicked(view: View?) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(resources.getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        // Stops/Resumes sending the local audio stream.
        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }

    fun onLocalVideoMuteClicked(view: View){
        val iv = view as ImageView
        iv.setOnClickListener {
            recording = !recording
            if (recording){
                iv.setColorFilter(resources.getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY)
            } else{
                iv.clearColorFilter()
            }
            startOrStopRecording(recording)
        }
    }

    fun onSwitchCameraClicked(view: View?) {
        mRtcEngine!!.switchCamera()
    }

    fun onEncCallClicked(view: View?) {
        finish()
    }
}