package fi.metropolia.spagu.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.obsearch.exception.IllegalIdException;
import net.obsearch.exception.OBException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import fi.metropolia.spagu.web.WlanSimilarity;


public class UploadServlet extends HttpServlet {

//	private Logger logger = Logger.getLogger(this.getClass());
	
	WlanSimilarity sim;
	
	@Override
	public void init() throws ServletException {
		super.init();
		sim = new WlanSimilarity();
		sim.doIndex();
	}
	
//	WlanSimilarity sim = new WlanSimilarity();
//	sim.doIndex();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
//		logger.debug("UploadServlet.doGet()");

		resp.setStatus(HttpServletResponse.SC_OK);
		String method = req.getParameter("method");
		
		Logger.getLogger("net.obsearch.*").setLevel(Level.OFF);
		
//		WlanSimilarity.init();	
	
		if (method.equals("getRoom")) {
			
			PrintWriter writer = resp.getWriter();//	
										
//			File f = new File("C:\\Users\\asentaja\\Desktop\\spagu\\test.file");
			
			File f = new File("C:\\Users\\asentaja\\workspace\\SimilarityWeb\\test.file");
			writer.println("<h1>Test data content</h1>" + f.getAbsolutePath());
			
			writer.println("<h1>Test data path</h1>" + readFile(f.getAbsolutePath()));
			
			
			try {
			
//				writer.println("<h1>Room: </h1>" + sim.getRoom(new File("C:\\Users\\asentaja\\workspace\\SimilarityWeb\\test.file")));
				writer.println("<h1>Room: </h1>" + sim.getRoom(f));
			
			} catch (Exception e) {
				e.printStackTrace();
			}	
			
	/**		writer.println("<h2>Data path</h2>" + f.getAbsolutePath());
			
			try {
				writer.println("<h2>Room</h2>" + sim.getRoom(new File("test.file")));
			} catch (IllegalIdException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (OBException e) {
				e.printStackTrace();
			} */
 					
			
		}

		
	}

	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}

}
