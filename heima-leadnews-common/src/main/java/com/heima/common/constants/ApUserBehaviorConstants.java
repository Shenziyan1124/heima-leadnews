package com.heima.common.constants;

public class ApUserBehaviorConstants {


    public final static Short LIKE = 0; // 点赞
    public final static Short CANCEL_LIKE = 1; // 取消点赞

    public final static Short DELETE = 1; // 删除
    public final static Short CANCEL_DELETE = 0; // 取消删除

    // 点赞 redis key
    public final static String LIKES_ARTICLE_KEY = "likes:article:"; // 文章点赞key
    public final static String LIKES_USER_KEY = "likes:user:"; // 用户点赞key

    // 点赞类型
    public final static Short LIKES_ARTICLE_TYPE = 0; // 文章
    public final static Short LIKES_DYNAMIC_TYPE = 1; // 动态
    public final static Short LIKES_COMMENT_TYPE = 2; // 评论

    // 点赞kafka topic
    public final static String LIKES_KAFKA_TOPIC = "likes_topic";

    // --------------------------------
    // 阅读 redis key
    public final static String READ_COUNT_KEY = "read_count:article:";
    // 阅读kafka topic
    public final static String READ_KAFKA_TOPIC = "read_topic";


    // -------------------------------
    // 不喜欢
    public static final short UN_LIKE = 2;      // 不喜欢
    public static final short CANCEL_UN_LIKE = 3; // 取消不喜欢

    // 不喜欢 redis key
    public final static String UN_LIKE_ARTICLE_KEY = "un_like:article:"; // 文章不喜欢key
    public final static String UN_LIKE_USER_KEY = "un_like:user:"; // 用户不喜欢key


}
