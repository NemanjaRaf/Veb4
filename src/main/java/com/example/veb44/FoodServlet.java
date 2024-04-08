package com.example.veb44;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@WebServlet(name = "FoodOrders", value = "/food-orders")
public class FoodServlet extends HttpServlet {

    String[] daysOfWeek = {"ponedeljak", "utorak", "sreda", "cetvrtak", "petak"};

    public void init() throws ServletException {
        ServletContext context = getServletContext();
        Map<String, Map<String, Integer>> weeklyOrders = new ConcurrentHashMap<>();// za thread-sigurnost


        for (String day : daysOfWeek) {
            List<String> meals = loadMealsForDay(day);
            Map<String, Integer> dailyOrders = meals.stream()
                    .collect(Collectors.toMap(Function.identity(), meal -> 0));
            weeklyOrders.put(day, dailyOrders);
        }

        context.setAttribute("weeklyOrders", weeklyOrders);
    }
    private List<String> loadMealsForDay(String dayFileName) throws ServletException {
        List<String> meals = new ArrayList<>();
        InputStream inputStream = getServletContext().getResourceAsStream("/WEB-INF/" + dayFileName + ".txt");
        if (inputStream != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    meals.add(line.trim());
                }
            } catch (IOException e) {
                throw new ServletException("Error reading " + dayFileName + ".txt", e);
            }
        }
        return meals;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<html><head><title>Lista odabranih jela</title></head><body>");
        out.println("<h1>Odabrana jela</h1>");

        ServletContext context = getServletContext();
        Map<String, Map<String, Integer>> weeklyOrders = (Map<String, Map<String, Integer>>) context.getAttribute("weeklyOrders");

        for (String day : daysOfWeek) {
            out.println("<h2>" + day + "</h2>");
            out.println("<table border='1'><tr><th>#</th><th>Jelo</th><th>Količina</th></tr>");
            Map<String, Integer> dailyOrders = weeklyOrders.get(day);
            int count = 1;
            for (Map.Entry<String, Integer> entry : dailyOrders.entrySet()) {
                out.println("<tr><td>" + (count++) + "</td><td>" + entry.getKey() + "</td><td>" + entry.getValue() + "</td></tr>");
            }
            out.println("</table>");
        }

        out.println("<form method='POST'>");
        out.println("<input type='submit' name='delete' value='Očisti'>");
        out.println("</form>");

        out.println("</body></html>");
    }



    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Processing food orders");
        HttpSession session = request.getSession();

        if (request.getParameter("delete") != null) {
            doDelete(request, response);
            return;
        }

        Map<String, List<String>> weeklyMenu = (Map<String, List<String>>)getServletContext().getAttribute("weeklyMenu");

        Map<String, String> selectedMeals = new HashMap<>();

        for (String day : weeklyMenu.keySet()) {
            String selectedMeal = request.getParameter(day);
            if (selectedMeal != null && !selectedMeal.trim().isEmpty()) {
                selectedMeals.put(day, selectedMeal);
            }
        }

        session.setAttribute("selectedMeals", selectedMeals);

        Map<String, Map<String, Integer>> weeklyOrders =
                (Map<String, Map<String, Integer>>)getServletContext().getAttribute("weeklyOrders");

        for (String day : weeklyOrders.keySet()) {
            String selectedMeal = request.getParameter(day);
            if (selectedMeal != null && !selectedMeal.trim().isEmpty()) {
                Map<String, Integer> dailyOrders = weeklyOrders.get(day);

                int currentCount = dailyOrders.getOrDefault(selectedMeal, 0);
                dailyOrders.put(selectedMeal, currentCount + 1);

            }
        }

        response.sendRedirect("food-choice");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Deleting all orders");
        ServletContext context = getServletContext();

        Map<String, Map<String, Integer>> weeklyOrders = (Map<String, Map<String, Integer>>) context.getAttribute("weeklyOrders");
        if (weeklyOrders != null) {
            for (Map<String, Integer> dailyOrders : weeklyOrders.values()) {
                dailyOrders.replaceAll((meal, count) -> 0);
            }
        }

        for (HttpSession session : SessionListener.getSessions()) {
            session.removeAttribute("selectedMeals");
        }

        response.sendRedirect("food-orders");
    }
}