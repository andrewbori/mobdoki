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
public class UserProfile extends HttpServlet {
   
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
        String name = request.getParameter("name");
        String address = request.getParameter("address");
        String email = request.getParameter("email");
        String oldpassword = request.getParameter("oldpassword");
        String newpassword = request.getParameter("newpassword");

        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {
                String sqlText;
                PreparedStatement ps;

                if (oldpassword!=null && newpassword!=null) {           // Ha a regi es az uj jelszo is adott
                    sqlText = "SELECT username FROM \"User\" " +
                              "WHERE username=? AND password=?";
                    ps = db.prepareStatement(sqlText);
                    ps.setString(1,username);
                    ps.setInt(2,Integer.parseInt(oldpassword));
                    ResultSet results = ps.executeQuery();                  // A megadott jelszo helyes?
                    if (results != null && results.next()) {                // Ha helyes, akkor megvaltoztat
                        sqlText = "UPDATE \"User\" SET password=? WHERE username=?";
                        ps = db.prepareStatement(sqlText);
                        ps.setInt(1,Integer.parseInt(newpassword));
                        ps.setString(2,username);
                        ps.executeUpdate();
                    } else {                                                // egyebkent hibauzenet es visszater
                        json.setErrorMessage("A régi jelszó helytelen!");
                        json.write(out);     // Osszeallitott JSON kiirasa az outputra
                        out.close();
                        db.close();
                        return;
                    }
                }
                
                sqlText = "UPDATE \"User\" SET name=?, address=?, email=? WHERE username=?";
                ps = db.prepareStatement(sqlText);
                ps.setString(1,name);
                ps.setString(2,address);
                ps.setString(3,email);
                ps.setString(4,username);
                ps.executeUpdate();                                         // Megadott user adatok frissitese
                json.setOKMessage("Sikeres módosítás!");
                
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
        return "User Profile";
    }// </editor-fold>

}
