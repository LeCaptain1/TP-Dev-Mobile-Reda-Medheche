package fr.mines.philidor.historique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.mines.philidor.historique.PartieJouee.Fin;
import fr.mines.philidor.historique.PartieJouee.Resultat;

/**
 * Historique gardé en mémoire, rempli de parties inventées.
 * Les vraies parties arrivent à la partie 5.
 */
public final class HistoriqueMemoire {

    private static final long HEURE = 60L * 60L * 1000L;
    private static final long JOUR = 24L * HEURE;

    private static List<PartieJouee> parties;

    private HistoriqueMemoire() { }

    /** Les parties, de la plus récente à la plus ancienne. */
    public static synchronized List<PartieJouee> getParties() {
        if (parties == null) {
            parties = inventerParties();
        }
        return parties;
    }

    public static synchronized void supprimer(PartieJouee partie) {
        getParties().remove(partie);
    }

    private static List<PartieJouee> inventerParties() {
        long maintenant = System.currentTimeMillis();
        List<PartieJouee> liste = new ArrayList<>();
        liste.add(new PartieJouee(maintenant - 2 * HEURE, true, Resultat.VICTOIRE, 41, Fin.MAT));
        liste.add(new PartieJouee(maintenant - 5 * HEURE, false, Resultat.DEFAITE, 28, Fin.MAT));
        liste.add(new PartieJouee(maintenant - JOUR - 3 * HEURE, true, Resultat.NULLE, 63, Fin.PAT));
        liste.add(new PartieJouee(maintenant - JOUR - 6 * HEURE, true, Resultat.DEFAITE, 19, Fin.ABANDON));
        liste.add(new PartieJouee(maintenant - 2 * JOUR - HEURE, false, Resultat.VICTOIRE, 52, Fin.MAT));
        liste.add(new PartieJouee(maintenant - 2 * JOUR - 4 * HEURE, false, Resultat.DEFAITE, 35, Fin.MAT));
        liste.add(new PartieJouee(maintenant - 3 * JOUR, true, Resultat.NULLE, 87, Fin.REPETITION));
        liste.add(new PartieJouee(maintenant - 4 * JOUR, false, Resultat.VICTOIRE, 46, Fin.ABANDON));
        liste.add(new PartieJouee(maintenant - 5 * JOUR, true, Resultat.DEFAITE, 31, Fin.MAT));
        liste.add(new PartieJouee(maintenant - 6 * JOUR, false, Resultat.NULLE, 112, Fin.CINQUANTE_COUPS));
        liste.add(new PartieJouee(maintenant - 8 * JOUR, true, Resultat.VICTOIRE, 1, Fin.ABANDON));
        liste.add(new PartieJouee(maintenant - 9 * JOUR, false, Resultat.NULLE, 74, Fin.MATERIEL));

        // De la plus récente à la plus ancienne
        Collections.sort(liste, (a, b) -> Long.compare(b.getDate(), a.getDate()));
        return liste;
    }
}
