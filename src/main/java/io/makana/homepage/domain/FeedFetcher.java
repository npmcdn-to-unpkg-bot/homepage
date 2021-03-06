package io.makana.homepage.domain;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Component;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

@Component
public class FeedFetcher {

    public static final int MAX_PER_SOURCE = 20;

    public NewsFeed buildFeed(String url) throws Exception {
        URLConnection urlConnection = new URL(url).openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
        String html = IOUtils.toString(urlConnection.getInputStream());

        BOMInputStream bomIn = new BOMInputStream(IOUtils.toInputStream(html));
        String f = IOUtils.toString(bomIn);
        Reader reader = new StringReader(f);

        SyndFeedInput input = new SyndFeedInput();
        SyndFeed feed = input.build(reader);

        NewsFeed newsFeed = new NewsFeed();
        newsFeed.setName(feed.getTitle());
        newsFeed.setUrl(feed.getLink());
        if (feed.getImage() != null &&
                feed.getImage().getUrl() != null &&
                !feed.getImage().getUrl().isEmpty()) {
            newsFeed.setImageUrl(feed.getImage().getUrl());
        }

        if (feed.getEntries() != null && !feed.getEntries().isEmpty()) {
            int feedCount = 0;
            Iterator i = feed.getEntries().iterator();
            while (i.hasNext() && feedCount <= MAX_PER_SOURCE) {
                SyndEntry entry = (SyndEntry) i.next();
                FeedItem feedItem = new FeedItem();
                feedItem.setUrl(entry.getLink());
                feedItem.setSubject(entry.getTitle());
                newsFeed.addFeedItem(feedItem);
                feedCount++;
            }
        }
        return newsFeed;
    }
}
