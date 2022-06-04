package mx.tecnm.ladm_u4_proyectoalumno_18401083

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import mx.tecnm.ladm_u4_proyectoalumno_18401083.Sockets.BClientSocket
import mx.tecnm.ladm_u4_proyectoalumno_18401083.Util.BluetoothStateCustom
import mx.tecnm.ladm_u4_proyectoalumno_18401083.databinding.ActivityEnvioAsistenciaBinding
import mx.tecnm.ladm_u4_proyectoalumno_18401083.interfaces.OnHandlerMsg
import mx.tecnm.ladm_u4_proyectoalumno_18401083.interfaces.OnSocketReceive
import java.io.*
import java.lang.Exception
import java.util.*
private const val REQUEST_CODE_ENABLE_BLUETOOTH = 1002
class EnvioAsistencia : AppCompatActivity(){
    lateinit var binding: ActivityEnvioAsistenciaBinding
    var noControl = ""
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevices: MutableList<BluetoothDevice>
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private lateinit var listDevicesNamed:MutableList<String>

    private var isFileTransferFlag = false

    private val uuid:UUID = UUID.fromString("213154a9-aa1c-4ffb-b53a-ae02bd2b079a")

    private var sendReceiveMsg: SendReceiveMsg?=null


    private val REQUEST_ENABLE_BLUETOOTH = 111
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnvioAsistenciaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        noControl = this.intent.extras!!.getString("No.Control")!!
        binding.lblNoControl.setText("Hola de nuevo: ${noControl}")


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        listDevicesNamed = mutableListOf()
        bluetoothDevices = mutableListOf()

        arrayAdapter = ArrayAdapter(applicationContext,android.R.layout.simple_list_item_1,listDevicesNamed)
        binding.listView.adapter = arrayAdapter

        if (!bluetoothAdapter.isEnabled){
            startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),REQUEST_ENABLE_BLUETOOTH)
        }

        binding.apply {


            btnListDevices.setOnClickListener {
                showDevices()
            }

            btnSend.setOnClickListener {
                val cal = GregorianCalendar.getInstance()
                var tiempo = ""

                if (cal.get(Calendar.AM_PM).equals(1)) tiempo = "PM"
                else if (cal.get(Calendar.AM_PM).equals(0)) tiempo = "AM"
                val hora = cal.get(Calendar.HOUR)
                val msg:String = noControl + "\n" +"${hora} ${tiempo} a ${hora+1} ${tiempo}"
                Toast.makeText(this@EnvioAsistencia,"Se ha enviado \n${msg}",Toast.LENGTH_LONG).show() //edMessage.text.toString()
                sendReceiveMsg?.writeBegin(1)
                sendReceiveMsg?.write(msg.toByteArray())
             }

            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                val bClientSocket = BClientSocket(bluetoothDevices[position],uuid,object:
                    OnHandlerMsg {
                    override fun onMsgGet(msg: Message) {
                        handler.sendMessage(msg)
                    }
                }, object: OnSocketReceive {
                    override fun onReceive(blueSocket: BluetoothSocket) {
                        sendReceiveMsg = SendReceiveMsg(blueSocket)
                        sendReceiveMsg!!.start()
                    }
                })
                bClientSocket.start()
                tvStatus.text = "Conectando..."
                binding.tvStatus.setTextColor(Color.rgb(38, 77, 135))
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==REQUEST_ENABLE_BLUETOOTH){
            if (resultCode== RESULT_OK){
                Toast.makeText(applicationContext,"Bluetooth is enabled",Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(applicationContext,"Bluetooth is cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDevices() {
        listDevicesNamed.clear()
        bluetoothDevices.clear()
        val listDevices:Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        listDevices.forEach {
            listDevicesNamed.add(it.name)
            bluetoothDevices.add(it)
        }
        arrayAdapter.notifyDataSetChanged()
    }

    private var handler:Handler = Handler { msg ->
        when (msg.what) {
            BluetoothStateCustom.STATE_LISTENING.state -> {
                binding.tvStatus.text = "Status: Escuchando"
            }
            BluetoothStateCustom.STATE_CONNECTED.state -> {
                binding.tvStatus.text = "Status: Conectado!"
                binding.tvStatus.setTextColor(Color.rgb(38, 135, 64))
            }
            BluetoothStateCustom.STATE_CONNECTING.state -> {
                binding.tvStatus.text = "Status: Conectando..."
                binding.tvStatus.setTextColor(Color.rgb(38, 77, 135))
            }
            BluetoothStateCustom.STATE_CONNECTION_FAILED.state -> {
                binding.tvStatus.text = "Status: Falló la conexión :("
                binding.tvStatus.setTextColor(Color.rgb(158, 30, 2))
            }
        }
        true
    }

    inner class SendReceiveMsg(var bluetoothSocket: BluetoothSocket):Thread(){
        private var inputStream: DataInputStream?
        private var outputStream: DataOutputStream?

        init {
            var tempIS:DataInputStream?=null
            var tempOS:DataOutputStream?=null
            try {
                tempIS = DataInputStream(bluetoothSocket.inputStream)
                tempOS = DataOutputStream(bluetoothSocket.outputStream)
            }catch (e:IOException){
                e.printStackTrace()
            }
            inputStream = tempIS
            outputStream = tempOS
        }

        override fun run() {
            var l = 0;
            if (!bluetoothSocket.isConnected)
                bluetoothSocket.close()
            else {
                inputStream?.let {
                    l = it.readInt()
                }
                if (l == 1) {
                    val buffer = ByteArray(1024)
                    var bytes = 0
                    inputStream?.let { iS ->
                        while (true) {
                            //-1 = because not used param arg2
                            try {
                                bytes = iS.read(buffer)
                                handler.obtainMessage(
                                    BluetoothStateCustom.STATE_MESSAGE_RECEIVED.state,
                                    bytes,
                                    -1,
                                    buffer
                                ).sendToTarget()
                            } catch (e: Exception) {
                                break
                            }
                        }

                    }
                }
            }
        }

        fun write(buffer:ByteArray){
            try {
                if (!isFileTransferFlag){
                    outputStream?.write(buffer)
                }
            }catch (e:IOException){
                e.printStackTrace()
            }
        }

        fun writeBegin(v: Int){
            try {
                outputStream?.writeInt(v)
                outputStream?.flush()
            }catch (e:IOException){
                e.printStackTrace()
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuopciones,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.opAcerca->{

            }
            R.id.opSalir->{
                finish()
            }
            R.id.opCerrarSesion->{
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }

        }
        return true
    }

}