/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.user;

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

/**
 *
 * @author Andreas
 */
public class Register extends HttpServlet {
   
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
            String username = request.getParameter("username");
            int password = Integer.parseInt(request.getParameter("password"));
            String usertype = request.getParameter("usertype");
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText = "SELECT username FROM \"User\" WHERE username LIKE ?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,username);
                ResultSet results = ps.executeQuery();  // A megadott felhasznalonev keresese:
                if (results != null && results.next()) {    // Foglalt?
                    json.setErrorMessage("A megadott felhasználónév már foglalt.");
                } else {                                    // Szabad? Akkor felvetel az adatbazisba!
                        sqlText = "SELECT id FROM \"UserType\" WHERE name=?";
                        ps = db.prepareStatement(sqlText);
                        ps.setString(1,usertype);
                        results = ps.executeQuery();
                        if (results != null && results.next()) {
                        
                            sqlText = "INSERT INTO \"User\" (username, password, \"usertypeID\") VALUES (?, ?, ?)";
                            ps = db.prepareStatement(sqlText);
                            ps.setString(1,username);
                            ps.setInt(2,password);
                            ps.setInt(3,results.getInt(1));
                            ps.executeUpdate();
                            json.setOKMessage("Sikeres regisztráció!");
                        } else json.setErrorMessage("Ismeretlen felhasználói típus.");
                }
                results.close();
                db.close();
            } else json.setServerError();   // adatbazis nem erheto el

        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
        } finally {
            json.write(out);     // Osszeallitott JSON kiirasa az outputra
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
        return "Register";
    }// </editor-fold>

}
