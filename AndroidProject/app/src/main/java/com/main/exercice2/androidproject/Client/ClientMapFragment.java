package com.main.exercice2.androidproject.Client;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.main.exercice2.androidproject.AlertType;
import com.main.exercice2.androidproject.CommercantListAdapter;
import com.main.exercice2.androidproject.CommercantObjet;
import com.main.exercice2.androidproject.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.Objects;
import com.main.exercice2.androidproject.Constantes;
import static android.content.Context.LOCATION_SERVICE;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ClientMapFragment extends Fragment implements SearchView.OnQueryTextListener {
    private MapView map;
    ArrayList<OverlayItem> items;
    ArrayList<CommercantObjet> commercantObjetArrayList;
    SearchView searchView;
    ListView listView;
    ArrayAdapter adapter;


    ClientMapFragment() {
    }

    private LocationManager locationManager;
    private Location currentLocation;
    Location cLocation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frag_map_client, container, false);
        searchView = rootView.findViewById(R.id.search);
        searchView.setOnQueryTextListener(this);
        listView = rootView.findViewById(R.id.listSearch);
        Configuration.getInstance().load(getActivity().getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()));

        //test
        map = rootView.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        //GeoPoint startPoint = new GeoPoint(43.6520,7.00517);
        currentLocation=getLocation();
        GeoPoint startPoint = new GeoPoint(currentLocation.getAltitude()* 1E6, currentLocation.getLongitude()* 1E6);
        IMapController mapController = map.getController();
        mapController.setCenter(startPoint);
        mapController.setZoom(18.0);

        items = new ArrayList<>();
        commercantObjetArrayList = new ArrayList<>();
        //CommercantObjet homeCom = new CommercantObjet("home","rallo's home", AlertType.DEFAULT,null,new GeoPoint(43.65020,7.00517));
        CommercantObjet homeCom = new CommercantObjet("home", "rallo's home", AlertType.DEFAULT, null, new GeoPoint(currentLocation.getAltitude()* 1E6, currentLocation.getLongitude()* 1E6));
        CommercantObjet restoCom = new CommercantObjet("resto", "delice de maman", AlertType.DEFAULT, null, new GeoPoint(43.64950, 7.00517));
        commercantObjetArrayList.add(homeCom);
        commercantObjetArrayList.add(restoCom);
        for (CommercantObjet c : commercantObjetArrayList) {
            items.add(new OverlayItem(c.getTitle(), c.getMessage(), c.getGeoPoint()));
        }
        //OverlayItem home = new OverlayItem("home","rallo's home", new GeoPoint(43.65020,7.00517));
        //Drawable m =home.getMarker(0);
        //items.add(home);



        //items.add(new OverlayItem("resto","delice de maman",new GeoPoint(43.64950,7.00517)));

        /** Mise en place de l'adapteur pour l'array list**/

        adapter =new CommercantListAdapter(this.getContext(),commercantObjetArrayList);
        ((ListView)rootView.findViewById(R.id.listSearch)).setAdapter(adapter);

        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(getActivity().getApplicationContext(), items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        return true;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                });

        mOverlay.setFocusItemsOnTap(true);
        map.getOverlays().add(mOverlay);

        listView.setVisibility(View.GONE);
        return rootView;
    }

    Location getLocation(){
        final LocationManager locationManager = (LocationManager) (getActivity().getSystemService(LOCATION_SERVICE));
        //check if GPS permission is already GRANTED
        final boolean permissionGranted = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        Log.d(TAG, "permissionGranted = " + permissionGranted);
        if (permissionGranted) {
            LocationListener loclistener = new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    cLocation = location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.d(TAG, provider + " sensor ON");
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    cLocation = locationManager.getLastKnownLocation(provider);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d(TAG, provider+" sensor OFF");
                    Intent intent=new Intent();
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent,Constantes.REQUEST_CAMERA);
                    return;
                }
            };
            cLocation=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, loclistener);
        }else {
            Log.d(TAG, "Permission NOT GRANTED  ! ");
            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                cLocation=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }else {
                Intent intent=new Intent();
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent,Constantes.REQUEST_GPS);

            }
        }

        return cLocation;
    }

    @Override
    public void onPause() {
        super.onPause();
        map.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        map.onResume();
    }


    @Override
    public boolean onQueryTextSubmit(String s) {
        listView.setVisibility(View.GONE);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        if(s.equals("")){
            listView.setVisibility(View.GONE);
        }
        else {
            listView.setVisibility(View.VISIBLE);
        }
        adapter.getFilter().filter(s);
        return true;
    }
}
