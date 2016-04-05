/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.pchmelar.ddw.homework1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import org.apache.commons.io.IOUtils;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author filletzz
 */
@WebServlet(name = "MainServlet", urlPatterns = {"/MainServlet"})
public class MainServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Simpsons Episode Analyzer</title>");
            out.println("<link rel=\"stylesheet\" href=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\">");
            out.println("<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js\"></script>");
            out.println("<script src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js\"></script>");
            out.println("</head>");
            out.println("<body style=\"background-color: #A0D1F5\">");
            
            out.println("<div class=\"container\">");
            out.println("<br>");
            out.println("<br>");
            out.println("<a href=\"/ddw-homework1/MainServlet\"><img src=\"http://www.returndates.com/backgrounds/simpsons.logo.png\" class=\"img-responsive center-block\" style=\"width: 50%; height: 50%\"></a>");
            out.println("<br>");
            out.println("<br>");

            //GET JSON of all simpsons episodes
            String urlString = "http://api.tvmaze.com/singlesearch/shows?q=simpsons&embed=episodes";
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            String JSON = IOUtils.toString(is, "UTF-8");

            //print season/episode/name for every episode in JSON
            JsonElement jelement = new JsonParser().parse(JSON);
            JsonObject jobject = jelement.getAsJsonObject();
            jobject = jobject.getAsJsonObject("_embedded");
            JsonArray jarray = jobject.getAsJsonArray("episodes");
            out.println("<table class=\"table table-bordered table-hover\" style=\"background-color: #FFFFFF\">");
            out.println("<thead><tr><th>Season</th><th>Episode</th><th>Name</th></tr></thead><tbody>");
            for (int i = 0; i < jarray.size(); i++){
                
                jobject = jarray.get(i).getAsJsonObject();
                String season = jobject.get("season").toString();
                if (season.length() == 1) season = "0" + season;
                String episode = jobject.get("number").toString();
                if (episode.length() == 1) episode = "0" + episode;
                
                out.println("<tr>");
                out.println("<td>" + season + "</td>");
                out.println("<td>" + episode + "</td>");
                out.println("<td><a href=\"/ddw-homework1/DetailServlet?season=" + season + "&episode=" + episode + "\">" + jobject.get("name").toString() + "</a></td>");
                out.println("</tr>");
            }
            out.println("</tbody></table>");
            out.println("</container>");

            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
