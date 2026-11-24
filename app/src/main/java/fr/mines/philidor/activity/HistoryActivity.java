package fr.mines.philidor.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import fr.mines.philidor.R;
import fr.mines.philidor.databinding.ActivityHistoryBinding;
import fr.mines.philidor.historique.HistoriqueAdapter;
import fr.mines.philidor.historique.HistoriqueMemoire;
import fr.mines.philidor.historique.PartieJouee;

public class HistoryActivity extends AppCompatActivity {

    private HistoriqueAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        ActivityHistoryBinding binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        adapter = new HistoriqueAdapter(this, HistoriqueMemoire.getParties());
        binding.listeParties.setAdapter(adapter);
        // Affiché automatiquement à la place de la liste quand elle est vide
        binding.listeParties.setEmptyView(binding.vide);

        binding.listeParties.setOnItemLongClickListener((parent, vue, position, id) -> {
            confirmerSuppression(adapter.getItem(position));
            return true;
        });
    }

    private void confirmerSuppression(PartieJouee partie) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.historique_supprimer_titre)
                .setMessage(R.string.historique_supprimer_message)
                .setNegativeButton(R.string.historique_annuler, null)
                .setPositiveButton(R.string.historique_supprimer, (dialogue, which) -> {
                    HistoriqueMemoire.supprimer(partie);
                    adapter.notifyDataSetChanged();
                })
                .show();
    }
}
