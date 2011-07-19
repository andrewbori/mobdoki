package mobdoki.client;

import java.util.Date;
import java.util.List;

import org.achartengine.chart.PointStyle;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.MultipleCategorySeries;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

/**
 * An abstract class for the charts to extend.
 */
public abstract class AbstractChart  {

	/**
	 * Builds an XY multiple series renderer.
	 * 
	 * @param colors the series rendering colors
	 * @param styles the series point styles
	 * @return the XY multiple series renderers
	 */
	protected XYMultipleSeriesRenderer buildRenderer(int[] colors, PointStyle[] styles) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		renderer.setAxisTitleTextSize(16);
		renderer.setChartTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setPointSize(5f);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
	    int length = colors.length;
	    for (int i = 0; i < length; i++) {
		    XYSeriesRenderer r = new XYSeriesRenderer();
		    r.setColor(colors[i]);
		    r.setPointStyle(styles[i]);
		    renderer.addSeriesRenderer(r);
	    }
	    return renderer;
	}
  
	protected XYMultipleSeriesRenderer buildRenderer(int colors, PointStyle styles) {
	    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	    renderer.setAxisTitleTextSize(16);
	    renderer.setChartTitleTextSize(20);
	    renderer.setLabelsTextSize(15);
	    renderer.setLegendTextSize(15);
	    renderer.setPointSize(5f);
	    renderer.setMargins(new int[] { 20, 30, 15, 0 });
	     
	    XYSeriesRenderer r = new XYSeriesRenderer();
	    r.setColor(colors);
	    r.setPointStyle(styles);
	    renderer.addSeriesRenderer(r);
	    
	    return renderer;
	}

	/**
	 * Sets a few of the series renderer settings.
	 * 
	 * @param renderer the renderer to set the properties to
	 * @param title the chart title
	 * @param xTitle the title for the X axis
	 * @param yTitle the title for the Y axis
	 * @param xMin the minimum value on the X axis
	 * @param xMax the maximum value on the X axis
	 * @param yMin the minimum value on the Y axis
	 * @param yMax the maximum value on the Y axis
	 * @param axesColor the axes color
	 * @param labelsColor the labels color
	 */
	protected void setChartSettings(XYMultipleSeriesRenderer renderer, String title, String xTitle,
	String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
	    renderer.setChartTitle(title);
	    renderer.setXTitle(xTitle);
	    renderer.setYTitle(yTitle);
	    renderer.setXAxisMin(xMin);
	    renderer.setXAxisMax(xMax);
	    renderer.setYAxisMin(yMin);
	    renderer.setYAxisMax(yMax);
	    renderer.setAxesColor(axesColor);
	    renderer.setLabelsColor(labelsColor);
	}

	/**
	 * Builds an XY multiple time dataset using the provided values.
	 * 
	 * @param titles the series titles
	 * @param xValues the values for the X axis
	 * @param yValues the values for the Y axis
	 * @return the XY multiple time dataset
	 */
	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues,
	List<double[]> yValues) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    int length = titles.length;
	    for (int i = 0; i < length; i++) {
		    TimeSeries series = new TimeSeries(titles[i]);
		    Date[] xV = xValues.get(i);
		    double[] yV = yValues.get(i);
		    int seriesLength = xV.length;
		    for (int k = 0; k < seriesLength; k++) {
		    	series.add(xV[k], yV[k]);
		    }
	        dataset.addSeries(series);
	    }
	    return dataset;
	}
  
	protected XYMultipleSeriesDataset buildDateDataset(String title, List<Date> xValues, List<Double> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		    	   
	    TimeSeries series = new TimeSeries(title);
	    List<Date> xV =  xValues;
	    List<Double> yV = yValues;
	  	  
	    int seriesLength = xV.size();
	    for (int k = 0; k < seriesLength; k++) {
	      series.add(xV.get(k), yV.get(k));
	    }
	    dataset.addSeries(series);
		    
		return dataset;
	}
  
	protected XYMultipleSeriesDataset buildDateDataset(String[] title, List<Date> xValues,
	List<Integer> yValues, List<Integer> y2Values) {
	    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	       
	    TimeSeries series = new TimeSeries(title[0]);
	    TimeSeries series2 = new TimeSeries(title[1]);
	    List<Date> xV =  xValues;
	    List<Integer> yV = yValues;
	    List<Integer> y2V = y2Values;
	    	  
	    int seriesLength = xV.size();
	    for (int k = 0; k < seriesLength; k++) {
		    series.add(xV.get(k), yV.get(k));
		    series2.add(xV.get(k), y2V.get(k));
	    }
	    dataset.addSeries(series);
	    dataset.addSeries(series2);
     
	    return dataset;
	}
  
	protected XYMultipleSeriesDataset buildDateDataset(String[] title, List<Date> xValues,
	List<Integer> yValues, List<Integer> y2Values, List<Integer> y3Values, List<Double> y4Values,
	List<Double> y5Values, List<Integer> y6Values) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	       
	    TimeSeries series = new TimeSeries(title[0]);
	    TimeSeries series2 = new TimeSeries(title[1]);
	    TimeSeries series3 = new TimeSeries(title[2]);
	    TimeSeries series4 = new TimeSeries(title[3]);
	    TimeSeries series5 = new TimeSeries(title[4]);
	    TimeSeries series6 = new TimeSeries(title[5]);
	    List<Date> xV =  xValues;
	    List<Integer> yV = yValues;
	    List<Integer> y2V = y2Values;
	    List<Integer> y3V = y3Values;
	    List<Double> y4V = y4Values;
	    List<Double> y5V = y5Values;
	    List<Integer> y6V = y6Values;
	    	  
	    int seriesLength = xV.size();
	    for (int k = 0; k < seriesLength; k++) {
		    series.add(xV.get(k), yV.get(k));
		    series2.add(xV.get(k), y2V.get(k));
		    series3.add(xV.get(k), y3V.get(k));
		    series4.add(xV.get(k), y4V.get(k));
		    series5.add(xV.get(k), y5V.get(k));
		    series6.add(xV.get(k), y6V.get(k));     
	    }
	    dataset.addSeries(series);
	    dataset.addSeries(series2);
	    dataset.addSeries(series3);
	    dataset.addSeries(series4);
	    dataset.addSeries(series5);
	    dataset.addSeries(series6);
	    
	    return dataset;
	}
  
	protected XYMultipleSeriesDataset buildDateDatase(String title, List<Date> xValues, List<Integer> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	    
	    TimeSeries series = new TimeSeries(title);
	    List<Date> xV =  xValues;
	    List<Integer> yV = yValues;
	    	  
	    int seriesLength = xV.size();
	    for (int k = 0; k < seriesLength; k++) {
	    	series.add(xV.get(k), yV.get(k));
	    }
	    dataset.addSeries(series);
	    
	    return dataset;
	}

	/**
	 * Builds a category series using the provided values.
	 * 
	 * @param titles the series titles
	 * @param values the values
	 * @return the category series
	 */
	protected CategorySeries buildCategoryDataset(String title, double[] values) {
		CategorySeries series = new CategorySeries(title);
		int k = 0;
	    for (double value : values) {
	    	series.add("Project " + ++k, value);
	    }

	    return series;
	}

	/**
	 * Builds a multiple category series using the provided values.
	 * 
	 * @param titles the series titles
	 * @param values the values
	 * @return the category series
	 */
	protected MultipleCategorySeries buildMultipleCategoryDataset(String title,
	List<String[]> titles, List<double[]> values) {
		MultipleCategorySeries series = new MultipleCategorySeries(title);
		int k = 0;
		for (double[] value : values) {
			series.add(2007 + k + "", titles.get(k), value);
			k++;
		}
		return series;
	}

	/**
	 * Builds a category renderer to use the provided colors.
	 * 
	 * @param colors the colors
	 * @return the category renderer
	 */
	protected DefaultRenderer buildCategoryRenderer(int[] colors) {
		DefaultRenderer renderer = new DefaultRenderer();
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(15);
		renderer.setMargins(new int[] { 20, 30, 15, 0 });
		for (int color : colors) {
			SimpleSeriesRenderer r = new SimpleSeriesRenderer();
			r.setColor(color);
			renderer.addSeriesRenderer(r);
		}
		return renderer;
	}
}
