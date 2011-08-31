package mobdoki.client;

import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;


public class HealthChart extends AbstractChart {
	private ArrayList<Double> weight;
	private ArrayList<Date> date;
	private ArrayList<Integer> mood;
	private ArrayList<Integer> bp1;
	private ArrayList<Integer> bp2;
	private ArrayList<Integer> pulse;
	private ArrayList<Double> temperature;

	
	public Intent executeAll(Context context, ArrayList<Integer> b1, ArrayList<Integer> b2, ArrayList<Integer> p,
							 ArrayList<Double> w, ArrayList<Double> t, ArrayList<Integer> m, ArrayList<Date> d) {
		weight = w;
		bp1 = b1;
		bp2 = b2;
		pulse = p;
		temperature = t;
		mood = m;
		date = d;
	  
		String[] titles = new String[] { "V�rnyom�s fels�", "Pulzus", "V�rnyom�s als�", "Testt�meg", "Testh�m�rs�klet", "K�z�rzet" };
		
		int length = date.size();
		int[] colors = new int[] { Color.YELLOW, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.RED, Color.GREEN };
		PointStyle[] styles = new PointStyle[] { PointStyle.POINT, PointStyle.POINT, PointStyle.POINT,
												 PointStyle.POINT,PointStyle.POINT,PointStyle.POINT };
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "All in one diagram", "D�tum", "�rt�kek", date.get(0).getTime(),
		    date.get(length-1).getTime(), 1, 200, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, date,bp1,pulse,bp2,weight,temperature,mood), renderer, "yyyy.MM.dd.");
	}
		  
	public Intent executeBP(Context context,ArrayList<Integer> b1, ArrayList<Integer> b2, ArrayList<Date> d) {
		bp1 = b1;
		bp2 = b2;
		date = d;
	    String[] titles =new String[] {"V�rnyom�s fels� �rt�k", "V�rnyom�s als� �rt�k"};
		
		int length = b1.size();
		int[] colors = new int[] {Color.YELLOW, Color.BLUE };
		PointStyle[] styles = new PointStyle[] {PointStyle.POINT, PointStyle.POINT};
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "V�rnyom�s diagram", "D�tum", "Nyom�s (Hgmm)", date.get(0).getTime(),
		    date.get(length-1).getTime(), 40, 200, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, date, bp1,bp2), renderer, "yyyy.MM.dd.");
	}
	
	public Intent executePulse(Context context, ArrayList<Integer> p, ArrayList<Date> d) {
		pulse = p;
		date = d;
	    String titles ="Pulzus";
		
		int length = p.size();
		int colors = Color.YELLOW;
		PointStyle styles = PointStyle.POINT;
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "Pulzus diagram", "D�tum", "Pulzus (BPM)", date.get(0).getTime(),
		    date.get(length-1).getTime(), 40, 120, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDatase(titles, date, pulse), renderer, "yyyy.MM.dd.");
	}
	
	public Intent executeWeight(Context context,ArrayList<Double> w, ArrayList<Date> d) {
		weight = w;
		date = d;
		String titles ="Testt�meg";
	
		int length = w.size();
		int colors = Color.YELLOW;
		PointStyle styles = PointStyle.POINT;
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "Testt�meg diagram", "D�tum", "T�meg (kg)", date.get(0).getTime(),
		    date.get(length-1).getTime(), 20, 150, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, date, weight), renderer, "yyyy.MM.dd.");
	  }
		  
	public Intent executeTemperature(Context context, ArrayList<Double> t, ArrayList<Date> d) {
		temperature = t;
		date = d;
	    String titles ="Testh�m�rs�klet";
		
		int length = t.size();
		int colors = Color.YELLOW;
		PointStyle styles = PointStyle.POINT;
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "Testh�m�rs�klet diagram", "D�tum", "H�m�rs�klet (�C)", date.get(0).getTime(),
		    date.get(length-1).getTime(), 35.0, 42.0, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDataset(titles, date, temperature), renderer, "yyyy.MM.dd.");
	}
	
	public Intent executeMood(Context context, ArrayList<Integer> m, ArrayList<Date> d) {
		mood = m;
		date = d;
	    String titles ="K�z�rzet";
		
		int length = m.size();
		int colors = Color.YELLOW;
		PointStyle styles = PointStyle.POINT;
		XYMultipleSeriesRenderer renderer = buildRenderer(colors, styles);
		setChartSettings(renderer, "K�z�rzet diagram", "D�tum", "K�z�rzet", date.get(0).getTime(),
		    date.get(length-1).getTime(), 0, 10, Color.GRAY, Color.LTGRAY);
		renderer.setXLabels(3);
		renderer.setYLabels(10);
		renderer.setZoomEnabled(true, false);
		renderer.setDisplayChartValues(true);
		return ChartFactory.getTimeChartIntent(context, buildDateDatase(titles, date, mood), renderer, "yyyy.MM.dd.");
	}
}

