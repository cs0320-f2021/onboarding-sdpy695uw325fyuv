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

  private Main(String[] args) {
    this.args = args;
  }


  // takes in 2 star numbers and the star coordinates returns the distance between them
  private double getDistance(Integer star1, Integer star2, List<Float> xlist, List<Float> ylist, List<Float> zlist){
    Float x1 = xlist.get(star1);
    Float y1 = ylist.get(star1);
    Float z1 = zlist.get(star1);

    Float x2 = xlist.get(star2);
    Float y2 = ylist.get(star2);
    Float z2 = zlist.get(star2);
    double distance =
        Math.sqrt(Math.pow((x2 - x1), 2) + Math.pow((y2 - y1), 2) + Math.pow((z2 - z1), 2));

    return distance;
  }


  @SuppressWarnings("checkstyle:WhitespaceAround")
  private void run() {
    // set up parsing of command line flags
    OptionParser parser = new OptionParser();

    // "./run --gui" will start a web server
    parser.accepts("gui");

    // use "--port <n>" to specify what port on which the server runs
    parser.accepts("port").withRequiredArg().ofType(Integer.class)
        .defaultsTo(DEFAULT_PORT);

    OptionSet options = parser.parse(args);
    if (options.has("gui")) {
      runSparkServer((int) options.valueOf("port"));
    }


    try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
      String input;

      while ((input = br.readLine()) != null) {
        //initializes variables to track stars
        List<Integer> numlist = new ArrayList<Integer>();
        List<String> namelist = new ArrayList<String>();
        List<Float> xlist = new ArrayList<Float>();
        List<Float> ylist = new ArrayList<Float>();
        List<Float> zlist = new ArrayList<Float>();
        List importedstars = new ArrayList<String>();

        try {
          input = input.trim();
          MathBot myBot = new MathBot();
          String[] arguments = input.split(" ");

          if (arguments[0].equals("naive_neighbors") && arguments.length == 5){
            Integer k = Integer.parseInt(arguments[1]);
            Integer[] closest_stars = new Integer[k];
            Float my_star_x = Float.parseFloat(arguments[2]);
            Float my_star_y = Float.parseFloat(arguments[3]);
            Float my_star_z = Float.parseFloat(arguments[4]);

            //iterate through all stored stars
            for (int i = 0; i < namelist.size(); i++){
              //compare euclidian distances
              Float x2 = xlist.get(i);
              Float y2 = ylist.get(i);
              Float z2 = zlist.get(i);

              double distance_to_mystar =
                  Math.sqrt(Math.pow((x2 - my_star_x), 2) + Math.pow((y2 - my_star_y), 2) + Math.pow((z2 - my_star_z), 2));

            }
            System.out.println("hold on dummy");
          }

          //adds 2 numbers when "add" is used
          if (arguments[0].equals("add")) {
            Double num1 = Double.parseDouble(arguments[1]);
            Double num2 = Double.parseDouble(arguments[2]);
            arguments[0] = Double.toString(myBot.add(num1, num2));
            System.out.println(arguments[0]);
          }

          //subtracts 2 numbers when "subtract" is used
          if (arguments[0].equals("subtract")) {
            Double num1 = Double.parseDouble(arguments[1]);
            Double num2 = Double.parseDouble(arguments[2]);
            arguments[0] = Double.toString(myBot.subtract(num1, num2));
            System.out.println(arguments[0]);
          }

          //loads a file containing stars' position information
          if (arguments[0].equals("stars")) {
            System.out.println("stars?");
            File filename = new File(arguments[1]);

            try (BufferedReader mybr = new BufferedReader(new InputStreamReader(
                new FileInputStream(filename), StandardCharsets.UTF_8))) {

              // reads the file line by line
              mybr.readLine(); // removes title line
              String line;
              while ((line = mybr.readLine()) != null) {

                String[] columns = line.split(",");

//                Integer starnum = Integer.parseInt(columns[0]);
//
//                //stores the star's number in the numlist
//                numlist.set(starnum, starnum);
//
//                //stores the star's name and coordinates
//                namelist.set(starnum, "sol");
//                xlist.set(starnum, Float.parseFloat(columns[2]));
//                ylist.set(starnum, Float.parseFloat(columns[3]));
//                zlist.set(starnum, Float.parseFloat(columns[4]));
//
//                //test to see how things are going (hint: not too good ;) )
//                System.out.println("Dist 1 and 2" + getDistance(1, 2, xlist, ylist, zlist) + "]");
              }

            } catch (IOException e) {
              System.out.println("ERROR: File not found");
            }

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
