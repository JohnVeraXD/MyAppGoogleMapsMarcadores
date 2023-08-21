package com.example.myappgooglemapsmarcadores;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import adapter.InfoAdaptador;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    GoogleMap mapa;
    private static String API_KEY = "AIzaSyBE_It7_wRv4xbhSgF_loFKT2nLfOHfH-k";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        //Limpiar los marcadores
        this.mapa.clear();

        //Cargar los marcadores cercanos
        this.CargarMarcadores(latLng, 500);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        //Coord de la UTEQ
        //-1.0126947416487295, -79.46918723470034
        mapa = googleMap;
        //Ya esta conectado el mapa
        mapa.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mapa.getUiSettings().setZoomControlsEnabled(true);
        //Mover mapa a una ubicacion
        LatLng madrid = new LatLng(-1.0126947416487295, -79.46918723470034);
        CameraPosition camPos = new CameraPosition.Builder()
                .target(madrid)
                .zoom(17)
                .bearing(45) //noreste arriba
                .tilt(0) //punto de vista de la cámara 0 grados
                .build();
        CameraUpdate camUpd3 =
                CameraUpdateFactory.newCameraPosition(camPos);

        mapa.animateCamera(camUpd3);
        mapa.setOnMapClickListener(this);

        InfoAdaptador infoWindowAdapter = new InfoAdaptador(this);
        mapa.setInfoWindowAdapter(infoWindowAdapter);
    }


    public void CargarMarcadores(LatLng latLng, double radius) {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?fields=name&location=" + latLng.latitude + "," + latLng.longitude + "&radius=" + radius + "&type=bar&key=" + API_KEY;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject place = results.getJSONObject(i);
                                String placeId = place.getString("place_id");
                                JSONObject geometry = place.getJSONObject("geometry");
                                JSONObject location = geometry.getJSONObject("location");
                                double placeLat = location.getDouble("lat");
                                double placeLng = location.getDouble("lng");
                                JSONArray photos = place.optJSONArray("photos");
                                if (photos != null && photos.length() > 0) {
                                    JSONObject firstPhoto = photos.getJSONObject(0);
                                    String photoReference = firstPhoto.getString("photo_reference");
                                    String detailsUrl = "https://maps.googleapis.com/maps/api/place/details/json?" +
                                            "fields=name%2Crating%2Cvicinity%2Cformatted_phone_number%2Copening_hours" +
                                            "&place_id=" + placeId +
                                            "&key=" + API_KEY;

                                    JsonObjectRequest detailsRequest = new JsonObjectRequest(Request.Method.GET, detailsUrl, null,
                                            new Response.Listener<JSONObject>() {
                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        String name = response.getJSONObject("result").getString("name");
                                                        String rating = response.getJSONObject("result").getString("rating");
                                                        String Direccion = response.getJSONObject("result").getString("vicinity");
                                                        String phoneNumber = response.getJSONObject("result").getString("formatted_phone_number");

                                                        JSONObject result = response.getJSONObject("result");
                                                        JSONObject openingHours = result.getJSONObject("opening_hours");
                                                        JSONArray weekdayTextArray = openingHours.getJSONArray("weekday_text");

                                                        String photoUrl = "https://maps.googleapis.com/maps/api/place/photo?" +
                                                                "maxwidth=200" +
                                                                "&photo_reference=" + photoReference +
                                                                "&key=" + API_KEY;
                                                        ImageRequest imageRequest = new ImageRequest(photoUrl,
                                                                new Response.Listener<android.graphics.Bitmap>() {
                                                                    @Override
                                                                    public void onResponse(android.graphics.Bitmap response) {
                                                                        LatLng placeLatLng = new LatLng(placeLat, placeLng);
                                                                        MarkerOptions markerOptions = new MarkerOptions()
                                                                                .position(placeLatLng)
                                                                                .title(name)
                                                                                .snippet("Calificación: " + rating + "\nDirección: " + Direccion + "\nTelefono: " + phoneNumber);

                                                                        // Agregar los textos de los días al snippet
                                                                        if (weekdayTextArray != null) {
                                                                            StringBuilder weekdayText = new StringBuilder("Horarios:\n");
                                                                            for (int i = 0; i < weekdayTextArray.length(); i++) {
                                                                                try {
                                                                                    weekdayText.append(weekdayTextArray.getString(i)).append("\n");
                                                                                } catch (
                                                                                        JSONException e) {
                                                                                    e.printStackTrace();
                                                                                }
                                                                            }
                                                                            markerOptions.snippet(markerOptions.getSnippet() + "\n" + weekdayText.toString());
                                                                        }

                                                                        Marker marker = mapa.addMarker(markerOptions);
                                                                        marker.setTag(response);

                                                                    }
                                                                }, 0, 0, ImageView.ScaleType.CENTER_CROP, null,
                                                                new Response.ErrorListener() {
                                                                    @Override
                                                                    public void onErrorResponse(VolleyError error) {
                                                                        error.printStackTrace();
                                                                    }
                                                                });
                                                        requestQueue.add(imageRequest);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.i("Error Volley", "onErrorResponse: " + error.toString());
                                                }
                                            });
                                    requestQueue.add(detailsRequest);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        requestQueue.add(request);
    }
}