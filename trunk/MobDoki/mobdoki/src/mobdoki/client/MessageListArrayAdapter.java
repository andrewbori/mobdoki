package mobdoki.client;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageListArrayAdapter<T> extends ArrayAdapter<T> {

	private Activity context;
	private int rowResource;
	private int subjectResource;
	private int senderResource;
	private int dateResource;
	private int mailiconResource;
	private int imageiconResource;
	private ArrayList<T> subjects;
	private ArrayList<T> senders;
	private ArrayList<T> dates;
	private ArrayList<Boolean> viewed;
	private ArrayList<Boolean> answered;
	private ArrayList<Integer> images;

	public MessageListArrayAdapter(Activity context,
          					  	   int rowResource,
          					  	   int subjectResource, int senderResource, int dateResource, int mailiconResource, int imageiconResource,
          					  	   ArrayList<T> subjects, ArrayList<T> senders, ArrayList<T> dates, ArrayList<Boolean> viewed, ArrayList<Boolean> answered, ArrayList<Integer> images) {
		super(context, rowResource, subjects);
		this.context = context;
		this.rowResource = rowResource;
	    this.subjectResource = subjectResource;
	    this.senderResource = senderResource;
	    this.dateResource = dateResource;
	    this.mailiconResource = mailiconResource;
	    this.imageiconResource = imageiconResource;
	    this.subjects = subjects;
	    this.senders = senders;
	    this.dates = dates;
	    this.viewed = viewed;
	    this.answered = answered;
	    this.images = images;
	}

    @Override  
    public View getView(int position, View reusableView, ViewGroup parent) {  
    	if (reusableView == null) {  
    		LayoutInflater inflater = context.getLayoutInflater();  
    		reusableView = inflater.inflate(this.rowResource, null);  
    	}  
      
    	TextView subject = (TextView) reusableView.findViewById(this.subjectResource);
    	TextView sender = (TextView) reusableView.findViewById(this.senderResource);
    	TextView date = (TextView) reusableView.findViewById(this.dateResource);
    	ImageView mailicon = (ImageView) reusableView.findViewById(this.mailiconResource);
    	ImageView imageicon = (ImageView) reusableView.findViewById(this.imageiconResource);
    	
    	subject.setText((String) this.subjects.get(position));  
    	sender.setText((String) this.senders.get(position));
		date.setText((String)this.dates.get(position));
    	
    	if (this.answered.get(position)) {
    		mailicon.setImageResource(R.drawable.mail_answered);
    		subject.setTypeface(null, Typeface.NORMAL);
    		sender.setTypeface(null, Typeface.NORMAL);
    		date.setTypeface(null, Typeface.NORMAL);
    	}
    	else if (!this.viewed.get(position)) {
    		mailicon.setImageResource(R.drawable.mail_unviewed);
    		subject.setTypeface(null, Typeface.BOLD);
    		sender.setTypeface(null, Typeface.BOLD);
    		date.setTypeface(null, Typeface.BOLD);
    	}
    	else {
    		mailicon.setImageResource(R.drawable.mail_viewed);
    		subject.setTypeface(null, Typeface.NORMAL);
    		sender.setTypeface(null, Typeface.NORMAL);
    		date.setTypeface(null, Typeface.NORMAL);
    	}
    	if (images.get(position)!=0) {
    		imageicon.setVisibility(ImageView.VISIBLE);
    	} else {
    		imageicon.setVisibility(ImageView.GONE);
    	}
      
    	return reusableView;  
    }
}
