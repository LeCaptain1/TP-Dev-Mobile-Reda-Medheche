package fr.mines.philidor.historique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.mines.philidor.R;
import fr.mines.philidor.databinding.ItemPartieBinding;

/** Adaptateur de la ListView de l'historique (motif ViewHolder pour un défilement fluide). */
public class HistoriqueAdapter extends BaseAdapter {

    private final Context contexte;
    private final LayoutInflater inflater;
    private final List<PartieJouee> parties;
    private final DateFormat formatDate;

    public HistoriqueAdapter(Context contexte, List<PartieJouee> parties) {
        this.contexte = contexte;
        this.inflater = LayoutInflater.from(contexte);
        this.parties = parties;
        // Jour + mois abrégé, dans le format de la langue du téléphone (« 22 nov. », « Nov 22 »…)
        Locale locale = Locale.getDefault();
        String motif = android.text.format.DateFormat.getBestDateTimePattern(locale, "dMMM");
        this.formatDate = new SimpleDateFormat(motif, locale);
    }

    @Override
    public int getCount() {
        return parties.size();
    }

    @Override
    public PartieJouee getItem(int position) {
        return parties.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            ItemPartieBinding binding = ItemPartieBinding.inflate(inflater, parent, false);
            holder = new ViewHolder(binding);
            convertView = binding.getRoot();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.lier(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        private final ItemPartieBinding binding;

        ViewHolder(ItemPartieBinding binding) {
            this.binding = binding;
        }

        void lier(PartieJouee partie) {
            // Pastille
            int lettre;
            int fond;
            int libelle;
            switch (partie.getResultat()) {
                case VICTOIRE:
                    lettre = R.string.historique_lettre_victoire;
                    fond = R.drawable.pastille_victoire;
                    libelle = R.string.historique_victoire;
                    break;
                case DEFAITE:
                    lettre = R.string.historique_lettre_defaite;
                    fond = R.drawable.pastille_defaite;
                    libelle = R.string.historique_defaite;
                    break;
                default:
                    lettre = R.string.historique_lettre_nulle;
                    fond = R.drawable.pastille_nulle;
                    libelle = R.string.historique_nulle;
                    break;
            }
            binding.pastille.setText(lettre);
            binding.pastille.setBackgroundResource(fond);

            // Ligne 1 : « Victoire · Blancs »
            String camp = contexte.getString(partie.isJoueurBlancs()
                    ? R.string.historique_blancs : R.string.historique_noirs);
            binding.resultat.setText(contexte.getString(
                    R.string.historique_format_resultat, contexte.getString(libelle), camp));

            // Ligne 2 : « 22 nov. · 41 coups · mat »
            String date = formatDate.format(new Date(partie.getDate()));
            String coups = contexte.getResources().getQuantityString(
                    R.plurals.historique_nb_coups, partie.getNbCoups(), partie.getNbCoups());
            binding.details.setText(contexte.getString(
                    R.string.historique_format_details, date, coups, texteFin(partie.getFin())));

            // Roi de la couleur jouée
            binding.roi.setText(partie.isJoueurBlancs()
                    ? R.string.symbole_roi_blanc : R.string.symbole_roi_noir);
        }

        private String texteFin(PartieJouee.Fin fin) {
            int id;
            switch (fin) {
                case MAT: id = R.string.fin_mat; break;
                case PAT: id = R.string.fin_pat; break;
                case ABANDON: id = R.string.fin_abandon; break;
                case MATERIEL: id = R.string.fin_materiel; break;
                case CINQUANTE_COUPS: id = R.string.fin_cinquante_coups; break;
                default: id = R.string.fin_repetition; break;
            }
            return contexte.getString(id);
        }
    }
}
