import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.BinaryParseData;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler {
	//private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|gif|jpg" + "|png|mp3|mp4|zip|gz))$");
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(xml|css|js" + "|mid|mp2|mp3|mp4|docx"
		      + "|wav|avi|mov|mpeg|ram|m4v|" + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");

	private static final Pattern binaryPatterns = Pattern.compile(".*(\\.(jpg|tif|ico|doc|pdf|bmp|gif|jpe?g|png|tiff?))$");
	private final static String root = "http://www.bbc.com/news";
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		//String href = url.getURL().toLowerCase();
		String href = url.getURL();
		boolean isInnerLink =  href.startsWith(root);//("http://www.nytimes.com/");
		try {
			String fileName = "urls.csv";
			File urls = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(urls, true));
    	
			//System.out.println("size: " + page.getContentData().length + "b");
			bw.write("\"" + href + "\",\"" + (isInnerLink? "OK" : "N_OK") + "\"");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (binaryPatterns.matcher(href).matches() && isInnerLink) {
			return true;
		}
		boolean isHtml = true;
		if (referringPage.getContentType() != null) {
			isHtml = referringPage.getContentType().contains("text/html");
		}
		return !FILTERS.matcher(href).matches() && isInnerLink && isHtml;
		//return isInnerLink && isHtml;
	}
	
	@Override
	public void visit(Page page) {
		String url = page.getWebURL().getURL();
		System.out.println("URL: " + url);
		
		if (page.getParseData() instanceof HtmlParseData) {
			if (page.getContentType().contains("text/html")) {
				HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
				Set<WebURL> links = htmlParseData.getOutgoingUrls();
				try {
						String fileName = "visit.csv";
						File visit = new File(fileName);
						BufferedWriter bw = new BufferedWriter(new FileWriter(visit, true));
			    	
						//System.out.println("size: " + page.getContentData().length + "b");
						boolean isOutLink = false;
						int outLinkCounter = 0;
						for (WebURL linkUrl : links) {
							isOutLink =  linkUrl.getURL().startsWith(root);
							if (!isOutLink) {
								outLinkCounter++;
							}
						}
						bw.write("\"" + page.getWebURL().getURL()+"\",\"" + page.getContentData().length+"\",\""+outLinkCounter+"\",\""+page.getContentType()+"\"");
						bw.newLine();
						bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				System.out.println("..");
			}
		} else if (page.getParseData() instanceof BinaryParseData){
			try {
				String fileName = "visit.csv";
				File visit = new File(fileName);
				BufferedWriter bw = new BufferedWriter(new FileWriter(visit, true));
	    	
				//System.out.println("size: " + page.getContentData().length + "b");
				int outLinkCounter = 0;
				bw.write("\"" + page.getWebURL().getURL()+"\",\"" + page.getContentData().length+"\",\""+outLinkCounter+"\",\""+page.getContentType()+"\"");
				bw.newLine();
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		} else {
			System.out.println("...");
		}
	}
	
	@Override
	protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
		
		if (statusCode != HttpStatus.SC_OK) {

			if (statusCode == HttpStatus.SC_NOT_FOUND) {
				logger.warn("Broken link: {}, this link was found in page: {}", webUrl.getURL(), webUrl.getParentUrl());
			} else {
				logger.warn("Non success status for link: {} status code: {}, description: ", webUrl.getURL(), statusCode, statusDescription);
			}
		}
		try {
			String fileName = "fetch.csv";
			File fetch = new File(fileName);
			BufferedWriter bw = new BufferedWriter(new FileWriter(fetch, true));
    	
			//System.out.println("size: " + page.getContentData().length + "b");
			bw.write("\"" + webUrl.getURL()+"\",\"" + statusCode + "\"");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
