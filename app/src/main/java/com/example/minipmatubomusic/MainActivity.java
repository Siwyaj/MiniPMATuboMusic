package com.example.minipmatubomusic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.types.Track;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements LocationListener, SensorEventListener {
    private static final String CLIENT_ID = "ddb886e12b6645c9b13c7449d188da5a";
    private static final String REDIRECT_URI = "http://localhost/";
    private SpotifyAppRemote mSpotifyAppRemote;
    private Boolean justLaunched = false;
    private Boolean playing=false;
    private TextView trackPlayingTextView;
    private SharedPreferences.Editor editor;
    private TextView lightSensorTextView;
    private SharedPreferences prefs;
    private boolean smallerGateOpen;
    private boolean greaterGateOpen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editor = getSharedPreferences(
                "speed", MODE_PRIVATE).edit();
        editor.putFloat("speed",5f);
        prefs = getSharedPreferences("speed", MODE_PRIVATE);
        trackPlayingTextView = findViewById(R.id.songTitle);
        //plaingBoolean = findViewById(R.id.Plaingbooleanchecker);
        //status = findViewById(R.id.status);
        smallerGateOpen = true;
        greaterGateOpen = true;
        ConnectSpotify();
        MakeButtonsInteractable();
        LocationManager lm = (LocationManager) this. getSystemService(Context.LOCATION_SERVICE);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);//this line gives an error, but the error is just a warning for the app to need location access. this is requested on the line before
        this.onLocationChanged((Location) null);//cant remember if this line actually does anything anymore, if it does not leaving it would not do any harm, if it does i dont want to remove it.
        addLightSensor();
    }


    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onStop() {
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
        // Aaand we will finish off here.
    }
    private void SetTitleOfSong(){
        trackPlayingTextView = findViewById(R.id.songTitle);

        //trackPlayingTextView.setText("SetTitleOfSong method ran, but did not change the name to song");
        mSpotifyAppRemote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
            final Track track = playerState.track;
                    if (track != null) {
                        String trackName = track.name;
                        if (trackName!=null){
                            trackPlayingTextView.setText(trackName);
                        }
                        else{
                            trackPlayingTextView.setText("tracknamelistener worked but did not find the song");
                        }
                    }
                    else{
                        //trackPlayingTextView.setText("track was null");
                    }
                });



    }
    private void ConnectSpotify(){
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });
    }
    private void MakeButtonsInteractable(){
        ImageView startPause = findViewById(R.id.play);
        startPause.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if (playing){
                    startPause.setImageResource(android.R.drawable.ic_media_play);
                    mSpotifyAppRemote.getPlayerApi().pause();
                    playing=false;
                   // plaingBoolean.setText("not playing");
                }
                else if(justLaunched){
                    startPause.setImageResource(android.R.drawable.ic_media_play);
                    //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:0JTaSx9jkW1saMOc6t0vIk?si=71c80f585d064358");//https://open.spotify.com/playlist/0nTyjcqOuR5ZPEObLV24DZ?si=loxr_xAOSQ6m3z3L9pzAgA&utm_source=copy-link https://open.spotify.com/playlist/0JTaSx9jkW1saMOc6t0vIk?si=71c80f585d064358
                    //mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                    //mSpotifyAppRemote.getPlayerApi().skipNext();
                    SetTitleOfSong();
                    justLaunched = false;
                    playing = true;
                   // plaingBoolean.setText("playing");
                }
                else{
                    startPause.setImageResource(android.R.drawable.ic_media_pause);
                    mSpotifyAppRemote.getPlayerApi().resume();
                    playing=true;
                    SetTitleOfSong();
                    //plaingBoolean.setText("playing");
                }
            }
        });

        ImageView skipButton = findViewById(R.id.skip);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipNext();
                SetTitleOfSong();
                //Log.d("MainActivity","skip button clicked");
            }
        });

        ImageView prevButton = findViewById(R.id.back);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote.getPlayerApi().skipPrevious();
                SetTitleOfSong();
                //Log.d("MainActivity","Prev button clicked");
            }
        });
        ImageView gotoTempoMatcherButton = findViewById(R.id.goToTempoMatcherButton);
        Intent goToTempoMatcherIntent = new Intent(this, Settings.class);
        gotoTempoMatcherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(goToTempoMatcherIntent);
            }
        });
    }
    Location lastLocation = null;
    @Override
    public void onLocationChanged(Location currentLocation) {
        float speed=prefs.getFloat("setSpeed",5);
        TextView SetSpeedtester = findViewById(R.id.setSpeed);
        //SetSpeedtester.setText(Float.toString(speed));
        Log.d("MainActivity","SpeedSet: " +speed);
        double speedInMph=0;
        if(lastLocation!=null){//calculates the speed, but very imprecisely
            double deltaDistance = currentLocation.distanceTo(lastLocation);
            double deltaTime = (currentLocation.getTime() - lastLocation.getTime()) / 1000.0;
            speedInMph = deltaDistance / deltaTime * 2.2369;
        }//got the best result by doing mph
        //TextView speedTester = findViewById(R.id.SpeedTester);
        //speedTester.setText(String.valueOf(speedInMph));
        boolean stateChanged=false;
        lastLocation=currentLocation;
        if(playing){
            if (speedInMph<speed&&smallerGateOpen){
                greaterGateOpen=true;
                smallerGateOpen=false;
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:5ksaUaYEgnywCzO6nmAIwN?si=7a3d715e39bc4ed3");//https://open.spotify.com/playlist/5ksaUaYEgnywCzO6nmAIwN?si=7a3d715e39bc4ed3
                mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                mSpotifyAppRemote.getPlayerApi().skipNext();
                //status.setText("walking");
            }
            else if(speedInMph>=speed&&greaterGateOpen){
                greaterGateOpen=false;
                smallerGateOpen = true;
                mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:0JTaSx9jkW1saMOc6t0vIk?si=71c80f585d064358");
                mSpotifyAppRemote.getPlayerApi().setShuffle(true);
                mSpotifyAppRemote.getPlayerApi().skipNext();
                //status.setText("running");
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        LocationListener.super.onStatusChanged(provider, status, extras);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        LocationListener.super.onProviderDisabled(provider);
    }
    private void addLightSensor(){//this method makes the song matcher text into a light sensor
        lightSensorTextView = findViewById(R.id.lightLevelTextView);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String lightLevel = Float.toString(sensorEvent.values[0]);
        Log.d("MainActivity","lightsensor: " +sensorEvent.values[0]);

        lightSensorTextView.setText(lightLevel);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}