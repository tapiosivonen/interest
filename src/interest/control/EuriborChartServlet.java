package interest.control;

import javax.servlet.annotation.*;
import javax.servlet.ServletException;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.imageio.ImageIO;
import interest.BOFEuriborFetcher;
import interest.image.InterestChart;

@WebServlet("/latest_euribor_chart.png")
public class EuriborChartServlet
    extends HttpServlet
{
    public EuriborChartServlet() {
	super();
    }

    private void internalErrorResponse(HttpServletResponse resp)
	throws IOException
    {
	resp.sendError(resp.SC_INTERNAL_SERVER_ERROR);
	return;
    }
    
    public void doGet(HttpServletRequest req,
		 HttpServletResponse resp)
	throws ServletException,
	       IOException
    {
	boolean success = false;
	try {
	    InterestChart ic
		= new InterestChart
		(new BOFEuriborFetcher()
		 .interestRateEntryMap()
		 .lastEntry()
		 .getValue()
		 );

	    resp.setStatus(resp.SC_OK);
	    resp.setContentType("image/png");
	    ImageIO.write(ic.interestAreaChartImage(),
			  "PNG",
			  resp.getOutputStream()
			  );

	    success = true;
	}
	finally {
	    if(!success)
		internalErrorResponse(resp);
	}
	return;
    }
}
