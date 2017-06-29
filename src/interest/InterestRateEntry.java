package interest;

import java.util.Date;
import java.text.*;

public class InterestRateEntry
{
    private String label;
    private Date myDate;
    private double rate;
    private int durationDays;
    public InterestRateEntry(String label,
			     Date d,
			     double rate,
			     int durationDays)
    {
	this.label = label;
	if(null == d) {
	    myDate = null;
	} else {
	    this.myDate = new Date(d.getTime());
	}
	this.rate = rate;
	this.durationDays = durationDays;
    }
    public String toString()
    {
	SimpleDateFormat sdf
	    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	return
	    "{"+getLabel()
	    +";"+((null==getDate())?"":sdf.format(getDate()))
	    +";"+getRate()
	    +";"+getDurationDays()
	    +"}";
    }
    public String getLabel()
    {
	return label;
    }
    public Date getDate()
    {
	if(null == myDate)
	    return null;
	return new Date(myDate.getTime());
    }
    public double getRate()
    {
	return rate;
    }
    public int getDurationDays()
    {
	return durationDays;
    }
}
