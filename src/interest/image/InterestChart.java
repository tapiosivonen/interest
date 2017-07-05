package interest.image;

import interest.*;

import java.awt.image.*;
import org.knowm.xchart.*;
import org.knowm.xchart.style.*;
import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.text.*;

import javax.swing.*;

public class InterestChart
{
    public static final String myFormat = "yyyy-MM-dd";
    private static
	String seriesName(Collection<? extends InterestRateEntry> series)
    {
	SimpleDateFormat myFormatter
	    = new SimpleDateFormat(myFormat);
	TreeSet<String> labels
	    = new TreeSet<String>
	    (series.parallelStream()
	     .map(interestEntry ->
		  (interestEntry.getLabel()
		   + " "
		   +myFormatter.format(interestEntry.getDate()
				       )
		   )
		  )
	     .collect(Collectors.toList()
		      )
	     );
	StringBuilder sb = new StringBuilder();
	boolean first = true;
	for(String s : labels) {
	    if(first)
		first = false;
	    else
		sb.append(" ");
	    sb.append(s);
	}
	return sb.toString();
    }

    private static XYSeries
    	addSeries(XYChart target,
		  Collection<? extends InterestRateEntry> series)
    //private static CategorySeries
    //	addSeries(CategoryChart target, Collection<InterestRateEntry> series)
    {
	String label
	    = seriesName(series);
	TreeSet<InterestRateEntry> myEntries
	    = new TreeSet<InterestRateEntry>
	    (Comparator.comparing(InterestRateEntry::getDurationDays));
	myEntries.addAll(series);
	ArrayList<Double> xDayValues
	    = new ArrayList<Double>(myEntries.size());
	ArrayList<Double> yRateValues
	    = new ArrayList<Double>(myEntries.size());
	InterestRateEntry origo
	    = new InterestRateEntry(label,
				    null,
				    0.0d,
				    0
				    );
	InterestRateEntry current = origo;
	xDayValues.add((double) origo.getDurationDays());
	yRateValues.add(origo.getRate());
	for(InterestRateEntry latter : myEntries) {
	    double currentLogRate
		= Math.log1p(current.getRate());
	    double latterLogRate
		= Math.log1p(latter.getRate());
	    double differenceLogRate
		= latterLogRate - currentLogRate;
	    int durationDayDifference
		= latter.getDurationDays()
		- current.getDurationDays();
	    double averageLogRate
		= differenceLogRate / durationDayDifference;
	    double averageRate
		= Math.expm1(averageLogRate);
	    
	    xDayValues.add((double) current.getDurationDays());
	    yRateValues.add(averageRate);
	    
	    xDayValues.add((double) latter.getDurationDays());
	    yRateValues.add(averageRate);

	    current = latter;
	}
	
	XYSeries result
	    = target.addSeries(label, xDayValues, yRateValues);
	result
	    .setXYSeriesRenderStyle(XYSeries
				    .XYSeriesRenderStyle
				    .Line
				    );
	
	/*
	CategorySeries result
	    = target.addSeries(label, xDayValues, yRateValues);
	result
	    .setChartCategorySeriesRenderStyle(CategorySeries
					  .CategorySeriesRenderStyle
					  .SteppedBar
					  );
	*/
	return result;
    }

    private List<? extends Collection<? extends InterestRateEntry>>
	series;
    private String label;

    public InterestChart(Collection<? extends InterestRateEntry>... series)
    {
	this(Arrays.asList(series));
    }
    public
	InterestChart(Collection<
		      ? extends Collection<
		      ? extends InterestRateEntry>> series)
    {
	this.series = new ArrayList(series);
	label = "Interest Rates";
    }
    /*    
    public static XYChart
	interestAreaChart(String label,
			  Collection<InterestRateEntry>... series)
    {
	return interestAreaChart(label, Arrays.asList(series));
    }
    public static XYChart
	interestAreaChart(String label,
			  Collection<? extends Collection<InterestRateEntry>> series)
    */
    public XYChart
	interestAreaChart()
    /*
    public static CategoryChart
	interestAreaChart(String label,
			  Collection<InterestRateEntry>... series)
    */
    {
	XYChart target
	    = new XYChartBuilder()
	    .title(label)
	    //= new CategoryChartBuilder()
	    .xAxisTitle("duration days")
	    .yAxisTitle("rate")
	    //	    .theme(Styler.ChartTheme.GGPlot2)
	    .build();
	for(Collection<? extends InterestRateEntry>
		entryCollectionEntry : series) {
	    addSeries(target,entryCollectionEntry);
	}
	return target;
    }

    public static void main(String args[])
	throws IOException
    {
	InterestChart ic
	    = new InterestChart(
				new BOFEuriborFetcher()
				.interestRateEntryMap()
				.values()
				);
	XYChart chart
	    //CategoryChart chart
	    = ic.interestAreaChart();
	//"DemoChart",
	//.lastEntry().getValue()
	//);
	//JFrame frame
	//=
	new SwingWrapper(chart).displayChart();
	    /*
	frame.pack();
	frame.setVisible(true);
	    */
	return;
    }
    
}
