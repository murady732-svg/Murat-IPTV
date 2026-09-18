package com.murad.iptv

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : Activity() {

    private lateinit var channelList: LinearLayout
    private lateinit var videoView: VideoView
    private lateinit var statusText: TextView

    private val playlistUrl =
        "https://iptv-org.github.io/iptv/countries/tr.m3u"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)

        val title = TextView(this)
        title.text = "🇹🇷 Murat IPTV"
        title.textSize = 24f
        title.setTextColor(Color.WHITE)
        title.setPadding(20, 20, 20, 20)

        root.addView(
            title,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        videoView = VideoView(this)

        root.addView(
            videoView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                500
            )
        )

        statusText = TextView(this)
        statusText.text = "Kanallar yükleniyor..."
        statusText.textSize = 18f
        statusText.setTextColor(Color.WHITE)
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(10, 15, 10, 15)

        root.addView(
            statusText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        channelList = LinearLayout(this)
        channelList.orientation = LinearLayout.VERTICAL

        val scrollView = ScrollView(this)
        scrollView.addView(channelList)

        root.addView(
            scrollView,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)

        loadChannels()
    }

    private fun loadChannels() {
        thread {
            try {
                val text = URL(playlistUrl).readText()

                val lines = text.lines()
                val channels = mutableListOf<Pair<String, String>>()

                var channelName = ""

                for (line in lines) {
                    val trimmed = line.trim()

                    if (trimmed.startsWith("#EXTINF")) {
                        channelName =
                            trimmed.substringAfterLast(",").trim()
                    }

                    if (
                        trimmed.startsWith("http://") ||
                        trimmed.startsWith("https://")
                    ) {
                        if (channelName.isNotEmpty()) {
                            channels.add(
                                Pair(channelName, trimmed)
                            )
                            channelName = ""
                        }
                    }
                }

                runOnUiThread {
                    statusText.text =
                        "${channels.size} kanal bulundu"

                    channelList.removeAllViews()

                    for (channel in channels) {
                        addChannelButton(
                            channel.first,
                            channel.second
                        )
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text =
                        "Kanallar yüklenemedi: ${e.message}"
                }
            }
        }
    }

    private fun addChannelButton(
        name: String,
        streamUrl: String
    ) {
        val button = Button(this)

        button.text = name
        button.textSize = 16f
        button.setTextColor(Color.WHITE)
        button.setBackgroundColor(Color.DKGRAY)

        button.setOnClickListener {
            statusText.text = "Oynatılıyor: $name"

            try {
                videoView.setVideoPath(streamUrl)
                videoView.start()
            } catch (e: Exception) {
                statusText.text =
                    "Bu kanal oynatılamadı."
            }
        }

        channelList.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }
}
