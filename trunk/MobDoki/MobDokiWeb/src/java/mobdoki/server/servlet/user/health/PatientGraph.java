/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.user.health;

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
import org.json.JSONArray;
import org.json.JSONObject;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author mani
 */
public class PatientGraph extends HttpServlet {
   
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
        
        String username = request.getParameter("username");
        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                JSONArray elements = new JSONArray();
                
                String sqlText = "SELECT bloodpressure1,bloodpressure2,pulse,weight,temperature,mood,date "+
                                 "FROM \"PatientHealth\" " +
                                 "WHERE username LIKE ? " +
                                 "ORDER BY date";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1, username);
                ResultSet results = ps.executeQuery();                              // a paciens egeszsegugyi allapotai idosorrendben
                if (results != null) {
                    while (results.next()) {
                        JSONObject element = new JSONObject();
                        element.put("bp1", results.getInt(1));
                        element.put("bp2", results.getInt(2));
                        element.put("pulse", results.getInt(3));
                        element.put("weight", results.getDouble(4));
                        element.put("temperature", results.getDouble(5));
                        element.put("mood", results.getInt(6));
                        element.put("date", results.getTimestamp(7).toString());
                        
                        elements.put(element);
                    }
                    json.put("elements", elements);
                    json.setOK();
                    results.close();
                } else json.setDBError();
                db.close();
            } else json.setServerError();   // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
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
