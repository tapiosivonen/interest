package interest;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class BOFEuriborFetcher
{
    private Document BOFEuriborDocument;

    public static final String BOF_EURIBOR_URI
    //="http://www.suomenpankki.fi/en/Statistics/interest-rates/"
	= "http://www.suomenpankki.fi/fi/Tilastot/korot/";
    public static final String EURIBOR_TABLE_CLASS
	= "tstyle1 zebra";

    private int parseDuration(String durationText)
    {
	String[] parts
	    = durationText
	    .trim()
	    .split(" ");
	int count
	    = Integer.parseInt(parts[0]);
	int multiplier
	    = "kk".equals(parts[1])
	    ? 30 : 7; //kk -> month, 30 days; others -> week, 7 days
	return count*multiplier;
    }

    private Date parseDate(String dateText)
    {
	SimpleDateFormat fiDate
	    = new SimpleDateFormat("d.M.yyyy");
	try {
	    return fiDate.parse(dateText.trim());
	}
	catch(ParseException pe) {
	    return null;
	}
    }

    private double parseRate(String rateText)
    {
	NumberFormat fiDouble
	    = NumberFormat
	    .getInstance(new Locale("fi","FI"));
	try {
	    return fiDouble
		.parse(rateText.trim())
		.doubleValue();
	}
	catch(ParseException pe) {
	    return Double.NaN;
	}
    }

    private Stream<InterestRateEntry>
	streamInterestRateRow(Element row,
			      final int[] durationDays)
    {
	Elements rowChildren
	    = row.children();
	final Date rowDate
	    = parseDate(rowChildren.get(0).text());
	if(null == rowDate)
	    return Stream.empty();
	return
	    IntStream
	    .range(0, durationDays.length)
	    .parallel()
	    .mapToObj(columnIndex ->
		      new InterestRateEntry
		      ("EURIBOR",
		       rowDate,
		       parseRate(rowChildren
				 .get(columnIndex+1)
				 .text()
				 ),
		       durationDays[columnIndex]
		       )
		      )
	    .filter(rateEntry ->
		    null != rateEntry.getDate()
		    );
    }

    public BOFEuriborFetcher()
	throws IOException
    {
	BOFEuriborDocument
	    = Jsoup.connect(BOF_EURIBOR_URI).get();
    }
    
    public Stream<InterestRateEntry> interestRateEntryStream()
    {
	Element euriborTable
	    = BOFEuriborDocument
	    //.body()
	    .getElementsByClass(EURIBOR_TABLE_CLASS)
	    .get(0); //first table with class EURIBOR_TABLE_CLASS
	Elements euriborTableRows
	    = euriborTable
	    .getElementsByTag("tr");

	Elements durationHeaders
	    = euriborTableRows
	    .get(1)
	    .getElementsByTag("th");

	int dataColumns
	    = durationHeaders.size()-1;
	int[] durationDays
	    = new int[dataColumns];
	Arrays
	    .parallelSetAll(durationDays,
			    dayIndex ->
			    parseDuration(durationHeaders
					  .get(dayIndex+1)
					  .text()
					  )
			    );
	return
	    euriborTableRows
	    .subList(2, euriborTableRows.size())
	    .parallelStream()
	    .flatMap(row ->
		     streamInterestRateRow(row, durationDays)
		     );
    }

    private NavigableSet<InterestRateEntry>
	durationComparingInterestRateEntrySet(InterestRateEntry... initialEntries)
    {
	return durationComparingInterestRateEntrySet(Arrays.asList(initialEntries));
    }
    private NavigableSet<InterestRateEntry>
	durationComparingInterestRateEntrySet(Collection<InterestRateEntry> initialEntries)
    {
	NavigableSet<InterestRateEntry> result
	    = new ConcurrentSkipListSet<InterestRateEntry>
	    ((left, right)
	     -> left.getDurationDays() - right.getDurationDays()
	     );
	result.addAll(initialEntries);
	return result;
    }

    private java.util.stream.Collector<InterestRateEntry,?,NavigableSet<InterestRateEntry>> toSetByDuration()
    {
	return
	    Collectors.
	    collectingAndThen(Collectors.toList(),
			      list -> durationComparingInterestRateEntrySet(list)
			      );
    }
    
    public NavigableMap<Date, NavigableSet<InterestRateEntry>> interestRateEntryMap()
    {
	return
	    new TreeMap<Date, NavigableSet<InterestRateEntry>>
	    (
	     interestRateEntryStream()
	     .filter(rateEntry ->
		     (null != rateEntry.getDate())
		     )
	     .collect(Collectors
		      .groupingByConcurrent(rateEntry ->
					    rateEntry.getDate(),
					    toSetByDuration()
					    )
		      )
	     );
    }
    
    public Collection<InterestRateEntry> interestRateEntries()
    {
	return
	    interestRateEntryStream()
	    .collect(Collectors.toList());
    }

    public static void main(String[] args)
	throws IOException
    {
	new BOFEuriborFetcher()
	    .interestRateEntries()
	    .forEach(e -> System.out.println(e));
    }
}
