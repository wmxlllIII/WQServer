package com.example.test.server.mapper;


import com.example.test.pojo.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("select * from users where email = #{email}")
    User getByEmail(String email);

    @Select("select * from users where phone = #{phone}")
    User getByPhone(String phone);

    @Select("select * from users where uu_number = #{uuNumber}")
    User getByUuNumber(long uuNumber);

    @Insert("insert into users ( email, password, username,uu_number, avatar_url, email_verified) VALUES " +
            "(#{email},#{password},#{username},#{uuNumber},#{avatarUrl},#{emailVerified})")
    void insert(User user);

    void updateAvatar(@Param("userId") long currentId, @Param("avatarUrl") String avatarUrl);

    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into friend_relationships (sender_id, receiver_id, status, valid_msg) values (#{senderId},#{receiverId},#{status},#{validMsg})")
    void saveFriReq(FriendRelationship friendRelationship);

    boolean existsPendingApply(@Param("userId") long currentId, @Param("friendId") long targetuuNumber);

    void requestSended(@Param("userId") long currentId, @Param("friendId") long targetuuNumber);

    void requestAllSended(@Param("userId") String currentId);

    void updateStateAgree(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);

    void updateStateReject(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);

    @Insert("insert into friends (user_uuid, friend_uuid) values (#{currentId}, #{uuNumber})")
    void insertFriend(@Param("uuNumber") long uuNumber, @Param("currentId") long currentId);


    @Select("select * from offline_messages where receiver_id=#{uuNumber} and msg_type=#{msgType}")
    List<OfflineMsg> getPendingRelas(@Param("uuNumber") long uuNumber, @Param("msgType") int msgType);

    @Select("select * from friend_relationships where receiver_id=#{uuNumber} or sender_id=#{uuNumber}")
    List<FriendRelationship> getAllRela(@Param("uuNumber") long currentId);

    @Select("select * from friend_relationships where sender_id=#{senderId} and receiver_id =#{receiverId} ")
    FriendRelationship getTargetRela(@Param("senderId") long senderId, @Param("receiverId") long receiverId);

    @Select("select * from friends where user_uuid=#{userId} or friend_uuid=#{userId}")
    List<Friend> getAllFriends(@Param("userId") long currentId);


    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("insert into message_history ( sender_id, receiver_id, content,type) values (#{senderId},#{receiverId},#{content},#{type})")
    void saveHistoryMsg(Msg msg);

    @Select("select * from message_history where id=#{id}")
    Msg getMsg(@Param("id") int id);

    @Insert("insert into offline_messages (msg_id, msg_type,sender_id, receiver_id ) values (#{id},#{msgType},#{senderId},#{receiverId})")
    void saveOfflineMsg(@Param("id") int id, @Param("msgType") int msgType, @Param("senderId") long senderId, @Param("receiverId") long receiverId);


    @Select("select * from offline_messages where receiver_id=#{userId} and msg_type=#{msgType}")
    List<OfflineMsg> getPendingMsg(@Param("userId") long receiverId, @Param("msgType") int msgType);

    @Delete("delete from offline_messages WHERE msg_id = #{id}")
    void deletePendingMsg(@Param("id") int messageId);

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
    @Insert("insert into posts (user_id, content,title, like_count) values (#{userId},#{content},#{title},#{likeCount})")
    void savePost(Post post);

    @Insert("insert into post_images (post_id, image_url, serial_num) values (#{postId},#{imageUrl},#{serialNum})")
    void savePostImages(PostImages postImages);

    @Select("select * from posts order by create_at desc limit #{offset}, #{limit}")
    List<Post> getPosts(@Param("offset") int offset, @Param("limit") int limit);

    List<PostImages> getImagesByPostIds(@Param("postId") List<Integer> postId);

    @Select("select * from comments where post_id = #{postId} and parent_id = -1 order by create_at desc limit #{size} offset #{offset}")
    List<Comment> getCommentByPostId(@Param("postId") int postId, @Param("size") int size, @Param("offset") int offset);

    @Insert("insert into comments (post_id, user_id, parent_id, reply_to_user_id, content) values (#{postId},#{userId},#{parentId},#{replyToUserId},#{content})")
    void addComment(Comment comment);

    List<Comment> getChildComments(@Param("parentIdList") List<Integer> parentIdList);

    @Select("select * from posts where user_id =#{user_id} order by create_at desc limit #{offset},#{limit}")
    List<Post> getMyPosts(@Param("user_id") long user_id, @Param("offset") int offset, @Param("limit") int limit);

    @Select("select * from posts where posts.id = #{postId}")
    Post getPostById(@Param("postId") int postId);

    @Select("SELECT * FROM message_history WHERE ((sender_id = #{currentId} AND receiver_id = #{targetId}) OR (sender_id = #{targetId} AND receiver_id = #{currentId})) ORDER BY create_at DESC LIMIT #{offset}, #{limit}")
    List<Msg> getUserMsg(@Param("currentId") long currentId, @Param("targetId") long targetId, @Param("offset") int offset, @Param("limit") int i);
}
