package fr.mines.philidor.historique;

/** Une partie terminée, telle qu'elle apparaît dans l'historique. */
public class PartieJouee {

    public enum Resultat { VICTOIRE, DEFAITE, NULLE }

    public enum Fin { MAT, PAT, ABANDON, MATERIEL, CINQUANTE_COUPS, REPETITION }

    private final long date;           // en millisecondes depuis epoch
    private final boolean joueurBlancs;
    private final Resultat resultat;
    private final int nbCoups;
    private final Fin fin;

    public PartieJouee(long date, boolean joueurBlancs, Resultat resultat, int nbCoups, Fin fin) {
        this.date = date;
        this.joueurBlancs = joueurBlancs;
        this.resultat = resultat;
        this.nbCoups = nbCoups;
        this.fin = fin;
    }

    public long getDate() { return date; }
    public boolean isJoueurBlancs() { return joueurBlancs; }
    public Resultat getResultat() { return resultat; }
    public int getNbCoups() { return nbCoups; }
    public Fin getFin() { return fin; }
}
