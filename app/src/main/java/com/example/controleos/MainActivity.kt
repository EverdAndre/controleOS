package com.example.controleos
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.content.SharedPreferences
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import android.text.InputType


// tela 1
private lateinit var sharedPreferences: SharedPreferences

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializa o SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE)

        // capturando valores do formulario de login
        val auth = FirebaseAuth.getInstance()
        val editEmail = findViewById<EditText>(R.id.textEmail)
        val editSenha = findViewById<EditText>(R.id.textSenha)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCriarConta = findViewById<Button>(R.id.btnCriarConta)
        val checkLembrar = findViewById<CheckBox>(R.id.checkLembrar)

        // Recuperar email e senha se já foram salvos
        val emailSalvo = sharedPreferences.getString("email", "")
        val senhaSalva = sharedPreferences.getString("senha", "")
        val lembrar = sharedPreferences.getBoolean("lembrar", false)

        if (lembrar) {
            editEmail.setText(emailSalvo)
            editSenha.setText(senhaSalva)
            checkLembrar.isChecked = true
        }

        btnCriarConta.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(getString(R.string.restrito))
            builder.setMessage(getString(R.string.senha_restrita))

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            builder.setView(input)

            builder.setPositiveButton("Confirmar") { dialog, _ ->
                val senhaDigitada = input.text.toString()
                val senhaCorreta = getString(R.string.senha_admin)

                if (senhaDigitada == senhaCorreta) {
                    val intent = Intent(this, CriarContaActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this,getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }

            builder.setNegativeButton("Cancelar") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }


        btnLogin.setOnClickListener {
            val email = editEmail.text.toString().trim() // Captura o email atualizado
            val senha = editSenha.text.toString().trim() // Captura a senha atualizada

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, getString(R.string.campo_vazio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Fazer login no Firebase com os dados atualizados
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //  Se o usuário marcou "Lembrar-me", salvar as credenciais
                        val editor = sharedPreferences.edit()
                        if (checkLembrar.isChecked) {
                            editor.putString("email", email)
                            editor.putString("senha", senha)
                            editor.putBoolean("lembrar", true)
                        } else {
                            editor.clear() // Se não estiver marcado, remover credenciais salvas
                        }
                        editor.apply()

                        // Ir para a tela principal
                        val intent = Intent(this, ListarActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // Se o login falhar, mostra um erro
                        Toast.makeText(this, getString(R.string.erro_conta) + ": " + task.exception?.message, Toast.LENGTH_SHORT).show()
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
