/*
 * Copyright 2010, 2011 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.server.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class GeoCoder
 */
public class GeoCoder extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoCoder() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		URL url = null;
		String lat = request.getParameter("lat");
		String lon = request.getParameter("lon");
		String name = request.getParameter("name");
// 	    right now, i'm not setting the encoding, because there is a problem with that on the server, see: 
//      http://trac.openstreetmap.org/ticket/3088
//		response.setCharacterEncoding("UTF-8");
//		response.setContentType("application/json; charset=UTF-8");
		response.setContentType("application/json;");
		PrintWriter writer = response.getWriter();
		String base_url = "http://nominatim.openstreetmap.org/";
		try {
			if (name != null) {
				name = URLDecoder.decode(name, "UTF-8");
				name = URLEncoder.encode(name, "UTF-8");
				// forward geocoding
				url = new URL (base_url + "search?format=json&q=" + name);
			} else if (lat != null && lon != null) {
				// reverse geocoding
				url = new URL (base_url + "reverse?format=json&addressdetails=1&lat=" + lat + "&lon=" + lon);
			}				
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream())); 
			String str; 
			while ((str = in.readLine()) != null) { 
				writer.write(str);
			} 
			in.close();  	
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
//	public static void main(String[] args) {
//		GeoCoder t = new GeoCoder();
//		HttpServletRequest request = new HttpServletRequest();
//		HttpServletResponse response = new HttpServletResponse();
//		t.doGet(request, response);
//		
//	}
}
