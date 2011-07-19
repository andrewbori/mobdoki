package mobdoki.client;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * Ikonokat is megjelenito lista
 */
public class IconListArrayAdapter<T> extends ArrayAdapter<T> {

	private Activity context;
	private int rowResource;
	private int labelResource;
	private int iconResource;
	private int[] icons;		// ikonok
	private T[] items;			// listaelemek

	public IconListArrayAdapter(Activity context,
          					  	int rowResource, int iconResource, int labelResource,
          					  	int[] icons, T[] items) {
		super(context, rowResource, items);
		this.context = context;
		this.rowResource = rowResource;
	    this.labelResource = labelResource;
	    this.iconResource = iconResource;
	    this.icons = icons;
	    this.items = items;
	}

    @Override  
    public View getView(int position, View reusableView, ViewGroup parent) {  
    	if (reusableView == null) {  
    		LayoutInflater inflater = context.getLayoutInflater();  
    		reusableView = inflater.inflate(this.rowResource, null);  
    	}  
      
    	TextView label = (TextView) reusableView.findViewById(this.labelResource);  
    	ImageView icon = (ImageView) reusableView.findViewById(this.iconResource);  
      
    	label.setText((String) this.items[position]);  
    	icon.setImageResource(this.icons[position]);  
      
    	return reusableView;  
    }
}
