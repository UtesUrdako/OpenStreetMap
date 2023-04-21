package com.utesurdako.openstreetmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.LruCache
import com.utesurdako.openstreetmap.Data.MapField
import com.utesurdako.openstreetmap.Data.MapTile
import com.utesurdako.openstreetmap.Utils.MapTileCache
import com.utesurdako.openstreetmap.databinding.ActivityMainBinding
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var pictureModel: Array<Array<Bitmap?>>

    lateinit var lru: LruCache<String, Bitmap>
    val client = OkHttpClient()

    private val sizeField: Int = 10
    private val sizeBuffer: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.customView.mapField = MapField(sizeField, sizeField)
        pictureModel = Array(sizeField){ Array(sizeField) { null } }

        lru = LruCache(1024)

        getTileMap()
    }

    fun getTileMap() {
        doWork()
    }

    fun doWork(zoom: Int = 19, x: Int = 325955, y: Int = 164817) {

        val executor = Executors.newFixedThreadPool(sizeField * sizeField)

        for (xOffset in 0 until sizeField) {
            for (yOffset in 0 until sizeField) {
                // Image URL
                val imageURL = "https://tile.openstreetmap.org/$zoom/${x + xOffset}/${y + yOffset}.png"
                val worker = Runnable {
                    val _x = xOffset
                    val _y = yOffset
                    val request = Request.Builder()
                        .url(imageURL)
                        .header("user-agent", "Chrome/110.0.0.0")
                        .build()

                    client.newCall(request).execute().use {
                        val result = it.body?.byteStream()
                        lru.put("${_x}x${_y}", BitmapFactory.decodeStream(result))
                        //pictureModel[_x][_y] = BitmapFactory.decodeStream(result)
                    }
                }
                executor.execute(worker)
            }
        }
        executor.shutdown()
        while (!executor.isTerminated) {}

        for (xOffset in 0 until sizeField) {
            for (yOffset in 0 until sizeField) {
                //val mapTile = MapTile(x, y, pictureModel[xOffset][yOffset])
                val mapTile = MapTile(x, y, lru.get("${xOffset}x${yOffset}"))
                binding.customView.mapField?.setTile(yOffset, xOffset, mapTile)
            }
        }
    }

    private fun cachingNewTiles(x: Int = 325955, y: Int = 164817)
    {

    }
}
