package mx.tecnm.ladm_u4_proyectoalumno_18401083.Sockets

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Message
import mx.tecnm.ladm_u4_proyectoalumno_18401083.Util.BluetoothStateCustom
import mx.tecnm.ladm_u4_proyectoalumno_18401083.interfaces.OnHandlerMsg
import mx.tecnm.ladm_u4_proyectoalumno_18401083.interfaces.OnSocketReceive
import java.io.IOException
import java.util.*

class BClientSocket(device: BluetoothDevice, uuid: UUID, onHandlerM: OnHandlerMsg, onReceive: OnSocketReceive):Thread() {
    private lateinit var bluetoothSocket: BluetoothSocket
    private var onHandlerMsg = onHandlerM
    private var onSocketReceive = onReceive

    init {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    override fun run() {
        try {
            bluetoothSocket.connect()
            onSocketReceive.onReceive(bluetoothSocket)
            val msg = Message.obtain()
            msg.what = BluetoothStateCustom.STATE_CONNECTED.state
            onHandlerMsg.onMsgGet(msg)
        }catch (e:IOException){
            e.printStackTrace()
            val msg = Message.obtain()
            msg.what = BluetoothStateCustom.STATE_CONNECTION_FAILED.state
            onHandlerMsg.onMsgGet(msg)
        }
    }

}