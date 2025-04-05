package com.example.controleos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
// tela 2
class TelaPrincipal : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore // Instância do Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.activity_tela_principal)

        // Inicializa o Firestore
        db = FirebaseFirestore.getInstance()

        // Capturar os valores dos campos
        val etNumeroOS = findViewById<EditText>(R.id.etNumeroOS)
        val etData = findViewById<EditText>(R.id.etData)
        val checkPago = findViewById<RadioButton>(R.id.checkPago)
        val checkAPagar = findViewById<RadioButton>(R.id.checkAPagar)
        val spinnerTipoPagamento = findViewById<Spinner>(R.id.TipoPagamento)
        val etInformado = findViewById<EditText>(R.id.informado)
        val etConstatado = findViewById<EditText>(R.id.constatado)
        val etExecutado = findViewById<EditText>(R.id.executado)
        val etValor = findViewById<EditText>(R.id.valor)
        val etSenhas = findViewById<EditText>(R.id.senhas)
        val btnSalvar = findViewById<Button>(R.id.btnSalvar)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnListar = findViewById<Button>(R.id.btnListar)
        val radioGroup = findViewById<RadioGroup>(R.id.radioGroupOpcPag)

        //  Define a data do dia automaticamente ao abrir a tela
        val calendario = Calendar.getInstance(TimeZone.getTimeZone("America/Sao_Paulo"))
        val formatoData = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etData.setText(formatoData.format(calendario.time))

        //  Abrir o DatePickerDialog ao clicar no campo
        etData.setOnClickListener {
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, anoSelecionado, mesSelecionado, diaSelecionado ->
                val dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%04d", diaSelecionado, mesSelecionado + 1, anoSelecionado)
                etData.setText(dataFormatada)
            }, ano, mes, dia)

            datePickerDialog.show()
        }

        //  Evento de clique no botão "Salvar"
        btnSalvar.setOnClickListener {
            val numeroOS = etNumeroOS.text.toString().trim()
            val data = etData.text.toString().trim()
            val statusPagamento = when {
                checkPago.isChecked -> "Pago"
                checkAPagar.isChecked -> "A Pagar"
                else -> ""
            }
            val tipoPagamento = spinnerTipoPagamento.selectedItem.toString()
            val informado = etInformado.text.toString().trim()
            val constatado = etConstatado.text.toString().trim()
            val executado = etExecutado.text.toString().trim()
            val valor = etValor.text.toString().trim()
            val senhas = etSenhas.text.toString().trim()
            //  Validação dos campos obrigatórios
            if (numeroOS.isEmpty() || data.isEmpty()) {
                Toast.makeText(this, getString(R.string.os_data_vazio), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (statusPagamento.isEmpty()) {
                Toast.makeText(this, "Selecione o status de pagamento!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Criando um objeto para salvar no Firestore
            val ordemServico = hashMapOf(
                "numeroOS" to numeroOS,
                "data" to data,
                "statusPagamento" to statusPagamento,
                "tipoPagamento" to tipoPagamento,
                "informado" to informado,
                "constatado" to constatado,
                "executado" to executado,
                "valor" to valor,
                "senhas" to senhas
            )

            //  Salvando no Firestore
            db.collection("ordens_servico").document(numeroOS)
                .set(ordemServico)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.db_success), Toast.LENGTH_SHORT).show()

                    //  LIMPAR O FORMULÁRIO APÓS SALVAR
                    etNumeroOS.setText("")
                    etData.setText("")
                    radioGroup.clearCheck()
                    spinnerTipoPagamento.setSelection(0)
                    etInformado.setText("")
                    etConstatado.setText("")
                    etExecutado.setText("")
                    etValor.setText("")
                    etSenhas.setText("")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "${getString(R.string.db_error)}: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        btnListar.setOnClickListener {
            val intent = Intent(this, ListarActivity::class.java)
            startActivity(intent)
        }

        //  Botão "Sair" (Logout)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Logout realizado!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        //  Ajuste das barras de status para evitar sobreposição
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //  Criando um ArrayAdapter usando o array do strings.xml
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_pagamento,
            android.R.layout.simple_spinner_dropdown_item
        )

        //  Aplicando o adapter ao Spinner
        spinnerTipoPagamento.adapter = adapter

        //  Definir "A pagar" como opção padrão
        spinnerTipoPagamento.setSelection(0)

    }
}
