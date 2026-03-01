package com.example.test.server.mapper;

import com.example.test.pojo.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from auth where auth_type = #{authType} and auth_value = #{authValue}")
    Auth getAuth(@Param("authType") int authType, @Param("authValue") String authValue);

    @Select("select * from auth where auth_type = #{authType} and user_id = #{userId}")
    Auth getAuthById(@Param("authType") int authType, @Param("userId") long userId);

    @Select("select * from users where uu_number = #{uuNumber}")
    User getByUuNumber(long uuNumber);

    @Insert("insert into users (username,uu_number, avatar_url) VALUES " +
            "(#{username},#{uuNumber},#{avatarUrl})")
    void insertUser(User user);

    @Insert("insert into auth (user_id, auth_type, auth_value, password) VALUES " +
            "(#{userId},#{authType},#{authValue},#{password})")
    void insertAuth(Auth auth);

    void updateAvatar(@Param("userId") long currentId, @Param("avatarUrl") String avatarUrl);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into friend_relationships (sender_id, receiver_id, status, valid_msg) values (#{senderId},#{receiverId},#{status},#{validMsg})")
    void saveFriReq(FriendRelationship friendRelationship);

    boolean existsPendingApply(@Param("userId") long currentId, @Param("friendId") long targetuuNumber);

    void requestSended(@Param("userId") long currentId, @Param("friendId") long targetuuNumber);

    void requestAllSended(@Param("userId") String currentId);

    void updateStateAgree(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);

    void updateStateReject(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);

    @Insert("insert into friend_relationships (sender_id, receiver_id,status) values (#{currentId}, #{uuNumber})")
    void insertFriend(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);

    @Select("select * from friend_relationships where receiver_id=#{uuNumber} or sender_id=#{uuNumber}")
    List<FriendRelationship> getAllRela(@Param("uuNumber") long currentId);

    @Select("select * from friend_relationships where sender_id=#{senderId} and receiver_id =#{receiverId} ")
    FriendRelationship getTargetRela(@Param("senderId") long senderId, @Param("receiverId") long receiverId);

    @Select("select * from friend_relationships where sender_id=#{userId} or receiver_id=#{userId} and status = #{status} ")
    List<FriendRelationship> getAllFriends(@Param("userId") long currentId,@Param("status")int status);


    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into message_history ( sender_id, chat_id, content,chat_type) values (#{senderId},#{chat_id},#{content},#{chat_type})")
    void saveHistoryMsg(Msg msg);

    @Select("select * from message_history where id=#{id}")
    Msg getMsg(@Param("id") int id);

    @Insert("insert into conversation_read (user_id, chat_id, chat_type, last_msg_id) values (#{userId},#{chatId},#{chatType}, 0)")
    void insertConversationRead(ConversationRead conversationRead);

    @Select("SELECT * FROM conversation_read WHERE user_id = #{userId}")
    List<ConversationRead> getUserConversations(@Param("userId") long userId);

    @Select("SELECT * FROM message_history WHERE chat_type = #{chatType} AND chat_id = #{chatId} AND id > #{lastMsgId} ORDER BY create_at ASC")
    List<Msg> selectUnreadMessages(@Param("chatType") int chatType, @Param("chatId") long chatId, @Param("lastMsgId") int lastMsgId);

    @Update("UPDATE conversation_read SET last_msg_id = #{lastMsgId} WHERE user_id = #{userId} AND chat_id = #{chatId}")
    void updateLastMsgId(@Param("userId") long userId, @Param("chatId") long chatId, @Param("lastMsgId") int lastMsgId);

    @Select("SELECT * FROM friend_relationships WHERE (sender_id = #{userId} or receiver_id = #{userId}) AND status = #{status}")
    List<FriendRelationship> getPendingFriendRequests(@Param("userId")long userId, @Param("status")int status);


    @Select("select * from movies")
    List<Movie> getAllMovies();

    @Select("select * from movies where id=#{movieId}")
    Movie getMovieById(@Param("movieId") int movieId);

    @Select("select * from rooms")
    List<Room> getAllRooms();

    @Insert("insert into rooms (room_id, movie_id) values (#{roomId},#{movieId})")
    void saveRoom(@Param("roomId") String roomId, @Param("movieId") int movieId);

    @Delete("delete from rooms where room_id=#{roomId}")
    void deleteRoom(@Param("roomId") String roomId);

    @Update("update users set username=#{userName} where uu_number=#{userId}")
    void updateUserInfo(@Param("userId") long userId, @Param("userName") String userName);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into posts (user_id, content, title) values (#{userId},#{content},#{title})")
    void savePost(Post post);

    @Insert("insert into post_images (post_id, image_url, serial_num) values (#{postId},#{imageUrl},#{serialNum})")
    void savePostImages(PostImages postImages);

    @Select("select * from posts order by create_at desc limit #{offset}, #{limit}")
    List<Post> getPosts(@Param("offset") int offset, @Param("limit") int limit);

    List<PostImages> getImagesByPostIds(@Param("postId") List<Integer> postId);

    List<Integer> getLikedPostIdsByUser(@Param("postIds") List<Integer> postIds, @Param("userId") Long userId);

    @Select("select * from comments where post_id = #{postId} and parent_id = -1 order by create_at desc limit #{size} offset #{offset}")
    List<Comment> getCommentByPostId(@Param("postId") int postId, @Param("size") int size, @Param("offset") int offset);

    @Insert("insert into comments (post_id, user_id, parent_id, reply_to_user_id, content) values (#{postId},#{userId},#{parentId},#{replyToUserId},#{content})")
    void addComment(Comment comment);

    List<Comment> getChildComments(@Param("parentIdList") List<Integer> parentIdList);

    @Select("select * from posts where user_id =#{user_id} order by create_at desc limit #{offset},#{limit}")
    List<Post> getMyPosts(@Param("user_id") long user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from posts where id = #{postId}")
    Post getPostById(@Param("postId") int postId);

    @Select("SELECT * FROM message_history WHERE ((sender_id = #{currentId} AND chat_id = #{targetId}) OR (sender_id = #{targetId} AND chat_id = #{currentId})) ORDER BY create_at DESC LIMIT #{offset}, #{limit}")
    List<Msg> getUserMsg(@Param("currentId") long currentId, @Param("targetId") long targetId, @Param("offset") int offset, @Param("limit") int i);

    @Insert("insert into follow (follower_id, followed_id) values (#{currentId},#{targetId})")
    void followUser(@Param("currentId") long currentId, @Param("targetId") long targetId);

    @Delete("delete from follow where follower_id = #{currentId} and followed_id = #{targetId}")
    void cancelFollowUser(@Param("currentId") long currentId, @Param("targetId") long targetId);

    @Delete("delete from friend_relationships where sender_id = #{currentId} and receiver_id = #{targetId} or sender_id = #{targetId} and receiver_id = #{currentId} and status = 1")
    boolean deleteFriend(@Param("currentId") long currentId, @Param("targetId") long targetId);

    @Select("select followed_id from follow where follower_id = #{currentId}")
    List<Long> getFollowedUserIds(@Param("currentId") long currentId);


    List<Post> getPostsByUserIds(@Param("userIds") List<Long> userIds,
                                 @Param("offset") int offset,
                                 @Param("limit") int limit);

    @Select("select * from movie_cate")
    List<MovieCategory> getMovieCategory();

    @Select("select * from movie_history where user_id = #{userId} and movie_id = #{movieId}")
    MovieHistory findByUserIdAndMovieId(@Param("userId") long userId, @Param("movieId") int movieId);

    @Insert("INSERT INTO movie_history (movie_id, user_id, watch_count, progress) " +
            "VALUES (#{movieId}, #{userId}, #{watchCount}, #{progress})")
    void insertMovieProgress(MovieHistory newHistory);

    @Update("UPDATE movie_history SET progress = #{progress} WHERE user_id = #{userId} AND movie_id = #{movieId}")
    void updateMovieProgress(MovieHistory existingHistory);

    @Select("SELECT * FROM movie_history WHERE user_id = #{userId}")
    List<MovieHistory> getWatchHistory(@Param("userId") long userId);

    @Select("SELECT * FROM actor WHERE actor_id = #{actorId}")
    Actor getActorProfile(@Param("actorId") int actorId);

    @Select("SELECT * FROM post_likes WHERE user_id = #{userId}")
    List<PostLike> getLikePost(@Param("userId") long userId);

    @Select("SELECT * FROM footprint WHERE user_id = #{userId}")
    List<FootPrint> getFootprintPost(@Param("userId") long userId);

    @Select("SELECT * FROM post_likes WHERE user_id = #{userId} AND post_id = #{postId}")
    PostLike selectExistLikePost(@Param("userId") long userId, @Param("postId") int postId);

    @Insert("INSERT INTO post_likes (user_id, post_id) VALUES (#{userId}, #{postId})")
    void insertLikePost(@Param("userId") long userId, @Param("postId") int postId);

    @Delete("DELETE FROM post_likes WHERE user_id = #{userId} AND post_id = #{postId}")
    void deleteLikePost(@Param("userId") long userId, @Param("postId") int postId);

}
