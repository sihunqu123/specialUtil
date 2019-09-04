package util.specialUtil;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.media.ComMediaUtil;

public class TelegramAVHelper {

	private static List<String> acceptImageExtension = new ArrayList<String>();

	static {
		acceptImageExtension.add("gif");
		acceptImageExtension.add("png");
		acceptImageExtension.add("jpg");
		acceptImageExtension.add("jpeg");
	}

    private final List<String> doneList = new ArrayList<String>();
    private final Map<String, Long> doneMap = new HashMap<String, Long>();
    private Integer max = null;
    private File listMd = null;

    private File dir = null;

	public TelegramAVHelper(File dir) {
		this.dir = dir;
        max = getMaxNumber(dir);
        listMd = new File(dir, "list.md");
	}

	public Integer getMaxNumber(File dir) throws NumberFormatException{

		File[] listFiles = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				return acceptImageExtension.contains(ComFileUtil.getFileExtension(name, false).toLowerCase());
			}
		});
		// get the max index number
		Integer max = 0;
		for(int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			String name = ComFileUtil.getFileInfo(file).getFileName();
			int number = Integer.parseInt(name);
			if(number > max) {
				max = number;
			}
		}
		return max;
	}

    public void watchDirectoryPath() throws Exception {
    	Path path = dir.toPath();
        // Sanity check - Check if path is a folder
        Boolean isFolder = (Boolean) Files.getAttribute(path, "basic:isDirectory", NOFOLLOW_LINKS);
        if (!isFolder) {
            throw new IllegalArgumentException("Path: " + path + " is not a folder");
        }

        ComLogUtil.info("Watching path: " + path);

        // We obtain the file system of the Path
        FileSystem fs = path.getFileSystem();

        // We create the new WatchService using the new try() block
        try (WatchService service = fs.newWatchService()) {

            // We register the path to the service
            // We watch for creation events
        	path.register(service, ENTRY_CREATE
        			// I don't want to watch for modify/delete events
        			, ENTRY_MODIFY
//        			, ENTRY_DELETE
        			);

            // Start the infinite polling loop
        	WatchKey key = null;

            while (true) {
                key = service.take();

                // Dequeueing events
                Kind<?> kind = null;
                for (WatchEvent<?> watchEvent : key.pollEvents()) {
                    // Get the type of the event
                    kind = watchEvent.kind();
                    if (OVERFLOW == kind) {
                    	ComLogUtil.info("kind = OVERFLOW. Will continue");
                        continue; // loop
                    } else if (ENTRY_CREATE == kind) {
                        // A new Path was created
                    	Path newPath = ((WatchEvent<Path>) watchEvent).context();
                    	ComLogUtil.info("create:" + newPath);
                    	handleFile(newPath);
                    } else if (ENTRY_MODIFY == kind) {
                        // modified
                    	Path newPath = ((WatchEvent<Path>) watchEvent).context();
                    	ComLogUtil.info("modify:" + newPath);
                    	handleFile(newPath);
                    } else if (ENTRY_DELETE == kind) {
                    	Path newPath = ((WatchEvent<Path>) watchEvent).context();
                        ComLogUtil.info("New path delete: " + newPath);
                    	ComLogUtil.info("on ENTRY_DELETE");
                    }
                }

                if (!key.reset()) {
                    break; // loop
                }
            }

        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }

    }

    private void handleFile(Path newPath) throws Exception {
        String newFileName = newPath.toString();
        File newFile = new File(dir, newFileName);

        if(shouldIgnore(newFile)) return;

        ComLogUtil.info("Event from  file: " + newFileName);
        if(newFileName.toLowerCase().endsWith(".mp4")) {
        	// sleep 2 sec to let files saved completely.
        	Thread.sleep(8000);

        	if(!ComMediaUtil.videoToGif(newFile, new File(dir, ++max + ".gif"))) {
        		throw new Exception("convert " + newFileName + " to gif failed!");
        	}
        	// add image preview code into listMd
        	ComFileUtil.appendString2File("\n\n![](" + max + ".gif)\n", listMd, "UTF-8");
        	doneList.add(newFileName);
        	doneMap.put(newFileName, newFile.length());
        }
    }

    private Boolean shouldIgnore(File newFile) {
    	String newFileName = ComFileUtil.getFileName(newFile, true);
    	if(doneList.contains(newFileName)) { // files that are already done.
    		long newFileLength = newFile.length();
    		Long oldFileLength = doneMap.get(newFileName);
    		ComLogUtil.info(" newFileLength: " + newFileLength + ", oldFileLength: " + oldFileLength);
    		if(newFileLength == oldFileLength) {
    			ComLogUtil.info("ignore done file: " + newFileName);
    			return true;
    		}
        }
        if(newFileName.startsWith("escapedFile_")) { // if it's the tmp file that should be ignored.
        	ComLogUtil.info("ignore escapedFile_ file: " + newFileName);
        	return true;
        }
        return  false;
    }

    public static void main(String[] args) throws Exception {
        // Folder we are going to watch
        // Path folder =
        // Paths.get(System.getProperty("C:\\Users\\Isuru\\Downloads"));
        File dir = new File("E:\\toDownload\\avid");
        new TelegramAVHelper(dir).watchDirectoryPath();
    }
}
