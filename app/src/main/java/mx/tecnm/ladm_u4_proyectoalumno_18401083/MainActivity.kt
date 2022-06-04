package mx.tecnm.ladm_u4_proyectoalumno_18401083

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import mx.tecnm.ladm_u4_proyectoalumno_18401083.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (FirebaseAuth.getInstance().currentUser!=null){
            //Sesion activa
            iniciarVentana()
        }
        binding.btnRegistrarse.setOnClickListener {
            val autenticacion = FirebaseAuth.getInstance()
            val dialogo = ProgressDialog(this)
            dialogo.setMessage("Registrando...")
            dialogo.setCancelable(false)
            dialogo.show()
            autenticacion.createUserWithEmailAndPassword(
                binding.txtNoControl.text.toString()+"@gmail.com",
                binding.txtNoControl.text.toString()
            )
                .addOnCompleteListener {
                    dialogo.dismiss()
                    if (it.isSuccessful){
                        Toast.makeText(this,"Registrado con éxito",Toast.LENGTH_LONG).show()
                        autenticacion.signOut()
                    }
                    else{
                        AlertDialog.Builder(this)
                            .setMessage("Error: No se pudo registrar.")
                            .show()
                    }
                }
            binding.txtNoControl.text.clear()
        }
        binding.btnEntrar.setOnClickListener {
            val autenticacion = FirebaseAuth.getInstance()
            val dialogo = ProgressDialog(this)
            dialogo.setMessage("Verificando número de control")
            dialogo.setCancelable(false)
            dialogo.show()
            autenticacion.signInWithEmailAndPassword(
                binding.txtNoControl.text.toString()+"@gmail.com",
                binding.txtNoControl.text.toString()
            ).addOnCompleteListener {
                dialogo.dismiss()
                if (it.isSuccessful){
                    iniciarVentana()
                    return@addOnCompleteListener
                }
                AlertDialog.Builder(this)
                    .setMessage("Error: No se encontró el No. Control")
                    .show()
            }
            binding.txtNoControl.text.clear()

        }
    }

    private fun iniciarVentana() {
           var ventana = Intent(this,EnvioAsistencia::class.java)
        var usuario = FirebaseAuth.getInstance().currentUser
        var partes:List<String>
        usuario.let {
            partes = usuario?.email.toString().split("@")
        }
        ventana.putExtra("No.Control",partes.get(0))
        startActivity(ventana)
            finish()
    }
}