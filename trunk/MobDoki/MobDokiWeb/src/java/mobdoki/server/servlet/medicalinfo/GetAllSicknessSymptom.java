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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;
import org.json.JSONArray;

/**
 *
 * @author Andreas
 */
public class GetAllSicknessSymptom extends HttpServlet {

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
        
        String SSID = request.getParameter("ssid");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT name FROM \"Symptom\" ORDER BY name";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ResultSet results = ps.executeQuery();          // A Symptom tabla osszes soranak a "name" attributuma
                
                JSONArray symptom = new JSONArray();              // JSON tomb a lekerdezett neveknek
                if (results != null) {
                    while (results.next()) {
                        symptom.put(results.getString(1));            // nev hozzaadasa a tombhoz
                    }
                    results.close();
                    json.put("symptom", symptom);                   // nevek tomb hozzafuzese az kimeneti JSON objektumhoz
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
                
                sqlText = "SELECT name,seriousness,coalesce(url,'') FROM \"Sickness\" ORDER BY name";
                ps = db.prepareStatement(sqlText);                 // A Sickness tabla osszes soranak attributuma
                results = ps.executeQuery();
                
                JSONArray sickness = new JSONArray();              // JSON tomb a lekerdezett neveknek
                JSONArray seriousness = new JSONArray();
                JSONArray url = new JSONArray();
                if (results != null) {
                    while (results.next()) {
                        sickness.put(results.getString(1));            // nev hozzaadasa a tombhoz
                        seriousness.put(results.getDouble(2));
                        url.put(results.getString(3));
                    }
                    results.close();
                    json.put("sickness", sickness);                   // nevek tomb hozzafuzese az kimeneti JSON objektumhoz
                    json.put("seriousness", seriousness);
                    json.put("url", url);
                    json.setOK();
                } else json.setDBError();                  // adatbazis hiba
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
        return "Gets every symptom's name and every sickness' attributes";
    }// </editor-fold>
}
