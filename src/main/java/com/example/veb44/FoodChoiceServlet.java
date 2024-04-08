package com.example.veb44;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "FoodChoice", value = "/food-choice")
public class FoodChoiceServlet extends HttpServlet {

    private Map<String, List<String>> weeklyMenu = new HashMap<>();
    String[] daysOfWeek = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};
    @Override
    public void init() throws ServletException {
        super.init();
        ServletContext context = getServletContext();


        for (String day : daysOfWeek) {
            List<String> mealsForDay = loadMealsForDay(day, context);
            weeklyMenu.put(day, mealsForDay);
        }

        context.setAttribute("weeklyMenu", weeklyMenu);
    }

    private List<String> loadMealsForDay(String day, ServletContext context) throws ServletException {
        List<String> meals = new ArrayList<>();
        InputStream inputStream = context.getResourceAsStream("/WEB-INF/" + day + ".txt");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    meals.add(line);
                }
            } catch (IOException e) {
                throw new ServletException("Failed to load meals for " + day, e);
            }
        }
        return meals;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Map<String, List<String>> weeklyMenu = (Map<String, List<String>>)getServletContext().getAttribute("weeklyMenu");

        HttpSession session = request.getSession();
        Map<String, String> selectedMeals = (Map<String, String>) session.getAttribute("selectedMeals");

        if (selectedMeals != null) {
            out.println("<h1>Odabrana jela</h1>");
            out.println("<ul>");
            for (String day : daysOfWeek) {
                out.println("<li>" + day + ": " + selectedMeals.get(day) + "</li>");
            }
            out.println("</ul>");
            out.println("<a href='food-orders'>Pregled odabranih jela</a>");
            return;
        }


        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Odaberite jelo za svaki dan</title></head><body>");
        out.println("<form action='food-orders' method='POST'>");

        for (String day : daysOfWeek) {
            List<String> meals = weeklyMenu.get(day);

            out.println("<label for='" + day + "'>" + day + "</label>: ");
            out.println("<select name='" + day + "' id='" + day + "'>");
            for(String meal : meals) {
                out.println("<option value='" + meal + "'>" + meal + "</option>");
            }
            out.println("</select><br>");
        }

        out.println("<input type='submit' value='Submit'>");
        out.println("</form>");
        out.println("</body></html>");
    }


}