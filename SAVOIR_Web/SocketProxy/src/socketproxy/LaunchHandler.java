// Licensed under Apache 2.0
// Copyright 2011, National Research Council of Canada
// Property of Lakehead University

package socketproxy;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
/**
 * Testing class to launch something given a path
 * 
 * @author Aaron Moss
 */
public class LaunchHandler {

	private static Desktop desktop;
	private static boolean canLaunchBrowser;
	
	static {
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			canLaunchBrowser = desktop.isSupported(Desktop.Action.BROWSE);
		} else {
			desktop = null;
			canLaunchBrowser = false;
		}
	}
	private static String homeDir = System.getProperty("user.home");
	/** File location of properties file */
	private static File propertiesFile = new File(
			System.getProperty("user.home") + File.separator + 
			".savoirLaunch.properties");
	
	/** mappings of names to paths for "savoir" URI scheme */
	private static Properties nameMappings = null;
	
	/**
	 * Operating systems 
	 */
	private enum OS {
		WINDOWS,
		MAC,
		UNSUPPORTED;
	}
	
	/** default properties for Windows */
	private static final Properties DEFAULT_MAPPINGS_WIN = new Properties();
	/** default properties for Mac */
	private static final Properties DEFAULT_MAPPINGS_MAC = new Properties();
	
	/** System-dependent default properties */
	private static EnumMap<OS, Properties> defaultMappings = 
		new EnumMap<OS, Properties>(OS.class);
	
	static {
		DEFAULT_MAPPINGS_WIN.put("Notepad", "C:\\WINDOWS\\Notepad.exe");
		//etc. more like this
		
		defaultMappings.put(OS.WINDOWS, DEFAULT_MAPPINGS_WIN);
		defaultMappings.put(OS.MAC, DEFAULT_MAPPINGS_MAC);
		defaultMappings.put(OS.UNSUPPORTED, new Properties());
	}
	
	/** Comment for generated properties files */
	private static final String PROPERTIES_COMMENT = 
		"Auto-generated default properties file.\n" +
		"Keys in this file are names of SAVOIR resources, while values are the local \n" +
		"paths of the executable for that resource.";
	private static String FILE_SEPARATOR = System.getProperty("file.separator");
	
	/**
	 * Thread for piping an input stream to an output stream.
	 * Will close input stream on termination, leaves output stream open.
	 */
	private static class PrintThread extends Thread {
		
		/** Name of process this is running */
		private String exeName;
		/** PrintStream from the process */
		private BufferedReader in;
		/** PrintStream to output to */
		private PrintStream out;
		
		public PrintThread(String exeName, InputStream in, PrintStream out) {
			this.exeName = exeName;
			this.in = new BufferedReader(new InputStreamReader(in));
			this.out = out;
		}
		
		public void run() {
			try {
				String line = in.readLine();
				while (line != null) {
					out.println("[" + exeName + "]\t" + line);
					line = in.readLine();
				}
			} catch (IOException e) {
				System.err.print("[" + exeName + "] ERROR: ");
				e.printStackTrace();
			} finally {
				try {
					in.close();
				} catch (IOException e) {
					System.err.print("[" + exeName + "] ERROR: ");
					e.printStackTrace();
				}
			}
		}
	}
	
	/** gets protocol as capture group 1, path as capture group 2 */
	private static Pattern uriPattern = Pattern.compile("(.+?)://(.*)");
	
	/**
	 * Launches the resource at the specified path.
	 * 
	 * @param uri	The path to launch. Can have one of the following 
	 * 				protocol strings:
	 * 				<ul>
	 * 				<li><b>http[s]</b>: launches a web browser to the given 
	 * 					URL.
	 * 				<li><b>file</b>: runs the executable at the given location 
	 * 					(if such exists)
	 * 				<li><b>savoir</b>: loads the executable path for the given 
	 * 					name fron the SAVOIR properties file (or system default 
	 * 					if no such file)
	 * 				</ul>
	 */ 
	public static void launch(String uri) {
		if (uri == null) {
			System.err.println("Invalid URI - cannot be null");
			return;
		}
		
		Matcher match = uriPattern.matcher(uri);
		
		//escape hatch for invalid URI
		if (!match.matches()) {
			System.err.println("Invalid URI `" + uri + "`");
			return;
		}
		
		String protocol = match.group(1).toLowerCase();
		String path = match.group(2);
		
		if ("http".equals(protocol) || "https".equals(protocol)) {
			browse(uri);
		} else if ("file".equals(protocol)) {
			//NOTE: I don't like this behaviour, from a security perspective, 
			// and we probably shouldn't do it.
			run(path);
		} else if ("savoir".equals(protocol)) {
			//parse out arguments
			String[] cmdArgs;	//the command must be index 0, 
								// followed by any arguments 
			
			int queryStart = path.indexOf('?');
			if (queryStart == -1) {
				//no arguments
				cmdArgs = new String[]{path};
			} else {
				//command line args
				//split arguments
				String[] args = path.substring(queryStart + 1).split("&");
				cmdArgs = new String[1 + args.length];
				
				//parse out command
				cmdArgs[0] = path.substring(0, queryStart);
				//decode arguments
				try {
					for (int i = 0; i < args.length; i++) {
						cmdArgs[i+1] = URLDecoder.decode(args[i], "UTF-8");
					}
				} catch (UnsupportedEncodingException wontHappen) {}
			}
			
			//run program
                        String savoirProgArgs[] = processSavoirLaunchProp(cmdArgs);

			//String savoirPath = getNameMappings().getProperty(cmdArgs[0]);
                        String savoirPath = savoirProgArgs[0];
			if (savoirPath == null) {
				System.err.println("No mapping in properties file for `" +
						cmdArgs[0] + "`");
				return;
			}
			
			//cmdArgs[0] = savoirPath;
			
			
                        debug("cmdArgs:" + Arrays.toString(savoirProgArgs));
			run(savoirProgArgs);
		}
	}

        private static String[] processSavoirLaunchProp(String[] savoirAppArgs){
            String[] cmdArgs;
            String savoirURI = getNameMappings().getProperty(savoirAppArgs[0]);
            debug("savoirURI = " + savoirURI);
            if (savoirURI.indexOf('?') == -1) {
                cmdArgs = new String[1];
                cmdArgs[0] = savoirURI;
                debug("cmdArgs" + cmdArgs[0]);
            } else {

                String[] path = savoirURI.split("\\?");
                debug("savoirPath = " + path[0]);
                String fileTypeStr = path[1];
                String[] typeStrAry = fileTypeStr.split(":");
                debug("savoirFile = " + typeStrAry[1]);
                String fileType = typeStrAry[1];
                if(fileType.indexOf("jar") != -1 && fileType.trim().length() == 3){
                   cmdArgs = new String[3 + savoirAppArgs.length];
                   cmdArgs[0] = "java";
                   cmdArgs[1] = "-jar";
                   cmdArgs[2] = path[0];
                   for(int j = 1; j < savoirAppArgs.length; j++){
                       cmdArgs[2 + j] = savoirAppArgs[j];
                   }
                   debug("cmdArgs = " + savoirAppArgs.toString());
                }else{
                String fullFileName = savoirAppArgs[0] + "." + fileType;
                cmdArgs = new String[2];
                cmdArgs[0] = path[0];
                //int indexExeDir = path[0].lastIndexOf("\\");
                int indexExeDir = path[0].lastIndexOf(FILE_SEPARATOR);
                String exeDir = path[0].substring(0,indexExeDir);
                String argsStr = "";
                for (int i = 1; i < savoirAppArgs.length; i++) {
                    argsStr = savoirAppArgs[i] + " ";
                }
                debug(argsStr + ";" + exeDir + ";" + fullFileName);
                createLaunchArgsFile(argsStr, exeDir, fullFileName);
                cmdArgs[1] = exeDir + File.separator + fullFileName;
                }
            }
            return cmdArgs;
        }
	
	/**
	 * Opens a browser to the given URI, if possible
	 * 
	 * @param uri		The URI to open
	 */
	public static void browse(String uri) {
		if (!canLaunchBrowser) {
			System.err.println("Cannot launch browser");
			return;
		}
		
		try {
			desktop.browse(new URI(uri));
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Runs the executable at the given path, if possible
	 * 
	 * @param path		The path of the executable to run
	 * @param args		Command line arguments for the executable
	 */
	public static void run(String... path) {
		if (path.length == 0) {
            return;
        }
        List<String> command = new ArrayList<String>();
        for (int j = 0; j < path.length; j++) {
            if (path[j] != null) {
                command.add(path[j]);
            }
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(new File(homeDir));
        if (path[0].indexOf("java") == -1) {
            File exePath = new File(path[0]);
            final String exeName = exePath.getName();

            if (!exePath.exists()) {
                System.err.println("No file at path `" + path + "`");
                return;
            }
            try {
                //launch command
                Process process = builder.start();

                //copy output and error streams to stdout, stderr
                new PrintThread(exeName, process.getInputStream(), System.out).start();
                new PrintThread(exeName, process.getErrorStream(), System.err).start();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        } else {
            //final String javaName = "java";
            //prepare to launch command at path
            File javaAppPath = new File(path[2]);
            final String javaName = javaAppPath.getName();

            try {
                //launch command
                Process process = builder.start();

                //copy output and error streams to stdout, stderr
                new PrintThread(javaName, process.getInputStream(), System.out).start();
                new PrintThread(javaName, process.getErrorStream(), System.err).start();

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
	}
	
	/**
	 * Gets the properties file containing the SAVOIR file mappings
	 * 
	 * @return the SAVOIR name mappings properties file
	 */
	public static Properties getNameMappings() {
		debug(propertiesFile.getAbsolutePath());
		
		if (nameMappings == null) {
			if (propertiesFile.exists()) {
				debug("properties file exists");
				//we already have a properties file - load
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(propertiesFile));
					nameMappings = new Properties();
					try {
						nameMappings.load(reader);
						debug("loaded from file");
						return nameMappings;
					} catch(IOException e) {
						e.printStackTrace();
						nameMappings = null;
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch(IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
			
			
			//properties file doesn't exist or failed to load
			//load defaults
			OS os = getOS();
			if (os == OS.UNSUPPORTED) {
				System.err.println("Unsupported operating system `" + 
						System.getProperty("os.name") + "`");
			}
			
			debug("OS is " + (os == OS.WINDOWS ? "Windows" : "Mac"));
			
			//sets new name mappings, and writes to disk
			setNameMappings(defaultMappings.get(os));
			
			return nameMappings;			
		} else {			
			return nameMappings;
		}
	}
	
	/**
	 * Sets the name mappings file
	 * @param properties	The properties to write (will be ignored if null)
	 */
	public static void setNameMappings(Properties properties) {
		if (properties == null) {
			System.err.println("Invalid properties file: cannot be null");
			return;
		}
		
		//overwrite live copy
		nameMappings = properties;
		
		//write mappings to file (fails cleanly)
		FileWriter writer = null;
		try {
			writer = new FileWriter(propertiesFile);
			nameMappings.store(writer, PROPERTIES_COMMENT);
			debug("wrote new properties file to disk");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				writer = null;
			}
		}
	}
	
	private static OS getOS() {
		String osName = System.getProperty("os.name");
		
		if (osName == null || osName.isEmpty()) return OS.UNSUPPORTED;
		
		if (osName.startsWith("Windows")) return OS.WINDOWS;
		if (osName.startsWith("Mac")) return OS.MAC;
		
		return OS.UNSUPPORTED;
	}
	
	
	private static boolean debugging = true;
	private static void debug(String line) {
		if (!debugging) return;
		System.err.println("DEBUG" + ((line == null) ? "" : ": " + line));
	}

        private static void createLaunchArgsFile(String chanArgs, String dir, String fileName) {
		try {
			// Create launch args file
			FileWriter fileStream = new FileWriter(dir.trim() + File.separator
					+ fileName);
			BufferedWriter output = new BufferedWriter(fileStream);
			output.write(chanArgs.trim());
                        output.flush();
			output.close();
                        fileStream.close();
		} catch (Exception e) {// Catch exception if any
			// zzz log and raise properly
			System.out.println(e.toString());
		}
	}
}
