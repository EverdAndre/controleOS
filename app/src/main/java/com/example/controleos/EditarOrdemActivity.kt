package com.example.controleos

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditarOrdemActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_ordem)

        db = FirebaseFirestore.getInstance()

        val etNumeroOS = findViewById<EditText>(R.id.etNumeroOS)
        val etData = findViewById<EditText>(R.id.etData)
        val checkOrcamento = findViewById<RadioButton>(R.id.checkOrcamento)
        val checkAprovacao = findViewById<RadioButton>(R.id.checkAgAprov)
        val checkAprovado = findViewById<RadioButton>(R.id.checkAprov)
        val checkReprovado = findViewById<RadioButton>(R.id.checkReprov)
        val checkPecas = findViewById<RadioButton>(R.id.checkAgPecas)
        val checkBancada = findViewById<RadioButton>(R.id.checkBancada)
        val checkPronto = findViewById<RadioButton>(R.id.checkPronto)
        val checkConcluido = findViewById<RadioButton>(R.id.checkConcluido)
        val checkPago = findViewById<RadioButton>(R.id.checkPago)
        val checkAPagar = findViewById<RadioButton>(R.id.checkAPagar)
        val spinnerTipoPagamento = findViewById<Spinner>(R.id.TipoPagamento)
        val etInformado = findViewById<EditText>(R.id.informado)
        val etConstatado = findViewById<EditText>(R.id.constatado)
        val etExecutado = findViewById<EditText>(R.id.executado)
        val etValor = findViewById<EditText>(R.id.valor)
        val etSenhas = findViewById<EditText>(R.id.senhas)
        val btnSalvar = findViewById<android.widget.Button>(R.id.btnSalvar)
        val btnExcluir = findViewById<android.widget.Button>(R.id.btnExcluir)
        val btnLogout = findViewById<android.widget.Button>(R.id.btnLogout)
        val radioGroupStatus1 = findViewById<RadioGroup>(R.id.radioGroupStatus)
        val radioGroupStatus2 = findViewById<RadioGroup>(R.id.radioGroupStatus2)
        val radioGroupStatus3 = findViewById<RadioGroup>(R.id.radioGroupStatus3)

        // Flag de bloqueio para evitar loop recursivo
        var bloqueando = false

        // Função que limpa os outros dois grupos sem causar loop
        fun limparOutros(grupoSelecionado: RadioGroup) {
            if (bloqueando) return
            bloqueando = true
            if (grupoSelecionado != radioGroupStatus1) radioGroupStatus1.clearCheck()
            if (grupoSelecionado != radioGroupStatus2) radioGroupStatus2.clearCheck()
            if (grupoSelecionado != radioGroupStatus3) radioGroupStatus3.clearCheck()
            bloqueando = false
        }

// Configura os listeners com proteção contra loop
        radioGroupStatus1.setOnCheckedChangeListener { _, _ -> limparOutros(radioGroupStatus1) }
        radioGroupStatus2.setOnCheckedChangeListener { _, _ -> limparOutros(radioGroupStatus2) }
        radioGroupStatus3.setOnCheckedChangeListener { _, _ -> limparOutros(radioGroupStatus3) }


        val statusPagamento = intent.getStringExtra("statusPagamento")
        val statusOs = intent.getStringExtra("statusOs")
        val numeroOS = intent.getStringExtra("numeroOS")
        val data = intent.getStringExtra("data")
        val tipoPagamento = intent.getStringExtra("tipoPagamento")
        val informado = intent.getStringExtra("informado")
        val constatado = intent.getStringExtra("constatado")
        val executado = intent.getStringExtra("executado")
        val valor = intent.getStringExtra("valor")
        val senhas = intent.getStringExtra("senhas")

        etNumeroOS.setText(numeroOS)
        etData.setText(data)

        when (statusPagamento) {
            "Pago" -> checkPago.isChecked = true
            "A Pagar" -> checkAPagar.isChecked = true
        }

        when (statusOs) {
            "Ag.Aprov" -> checkAprovacao.isChecked = true
            "Aprovado" -> checkAprovado.isChecked = true
            "Reprovado" -> checkReprovado.isChecked = true
            "Ag.Peças" -> checkPecas.isChecked = true
            "Bancada" -> checkBancada.isChecked = true
            "Pronto" -> checkPronto.isChecked = true
            "OS Concluida" -> checkConcluido.isChecked = true
            else -> checkOrcamento.isChecked = true
        }

        etInformado.setText(informado)
        etConstatado.setText(constatado)
        etExecutado.setText(executado)
        etValor.setText(valor)
        etSenhas.setText(senhas)

        etNumeroOS.isEnabled = false

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.tipo_pagamento,
            android.R.layout.simple_spinner_dropdown_item
        )
        spinnerTipoPagamento.adapter = adapter
        spinnerTipoPagamento.setSelection(adapter.getPosition(tipoPagamento))

        etData.setOnClickListener {
            val calendario = Calendar.getInstance()
            val ano = calendario.get(Calendar.YEAR)
            val mes = calendario.get(Calendar.MONTH)
            val dia = calendario.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(this, { _, y, m, d ->
                val dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y)
                etData.setText(dataFormatada)
            }, ano, mes, dia)
            datePicker.show()
        }

        btnSalvar.setOnClickListener {
            val statusOsSelecionado = when {
                checkOrcamento.isChecked -> "Orçamento"
                checkAprovacao.isChecked -> "Ag.Aprov"
                checkAprovado.isChecked -> "Aprovado"
                checkReprovado.isChecked -> "Reprovado"
                checkPecas.isChecked -> "Ag.Peças"
                checkBancada.isChecked -> "Bancada"
                checkPronto.isChecked -> "Pronto"
                checkConcluido.isChecked -> "OS Concluida"
                else -> "Orçamento"
            }

            val novaOS = hashMapOf(
                "numeroOS" to (numeroOS ?: ""),
                "data" to etData.text.toString(),
                "statusOs" to statusOsSelecionado,
                "statusPagamento" to if (checkPago.isChecked) "Pago" else "A Pagar",
                "tipoPagamento" to spinnerTipoPagamento.selectedItem.toString(),
                "informado" to etInformado.text.toString(),
                "constatado" to etConstatado.text.toString(),
                "executado" to etExecutado.text.toString(),
                "valor" to etValor.text.toString(),
                "senhas" to etSenhas.text.toString()
            )

            db.collection("ordens_servico").document(numeroOS ?: "")
                .set(novaOS)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.update_sucesso), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, getString(R.string.update_error, e.message), Toast.LENGTH_SHORT).show()
                }
        }

        btnExcluir.setOnClickListener {
            val alertDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Confirmar exclusão")
                .setMessage(getString(R.string.excluir_confirm))
                .setPositiveButton("Sim") { dialog, _ ->
                    db.collection("ordens_servico").document(numeroOS ?: "")
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, getString(R.string.excluir_succes), Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, getString(R.string.erro_ao_excluir, e.message), Toast.LENGTH_SHORT).show()
                        }
                    dialog.dismiss()
                }
                .setNegativeButton(getString(R.string.cancelar)) { dialog, _ -> dialog.dismiss() }
                .create()

            alertDialog.show()
        }

        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, ListarActivity::class.java)
            startActivity(intent)
            finish()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
