/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.pchmelar.ddw.homework1;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.CreoleRegister;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.Node;
import gate.ProcessingResource;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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

    // corpus pipeline
    private static SerialAnalyserController annotationPipeline = null;

    // whether the GATE is initialised
    private static boolean isGateInitilised = false;

    private void initialiseGate() {

        try {
            // set GATE home folder
            // Eg. /Applications/GATE_Developer_7.0
            File gateHomeFile = new File("/Applications/GATE_Developer_8.1");
            Gate.setGateHome(gateHomeFile);

            // set GATE plugins folder
            // Eg. /Applications/GATE_Developer_7.0/plugins            
            File pluginsHome = new File("/Applications/GATE_Developer_8.1/plugins");
            Gate.setPluginsHome(pluginsHome);

            // set user config file (optional)
            // Eg. /Applications/GATE_Developer_7.0/user.xml
            Gate.setUserConfigFile(new File("/Applications/GATE_Developer_8.1", "user.xml"));

            // initialise the GATE library
            Gate.init();

            // load ANNIE plugin
            CreoleRegister register = Gate.getCreoleRegister();
            URL annieHome = new File(pluginsHome, "ANNIE").toURL();
            register.registerDirectories(annieHome);

            // flag that GATE was successfuly initialised
            isGateInitilised = true;

        } catch (MalformedURLException ex) {
        } catch (GateException ex) {
        }
    }

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
            out.println("<a href=\"/ddw-homework1/MainServlet\"><img src=\"http://www.returndates.com/backgrounds/simpsons.logo.png\" class=\"img-responsive center-block\" style=\"width: 50%; height: 50%\"></a>");
            out.println("<br>");
            out.println("<br>");

            //GET script of selected episode (jsoup)
            String urlString = "http://www.springfieldspringfield.co.uk/view_episode_scripts.php?tv-show=the-simpsons&episode=s" + season + "e" + episode;
            Document document = Jsoup.connect(urlString).get();
            Elements script = document.select("div.scrolling-script-container");
            String content = script.html().replaceAll("<br> ?", "");

            out.println("<h2>Script of episode " + season + "/" + episode + " :</h2>");
            out.println("<textarea class=\"form-control\" rows=\"12\" style=\"resize: none;\">" + content + "</textarea>");

            //GATE stuff (sentiment analyze of script)
            if (!isGateInitilised) {
                initialiseGate();
            }
            try {
                // locate the JAPE grammar file
                File japeOrigFile = new File("/Users/filletzz/Documents/!School_CVUT/Semestr10/[MI-DDW]/cv02/sentiment.jape");
                java.net.URI japeURI = japeOrigFile.toURI();

                // create feature map for the JAPE transducer
                FeatureMap transducerFeatureMap = Factory.newFeatureMap();
                try {
                    // set the grammar location
                    transducerFeatureMap.put("grammarURL", japeURI.toURL());
                    // set the grammar encoding
                    transducerFeatureMap.put("encoding", "UTF-8");
                } catch (MalformedURLException e) {
                    System.out.println("Malformed URL of JAPE grammar");
                    System.out.println(e.toString());
                }

                // create instances of processing resources
                ProcessingResource documentResetPR = (ProcessingResource) Factory.createResource("gate.creole.annotdelete.AnnotationDeletePR");
                ProcessingResource tokenizerPR = (ProcessingResource) Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
                ProcessingResource sentenceSplitterPR = (ProcessingResource) Factory.createResource("gate.creole.splitter.SentenceSplitter");
                ProcessingResource gazetteerPR = (ProcessingResource) Factory.createResource("gate.creole.gazetteer.DefaultGazetteer");
                ProcessingResource japeTransducerPR = (ProcessingResource) Factory.createResource("gate.creole.Transducer", transducerFeatureMap);

                // create corpus pipeline
                annotationPipeline = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController");

                // add the processing resources (modules) to the pipeline
                annotationPipeline.add(documentResetPR);
                annotationPipeline.add(tokenizerPR);
                annotationPipeline.add(sentenceSplitterPR);
                annotationPipeline.add(gazetteerPR);
                annotationPipeline.add(japeTransducerPR);

                // create a document
                gate.Document gateDocument = Factory.newDocument(content);

                // create a corpus and add the document
                Corpus corpus = Factory.newCorpus("");
                corpus.add(gateDocument);

                // set the corpus to the pipeline
                annotationPipeline.setCorpus(corpus);

                //run the pipeline
                annotationPipeline.execute();

                // loop through the documents in the corpus
                for (int i = 0; i < corpus.size(); i++) {

                    gate.Document doc = corpus.get(i);
                    
                    out.println("<br>");
                    out.println("<table class=\"table table-bordered\" style=\"background-color: #FFFFFF\">");
                    out.println("<thead><tr><th>Positive</th><th>Negative</th><th>Sentiment ratio</th></tr></thead><tbody>");

                    // get the default annotation set
                    AnnotationSet as_default = doc.getAnnotations();
                    FeatureMap futureMap = null;
                    
                    // get all Positive annotations
                    AnnotationSet annSetTokens = as_default.get("Positive", futureMap);
                    double positive = annSetTokens.size();
                    out.println("<tr><td>" + positive + "</td>");
                    

//                    // loop through the Positive annotations
//                    ArrayList tokenAnnotations = new ArrayList(annSetTokens);
//                    for (int j = 0; j < tokenAnnotations.size(); ++j) {
//
//                        // get a token annotation
//                        Annotation token = (Annotation) tokenAnnotations.get(j);
//
//                        // get the underlying string for the token
//                        Node isaStart = token.getStartNode();
//                        Node isaEnd = token.getEndNode();
//                        String underlyingString = doc.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
//                        out.println("Positive: " + underlyingString);
//                    }

                    // get all Negative annotations
                    annSetTokens = as_default.get("Negative", futureMap);
                    double negative = annSetTokens.size();
                    out.println("<td>" + negative + "</td>");

//                    // loop through the Negative annotations
//                    tokenAnnotations = new ArrayList(annSetTokens);
//                    for (int j = 0; j < tokenAnnotations.size(); ++j) {
//
//                        // get a token annotation
//                        Annotation token = (Annotation) tokenAnnotations.get(j);
//
//                        // get the underlying string for the Token
//                        Node isaStart = token.getStartNode();
//                        Node isaEnd = token.getEndNode();
//                        String underlyingString = doc.getContent().getContent(isaStart.getOffset(), isaEnd.getOffset()).toString();
//                        out.println("Negative: " + underlyingString);
//                    }

                    // calculate sentiment ratio
                    double ratio = positive / negative;
                    out.println("<td>" + ratio + "</td></tr>");

                    out.println("</tbody></table>");
                }
            } catch (GateException ex) {
            }

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
