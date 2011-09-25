package mobdoki.client;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentListArrayAdapter <T> extends ArrayAdapter<T> {

	private Activity context;
	private int rowResource;
	private int senderResource;
	private int dateResource;
	private int commentResource;
	private int imageResource;
	private ArrayList<T> senders;
	private ArrayList<T> dates;
	private ArrayList<T> comments;
	private HashMap<String,Bitmap> images;

	public CommentListArrayAdapter(Activity context,
          					  	   int rowResource,
          					  	   int senderResource, int dateResource, int commentResource, int imageResource,
          					  	   ArrayList<T> senders, ArrayList<T> dates, ArrayList<T> comments, HashMap<String,Bitmap> images) {
		super(context, rowResource, senders);
		this.context = context;
		this.rowResource = rowResource;	    
	    this.senderResource = senderResource;
	    this.dateResource = dateResource;
	    this.commentResource = commentResource;
	    this.imageResource = imageResource;
	    this.senders = senders;
	    this.dates = dates;
	    this.comments = comments;
	    this.images = images;
	}
	
	public void setImages (HashMap<String,Bitmap> images) {
		this.images = images;
	}

    @Override  
    public View getView(int position, View reusableView, ViewGroup parent) {  
    	if (reusableView == null) {  
    		LayoutInflater inflater = context.getLayoutInflater();  
    		reusableView = inflater.inflate(this.rowResource, null);  
    	}  
      
    	TextView sender = (TextView) reusableView.findViewById(this.senderResource);
    	TextView date = (TextView) reusableView.findViewById(this.dateResource);
    	TextView comment = (TextView) reusableView.findViewById(this.commentResource);
    	ImageView imageicon = (ImageView) reusableView.findViewById(this.imageResource);
    	
    	String senderName = (String) this.senders.get(position);
    	sender.setText(senderName);
		date.setText((String)this.dates.get(position));
		comment.setText((String) this.comments.get(position));
    	
    	if (images.containsKey(senderName)) {
    		imageicon.setImageBitmap(images.get(senderName));
    	} else {
    		imageicon.setImageResource(R.drawable.icon);
    	}
      
    	return reusableView;  
    }
}
