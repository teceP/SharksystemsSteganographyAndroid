package de.htw.berlin.steganography.apis.reddit;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import junit.framework.TestCase;

import org.junit.Assert;

import java.util.List;

import de.htw.berlin.steganography.apis.models.MyDate;
import de.htw.berlin.steganography.apis.models.PostEntry;
import de.htw.berlin.steganography.apis.reddit.models.RedditGetResponse;

public class RedditUtilTest extends TestCase {
    private RedditUtil util = new RedditUtil();

    public void testGetUrl() {
        RedditGetResponse getResponseObj = new Gson().fromJson(getResponse, RedditGetResponse.class);
        String url = util.getUrl(getResponseObj.getData().getChildren().get(0));
        Assert.assertTrue(url.contains("https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.png?auto=webp&s=9bd0364de4c82a2bfdd18216b3dd85d02c4f1440"));

        url = util.getUrl(getResponseObj.getData().getChildren().get(2));
        Assert.assertTrue(url.contains("https://imgur.com/asdai121a.png"));
    }

    public void testGetUrlNoValidUrl() {
        RedditGetResponse getResponseObj = new Gson().fromJson(getResponse, RedditGetResponse.class);
        String url = util.getUrl(getResponseObj.getData().getChildren().get(0));
        Assert.assertTrue(url.contains("https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.png?auto=webp&s=9bd0364de4c82a2bfdd18216b3dd85d02c4f1440"));

        url = util.getUrl(getResponseObj.getData().getChildren().get(1));
        Assert.assertTrue(url.equals(""));
    }

    public void testGetTimestamp() {
        RedditGetResponse getResponseObj = new Gson().fromJson(getResponse, RedditGetResponse.class);
        MyDate myDate = util.getTimestamp(getResponseObj.getData().getChildren().get(0));
        Assert.assertTrue(myDate.getTime() == 1611915822000L);

        myDate = util.getTimestamp(getResponseObj.getData().getChildren().get(1));
        Assert.assertTrue(myDate.getTime() == 1611913930000L);

        myDate = util.getTimestamp(getResponseObj.getData().getChildren().get(2));
        Assert.assertTrue(myDate.getTime() == 1611908481000L);
    }

    public void testGetPostsNoPostForNotPresentKeyword() {
        List<PostEntry> postEntryList = util.getPosts("diesesKeywordIstNichtDabei", getResponse);
        postEntryList.stream().forEach(r -> System.out.println( r + ""));

        Assert.assertTrue(postEntryList.size() == 0);
    }

    public void testGetPostsCheckTimestampsMs() {
        List<PostEntry> postEntryList = util.getPosts("test", getResponse);
        postEntryList.stream().forEach(r -> System.out.println( r + ""));

        //title = "test"
        Assert.assertTrue(postEntryList.size() == 2);
        Assert.assertTrue(postEntryList.get(0).getDate().getTime() == 1611915822000L);
        Assert.assertTrue(postEntryList.get(1).getDate().getTime() == 1611908481000L);
    }

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

    public void testGetResponse() {
        RedditGetResponse getResponseObj = new Gson().fromJson(getResponse, RedditGetResponse.class);

        Assert.assertTrue(getResponseObj.getData().getChildren().size() == 3);

        //Title matching
        Assert.assertTrue(getResponseObj.getData().getChildren().get(0).getData().getTitle().contains("test"));
        Assert.assertTrue(getResponseObj.getData().getChildren().get(1).getData().getTitle().contains("A"));
        Assert.assertTrue(getResponseObj.getData().getChildren().get(2).getData().getTitle().contains("Video post"));
    }

    private String getResponse =
            "{\n" +
                    "    \"kind\": \"Listing\",\n" +
                    "    \"data\": {\n" +
                    "        \"modhash\": \"\",\n" +
                    "        \"dist\": 25,\n" +
                    "        \"children\": [\n" +
                    "            {\n" +
                    "                \"kind\": \"t3\",\n" +
                    "                \"data\": {\n" +
                    "                    \"approved_at_utc\": null,\n" +
                    "                    \"subreddit\": \"test\",\n" +
                    "                    \"selftext\": \"\",\n" +
                    "                    \"author_fullname\": \"t2_9qozmt1w\",\n" +
                    "                    \"saved\": false,\n" +
                    "                    \"mod_reason_title\": null,\n" +
                    "                    \"gilded\": 0,\n" +
                    "                    \"clicked\": false,\n" +
                    "                    \"title\": \"test\",\n" +
                    "                    \"link_flair_richtext\": [],\n" +
                    "                    \"subreddit_name_prefixed\": \"r/test\",\n" +
                    "                    \"hidden\": false,\n" +
                    "                    \"pwls\": 6,\n" +
                    "                    \"link_flair_css_class\": null,\n" +
                    "                    \"downs\": 0,\n" +
                    "                    \"thumbnail_height\": 78,\n" +
                    "                    \"top_awarded_type\": null,\n" +
                    "                    \"hide_score\": false,\n" +
                    "                    \"name\": \"t3_l7ggfb\",\n" +
                    "                    \"quarantine\": false,\n" +
                    "                    \"link_flair_text_color\": \"dark\",\n" +
                    "                    \"upvote_ratio\": 1.0,\n" +
                    "                    \"author_flair_background_color\": null,\n" +
                    "                    \"subreddit_type\": \"public\",\n" +
                    "                    \"ups\": 1,\n" +
                    "                    \"total_awards_received\": 0,\n" +
                    "                    \"media_embed\": {},\n" +
                    "                    \"thumbnail_width\": 140,\n" +
                    "                    \"author_flair_template_id\": null,\n" +
                    "                    \"is_original_content\": false,\n" +
                    "                    \"user_reports\": [],\n" +
                    "                    \"secure_media\": null,\n" +
                    "                    \"is_reddit_media_domain\": false,\n" +
                    "                    \"is_meta\": false,\n" +
                    "                    \"category\": null,\n" +
                    "                    \"secure_media_embed\": {},\n" +
                    "                    \"link_flair_text\": null,\n" +
                    "                    \"can_mod_post\": false,\n" +
                    "                    \"score\": 1,\n" +
                    "                    \"approved_by\": null,\n" +
                    "                    \"author_premium\": false,\n" +
                    "                    \"thumbnail\": \"https://b.thumbs.redditmedia.com/eqwx5pFLWM-3eJBvKnGyLQyTnpd57FKWXe8w3JKehJo.jpg\",\n" +
                    "                    \"edited\": false,\n" +
                    "                    \"author_flair_css_class\": null,\n" +
                    "                    \"author_flair_richtext\": [],\n" +
                    "                    \"gildings\": {},\n" +
                    "                    \"post_hint\": \"link\",\n" +
                    "                    \"content_categories\": null,\n" +
                    "                    \"is_self\": false,\n" +
                    "                    \"mod_note\": null,\n" +
                    "                    \"created\": 1611915822.0,\n" +
                    "                    \"link_flair_type\": \"text\",\n" +
                    "                    \"wls\": 6,\n" +
                    "                    \"removed_by_category\": null,\n" +
                    "                    \"banned_by\": null,\n" +
                    "                    \"author_flair_type\": \"text\",\n" +
                    "                    \"domain\": \"medium.com\",\n" +
                    "                    \"allow_live_comments\": false,\n" +
                    "                    \"selftext_html\": null,\n" +
                    "                    \"likes\": null,\n" +
                    "                    \"suggested_sort\": null,\n" +
                    "                    \"banned_at_utc\": null,\n" +
                    "                    \"url_overridden_by_dest\": \"https://medium.com/@edmcclain78/how-20-years-were-wasted-because-of-3144b708c261\",\n" +
                    "                    \"view_count\": null,\n" +
                    "                    \"archived\": false,\n" +
                    "                    \"no_follow\": true,\n" +
                    "                    \"is_crosspostable\": false,\n" +
                    "                    \"pinned\": false,\n" +
                    "                    \"over_18\": false,\n" +
                    "                    \"preview\": {\n" +
                    "                        \"images\": [\n" +
                    "                            {\n" +
                    "                                \"source\": {\n" +
                    "                                    \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.png?auto=webp&amp;s=9bd0364de4c82a2bfdd18216b3dd85d02c4f1440\",\n" +
                    "                                    \"width\": 1200,\n" +
                    "                                    \"height\": 674\n" +
                    "                                },\n" +
                    "                                \"resolutions\": [\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=108&amp;crop=smart&amp;auto=webp&amp;s=472ccd744197f945b93eafdfab1722e783837de7\",\n" +
                    "                                        \"width\": 108,\n" +
                    "                                        \"height\": 60\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=216&amp;crop=smart&amp;auto=webp&amp;s=d645ee977d13c65564c49144bf7781412b2cee69\",\n" +
                    "                                        \"width\": 216,\n" +
                    "                                        \"height\": 121\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=320&amp;crop=smart&amp;auto=webp&amp;s=2250fd174a4e69a7d7d33870adb22b64764b3b1d\",\n" +
                    "                                        \"width\": 320,\n" +
                    "                                        \"height\": 179\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=640&amp;crop=smart&amp;auto=webp&amp;s=32b2707d744ae9e8dceb8feb0bd5f97f013831fc\",\n" +
                    "                                        \"width\": 640,\n" +
                    "                                        \"height\": 359\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=960&amp;crop=smart&amp;auto=webp&amp;s=fad28b180ec6981261481f5b20000807999c0bdf\",\n" +
                    "                                        \"width\": 960,\n" +
                    "                                        \"height\": 539\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/8duKGRTTAysepO3tsyyVa1_64T6LI0glGt8WV2thZDY.jpg?width=1080&amp;crop=smart&amp;auto=webp&amp;s=f91a5d2d03f86a4b899398365f473670e268c11c\",\n" +
                    "                                        \"width\": 1080,\n" +
                    "                                        \"height\": 606\n" +
                    "                                    }\n" +
                    "                                ],\n" +
                    "                                \"variants\": {},\n" +
                    "                                \"id\": \"NI0lLmcQaedGRQ1VuDZonrN95DTVEoIN42aNIT6wBoQ\"\n" +
                    "                            }\n" +
                    "                        ],\n" +
                    "                        \"enabled\": false\n" +
                    "                    },\n" +
                    "                    \"all_awardings\": [],\n" +
                    "                    \"awarders\": [],\n" +
                    "                    \"media_only\": false,\n" +
                    "                    \"can_gild\": false,\n" +
                    "                    \"spoiler\": false,\n" +
                    "                    \"locked\": false,\n" +
                    "                    \"author_flair_text\": null,\n" +
                    "                    \"treatment_tags\": [],\n" +
                    "                    \"visited\": false,\n" +
                    "                    \"removed_by\": null,\n" +
                    "                    \"num_reports\": null,\n" +
                    "                    \"distinguished\": null,\n" +
                    "                    \"subreddit_id\": \"t5_2qh23\",\n" +
                    "                    \"mod_reason_by\": null,\n" +
                    "                    \"removal_reason\": null,\n" +
                    "                    \"link_flair_background_color\": \"\",\n" +
                    "                    \"id\": \"l7ggfb\",\n" +
                    "                    \"is_robot_indexable\": true,\n" +
                    "                    \"report_reasons\": null,\n" +
                    "                    \"author\": \"2021_now\",\n" +
                    "                    \"discussion_type\": null,\n" +
                    "                    \"num_comments\": 1,\n" +
                    "                    \"send_replies\": false,\n" +
                    "                    \"whitelist_status\": \"all_ads\",\n" +
                    "                    \"contest_mode\": false,\n" +
                    "                    \"mod_reports\": [],\n" +
                    "                    \"author_patreon_flair\": false,\n" +
                    "                    \"author_flair_text_color\": null,\n" +
                    "                    \"permalink\": \"/r/test/comments/l7ggfb/test/\",\n" +
                    "                    \"parent_whitelist_status\": \"all_ads\",\n" +
                    "                    \"stickied\": false,\n" +
                    "                    \"url\": \"https://medium.com/@edmcclain78/how-20-years-were-wasted-because-of-porn-3144b708c261\",\n" +
                    "                    \"subreddit_subscribers\": 9269,\n" +
                    "                    \"created_utc\": 1611887022.0,\n" +
                    "                    \"num_crossposts\": 0,\n" +
                    "                    \"media\": null,\n" +
                    "                    \"is_video\": false\n" +
                    "                }\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"kind\": \"t3\",\n" +
                    "                \"data\": {\n" +
                    "                    \"approved_at_utc\": null,\n" +
                    "                    \"subreddit\": \"test\",\n" +
                    "                    \"selftext\": \".\",\n" +
                    "                    \"author_fullname\": \"t2_33gfzua1\",\n" +
                    "                    \"saved\": false,\n" +
                    "                    \"mod_reason_title\": null,\n" +
                    "                    \"gilded\": 0,\n" +
                    "                    \"clicked\": false,\n" +
                    "                    \"title\": \"A\",\n" +
                    "                    \"link_flair_richtext\": [],\n" +
                    "                    \"subreddit_name_prefixed\": \"r/test\",\n" +
                    "                    \"hidden\": false,\n" +
                    "                    \"pwls\": 6,\n" +
                    "                    \"link_flair_css_class\": null,\n" +
                    "                    \"downs\": 0,\n" +
                    "                    \"thumbnail_height\": null,\n" +
                    "                    \"top_awarded_type\": null,\n" +
                    "                    \"hide_score\": false,\n" +
                    "                    \"name\": \"t3_l7frq2\",\n" +
                    "                    \"quarantine\": false,\n" +
                    "                    \"link_flair_text_color\": \"dark\",\n" +
                    "                    \"upvote_ratio\": 1.0,\n" +
                    "                    \"author_flair_background_color\": null,\n" +
                    "                    \"subreddit_type\": \"public\",\n" +
                    "                    \"ups\": 1,\n" +
                    "                    \"total_awards_received\": 0,\n" +
                    "                    \"media_embed\": {},\n" +
                    "                    \"thumbnail_width\": null,\n" +
                    "                    \"author_flair_template_id\": null,\n" +
                    "                    \"is_original_content\": false,\n" +
                    "                    \"user_reports\": [],\n" +
                    "                    \"secure_media\": null,\n" +
                    "                    \"is_reddit_media_domain\": false,\n" +
                    "                    \"is_meta\": false,\n" +
                    "                    \"category\": null,\n" +
                    "                    \"secure_media_embed\": {},\n" +
                    "                    \"link_flair_text\": null,\n" +
                    "                    \"can_mod_post\": false,\n" +
                    "                    \"score\": 1,\n" +
                    "                    \"approved_by\": null,\n" +
                    "                    \"author_premium\": false,\n" +
                    "                    \"thumbnail\": \"self\",\n" +
                    "                    \"edited\": false,\n" +
                    "                    \"author_flair_css_class\": null,\n" +
                    "                    \"author_flair_richtext\": [],\n" +
                    "                    \"gildings\": {},\n" +
                    "                    \"content_categories\": null,\n" +
                    "                    \"is_self\": true,\n" +
                    "                    \"mod_note\": null,\n" +
                    "                    \"created\": 1611913930.0,\n" +
                    "                    \"link_flair_type\": \"text\",\n" +
                    "                    \"wls\": 6,\n" +
                    "                    \"removed_by_category\": null,\n" +
                    "                    \"banned_by\": null,\n" +
                    "                    \"author_flair_type\": \"text\",\n" +
                    "                    \"domain\": \"self.test\",\n" +
                    "                    \"allow_live_comments\": false,\n" +
                    "                    \"selftext_html\": \"&lt;!-- SC_OFF --&gt;&lt;div class=\\\"md\\\"&gt;&lt;p&gt;.&lt;/p&gt;\\n&lt;/div&gt;&lt;!-- SC_ON --&gt;\",\n" +
                    "                    \"likes\": null,\n" +
                    "                    \"suggested_sort\": null,\n" +
                    "                    \"banned_at_utc\": null,\n" +
                    "                    \"view_count\": null,\n" +
                    "                    \"archived\": false,\n" +
                    "                    \"no_follow\": true,\n" +
                    "                    \"is_crosspostable\": false,\n" +
                    "                    \"pinned\": false,\n" +
                    "                    \"over_18\": false,\n" +
                    "                    \"all_awardings\": [],\n" +
                    "                    \"awarders\": [],\n" +
                    "                    \"media_only\": false,\n" +
                    "                    \"can_gild\": false,\n" +
                    "                    \"spoiler\": false,\n" +
                    "                    \"locked\": false,\n" +
                    "                    \"author_flair_text\": null,\n" +
                    "                    \"treatment_tags\": [],\n" +
                    "                    \"visited\": false,\n" +
                    "                    \"removed_by\": null,\n" +
                    "                    \"num_reports\": null,\n" +
                    "                    \"distinguished\": null,\n" +
                    "                    \"subreddit_id\": \"t5_2qh23\",\n" +
                    "                    \"mod_reason_by\": null,\n" +
                    "                    \"removal_reason\": null,\n" +
                    "                    \"link_flair_background_color\": \"\",\n" +
                    "                    \"id\": \"l7frq2\",\n" +
                    "                    \"is_robot_indexable\": true,\n" +
                    "                    \"report_reasons\": null,\n" +
                    "                    \"author\": \"ReptileSlave\",\n" +
                    "                    \"discussion_type\": null,\n" +
                    "                    \"num_comments\": 0,\n" +
                    "                    \"send_replies\": true,\n" +
                    "                    \"whitelist_status\": \"all_ads\",\n" +
                    "                    \"contest_mode\": false,\n" +
                    "                    \"mod_reports\": [],\n" +
                    "                    \"author_patreon_flair\": false,\n" +
                    "                    \"author_flair_text_color\": null,\n" +
                    "                    \"permalink\": \"/r/test/comments/l7frq2/a/\",\n" +
                    "                    \"parent_whitelist_status\": \"all_ads\",\n" +
                    "                    \"stickied\": false,\n" +
                    "                    \"url\": \"https://www.reddit.com/r/test/comments/l7frq2/a/\",\n" +
                    "                    \"subreddit_subscribers\": 9269,\n" +
                    "                    \"created_utc\": 1611885130.0,\n" +
                    "                    \"num_crossposts\": 0,\n" +
                    "                    \"media\": null,\n" +
                    "                    \"is_video\": false\n" +
                    "                }\n" +
                    "            },\n" +
                    "            {\n" +
                    "                \"kind\": \"t3\",\n" +
                    "                \"data\": {\n" +
                    "                    \"approved_at_utc\": null,\n" +
                    "                    \"subreddit\": \"test\",\n" +
                    "                    \"selftext\": \"\",\n" +
                    "                    \"author_fullname\": \"t2_dq0bd\",\n" +
                    "                    \"saved\": false,\n" +
                    "                    \"mod_reason_title\": null,\n" +
                    "                    \"gilded\": 0,\n" +
                    "                    \"clicked\": false,\n" +
                    "                    \"title\": \"Video post test\",\n" +
                    "                    \"link_flair_richtext\": [],\n" +
                    "                    \"subreddit_name_prefixed\": \"r/test\",\n" +
                    "                    \"hidden\": false,\n" +
                    "                    \"pwls\": 6,\n" +
                    "                    \"link_flair_css_class\": null,\n" +
                    "                    \"downs\": 0,\n" +
                    "                    \"thumbnail_height\": 105,\n" +
                    "                    \"top_awarded_type\": null,\n" +
                    "                    \"hide_score\": false,\n" +
                    "                    \"name\": \"t3_l7douf\",\n" +
                    "                    \"quarantine\": false,\n" +
                    "                    \"link_flair_text_color\": \"dark\",\n" +
                    "                    \"upvote_ratio\": 1.0,\n" +
                    "                    \"author_flair_background_color\": null,\n" +
                    "                    \"subreddit_type\": \"public\",\n" +
                    "                    \"ups\": 1,\n" +
                    "                    \"total_awards_received\": 0,\n" +
                    "                    \"media_embed\": {\n" +
                    "                        \"content\": \"&lt;iframe width=\\\"356\\\" height=\\\"200\\\" src=\\\"https://www.youtube.com/embed/Wu7XIyjrahs?feature=oembed&amp;enablejsapi=1\\\" frameborder=\\\"0\\\" allow=\\\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\\\" allowfullscreen&gt;&lt;/iframe&gt;\",\n" +
                    "                        \"width\": 356,\n" +
                    "                        \"scrolling\": false,\n" +
                    "                        \"height\": 200\n" +
                    "                    },\n" +
                    "                    \"thumbnail_width\": 140,\n" +
                    "                    \"author_flair_template_id\": null,\n" +
                    "                    \"is_original_content\": false,\n" +
                    "                    \"user_reports\": [],\n" +
                    "                    \"secure_media\": {\n" +
                    "                        \"type\": \"youtube.com\",\n" +
                    "                        \"oembed\": {\n" +
                    "                            \"provider_url\": \"https://www.youtube.com/\",\n" +
                    "                            \"version\": \"1.0\",\n" +
                    "                            \"title\": \"Im just here so I hold the line\",\n" +
                    "                            \"type\": \"video\",\n" +
                    "                            \"thumbnail_width\": 480,\n" +
                    "                            \"height\": 200,\n" +
                    "                            \"width\": 356,\n" +
                    "                            \"html\": \"&lt;iframe width=\\\"356\\\" height=\\\"200\\\" src=\\\"https://www.youtube.com/embed/Wu7XIyjrahs?feature=oembed&amp;enablejsapi=1\\\" frameborder=\\\"0\\\" allow=\\\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\\\" allowfullscreen&gt;&lt;/iframe&gt;\",\n" +
                    "                            \"author_name\": \"2MinuteTwilightZone\",\n" +
                    "                            \"provider_name\": \"YouTube\",\n" +
                    "                            \"thumbnail_url\": \"https://i.ytimg.com/vi/Wu7XIyjrahs/hqdefault.jpg\",\n" +
                    "                            \"thumbnail_height\": 360,\n" +
                    "                            \"author_url\": \"https://www.youtube.com/user/2MinuteTwilightZone\"\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                    \"is_reddit_media_domain\": false,\n" +
                    "                    \"is_meta\": false,\n" +
                    "                    \"category\": null,\n" +
                    "                    \"secure_media_embed\": {\n" +
                    "                        \"content\": \"&lt;iframe width=\\\"356\\\" height=\\\"200\\\" src=\\\"https://www.youtube.com/embed/Wu7XIyjrahs?feature=oembed&amp;enablejsapi=1\\\" frameborder=\\\"0\\\" allow=\\\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\\\" allowfullscreen&gt;&lt;/iframe&gt;\",\n" +
                    "                        \"width\": 356,\n" +
                    "                        \"scrolling\": false,\n" +
                    "                        \"media_domain_url\": \"https://www.redditmedia.com/mediaembed/l7douf\",\n" +
                    "                        \"height\": 200\n" +
                    "                    },\n" +
                    "                    \"link_flair_text\": null,\n" +
                    "                    \"can_mod_post\": false,\n" +
                    "                    \"score\": 1,\n" +
                    "                    \"approved_by\": null,\n" +
                    "                    \"author_premium\": false,\n" +
                    "                    \"thumbnail\": \"https://a.thumbs.redditmedia.com/ikyjr4UkKWTqVzjFBTU153fU_YywmWvbiMg5AMd30U4.jpg\",\n" +
                    "                    \"edited\": false,\n" +
                    "                    \"author_flair_css_class\": null,\n" +
                    "                    \"author_flair_richtext\": [],\n" +
                    "                    \"gildings\": {},\n" +
                    "                    \"post_hint\": \"rich:video\",\n" +
                    "                    \"content_categories\": null,\n" +
                    "                    \"is_self\": false,\n" +
                    "                    \"mod_note\": null,\n" +
                    "                    \"created\": 1611908481.0,\n" +
                    "                    \"link_flair_type\": \"text\",\n" +
                    "                    \"wls\": 6,\n" +
                    "                    \"removed_by_category\": null,\n" +
                    "                    \"banned_by\": null,\n" +
                    "                    \"author_flair_type\": \"text\",\n" +
                    "                    \"domain\": \"youtu.be\",\n" +
                    "                    \"allow_live_comments\": false,\n" +
                    "                    \"selftext_html\": null,\n" +
                    "                    \"likes\": null,\n" +
                    "                    \"suggested_sort\": null,\n" +
                    "                    \"banned_at_utc\": null,\n" +
                    "                    \"url_overridden_by_dest\": \"https://imgur.com/asdai121a\",\n" +
                    "                    \"view_count\": null,\n" +
                    "                    \"archived\": false,\n" +
                    "                    \"no_follow\": true,\n" +
                    "                    \"is_crosspostable\": false,\n" +
                    "                    \"pinned\": false,\n" +
                    "                    \"over_18\": false,\n" +
                    "                    \"preview\": {\n" +
                    "                        \"images\": [\n" +
                    "                            {\n" +
                    "                                \"source\": {\n" +
                    "                                    \"url\": \"https://external-preview.redd.it/jTtqTDzUB_cC9lcNPdNX3NgKDxUnOM9xKO_ikucQB-E.jpg?auto=webp&amp;s=1f24d3f2ae6b062e07db460336cc306b78679a55\",\n" +
                    "                                    \"width\": 480,\n" +
                    "                                    \"height\": 360\n" +
                    "                                },\n" +
                    "                                \"resolutions\": [\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/jTtqTDzUB_cC9lcNPdNX3NgKDxUnOM9xKO_ikucQB-E.jpg?width=108&amp;crop=smart&amp;auto=webp&amp;s=f32be810dcbf2325b979f08dbd787ef9144f9fc0\",\n" +
                    "                                        \"width\": 108,\n" +
                    "                                        \"height\": 81\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/jTtqTDzUB_cC9lcNPdNX3NgKDxUnOM9xKO_ikucQB-E.jpg?width=216&amp;crop=smart&amp;auto=webp&amp;s=505e8b1bb470a4a75a3dd60c5d8e5baaf20ed2fe\",\n" +
                    "                                        \"width\": 216,\n" +
                    "                                        \"height\": 162\n" +
                    "                                    },\n" +
                    "                                    {\n" +
                    "                                        \"url\": \"https://external-preview.redd.it/jTtqTDzUB_cC9lcNPdNX3NgKDxUnOM9xKO_ikucQB-E.jpg?width=320&amp;crop=smart&amp;auto=webp&amp;s=cbcc4d22f3b5f89b04c7974143b08d42191c8139\",\n" +
                    "                                        \"width\": 320,\n" +
                    "                                        \"height\": 240\n" +
                    "                                    }\n" +
                    "                                ],\n" +
                    "                                \"variants\": {},\n" +
                    "                                \"id\": \"3xsBycKhEKLjtpkZmeX1r6FUo93WZNLjnOpg61sEvRo\"\n" +
                    "                            }\n" +
                    "                        ],\n" +
                    "                        \"enabled\": false\n" +
                    "                    },\n" +
                    "                    \"all_awardings\": [],\n" +
                    "                    \"awarders\": [],\n" +
                    "                    \"media_only\": false,\n" +
                    "                    \"can_gild\": false,\n" +
                    "                    \"spoiler\": false,\n" +
                    "                    \"locked\": false,\n" +
                    "                    \"author_flair_text\": null,\n" +
                    "                    \"treatment_tags\": [],\n" +
                    "                    \"visited\": false,\n" +
                    "                    \"removed_by\": null,\n" +
                    "                    \"num_reports\": null,\n" +
                    "                    \"distinguished\": null,\n" +
                    "                    \"subreddit_id\": \"t5_2qh23\",\n" +
                    "                    \"mod_reason_by\": null,\n" +
                    "                    \"removal_reason\": null,\n" +
                    "                    \"link_flair_background_color\": \"\",\n" +
                    "                    \"id\": \"l7douf\",\n" +
                    "                    \"is_robot_indexable\": true,\n" +
                    "                    \"report_reasons\": null,\n" +
                    "                    \"author\": \"mammolastan\",\n" +
                    "                    \"discussion_type\": null,\n" +
                    "                    \"num_comments\": 0,\n" +
                    "                    \"send_replies\": true,\n" +
                    "                    \"whitelist_status\": \"all_ads\",\n" +
                    "                    \"contest_mode\": false,\n" +
                    "                    \"mod_reports\": [],\n" +
                    "                    \"author_patreon_flair\": false,\n" +
                    "                    \"author_flair_text_color\": null,\n" +
                    "                    \"permalink\": \"/r/test/comments/l7douf/video_post/\",\n" +
                    "                    \"parent_whitelist_status\": \"all_ads\",\n" +
                    "                    \"stickied\": false,\n" +
                    "                    \"url\": \"https://youtu.be/Wu7XIyjrahs\",\n" +
                    "                    \"subreddit_subscribers\": 9269,\n" +
                    "                    \"created_utc\": 1611879681.0,\n" +
                    "                    \"num_crossposts\": 0,\n" +
                    "                    \"media\": {\n" +
                    "                        \"type\": \"youtube.com\",\n" +
                    "                        \"oembed\": {\n" +
                    "                            \"provider_url\": \"https://www.youtube.com/\",\n" +
                    "                            \"version\": \"1.0\",\n" +
                    "                            \"title\": \"Im just here so I hold the line\",\n" +
                    "                            \"type\": \"video\",\n" +
                    "                            \"thumbnail_width\": 480,\n" +
                    "                            \"height\": 200,\n" +
                    "                            \"width\": 356,\n" +
                    "                            \"html\": \"&lt;iframe width=\\\"356\\\" height=\\\"200\\\" src=\\\"https://www.youtube.com/embed/Wu7XIyjrahs?feature=oembed&amp;enablejsapi=1\\\" frameborder=\\\"0\\\" allow=\\\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\\\" allowfullscreen&gt;&lt;/iframe&gt;\",\n" +
                    "                            \"author_name\": \"2MinuteTwilightZone\",\n" +
                    "                            \"provider_name\": \"YouTube\",\n" +
                    "                            \"thumbnail_url\": \"https://i.ytimg.com/vi/Wu7XIyjrahs/hqdefault.jpg\",\n" +
                    "                            \"thumbnail_height\": 360,\n" +
                    "                            \"author_url\": \"https://www.youtube.com/user/2MinuteTwilightZone\"\n" +
                    "                        }\n" +
                    "                    },\n" +
                    "                    \"is_video\": false\n" +
                    "                }\n" +
                    "            }\n" +
                    "        ],\n" +
                    "        \"after\": \"t3_l7douf\",\n" +
                    "        \"before\": \"t3_l7qci1\"\n" +
                    "    }\n" +
                    "}";

}