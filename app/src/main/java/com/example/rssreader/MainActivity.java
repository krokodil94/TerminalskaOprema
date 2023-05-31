package com.example.rssreader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import android.app.ProgressDialog;





// Zažene se ob zagonu aplikacije
public class MainActivity extends AppCompatActivity {
    private ProgressDialog progressDialog; // ProgressDialog za prikaz napredka pri nalaganju RSS vsebine



    ListView lvRss;  // ListView za prikaz RSS vsebine v obliki seznama
    ArrayList<String> titles = new ArrayList<>() ; // ArrayList za shranjevanje naslovov RSS vsebine; ArrayList je dinamičen seznam, ki omogoča shranjevanje elementov poljubnega tipa
    ArrayList<String> imageUrls = new ArrayList<>(); // Linki do slik
    List<String> categories = new ArrayList<>(); // Kategorije
    ArrayList<String> links = new ArrayList<String>();// Linki do člankov
    ArrayList<String> descriptions = new ArrayList<>();; // Opisi
    /*Ustvari nov seznam 'uniqueCategories tipa 'List', ki vsebuje samo unikatne elemente iz seznama 'categories'*/
    List<String> uniqueCategories = new ArrayList<>(new HashSet<>(categories));
    /*1. Ustvarimo nov HashSet - shrani elemente in odstrani podvojene elemente */
    /*2. Ustvarimo nov ArrayList iz elementov HashSet v vrstnem redu, kot jih vrne 'HashSet' */

    private void displayAllItems() {
        CustomArrayAdapter adapter = new CustomArrayAdapter(this, titles, imageUrls);
        lvRss.setAdapter(adapter);
    }

    public void createUniqueCategories() {
        // Ustvari nov HashSet, ki shrani elemente iz seznama categories
        // in odstrani podvojene elemente, saj HashSet ne dovoljuje podvajanja
        Set<String> uniqueCategoriesSet = new LinkedHashSet<>(categories);
        // Ustvari nov ArrayList, ki vsebuje elemente iz HashSet-a,
        // vendar v vrstnem redu, kot jih vrne HashSet
        uniqueCategories = new ArrayList<>(uniqueCategoriesSet);
    }
    // ProgressDialog je razred v Android SDK, ki nam omogoča prikaz napredka pri nalaganju vsebine
    @Override
    protected void onDestroy() {
        // Preverimo, če ProgressDialog obstaja in če se prikazuje
        if (progressDialog != null && progressDialog.isShowing()) {
            // Če obstaja, ga zapremo
            progressDialog.dismiss();
        }
        // Kličemo metodo onDestroy() iz nadrazreda, da pravilno izvedemo destruktorje vseh komponent
        super.onDestroy();
    }

    // Metoda za prikaz obvestila v obliki notifikacije
    private void showNotification(String title, String content) {
        // Določitev ID-ja notifikacije in ID-ja kanala, ki sta potrebna za prikaz obvestila
        int notificationId = 1;
        String channelId = "rss_reader_channel";
        // Ustvarjanje objekta,ki se uporablja za gradnjo obvestila.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                // Nastavitev ikone, naslova,vsebine in prioritete obvestila
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                // Določa, kako pomembno se obvestilo zdi uporabniku, in se lahko
                //uporabi za določanje o tem, ali naj se obvesitlo prikaže na glavnem zaslonu...
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Ustvarjanje objekta, ki predstavlja kanal za prikaz obvestila
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // prikaz obvestila s klicem metode 'notify' iz objekta 'notificationManager'
        // metoda prikaže obvestilo z določenim IDjem in vsebino, ki jo določi objekt 'builder'
        notificationManager.notify(notificationId, builder.build());
    }

    @Override // označuje, da gre za metodo, ki jo prekrivamo iz nadrazreda
    // Metoda za ustvarjanje menija v aplikaciji
    // izvede se, ko je meni v aktivnosti prvič prikazan.
    public boolean onCreateOptionsMenu(Menu menu) {
        // ustvarimo objekt 'inflater' tipa 'MenuInflater',
        // ki nam omogoča, da napišemo XML datoteko za naš meni
        MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.main_menu, menu);

        inflater.inflate(R.menu.main_menu, menu);
        // zanka, ki gre skozi seznam uniqueCategories in
        // za vsako kategorijo doda element menija s  `MenuItem` razredom.
        // Ta element ima enoličen ID, ki ga določi parameter 'i' v zanki.
        // in ime kategorije, ki ga dobimo iz seznama.
        for (int i = 0; i < uniqueCategories.size(); i++) {
            MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, uniqueCategories.get(i));

        }
        // metoda vrne boolean vrednost, ki pove, ali naj se meni prikaže ali ne
        return true;
    }
    // rabim to????????
    @Override
    // Metoda se kliče, ko se pripravlja meni preden se prikaže
    // Posodobitev menija glede na kategorije, ki so shranjene v "uniqueCategories
    public boolean onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);// kličemo metodo iz nadrazreda, da se pripravi meni
        menu.clear(); // Počisti meni
        getMenuInflater().inflate(R.menu.main_menu, menu); // Napolni meni z elementi iz XML datoteke

        // Dodaj kategorije meniju
        for (int i = 0; i < uniqueCategories.size(); i++) {
            // Dodaj element menija za kategorijo i s poimensko oznako
            MenuItem item = menu.add(Menu.NONE, i, Menu.NONE, uniqueCategories.get(i));

        }
        return true; // Vrnemo true, kar označi da smo uspešno pripravili meni
    }

    @Override
    // metoda, ki se izvede pri ustvarjanju aktivnosti
    // savedInstanceState je objekt razreda Bundle, ki vsebuje stanje dejavnosti,
    // če je bila prej ustvarjena
    //
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState); // kliče ustvarjanje dejavnosti iz nadrazreda
        setContentView(R.layout.activity_main); // določi, kateri pogled bo prikazan v aktivnosti.
        // V tem primeru se uporablja datoteka activity_main.xml, ki je definirana v mapi res/layout

        Switch switchTheme = findViewById(R.id.switch_theme); // pridobimo referenco na stikalo za temo
        // Nastavimo začetno vrednost stikala, ki ustreza privzeti nastavitvi naprave
        switchTheme.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);
        // Nastavimo poslušalca spremembe stikala teme
        switchTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchTheme();
            }
        });
        // Pridobimo referenco na akcijsko vrstico in shranimo v spremenljivko actionBar
        ActionBar actionBar = getSupportActionBar();

            actionBar.setTitle("24UR"); // Nastavimo naslov v akcijski vrstici

        // Poiščemo referenco na ListView in shranimo v spremenljivko lvRss
        lvRss = findViewById(R.id.lvRss);
        // Nastavimo poslušalca, ki se sproži, ko uporabnik klikne na element v seznamu
        lvRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            // metoda, ki se izvede, ko uporabnik klikne na element v seznamu
            // parent je objekt razreda AdapterView, ki vsebuje seznam
            // view je pogled, ki je bil kliknjen
            // position je indeks elementa v seznamu, ki je bil kliknjen
            // id je ID elementa, ki je bil kliknjen
            //  ko uporabnik klikne na seznamu, preverimo ali je izbran element veljaven.
            // če je veljaven, se ustvari nov objekt razreda Intent,
            // ki prenaša podatke o naslovu opisu in sliki na novo aktivnost(DetailActivity) in zažene novo aktivnost

            // Če je pozicija elementa manjša od velikosti seznama links, in seznama descriptions
            // se ustvari namen Intent, ki odpre aktivnost DetailActivity in se pošljejo podatki
            // o naslovu, opisu in URL-ju slike izbrane novice. Nato se aktivnost
            // DetailActivity zažene s klicem metode startActivity.
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < links.size() && position < descriptions.size()) {
                    Intent detailIntent = new Intent(MainActivity.this, DetailActivity.class);
                    detailIntent.putExtra("title", titles.get(position));
                    detailIntent.putExtra("description", descriptions.get(position));
                    detailIntent.putExtra("imageUrl", imageUrls.get(position));
                    detailIntent.putExtra("description", descriptions.get(position));
                    startActivity(detailIntent);
                } else {
                    // če izbrani element ni veljaven, se vnesena napaka zabeleži v dnevnik
                    Log.e("MainActivity", "Index out of bounds: position = " + position);
                }
            }
        });

        new ProcessInBackground().execute(); // zaženemo proces, za obdelavo podatkov iz vira RSS

        // Nastavimo trenutno temo glede na privzeto nastavitev naprave
        switchTheme.setChecked(AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES);

    }

    // sprememba teme aplikacije iz temne na svetlo in obratno
    private void switchTheme() {
        // Pridobimo trenutni način noči iz konfiguracije naprave
        // iz konfig. preberemo uiMode in uporabimo and operator
        // z vrednostjo Configuration.UI_MODE_NIGHT_MASK, da pridobimo trenutni način noči.
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        // Nastavimo novi način noči, ki je nasproten trenutnemu
        int newNightMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(newNightMode); // Nastavimo novi način noči v privzeto vrednost

        recreate(); // Ponovno ustvarimo aktivnost, da se uporabi nova tema
    }



    // se izvede, ko kliknemo na element v meniju( izberemo eno izmed kategorij)
    // menuItem -> kategorija menija, ki je bila izbrana
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // preverimo, če je bil izbran element za preklop teme
        switch (item.getItemId()) {
            case R.id.action_switch_theme: // ko je izbran elememt menija z IDjem action_switch_theme
                // če je bil, sprožimo funkcijo switchTheme()
                switchTheme();
                return true; // --> event je bil sprožen
            case R.id.action_home:
                displayAllItems();
                return true;
        }

        // preverimo, katera kategorija je bila izbrana
        // dobimo ime izbranega menija, in ga pretvorimo v string
        String category = item.getTitle().toString();
        // ustvarimo dva seznama(filteredTitles in filtered ImageUrls)
        ArrayList<String> filteredTitles = new ArrayList<>();
        ArrayList<String> filteredImageUrls = new ArrayList<>();

        // preveri se vsak naslov in primerja z izbrano kategorijo
        // Če se kategoriji ujemata, se naslov in URL slike dodata v seznam za filtriranje
        for (int i = 0; i < titles.size(); i++) {
            if (categories.get(i).equals(category)) {
                filteredTitles.add(titles.get(i));
                filteredImageUrls.add(imageUrls.get(i));
            }
        }

        // ustvari se nov prilagojen adapter z novimi filtriranimi seznami in ga nastavimo na ListView
        CustomArrayAdapter adapter = new CustomArrayAdapter(this, filteredTitles, filteredImageUrls);
        lvRss.setAdapter(adapter);

        // Prikažemo obvestilo o uspešni spremembi kategorije
        String notificationTitle = "Izbrana kategorija";
        String notificationContent = "Izbrali ste kategorijo: " + category;
        showNotification(notificationTitle, notificationContent);

        return super.onOptionsItemSelected(item);
    }







    // Prikaz podatkov
    // izhajamo iz ArrayAdapter razreda in ga prilagodimo za delo s dvema ArrayList objektoma
    public class CustomArrayAdapter extends ArrayAdapter<String> {
        // Definiramo zasebne atribute razreda
        private final Context context; // trenutni kontekst aplikacije
        private final ArrayList<String> titles; // shranjevanje naslovov
        private final ArrayList<String> imageUrls; // shranjevanje URL-jev slik

        // konstruktor CustomArrayAdapter razreda, ki prejme parametre context,titles in imageUrls.
        // konstruktor kliče konstruktor nadrejenega razreda ArrayAdapter in nastavi context,titles in ImageUrls atribute
        public CustomArrayAdapter(Context context, ArrayList<String> titles, ArrayList<String> imageUrls) {
            super(context, R.layout.list_item, titles);
            this.context = context;
            this.titles = titles;
            this.imageUrls = imageUrls;
        }

        @SuppressLint("RestrictedApi") // odstrani opozorilo, s strani Android Studio lint toola
        // RestrictedApi preprečuje opozorilo za uporabo nejavne API metode v Androidu
        @NonNull // getView() ne sme vrniti null kot rezultat
        @Override
        // metoda getView() ki se uporablja za prikaz podatkov v kontrolnikih na uporabniškem vmesniku.
        // Metoda ima tri parametre: position, convertView in parent
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Ustvarjanje novega LayoutInflater objekta( pretvorba XML v View objekt)
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Ustvarjanje novega View objekta iz XML predloge, ki se uporablja za prikaz podatkov v seznamu
            View rowView = inflater.inflate(R.layout.list_item, parent, false);
            // klic na TextView in ImageView iz View objekta, ki se uporabljajo za prikaz naslova in slike
            TextView textView = rowView.findViewById(R.id.title);
            ImageView imageView = rowView.findViewById(R.id.image);
            // Nastavitev besedila v TextView, z naslovom ki se nahaja na določenem položaju v seznamu
            textView.setText(titles.get(position));
            // Nalaganje slik v ImageView preko Picasso knjižnice
            Picasso.get().load(imageUrls.get(position)).into(imageView);
            // vrnitev View objekta, ki se uporablja za prikaz podatkov v seznamu
            return rowView;
        }
    }
    // metoda za odpiranje povezave in pridobivanje podatkov iz spletne strani v obliki inputStream objetka
    public InputStream getInputStream(URL url) {
        try {
            // najprej poskusimo pridobiti InputStream objekt preko openConnection() metode objekta URL,
            // ki se uporablja za vzpostavljanje povezave s ciljnim strežnikom in odpiranje tokov podatkov

            return url.openConnection().getInputStream(); // če se tok podatkov uspešno odpre, se InputStream vrne kot rezultat
            // Če se pri poskusu odpiranja tokov podatkov pojavi izjema tipa IOException, se vrnjeni
            //inputStream objekt nastavi na null in vrne se izjema.(povezava prekinjena/ ciljni vir podatkov ne obstaja)

        } catch (IOException e) {
            // IOException -- napaka pri branju ali pisanju podatkov v toku podatkov, kot je InputStream ali OutputStream
            return null;
        }
    }
    // ProcessInBackground razširja razred AsyncTask, ki se uporablja za izvajanje dolgotrajnih nalog v ozadju
    // da se ne blokira uporabniški vmesnik medtem ko se nalaganje podatkov izvaja.
    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {
        // imamo 3 razrede, onPreExecute, doInBackground in onPostExecute, ki se vsak izvajajo v določenem zaporedju
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        Exception exception = null;

        @Override
        // se izvaja preden se začne izvajanje metode doInBackground

        protected void onPreExecute() {
            super.onPreExecute();
            // pripravimo loading screen ("Busy loading RSS Feed...) ki ga nato prikažemo
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Nalagam novice...");
            progressDialog.show();
        }

        @Override
        // izvaja se v ozadju in se uporablja za nalaganje podatkov iz spleta

        protected Exception doInBackground(Integer... params) {
            try {
                // definiramo URL objekt, ki vsebuje spletno stran z RSS vsebino
                URL url = new URL("https://www.24ur.com/rss");
                // Ustvarimo objekt XmlPullParserFactory za razčlenjevanje XML podatkov.
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(false); // ???????????????????????????????
                // Pridobimo XmlPullParser objekt za razčlenjevanje podatkov.
                XmlPullParser xpp = factory.newPullParser();
                // Pridobimo InputStream objekt z uporabo metode getInputStream() iz prejšnje kode.
                InputStream inputStream = getInputStream(url);
                // Če InputStream objekt ni uspešno odprt, sprožimo izjemo
                if (inputStream == null) {
                    throw new IOException("Failed to open input stream");
                }
                // Nastavimo XmlPullParser objekt, da bere iz InputStream objekta in uporablja kodno tabelo UTF-8.
                xpp.setInput(inputStream,"UTF_8");
                // Definiramo spremenljivke, ki bodo uporabljene za shranjevanje podatkov med razčlenjevanjem.
                boolean insideItem = false;
                String category = null;

                int eventType = xpp.getEventType(); // ????????????????
                // Začnemo razčlenjevanje XML podatkov.
                while (eventType != XmlPullParser.END_DOCUMENT){
                    if (eventType == XmlPullParser.START_TAG){
                        // Preverimo, ali smo znotraj elementa "item".
                        if (xpp.getName().equalsIgnoreCase("item")){
                            insideItem = true;
                        }else if (xpp.getName().equalsIgnoreCase("title")){ // Če smo znotraj elementa "item", preverimo, ali gre za element "title".
                            if (insideItem){
                                String title = xpp.nextText(); // Preberemo besedilo v naslovu.
                                if (title != null) {
                                    titles.add(title); // Dodamo naslov v seznam naslovov.
                                }
                            }
                        }else if (xpp.getName().equalsIgnoreCase("link")){ // Preverimo, ali gre za element "link".
                            if (insideItem){
                                String link = xpp.nextText(); // Preberemo besedilo v povezavi.
                                if (link != null) {
                                    links.add(link); // Dodamo povezavo v seznam povezav.
                                }
                            }
                        }  else if (xpp.getName().equalsIgnoreCase("description")) { // Preverimo, ali gre za element "description".
                        if (insideItem) {
                            String description = xpp.nextText(); // Preberemo besedilo v opisu.
                            String[] parts = description.split("<img src=\"");
                            if (imageUrls == null) {
                                imageUrls = new ArrayList<>(); // Če seznam URL-jev slik še ni inicializiran, ga inicializiramo.
                            }
                            if (parts.length > 1) {
                                String imageUrl = parts[1].split("\"")[0];  // Izvlečemo URL slike.
                                imageUrls.add(imageUrl); // Dodamo URL v seznam URL-jev slik
                                description = parts[1].substring(parts[1].indexOf(">") + 1); // izvlečemo besedilo po URL-ju slike
                            } else {
                                description = parts[0]; // če ne vsebuje URLja slike, se shrani celotno besedilo
                            }
                            descriptions.add(description.trim()); // Store the description text // shranimo description v seznam descriptions . trim() izbriše presledke na začetku in koncu
                        }
                    }

                    else if (xpp.getName().equalsIgnoreCase("category")) { // Preverimo, ali gre za element "description".
                            if (insideItem) {
                                category = xpp.nextText(); // Preberemo besedilo v elementu "category"
                                if (category != null) {
                                    categories.add(category); // Če je vrednost spremenljivke "category" različna od "null", dodamo kategorijo v seznam "categories"
                                }
                            }
                        }
                    // preverjamo ali je tip trenutnega dogodka "END_TAG" in ali ime elementa ustreza "item" --> getEventType() in getName()
                    }else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")){ // preverimo, ali je konec elementa "item" v XML.
                        insideItem = false; // če je konec, se spremenljivka insideItem nastavi na false, kar pomeni da se ne obdeluje več noben element "item"
                    }
                    eventType = xpp.next(); // premaknemo se na naslednji dogodek v XML datoteki -> to se izvede v zanki "while", ki se izvaja dokler ni dosežen konec dokumenta
                }
                // exception se nastavi na ustrezno vrednost, ki se nato vrne iz metode 'doInBackground()'
            // napačen URL
            } catch (MalformedURLException e){
                exception = e;
            // neveljaven XML format
            } catch (XmlPullParserException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            }

            return exception;
        }

        @Override
        // se izvede po tem, ko se konča izvajanje metode doInBackground() v niti 'AsyncTask';


        protected void onPostExecute(Exception s) {
            super.onPostExecute(s);
            // preveri ali dialog za napredek obstaja in ali je prikazan.
            // v primeru, da je prikazan, se ga zapre z metodo 'dismiss()'
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            // ustvari se nov objekt 'CustomArrayAdapter', za prikaz elementov RSS vira v seznamu 'ListView'
            CustomArrayAdapter adapter = new CustomArrayAdapter(MainActivity.this, titles, imageUrls);
            lvRss.setAdapter(adapter);
            // ustvarimo seznam "unikatnih" kategorij
            displayAllItems();
            createUniqueCategories();
            progressDialog.dismiss(); //zapremo dialog za napredek
            invalidateOptionsMenu(); // ponovno kličemo metodi onCreateOptionsMenu() in onPrepareOptionsMenu()
            // za posodobitev prikaza menija aplikacije
        }
    }
    }
