package org.cluster.membership.protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.TimeZone;
import java.util.UUID;
import java.util.logging.Logger;

import org.cluster.membership.common.model.Node;
import org.cluster.membership.common.model.util.EnvUtils;
import org.cluster.membership.common.model.util.Literals;
import org.cluster.membership.protocol.structures.DList;
import org.springframework.boot.ApplicationArguments;

public class Config {
	private static final Logger logger = Logger.getLogger(Config.class.getName());
	
	//private static HashMap<String, String> map;// = Parsing.readAppConfig();
			
	/**The time interval for making requests to other nodes in the cluster*/
	public static long ITERATION_INTERVAL_MS;
	
	/**The factor to multiply by iteration.interval.ms * (iterations=max.expected.node.log.2 || log2(cluster size)), and consider to send an update request*/
	public static int READ_IDDLE_ITERATIONS_FACTOR;

	/**The time out for connection to other nodes*/
	public static long CONNECTION_TIME_OUT_MS;
		
	/**The time to wait for a node sends a keep alive signal, to avoid removing from cluster*/
	public static long FAILING_NODE_EXPIRATION_TIME_MS;//one day
				
	/**The max  number of iterations to select a random node*/
	public static int MAX_EXPECTED_NODE_LOG_2_SIZE;
	
	
	/**The max length of the set for storing rumors messages, used for recovery other nodes later*/
	public static int MAX_RUMORS_LOG_SIZE;
	
	/**The max number of bytes allowed to transfer between client and server*/
	public static int MAX_OBJECT_SIZE;
	
	
	public static Node THIS_PEER;
	
	public static final DList SEEDS = new DList();
	
	public static final String[] MODE = new String[] {Literals.APP_RELEASE_MODE};
	
	private static Properties properties;
	
	public static boolean isValid() {
		return (ITERATION_INTERVAL_MS > 500 && ITERATION_INTERVAL_MS < 1000*60) &&
				(CONNECTION_TIME_OUT_MS > 100 && CONNECTION_TIME_OUT_MS < 1000*60) &&
				(FAILING_NODE_EXPIRATION_TIME_MS > 1000*60*60 && FAILING_NODE_EXPIRATION_TIME_MS < 1000*60*60*24*3) &&
				(MAX_RUMORS_LOG_SIZE < 10*1000*1000) && THIS_PEER != null;
	}
	
	public static Properties read(ApplicationArguments args) throws Exception {
		if(args.containsOption(Literals.APP_HOME)) Parsing.appHome = args.getOptionValues(Literals.APP_HOME).get(0);			
		else Parsing.appHome = EnvUtils.getHomePath(ClusterNodeEntry.class, Parsing.configFolder);

		//map = Parsing.readAppConfig();
		properties = Parsing.prop(Parsing.configFolder + File.separator  + Parsing.appConfigFile);
		
		ITERATION_INTERVAL_MS = Long.parseLong(properties.getProperty(Literals.ITERATION_INTERVAL_MS));
		READ_IDDLE_ITERATIONS_FACTOR = Integer.parseInt(properties.getProperty(Literals.READ_IDDLE_ITERATIONS_FACTOR));
		CONNECTION_TIME_OUT_MS = Long.parseLong(properties.getProperty(Literals.CONNECTION_TIME_OUT_MS));
		FAILING_NODE_EXPIRATION_TIME_MS = Long.parseLong(properties.getProperty(Literals.FAILING_NODE_EXPIRATION_TIME_MS));//one day
		MAX_EXPECTED_NODE_LOG_2_SIZE = Integer.parseInt(properties.getProperty(Literals.MAX_EXPECTED_NODE_LOG_2_SIZE));
		MAX_RUMORS_LOG_SIZE = Integer.parseInt(properties.getProperty(Literals.MAX_RUMORS_LOG_SIZE));
		MAX_OBJECT_SIZE = Integer.parseInt(properties.getProperty(Literals.MAX_OBJECT_SIZE));
		THIS_PEER = Parsing.readThisPeer();
		
		Parsing.setSeedNodes(args);		
		String appProperties = "using properties: \n";
		for(Object key : properties.keySet()) 
			appProperties += key + "=" + properties.getProperty(key.toString()) + "\n";
		
		logger.info(appProperties);			
		
		return properties;
	}
	
	private static class Parsing {
		
		
		private static final String configFolder = "config";
		private static final String appConfigFile = Literals.APP_PROP_FILE;
		
		private static String appHome;
		
		private static Properties prop(String file) throws Exception {			
			String peerConf = appHome + File.separator + file;
			Properties p = new Properties();
			p.load(new FileInputStream(peerConf));			
			return p;
		}
		
		private static Node readThisPeer() {
			try {				
				Properties p = prop(configFolder + File.separator  + appConfigFile);
				boolean noId = false;
				String cId = properties.getProperty(Literals.NODE_ID).trim();
				if(cId.isEmpty()) {
					noId = true;
					cId = UUID.randomUUID().toString();
					p.setProperty(Literals.NODE_ID, cId);
				}

				String cAddress = properties.getProperty(Literals.NODE_ADDRESS).trim();
				Integer cProtocolPort = Integer.parseInt(properties.getProperty(Literals.NODE_PROTOCOL_PORT).trim());
				Integer cServicePort = Integer.parseInt(properties.getProperty(Literals.NODE_SERVER_PORT).trim());
				String cTimeZone = properties.getProperty(Literals.NODE_TIME_ZONE).trim();
				
				
				Node node = new Node(cId, cAddress, cProtocolPort, cServicePort, TimeZone.getTimeZone(cTimeZone));
				
				if(noId) {
					OutputStream outFile = new FileOutputStream(appHome + File.separator + configFolder + 						
						File.separator + appConfigFile);
					p.store(outFile, (noId ? "#The node id has been generated by the program" : ""));				

				}				
				return node;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}			
		}
				
		/*private static HashMap<String, String> readAppConfig() {
			try {
				Properties p = prop(configFolder + File.separator  + Literals.APP_PROP_FILE);
				
				HashMap<String, String> map = new HashMap<String, String>();
				
				for(Object key : p.keySet()) map.put(key.toString().trim(), p.getProperty(key.toString()).trim());
				
				return map;
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
			
		}*/
		
		private static int containsOptions(ApplicationArguments args, String... options) {		
			int count = 0;
			
			for(String o : options) if(args.containsOption(o)) count++;
			
			if(count == options.length) return 1;
			return (count == 0 ? 0 : -1);
			
		}
		
		private static void setSeedNodes(ApplicationArguments args) throws Exception {
			int count = 1;
			do {
				int contains = containsOptions(args, Literals.NODE_ID + "." + count, 
						Literals.NODE_ADDRESS + "." + count,
						Literals.NODE_PROTOCOL_PORT + "." + count,
						Literals.NODE_SERVER_PORT + "." + count,
						Literals.NODE_TIME_ZONE + "." + count
						);
				
				if(contains == 0) break;
				if(contains == -1) throw new Exception("Error reading config for node " + count + 
						", some attributes are missing or wrongly configured");
				
				try {				
					
					String cId = args.getOptionValues(Literals.NODE_ID + "." + count).get(0);
					String cAddress = args.getOptionValues(Literals.NODE_ADDRESS + "." + count).get(0);
					int cProtocolPort = Integer.parseInt(args.getOptionValues(Literals.NODE_PROTOCOL_PORT + "." + count).get(0));
					int cServicePort = Integer.parseInt(args.getOptionValues(Literals.NODE_SERVER_PORT + "." + count).get(0));
					String cTimeZone = args.getOptionValues(Literals.NODE_TIME_ZONE + "." + count).get(0);
					
					Node n = new Node(cId, cAddress, cProtocolPort, cServicePort, TimeZone.getTimeZone(cTimeZone));
					Config.SEEDS.add(n);

				} catch(Exception e) {
					throw new Exception("The node " + count + " is not configured properly.");
				}
				count++;
			} while(true);
			
			if(args.containsOption(Literals.APP_MODE)) {
				String mode = args.getOptionValues(Literals.APP_MODE).get(0);
				if(!mode.equals(Literals.APP_DEBUG_MODE) && !mode.equals(Literals.APP_RELEASE_MODE)) throw new Exception("Invalid mode argument");				
				Config.MODE[0] = mode;
			}			
		}
	}
}
