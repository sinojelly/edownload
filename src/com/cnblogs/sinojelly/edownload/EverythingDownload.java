package com.cnblogs.sinojelly.edownload;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

/**
 * Download file from Everything http server without absolute path.
 * 
 * Usage: 
 *   edownload [-v -o local] url
 * 
 * Options:
 *   -v : verbose infomation.   (support since version 1.2)
 *   -o local : specify local file path. (default current directory.)  (support since version 1.2)
 * 
 * @version 1.1
 * 
 * @author Jelly
 *
 */

public class EverythingDownload {

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        final String URL_SEPARATOR = "/?s=";
        
        if (args.length <= 0 || args[0].equals("--help")) { // without parameter
            System.out.println("edownload version: 1.1");
            System.out.println("");
            System.out.println("Usage:");
            System.out.println("  edownload url");
            System.out.println("");
            System.out.println("url: should include \""+URL_SEPARATOR+"\", and ends with the file's full name which is searched.");
            System.out.println("");
            System.out.println("eg: edownload http://localhost/?s=bin2txt.bat");
            return;
        }
        
        String inputUrl = args[0]; //"http://localhost/?s=bin2txt.bat";        
        
        int separateIndex = inputUrl.indexOf(URL_SEPARATOR);
        
        if (separateIndex < 0) {
            System.err.println("Error: Input URL invalid, should include \"" + URL_SEPARATOR + "\"");
        }
        
        String baseUrl = inputUrl.substring(0, separateIndex);
        String fileName = inputUrl.substring(separateIndex + URL_SEPARATOR.length());
        
        System.out.println("Base URL : " + baseUrl);
        System.out.println("File Name: " + fileName);
        
        String filePath = null;
        
        try{
            Parser parser = new Parser( (HttpURLConnection) (new URL(inputUrl)).openConnection() ); // TODO: treat with open connection timeout.

            NodeFilter withTagA = new TagNameFilter ("a");
            NodeFilter withAttributeClass = new HasAttributeFilter("class");
            NodeFilter tagAwithClass = new AndFilter(withTagA, withAttributeClass);
            
            NodeFilter withTagTd = new TagNameFilter("td");
            NodeFilter withAttributeNowrap = new HasAttributeFilter("nowrap");
            NodeFilter tagTdwithNowrap = new AndFilter(withTagTd, withAttributeNowrap);
            NodeFilter hasParentTagTd = new HasParentFilter(tagTdwithNowrap);
            
            NodeFilter filter = new AndFilter(tagAwithClass, hasParentTagTd);
            
            NodeList nodes = parser.extractAllNodesThatMatch(filter); 
            
            if(nodes!=null) {
                for (int i = 0; i < nodes.size(); i++) {
                    Node textnode = (Node) nodes.elementAt(i);
                    String href = ((TagNode)textnode).getAttribute("href");
                    filePath = baseUrl+href;
                    if (filePath.endsWith("/" + fileName)) {
                        break;
                    }
                }
            }
            
            if (filePath != null) {
                System.out.println("Download : " + filePath);
                String destDir = System.getProperty("user.dir"); // download to current directory.
                System.out.println("To directory: " + destDir);
                String result = downloadFromUrl(filePath, destDir);
                System.out.println("Result : " + result);
            } else {
                System.err.println("Error: Can not find " + fileName);
            }

        }
        catch( Exception e ) {            
        }
    }
    
    
    public static String downloadFromUrl(String url,String dir) {     
        
        try {     
            URL httpurl = new URL(url);     
            String fileName = getFileNameFromUrl(url);     
            File f = new File(dir + System.getProperty("file.separator")+ fileName);     
            System.out.println("Local file path : " + f.getAbsolutePath());
            FileUtils.copyURLToFile(httpurl, f);   
        } catch (Exception e) {     
            e.printStackTrace();     
            return "Fault!";     
        }      
        return "Successful!";     
    }     
         
    public static String getFileNameFromUrl(String url){     
        String name = new Long(System.currentTimeMillis()).toString() + ".X";     
        int index = url.lastIndexOf("/");     
        if(index > 0){     
            name = url.substring(index + 1);     
            if(name.trim().length()>0){     
                return name;     
            }     
        }     
        return name;     
    } 

}
