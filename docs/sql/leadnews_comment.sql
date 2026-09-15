-- 评论服务MongoDB初始化脚本
-- 在MongoDB中执行以下命令

-- 创建数据库
use leadnews-comment;

-- 创建评论集合
db.createCollection("ap_comment");

-- 创建评论索引
db.ap_comment.createIndex({ "articleId": 1 });
db.ap_comment.createIndex({ "authorId": 1 });
db.ap_comment.createIndex({ "createdTime": -1 });

-- 创建评论回复集合
db.createCollection("ap_comment_reply");

-- 创建评论回复索引
db.ap_comment_reply.createIndex({ "commentId": 1 });
db.ap_comment_reply.createIndex({ "authorId": 1 });
db.ap_comment_reply.createIndex({ "createdTime": -1 });

-- 示例文档结构（仅供参考，实际使用时可插入测试数据）
/*
评论文档示例：
{
    _id: ObjectId("..."),
    articleId: "文章ID",
    authorId: 1,
    authorName: "用户名",
    content: "评论内容",
    likes: 0,
    reply: 0,
    createdTime: ISODate("2024-01-01T00:00:00Z"),
    updatedTime: ISODate("2024-01-01T00:00:00Z"),
    status: 1,
    commentId: null
}

回复文档示例：
{
    _id: ObjectId("..."),
    commentId: "评论ID",
    authorId: 1,
    authorName: "用户名",
    content: "回复内容",
    likes: 0,
    createdTime: ISODate("2024-01-01T00:00:00Z"),
    updatedTime: ISODate("2024-01-01T00:00:00Z")
}
*/
