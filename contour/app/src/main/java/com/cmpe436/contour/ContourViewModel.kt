package com.cmpe436.contour

import android.graphics.Rect
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.String



class ContourViewModel : ViewModel() {
    var _shapes = HashMap<Int, RRect>()
    var shapes = MutableLiveData<MutableMap<Int, RRect>>()
    var selectedShape = MutableLiveData<RRect?>()
    var socket: Socket? = null
    var dataInputStream: DataInputStream? = null
    var dataOutputStream: DataOutputStream? = null


    init {
        startConnection()
        selectedShape.observeForever {
            if (it != null) {
               modifyShape()
            }
        }
    }

    private fun startConnection() {
        viewModelScope.launch {
            Thread{
                try{
                    socket = Socket("35.223.204.211", 1616)
                    dataInputStream = DataInputStream(socket!!.getInputStream())
                    dataOutputStream = DataOutputStream(socket!!.getOutputStream())
                    listenSocket()
                }
                catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun listenSocket() {
        try {
            while (true) {
                try {
                    val data = dataInputStream!!.readUTF()

                    var payload = data.split("|")
                    Log.d("response", payload[0])
                    when (payload[0]) {
                        "init" -> {
                            var shapesRaw = payload[1].split("#")
                            for (shapeRaw in shapesRaw) {
                                var shape = shapeRaw.split("/")
                                var re = RRect(Rect(shape[1].toInt(), shape[2].toInt(), shape[3].toInt(), shape[4].toInt()), shape[5].toFloat(), id = shape[0].toInt())
                                _shapes[shape[0].toInt()] = re
                                shapes.postValue(_shapes)
                            }
                        }
                        "createShape" -> {
                            var re = RRect(Rect(payload[2].toInt(), payload[3].toInt(), payload[4].toInt(), payload[5].toInt()), payload[6].toFloat(), id = payload[1].toInt())
                            Log.d("response", re.toString())
                            _shapes[payload[1].toInt()] = re
                            shapes.postValue(_shapes)
                        }
                        "deleteShape" -> {
                            _shapes.clear()
                            shapes.postValue(_shapes)
                            selectedShape.postValue(null)

                        }
                        "modifyShape" -> {
                            var re = RRect(Rect(payload[2].toInt(), payload[3].toInt(), payload[4].toInt(), payload[5].toInt()), payload[6].toFloat(), id = payload[1].toInt())
                            _shapes[payload[1].toInt()] = re
                            shapes.postValue(_shapes)

                        }
                        "acquireShape" -> {
                            if(payload[1] == "true") {
                                selectedShape.postValue(_shapes[payload[2].toInt()])

                            }
                            else {
                                selectedShape.postValue(null)
                            }
                        }
                        "releaseShape" -> {
                            selectedShape.postValue(null)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createShape() {
        Log.d("createShape", "createShape")
        viewModelScope.launch {
            Thread{
                try {
                    selectedShape.value?.let {
                        Log.d("createShape", "createShape: $it")
                        synchronized(dataOutputStream!!) {
                        dataOutputStream!!.writeUTF("createShape|${it.id}|${it.rect.left}|${it.rect.top}|${it.rect.right}|${it.rect.bottom}|${it.rotation}\n")
                    }

                    }
                } catch (e: Exception) {
                    Log.d("createShape", "createShape: $e")
                    e.printStackTrace()
                }
            }.start()
        }
    }
    fun modifyShape() {
        viewModelScope.launch {
            Thread{
                try {
                    selectedShape.value?.let {
                        synchronized(dataOutputStream!!) {
                            dataOutputStream!!.writeUTF("modifyShape|${it.id}|${it.rect.left}|${it.rect.top}|${it.rect.right}|${it.rect.bottom}|${it.rotation}\n")
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    fun deleteShape() {
        viewModelScope.launch {
            Thread{
                try {
                    synchronized(dataOutputStream!!) {
                            dataOutputStream!!.writeUTF("deleteShape|2\n")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    fun acquireShape(id: Int) {
        viewModelScope.launch {
            Thread{
                try {
                    synchronized(dataOutputStream!!) {
                        dataOutputStream!!.writeUTF("acquireShape|$id\n")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    fun releaseShape() {
        viewModelScope.launch {
            Thread{
                try {
                    selectedShape.value?.let {
                        synchronized(dataOutputStream!!) {
                            dataOutputStream!!.writeUTF("releaseShape|${it.id}\n")
                            selectedShape.postValue(null)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }.start()
        }
    }


}
