package de.htw.berlin.steganography.apis.imgur;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.List;

import de.htw.berlin.steganography.apis.models.PostEntry;

public class ImgurUtilTest extends TestCase {
    ImgurUtil util = new ImgurUtil();
/*
    public void testGetPostsNoPostEntryForNoPicture() {
        List<PostEntry> postEntryList = util.getPosts("test", getResponse);
        postEntryList.stream().forEach(r -> System.out.println( r + ""));

        postEntryList.stream().forEach(pe -> Assert.assertNotEquals("https://www.reddit.com/r/test/comments/l7frq2/a/", pe.getUrl()));
    }

    public void testGetPostsByImgurUrl() {
        List<PostEntry> postEntryList = util.getPosts("test", getResponse);
        postEntryList.stream().forEach(r -> System.out.println( r + ""));

        //title = "test"
        Assert.assertTrue(postEntryList.size() == 2);
        Assert.assertTrue(postEntryList.get(0).getUrl().contains("https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.png?auto=webp&s=9bd0364de4c82a2bfdd18216b3dd85d02c4f1440"));
        Assert.assertTrue(postEntryList.get(1).getUrl().equals("https://imgur.com/asdai121a.png"));
    }

    public void testGetPosts() {
        List<PostEntry> postEntryList = util.getPosts("test", getResponse);
        postEntryList.stream().forEach(r -> System.out.println( r + ""));

        //title = "test" && has image in source
        Assert.assertTrue(postEntryList.get(0).getUrl().contains("https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.png?auto=webp&s=9bd0364de4c82a2bfdd18216b3dd85d02c4f1440"));
    }
*/
}