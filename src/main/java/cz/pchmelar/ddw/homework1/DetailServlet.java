/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.pchmelar.ddw.homework1;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author filletzz
 */
@WebServlet(name = "DetailServlet", urlPatterns = {"/DetailServlet"})
public class DetailServlet extends HttpServlet {

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
        
        //get params
        String season = java.net.URLDecoder.decode(request.getParameter("season"), "UTF-8");
        String episode = java.net.URLDecoder.decode(request.getParameter("episode"), "UTF-8");
        
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
            out.println("<a href=\"/ddw-homework1/MainServlet\"><img src=\"http://www.returndates.com/backgrounds/simpsons.logo.png\" class=\"img-responsive center-block\"></a>");
            out.println("<br>");
            out.println("<br>");
            
            //GET script of selected episode (jsoup)
            String urlString = "http://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=the-simpsons&episode=s" + season + "e" + episode;
            Document document = Jsoup.connect(urlString).get();
            Elements script = document.select("div.scrolling-script-container");
            String content = script.html().replaceAll("<br> ?", "");
                    
            out.println("<h2>Script of episode " + season + "/" + episode + " :</h2>");
            out.println("<textarea class=\"form-control\" rows=\"12\" style=\"resize: none;\">" + content + "</textarea>");
            
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
