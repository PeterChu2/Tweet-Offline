package chu.ForCHUApps.tweetoffline.models;

/**
 * Created by peter on 25/08/15.
 */
public class User {
    private String mProfilePicURL;
    private String mUsername;
    private String mName;
    private String mRecentTweet;
    private String mBio;

    public User(String username, String name, String recentTweet, String bio, String profilePicURL) {
        mProfilePicURL = profilePicURL;
        mName = name;
        mUsername = username;
        mRecentTweet = recentTweet;
        mBio = bio;
    }

    public User(String username, String name) {
        this(username, name, null, null, null);
    }

    public String getUsername() {
        return mUsername;
    }

    public String getName() {
        if(mName == null) {
            return "";
        }
        return mName;
    }

    public String getRecentTweet() {
        if(mRecentTweet == null) {
            return "";
        }
        return mRecentTweet;
    }

    public String getBio() {
        if(mBio == null) {
            return "";
        }
        return mBio;
    }

    public String getProfilePicURL() {
        if(mProfilePicURL == null) {
            return "";
        }
        return mProfilePicURL;
    }
}
