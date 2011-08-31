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
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import mobdoki.server.Connect;
import mobdoki.server.JSONObj;
import mobdoki.server.Sessions;

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
        
        String SSID = request.getParameter("ssid");
        String symptoms = request.getParameter("symptoms");
        
        try {
            if (!Sessions.MySessions().isValid(SSID)) {
                json.setUnauthorizedError();
                return;
            }
            
            if (symptoms==null || symptoms.equals("")) {   //hibas parameter eseten kivetel dobasa
                throw new Exception();
            }

            ArrayList<String> symptomlist = new ArrayList<String>();        // tunetek listaja
            StringTokenizer st = new StringTokenizer(symptoms, ", ");
	    while(st.hasMoreElements()) symptomlist.add(st.nextToken());    // tunetek parsolasa


            Class.forName(Connect.driver); //load the driver
            Connection db = DriverManager.getConnection(Connect.url, Connect.user, Connect.pass);

            if (db!=null) {

                String sqlText = "SELECT si.name " +
                                 "FROM \"Diagnosis\" d " +
                                        "INNER JOIN \"Symptom\" sy ON (d.\"symptomID\"=sy.id) " +
                                        "INNER JOIN \"Sickness\" si ON (d.\"sicknessID\"=si.id) " +
                                 "WHERE sy.name LIKE ?";     // Diagnozisok keresese
                PreparedStatement ps = db.prepareStatement(sqlText);

                json.put("size", symptomlist.size());               // tunetek szama a kimenetbe
                JSONArray sicknesses = new JSONArray();             // tomb a talalatoknak
                JSONArray list = new JSONArray();                   // tomb a talalatoknak darabszammal
                
                TreeMap<String, Integer> sicknessMap = new TreeMap<String, Integer>();	// betegsegek + talalati darabszamuk
                 
                for(String symptom : symptomlist) {                 // kereses a megadott tunetekre egyenkent
                    ps.setString(1, "%" + symptom + "%");                            // parameter beallitasa az adott tunetre
                    ResultSet results = ps.executeQuery();

                    if (results != null) {
                        while (results.next()) {                        // menjunk vegig a talalatokon
                            String sickness = results.getString(1);
                            if (sicknessMap.containsKey(sickness)) {                        // Ha mar a listaban van a betegseg: darabszam novelese
                                sicknessMap.put(sickness, sicknessMap.get(sickness)+1);
                            } else {                                                        // Ha nincs, akkor felvetel, 1-es darabszammal (es a listahoz hozzaadas)
                                    sicknessMap.put(sickness, 1);
                            }
                        }
                        results.close();
                    } else json.setDBError();
                }
                
                int MAX = 0;
                for (String s : sicknessMap.keySet()) {
                    int v = sicknessMap.get(s);
                    if (v>MAX) MAX=v;
                }
                for (int i=MAX; i>0; i--) {
                    for (String s : sicknessMap.keySet()) {
                        if (sicknessMap.get(s)==i) {
                            sicknesses.put(s);
                            list.put(s + " (" + sicknessMap.get(s) + ") ");
                        }
                    }
                }
                
                json.put("sicknesses", sicknesses);                 // Megtalalt betegsegek hozzadadasa a kimenethez
                json.put("list", list);
                json.setOK();
                db.close();
            } else json.setServerError();        // adatbazis nem erheto el
        } catch (Exception e) {
            json.setDBError();                  // adatbazis hiba
            json.setErrorMessage(e.getMessage());
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