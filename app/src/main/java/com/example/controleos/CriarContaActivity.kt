package com.example.controleos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CriarContaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_criar_conta)

        // capturando valores do formulario de cadastro
        val auth = FirebaseAuth.getInstance()
        val editNome = findViewById<EditText>(R.id.etNome)
        val editEmail = findViewById<EditText>(R.id.etEmail)
        val editTelefone = findViewById<EditText>(R.id.etTelefone)
        val editSenha = findViewById<EditText>(R.id.etSenhaCad)
        val editSenhaConf = findViewById<EditText>(R.id.etSenhaConf)
        val btnCriarConta = findViewById<Button>(R.id.btnCriarConta)

        //Cria a conta
        val db = FirebaseFirestore.getInstance()

        btnCriarConta.setOnClickListener{
            val nome = editNome.text.toString()
            val email = editEmail.text.toString()
            val telefone = editTelefone.text.toString()
            val senha = editSenha.text.toString()
            val senhaconf = editSenhaConf.text.toString()

            if (email.isEmpty() || senha.isEmpty()){
                Toast.makeText(this,getString(R.string.campo_vazio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else if (senha != senhaconf){
                Toast.makeText(this,getString(R.string.senha_conf), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else
            // criar uma conta no Firebase
            auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = task.result?.user?.uid
                        if (uid == null) {
                            Toast.makeText(this, getString(R.string.error_uid), Toast.LENGTH_SHORT)
                                .show()
                            return@addOnCompleteListener
                        }
                        val userMap = hashMapOf(
                            "nome" to nome,
                            "email" to email,
                            "telefone" to telefone
                        )
                        // Salva no Firestore usando o UID como ID do documento
                        db.collection("usuarios").document(uid ?: "")
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this,getString(R.string.sucesso_conta), Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this,getString(R.string.erro_conta), Toast.LENGTH_SHORT).show()
                            }

                    } else {
                        Toast.makeText(this, "Erro: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}