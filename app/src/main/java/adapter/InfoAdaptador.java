package adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myappgooglemapsmarcadores.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

public class InfoAdaptador extends AppCompatActivity implements GoogleMap.InfoWindowAdapter{
    private Context context;

    public InfoAdaptador(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        View view = LayoutInflater.from(context).inflate(R.layout.mostrarinfo, null);

        TextView titleTextView = view.findViewById(R.id.txtNombre);
        TextView snippetTextView = view.findViewById(R.id.txtInfo);
        ImageView Imagen=view.findViewById(R.id.imgLogo);


        Bitmap foto = (Bitmap) marker.getTag();
        titleTextView.setText(marker.getTitle());
        snippetTextView.setText(marker.getSnippet());
        Imagen.setImageBitmap(foto);

        return view;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }

}
