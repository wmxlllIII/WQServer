package com.example.test.server.service;

import com.example.test.common.result.PageResult;
import com.example.test.common.result.Result;
import com.example.test.pojo.dto.*;
import com.example.test.pojo.entity.Comment;
import com.example.test.pojo.entity.Movie;
import com.example.test.pojo.entity.Msg;
import com.example.test.pojo.entity.User;
import com.example.test.pojo.vo.*;
import com.example.test.pojo.dto.SaveRoomDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    void getCode(SendCodeDTO sendCodeDTO);

    User register(RegisterDTO registerDTO);

    User login(UserLoginDTO userLoginDTO);
    User autoLogin(Object obj);


    String updateAvatar(AvatarUploadDTO avatarUploadDTO);

    User searchUser(SearchUserDTO searchUserDTO);

    String FriendApply(FriendApplyDTO friendApplyDTO);

    HandleFriendRequestVO handleResponse(HandleFriendRequestDTO friendRequestDTO);

    List<Map<String, Object>> getAllFriendRequest();

    List<FriendInfoVO> getAllFriends();

    List<MsgVO> handleMsg(MsgDTO msgDTO);

    List<Movie> getMovies();

    List<RoomVO> getRooms();

    void saveRoom(SaveRoomDTO roomDTO);

    void removeRoom(RemoveRoomDTO removeRoomDTO);

    void updateUserInfo(UpdateUserInfoDTO updateUserInfoDTO);

    int saveShareMessage(ShareMessageDTO shareDTO);

    PostsVO publishPost(PostsDTO postsDTO);

    PageResult<PostsVO> getPosts(PostsQueryDTO postsQueryDTO);

    PageResult<GetCommentVO> getComment(GetCommentDTO commentDTO);

    Result<Comment> addComment(AddCommentDTO addCommentDTO);

    PageResult<PostsVO> getMyPosts(PostsQueryDTO postsQueryDTO);

    StsVO getSts();

    PageResult<MsgVO> getMsg(GetMsgDTO getMsgDTO);
}
