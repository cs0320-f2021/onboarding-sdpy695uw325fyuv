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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  private HashMap<Integer, Star> stored_stars;

  private Main(String[] args) {
    this.args = args;
    stored_stars = new HashMap<Integer, Star>();
  }


  // takes in 2 sets of coordinates and returns the distance between them
  private float getDistance(Float x1, Float x2, Float y1, Float y2, Float z1, Float z2){
    float distance = (float)
        Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2));
    return distance;
  }


  // recursive function to sort array values
  private void quickSort(ArrayList<Star> a, int low, int high){
    if (low < high) {
      int partitionIndex = partition(a, low, high);
      quickSort(a, low, partitionIndex-1);
      quickSort(a, partitionIndex+1, high);
    }
  }

  //aids quicksort function: divides an arraylist in 2 and sorts e
  private int partition(ArrayList<Star> a, int low, int high) {
    int pivotIndex = (int) Math.floor(Math.random() * (high-low) + low);
    Star pivotValue = a.get(pivotIndex);
    a.set(pivotIndex, a.get(high));
    a.set(high, pivotValue);
    int line = low;

    for (int j = low; j < high-1; j++){
      if (a.get(j).getMyDist() < pivotValue.getMyDist()) {
        Star tempPivot = a.get(line);
        a.set(line, a.get(j));
        a.set(j, tempPivot);
      }
      line += line;
    }

    Star tempPivot = a.get(line);
    a.set(line, a.get(high));
    a.set(high, tempPivot);

    return line;
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
                  //instantiates a star to hold data + adds it to the hashmap
                  Star star = new Star(
                      Integer.parseInt(columns[0]),
                      columns[1],
                      Float.parseFloat(columns[2]),
                      Float.parseFloat(columns[3]),
                      Float.parseFloat(columns[4]));

                  stored_stars.put(Integer.parseInt(columns[0]), star);
                }
                System.out.println("stars imported");
              } catch (IOException e) {
                System.out.println("ERROR: File not found");
              }
              break;

              //naive_neighbors: calculates closest neighbors to a star
            case "naive_neighbors":
              if (arguments.length == 5) {
                Integer k = Integer.parseInt(arguments[1]);

                //extracts location data from input
                Float x1 = Float.parseFloat(arguments[2]);
                Float y1 = Float.parseFloat(arguments[3]);
                Float z1 = Float.parseFloat(arguments[4]);

                ArrayList<Star> all_stars = new ArrayList<Star>();

                //iterate through all stored stars to find distance to new star
                for (Star star : stored_stars.values()) {
                  Float x2 = star.getX();
                  Float y2 = star.getY();
                  Float z2 = star.getZ();

                  star.setMyDist(getDistance(x1, x2, y1, y2, z1, z2));
                  all_stars.add(star);
                }

                this.quickSort(all_stars, 0, all_stars.size()-1);

                System.out.println("The closest " + k + " stars to x:" + x1 + ", y:" + y1 + ", z:" + z1 + " are:");

                for (int j = 0; j < k; j++){
                  System.out.println(
                      j
                      + ". Star Number: "
                      + all_stars.get(j).getStarNum()
                      + ", With distance: "
                      + all_stars.get(j).getMyDist()
                      );
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
