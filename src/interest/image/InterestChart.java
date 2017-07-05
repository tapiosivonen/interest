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
            (series.stream()
             .map(interestEntry ->
                  (interestEntry.getLabel()
                   + " "
                   +myFormatter
                   .format(interestEntry
                           .getDate())
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
            = new InterestRateEntry
            (label, null, 0.0d, 0);
        xDayValues.add((double) origo.getDurationDays());
        yRateValues.add(origo.getRate());

        InterestRateEntry current = origo;
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
            .setXYSeriesRenderStyle
            (XYSeries
             .XYSeriesRenderStyle
             .Line
             );

        return result;
    }

    private List<? extends Collection<? extends InterestRateEntry>>
        series;
    private String label;

    @SafeVarargs
    public InterestChart(Collection<? extends InterestRateEntry>... series)
    {
        this(Arrays.asList(series));
    }
    public
        InterestChart(Collection<
                      ? extends Collection<
                      ? extends InterestRateEntry>> series)
    {
        this.series
            = new ArrayList<Collection<
                ? extends InterestRateEntry>>(series);
        label = "Interest Rates";
    }
    public XYChart
        interestAreaChart()
    {
        XYChart target
            = new XYChartBuilder()
            .title(label)
            .xAxisTitle("duration days")
            .yAxisTitle("percentage rate")
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
            = new InterestChart
            (new BOFEuriborFetcher()
             .interestRateEntryMap()
             .values()
             );
        XYChart chart
            = ic.interestAreaChart();
        new SwingWrapper<XYChart>(chart).displayChart();
        return;
    }
}
