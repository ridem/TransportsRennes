/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.lemet.application.activity.bus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.Serializable;
import java.util.List;

import fr.lemet.application.R;
import fr.lemet.application.adapters.bus.LigneAdapter;
import fr.lemet.application.application.TransportsMetzApplication;
import fr.lemet.transportscommun.activity.commun.BaseActivity;
import fr.lemet.transportscommun.donnees.modele.Ligne;

/**
 * Activité affichant les lignes de bus..
 *
 * @author ybonnel
 */
public class BusMetz extends BaseActivity.BaseListActivity {

    private void constructionListe() {
        List<Ligne> lignes = TransportsMetzApplication.getDataBaseHelper().select(new Ligne(), "ordre");
        setListAdapter(new LigneAdapter(this, lignes));
        ListView lv = getListView();
        lv.setFastScrollEnabled(true);
        lv.setTextFilterEnabled(true);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Serializable ligne = (Serializable) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(BusMetz.this, ListArret.class);
                intent.putExtra("ligne", ligne);
                startActivity(intent);
            }

        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bus);
		getActivityHelper().setupActionBar(R.menu.default_menu_items, R.menu.holo_default_menu_items);
        constructionListe();
    }
}
