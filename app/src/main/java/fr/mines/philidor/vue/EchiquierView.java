package fr.mines.philidor.vue;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import fr.mines.philidor.R;
import fr.mines.philidor.moteur.Partie;

/**
 * L'échiquier, dessiné à la main. Il affiche une {@link Partie}, gère la sélection
 * d'une pièce et signale le coup choisi à son écouteur (c'est l'activité qui le joue).
 */
public class EchiquierView extends View {

    /** Prévenu quand l'utilisateur a choisi un coup légal. */
    public interface EcouteurCoup {
        void coupChoisi(String uci);
    }

    /** Ordre des pièces dans les chaînes de glyphes : celui du codage de Partie. */
    private static final String CODES_PIECES = "KQRBNP";
    /** Force le rendu « texte » (et non emoji) des symboles d'échecs, notamment ♟. */
    private static final String RENDU_TEXTE = "︎";

    private final Paint peintureClair = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureFonce = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureSelection = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureDernierCoup = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureCible = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureAnneau = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peinturePiece = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureContour = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint peintureCoordonnee = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int couleurClair;
    private final int couleurFonce;
    private final int couleurPieceBlanche;
    private final int couleurPieceNoire;
    private final String glyphes;

    private Partie partie;
    private boolean retourne;
    private boolean actif = true;
    private EcouteurCoup ecouteur;

    private int colonneSelection = -1;
    private int ligneSelection = -1;
    private List<String> cibles = new ArrayList<>();

    public EchiquierView(Context context) {
        this(context, null);
    }

    public EchiquierView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        couleurClair = ContextCompat.getColor(context, R.color.echiquier_clair);
        couleurFonce = ContextCompat.getColor(context, R.color.echiquier_fonce);
        couleurPieceBlanche = ContextCompat.getColor(context, R.color.piece_blanche);
        couleurPieceNoire = ContextCompat.getColor(context, R.color.piece_noire);
        glyphes = context.getString(R.string.glyphes_pieces);

        peintureClair.setColor(couleurClair);
        peintureFonce.setColor(couleurFonce);
        peintureSelection.setColor(ContextCompat.getColor(context, R.color.echiquier_selection));
        peintureDernierCoup.setColor(ContextCompat.getColor(context, R.color.echiquier_dernier_coup));
        peintureCible.setColor(ContextCompat.getColor(context, R.color.echiquier_cible));
        peintureAnneau.setColor(ContextCompat.getColor(context, R.color.echiquier_cible));
        peintureAnneau.setStyle(Paint.Style.STROKE);

        peinturePiece.setTextAlign(Paint.Align.CENTER);
        peintureContour.setTextAlign(Paint.Align.CENTER);
        peintureContour.setStyle(Paint.Style.STROKE);
        peintureContour.setStrokeJoin(Paint.Join.ROUND);
        peintureContour.setColor(ContextCompat.getColor(context, R.color.piece_contour));
        peintureCoordonnee.setTypeface(Typeface.DEFAULT_BOLD);
    }

    // ------------------------------------------------------------------
    // Réglages depuis l'activité
    // ------------------------------------------------------------------

    public void setPartie(Partie partie) {
        this.partie = partie;
        effacerSelection();
    }

    /** Vrai pour voir l'échiquier du côté des Noirs. */
    public void setRetourne(boolean retourne) {
        this.retourne = retourne;
        invalidate();
    }

    /** Faux pour ignorer les touchers (partie terminée, par exemple). */
    public void setActif(boolean actif) {
        this.actif = actif;
        if (!actif) {
            effacerSelection();
        }
    }

    public void setEcouteurCoup(EcouteurCoup ecouteur) {
        this.ecouteur = ecouteur;
    }

    public void effacerSelection() {
        colonneSelection = -1;
        ligneSelection = -1;
        cibles = new ArrayList<>();
        invalidate();
    }

    // ------------------------------------------------------------------
    // Mesure : un carré aussi large que possible
    // ------------------------------------------------------------------

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int largeur = MeasureSpec.getSize(widthMeasureSpec);
        int hauteur = MeasureSpec.getSize(heightMeasureSpec);
        int cote = largeur;
        if (MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.UNSPECIFIED && hauteur > 0) {
            cote = Math.min(largeur, hauteur);
        }
        setMeasuredDimension(cote, cote);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float taille = taille();
        peinturePiece.setTextSize(taille * 0.78f);
        peintureContour.setTextSize(taille * 0.78f);
        peintureContour.setStrokeWidth(taille * 0.04f);
        peintureAnneau.setStrokeWidth(taille * 0.08f);
        peintureCoordonnee.setTextSize(taille * 0.2f);
    }

    private float taille() {
        return getWidth() / 8f;
    }

    // ------------------------------------------------------------------
    // Conversions case <-> écran
    // ------------------------------------------------------------------

    private float gauche(int colonne) {
        return (retourne ? 7 - colonne : colonne) * taille();
    }

    private float haut(int ligne) {
        return (retourne ? ligne : 7 - ligne) * taille();
    }

    private int colonneEn(float x) {
        int c = (int) (x / taille());
        c = Math.max(0, Math.min(7, c));
        return retourne ? 7 - c : c;
    }

    private int ligneEn(float y) {
        int l = (int) (y / taille());
        l = Math.max(0, Math.min(7, l));
        return retourne ? l : 7 - l;
    }

    // ------------------------------------------------------------------
    // Dessin
    // ------------------------------------------------------------------

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        float t = taille();

        // Cases (a1 est une case foncée)
        for (int c = 0; c < 8; c++) {
            for (int l = 0; l < 8; l++) {
                Paint p = (c + l) % 2 == 0 ? peintureFonce : peintureClair;
                canvas.drawRect(gauche(c), haut(l), gauche(c) + t, haut(l) + t, p);
            }
        }

        if (partie == null) {
            return;
        }

        // Les deux cases du dernier coup
        String dernier = partie.dernierCoup();
        if (dernier != null) {
            surligner(canvas, dernier.charAt(0) - 'a', dernier.charAt(1) - '1', peintureDernierCoup);
            surligner(canvas, dernier.charAt(2) - 'a', dernier.charAt(3) - '1', peintureDernierCoup);
        }

        // La pièce sélectionnée
        if (colonneSelection >= 0) {
            surligner(canvas, colonneSelection, ligneSelection, peintureSelection);
        }

        dessinerCoordonnees(canvas, t);

        // Les pièces
        Paint.FontMetrics fm = peinturePiece.getFontMetrics();
        float decalage = -(fm.ascent + fm.descent) / 2f;
        for (int c = 0; c < 8; c++) {
            for (int l = 0; l < 8; l++) {
                char piece = partie.piece(c, l);
                if (piece == ' ') {
                    continue;
                }
                int index = CODES_PIECES.indexOf(Character.toUpperCase(piece));
                if (index < 0) {
                    continue;
                }
                String glyphe = glyphes.charAt(index) + RENDU_TEXTE;
                float x = gauche(c) + t / 2f;
                float y = haut(l) + t / 2f + decalage;
                peinturePiece.setColor(Character.isUpperCase(piece) ? couleurPieceBlanche : couleurPieceNoire);
                canvas.drawText(glyphe, x, y, peintureContour);
                canvas.drawText(glyphe, x, y, peinturePiece);
            }
        }

        // Cases d'arrivée possibles : un point pour une case vide, un cercle pour une prise
        for (String uci : cibles) {
            int c = uci.charAt(2) - 'a';
            int l = uci.charAt(3) - '1';
            float cx = gauche(c) + t / 2f;
            float cy = haut(l) + t / 2f;
            if (estUnePrise(uci)) {
                canvas.drawCircle(cx, cy, t / 2f - peintureAnneau.getStrokeWidth() / 2f, peintureAnneau);
            } else {
                canvas.drawCircle(cx, cy, t * 0.15f, peintureCible);
            }
        }
    }

    private void surligner(Canvas canvas, int c, int l, Paint p) {
        if (c < 0 || c > 7 || l < 0 || l > 7) {
            return;
        }
        canvas.drawRect(gauche(c), haut(l), gauche(c) + taille(), haut(l) + taille(), p);
    }

    /** Lettres des colonnes sur la rangée du bas, chiffres des lignes sur la colonne de gauche. */
    private void dessinerCoordonnees(Canvas canvas, float t) {
        float marge = t * 0.06f;
        int ligneBas = retourne ? 7 : 0;
        int colonneGauche = retourne ? 7 : 0;
        for (int c = 0; c < 8; c++) {
            peintureCoordonnee.setColor((c + ligneBas) % 2 == 0 ? couleurClair : couleurFonce);
            peintureCoordonnee.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(String.valueOf((char) ('a' + c)),
                    gauche(c) + t - marge, haut(ligneBas) + t - marge, peintureCoordonnee);
        }
        for (int l = 0; l < 8; l++) {
            peintureCoordonnee.setColor((colonneGauche + l) % 2 == 0 ? couleurClair : couleurFonce);
            peintureCoordonnee.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.valueOf((char) ('1' + l)),
                    gauche(colonneGauche) + marge, haut(l) + marge - peintureCoordonnee.ascent(),
                    peintureCoordonnee);
        }
    }

    /** Une prise : la case d'arrivée est occupée, ou un pion part en diagonale (prise en passant). */
    private boolean estUnePrise(String uci) {
        int c1 = uci.charAt(0) - 'a';
        int l1 = uci.charAt(1) - '1';
        int c2 = uci.charAt(2) - 'a';
        int l2 = uci.charAt(3) - '1';
        if (partie.piece(c2, l2) != ' ') {
            return true;
        }
        return Character.toUpperCase(partie.piece(c1, l1)) == 'P' && c1 != c2;
    }

    // ------------------------------------------------------------------
    // Toucher
    // ------------------------------------------------------------------

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (partie == null || !actif) {
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            toucher(colonneEn(event.getX()), ligneEn(event.getY()));
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private void toucher(int c, int l) {
        // 1. Une case d'arrivée de la pièce sélectionnée : on joue le coup
        String coup = coupVers(c, l);
        if (coup != null) {
            effacerSelection();
            if (ecouteur != null) {
                ecouteur.coupChoisi(coup);
            }
            return;
        }

        // 2. Une pièce du camp qui a le trait : on la sélectionne
        char piece = partie.piece(c, l);
        boolean pieceDuTrait = piece != ' ' && Character.isUpperCase(piece) == partie.traitAuxBlancs();
        if (pieceDuTrait && !(c == colonneSelection && l == ligneSelection)) {
            colonneSelection = c;
            ligneSelection = l;
            cibles = partie.coupsDepuis(c, l);
            invalidate();
            return;
        }

        // 3. Ailleurs (ou la même pièce) : on annule la sélection
        effacerSelection();
    }

    /** Le coup de la pièce sélectionnée vers cette case ; une promotion devient une dame. */
    @Nullable
    private String coupVers(int c, int l) {
        String trouve = null;
        for (String uci : cibles) {
            if (uci.charAt(2) - 'a' == c && uci.charAt(3) - '1' == l) {
                if (uci.length() == 4 || uci.charAt(4) == 'q') {
                    return uci;
                }
                trouve = uci;
            }
        }
        return trouve;
    }
}
