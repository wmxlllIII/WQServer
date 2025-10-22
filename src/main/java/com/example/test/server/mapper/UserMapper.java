package com.example.test.server.mapper;


import com.example.test.pojo.entity.*;
import org.apache.ibatis.annotations.*;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from users where email = #{email}")
    User getByEmail(String email);

    @Select("select * from users where phone = #{phone}")
    User getByPhone(String phone);

    @Select("select * from users where uu_number = #{uuNumber}")
    User getByUuNumber(BigInteger uuNumber);

    @Select("select * from users where uuid = #{uuid}")
    User getById(String uuId);

    @Insert("insert into users (uuid, email, password, username,uu_number, avatar_url, email_verified) VALUES " +
            "(#{uuid},#{email},#{password},#{username},#{uuNumber},#{avatarUrl},#{emailVerified})")
    void insert(User user);

    void updateAvatar(@Param("userId") String currentId, @Param("avatarUrl") String avatarUrl);


    void requestFriend(@Param("userId") String currentId, @Param("validMsg") String validMsg, @Param("friendId") String targetUuId);

    boolean existsPendingApply(@Param("userId") String currentId, @Param("friendId") String targetUuId);

    void requestSended(@Param("userId") String currentId, @Param("friendId") String targetUuId);

    void requestAllSended(@Param("userId") String currentId);

    @Select("select status from friend_relationships where sender_id = #{currentId} and receiver_id = #{uuid}")
    String getState(@Param("currentId") String currentId, @Param("uuid") String uuid);


    void updateStateAgree(@Param("uuid") String uuid, @Param("currentId") String currentId);

    void updateStateReject(@Param("uuid") String uuid, @Param("currentId") String currentId);

    @Insert("insert into friends (user_uuid, friend_uuid) values (#{currentId}, #{uuid})")
    void addFriend(@Param("uuid") String uuid, @Param("currentId") String currentId);


    @Select("select * from friend_relationships where receiver_id=#{uuid} and status='pending' ")
    List<FriendRelationship> getPendingRelas(@Param("uuid") String uuid);

    @Select("select * from friend_relationships where receiver_id=#{uuid} or sender_id=#{uuid}")
    List<FriendRelationship> getAllRela(@Param("uuid") String currentId);

    @Select("select * from friend_relationships where sender_id=#{userId} and receiver_id =#{friengId} ")
    List<FriendRelationship> getTargetRela(@Param("userId") String currentId, @Param("friendId") String targetUuId);

    @Select("select * from friends where user_uuid=#{userId} or friend_uuid=#{userId}")
    List<Friend> getAllFriends(@Param("userId") String currentId);


    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into message_history (session_id, sender_id, receiver_id, content,type) values (#{sessionId},#{senderId},#{receiverId},#{content},#{type})")
    void saveHistoryMsg(Msg msg);

    @Select("select * from message_history where id=#{id}")
    List<Msg> getAllMsg(@Param("id") int id);

    @Insert("insert into offline_messages (message_id, sender_id, receiver_id ) values (#{id},#{senderId},#{receiverId})")
    void saveOfflineMsg(@Param("id") int id, @Param("senderId") String senderId, @Param("receiverId") String receiverId);


    @Select("select * from offline_messages where receiver_id=#{userId}")
    List<OfflineMsg> getAllOfflineMsg(@Param("userId") String receiverId);

    @Delete("delete from offline_messages WHERE message_id = #{id}")
    void deleteMsg(@Param("id") int messageId);

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

    @Update("update users set username=#{userName} where uuid=#{userId}")
    void updateUserInfo(@Param("userId") String userId, @Param("userName") String userName);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into posts (user_id, content, like_count) values (#{userId},#{content},#{likeCount})")
    void savePost(Post post);

    @Insert("insert into post_images (post_id, image_url, serial_num) values (#{postId},#{imageUrl},#{serialNum})")
    void savePostImages(PostImages postImages);

    @Select("select * from posts order by create_at desc limit #{offset}, #{limit}")
    List<Post> getPosts(@Param("offset") int offset, @Param("limit") int limit);

    List<PostImages> getImagesByPostIds(@Param("postId") List<Integer> postId);

    @Select("select * from comments where post_id = #{postId} and parent_id = -1 order by create_at desc limit #{size} offset #{offset}")
    List<Comment> getCommentByPostId(@Param("postId") int postId,@Param("size") int size,@Param("offset") int offset);

    @Insert("insert into comments (post_id, user_id, parent_id, reply_to_user_id, content) values (#{postId},#{userId},#{parentId},#{replyToUserId},#{content})")
    void addComment(Comment comment);

    List<Comment> getChildComments(@Param("parentIdList") List<Integer> parentIdList);

    @Select("select * from posts where user_id =#{user_id} order by create_at desc limit #{offset},#{limit}")
    List<Post> getMyPosts(@Param("user_id") String user_id,@Param("offset") int offset, @Param("limit") int limit);
}
