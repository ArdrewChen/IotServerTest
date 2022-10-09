package com.example.iotserver

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    var client: String? = ""
    var msg: String? = ""
    private val serverSocket = ServerSocket(2333)


    private var socket: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button3: Button? = findViewById(R.id.button3)
        val button2: Button? = findViewById(R.id.button2)

        button3?.setOnClickListener {
            thread {
                startServer()
            }
            Thread.sleep(300)
        }
        button2?.setOnClickListener {
            thread {
                sendMessage()
            }
        }
    }

    @SuppressLint("WrongViewCast")
    fun startServer() {
        val text: TextView? = findViewById(R.id.textView)
        try {
            while (true) {
                socket = serverSocket.accept() //等待客户端连接
                text?.text = "服务器打开"
                client = "得到客户端连接：$socket"
                println(client)
                text?.text = client
                receiveMessage()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun receiveMessage() {//接收客户端消息
        try {
            val reader = InputStreamReader(socket?.getInputStream())

            val chars = CharArray(1024)
            var len: Int
            var strClient = ""
            while (reader.read(chars).also { len = it } != -1) {
                strClient = String(chars, 0, len)
                println("Receive from client message=: $strClient")
            }
            socket?.shutdownInput()
            //读取图片代码，转换成字节，后续存储未写，存储类比客户端存储
//            val inputStream = DataInputStream(socket?.getInputStream())
//            //同样是先读长度
//            val image: ImageView = findViewById(R.id.imageView)
//            val len = inputStream.readLong()
//            println("len = $len")
//            val bytes = ByteArray(len.toInt())
//            //然后在读这个长度的字节到字节数组
//            val bitmap: Bitmap? = BitmapFactory
//                .decodeByteArray(bytes, 0, bytes.size)
//
//            // 将图片显示到ImageView上
//            // 此处由于view中的组件都是线程不安全的,使用android提供的这个办法处理（详见下文“附2”）
//            bitmap?.let { saveImageToGallery(it) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun sendMessage() {
        val outputStream = socket?.getOutputStream()
        val data = "wjiaohehao" + "\n"
        outputStream?.write(data.toByteArray(charset("utf-8")))
    }

    fun saveImageToGallery(bitmap: Bitmap) {//存储图片
        // 先将图片保存到文件
        val imageDir = File(Environment.getExternalStorageDirectory(), "testGallery")
        if (!imageDir.exists()) {
            imageDir.mkdir()
        }
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val file = File(imageDir, fileName)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 再通知图库更新数据库
        val uri = Uri.fromFile(file)
        sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
    }
}