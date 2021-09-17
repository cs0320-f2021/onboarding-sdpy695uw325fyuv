package edu.brown.cs.student.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import freemarker.template.Configuration;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import spark.ExceptionHandler;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.TemplateViewRoute;
import spark.template.freemarker.FreeMarkerEngine;

/**
 * The Main class of our project. This is where execution begins.
 */
public final class Main {

  // use port 4567 by default when running server
  private static final int DEFAULT_PORT = 4567;


  /**
   * The initial method called when execution begins.
   *
   * @param args An array of command line arguments
   */
  public static void main(String[] args) {
    new edu.brown.cs.student.main.Main(args).run();
  }

  private String[] args;

  private Main(String[] args) {
    this.args = args;
  }


  // takes in 2 star numbers and the star coordinates returns the distance between them
  private float getDistance(Float x1, Float x2, Float y1, Float y2, Float z1, Float z2){
    float distance = (float)
        Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2));
    return distance;
  }


  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);

    //ArrayList<String[]> stored_stars = new ArrayList<String[]>();
    Map<String, Float[]> starMap = new HashMap<String,Float[]>();

    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }

    // TODO: Add your REPL here!
    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;
      while ((input = br.readLine()) != null) {
        try {
          input = input.trim();
          MathBot myBot = new MathBot();
          String[] arguments = input.split(" ");
          switch(arguments[0]){
            //add command: adds 2 numbers
            case "add":
              try {
                Double num1 = Double.parseDouble(arguments[1]);
                Double num2 = Double.parseDouble(arguments[2]);
                arguments[0] = Double.toString(myBot.add(num1, num2));
                System.out.println(arguments[0]);
              } catch (Exception e) {
                System.out.println("ERROR: Improper input for add");
              }
              break;

            //subtract command: subtracts 2 numbers
            case "subtract":
              try {
                Double num1 = Double.parseDouble(arguments[1]);
                Double num2 = Double.parseDouble(arguments[2]);
                arguments[0] = Double.toString(myBot.subtract(num1, num2));
                System.out.println(arguments[0]);
              } catch (Exception e) {
                System.out.println("ERROR: Improper input for subtract");
              }
              break;

            //loads a file containing stars' position information
            case "stars":
              File filename = new File(arguments[1]);
              try (BufferedReader mybr = new BufferedReader(new InputStreamReader(
                  new FileInputStream(filename), StandardCharsets.UTF_8))) {

                // reads the file line by line
                mybr.readLine(); // removes title line
                String line;
                while ((line = mybr.readLine()) != null) {
                  String[] columns = line.split(",");
                  //stores star's data
                  //stored_stars.add(columns);

                  Float xval = Float.parseFloat(columns[2]);
                  Float yval = Float.parseFloat(columns[3]);
                  Float zval = Float.parseFloat(columns[4]);

                  starMap.put(columns[1], new Float[] {xval, yval, zval});
                }
                System.out.println("stars imported");
              } catch (IOException e) {
                System.out.println("ERROR: File not found");
              }
              break;

            case "naive_neighbors":
              if (arguments.length == 5) {
                Integer k = Integer.parseInt(arguments[1]);
                String[][] closest_stars = new String[k][2];
                ArrayList<String[]> nearest = new ArrayList<String[]>();

                //sets default nearest neighbors to max
                for (int i = 0; i < k; i++){
                  closest_stars[i][0] = String.valueOf(10000000);
                }

                //extracts location data from input
                Float x1 = Float.parseFloat(arguments[2]);
                Float y1 = Float.parseFloat(arguments[3]);
                Float z1 = Float.parseFloat(arguments[4]);

                //iterate through all stored stars to find distance to new star
                //for (int i = 0; i < stored_stars.size(); i++){
                for (String key : starMap.keySet()){
                  //String[] star = stored_stars.get(i);
                  String star = key;

                  System.out.println("star =  " + star);
//                  Float x2 = Float.parseFloat(star[2]);
//                  Float y2 = Float.parseFloat(star[3]);
//                  Float z2 = Float.parseFloat(star[4]);

                    Float[] coordinates = starMap.get(key);

                    Float x2 = coordinates[0];
                    Float y2 = coordinates[1];
                    Float z2 = coordinates[2];

                  Float dist = getDistance(x1, x2, y1, y2,z1, z2);

                  //compares new distance to previous distance
                  for (int j = 0; j < k; j++){
                    System.out.println("Double.parseDouble(closest_stars[j][0]) = " + Double.parseDouble(closest_stars[j][0]));
                    System.out.println("dist =  " + dist);
                    if (Double.parseDouble(closest_stars[j][0]) == dist){
                      if (Math.random() > .5) {
                        System.out.println("math.random()" + Math.random());
                        closest_stars[j][0] = String.valueOf(dist);
                        closest_stars[j][1] = star[1];
                      }
                    }
                    else if (Double.parseDouble(closest_stars[j][0]) > dist){
                      System.out.println("check 5.star [1]: " + star[1]);
                      closest_stars[j][0] = String.valueOf(dist);
                      closest_stars[j][1] = star[1];
                      System.out.println("check 6 ");
                    }
                    else {

                    }
                  }
                }
                System.out.println("The closest Stars to x:" + x1 + ", y:" + y1 + ", z:" + z1 + " are:");

                for (int j = 0; j < k; j++){
                  System.out.println(j + ". Star: " + closest_stars[j][1] + ", With distance: " + closest_stars[j][0]);
                }

              }
              else {
                System.out.println("ERROR: please enter in the form: 'naive_neighbors k x y z'");
              }
              break;

          }
        } catch (Exception e) {
          // e.printStackTrace();
          System.out.println("ERROR: We couldn't process your input");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("ERROR: Invalid input for REPL");
    }

  }


  private static FreeMarkerEngine createEngine() {
    Configuration config = new Configuration(Configuration.VERSION_2_3_0);

    // this is the directory where FreeMarker templates are placed
    File templates = new File("src/main/resources/spark/template/freemarker");
    try {
      config.setDirectoryForTemplateLoading(templates);
    } catch (IOException ioe) {
      System.out.printf("ERROR: Unable use %s for template loading.%n",
          templates);
      System.exit(1);
    }
    return new FreeMarkerEngine(config);
  }

  private void runSparkServer(int port) {
    // set port to run the server on
    Spark.port(port);

    // specify location of static resources (HTML, CSS, JS, images, etc.)
    Spark.externalStaticFileLocation("src/main/resources/static");

    // when there's a server error, use ExceptionPrinter to display error on GUI
    Spark.exception(Exception.class, new edu.brown.cs.student.main.Main.ExceptionPrinter());

    // initialize FreeMarker template engine (converts .ftl templates to HTML)
    FreeMarkerEngine freeMarker = createEngine();

    // setup Spark Routes
    Spark.get("/", new edu.brown.cs.student.main.Main.MainHandler(), freeMarker);
  }

  /**
   * Display an error page when an exception occurs in the server.
   */
  private static class ExceptionPrinter implements ExceptionHandler<Exception> {
    @Override
    public void handle(Exception e, Request req, Response res) {
      // status 500 generally means there was an internal server error
      res.status(500);

      // write stack trace to GUI
      StringWriter stacktrace = new StringWriter();
      try (PrintWriter pw = new PrintWriter(stacktrace)) {
        pw.println("<pre>");
        e.printStackTrace(pw);
        pw.println("</pre>");
      }
      res.body(stacktrace.toString());
    }
  }

  /**
   * A handler to serve the site's main page.
   *
   * @return ModelAndView to render.
   * (main.ftl).
   */
  private static class MainHandler implements TemplateViewRoute {
    @Override
    public ModelAndView handle(Request req, Response res) {
      // this is a map of variables that are used in the FreeMarker template
      Map<String, Object> variables = ImmutableMap.of("title",
          "Go go GUI");

      return new ModelAndView(variables, "main.ftl");
    }
  }
}
