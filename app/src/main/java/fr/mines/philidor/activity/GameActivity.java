package fr.mines.philidor.activity;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import fr.mines.philidor.R;
import fr.mines.philidor.databinding.ActivityGameBinding;
import fr.mines.philidor.moteur.Partie;

public class GameActivity extends AppCompatActivity {

    private static final String ETAT_COUPS = "coups";
    private static final String ETAT_JOUEUR_BLANCS = "joueurBlancs";

    private ActivityGameBinding binding;
    private Partie partie;
    private boolean joueurBlancs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        binding = ActivityGameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        float creativite = getIntent().getFloatExtra("creativite", 0);

        // La couleur du joueur (« Hasard » est tiré au sort une seule fois)
        if (savedInstanceState != null) {
            joueurBlancs = savedInstanceState.getBoolean(ETAT_JOUEUR_BLANCS, true);
        } else {
            MainActivity.COULEUR couleur =
                    (MainActivity.COULEUR) getIntent().getSerializableExtra("couleur");
            if (couleur == MainActivity.COULEUR.noir) {
                joueurBlancs = false;
            } else if (couleur == MainActivity.COULEUR.hasard) {
                joueurBlancs = new Random().nextBoolean();
            } else {
                joueurBlancs = true;
            }
        }

        // La partie (reconstruite après une recréation de l'activité)
        partie = new Partie();
        if (savedInstanceState != null) {
            ArrayList<String> coups = savedInstanceState.getStringArrayList(ETAT_COUPS);
            if (coups != null) {
                try {
                    partie = Partie.depuisCoups(coups);
                } catch (IllegalArgumentException e) {
                    partie = new Partie();
                }
            }
        }

        // Bandeaux : l'adversaire en haut, le joueur en bas
        String campJoueur = getString(joueurBlancs ? R.string.partie_blancs : R.string.partie_noirs);
        String campAdverse = getString(joueurBlancs ? R.string.partie_noirs : R.string.partie_blancs);
        binding.nomAdversaire.setText(R.string.title);
        binding.detailAdversaire.setText(getString(R.string.partie_camp_creativite, campAdverse, creativite));
        binding.nomJoueur.setText(R.string.partie_vous);
        binding.detailJoueur.setText(campJoueur);

        // L'échiquier : le joueur voit son camp en bas
        binding.echiquier.setRetourne(!joueurBlancs);
        binding.echiquier.setPartie(partie);
        binding.echiquier.setEcouteurCoup(uci -> {
            if (partie.jouer(uci)) {
                rafraichir();
            }
        });

        binding.butAnnuler.setOnClickListener(v -> {
            partie.annuler();
            binding.echiquier.effacerSelection();
            rafraichir();
        });

        rafraichir();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(ETAT_COUPS, new ArrayList<>(partie.coups()));
        outState.putBoolean(ETAT_JOUEUR_BLANCS, joueurBlancs);
    }

    /** Met à jour l'échiquier, le statut, la liste des coups et le bouton Annuler. */
    private void rafraichir() {
        Partie.Etat etat = partie.etat();
        binding.echiquier.setActif(etat == Partie.Etat.EN_COURS);
        binding.echiquier.invalidate();
        binding.statut.setText(texteStatut(etat));
        binding.listeCoups.setText(texteCoups());
        binding.defilementCoups.post(() -> binding.defilementCoups.fullScroll(View.FOCUS_RIGHT));
        binding.butAnnuler.setEnabled(!partie.coups().isEmpty());
    }

    private String texteStatut(Partie.Etat etat) {
        String trait = getString(partie.traitAuxBlancs() ? R.string.partie_trait_blancs : R.string.partie_trait_noirs);
        switch (etat) {
            case MAT:
                return getString(partie.traitAuxBlancs() ? R.string.partie_mat_noirs_gagnent
                        : R.string.partie_mat_blancs_gagnent);
            case PAT:
                return getString(R.string.partie_pat);
            case NULLE_MATERIEL:
            case NULLE_50_COUPS:
            case NULLE_REPETITION:
                return getString(R.string.partie_nulle);
            default:
                return partie.enEchec() ? getString(R.string.partie_echec, trait) : trait;
        }
    }

    /** « 1. e4 e5 2. Nf3 Nc6 » */
    private String texteCoups() {
        List<String> san = partie.coupsSan();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < san.size(); i++) {
            if (i > 0) {
                sb.append(' ');
            }
            if (i % 2 == 0) {
                sb.append(getString(R.string.partie_numero_coup, i / 2 + 1)).append(' ');
            }
            sb.append(san.get(i));
        }
        return sb.toString();
    }
}
