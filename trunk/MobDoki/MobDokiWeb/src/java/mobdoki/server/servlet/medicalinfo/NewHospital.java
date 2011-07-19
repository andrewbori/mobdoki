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
import org.postgresql.geometric.PGpoint;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;

/**
 *
 * @author Andreas
 */
public class NewHospital extends HttpServlet {
   
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
               
        try {
            String name = request.getParameter("name");
            String address = request.getParameter("address");
            double x = Double.parseDouble(request.getParameter("x"));
            double y = Double.parseDouble(request.getParameter("y"));
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT name FROM \"Hospital\" WHERE name LIKE ?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,name);
                ResultSet results = ps.executeQuery();                  // Mar van ilyen nevu korhaz?
                if (results != null) {
                    if (results.next()) {                                  // Ha van, akkor hibauzenet.     
                        json.setErrorMessage("A megadott kórház már megtalálható.");
                    } else {                                                // Ha nincs, akkor felvetel...
                        sqlText = "INSERT INTO \"Hospital\"(name,address,coordinates) VALUES (?,?,?)";
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,name);
                        ps.setString(2,address);
                        ps.setObject(3, new PGpoint(x,y));
                        ps.executeUpdate();
                        json.setOKMessage("Sikeres felvétel!");
                    }
                    results.close();
                }  else json.setDBError();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
        } finally {
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
        return "NewHospital";
    }// </editor-fold>

}
