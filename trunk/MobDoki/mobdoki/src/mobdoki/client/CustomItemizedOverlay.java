package mobdoki.client;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

 /*
 * Terkepen megjelolt helyek
 */
public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> mapOverlays = new ArrayList<OverlayItem>();
    private Context context;

    public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
        super(boundCenterBottom(defaultMarker));
        this.context = context;
    }

    public void addOverlay(OverlayItem overlay) {
        mapOverlays.add(overlay);
        populate();
    }
    
    @Override
    protected OverlayItem createItem(int i) {
        return mapOverlays.get(i);
    }

    @Override
    public int size() {
        return mapOverlays.size();
    }
    
    @Override
	protected boolean onTap(int index) {
		OverlayItem item = mapOverlays.get(index);
		Toast.makeText(context, item.getSnippet(), 300).show();
		return true;
	}
}