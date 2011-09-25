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
import org.json.JSONObject;
import org.postgresql.geometric.PGpoint;

/**
 *
 * @author Andreas
 */
public class HospitalInfo extends HttpServlet {

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
        String hospital = request.getParameter("hospital");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {

                String sqlText = "SELECT address,coordinates,coalesce(phone,'-'),coalesce(email,'-'),id FROM \"Hospital\" WHERE name LIKE ?";     // Betegseg adatainak lekerese
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,hospital);                           // parameter beallitasa az adott tunetre
                ResultSet results = ps.executeQuery();
                
                int hospitalID=0;
                if (results != null && results.next()) {            // Ha van talalat
                    json.put("address", results.getString(1));      // irjuk ki a megtalalalt cimet
                    PGpoint point = (PGpoint)results.getObject(2);      // irjuk ki a megtalalalt korhaz koordinatait
                    json.put("coordinates",(new JSONObject()).put("x", point.x).put("y", point.y));
                    json.put("phone", results.getString(3));        // irjuk ki a megtalalalt telefon szamot
                    json.put("email", results.getString(4));        // irjuk ki a megtalalalt email cimet szamot
                    hospitalID=results.getInt(5);
                    results.close();
                }
                
                JSONArray sickness = new JSONArray();

                sqlText = "SELECT s.name " +
                          "FROM \"Curing\" c INNER JOIN \"Sickness\" s ON (c.\"sicknessID\"=s.id) " +
                          "WHERE c.\"hospitalID\"=? " +
                          "ORDER BY s.name";     // Kezelo korhazak keresese
                ps = db.prepareStatement(sqlText);
                ps.setInt(1,hospitalID);                             // parameter beallitasa az adott tunetre
                results = ps.executeQuery();

                if (results != null) {
                    while (results.next()) {                        // menjunk vegig a talalatokon
                        sickness.put(results.getString(1));                 // irjuk ki a megtalalalt korhaz nevet
                    }
                    json.put("sickness", sickness);                  // korhaznevek
                    results.close();
                }
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
