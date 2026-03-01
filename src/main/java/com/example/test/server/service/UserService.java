package com.example.test.server.service;

import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.pojo.dto.*;
import com.example.test.pojo.entity.Comment;
import com.example.test.pojo.entity.Movie;
import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.*;
import com.example.test.pojo.dto.SaveRoomDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    void getCode(SendCodeDTO sendCodeDTO);

    User register(RegisterDTO registerDTO);

    User login(UserLoginDTO userLoginDTO);

    UserLoginVO autoLogin();

    UserVO updateAvatar(AvatarUploadDTO avatarUploadDTO);

    SearchUserVO searchUser(SearchUserDTO searchUserDTO);

    String FriendApply(FriendApplyDTO friendApplyDTO);

    boolean deleteFriend(DeleteFriendDTO dto);

    HandleFriendRequestVO handleResponse(HandleFriendRequestDTO friendRequestDTO);

    List<Map<String, Object>> getAllFriendRequest();

    List<FriendInfoVO> getAllFriends();

    List<MsgVO> handleMsg(MsgDTO msgDTO);

    List<MovieVO> getMovies();

    List<RoomVO> getRooms();

    void saveRoom(SaveRoomDTO roomDTO);

    void removeRoom(RemoveRoomDTO removeRoomDTO);

    UserVO updateUserInfo(UpdateUserinfoDTO updateUserInfoDTO);

    int saveShareMessage(ShareMessageDTO shareDTO);

    //    PostsVO publishPost(OssPostsDTO postsDTO);
    PostsVO publishPost(PostDTO postDTO);

    PageResult<PostsVO> getPosts(PostsQueryDTO postsQueryDTO);

    PageResult<GetCommentVO> getComment(GetCommentDTO commentDTO);

    Result<Comment> addComment(AddCommentDTO addCommentDTO);

    PageResult<PostsVO> getMyPosts(PostsQueryDTO postsQueryDTO);

    PageResult<PostsVO> getFollowerPost(PostsQueryDTO postsQueryDTO);

    StsVO getSts();

    PageResult<MsgVO> getMsg(GetMsgDTO getMsgDTO);

    FollowUserVO followUser(FollowUserDTO dto);

    UnFollowUserVO unFollowUser(FollowUserDTO dto);

    List<MovieCateVO> getMovieCategory();

    void saveMovieProgress(SaveProgressDTO dto);

    List<MovieHistoryVO> getWatchHistory();

    ActorProfileVO getActorProfile(ActorProfileDTO dto);

    List<PostsVO> getLikePost();

    List<PostsVO> getFootprintPost();

    Boolean likePostIfNeed(LikePostDTO dto);
}
