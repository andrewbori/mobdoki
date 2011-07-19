/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.medicalinfo;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author Andreas
 */
public class SearchSickness extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObj json = new JSONObj();
        String symptoms = request.getParameter("symptoms");
        try {


            if (symptoms==null || symptoms.equals("")) {   //hibas parameter eseten kivetel dobasa
                throw new Exception();
            }

            ArrayList<String> symptomlist = new ArrayList<String>();        // tunetek listaja
            StringTokenizer st = new StringTokenizer(symptoms, ", ");
	    while(st.hasMoreElements()) symptomlist.add(st.nextToken());    // tunetek parsolasa


            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {

                String sqlText = "SELECT sickness FROM \"Diagnosis\" WHERE symptom LIKE ?";     // Diagnozisok keresese
                PreparedStatement ps = db.prepareStatement(sqlText);

                json.put("size", symptomlist.size());               // tunetek szama a kimenetbe
                JSONArray sicknesses = new JSONArray();             // tomb a talalatoknak
                
                for(String symptom : symptomlist) {                 // kereses a megadott tunetekre egyenkent
                    ps.setString(1,symptom);                            // parameter beallitasa az adott tunetre
                    ResultSet results = ps.executeQuery();

                    if (results != null) {
                        while (results.next()) {                        // menjunk vegig a talalatokon
                            sicknesses.put(results.getString(1));           // talalat hozzaadasa a betgsegtombhoz
                        }
                        results.close();
                    } else json.setDBError();
                }
                
                json.put("sicknesses", sicknesses);                 // Megtalalt betegsegek hozzadadasa a kimenethez
                json.setOK();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        }
        finally {
            json.write(out);
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}