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
public class Login extends HttpServlet {
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        JSONObj json = new JSONObj();
        String username;
        int password;

        try {
            username = request.getParameter("username");
            password = Integer.parseInt(request.getParameter("password"));
        } catch (Exception e) {
            json.setParameterError();       // status: parameterezesi hiba
            json.write(out);
            out.close();
            return;
        }
        
        try {
            username = request.getParameter("username");
            password = Integer.parseInt(request.getParameter("password"));
            
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            
            if (db!=null) {
                String sqlText = "select usertype from \"User\" where username like ? and password=?";
                PreparedStatement ps = db.prepareStatement(sqlText);
                ps.setString(1,username);                                       // felhasznalonev
                ps.setInt(2,password);                                          //  es jelszoho paroshoz
                ResultSet results = ps.executeQuery();                          //   tartozo felhasználo tipusa
                if (results != null && results.next()) {
                    json.put("userType", results.getString(1));
                    json.setOK();           // status: OK
                } else json.setErrorMessage("A megadott felhasználónév vagy jelszó helytelen.");
                
                results.close();
                db.close();
            } else json.setServerError();   // adatbazis nem erheto el
            
        } catch (Exception e) {
            json.setDBError();              // adatbazis hiba
        } finally {
            json.write(out);    // Osszeallitott JSON kiirasa az outputra
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

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
        return "Login";
    }
    // </editor-fold>

}
