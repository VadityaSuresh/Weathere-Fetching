
package myProject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class MyServlet
 */


public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendRedirect("index.html");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    //API Key
	    String apiKey = "77c94c365b51af13474d6c902c0f8bc0";
	    // Get the city from the form input
	    String city = request.getParameter("city"); 

	    try {
	        // Validate user input
	        if (city == null || city.isEmpty()) {
	            throw new CustomWeatherException("City name is required");
	        }
	        if (!city.matches("^[a-zA-Z ]+$")) {
	            throw new CustomWeatherException("Invalid city name. Only letters and spaces are allowed.");
	        }

	        // Create the URL for the OpenWeatherMap API request
	        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

	        URL url = new URL(apiUrl);
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setRequestMethod("GET");

	        InputStream inputStream = connection.getInputStream();
	        InputStreamReader reader = new InputStreamReader(inputStream);

	        Scanner scanner = new Scanner(reader);
	        StringBuilder responseContent = new StringBuilder();

	        while (scanner.hasNext()) {
	            responseContent.append(scanner.nextLine());
	        }
	        scanner.close();

	        // Parse the JSON response to extract temperature, date, and humidity
	        Gson gson = new Gson();
	        JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);

	        // Date & Time
	        long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
	        Date date = new Date(dateTimestamp);

	        // Temperature
	        double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
	        int temperatureCelsius = (int) (temperatureKelvin - 273.15);

	        // Humidity
	        int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();

	        // Wind Speed
	        double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();

	        // Weather Condition
	        String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();

	        // Set the data as request attributes (for sending to the jsp page)
	        request.setAttribute("date", date);
	        request.setAttribute("city", city);
	        request.setAttribute("temperature", temperatureCelsius);
	        request.setAttribute("weatherCondition", weatherCondition); 
	        request.setAttribute("humidity", humidity);    
	        request.setAttribute("windSpeed", windSpeed);
	        request.setAttribute("weatherData", responseContent.toString());

	        connection.disconnect();
	    } catch (CustomWeatherException e) {
	        // Handle your custom exception here
	        // You can set an attribute with the exception message to display in the JSP page
	        request.setAttribute("errorMessage", e.getMessage());
	        request.getRequestDispatcher("error.jsp").forward(request, response);
	    } catch (IOException e) {
	        // Handle other IOExceptions
	        e.printStackTrace();
	        // You can set an attribute with a general error message
	        request.setAttribute("errorMessage", "An error occurred while fetching weather data.");
	        request.getRequestDispatcher("error.jsp").forward(request, response);
	    }

	    // Forward the request to the weather.jsp page for rendering
	    request.getRequestDispatcher("index.jsp").forward(request, response);
	}
}



