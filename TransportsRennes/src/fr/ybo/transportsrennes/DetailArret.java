package fr.ybo.transportsrennes;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import fr.ybo.transportsrennes.keolis.gtfs.modele.ArretFavori;
import fr.ybo.transportsrennes.keolis.gtfs.modele.Route;
import fr.ybo.transportsrennes.util.Formatteur;
import fr.ybo.transportsrennes.util.JoursFeries;
import fr.ybo.transportsrennes.util.LogYbo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Activitée permettant d'afficher les détails d'une station.
 *
 * @author ybonnel
 */
public class DetailArret extends ListActivity {

	private static final LogYbo LOG_YBO = new LogYbo(DetailArret.class);
	private final static Class<?> classDrawable = R.drawable.class;

	private boolean prochainArrets = true;

	private Cursor currentCursor;

	private String clauseWhereForTodayCalendrier() {
		if (JoursFeries.isJourFerie()) {
			return "Dimanche = 1";
		}
		final Calendar calendar = Calendar.getInstance();
		switch (calendar.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.MONDAY:
				return "Lundi = 1";
			case Calendar.TUESDAY:
				return "Mardi = 1";
			case Calendar.WEDNESDAY:
				return "Mercredi = 1";
			case Calendar.THURSDAY:
				return "Jeudi = 1";
			case Calendar.FRIDAY:
				return "Vendredi = 1";
			case Calendar.SATURDAY:
				return "Samedi = 1";
			case Calendar.SUNDAY:
				return "Dimanche = 1";
			default:
				return null;
		}
	}

	private ArretFavori favori;

	private void recuperationDonneesIntent() {
		favori = (ArretFavori) getIntent().getExtras().getSerializable("favori");
		if (favori == null) {
			favori = new ArretFavori();
			favori.setStopId(getIntent().getExtras().getString("idArret"));
			favori.setNomArret(getIntent().getExtras().getString("nomArret"));
			favori.setDirection(getIntent().getExtras().getString("direction"));
			final Route myRoute = (Route) getIntent().getExtras().getSerializable("route");
			favori.setRouteId(myRoute.getId());
			favori.setRouteNomCourt(myRoute.getNomCourt());
			favori.setRouteNomLong(myRoute.getNomLong());
		}
	}

	private void gestionViewsTitle() {
		LinearLayout conteneur = (LinearLayout) findViewById(R.id.conteneurImage);
		TextView nomLong = (TextView) findViewById(R.id.nomLong);
		nomLong.setText(Formatteur.formatterChaine(favori.getRouteNomLong()));
		try {
			Field fieldIcon = classDrawable.getDeclaredField("i" + favori.getRouteNomCourt().toLowerCase());
			int ressourceImg = fieldIcon.getInt(null);
			ImageView imgView = new ImageView(getApplicationContext());
			imgView.setImageResource(ressourceImg);
			conteneur.addView(imgView);
		} catch (NoSuchFieldException e) {
			TextView textView = new TextView(getApplicationContext());
			textView.setTextSize(16);
			textView.setText(favori.getRouteNomCourt());
			conteneur.addView(textView);
		} catch (IllegalAccessException e) {
			TextView textView = new TextView(getApplicationContext());
			textView.setTextSize(16);
			textView.setText(favori.getRouteNomCourt());
			conteneur.addView(textView);
		}
		((TextView) findViewById(R.id.detailArret_nomArret)).setText(
				favori.getNomArret() + " vers " + Formatteur.formatterChaine(favori.getDirection().replaceAll(favori.getRouteNomCourt(), "")));
	}

	private DetailArretAdapter construireAdapter() {
		closeCurrentCursor();
		if (prochainArrets) {
			return construireAdapterProchainsDeparts();
		}
		return construireAdapterAllDeparts();
	}

	private DetailArretAdapter construireAdapterAllDeparts() {
		Calendar calendar = Calendar.getInstance();
		int now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		StringBuilder requete = new StringBuilder();
		requete.append("select HeuresArrets.heureDepart as _id ");
		requete.append("from Calendrier,  HeuresArrets");
		requete.append(Route.getIdWithoutSpecCar(favori.getRouteId()));
		requete.append(" as HeuresArrets ");
		requete.append("where ");
		requete.append(clauseWhereForTodayCalendrier());
		requete.append(" and HeuresArrets.serviceId = Calendrier.id");
		requete.append(" and HeuresArrets.routeId = :routeId");
		requete.append(" and HeuresArrets.stopId = :arretId");
		requete.append(" order by HeuresArrets.heureDepart;");
		List<String> selectionArgs = new ArrayList<String>();
		selectionArgs.add(favori.getRouteId());
		selectionArgs.add(favori.getStopId());
		LOG_YBO.debug("Exécution de la requête permettant de récupérer tous les horaires des arrêts.");
		currentCursor = BusRennesApplication.getDataBaseHelper().executeSelectQuery(requete.toString(), selectionArgs);
		LOG_YBO.debug("Exécution de la requête permettant de récupérer tous les horaires des arrêts terminée : " + currentCursor.getCount());
		return new DetailArretAdapter(getApplicationContext(), currentCursor, now);
	}

	private DetailArretAdapter construireAdapterProchainsDeparts() {
		Calendar calendar = Calendar.getInstance();
		int now = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
		StringBuilder requete = new StringBuilder();
		requete.append("select HeuresArrets.heureDepart as _id ");
		requete.append("from Calendrier,  HeuresArrets");
		requete.append(Route.getIdWithoutSpecCar(favori.getRouteId()));
		requete.append(" as HeuresArrets ");
		requete.append("where ");
		requete.append(clauseWhereForTodayCalendrier());
		requete.append(" and HeuresArrets.serviceId = Calendrier.id");
		requete.append(" and HeuresArrets.routeId = :routeId");
		requete.append(" and HeuresArrets.stopId = :arretId");
		requete.append(" and HeuresArrets.heureDepart >= :maintenant");
		requete.append(" order by HeuresArrets.heureDepart;");
		List<String> selectionArgs = new ArrayList<String>();
		selectionArgs.add(favori.getRouteId());
		selectionArgs.add(favori.getStopId());
		selectionArgs.add(Long.toString(now));
		LOG_YBO.debug("Exécution de la requête permettant de récupérer les arrêts avec les temps avant les prochains bus");
		currentCursor = BusRennesApplication.getDataBaseHelper().executeSelectQuery(requete.toString(), selectionArgs);
		LOG_YBO.debug("Exécution de la requête permettant de récupérer les arrêts terminée : " + currentCursor.getCount());
		return new DetailArretAdapter(getApplicationContext(), currentCursor, now);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detailarret);
		recuperationDonneesIntent();
		gestionViewsTitle();
		setListAdapter(construireAdapter());
		final ListView lv = getListView();
		lv.setTextFilterEnabled(true);
	}

	private void closeCurrentCursor() {
		if (currentCursor != null && !currentCursor.isClosed()) {
			currentCursor.close();
		}
	}

	@Override
	protected void onDestroy() {
		closeCurrentCursor();
		super.onDestroy();
	}


	private static final int GROUP_ID = 0;
	private static final int MENU_ALL_STOPS = Menu.FIRST;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(GROUP_ID, MENU_ALL_STOPS, Menu.NONE, R.string.menu_prochainArrets);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		menu.findItem(MENU_ALL_STOPS).setTitle(prochainArrets ? R.string.menu_allArrets : R.string.menu_prochainArrets);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
			case MENU_ALL_STOPS:
				prochainArrets = !prochainArrets;
				setListAdapter(construireAdapter());
				getListView().invalidate();
				return true;
		}
		return false;
	}

}