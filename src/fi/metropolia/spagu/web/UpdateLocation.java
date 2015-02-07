package fi.metropolia.spagu.web;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import fi.metropolia.spagu.data.DataHandler;

public class UpdateLocation extends HttpServlet {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	//private RequestDispatcher homeJsp;
	
	private DataHandler dataHandler;
	private String tagID = "";
	private String WLANData = "";
	
	private String instantRoom;
	private WlanSimilarity sim; 	
	
	@Override
	public void init() throws ServletException {
		super.init();		 dataHandler = new DataHandler();
		 sim = new WlanSimilarity();
		 sim.doIndex();;
	}		

	/**
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		ServletContext context = config.getServletContext();
		homeJsp = context.getRequestDispatcher("/WEB-INF/jsp/home.jsp");
	}*/

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		logger.debug("UpdateLocation.doPost()");
		/**	
		req.setAttribute("message", "Bye");
		homeJsp.forward(req, resp);		*/
		
		resp.setStatus(HttpServletResponse.SC_OK);
		
		OutputStream os = resp.getOutputStream();
		resp.setContentType("text/plain");
		
		// read the result
		DataInputStream is = new DataInputStream(req.getInputStream());
			
		String[] tokens = dataHandler.convertStreamToString(is).split("\n", 2);
		
		tagID = tokens[0];
		WLANData =  tokens[1];;
		
		instantRoom = dataHandler.responseContent(WLANData).toString();
		
		os.write(instantRoom.getBytes());
		os.flush();
		os.close();

		dataHandler.writeDataToLog(tagID, instantRoom, dataHandler.dateToString(new Date()));		
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		doPost(req, resp);
	}

}
