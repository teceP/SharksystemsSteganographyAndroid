package de.htw.berlin.steganography.apis.utils;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.htw.berlin.steganography.apis.SocialMedia;
import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.reddit.Reddit;

public class BaseUtilTest extends TestCase {

    public void testUpdateListeners() {
     /*    BaseUtil util = new BaseUtil();
        SocialMedia socialMedia = new Reddit();
        List<String> msgList = new ArrayList<>();
        msgList.add("new");
        msgList.add("msg");
        util.updateListeners(socialMedia, msgList);*/
    }

    public void testSortPostEntries() {
        List<PostEntry> postEntries = new ArrayList<>();
        postEntries.add(new PostEntry("bUrl", new MyDate(new Date(4000)), "aType"));
        postEntries.add(new PostEntry("aUrl", new MyDate(new Date(3000)), "aType"));
        postEntries.add(new PostEntry("bUrl", new MyDate(new Date(6000)), "aType"));

        BaseUtil.sortPostEntries(postEntries);
        Assert.assertTrue(postEntries.get(0).getDate().getTime() == 3000);
        Assert.assertTrue(postEntries.get(1).getDate().getTime() == 4000);
        Assert.assertTrue(postEntries.get(2).getDate().getTime() == 6000);
    }

    public void testSetLatestPostTimestamp() {
       /* BaseUtil util = new BaseUtil();
        SocialMedia socialMedia = new Reddit();
        socialMedia.subscribeToKeyword("keyword");
        util.setLatestPostTimestamp(socialMedia, "keyword", new MyDate(new Date(1000000)));
        System.out.println(socialMedia.getLastTimeCheckedForKeyword("keyword"));*/
    }

    public void testGetLatestStoredTimestamp() {
    }

    public void testGetKeywordAndLastTimeCheckedMap() {
    }

    public void testGetTimestamp() {
    }

    public void testHasErrorCode() {
    }

    public void testElimateOldPostEntries() {
    }

    public void testDecodeUrl() {
    }
}