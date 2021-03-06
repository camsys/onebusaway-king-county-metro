package org.onebusaway.king_county_metro.legacy_avl_to_siri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.siri.core.SiriCoreModule;
import org.onebusaway.siri.core.SiriServer;
import org.onebusaway.siri.core.SiriCommon.ELogRawXmlType;
import org.onebusaway.siri.core.guice.LifecycleService;
import org.onebusaway.siri.jetty.SiriJettyModule;
import org.onebusaway.siri.jetty.StatusServletSource;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class LegacyAvlToSiriMain {

  private static final String ARG_ID = "id";

  private static final String ARG_SERVER_URL = "serverUrl";

  private static final String ARG_PRIVATE_SERVER_URL = "privateServerUrl";

  private static final String ARG_FROM_FILE = "fromFile";

  private static final String ARG_PAUSE_BETWEEN_PACKETS = "pauseBetweenPackets";

  private static final String ARG_LOG_RAW_XML = "logRawXml";

  private static final String ARG_FORMAT_OUTPUT_XML = "formatOutputXml";

  private static final String ARG_PACKET_OUTPUT_PATH = "packetOutputPath";

  public static void main(String[] args) throws Exception {
    LegacyAvlToSiriMain m = new LegacyAvlToSiriMain();
    m.run(args);
  }

  private SiriServer _server;

  private LegacyAvlToSiriTask _task;

  private LifecycleService _lifecycleService;

  @Inject
  public void setSiriServer(SiriServer server) {
    _server = server;
  }

  @Inject
  public void setTask(LegacyAvlToSiriTask task) {
    _task = task;
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  @Inject
  public void setStatusServletSource(StatusServletSource statusServletSource) {
    /**
     * This is a no-op primarily here to make sure the status servlet is
     * instantiated.
     */
  }

  public void run(String[] args) throws ParseException, FileNotFoundException {

    if (args.length == 1
        && (args[0].equals("--help") || args[0].equals("-help") || args[0].equals("-h"))) {
      printUsage();
      System.exit(0);
    }

    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    List<Module> modules = new ArrayList<Module>();
    modules.addAll(SiriCoreModule.getModules());
    modules.add(new SiriJettyModule());
    modules.add(new LegacyAvlToSiriModule());
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    configure(cli);

    _lifecycleService.start();
  }

  protected void printUsage() {

    InputStream is = getClass().getResourceAsStream("usage.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (IOException ex) {

    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {

        }
      }
    }
  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_ID, true, "SIRI client participant id");
    options.addOption(ARG_SERVER_URL, true, "server url");
    options.addOption(ARG_PRIVATE_SERVER_URL, true, "private server url");
    options.addOption(ARG_FROM_FILE, true, "pcap data file");
    options.addOption(ARG_PAUSE_BETWEEN_PACKETS, true, "pause between packets");
    options.addOption(ARG_LOG_RAW_XML, true, "log raw xml");
    options.addOption(ARG_FORMAT_OUTPUT_XML, false, "format output xml");
    options.addOption(ARG_PACKET_OUTPUT_PATH, true, "packet output path");
  }

  protected void configure(CommandLine cli) throws FileNotFoundException {

    if (cli.hasOption(ARG_ID)) {
      String id = cli.getOptionValue(ARG_ID);
      _server.setIdentity(id);
    }

    if (cli.hasOption(ARG_SERVER_URL)) {
      String url = cli.getOptionValue(ARG_SERVER_URL);
      _server.setUrl(url);
    }

    if (cli.hasOption(ARG_PRIVATE_SERVER_URL)) {
      String privateUrl = cli.getOptionValue(ARG_PRIVATE_SERVER_URL);
      _server.setPrivateUrl(privateUrl);
    }

    if (cli.hasOption(ARG_FROM_FILE)) {
      String path = cli.getOptionValue(ARG_FROM_FILE);
      FileInputStream in = new FileInputStream(path);
      _task.setSource(in);
    }

    if (cli.hasOption(ARG_PAUSE_BETWEEN_PACKETS)) {
      long pauseBetweenPackets = Long.parseLong(cli.getOptionValue(ARG_PAUSE_BETWEEN_PACKETS));
      _task.setPauseBetweenPackets(pauseBetweenPackets);
    }

    if (cli.hasOption(ARG_LOG_RAW_XML)) {
      String value = cli.getOptionValue(ARG_LOG_RAW_XML);
      ELogRawXmlType type = ELogRawXmlType.valueOf(value.toUpperCase());
      _server.setLogRawXmlType(type);
    }

    if (cli.hasOption(ARG_PACKET_OUTPUT_PATH)) {
      _task.setPacketOutputDirectory(new File(
          cli.getOptionValue(ARG_PACKET_OUTPUT_PATH)));
    }

    _server.setFormatOutputXmlByDefault(cli.hasOption(ARG_FORMAT_OUTPUT_XML));
  }
}
