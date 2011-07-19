/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package mobdoki.server.servlet.user.symptom;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mobdoki.server.Connect;

/**
 *
 * @author mani
 */
public class PictureDownload extends HttpServlet {
   
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image");
        OutputStream output = response.getOutputStream();
        String PictureName = request.getParameter("picturename");


        try {
            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);
            if (db != null) {
                PreparedStatement ps = db.prepareStatement("SELECT img FROM \"Picture\" WHERE imgname LIKE ?");
                ps.setString(1,PictureName);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    byte[] imgBytes = rs.getBytes(1);

                    output.write(imgBytes);
                    output.close();


                  //  out.println("sikeres letöltés");
                }
                rs.close();
                ps.close();
            } else {
                throw new Exception();
            }
        } catch (Exception e) {

        } finally {
            output.close();
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
