package com.example.ryan.whosaround;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener, AdapterView.OnItemClickListener{

    private Spinner mSpinner, mSpinner2;
    private ListView mListView;
    private UsersAdapter adapter;
    ArrayAdapter<String> spinnerAdaper, spinnerAdapter2, listViewAdapter;
    // Array of choices for the spinner
    private String[] minutes = {"Select minutes", "15 min","30 min", "1 hr", "12 hr", "24 hr"};
    private String[] miles = {"Select miles", "5 mi", "10 mi", "15 mi", "20 mi"};
    ArrayList<Person> people;
    public static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //create Spinner
        //1. create the data source
        //2. define the appearance layout file through which the adapter will put data inside the spinner
        //3. define what to do when the user clicks on the spinner using the onItemSelectedListener
        // Selection of the spinner
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner2 = (Spinner) findViewById(R.id.spinner2);
        // Application of the Array to the Spinner
        spinnerAdaper = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minutes);
        spinnerAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, miles);
        // The drop down view
        spinnerAdaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdaper);
        mSpinner.setOnItemSelectedListener(this);

        spinnerAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner2.setAdapter(spinnerAdapter2);
        mSpinner2.setOnItemSelectedListener(this);

        //create basic ArrayAdapter
        mListView = (ListView) findViewById(R.id.listView);
//        listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, people);
//        mListView.setAdapter(listViewAdapter);
        mListView.setOnItemClickListener(this);

        //Using a Custom ArrayAdapter
        // Construct the data source
        ArrayList<Person> arrayOfPeople = new ArrayList<Person>();
        // Create the adapter to convert the array to views
        adapter = new UsersAdapter(this, arrayOfPeople);
        // Attach the adapter to a ListView
        mListView.setAdapter(adapter);
        // Add item to adapter

        //get data - arraylist of person from previous activity
        Bundle bundle = getIntent().getExtras();
        people = bundle.getParcelableArrayList("mylist");

        //add person object to adapter
        for(Person person : people){
            adapter.add(person);
        }
    }

    //OnItemSelectedListener is used for Spinners
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        TextView myText = (TextView) view;
        Spinner spinner = (Spinner) parent;
        //minute spinner
        if(spinner.getId() == R.id.spinner)
        {
            ArrayList<Person> usersLast15min = new ArrayList<Person>();
            ArrayList<Person> usersLast30min = new ArrayList<Person>();
            ArrayList<Person> usersLast1hr = new ArrayList<Person>();
            ArrayList<Person> usersLast12hr = new ArrayList<Person>();
            ArrayList<Person> usersLast24hr = new ArrayList<Person>();

            /*Find the time difference between now(current) and the person object last seen*/

            //HH converts hour in 24 hours format (0-23), day calculation
            SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            Date d1 = new Date();
            Date d2 = null;
            adapter.clear();
            for(Person person : people){
                try {
                    d1 = formatter.parse(formatter.format(d1));
                    d2 = formatter.parse(person.getLastSeen());
                    long diff = d1.getTime() - d2.getTime();
                    long diffMinutes = diff / (60 * 1000) % 60;
                    long diffHours = diff / (60 * 60 * 1000) % 24;
                    long diffDays = diff / (24 * 60 * 60 * 1000);
//                Log.i(TAG, person.getFirstName()+" "+person.getLastName()+": ");
//                Log.i(TAG, diffDays + " days, ");
//                Log.i(TAG, diffHours + " hours, ");
//                Log.i(TAG, diffMinutes + " minutes, ");
                    if(diffMinutes <= 15){
                        usersLast15min.add(person);
                    }
                    else if(diffMinutes <= 30){
                        usersLast30min.add(person);
                    }else if(diffHours <= 1){
                        usersLast1hr.add(person);
                    }else if(diffHours <= 12){
                        usersLast12hr.add(person);
                    }else{
                        usersLast24hr.add(person);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if(myText.getText().toString() == "15 min"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersLast15min){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "30 min"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersLast30min){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "1 hr"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersLast1hr){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "12 hr"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersLast12hr){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "24 hr") {
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersLast15min){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }
        }
        //mile spinner
        else if(spinner.getId() == R.id.spinner2)
        {
            ArrayList<Person> usersin5Miles = new ArrayList<Person>();
            ArrayList<Person> usersin10Miles = new ArrayList<Person>();
            ArrayList<Person> usersin15Miles = new ArrayList<Person>();
            ArrayList<Person> usersin20Miles = new ArrayList<Person>();
            adapter.clear();
            for(Person person : people){
                String str = person.getMiles();
                int spaceIndex = str.indexOf(" ");
                Double mile = Double.parseDouble(str.substring(0, spaceIndex));
                if(mile <= 5.0){
                    usersin5Miles.add(person);
                }else if(mile <= 10.0){
                    usersin10Miles.add(person);
                }else if(mile <= 15.0){
                    usersin15Miles.add(person);
                }else{
                    usersin20Miles.add(person);
                }
            }
            if(myText.getText().toString() == "5 mi"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersin5Miles){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "10 mi"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersin10Miles){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "15 mi"){
                Toast.makeText(this, "You selected "+myText.getText(), Toast.LENGTH_SHORT).show();
                for(Person person : usersin15Miles){
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }else if(myText.getText().toString() == "20 mi") {
                Toast.makeText(this, "You selected " + myText.getText(), Toast.LENGTH_SHORT).show();
                for (Person person : usersin20Miles) {
                    adapter.add(person);
                }
                adapter.notifyDataSetChanged();
            }
        }


    }

    //onNothingSelected is called when you current selection disappears due to some event like touch getting
    //activated or the adapter becomes empty
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    // OnItemClickListener is used for ListViews.
    //when user press on a person's name it will send an email
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LinearLayout myText = (LinearLayout) view;
//        Toast.makeText(this, "You selected "+position, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, "ryanefendy95@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hi, this was sent from my app");
        intent.putExtra(Intent.EXTRA_TEXT, "Hey I am nearby");
        intent.setType("message/rfc822");
        Intent chooser = Intent.createChooser(intent, "Send Email");
        startActivity(chooser);
    }

    public class UsersAdapter extends ArrayAdapter<Person> {
        public UsersAdapter(Context context, ArrayList<Person> people) {
            super(context, 0, people);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Person person = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.custom_template, parent, false);
            }
            // Lookup view for data population
            TextView tvMiles = (TextView) convertView.findViewById(R.id.tvMiles);
            TextView tvPerson = (TextView) convertView.findViewById(R.id.tvPerson);
            // Populate the data into the template view using the data object
            tvMiles.setText(person.getMiles());
            tvPerson.setText(person.getFirstName() + " " + person.getLastName()+"\nLast Seen: "+person.getLastSeen());
            // Return the completed view to render on screen
            return convertView;
        }
    }
}
