package com.example.controleos
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
// tela 3
class ListarActivity : AppCompatActivity() {
    private lateinit var editarOrdemLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_listar)

        editarOrdemLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                // Recarrega a lista após edição/exclusão
                recarregarLista()
            }
        }

        db = FirebaseFirestore.getInstance()

        val btnLogout = findViewById<Button>(R.id.btnLogoutListar)
        btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, TelaPrincipal::class.java)
            startActivity(intent)
            finish()
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.listarOs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val db = FirebaseFirestore.getInstance()

        db.collection("ordens_servico")
            .get()
            .addOnSuccessListener { result ->
                val listaOrdens = mutableListOf<OrdemServico>()
                for (document in result) {
                    val ordem = OrdemServico(
                        numeroOS = document.getString("numeroOS") ?: "",
                        data = document.getString("data") ?: "",
                        statusPagamento = document.getString("statusPagamento") ?: "",
                        tipoPagamento = document.getString("tipoPagamento") ?: "",
                        informado = document.getString("informado") ?: "",
                        constatado = document.getString("constatado") ?: "",
                        executado = document.getString("executado") ?: "",
                        senhas = document.getString("senhas") ?: ""
                    )
                    listaOrdens.add(ordem)
                }
                recyclerView.adapter = OrdemAdapter(listaOrdens, editarOrdemLauncher)
            }
            .addOnFailureListener {

            }
    }

    private fun recarregarLista() {
        val recyclerView = findViewById<RecyclerView>(R.id.listarOs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        db.collection("ordens_servico")
            .get()
            .addOnSuccessListener { result ->
                val listaOrdens = mutableListOf<OrdemServico>()
                for (document in result) {
                    val ordem = OrdemServico(
                        numeroOS = document.getString("numeroOS") ?: "",
                        data = document.getString("data") ?: "",
                        statusPagamento = document.getString("statusPagamento") ?: "",
                        tipoPagamento = document.getString("tipoPagamento") ?: "",
                        informado = document.getString("informado") ?: "",
                        constatado = document.getString("constatado") ?: "",
                        executado = document.getString("executado") ?: "",
                        senhas = document.getString("senhas") ?: ""
                    )
                    listaOrdens.add(ordem)
                }

                // Passa o launcher para o adapter
                recyclerView.adapter = OrdemAdapter(listaOrdens, editarOrdemLauncher)
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.erro_lista), Toast.LENGTH_SHORT).show()
            }
    }
}

    // Adapter dentro da activity
    class OrdemAdapter(
        private val listaOrdens: List<OrdemServico>,
        private val editarOrdemLauncher: ActivityResultLauncher<Intent>
    ) :
        RecyclerView.Adapter<OrdemAdapter.OrdemViewHolder>() {

        class OrdemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtData: TextView = itemView.findViewById(R.id.txtData)
            val txtNumeroOS: TextView = itemView.findViewById(R.id.txtNumeroOS)
            val txtInformado: TextView = itemView.findViewById(R.id.txtInformado)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_ordem, parent, false)
            return OrdemViewHolder(view)
        }

        override fun onBindViewHolder(holder: OrdemViewHolder, position: Int) {

            val ordem = listaOrdens[position]
            holder.txtData.text = ordem.data
            holder.txtNumeroOS.text = ordem.numeroOS
            holder.txtInformado.text = ordem.informado

            holder.itemView.setOnClickListener {
                val context = holder.itemView.context
                val intent = Intent(context, EditarOrdemActivity::class.java).apply {
                    putExtra("numeroOS", ordem.numeroOS)
                    putExtra("data", ordem.data)
                    putExtra("statusPagamento", ordem.statusPagamento)
                    putExtra("tipoPagamento", ordem.tipoPagamento)
                    putExtra("informado", ordem.informado)
                    putExtra("constatado", ordem.constatado)
                    putExtra("executado", ordem.executado)
                    putExtra("senhas", ordem.senhas)
                }
                editarOrdemLauncher.launch(intent)
            }

        }
        override fun getItemCount(): Int = listaOrdens.size

    }

